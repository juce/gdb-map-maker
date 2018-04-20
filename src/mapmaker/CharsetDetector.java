package mapmaker;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class CharsetDetector {

    public Charset detect(String s, String[] charsets) {
        for (String charsetName : charsets) {
            Charset charset = detect(s, Charset.forName(charsetName));
            if (charset != null) {
                return charset;
            }
        }
        return null;
    }

    private Charset detect(String s, Charset charset) {
        try {
            byte[] buffer = Files.readAllBytes(Paths.get(s));
            CharsetDecoder decoder = charset.newDecoder();
            decoder.decode(ByteBuffer.wrap(buffer));
        } catch (Exception e) {
            return null;
        }
        return charset;
    }

    public static void main(String[] args) {
        String[] charsetsToBeTested = {"utf-8", "iso-8859-1", "windows-1251", "windows-1252", "windows-1253", "iso-8859-7"};
        Charset charset = new CharsetDetector().detect(args[0], charsetsToBeTested);

        if (charset != null) {
            System.out.println(charset);
        }
        else {
            System.out.println("Unrecognized charset.");
        }
    }
}
