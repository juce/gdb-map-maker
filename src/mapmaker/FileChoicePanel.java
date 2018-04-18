package mapmaker;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileChoicePanel extends JPanel {

    String baseDir;
    JButton xbutt;
    JButton button;
    JLabel text;
    JLabel image;
    List<Listener> listeners;

    public interface Listener {
        public void valueChanged(String value);
    }

    public FileChoicePanel(String baseDir, String buttonText, String choiceText, String filterDesc, String ext) {
        super();
        this.baseDir = baseDir;
        listeners = new ArrayList<Listener>();

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        xbutt = new JButton("x");
        xbutt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                text.setText("None");
                if (image != null) {
                    remove(image);
                }
                for (Listener li: listeners) {
                    li.valueChanged(null);
                }
            }
        });

        button = new JButton(buttonText);
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

                    if (image != null) {
                        remove(image);
                    }
                    if (newFullPath.endsWith(".png")) {
                        ImageIcon localImage = new ImageIcon(newFullPath);
                        image = new JLabel(localImage);
                        add(image);
                    }
                    else if (newFullPath.endsWith(".bin")) {
                        ImageIcon localImage = new ImageIcon(BinReader.getFirstTexture(newFullPath));
                        image = new JLabel(localImage);
                        add(image);
                    }

                    // notify listeners
                    for (Listener li : listeners) {
                        li.valueChanged(newValue);
                    }
                }
            }
        });
        text = new JLabel(choiceText);
        add(xbutt);
        add(button);
        add(text);
        setVisible(true);
        button.setEnabled(false);
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
        text.setText(choiceText);
        button.setEnabled(true);
        if (image != null) {
            remove(image);
        }
        File f = new File(this.baseDir + "/" + choiceText.replace("\\","/").replace("\"",""));
        String newFullPath = f.getAbsolutePath();
        if (newFullPath.endsWith(".png")) {
            ImageIcon localImage = new ImageIcon(newFullPath);
            image = new JLabel(localImage);
            add(image);
        }
        else if (newFullPath.endsWith(".bin")) {
            ImageIcon localImage = new ImageIcon(BinReader.getFirstTexture(newFullPath));
            image = new JLabel(localImage);
            add(image);
        }
    }

    public void clear() {
        text.setText("");
        button.setEnabled(false);
        if (image != null) {
            remove(image);
        }
    }

    public void addListener(Listener li) {
        listeners.add(li);
    }
}
