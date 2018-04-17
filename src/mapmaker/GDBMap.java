package mapmaker;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;

public class GDBMap
{
    static class Entry {
        String[] values;
        String comment;

        Entry(String[] values, String comment) {
            this.values = values;
            this.comment = comment;
        }

        public String toString() {
            String val = String.join(",", values);
            if (comment != null) {
                val = val + " " + comment;
            }
            return val;
        }
    }

    Map<Integer,Entry> map;

    public GDBMap() {
        map = new HashMap<Integer,Entry>();
    }

    public void load(String filename) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "iso8859-1"));
        String line = null;
        Pattern pattern = Pattern.compile("^\\s*([0-9]+)\\s*,\\s*(\"[^\"]*\"|[^,#]*)(\\s*,[^#]*)?\\s*(#.*)?$");
        Pattern pattern2 = Pattern.compile("\\s*,\\s*(\"[^\"]*\"|[^,]*)");
        while ((line = br.readLine()) != null) {
            line = line.trim();
            Matcher matcher = pattern.matcher(line);

            matcher.reset();
            while (matcher.find()) {
                int key = Integer.parseInt(matcher.group(1));
                List<String> values = new ArrayList<String>();
                values.add(matcher.group(2));
                int s = matcher.start(3);
                int e = matcher.end(3);
                String val = matcher.group(3);
                if (val != null) {
                    Matcher m = pattern2.matcher(line.substring(s, e));
                    while (m.find()) {
                        values.add(m.group(1));
                    }
                }
                String comment = matcher.group(4);
                String[] vals = new String[values.size()];
                values.toArray(vals);
                Entry entry = new Entry(vals, comment);
                map.put(key, entry);
            }
        }
        br.close();
    }

    public void save(String filename) {
    }

    public void print() {
        System.out.println("# GDBMap");
        for (Map.Entry<Integer,Entry> e : map.entrySet()) {
            System.out.println(e.getKey() + "," + e.getValue());
        }
    }

    public String getFirst(int id) {
        Entry e = map.get(id);
        if (e != null) {
            return e.values[0];
        }
        return null;
    }

    public Entry get(int id) {
        return map.get(id);
    }

    public void put(int id, Entry e) {
        map.put(id, e);
    }

    public Entry remove(int id) {
        return map.remove(id);
    }

    public static void main(String args[]) throws Exception {
        // test
        GDBMap map = new GDBMap();
        map.load(args[0]);
        map.print();
    }
}
