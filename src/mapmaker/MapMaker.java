package mapmaker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.UIManager;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;


class MapMakerStartup extends JFrame
{
    JLabel waitText;
    JProgressBar pb;
    JPanel loadingPane;
    List<Listener> listeners;

    boolean doneLoading;
    int bCount;

    public interface Listener {
        public void startup(Settings settings);
    }

    public static void tryTheme(String theme) {
        try {
            if ("native".equals(theme)) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            else if ("metal".equals(theme)) {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
        }
        catch (Exception e) {
            System.out.println("Unable to set theme: " + theme);
        }
    }

    public MapMakerStartup(Settings settings, boolean chooseNew) {
        super("GDB Map Maker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        System.out.println("MapMakerStartup constructor called");
        listeners = new ArrayList<Listener>();
        setIcon();
        loadingPane = new JPanel();
        loadingPane.setLayout(new BoxLayout(loadingPane, BoxLayout.PAGE_AXIS));
        waitText = new JLabel("Loading, please wait ...");
        waitText.setAlignmentX(Component.CENTER_ALIGNMENT);
        pb = new JProgressBar();
        pb.setAlignmentX(Component.CENTER_ALIGNMENT);
        pb.setValue(0);
        URL localURL = getClass().getResource("data/splash.jpg");
        if (localURL != null) {
            ImageIcon localImage = new ImageIcon(localURL);
            JLabel lab = new JLabel(localImage);
            lab.setAlignmentX(Component.CENTER_ALIGNMENT);
            loadingPane.add(lab);
        }
        loadingPane.add(Box.createVerticalStrut(8));

        // progress bar updater
        doneLoading = false;
        Thread t1 = new Thread(new Runnable() {
            public void run() {
                while (!doneLoading) {
                    try { Thread.sleep(20); }
                    catch (InterruptedException e) {}
                    pb.setValue((pb.getValue() + 1) % 100);
                }
            }
        });
        t1.start();

        boolean noButtons = true;
        bCount = 0;

        if (settings.optionFilename == null || chooseNew) {
            waitText.setVisible(false);
            pb.setVisible(false);
            noButtons = false;

            JButton b = new JButton("Choose option file");
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    JFileChooser jfc;
                    if (settings.optionFilename != null) {
                        // look in the same directory where last time
                        File f = new File(settings.optionFilename);
                        jfc = new JFileChooser(f.getParent());
                    }
                    else {
                        jfc = new JFileChooser();
                    }
                    jfc.setDialogTitle("Choose OPTION file");
                    int returnValue = jfc.showOpenDialog(null);
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = jfc.getSelectedFile();
                        settings.optionFilename = selectedFile.getAbsolutePath();
                        System.out.println("Now using optionFilename: " + settings.optionFilename);
                        loadingPane.remove(b);
                        pack();
                        bCount |= 1;
                        if (bCount == 3) {
                            triggerStartup(settings);
                        }
                    }
                }
            });
            loadingPane.add(b);
        }
        if (settings.gdbDirname == null || chooseNew) {
            waitText.setVisible(false);
            pb.setVisible(false);
            noButtons = false;

            JButton b = new JButton("Choose GDB directory");
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    JFileChooser jfc;
                    if (settings.gdbDirname != null) {
                        File f = new File(settings.gdbDirname);
                        jfc = new JFileChooser(f.getParent());
                    }
                    else {
                        jfc = new JFileChooser();
                    }
                    jfc.setDialogTitle("Choose GDB directory");
                    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int returnValue = jfc.showOpenDialog(null);
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = jfc.getSelectedFile();
                        settings.gdbDirname = selectedFile.getAbsolutePath();
                        System.out.println("Now using gdbDirname: " + settings.gdbDirname);
                        loadingPane.remove(b);
                        pack();
                        bCount |= 2;
                        if (bCount == 3) {
                            triggerStartup(settings);
                        }
                    }
                }
            });
            loadingPane.add(b);
            loadingPane.add(Box.createVerticalStrut(8));
        }

        loadingPane.add(waitText);
        loadingPane.add(pb);
        loadingPane.add(Box.createVerticalStrut(8));

        Container pane = getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.LINE_AXIS));
        pane.add(Box.createHorizontalStrut(8));
        pane.add(loadingPane);
        pane.add(Box.createHorizontalStrut(8));

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        doneLoading = false;

        if (noButtons) {
            triggerStartup(settings);
        }
    }

    public void triggerStartup(Settings settings) {
        // kick-off initialization
        waitText.setVisible(true);
        pb.setVisible(true);
        pack();

        Thread t = new Thread(new Runnable() {
            public void run() {
                log("starting");
                for (Listener li : listeners) {
                    li.startup(settings);
                }
                log("startup done");
                doneLoading = true;
                setVisible(false);
                dispose();
            }
        });
        t.start();
        settings.save();
    }

    public void log(String message) {
        Date date = new Date();
        System.out.println(String.format("%s: %s", date, message));
    }

    private void setIcon() {
        URL localURL = getClass().getResource("data/icon.png");
        if (localURL != null) {
            ImageIcon localImageIcon = new ImageIcon(localURL);
            setIconImage(localImageIcon.getImage());
        }
    }

    public void onStartup(Listener listener) {
        listeners.add(listener);
    }
}

public class MapMaker extends JFrame
{
    JTabbedPane tabbedPane;
    PlayersPanel playersPanel;
    StadiumsPanel stadiumsPanel;
    BallsPanel ballsPanel;
    Settings settings;

    public static String VERSION = "0.3";

