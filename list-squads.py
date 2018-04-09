import sys
import struct

"""
  private boolean inSquad(int paramInt1, int paramInt2)
  {
    boolean bool = false;
    if (paramInt2 != 0)
    {
      int i;
      int j;
      if (paramInt1 < 74)
      {
        i = 23;
        j = 664054 + paramInt1 * i * 2;
      }
      else
      {
        i = 32;
        j = 667458 + (paramInt1 - 74) * i * 2;
      }
      for (int n = 0; (!bool) && (n < i); n++)
      {
        int m = j + n * 2;
        int k = this.of.toInt(this.of.data[(m + 1)]) << 8 | this.of.toInt(this.of.data[m]);
        if (k == paramInt2) {
          bool = true;
        }
      }
    }
    return bool;
  }
"""

def print_player(players, player_id, listed):
    s = "unknown"
    p = players.get(player_id)
    if p:
        s = ("%d %s (%s)" % p).encode('utf-8')
        listed[player_id] = p
    print("player: %s" % s)

def list_squads(nat, club, players, club_names, listed):
    for i in range(64):
        num = 23
        offs = nat + i * (num*2)
        print("team: %d" % i)
        print("~~~~~~~~~~~~~~~~~~~~~~~~~~")
        for j in range(num):
            player_id = struct.unpack('<H',data[offs+j*2:offs+j*2+2])[0]
            print_player(players, player_id, listed)
        print("")
    for i in range(219-74):
        num = 32
        offs = club + i * (num*2)
        team_name = club_names.get(i+64,"")
        if team_name:
            team_name = "(%s) %s" % team_name
        print(("team: %d %s" % (i+64, team_name)).encode('utf-8'))
        print("~~~~~~~~~~~~~~~~~~~~~~~~~~")
        for j in range(num):
            player_id = struct.unpack('<H',data[offs+j*2:offs+j*2+2])[0]
            print_player(players, player_id, listed)
        print("")

def list_unaffiliated(players, listed):
    li = []
    for player_id,p in players.iteritems():
        if listed.get(player_id) is None:
            if p[0]>32768 and p[1]=="" and p[2]=="":
                # skip empty player slots
                continue
            li.append(p)
    def comp(a,b):
        res = cmp(a[2],b[2])
        if res == 0:
            res = cmp(a[1],b[1])
            if res == 0:
                res = cmp(a[0],b[0])
        return res
    li.sort(comp)
    print("unaffiliated:")
    print("~~~~~~~~~~~~~~~~~~~~~~~~~~")
    for p in li:
        s = ("%d %s (%s)" % p).encode('utf-8')
        print("player: %s" % s)
    print("")

def get_players(start, finish, id_offset):
    d = {}
    for i in range((finish - start)/0x7c):
        name = data[start + i*0x7c + 0x00 : start + i*0x7c + 0x20]
        end = name.find('\0\0')
        if (end % 2) == 1:
            end += 1
        name = name[:end]
        if name:
            name = name.decode('utf-16')
        shirt_name = data[start + i*0x7c + 0x20 : start + i*0x7c + 0x30]
        shirt_name = shirt_name.strip('\0')
        d[i+id_offset] = (i+id_offset, name, shirt_name)
    return d

def get_club_names(s, e):
    d = {}
    for i in range((e - s)/0x8c):
        name = data[s + i*0x8c : s + i*0x8c + 0x31]
        name = name[:name.find('\0')]
        if name:
            name = name.decode('utf-8')
        else:
            # default, unmodified
            name = data[s + i*0x8c + 0x31, s + i*0x8c + 0x41]
            name = name[:name.find('\0')]
            if name:
                name = name.decode('utf-8')
        abbr = data[s + i*0x8c + 0x49 : s + i*0x8c + 0x49 + 3]
        if abbr == '\0\0\0':
            abbr = data[s + i*0x8c + 0x4d : s + i*0x8c + 0x4d + 3]
        d[64 + i] = (abbr, name)
    return d
        

data = sys.stdin.read()

listed = {}

# existing players
start = struct.unpack('<i',data[0x154:0x158])[0]
finish = struct.unpack('<i',data[0x14c:0x150])[0]
players = get_players(start, finish, 0)

# created players
start = struct.unpack('<i',data[0x148:0x14c])[0]
finish = struct.unpack('<i',data[0x150:0x154])[0]
players.update(get_players(start, finish, 32768))

# club names
club_names_s = struct.unpack('<i',data[0x174:0x178])[0]
club_names_e = struct.unpack('<i',data[0x178:0x17c])[0]
club_names = get_club_names(club_names_s, club_names_e)

# by team
nat = struct.unpack('<i',data[0x164:0x168])[0]
club = struct.unpack('<i',data[0x168:0x16c])[0]

list_squads(nat, club, players, club_names, listed)

list_unaffiliated(players, listed)

