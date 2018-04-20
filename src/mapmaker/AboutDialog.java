package mapmaker;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import java.net.URL;


public class AboutDialog extends JDialog {

    JButton ok;

    public AboutDialog(JFrame parent, String version) {
        super(parent, "About GDB Map Maker", false);
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
        URL localURL = getClass().getResource("data/splash.jpg");
        if (localURL != null) {
            ImageIcon localImage = new ImageIcon(localURL);
            JLabel lab = new JLabel(localImage);
            lab.setAlignmentX(Component.CENTER_ALIGNMENT);
            pane.add(lab);
        }
        pane.add(Box.createVerticalStrut(8));
        JLabel l1 = new JLabel("GDB Map Maker");
        l1.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l2 = new JLabel("version " + version + ", from April 2018");
        l2.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l3 = new JLabel("by juce");
        l3.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l4 = new JLabel("for PC games:");
        l4.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l5 = new JLabel("Pro Evolution Soccer 5");
        l5.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l6 = new JLabel("Winning Eleven 9");
        l6.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l7 = new JLabel("Winning Eleven 9 Liveware Evolution");
        l7.setAlignmentX(Component.CENTER_ALIGNMENT);
        pane.add(l1);
        pane.add(l2);
        pane.add(l3);
        pane.add(Box.createVerticalStrut(12));
        pane.add(l4);
        pane.add(l5);
        pane.add(l6);
        pane.add(l7);
        pane.add(Box.createVerticalStrut(8));

        ok = new JButton("Ok");
        ok.setAlignmentX(Component.CENTER_ALIGNMENT);
        pane.add(ok);
        pane.add(Box.createVerticalStrut(8));

        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
                dispose();
            }
        });
        addWindowListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                focusOnButton();
            }
        });

        getContentPane().add(pane);
        pack();
        focusOnButton();
        setLocationRelativeTo(null);
        setModal(true);
    }

    public void focusOnButton() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ok.requestFocusInWindow();
            }
        });
    }
}


