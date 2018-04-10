package mapmaker;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.io.UnsupportedEncodingException;

public class Data
{
    OptionFile of;
    Map<Integer,Player> players;
    Map<Integer,Team> teams;
    Map<Integer,Squad> squads;
    Map<Integer,Player> freeAgents;

    public Data(OptionFile of) {
        this.of = of;
        players = new HashMap<Integer,Player>();
        teams = new HashMap<Integer,Team>();
        squads = new HashMap<Integer,Squad>();
        freeAgents = new HashMap<Integer,Player>();
    }

    private int fourBytesToInt(byte[] data, int offset) {
        int value = data[offset] & 0xFF;
        value |= (data[offset+1] << 8) & 0xFFFF;
        value |= (data[offset+2] << 16) & 0xFFFFFF;
        value |= (data[offset+3] << 24) & 0xFFFFFFFF;
        return value;
    }

    private int twoBytesToInt(byte[] data, int offset) {
        int value = data[offset] & 0xFF;
        value |= (data[offset+1] << 8) & 0xFFFF;
        return value;
    }

    private String bytesUtf16ToString(byte[] data, int offset, int len) {
        try {
            String s = new String(data, offset, len, "UTF-16LE");
            int zeroIndex = s.indexOf('\0');
            if (zeroIndex != -1) {
                s = s.substring(0, zeroIndex);
            }
            return s;
        }
        catch (UnsupportedEncodingException e) {
            return "<**BAD*UTF16*STRING*>";
        }
    }

    private String bytesUtf8ToString(byte[] data, int offset, int len) {
        try {
            String s = new String(data, offset, len, "UTF-8");
            int zeroIndex = s.indexOf('\0');
            if (zeroIndex != -1) {
                s = s.substring(0, zeroIndex);
            }
            return s;
        }
        catch (UnsupportedEncodingException e) {
            return "<**BAD*UTF8*STRING*>";
        }
    }

    private void initClubTeams(OptionFile of, Map<Integer,Team> map) {
        int s = fourBytesToInt(of.data, 0x174);
        int e = fourBytesToInt(of.data, 0x178);
        int n = (e - s)/0x8c;

        for (int i=0; i<n; i++) {
            String name = bytesUtf8ToString(of.data, s + i*0x8c, 0x31);
            if (name.equals("")) {
                name = bytesUtf8ToString(of.data, s + i*0x8c + 0x81, 0x10);
            }
            String abbr = bytesUtf8ToString(of.data, s + i*0x8c + 0x49, 3);
            if (abbr.equals("")) {
                abbr = bytesUtf8ToString(of.data, s + i*0x8c + 0x4d, 3);
            }

            map.put(i, new Team(i+64, abbr, name, true));
        }
    }

    private void initPlayers(OptionFile of, Map<Integer,Player> map) {
        // regular players
        int s = fourBytesToInt(of.data, 0x154);
        int e = fourBytesToInt(of.data, 0x14c);
        int n = (e - s)/0x7c;
        // created players
        int cs = fourBytesToInt(of.data, 0x148);
        int ce = fourBytesToInt(of.data, 0x150);
        int cn = (ce - cs)/0x7c;

        for (int i=0; i<n; i++) {
            String name = bytesUtf16ToString(of.data, s + i*0x7c, 0x20);
            String shirtName = bytesUtf8ToString(of.data, s + i*0x7c + 0x20, 0x10);
            map.put(i, new Player(i, name, shirtName));
            freeAgents.put(i, new Player(i, name, shirtName));
        }
        for (int i=0; i<cn; i++) {
            String name = bytesUtf16ToString(of.data, cs + i*0x7c, 0x20);
            String shirtName = bytesUtf8ToString(of.data, cs + i*0x7c + 0x20, 0x10);
            map.put(n+i, new Player(i+32768, name, shirtName));
            freeAgents.put(n+i, new Player(i+32768, name, shirtName));
        }
    }

    private void initTeams(OptionFile of, Map<Integer,Team> map) {
        initClubTeams(of, map);
    }

    private void initSquads(OptionFile of, Map<Integer,Squad> map) {
        int nat_off = fourBytesToInt(of.data, 0x164);
        int club_off = fourBytesToInt(of.data, 0x168);

        final int NUM_SQUADS = 217;

        int num = 23;
        for (int i=0; i<64; i++) {
            int team_off = nat_off + i*2*num;
            Squad squad = new Squad(i, num);
            for (int j=0; j<num; j++) {
                int player_id = twoBytesToInt(of.data, team_off + j*2);
                squad.players[j] = player_id;
                freeAgents.remove(player_id);
            }
            map.put(i, squad);
        }

        num = 32;
        for (int i=0; i<NUM_SQUADS-74; i++) {
            int team_off = club_off + i*2*num;
            Squad squad = new Squad(i, num);
            for (int j=0; j<num; j++) {
                int player_id = twoBytesToInt(of.data, team_off + j*2);
                squad.players[j] = player_id;
                freeAgents.remove(player_id);
            }
            map.put(i+64, squad);
        }
    }

    public void load() {
        initPlayers(of, players);
        initTeams(of, teams);
        initSquads(of, squads);
    }

    /**
     * get all players, no filter
     */
    public Player[] getPlayers() {
        List<Player> li = new ArrayList<Player>();
        for (Map.Entry<Integer,Player> entry : players.entrySet()) {
            Player p = entry.getValue();
            if (p.id != 0) {
                li.add(p);
            }
        }
        Collections.sort(li, new Comparator<Player>() {
            @Override
            public int compare(Player o1, Player o2) {
                if (o1.id < o2.id) return -1;
                else if (o1.id > o2.id) return 1;
                return 0;
            }
        });
        Player[] arr = new Player[li.size()];
        return li.toArray(arr);
    }

    /**
     * get all teams
     */
    public Team[] getTeams() {
        List<Team> li = new ArrayList<Team>();
        for (Map.Entry<Integer,Team> entry : teams.entrySet()) {
            li.add(entry.getValue());
        }
        Collections.sort(li, new Comparator<Team>() {
            @Override
            public int compare(Team o1, Team o2) {
                if (o1.id < o2.id) return -1;
                else if (o1.id > o2.id) return 1;
                return 0;
            }
        });
        Team[] arr = new Team[li.size()];
        return li.toArray(arr);
    }

    /**
     * get players for specific team
     */
    public Player[] getPlayers(int teamId) {
        Squad squad = squads.get(teamId);
        if (squad == null) {
            return new Player[0];
        }
        List<Player> li = new ArrayList<Player>();
        Player[] players = new Player[squad.players.length];
        for (int i=0; i<squad.players.length; i++) {
            Player p = this.players.get(squad.players[i]);
            if (p != null && p.id != 0) {
                li.add(p);
            }
        }
        Player[] arr = new Player[li.size()];
        return li.toArray(arr);
    }

    /**
     * get free agents
     */
    public Player[] getFreeAgents() {
        List<Player> li = new ArrayList<Player>();
        for (Map.Entry<Integer,Player> entry : freeAgents.entrySet()) {
            Player p = entry.getValue();
            if (p.id != 0) {
                li.add(p);
            }
        }
        Collections.sort(li, new Comparator<Player>() {
            @Override
            public int compare(Player o1, Player o2) {
                if (o1.id < o2.id) return -1;
                else if (o1.id > o2.id) return 1;
                return 0;
            }
        });
        Player[] arr = new Player[li.size()];
        return li.toArray(arr);
    }
}
