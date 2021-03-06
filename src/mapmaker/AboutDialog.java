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
        super(parent, "About GDB Map Maker", true);
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
        JLabel l30 = new JLabel("Special thanks to:");
        JLabel l31 = new JLabel("Compulsion (www.purplehaze.eclipse.co.uk)");
        JLabel l32 = new JLabel("and lazanet (github.com/lazanet)");
        JLabel l33 = new JLabel("for code to decrypt Option file");
        l30.setAlignmentX(Component.CENTER_ALIGNMENT);
        l31.setAlignmentX(Component.CENTER_ALIGNMENT);
        l32.setAlignmentX(Component.CENTER_ALIGNMENT);
        l33.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l4 = new JLabel("Supported PC games:");
        l4.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l5 = new JLabel("Pro Evolution Soccer 5, Winning Eleven 9");
        l5.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l6 = new JLabel("Winning Eleven 9 Liveware Evolution");
        l6.setAlignmentX(Component.CENTER_ALIGNMENT);
        pane.add(l1);
        pane.add(l2);
        pane.add(l3);
        pane.add(Box.createVerticalStrut(8));
        pane.add(l30);
        pane.add(l31);
        pane.add(l32);
        pane.add(l33);
        pane.add(Box.createVerticalStrut(12));
        pane.add(l4);
        pane.add(l5);
        pane.add(l6);
        pane.add(Box.createVerticalStrut(8));

        ok = new JButton("Ok");
        ok.setAlignmentX(Component.CENTER_ALIGNMENT);
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
                dispose();
            }
        });
        pane.add(ok);
        pane.add(Box.createVerticalStrut(8));

        getContentPane().add(pane);
        pack();
        SwingUtilities.getRootPane(ok).setDefaultButton(ok);
        setLocationRelativeTo(null);
        setModal(true);
    }
}


