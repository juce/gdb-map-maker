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
import javax.swing.Box;
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

    PlayerInfoPanel info;
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
        filtersPanel.setLayout(new BoxLayout(filtersPanel,BoxLayout.PAGE_AXIS));
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
        filtersPanel.add(new JLabel("Search"));
        filtersPanel.add(nameSearchText);

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
        filtersPanel.add(teamSelector);

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        filtersPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        add(filtersPanel);

        playerList = new JList();
        playerList.setVisibleRowCount(32);
        playerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playerList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting())
                {
                    if (playerList.isSelectionEmpty())
                    {
                        info.clear();
                        faceBin.clear();
                        hairBin.clear();
                        bootsFile.clear();
                    }
                    else
                    {
                        Player p = (Player)playerList.getSelectedValue();
                        info.update(p);

                        String face = facesMap.getFirst(p.id);
                        String hair = hairMap.getFirst(p.id);
                        String boots = bootsMap.getFirst(p.id);

                        faceBin.setChoice(face);
                        hairBin.setChoice(hair);
                        bootsFile.setChoice(boots);
                    }
                }
            }
        });
        JScrollPane jsp = new JScrollPane(playerList);
        jsp.setPreferredSize(new Dimension(0, 750));
        filtersPanel.add(jsp);

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

        info = new PlayerInfoPanel();
        info.setAlignmentX(Component.LEFT_ALIGNMENT);

        faceBin = new FileChoicePanel(this.gdbDirname + "/faces", "Face", true, 64, 128, "", "BIN files", "bin");
        faceBin.setAlignmentX(Component.LEFT_ALIGNMENT);
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

        hairBin = new FileChoicePanel(this.gdbDirname + "/hair", "Hair", true, 128, 64, "", "BIN files", "bin");
        hairBin.setAlignmentX(Component.LEFT_ALIGNMENT);
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

        bootsFile = new FileChoicePanel(this.gdbDirname + "/boots", "Boots", false, 170, 256, "", "PNG files", "png");
        bootsFile.setAlignmentX(Component.LEFT_ALIGNMENT);
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

        playerPanel.add(info);
        playerPanel.add(faceBin);
        playerPanel.add(hairBin);
        playerPanel.add(bootsFile);
        playerPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        playerPanel.setPreferredSize(new Dimension(550,0));
        add(playerPanel);
    }
}
 
