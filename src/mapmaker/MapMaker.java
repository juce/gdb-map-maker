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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
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


class MapMakerStartup extends JFrame
{
    JProgressBar pb;
    JPanel loadingPane;
    List<Listener> listeners;

    String optionFilename;
    String gdbDirname;
    boolean doneLoading;

    public interface Listener {
        public void startup(String optionFilename, String gdbDirname);
    }

    public MapMakerStartup() {
        super("GDB Map Maker");
        listeners = new ArrayList<Listener>();
        setIcon();
        loadingPane = new JPanel();
        loadingPane.setLayout(new BoxLayout(loadingPane, BoxLayout.Y_AXIS));
        pb = new JProgressBar();
        pb.setValue(0);
        URL localURL = getClass().getResource("data/icon-large.png");
        if (localURL != null) {
            ImageIcon localImage = new ImageIcon(localURL);
            loadingPane.add(new JLabel(localImage));
        }
        loadingPane.add(new JLabel("Loading, please wait ..."));
        loadingPane.add(pb);
        getContentPane().add(loadingPane);
        pack();
        setVisible(true);
        doneLoading = false;

        // progress bar updater
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

        // kick-off initialization
        Thread t = new Thread(new Runnable() {
            public void run() {
                log("starting");
                for (Listener li : listeners) {
                    li.startup(optionFilename, gdbDirname);
                }
                log("startup done");
                setVisible(false);
                doneLoading = true;
            }
        });
        t.start();
    }

    public void loadFileNames() {
        optionFilename = "m17/KONAMI-WIN32WE9KOPT";
        gdbDirname = "";
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
        menu.add(exitItem);
        mb.add(menu);
        setJMenuBar(mb);
    }
 
    private void setIcon() {
        URL localURL = getClass().getResource("data/icon.png");
        if (localURL != null) {
            ImageIcon localImageIcon = new ImageIcon(localURL);
            setIconImage(localImageIcon.getImage());
        }
    }
          
    public static void main(String args[]) {
        new MapMakerStartup().onStartup(new MapMakerStartup.Listener() {
            public void startup(String optionFilename, String gdbDirname) {
                new MapMaker(optionFilename, gdbDirname);
            }
        });
    }
}
