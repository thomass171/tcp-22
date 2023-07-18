package de.yard.threed.maze;


import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.Color;

/**
 * Defines visual appearance only. Should not have effect on game play.
 * Especially sizes of fields, players might effect something like visibility at corners, bullet hitting, firetargetmarker, etc.
 * So be careful.
 * <p>
 * Was MazeSettings once.
 * 18.7.23: No more a singleton.
 * <p/>
 * Created by thomass on 11.01.16.
 */
public class MazeTheme {
    //public String grid;
    public boolean ambilight = false;
    public static final int THEME_TRADITIONAL = 2;
    public static final int THEME_DUNGEON = 3;
    // Theme THEME_REALWOOD was an idea based on real 'jenga' texture "textures/realwood/Wall.png". But without normalmap etc.
    // its just not complete. (See history of MazeModelFactory).
    //public static final int THEME_REALWOOD = 3;
    private int theme = -1;
    //13.1.16 Hoehe 0.9 statt 0.3, Diameter 0.3->0.1 um nicht sichtbar zu sein
    public float simplerayheight = 0.9f;
    public float simpleraydiameter = 0.1f;
    private static MazeTheme st;
    public boolean debug = false;
    public float sokobanboxsize = 0.6f;
    // Color is well fitting to ground
    public static Color hudColor = new Color(255, 217, 102, 128);
    // Standard Color.YELLOW is too bright. Color needs to fit to hud background
    public static Color bulletColor = new Color(0xff, 0x80, 00);
    // Color.LIGHTBLUE is general background. So a custom lighter blue. .
    public static Color diamondColor = new Color(0x99, 0xFF, 0xFF);
    public static String[] teamColors = new String[]{"darkgreen", "red"};
    private MazeModelFactory mazeModelFactory;

    private MazeTheme(int theme) {
        this.theme = theme;
        // no nice solution, but factory currently needs constructor parameter (which makes sense), so we have a cycle dependency.
        this.mazeModelFactory =  new MazeTraditionalModelFactory(this);
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

    public static MazeTheme init() {
        String theme = Platform.getInstance().getConfiguration().getString("theme", "traditional");
        if (theme.equals("dungeon")) {
            return init(THEME_DUNGEON);
        } else {
            return init(THEME_TRADITIONAL);
        }
    }

    public static MazeTheme init(int theme) {
        if (st == null) {
            st = new MazeTheme(theme);
        }
        return st;
    }

    public int getTheme() {
        return theme;
    }

    public MazeModelFactory getMazeModelFactory() {
        return mazeModelFactory;
    }

    public AbstractMazeTerrain buildTerrain(MazeLayout layout) {
        return new MazeTraditionalTerrain(layout, (MazeTraditionalModelFactory) mazeModelFactory);
    }
}
