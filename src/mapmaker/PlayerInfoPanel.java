package mapmaker;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.util.List;
import java.util.ArrayList;

public class PlayerInfoPanel extends JPanel
{
    JLabel playerInfo;
    JLabel playerTeams;

    public PlayerInfoPanel() {
        super();
        playerInfo = new JLabel();
        playerInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        playerTeams = new JLabel();
        playerTeams.setAlignmentX(Component.LEFT_ALIGNMENT);

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(Box.createRigidArea(new Dimension(30,0)));
        add(playerInfo);
        add(playerTeams);
        clear();
    }

    public void update(Player p) {
        playerInfo.setText(String.format("Player: %d %s (%s)", p.id, p.name, p.shirtName));
        List<String> teams = new ArrayList<String>();
        for (Team team : p.teams) {
            teams.add(team.toString());
        }
        playerTeams.setText("Teams: " + (teams.size() > 0 ? String.join(", ", teams) : "free agent"));
    }

    public void clear() {
        playerInfo.setText("Player:");
        playerTeams.setText("Teams:");
    }
}