    public MapMaker(Settings settings) {
        super("GDB Map Maker " + VERSION);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.settings = settings;
        setIcon();
        buildMenu();

        playersPanel = new PlayersPanel(settings.optionFilename, settings.gdbDirname);
        stadiumsPanel = new StadiumsPanel();
        ballsPanel = new BallsPanel();

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Players", null, playersPanel, null);
        tabbedPane.addTab("Stadiums", null, stadiumsPanel, null);
        tabbedPane.addTab("Balls", null, ballsPanel, null);
 
        getContentPane().add(tabbedPane);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildMenu() {
        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
                if (playersPanel.needsSave) {
                    int dialogButton = JOptionPane.YES_NO_CANCEL_OPTION;
                    int dialogResult = JOptionPane.showConfirmDialog(null, "Save your changes?", "Warning", dialogButton);
                    if (dialogResult != JOptionPane.CANCEL_OPTION) {
                        if (dialogResult == JOptionPane.YES_OPTION) {
                            saveAllMaps();
                        }
                        System.exit(0);
                    }
                }
                else {
                    System.exit(0);
                }
            }
        });
        JMenuItem chooseItem = new JMenuItem("Choose option and GDB");
        chooseItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                newUI(true);
                setVisible(false);
                dispose();
            }
        });
        JMenuItem saveItem = new JMenuItem("Save all maps");
        saveItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                saveAllMaps();
            }
        });
        menu.add(chooseItem);
        menu.add(saveItem);
        menu.add(exitItem);
        mb.add(menu);

        JMenu menu1 = new JMenu("Tools");
        JMenuItem systemLook = new JMenuItem("Set native theme");
        systemLook.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                settings.theme = "native";
                settings.save();
                int dialogButton = JOptionPane.INFORMATION_MESSAGE;
                JOptionPane.showMessageDialog(null, "Restart GDB Map Maker to see new theme", "Theme switched to: "+settings.theme, dialogButton);
            }
        });
        JMenuItem metalLook = new JMenuItem("Set Java-metal theme");
        metalLook.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                settings.theme = "metal";
                settings.save();
                int dialogButton = JOptionPane.INFORMATION_MESSAGE;
                JOptionPane.showMessageDialog(null, "Restart GDB Map Maker to see new theme", "Theme switched to: "+settings.theme, dialogButton);
            }
        });
        menu1.add(systemLook);
        menu1.add(metalLook);
        mb.add(menu1);

        JFrame parent = this;
        JMenu menu2 = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AboutDialog dialog = new AboutDialog(parent, VERSION);
                dialog.show();
            }
        });
        menu2.add(about);
        mb.add(menu2);

        setJMenuBar(mb);
    }

    private void saveAllMaps() {
        // faces
        saveMap(settings.gdbDirname + "/faces/map.txt", playersPanel.facesMap, playersPanel.data);
        // hair
        saveMap(settings.gdbDirname + "/hair/map.txt", playersPanel.hairMap, playersPanel.data);
        // boots
        saveMap(settings.gdbDirname + "/boots/map.txt", playersPanel.bootsMap, playersPanel.data);

        // mark as saved
        playersPanel.needsSave = false;
    }

    public void saveMap(String filename, GDBMap map, Data data) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), settings.mapOutputEncoding));
            bw.write(String.format("# Map generated by GDB Map Maker v%s\r\n", VERSION));
            for (Map.Entry<Integer,Squad> e : data.squads.entrySet()) {
                int teamId = e.getKey().intValue();
                Squad squad = e.getValue();
                if (squad.players.length > 0) {
                    List<String> lines = new ArrayList<String>();
                    for (int i=0; i<squad.players.length; i++) {
                        Player p = data.players.get(squad.players[i]);
                        if (p != null) {
                            GDBMap.Entry entry = map.get(p.id);
                            // favour club teams
                            if (entry != null && p.teams.get(p.teams.size()-1).id == teamId) {
                                String comment = (entry.comment != null) ? entry.comment : "# " + p.name;
                                lines.add(String.format("%d,%s %s\r\n", p.id, entry.values[0], comment));
                            }
                        }
                    }
                    if (lines.size() > 0) {
                        Team t = data.teams.get(teamId);
                        bw.write("\r\n");
                        bw.write("# " + t.name + "\r\n");
                        for (String line : lines) {
                            bw.write(line);
                        }
                    }
                }
            }
            bw.write("\r\n");
            bw.write("# free agents\r\n");
            for (Map.Entry<Integer,Player> e : data.freeAgents.entrySet()) {
                Player p = e.getValue();
                GDBMap.Entry entry = map.get(p.id);
                if (entry != null) {
                    String comment = (entry.comment != null) ? entry.comment : "# " + p.name;
                    bw.write(String.format("%d,%s %s\r\n", p.id, entry.values[0], comment));
                }
            }
            bw.write("\r\n");
            bw.write("# end-of-map\r\n");
            bw.close();
            System.out.println("SAVED: " + filename);
        }
        catch (IOException e) {
            System.out.println("Problem saving: " + filename);
        }
    }
 
    private void setIcon() {
        URL localURL = getClass().getResource("data/icon.png");
        if (localURL != null) {
            ImageIcon localImageIcon = new ImageIcon(localURL);
            setIconImage(localImageIcon.getImage());
        }
    }

    public static void newUI(boolean chooseNew) {
        System.out.println("creating new UI");
        Settings settings = new Settings();
        MapMakerStartup.tryTheme(settings.theme);
        new MapMakerStartup(settings, chooseNew).onStartup(new MapMakerStartup.Listener() {
            public void startup(Settings settings) {
                try {
                    new MapMaker(settings);
                }
                catch (Exception e) {
                    System.out.println("FATAL problem: " + e);
                    System.exit(3);
                }
            }
        });
    }

    public static void main(String args[]) {
        newUI(false);
    }
}
