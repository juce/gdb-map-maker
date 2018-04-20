package mapmaker;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.imageio.ImageIO;

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

                // Create label
                JLabel img=new JLabel();
                img.setPreferredSize(new Dimension(imageWidth,imageHeight));
                jfc.setAccessory(img);
                jfc.setAcceptAllFileFilterUsed(false);

                // Add property change listener
                jfc.addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(final PropertyChangeEvent pe) {
                        SwingWorker<Image,Void> worker=new SwingWorker<Image,Void>(){
                            // The image processing method
                            protected Image doInBackground() {
                                // If selected file changes..
                                if(pe.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                                    File f=jfc.getSelectedFile();
                                    String path = f.getAbsolutePath();
                                    if (path.endsWith(".bin")) {
                                        BufferedImage bi = BinReader.getFirstTexture(f.getAbsolutePath());
                                        if (bi != null) {
                                            return bi.getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
                                        }
                                        else {
                                            img.setText(" No preview");
                                        }
                                    }
                                    else {
                                        try {
                                            // Expecting an image
                                            FileInputStream fin=new FileInputStream(f);
                                            BufferedImage bi=ImageIO.read(fin);
                                            return bi.getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
                                        }
                                        catch(Exception e){
                                            // If there is a problem reading image,
                                            // it might not be a valid image or unable
                                            // to read
                                            img.setText(" No preview");
                                        }
                                    }
                                }
                                return null;
                            }
                            protected void done() {
                                try {
                                    Image i=get(1L, TimeUnit.NANOSECONDS);
                                    if (i==null) return;
                                    img.setIcon(new ImageIcon(i));
                                }
                                catch(Exception e) {
                                    img.setText(" Preview error");
                                }
                            }
                        };
                        worker.execute();
                    }
                });

                int returnValue = jfc.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    String newFullPath = selectedFile.getAbsolutePath();
                    System.out.println(newFullPath);
                    String newValue = "\"" + getRelPath(baseDir, newFullPath) + "\"";
                    setChoice(newValue);

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
        text.setForeground(Color.black);
        button.setEnabled(true);
        if (choiceText != null) {
            text.setText(choiceText);
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
