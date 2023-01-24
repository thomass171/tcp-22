package de.yard.threed.maze;


import de.yard.threed.core.*;
import de.yard.threed.engine.*;
import de.yard.threed.engine.apps.WoodenToyFactory;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.core.Color;

/**
 * MazeSettings
 * <p/>
 * Created by thomass on 11.01.16.
 */
public class MazeSettings {
    public String grid;
    public boolean ambilight;
    //16.8.19: Sowas wie mode sollte durch Ableitungen von AbstractScene und ECS obsolet sein.
    @Deprecated
    public static final int MODE_MAZE = 1;
    public static final int MODE_SOKOBAN = 2;
    //public int mode = MODE_SOKOBAN;
    public static final int THEME_DRAFT = 1;
    public static final int THEME_WOOD = 2;
    public static final int THEME_REALWOOD = 3;
    private int theme = THEME_DRAFT;
    //13.1.16 Hoehe 0.9 statt 0.3, Diameter 0.3->0.1 um nicht sichtbar zu sein
    public float simplerayheight = 0.9f;
    public float simpleraydiameter = 0.1f;
    private static MazeSettings st;
    public boolean debug = false;
    public float sokobanboxsize = 0.6f;
    // Color is well fitting to ground
    public static Color hudColor = new Color(255, 217, 102, 128);
    // Standard Color.YELLOW is too bright. Color needs to fit to hud background
    public static Color bulletColor = new Color(0xff, 0x80, 00);
    // Color.LIGHTBLUE is general background. So a custom lighter blue. .
    public static Color diamondColor = new Color(0x99, 0xFF, 0xFF);
    public static String[] teamColors = new String[]{"darkgreen", "red"};

    private MazeSettings(int mode) {
        switch (mode) {
            case MODE_MAZE:
                grid = "maze/grid1.txt";
                ambilight = false;
                break;
            case MODE_SOKOBAN:
                grid = "skbn/SokobanWikipedia.txt";
                //grid = "skbn/SokobanTrivial.txt";
                ambilight = false;
                theme = THEME_WOOD;
                break;
        }

    }

    public LocalTransform getViewpoint() {
        //12.10.15: Nicht auf den Boden y=0), sondern auf halbe höhe sehen.
        // Die Reihenfolge ist wichtig. Erst Position/Lookup, dann attachen. 25.2.16: NeeNee, erst attach!
        // 19.5.16: Camera etwas höher, damit man targets hinter einer Box sehen kann.(plus 0.1->0.3)
        //16.8.19 camera ist an Avatar camera.attach(MazeView.ray.scenenode.getTransform());
        //6.4.21 der Blickwinkel nach unten scheint jetzt (beim Setzen über Avatar statt direkt camera) verloren zu gehen.
        //ist eh noch nicht das wahre, weil es per initialtransform den ganzen Avatar kippen duerfte.
        //16.5.21: Aus MazeScene hierhin.
        // 19.3.22: Warum der zoffset 0.2 war ist unklar. Vielleicht hat es sich nie ausgewirkt, jetzt sieht man aber Teile vom eigenen AvatarA.
        // Darum lieber 0. Die View ist damit ganz leicht anders.
        double zoffset = 0.2;
        zoffset = 0;
        LocalTransform viewpoint = new LocalTransform(new Vector3(0, /*movingsystem.getSettings().*/simplerayheight / 2 + 0.3f, zoffset/*0/*+0.4f*/),
                //4.3.17: Blickrichtung nochmal 0.1 hoeher, das fühlt sich angenehmer an, nicht so stark nach unten zu blicken.
                Quaternion.buildRotationX(new Degree(-20)));
        return viewpoint;
    }

    public static MazeSettings init(int mode) {
        if (st == null) {
            st = new MazeSettings(mode);
        }
        return st;
    }

    public static MazeSettings getSettings() {
        return st;
    }

    public int getTheme(){
        return theme;
    }
}
