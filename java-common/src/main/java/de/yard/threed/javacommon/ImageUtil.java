package de.yard.threed.javacommon;


import de.yard.threed.core.ImageData;
import de.yard.threed.core.resource.NativeResource;
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
    public static Cache pngcache = null;

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
     * Read image from "some location" or cache and optionally add it to cache. Uses Javas ImageIO, so only works with formats supported by ImageIO.
     * ImageIO is not very fast (both png and jpg). For having short turnaround times during development, it seems acceptable to cache
     * more efficient raw data for the price of disk space.
     * <p>
     * For example for "2_no_clouds_4k.jpg" (1,4MB) we have 33,6MB cached file and load time reduction from >2000ms to 343ms.
     * <p>
     * The main use case for this method is loading a texture for Java platforms. But also some scenery tools might use it. And caching
     * might be an option on platforms where the resources reside in tar/zip archives (Android). Even HTTP could be possible in principle, though not implemented.
     * So for maximum flexibility NativeResource is used instead of java.io.File.
     *
     * Returning a byte buffer (LoadedImage) doesn't provide a real benefit because subsequent conversions are still needed
     * (eg. JME has its own internal structure).
     * <p>
     * Maybe caching was used once for hiding conversions of strange formats like "rgb" (called "preprocessed"?). But that is no valid
     * use case any more (if it ever existed).
     * </p>
     * <p>
     * Returns null in the case of error (already logged)
     *
     * @param file
     * @return
     */
    public static BufferedImage loadCachableImage(NativeResource file) {

        if (System.getProperty("enablePngCaching") != null && pngcache == null) {
            pngcache = new Cache("png-cache", System.getProperty("user.home") + "/tmp");
        }
        BufferedImage li;
        if (pngcache != null) {
            // check cache for file
            li = pngcache.getCachedImage(file);
            if (li != null) {
                return li;
            }
        }

        BufferedImage bi = ImageUtil.loadImageFromFile(file);
        if (bi == null) {
            return null;
        }

        if (pngcache != null) {
            logger.debug("adding file " + file.getName() + " to cache");

            try {
                pngcache.saveImage(file, bi);
            } catch (Exception e) {
                logger.error("saveCachedObject failed:" + e.getMessage(), e);
                return null;
            }
        }
        return bi;
    }
}
