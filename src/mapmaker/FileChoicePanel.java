package mapmaker;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileChoicePanel extends JPanel {

    String baseDir;
    boolean hasHD;
    JTabbedPane tpane;
    JPanel panel;
    JButton xbutt;
    JButton button;
    JPanel images;
    JLabel text;
    JLabel image;
    JLabel imageHD;
    int imageWidth;
    int imageHeight;
    List<Listener> listeners;

    public interface Listener {
        public void valueChanged(String value);
    }

    public FileChoicePanel(String baseDir, String title, boolean hasHD, int imageWidth, int imageHeight, String choiceText, String filterDesc, String ext) {
        super();
        this.baseDir = baseDir;
        this.hasHD = hasHD;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        listeners = new ArrayList<Listener>();

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        xbutt = new JButton("x");
        xbutt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                text.setText("None");
                if (images != null) {
                    panel.remove(images);
                    images = null;
                }
                for (Listener li: listeners) {
                    li.valueChanged(null);
                }
            }
        });

        button = new JButton("Choose");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String relPath = text.getText().replace("\\","/").replace("\"","");
                File f = new File(baseDir + "/" + relPath);
                System.out.println(f);
                System.out.println(f.getParent());
                JFileChooser jfc = new JFileChooser(f.getParent());
                jfc.setDialogTitle("Choose file");
                jfc.setFileFilter(new FileNameExtensionFilter(filterDesc, ext));
                int returnValue = jfc.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    String newFullPath = selectedFile.getAbsolutePath();
                    System.out.println(newFullPath);
                    String newValue = "\"" + getRelPath(baseDir, newFullPath) + "\"";
                    text.setText(newValue);
                    text.setForeground(Color.black);

                    setImages(newValue);

                    // notify listeners
                    for (Listener li : listeners) {
                        li.valueChanged(newValue);
                    }
                }
            }
        });

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JPanel p1 = new JPanel();
        p1.setAlignmentX(Component.CENTER_ALIGNMENT);
        p1.setLayout(new BoxLayout(p1, BoxLayout.LINE_AXIS));
        p1.add(xbutt);
        p1.add(button);
        panel.add(p1);

        text = new JLabel(choiceText);
        text.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(text);
        panel.setVisible(true);
        button.setEnabled(false);

        tpane = new JTabbedPane();
        tpane.addTab(title, null, panel, null);
        add(tpane);
    }

    public String getRelPath(String base, String path) {
        base = base.replace("\\","/");
        path = path.replace("\\","/");
        Path pathAbsolute = Paths.get(path);
        Path pathBase = Paths.get(base);
        Path pathRelative = pathBase.relativize(pathAbsolute);
        return pathRelative.toString().replace("/","\\");
    }

    public void setChoice(String choiceText) {
        clear();
        if (choiceText != null) {
            text.setText(choiceText);
            text.setForeground(Color.black);
            button.setEnabled(true);
            setImages(choiceText);
        }
        else {
            text.setText("None");
        }
    }

    private void setImages(String choiceText) {
        images = new JPanel();
        images.setAlignmentX(Component.CENTER_ALIGNMENT);
        images.setLayout(new BoxLayout(images, BoxLayout.LINE_AXIS));
        images.add(Box.createRigidArea(new Dimension(0,imageHeight + 10)));

        File f = new File(this.baseDir + "/" + choiceText.replace("\\","/").replace("\"",""));
        String newFullPath = f.getAbsolutePath();
        if (newFullPath.endsWith(".png")) {
            ImageIcon localImage = new ImageIcon(newFullPath);
            if (localImage != null) {
                image = new JLabel(localImage);
                images.add(image);
            }
            else {
                text.setForeground(Color.red);
            }
        }
        else if (newFullPath.endsWith(".bin")) {
            BufferedImage bi = BinReader.getFirstTexture(newFullPath);
            if (bi != null) {
                ImageIcon localImage = new ImageIcon(bi);
                image = new JLabel(localImage);
                images.add(image);
            }
            else {
                text.setForeground(Color.red);
            }

            // check for HD image
            if (hasHD) {
                String hdPath = newFullPath.substring(0, newFullPath.length()-4) + ".png";
                File hdf = new File(hdPath);
                if (hdf.exists()) {
                    ImageIcon localImage = new ImageIcon(hdPath);
                    if (localImage != null) {
                        // scale for displaying side-by-side
                        Image img = localImage.getImage(); // transform it
                        Image newimg = img.getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
                        localImage = new ImageIcon(newimg);

                        imageHD = new JLabel(localImage);
                        images.add(Box.createRigidArea(new Dimension(10,0)));
                        images.add(imageHD);

                        // update text
                        text.setText(text.getText() + String.format("  HD: %dx%d", img.getWidth(null), img.getHeight(null)));
                    }
                }
            }
        }
        panel.add(images);
    }

    public void clear() {
        text.setText("");
        button.setEnabled(false);
        if (images != null) {
            panel.remove(images);
            images = null;
        }
    }

    public void addListener(Listener li) {
        listeners.add(li);
    }
}
