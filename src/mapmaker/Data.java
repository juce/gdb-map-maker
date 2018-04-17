package mapmaker;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.io.UnsupportedEncodingException;
import java.io.BufferedReader;
import java.io.FileReader;

public class Data
{
    OptionFile of;
    Map<Integer,Player> players;
    Map<Integer,Team> teams;
    Map<Integer,Squad> squads;
    Map<Integer,Player> freeAgents;

    final int NUM_SQUADS = 217;

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

            map.put(i+64, new Team(i+64, abbr, name, true));
        }
    }

    private void initOtherTeams(Map<Integer,Team> map) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("team-names.csv"));
            String line = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String[] tokens = line.split(",");
                if (tokens.length == 3) {
                    try {
                        int id = Integer.parseInt(tokens[0].trim());
                        String abbr = tokens[1].trim();
                        String name = tokens[2].trim().replace("\"","");
                        Team team = new Team(id, abbr, name, false);
                        map.put(id, team);
                    }
                    catch (Exception e) {
                        // skip non-complaint lines
                    }
                }
            }
            br.close();
        }
        catch (Exception e) {
        }

        /* backfill with generic names, if needed */
        for (int i=0; i<64; i++) {
            if (map.get(i) == null) {
                String name = String.format("Team %d", i);
                String abbr = "NAT";
                map.put(i, new Team(i, abbr, name, false));
            }
        }
        int[] ids = {202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216};
        String[] abbrs = {"ML","SH1","SH2","SH3","SH4","JP1","JP2","EN1","EN2","EN3","EN4","EN5","EN6","EN7","EN8"};
        String[] names = {
            "<ML Default>", "<Shop 1>", "<Shop 2>", "<Shop 3>", "<Shop 4>",
            "<Japan 1>", "<Japan 2>",
            "<Edited 1>", "<Edited 2>", "<Edited 3>", "<Edited 4>",
            "<Edited 5>", "<Edited 6>", "<Edited 7>", "<Edited 8>",
        };
        for (int i=0; i<ids.length; i++) {
            if (map.get(ids[i]) == null) {
                map.put(ids[i], new Team(ids[i], abbrs[i], names[i], false));
            }
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
            map.put(i+32768, new Player(i+32768, name, shirtName));
            freeAgents.put(i+32768, new Player(i+32768, name, shirtName));
        }
    }

    private void initTeams(OptionFile of, Map<Integer,Team> map) {
        initClubTeams(of, map);
        initOtherTeams(map);
    }

    private void initSquads(OptionFile of, Map<Integer,Squad> map) {
        int nat_off = fourBytesToInt(of.data, 0x164);
        int club_off = fourBytesToInt(of.data, 0x168);

        int num = 23;
        for (int i=0; i<74; i++) {
            int team_off = nat_off + i*2*num;
            int k = (i<64) ? i : i+NUM_SQUADS-74;
            Squad squad = new Squad(k, num);
            Team team = teams.get(k);
            for (int j=0; j<num; j++) {
                int player_id = twoBytesToInt(of.data, team_off + j*2);
                squad.players[j] = player_id;
                freeAgents.remove(player_id);

                // plays for national team
                Player p = players.get(player_id);
                if (p != null && team != null) {
                    p.teams.add(team);
                }
            }
            map.put(k, squad);
        }

        num = 32;
        for (int i=0; i<NUM_SQUADS-74; i++) {
            int team_off = club_off + i*2*num;
            Squad squad = new Squad(i, num);
            Team team = teams.get(i+64);
            for (int j=0; j<num; j++) {
                int player_id = twoBytesToInt(of.data, team_off + j*2);
                squad.players[j] = player_id;
                freeAgents.remove(player_id);

                // plays for club
                Player p = players.get(player_id);
                if (p != null && team != null) {
                    p.teams.add(team);
                }
            }
            map.put(i+64, squad);
        }
    }

    public void load() {
        initPlayers(of, players);
        initTeams(of, teams);
        initSquads(of, squads);
    }

    private boolean playerMatch(Player p, String namePrefix) {
        if (namePrefix == null || namePrefix.equals("")) {
            return true;
        }
        if (p.name.toUpperCase().startsWith(namePrefix.toUpperCase())) {
            return true;
        }
        String sn = p.shirtName.toUpperCase();
        if (sn.toUpperCase().startsWith(namePrefix.toUpperCase())) {
            return true;
        }
        sn = sn.replace(" ","").trim();
        if (sn.toUpperCase().startsWith(namePrefix.toUpperCase())) {
            return true;
        }
        return false;
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
     * get players
     */
    public Player[] getPlayers(String namePrefix) {
        List<Player> li = new ArrayList<Player>();
        for (Map.Entry<Integer,Player> entry : players.entrySet()) {
            Player p = entry.getValue();
            if (p.id != 0) {
                if (playerMatch(p, namePrefix)) {
                    li.add(p);
                }
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
     * get players for specific team
     */
    public Player[] getPlayers(int teamId, String namePrefix) {
        Squad squad = squads.get(teamId);
        if (squad == null) {
            System.out.println("squad is null for teamId: " + teamId);
            return new Player[0];
        }
        List<Player> li = new ArrayList<Player>();
        Player[] players = new Player[squad.players.length];
        for (int i=0; i<squad.players.length; i++) {
            Player p = this.players.get(squad.players[i]);
            if (p != null && p.id != 0) {
                if (playerMatch(p, namePrefix)) {
                    li.add(p);
                }
            }
        }
        Player[] arr = new Player[li.size()];
        return li.toArray(arr);
    }

    /**
     * get free agents
     */
    public Player[] getFreeAgents(String namePrefix) {
        List<Player> li = new ArrayList<Player>();
        for (Map.Entry<Integer,Player> entry : freeAgents.entrySet()) {
            Player p = entry.getValue();
            if (p.id != 0) {
                if (playerMatch(p, namePrefix)) {
                    li.add(p);
                }
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
