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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


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

    OptionFile of;

    private int fourBytesToInt(byte[] data, int offset) {
        int value = data[offset] & 0xFF;
        value |= (data[offset+1] << 8) & 0xFFFF;
        value |= (data[offset+2] << 16) & 0xFFFFFF;
        value |= (data[offset+3] << 24) & 0xFFFFFFFF;
        return value;
    }

    private String bytesUtf16ToString(byte[] data, int offset, int len) {
        try {
            String s = new String(data, offset, len, "UTF-16LE");
            int zeroIndex = s.indexOf('\0');
            if (zeroIndex != -1) {
                s = s.substring(0, zeroIndex);
            }
            return s;
        }
        catch (UnsupportedEncodingException e) {
            return "<**BAD*UTF16*STRING*>";
        }
    }

    private String bytesUtf8ToString(byte[] data, int offset, int len) {
        try {
            String s = new String(data, offset, len, "UTF-8");
            int zeroIndex = s.indexOf('\0');
            if (zeroIndex != -1) {
                s = s.substring(0, zeroIndex);
            }
            return s;
        }
        catch (UnsupportedEncodingException e) {
            return "<**BAD*UTF8*STRING*>";
        }
    }
/*
def get_club_names(s, e):
    d = {}
    for i in range((e - s)/0x8c):
        name = data[s + i*0x8c : s + i*0x8c + 0x31]
        name = name[:name.find('\0')]
        if name:
            name = name.decode('utf-8')
        else:
            # default, unmodified
            name = data[s + i*0x8c + 0x31, s + i*0x8c + 0x41]
            name = name[:name.find('\0')]
            if name:
                name = name.decode('utf-8')
        abbr = data[s + i*0x8c + 0x49 : s + i*0x8c + 0x49 + 3]
        if abbr == '\0\0\0':
            abbr = data[s + i*0x8c + 0x4d : s + i*0x8c + 0x4d + 3]
        d[64 + i] = (abbr, name)
    return d
*/

    private Team[] getClubTeams(OptionFile of) {
        int s = fourBytesToInt(of.data, 0x174);
        int e = fourBytesToInt(of.data, 0x178);
        int n = (e - s)/0x8c;

        Team[] t = new Team[n];

        for (int i=0; i<n; i++) {
            String name = bytesUtf8ToString(of.data, s + i*0x8c, 0x31);
            if (name.equals("")) {
                name = bytesUtf8ToString(of.data, s + i*0x8c + 0x81, 0x10);
            }
            String abbr = bytesUtf8ToString(of.data, s + i*0x8c + 0x49, 3);
            if (abbr.equals("")) {
                abbr = bytesUtf8ToString(of.data, s + i*0x8c + 0x4d, 3);
            }

            t[i] = new Team(i+64, abbr, name, true); 
        }
        return t;
    }
 
    private Player[] getPlayers(OptionFile of) {
        // regular players
        int s = fourBytesToInt(of.data, 0x154);
        int e = fourBytesToInt(of.data, 0x14c);
        int n = (e - s)/0x7c;
        // created players
        int cs = fourBytesToInt(of.data, 0x148);
        int ce = fourBytesToInt(of.data, 0x150);
        int cn = (ce - cs)/0x7c;

        Player[] p = new Player[n + cn];

        for (int i=0; i<n; i++) {
            String name = bytesUtf16ToString(of.data, s + i*0x7c, 0x20);
            String shirtName = bytesUtf8ToString(of.data, s + i*0x7c + 0x20, 0x10);
            p[i] = new Player(i, name, shirtName);
        }
        for (int i=0; i<cn; i++) {
            String name = bytesUtf16ToString(of.data, cs + i*0x7c, 0x20);
            String shirtName = bytesUtf8ToString(of.data, cs + i*0x7c + 0x20, 0x10);
            p[n + i] = new Player(i+32768, name, shirtName);
        }
        return p;
    }

    private Team[] getTeams(OptionFile of) {
        Team[] t = getClubTeams(of);
        return t;
    }

    private void populatePlayerList(OptionFile of) {
        Player[] p = getPlayers(of);
        playerList.setListData(p);
    }

    private void populateTeamSelector(OptionFile of) {
        Team[] t = getTeams(of);
        for (int i=0; i<t.length; i++) {
            teamSelectorModel.addElement(t[i]);
        }
    }

    public PlayersPanel() {
        super();

        filtersPanel = new JPanel(new GridLayout(3,1));
        nameSearchText = new JTextField("Name", 10);
        teamSelectorModel = new DefaultComboBoxModel<Team>();
        teamSelector = new JComboBox(teamSelectorModel);
        teamSelector.addItem("<Any team>");
        nationalitySelector = new JComboBox();
        nationalitySelector.addItem("<Any nationality>");
        filtersPanel.add(nameSearchText);
        filtersPanel.add(teamSelector);
        filtersPanel.add(nationalitySelector);
        add(filtersPanel);

        resultsPanel = new JPanel();
        playerListModel = new DefaultListModel<Player>();
        playerList = new JList(playerListModel);
        resultsPanel.add(new JScrollPane(playerList));
        add(resultsPanel);

        of = new OptionFile();
        //of.readXPS(new File("m17/KONAMI-WIN32WE9KOPT"));
        of.readXPS(new File("KONAMI-WIN32WE9KOPT"));

        populatePlayerList(of);
        populateTeamSelector(of);
    }
}
 
