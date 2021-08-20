package de.yard.threed.core;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;


/**
 * Platformübergreifende ImageData Methoden. Liegt in der Platform, damit die Methoden auch aus der Platform
 * und nicht nur aus der Engine verwendet werden können.
 * <p/>
 * 19.5.16: Da aber z.B. Unity keinen buildTextImage bzw. überhaupt keine draw Funktionen hat, ist der Nutzen dieser Klasse an dieser Stelle fraglich. Ausser fuer ganz simples.
 * 21.5.16: Zum Teil nach desktop tools verschoben um da Imagedateien zu erzeugen. Ist dann natuerlich statisch. Hier sind jetzt nur noch Methoden, die wirklich in
 * jeder Platform verwendet werden koennen.
 *
 * 09.10.19: Nachfolger ist NativeCanvas, der mehr in der Plaform macht. Mit Imagedata ausserhalb der Platform ist viel zu ressourcenhungrig.
 * <p/>
 * Created by thomass on 29.09.15.
 */
public class ImageFactory {
    //MA36 static Platform pf = (Platform) Platform.getInstance();
    static Log logger = Platform.getInstance().getLog(ImageData.class);
    private static ImageFactory instance = null;
    //private HashMap<String, Texture> textures = new HashMap<String, Texture>();
    //MA36 static Log logger = pf.getLog(ImageFactory.class);
    
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

    public static ImageData buildStripes(int striptcnt, int stripwidth, int height, Color color1, Color color2) {
        int width = striptcnt * stripwidth;

        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            Color color = color1;

            for (int strip = 0; strip < striptcnt; strip++) {
                for (int x = strip * stripwidth; x < (strip + 1) * (stripwidth + 0); x++) {
                    pixels[y * width + x] =
                            color.getARGB();
                }
                if (color == color1)
                    color = color2;
                else
                    color = color1;
            }
        }
        ImageData image = new ImageData(width, height, pixels);
        return image;
    }







}
