package de.yard.threed.engine.gui;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.geometry.ProportionalUvMap;
import de.yard.threed.engine.Texture;
import de.yard.threed.core.geometry.UvMap1;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by thomass on 22.02.17.
 */
public class Icon implements GuiTexture {
    static Log logger = Platform.getInstance().getLog(Icon.class);

    public static Icon ICON_DESTINATION = new Icon(0, 0);
    public static Icon ICON_LEFTARROW = new Icon(1, 0);
    public static Icon ICON_RIGHTARROW = new Icon(2, 0);
    public static Icon ICON_UPARROW = new Icon(3, 0);
    public static Icon ICON_DOWNARROW = new Icon(4, 0);
    public static Icon ICON_PLUS = new Icon(5, 0);
    public static Icon ICON_HORIZONTALLINE = new Icon(6, 0);
    //blank war mal undo
    public static Icon ICON_BLANK = new Icon(7, 0);
    public static Icon ICON_VERTICALLINE = new Icon(8, 0);
    public static Icon ICON_CLOSE = new Icon(9, 0);
    public static Icon ICON_HELP = new Icon(10, 0);
    public static Icon ICON_MENU = new Icon(11, 0);
    public static Icon ICON_RESET = new Icon(12, 0);
    public static Icon ICON_POSITION = new Icon(13, 0);
    public static Icon ICON_TURNRIGHT = new Icon(14, 0);
    public static Icon ICON_TURNLEFT = new Icon(15, 0);
    public static Texture texture = Texture.buildBundleTexture("engine", "Iconset-Orange.png");
    private static Map<Integer, Icon> iconnumber = new HashMap<Integer, Icon>();
    int x, y;
    // muss zum Imagebuilder passen!
    int cnt = 16;

    /**
     * xy sind die logischen Koordinaten in der Textur mit y=0 oben
     *
     * @param x
     * @param y
     */
    public Icon(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public Texture getTexture() {
        return texture;
    }

    public UvMap1 getUvMap() {
        // wegen der Planeorientierung ist noch der rechts rotate erforderlich.
        // 3.5.21: Es gibt jetzt aber noch andere planes, die more natural sind und das nicht mehr brauchen. Das kann/soll er hier aber gar nicht wissen.
        ProportionalUvMap uvmap = ProportionalUvMap.buildForGridElement(cnt, x, y, true);
        return uvmap;//.rotateRight();
    }

    /**
     * Liefert das Icon für eine Zahl 1-48.
     * ab 1
     *
     * @param number
     * @return
     */
    public static Icon IconNumber(int number) {
        if (number < 1 || number > 48) {
            logger.error("invalid number " + number);
            return ICON_BLANK;
        }
        if (iconnumber.get(number) == null) {
            iconnumber.put(number, new Icon((number - 1) % 16, 1 + (number - 1) / 16));
        }
        return iconnumber.get(number);
    }

    /**
     * Returns icon for character A-Z.
     * 11=L
     * 12=M
     * 13=N
     * 14=O
     * 15=P
     * 18=S
     * For now without cache (will that be a benefit?)
     *
     * @param index
     * @return
     */
    public static Icon IconCharacter(int index) {
        if (index < 0 || index > 25) {
            logger.error("invalid index " + index);
            return ICON_BLANK;
        }
        return new Icon((index) % 16, 4 + index / 16);
    }
}
