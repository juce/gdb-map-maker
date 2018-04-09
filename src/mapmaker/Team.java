package mapmaker;

public class Team
{
    int id;
    String abbr;
    String name;
    boolean isClub;

    public Team(int id, String abbr, String name, boolean isClub) {
        this.id = id;
        this.abbr = abbr;
        this.name = name;
        this.isClub = isClub;
    }

    public String toString() {
        return "" + id + " (" + abbr + ") " + name;
    }
}
