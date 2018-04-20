package mapmaker;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;


public class Settings {
    public String optionFilename;
    public String gdbDirname;
    public String mapOutputEncoding;

    private String filename;

    public static final String INI_FILE = "mapmaker.ini";
    public static final String DEFAULT_OUTPUT_ENCODING = "iso-8859-1";

    public Settings(String filename) {
        this.filename = filename;
        load();
    }
    
    public Settings() {
        this(INI_FILE);
    }

    public void load() {
        mapOutputEncoding = DEFAULT_OUTPUT_ENCODING;
        try {
            int count = 0;
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(INI_FILE), "utf-8"));
            while ((line = br.readLine()) != null) {
                line = line.trim();
                count++;
                // new format: name = value
                String[] tokens = line.split("=",2);
                if (tokens.length == 2) {
                    String key = tokens[0].trim();
                    String value = tokens[1].trim();

                    if ("option.file".equals(key)) {
                        optionFilename = value;
                    }
                    else if ("gdb.dir".equals(key)) {
                        gdbDirname = value;
                    }
                    else if ("map.output.encoding".equals(key)) {
                        mapOutputEncoding = value;
                    }
                }
                else if (line.charAt(0)!='#') {
                    // check old format
                    if (count == 1 && line != null) {
                        optionFilename = line;
                    }
                    else if (count == 2 && line != null) {
                        gdbDirname = line;
                    }
                    else if (count == 3 && line != null) {
                        mapOutputEncoding = line;
                    }
                }
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
        System.out.println("Using mapOutputEncoding: " + mapOutputEncoding);
    }

    public void save() {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(INI_FILE), "utf-8"));
            bw.write("option.file = " + optionFilename + "\r\n");
            bw.write("gdb.dir = " + gdbDirname + "\r\n");
            bw.write("map.output.encoding = " + mapOutputEncoding + "\r\n");
            bw.close();
        }
        catch (IOException e1) {
            System.out.println("Warning: cannot save " + INI_FILE);
        }
    }
}
