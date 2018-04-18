package mapmaker;

import java.awt.image.BufferedImage;
import java.util.zip.Inflater;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.SampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class BinReader
{
    private static int fourBytesToInt(byte[] data, int offset) {
        int value = data[offset] & 0xFF;
        value |= (data[offset+1] << 8) & 0xFFFF;
        value |= (data[offset+2] << 16) & 0xFFFFFF;
        value |= (data[offset+3] << 24) & 0xFFFFFFFF;
        return value;
    }

    private static int twoBytesToInt(byte[] data, int offset) {
        int value = data[offset] & 0xFF;
        value |= (data[offset+1] << 8) & 0xFFFF;
        return value;
    }

    public static BufferedImage getFirstTexture(String filename) {
        try {
            return _getFirstTexture(filename);
        }
        catch (Exception e) {
            System.out.println("Warning: unable to get texture from BIN: " + filename);
        }
        return null;
    }

    private static BufferedImage _getFirstTexture(String filename) throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(filename));

        // get compressed and decompressed sizes
        int compressedSize = fourBytesToInt(bytes, 4);
        int decompressedSize = fourBytesToInt(bytes, 8);

        // Decompress the bytes
        Inflater decompresser = new Inflater();
        decompresser.setInput(bytes, 0x20, compressedSize);
        byte[] result = new byte[decompressedSize];
        int resultLength = decompresser.inflate(result);
        decompresser.end();

        // read TOC and look for texture
        int texStructOffset = 0;
        int numItems = fourBytesToInt(result, 0);
        final byte texSig[] = { (byte)0x94, 0x72, (byte)0x85, 0x29, 0x01 };
        boolean found = false;
        for (int i=0; i<numItems; i++) {
            texStructOffset = fourBytesToInt(result, 8 + 4*i);
            found = true;
            for (int j=0; j<texSig.length; j++) {
                if (texSig[j] != result[texStructOffset + j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                break;
            }
        }

        // texture size
        int w = twoBytesToInt(result, texStructOffset + 0x14);
        int h = twoBytesToInt(result, texStructOffset + 0x16);

        // adjust palette color banks
        int paletteOffset = texStructOffset + 0x80;
        byte[] tmp = new byte[8*4];
        for (int i=0; i<8; i++) {
            int bank2 = i*32*4 + 1*8*4;
            int bank3 = i*32*4 + 2*8*4;
            System.arraycopy(result, paletteOffset + bank2, tmp, 0, 32);
            System.arraycopy(result, paletteOffset + bank3, result, paletteOffset + bank2, 32);
            System.arraycopy(tmp, 0, result, paletteOffset + bank3, 32);
        }

        // pixels: extract larger texture
        int dataOffset = paletteOffset + 0x400;
        byte[] buffer = new byte[w*h*3];
        for (int i=0; i<h; i++) {
            for (int j=0; j<w; j++) {
                byte ci = result[dataOffset + i*w + j];
                int colorIndex = (ci < 0) ? 256+ci : ci;
                int off = 3*(i*w + j);
                buffer[off+0] = result[paletteOffset + colorIndex*4 + 0];
                buffer[off+1] = result[paletteOffset + colorIndex*4 + 1];
                buffer[off+2] = result[paletteOffset + colorIndex*4 + 2];
            }
        }

        // create new image object
        return getImage(w, buffer);
    }

    /**
     *
     * @param width The image width (height derived from buffer length)
     * @param buffer The buffer containing raw RGB pixel data
     *
     * @return The image
     */
    public static BufferedImage getImage(int width, byte[] buffer) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
        int[] nBits = { 8, 8, 8 };
        int height = buffer.length / width / nBits.length;
        ColorModel cm = new ComponentColorModel(cs, nBits, false, true,
                Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        SampleModel sm = cm.createCompatibleSampleModel(width, height);
        DataBufferByte db = new DataBufferByte(buffer, width * height);
        WritableRaster raster = Raster.createWritableRaster(sm, db, null);
        BufferedImage result = new BufferedImage(cm, raster, false, null);
        return result;
    }
}
