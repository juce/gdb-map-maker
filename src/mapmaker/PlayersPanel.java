package mapmaker;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.Dimension;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


class VTeam extends Team {
    public static int EVERYONE = -2;
    public static int FREE_AGENTS = -1;

    public VTeam(int id, String name) {
        super(id, "", name, false);
        this.name = name;
    }
    public String toString() {
        return this.name;
    }
}

public class PlayersPanel extends JPanel
{
    JPanel filtersPanel;
    JPanel resultsPanel;
    JPanel playerPanel;

    JTextField nameSearchText;
    JComboBox teamSelector;
    JComboBox nationalitySelector;
    JList playerList;

    DefaultListModel<Player> playerListModel;
    DefaultComboBoxModel<Team> teamSelectorModel;
    DefaultComboBoxModel<Nationality> nationalitySelectorModel;

    OptionFile of;
    Data data;

    private void populatePlayerList(Player[] players) {
        playerList.setListData(players);
    }

    private void populateTeamSelector(Team[] teams) {
        for (int i=0; i<teams.length; i++) {
            teamSelectorModel.addElement(teams[i]);
        }
    }

    private void populateNationalitySelector() {
        Nationality[] nat = new Nationality[Nationality.names.length];
        for (int i=0; i<nat.length; i++) {
            nat[i] = new Nationality(i, Nationality.names[i]);
            nationalitySelectorModel.addElement(nat[i]);
        }
    }

    public void updatePlayerList() {
        String prefix = nameSearchText.getText().trim();
        Team t = teamSelectorModel.getElementAt(teamSelector.getSelectedIndex());
        if (prefix.equals("") || prefix.equals("<Name filter>")) {
            prefix = null;
        }
        if (t.id >= 0) {
            populatePlayerList(data.getPlayers(t.id, prefix));
        }
        else if (t.id == VTeam.EVERYONE) {
            populatePlayerList(data.getPlayers(prefix));
        }
        else if (t.id == VTeam.FREE_AGENTS) {
            populatePlayerList(data.getFreeAgents(prefix));
        }
    }

    public PlayersPanel() {
        super();

        filtersPanel = new JPanel(new GridLayout(3,1));
        nameSearchText = new JTextField("<Name filter>", 10);
        nameSearchText.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                update();
            }
            public void removeUpdate(DocumentEvent e) {
                update();
            }
            public void insertUpdate(DocumentEvent e) {
                update();
            }
            public void update() {
                updatePlayerList();
            }
        });

        teamSelectorModel = new DefaultComboBoxModel<Team>();
        teamSelector = new JComboBox(teamSelectorModel);
        teamSelectorModel.addElement(new VTeam(VTeam.EVERYONE, "<Any player>"));
        teamSelectorModel.addElement(new VTeam(VTeam.FREE_AGENTS, "<Free agents>"));
        teamSelector.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    updatePlayerList();
                }
            }
        });

        nationalitySelectorModel = new DefaultComboBoxModel<Nationality>();
        nationalitySelector = new JComboBox(nationalitySelectorModel);
        nationalitySelector.addItem("<Any nationality>");
        nationalitySelector.setEnabled(false);

        filtersPanel.add(nameSearchText);
        filtersPanel.add(teamSelector);
        filtersPanel.add(nationalitySelector);
        add(filtersPanel);

        resultsPanel = new JPanel();
        playerListModel = new DefaultListModel<Player>();
        playerList = new JList(playerListModel);
        playerList.setVisibleRowCount(32);
        resultsPanel.add(new JScrollPane(playerList));
        add(resultsPanel);

        of = new OptionFile();
        //of.readXPS(new File("m17/KONAMI-WIN32WE9KOPT"));
        of.readXPS(new File("KONAMI-WIN32WE9KOPT"));

        data = new Data(of);
        data.load();

        populatePlayerList(data.getPlayers(null));
        populateTeamSelector(data.getTeams());
        populateNationalitySelector();
    }
}
 
