package de.yard.threed.javacommon;


import org.apache.commons.io.FileUtils;
import org.apache.hc.client5.http.utils.Base64;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by thomass on 22.02.17.
 */
public class FileUtil {
    /**
     *3.01.19: Throw exception instead of hiding it
     */
    public static void saveToPngFile(BufferedImage image, String filename) throws IOException {
        File outfile = new File(filename);
        //try {
            ImageIO.write(image, "png", outfile);
        /*} catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    /*10.12.18 public static void saveToPngFile(ImageData img, String filename) {
        BufferedImage image = ImageUtil.buildBufferedImage(img);
        File outfile = new File(filename);
        try {
            ImageIO.write(image, "png", outfile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void saveToPngFile(NormalMap img, String filename) {
        saveToPngFile(img.image, filename);
    }

    public static void saveToPngFile(HeightMap img, String filename) {
        saveToPngFile(img.image, filename);
    }*/

    /**
     * Moved here from jme main.
     */
    private static void encodeBase64(String path, String filename) {
        try {
            byte[] buf = FileUtils.readFileToByteArray(new File(path + "/" + filename));
            buf = Base64.encodeBase64(buf);
            File outfile = new File(path + "/" + filename + ".b64");
            FileUtils.writeByteArrayToFile(outfile, buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
