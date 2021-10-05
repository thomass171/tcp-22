package de.yard.threed.javacommon;


import de.yard.threed.core.ImageData;
import de.yard.threed.core.buffer.ByteArrayInputStream;
import de.yard.threed.core.buffer.NativeOutputStream;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.core.resource.ResourceSaveException;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Nur fuer Desktop, denn BufferedImage und java.awt.* gibt es nicht in Android. Und in GWT doch auch nicht?  Und Unity schon gar nicht.
 * 6.4.16: TODO: Das muss zumindest zum Teil wie der StringHelper ueber Interface in die Platform.
 * 24.5.16: Fuer in der Platform common gibt es ImageFactory.
 * <p/>
 * Created by thomass on 03.11.15.
 */
public class ImageUtil {
    static Log logger = Platform.getInstance().getLog(ImageUtil.class);

    /**
     * 24.5.16: Fuer innerhalb der Platformen, die pngs lesen koennen.
     */
    public static ImageData drawString(ImageData image, String text, int x, int y, de.yard.threed.core.Color textcolor, String font, int fontsize) {

        BufferedImage img = buildBufferedImage(image);
        addText(img, text, x, y, new Color(textcolor.getR(), textcolor.getG(), textcolor.getB(), textcolor.getAlpha()), font, fontsize);
        return buildImageData(img);
    }

    public static BufferedImage buildBufferedImage(ImageData image) {
        int width = image.width;
        int height = image.height;
        BufferedImage img = new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //TODO alpha
                img.setRGB(x, y, image.pixel[y * width + x]);
            }
        }
        return img;
    }

    /**
     * BufferedImage hat so wie ImageData (0,0) links oben
     *
     * @param image
     * @return
     */
    public static ImageData buildImageData(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // getrgb() liefert auch den alpha wert in TYPE_INT_ARGB.
                pixels[y * width + x] = image.getRGB(x, y);
            }
        }
        return new ImageData(width, height, pixels);
    }

    /**
     * 24.5.16: Fuer ausserhalb der Platform, muss nach desktop TODO oder? JME etc brauchen das auch?
     *
     * @param bufferedimage
     * @param text
     * @param x
     * @param y
     * @param textcolor
     * @param fontsize
     */
    public static void addText(BufferedImage bufferedimage, String text, int x, int y, Color textcolor, String fontname, int fontsize) {
        Graphics2D g2 = bufferedimage.createGraphics();
        // siehe http://docs.oracle.com/javase/tutorial/2d/text/renderinghints.html
        // AA ausschalten, damit man die resultierende Bitmap später mit Bordmittlen mit andferen zusammenführen kann (z.B. Imagedata.overlay)
        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF/*RenderingHints.VALUE_TEXT_ANTIALIAS_GASP*/);

        Font font = new Font(fontname, Font.PLAIN, fontsize);
        g2.setFont(font);
        g2.setColor(textcolor);
        g2.drawString(text, x, y);
        //g2.drawLine(0, 0, 30, 30);
    }

    public static BufferedImage loadImageFromFile(InputStream is, String name) {
        return ImageUtils.loadImageFromFile(logger, is, name);
    }

    public static BufferedImage loadImageFromFile(NativeResource resource) {
        try {
            final InputStream is = FileReader.getFileStream(resource);
            return loadImageFromFile(FileReader.getInputStream(is), resource.getFullName());
        } catch (IOException e) {
            logger.error("loadImageFromFile: ImageIO.read failed for " + resource.getFullName());
            return null;
        }
    }

    /**
     * Read file.
     *
     * Return null in the case of error (already logged)
     *
     * @param file
     * @return
     */
    public static LoadedImage loadPNG(NativeResource file) {

        BufferedImage bi = null;
        ByteBuffer bb = null;
        //  try {
        LoadedImage loadedimage = null;

        bi = ImageUtil.loadImageFromFile(file);
        if (bi == null) {
            return null;
        }

        //logger.debug(String.format("loadFromFile took %d ms", System.currentTimeMillis() - starttime));
        int[] pxl = bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), null, 0, bi.getWidth());
        bb = BufferHelper.buildTextureBuffer(bi.getWidth(), bi.getHeight(), pxl,4);
        //logger.debug("ByteBuffer created");

        return new LoadedImage(bi.getWidth(), bi.getHeight(), bb);
    }
}
