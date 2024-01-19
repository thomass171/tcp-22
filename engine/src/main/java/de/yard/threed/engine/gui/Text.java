package de.yard.threed.engine.gui;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.geometry.ProportionalUvMap;
import de.yard.threed.engine.Texture;
import de.yard.threed.core.geometry.UvMap1;
import de.yard.threed.core.platform.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Einfache Textdarstellung über Fontmap. Alternative zu Canvas basiertem, was in Unity problematisch ist.
 * <p>
 * Aus Testgründen und wegen Einheitlichkeit aber auch über die Fontmap
 * <p>
 * Das ist zwar für "Office" nicht schön, aber, naja, da muss für Unity eh eine andere Lösung her, evtl. per PDF dingens.
 * Created by thomass on 28.02.17.
 */
public class Text implements GuiTexture {
    static Log logger = Platform.getInstance().getLog(Text.class);

    //12.12.18 public static Texture texture = Texture.buildBundleTexture("core", "FontMap.png");
    private static Map<Integer, Text> iconnumber = new HashMap<Integer, Text>();
    private static ImageData fontmap;
    Texture texture;

    /**
     *
     */
    /*12.12.18 public Text(int x, int y) {
        this.x = x;
        this.y = y;
    }*/
    public Text(String text, Color textcolor, Color background) {
        ImageData imageData = buildTextImage(text, textcolor, background);
        texture = new Texture(imageData);
    }

    @Override
    public Texture getTexture() {
        return texture;
    }

    @Override
    public UvMap1 getUvMap() {
        return /*3.5.21 UvMap1.rightRotatedTexture*/new ProportionalUvMap();
    }

    public static ImageData buildTextImage(String text, Color textcolor, Color background) {
        ImageData image = buildLabelImage(text, textcolor, background);
        return image;
    }

    /**
     * Die Groesse des Image ergibt sich aus der Stringlaenge.
     * Ist zumindest fuer Platformen, die kein drawstring haben. Kann aber auch von anderen verwendet werden,
     * um eine einheitliche Darstellung zu haben.
     * 28.9.18: Ginge wohl auch effizienter per UVs, dann hat man aber eine Textur pro Character. Mit ImageData ist es etwas einfacher handhabbar, oder?
     * Alternative zu Canvas basiertem, was in Unity problematisch ist.
     */
    public static ImageData buildLabelImage(String label, /*Dimension size,*/ Color textcolor, Color background) {
        if (fontmap == null) {
            // myargb ist eigentlich nur für WebGl erforderlich. Aber für Einheitlichkeit und Tests kann man es auch immer verwenden.
            try {
                fontmap = ImageData.buildFromMyargb(BundleRegistry.getBundle("engine").getResource("FontMap.txt").getContentAsString());
            } catch (CharsetException e) {
                // TODO improved eror handling
                throw new RuntimeException(e);
            }
        }
        int basesize = 32;
        int width = StringUtils.length(label) * basesize / 2;
        int height = basesize;
        ImageData image = ImageFactory.buildSingleColor(width, height, background);

        for (int i = 0; i < StringUtils.length(label); i++) {
            char c = StringUtils.charAt(label, i);
            int ci = (int) c;
            ImageData chardata = fontmap.getSubimage((ci % 16) * 16, (ci / 16) * 32 - (2 * 32), 16, basesize);
            // Die Schrift in Fontmap ist schwarz auf transparent.
            chardata.replaceColor(Color.BLACK, textcolor);
            // 12.12.18: Background muss auch explizit gesetzt werden.
            chardata.replaceColor(Color.BLACK_FULLTRANSPARENT, background);
            image.overlayImage(chardata, i * basesize / 2, 0);
        }
        return image;

    }

    /**
     * 5.7.21: Moved here from ImageFactory
     *
     * @param label
     * @param size
     * @param background
     * @return
     */
    public static ImageData buildLabelImage(String label, Dimension size, Color background) {
        //ImageData image = buildSingleColor(size.getWidth(), size.getHeight(), background);

        // irgendwie zentrieren
        int fontsize = 36;
       /*24.5.16   int width = StringUtils.length(label) * 15;
        int height = fontsize * 2;
*/
        ImageData image ;//1.3.17 pf.buildTextImage(label, /*size.getWidth() / 2 - width / 2, size.getHeight() / 2 - height / 2, */Color.BLACK, "Arial", fontsize);
        image = Text.buildTextImage(label, /*size.getWidth() / 2 - width / 2, size.getHeight() / 2 - height / 2, */Color.BLACK,Color.BLACK_FULLTRANSPARENT);

        return image;

    }
}
