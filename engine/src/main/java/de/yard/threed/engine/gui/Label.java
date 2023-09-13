package de.yard.threed.engine.gui;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.geometry.ProportionalUvMap;
import de.yard.threed.engine.Texture;
import de.yard.threed.core.geometry.UvMap1;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.Log;

/**
 * Created by thomass on 22.02.17.
 */
public class Label implements GuiTexture {
    static Log logger = Platform.getInstance().getLog(Label.class);

    public static Label LABEL_RESET = new Label(0, 0,4,1);
    public static Label LABEL_START = new Label(4, 0,4,1);
    public static Label LABEL_LOADINGVEHICLE = new Label(0, 2,8,1);
    public static Texture texture = Texture.buildBundleTexture("engine", "Labelset-LightBlue.png");
    private int x,y,w,h;
    // das Grundraster
    private int cnt = 16;
    /**
     * xy sind die logischen Koordinaten in der Textur mit y=0 oben
     *
     * @param x
     * @param y
     */
    public Label(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w=w;
        this.h=h;
    }

    @Override
    public Texture getTexture() {
        return texture;
    }

    public UvMap1 getUvMap() {
        // wegen der Planeorientierung ist noch der rechts rotate erforderlich.
        ProportionalUvMap uvmap = new ProportionalUvMap(
                new Vector2(((float) x / cnt), (((float) cnt - y - h) / cnt)),
                new Vector2((((float) x + w ) / cnt), (((float) cnt - y) / cnt)) /*3.5.21 ProportionalUvMap.ROTATION_RIGHT*/);
        return uvmap;//.rotateRight();
    }

    
  
}
