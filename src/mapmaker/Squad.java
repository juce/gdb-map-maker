package mapmaker;

public class Squad
{
    int id;
    int[] players;

    public Squad(int id, int numPlayers) {
        this.id = id;
        this.players = new int[numPlayers];
    }
}
