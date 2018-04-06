import sys
import struct

def list_players(start, finish, id_offset):
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
        print(("%5d\t%s\t(%s)" % (i + id_offset, name, shirt_name)).encode('utf-8'))

data = sys.stdin.read()

# existing players
start = struct.unpack('<i',data[0x154:0x158])[0]
finish = struct.unpack('<i',data[0x14c:0x150])[0]

list_players(start, finish, 0)

# created players
start = struct.unpack('<i',data[0x148:0x14c])[0]
finish = struct.unpack('<i',data[0x150:0x154])[0]

list_players(start, finish, 32768)

