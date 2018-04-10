package mapmaker;

public class Player
{
    int id;
    String name;
    String shirtName;

    public Player(int id, String name, String shirtName) {
        this.id = id;
        this.name = name;
        this.shirtName = shirtName;
    }

    public String toString() {
        if (this.id >= 32768 && this.name.equals("")) {
            return "" + id + ": <empty slot>";
        }
        return "" + id + ": " + name + " (" + shirtName + ")";
    }
}
