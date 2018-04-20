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
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;


class Settings {
    String optionFilename;
    String gdbDirname;
    String mapOutputEncoding;
}

class MapMakerStartup extends JFrame
{
    JLabel waitText;
    JProgressBar pb;
    JPanel loadingPane;
    List<Listener> listeners;

    String optionFilename;
    String gdbDirname;
    boolean doneLoading;
    int bCount;

    public static final String INI_FILE = "mapmaker.ini";
    public static final String DEFAULT_OUTPUT_ENCODING = "iso-8859-1";

    public interface Listener {
        public void startup(Settings settings);
    }

    public MapMakerStartup(boolean chooseNew) {
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

        // load file locations
        Settings settings = loadSettings();
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
                    if (optionFilename != null) {
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
                    if (gdbDirname != null) {
                        File f = new File(gdbDirname);
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
        saveSettings(settings);
    }

    public Settings loadSettings() {
        Settings settings = new Settings();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(INI_FILE), "utf-8"));
            settings.optionFilename = br.readLine();
            if (settings.optionFilename != null) {
                settings.optionFilename = settings.optionFilename.trim();
            }
            settings.gdbDirname = br.readLine();
            if (settings.gdbDirname != null) {
                settings.gdbDirname = settings.gdbDirname.trim();
            }
            settings.mapOutputEncoding = br.readLine();
            if (settings.mapOutputEncoding != null) {
                settings.mapOutputEncoding = settings.mapOutputEncoding.trim();
            }
            else {
                settings.mapOutputEncoding = DEFAULT_OUTPUT_ENCODING;
            }
            br.close();
        }
        catch (FileNotFoundException e1) {
            System.out.println("Warning: " + INI_FILE + " was not found");
        }
        catch (IOException e2) {
            System.out.println("Problem: " + e2);
        }
        System.out.println("Using optionFilename: " + settings.optionFilename);
        System.out.println("Using gdbDirname: " + settings.gdbDirname);
        System.out.println("Using mapOutputEncoding: " + settings.mapOutputEncoding);
        return settings;
    }

    public void saveSettings(Settings settings) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(INI_FILE), "utf-8"));
            bw.write(settings.optionFilename + "\r\n");
            bw.write(settings.gdbDirname + "\r\n");
            bw.write(settings.mapOutputEncoding + "\r\n");
            bw.close();
        }
        catch (IOException e1) {
            System.out.println("Warning: cannot save " + INI_FILE);
        }
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

    public MapMaker(Settings settings) {
        super("GDB Map Maker");
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
                int dialogButton = JOptionPane.YES_NO_CANCEL_OPTION;
                int dialogResult = JOptionPane.showConfirmDialog(null, "Save your changes?", "Warning", dialogButton);
                if (dialogResult != JOptionPane.CANCEL_OPTION) {
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        saveAllMaps();
                    }
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
        setJMenuBar(mb);
    }

    private void saveAllMaps() {
        // faces
        saveMap(settings.gdbDirname + "/faces/map.txt", playersPanel.facesMap, playersPanel.data);
        // hair
        saveMap(settings.gdbDirname + "/hair/map.txt", playersPanel.hairMap, playersPanel.data);
        // boots
        saveMap(settings.gdbDirname + "/boots/map.txt", playersPanel.bootsMap, playersPanel.data);
    }

    public void saveMap(String filename, GDBMap map, Data data) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), settings.mapOutputEncoding));
            bw.write("# Map generated by GDB Map Maker\r\n");
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
        new MapMakerStartup(chooseNew).onStartup(new MapMakerStartup.Listener() {
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
