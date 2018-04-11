package mapmaker;

import java.util.List;
import java.util.ArrayList;

public class Player
{
    int id;
    String name;
    String shirtName;
    List<Team> teams;

    public Player(int id, String name, String shirtName) {
        this.id = id;
        this.name = name;
        this.shirtName = shirtName;
        this.teams = new ArrayList<Team>();
    }

    public String toString() {
        if (this.name.equals("")) {
            if (this.id >= 32768) {
                return "" + id + ": <empty slot>";
            }
        }
        return "" + id + ": " + name + " (" + shirtName + ")";
    }
}
