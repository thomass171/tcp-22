package de.yard.threed.core;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;


/**
 * Ob es guenstig ist, hier Logik drinzuhaben, ist offen. Schliesslich liegt es in common und wird auch in Platform verwendet.
 * <p/>
 * 20.10.15: Auch ist die Verwendung dieser Klasse bzw. alles was daranhängt nicht für z.B. echte Images geeignet. Das sieht
 * man schon an der Größe der myargb Dateien. Viel zu gross!
 * <p/>
 * 05.09.16: Fuer die Konvertierung zwischen verschiedenen Klassen, wenn es denn sein muss, ist sie aber vielleicht doch ganz gut? Aber nicht in core.
 * 1.3.17: Och, wer weiss. Für Manipulationen ala FontMap ganz gut.
 * 20.3.17: Und für das Erstellen dynamischer Normalmaps vielleicht ja auch.
 * 
 * Created by thomass on 29.09.15.
 */
public class ImageData {
    static Log logger = Platform.getInstance().getLog(ImageData.class);

    // pixel sind im Format ARGB. Das Image ist hier von oben nach unten enthalten. HTML Canvase hat 0,0 links oben
    public int[] pixel;
    public int width;
    public int height;
    // 27.4.16 Der buf ist eine Krueckerspeziell für Unity PDFs. TODO: Muss wieder raus.
    @Deprecated
    public byte[] buf;

    @Deprecated
    public ImageData(byte[] buf) {
        this.buf = buf;
    }
    
    public ImageData(int width, int height, int[] pixel) {
        this.width = width;
        this.height = height;
        this.pixel = pixel;
    }

    public ImageData(int width, int height) {
        this.width = width;
        this.height = height;
        int size = width*height;
        this.pixel = new int[size];
    }
    
    /**
     * Erstellen aus myargb Format. Still needed for something like "Fontmap.txt".
     * @param ins
     */
    public static ImageData buildFromMyargb(String ins) {
        String[] data = StringUtils.split(ins, "\n");
        int width = Util.parseInt(data[0]);
        int height = Util.parseInt(data[1]);
        logger.info("buildFromMyargb: image loaded. length=" + data.length + ", width=" + width + ", height=" + height);
        int[] pixel = new int[data.length - 2];
        for (int i = 2; i < data.length; i ++) {
            pixel[i-2] = Util.parseInt(data[i]);
            if (pixel[i-2] != 0) {
                //logger.info("image loaded. pixel i="+pixel[i]);
            }
        }
        //logger.info("image loaded. pixel0=" + pixel[0]);
        //logger.info("image loaded. pixel10=" + pixel[10]);
        if (pixel.length != width * height) {
            //TO DO andere Fehlerbehandlung
            throw new RuntimeException("invalid pixel count "+pixel.length+", expected "+width*height);
        }
        return new ImageData(width, height, pixel);
    }
    
    /**
     * Das darueberzulegende Image wird evtl. unten oder rechts abgeschnitten.
     */
    public void overlayImage(ImageData image, int xp, int yp) {
        int overlaywidth = Math.min(image.width, width - xp);
        int overlayheight = Math.min(image.height, height - yp);
        //logger.debug("overlaywidth=" + overlaywidth + ",overlayheight=" + overlayheight);
        //loadPerRow(pixel, y, x, image, overlaywidth, overlayheight);
        for (int y = 0; y < overlayheight; y++) {
            for (int x = 0; x < overlaywidth; x++) {
                pixel[(yp + y) * width + (xp + x)] = image.pixel[(y) * image.width + (x)];
            }
        }
    }

    public ImageData getSubimage(int xp, int yp, int width, int height) {
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int srcpos = (yp + y) * this.width + (xp + x);
                int destpos = y * width + x;
                //logger.debug("destpos="+destpos+",srcpos="+srcpos+",pixel="+pixel[srcpos]);
                pixels[destpos] = pixel[srcpos];

            }
        }
        ImageData image = new ImageData(width, height, pixels);
        return image;
    }

    public Color getColor(int x, int y){
        return new Color(pixel[y * width + x]);
    }

    public void setColor(int x, int y, Color color){
        pixel[y * width + x] = color.getARGB();
    }
    



    /**
     * Die als voll transparent markierten Pixel mit einer Farbe versehen. Der Alphawert kommt aus der Farbe.
     *
     * @param c
     */
    public void setTransparentToColor(Color c) {
        for (int i = 0; i < pixel.length; i++) {
            if ((pixel[i] >> 24) == 0x00) {
                pixel[i] = c.getARGB();
            }
        }
    }

    public void replaceColor(Color existing, Color newcolor) {
        for (int i = 0; i < pixel.length; i++) {
            if (pixel[i] == existing.getARGB()) {
                pixel[i] = newcolor.getARGB();
            }
        }
    }

    public static ImageData buildSingleColor(int width, int height, Color color) {
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = color.getARGB();
            }
        }
        ImageData image = new ImageData(width, height, pixels);
        return image;
    }



}
