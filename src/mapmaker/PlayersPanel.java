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
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Date;


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
    JPanel playerPanel;

    JTextField nameSearchText;
    JComboBox teamSelector;
    JComboBox nationalitySelector;
    JList playerList;

    JLabel playerId;
    JLabel playerName;
    JLabel playerShirtName;
    JPanel playerTeams;

    FileChoicePanel faceBin;
    FileChoicePanel hairBin;
    FileChoicePanel bootsFile;

    DefaultListModel<Player> playerListModel;
    DefaultComboBoxModel<Team> teamSelectorModel;
    DefaultComboBoxModel<Nationality> nationalitySelectorModel;

    OptionFile of;
    Data data;
    String gdbDirname;

    GDBMap facesMap;
    GDBMap hairMap;
    GDBMap bootsMap;

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
        if (prefix.equals("")) {
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

    private void loadMap(GDBMap map, String filename) {
        try {
            map.load(filename);
        }
        catch (Exception e) {
            System.out.println("Warning: failed to load GDB map: " + filename);
        }
    }

    public PlayersPanel(String optionFilename, String gdbDirname) {
        super();

        filtersPanel = new JPanel();
        filtersPanel.setLayout(new BoxLayout(filtersPanel,BoxLayout.Y_AXIS));
        JPanel searchPanel = new JPanel();
        nameSearchText = new JTextField("", 15);
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
        searchPanel.add(new JLabel("Search"));
        searchPanel.add(nameSearchText);

        teamSelectorModel = new DefaultComboBoxModel<Team>();
        teamSelector = new JComboBox(teamSelectorModel);
        teamSelector.setMaximumRowCount(20);
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

        filtersPanel.add(searchPanel);
        filtersPanel.add(teamSelector);
        //filtersPanel.add(nationalitySelector);

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(filtersPanel);

        //playerListModel = new DefaultListModel<Player>();
        //playerList = new JList(playerListModel);
        playerList = new JList();
        playerList.setVisibleRowCount(32);
        playerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playerList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting())
                {
                    if (playerList.isSelectionEmpty())
                    {
                        playerId.setText("Player id: ");
                        playerName.setText("Player name: ");
                        playerShirtName.setText("Player shirt name: ");
                        playerTeams.removeAll();
                        faceBin.clear();
                        hairBin.clear();
                        bootsFile.clear();
                    }
                    else
                    {
                        Player p = (Player)playerList.getSelectedValue();
                        playerId.setText("Player id: " + p.id);
                        playerName.setText("Player name: " + p.name);
                        playerShirtName.setText("Player shirt name: " + p.shirtName);
                        playerTeams.removeAll();
                        for (Team team : p.teams) {
                            playerTeams.add(new JLabel(team.toString()));
                        }

                        String face = facesMap.getFirst(p.id);
                        face = (face == null) ? "None" : face;
                        String hair = hairMap.getFirst(p.id);
                        hair = (hair == null) ? "None" : hair;
                        String boots = bootsMap.getFirst(p.id);
                        boots = (boots == null) ? "None" : boots;

                        faceBin.setChoice(face);
                        hairBin.setChoice(hair);
                        bootsFile.setChoice(boots);
                    }
                }
            }
        });
        filtersPanel.add(new JScrollPane(playerList));

        // load option file
        System.out.println(""+(new Date())+": loading option file");
        of = new OptionFile();
        of.readXPS(new File(optionFilename));
        System.out.println(""+(new Date())+": option file loaded");
        data = new Data(of);
        data.load();

        // load GDB maps
        this.gdbDirname = gdbDirname;
        facesMap = new GDBMap();
        loadMap(facesMap, this.gdbDirname + "/faces/map.txt");
        hairMap = new GDBMap();
        loadMap(hairMap, this.gdbDirname + "/hair/map.txt");
        bootsMap = new GDBMap();
        loadMap(bootsMap, this.gdbDirname + "/boots/map.txt");

        //facesMap.print();
        //hairMap.print();
        //bootsMap.print();

        populatePlayerList(data.getPlayers(null));
        populateTeamSelector(data.getTeams());
        populateNationalitySelector();

        playerPanel = new JPanel();
        playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
        playerId = new JLabel("Player id: ");
        playerName = new JLabel("Player name: ");
        playerShirtName = new JLabel("Player shirt name: ");
        playerTeams = new JPanel();
        playerTeams.setLayout(new BoxLayout(playerTeams, BoxLayout.Y_AXIS));

        faceBin = new FileChoicePanel(this.gdbDirname + "/faces", "Face", "", "BIN files", "bin");
        faceBin.addListener(new FileChoicePanel.Listener() {
            public void valueChanged(String value) {
                Player p = (Player)playerList.getSelectedValue();
                if (value == null) {
                    facesMap.remove(p.id);
                    return;
                }
                GDBMap.Entry e = facesMap.get(p.id);
                if (e != null) {
                    e.values[0] = value;
                }
                else {
                    String[] values = { value };
                    facesMap.put(p.id, new GDBMap.Entry(values, "# "+p.name));
                }
            }
        });

        hairBin = new FileChoicePanel(this.gdbDirname + "/hair", "Hair", "", "BIN files", "bin");
        hairBin.addListener(new FileChoicePanel.Listener() {
            public void valueChanged(String value) {
                Player p = (Player)playerList.getSelectedValue();
                if (value == null) {
                    hairMap.remove(p.id);
                    return;
                }
                GDBMap.Entry e = hairMap.get(p.id);
                if (e != null) {
                    e.values[0] = value;
                }
                else {
                    String[] values = { value };
                    hairMap.put(p.id, new GDBMap.Entry(values, "# "+p.name));
                }
            }
        });

        bootsFile = new FileChoicePanel(this.gdbDirname + "/boots", "Boots", "", "PNG files", "png");
        bootsFile.addListener(new FileChoicePanel.Listener() {
            public void valueChanged(String value) {
                Player p = (Player)playerList.getSelectedValue();
                if (value == null) {
                    bootsMap.remove(p.id);
                    return;
                }
                GDBMap.Entry e = bootsMap.get(p.id);
                if (e != null) {
                    e.values[0] = value;
                }
                else {
                    String[] values = { value };
                    bootsMap.put(p.id, new GDBMap.Entry(values, "# "+p.name));
                }
            }
        });

        playerPanel.add(playerId);
        playerPanel.add(playerName);
        playerPanel.add(playerShirtName);
        playerPanel.add(new JLabel("Player teams: "));
        playerPanel.add(playerTeams);
        playerPanel.add(faceBin);
        playerPanel.add(hairBin);
        playerPanel.add(bootsFile);
        add(playerPanel);
    }
}
 
