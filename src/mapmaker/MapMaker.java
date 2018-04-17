package mapmaker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
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
import javax.swing.BoxLayout;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;


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

    public interface Listener {
        public void startup(String optionFilename, String gdbDirname);
    }

    public MapMakerStartup(boolean chooseNew) {
        super("GDB Map Maker");
        System.out.println("MapMakerStartup constructor called");
        listeners = new ArrayList<Listener>();
        setIcon();
        loadingPane = new JPanel();
        loadingPane.setLayout(new BoxLayout(loadingPane, BoxLayout.Y_AXIS));
        waitText = new JLabel("Loading, please wait ...");
        pb = new JProgressBar();
        pb.setValue(0);
        URL localURL = getClass().getResource("data/icon-large.png");
        if (localURL != null) {
            ImageIcon localImage = new ImageIcon(localURL);
            loadingPane.add(new JLabel(localImage));
        }
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
        loadFileNames();
        boolean noButtons = true;
        bCount = 0;

        if (optionFilename == null || chooseNew) {
            waitText.setVisible(false);
            pb.setVisible(false);
            noButtons = false;

            JButton b = new JButton("Choose option file");
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    JFileChooser jfc;
                    if (optionFilename != null) {
                        // look in the same directory where last time
                        File f = new File(optionFilename);
                        jfc = new JFileChooser(f.getParent());
                    }
                    else {
                        jfc = new JFileChooser();
                    }
                    jfc.setDialogTitle("Choose OPTION file");
                    int returnValue = jfc.showOpenDialog(null);
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = jfc.getSelectedFile();
                        optionFilename = selectedFile.getAbsolutePath();
                        System.out.println("Now using optionFilename: " + optionFilename);
                        loadingPane.remove(b);
                        pack();
                        bCount |= 1;
                        if (bCount == 3) {
                            triggerStartup();
                        }
                    }
                }
            });
            loadingPane.add(b);
        }
        if (gdbDirname == null || chooseNew) {
            waitText.setVisible(false);
            pb.setVisible(false);
            noButtons = false;

            JButton b = new JButton("Choose GDB directory");
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
                        gdbDirname = selectedFile.getAbsolutePath();
                        System.out.println("Now using gdbDirname: " + gdbDirname);
                        loadingPane.remove(b);
                        pack();
                        bCount |= 2;
                        if (bCount == 3) {
                            triggerStartup();
                        }
                    }
                }
            });
            loadingPane.add(b);
        }

        loadingPane.add(waitText);
        loadingPane.add(pb);
        getContentPane().add(loadingPane);
        pack();
        setVisible(true);
        doneLoading = false;

        if (noButtons) {
            triggerStartup();
        }
    }

    public void triggerStartup() {
        // kick-off initialization
        waitText.setVisible(true);
        pb.setVisible(true);
        pack();

        Thread t = new Thread(new Runnable() {
            public void run() {
                log("starting");
                for (Listener li : listeners) {
                    li.startup(optionFilename, gdbDirname);
                }
                log("startup done");
                doneLoading = true;
                setVisible(false);
                dispose();
            }
        });
        t.start();
        saveFileNames();
    }

    public void loadFileNames() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(INI_FILE), "utf-8"));
            optionFilename = br.readLine();
            if (optionFilename != null) {
                optionFilename = optionFilename.trim();
            }
            gdbDirname = br.readLine();
            if (gdbDirname != null) {
                gdbDirname = gdbDirname.trim();
            }
            br.close();
        }
        catch (FileNotFoundException e1) {
            System.out.println("Warning: " + INI_FILE + " was not found");
        }
        catch (IOException e2) {
            System.out.println("Problem: " + e2);
        }
        System.out.println("Using optionFilename: " + optionFilename);
        System.out.println("Using gdbDirname: " + gdbDirname);
    }

    public void saveFileNames() {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(INI_FILE), "utf-8"));
            bw.write(optionFilename + "\r\n");
            bw.write(gdbDirname + "\r\n");
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

    public MapMaker(String optionFilename, String gdbDirname) {
        super("GDB Map Maker");
        setIcon();
        buildMenu();

        playersPanel = new PlayersPanel(optionFilename, gdbDirname);
        stadiumsPanel = new StadiumsPanel();
        ballsPanel = new BallsPanel();

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Players", null, playersPanel, null);
        tabbedPane.addTab("Stadiums", null, stadiumsPanel, null);
        tabbedPane.addTab("Balls", null, ballsPanel, null);
 
        getContentPane().add(tabbedPane);
        pack();
        setVisible(true);
    }

    private void buildMenu() {
        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent paramAnonymousActionEvent) {
                System.exit(0);
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
        JMenuItem saveItem = new JMenuItem("Save maps");
        saveItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                // faces
                saveMap("map-faces.txt", playersPanel.facesMap, playersPanel.data);
                // hair
                saveMap("map-hair.txt", playersPanel.hairMap, playersPanel.data);
                // boots
                saveMap("map-boots.txt", playersPanel.bootsMap, playersPanel.data);
            }
        });
        menu.add(chooseItem);
        menu.add(saveItem);
        menu.add(exitItem);
        mb.add(menu);
        setJMenuBar(mb);
    }

    public void saveMap(String filename, GDBMap map, Data data) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
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
                            if (entry != null) {
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
            bw.write("# end-of-map\r\n");
            bw.close();
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
            public void startup(String optionFilename, String gdbDirname) {
                try {
                    new MapMaker(optionFilename, gdbDirname);
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
