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
    public Texture walltexture;
    public Texture[] wallnormalmap;
    public Material pillarmaterial, groundmaterial;
    public float sokobanboxsize = 0.6f;
    public ShapeGeometry sokobanboxgeo;
    public Material sokobanboxmaterial;
    // die Farbe passt gut zum Ground
    public static Color hudColor = new Color(255, 217, 102, 128);

    MazeSettings(int mode) {
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
        switch (theme) {
            case THEME_DRAFT:
                Util.nomore();
                /*14.6.21 unauusgegoren
                walltexture =buildTexture ("images/BrickLargeBare0001_1_M.jpg");
                pillarmaterial = Material.buildLambertMaterial(buildTexture("images/WoodPlanksDirty0085_M.jpg"));
                UvMap1 uvmap = new ProportionalUvMap(new Vector2(0, 1f / 3f), new Vector2(0.5f, 2f / 3f));
                List<UvMap1> uvmaps = new ArrayList<UvMap1>();
                for (int i = 0; i < 6; i++) {
                    uvmaps.add(uvmap);
                }
                sokobanboxgeo = ShapeGeometry.buildBox(sokobanboxsize, sokobanboxsize, sokobanboxsize, uvmaps);
                sokobanboxmaterial = Material.buildLambertMaterial(buildTexture("images/texturedcube-atlas.jpg"));
                groundmaterial = Material.buildPhongMaterialWithNormalMap(buildTexture("images/FloorsMedieval0034_6_M.jpg"),
                        Texture.buildNormalMap(MazeModelFactory.buildEdgeNormalmap().image));*/
                break;
            case THEME_WOOD:
                walltexture = buildTexture("textures/gimp/wood/BucheHell.png");
                pillarmaterial = Material.buildLambertMaterial(buildTexture("textures/gimp/wood/BucheDunkel.png"));
                sokobanboxgeo = ShapeGeometry.buildBox(sokobanboxsize, sokobanboxsize, sokobanboxsize, null);
                sokobanboxmaterial = Material.buildLambertMaterial(buildTexture("textures/gimp/wood/BucheMedium.png"));
                //22.3.17: Wandmaterial auch fuer Boden statt Ground.png
                groundmaterial = Material.buildPhongMaterialWithNormalMap(buildTexture("textures/gimp/wood/BucheHell.png"),
                        Texture.buildNormalMap(MazeModelFactory.buildEdgeNormalmap().image));
                //wallnormalmap = buildTexture("textures/gimp/SampleWallNormalMapByPetry.png"));
                //Ein paar anlegen, damit nicht alle gleich aussehen. Die werden durch randon alle unterschiedlich
                wallnormalmap = new Texture[5];
                wallnormalmap[0] = Texture.buildNormalMap(new WoodenToyFactory().buildWallNormalMap(6).image);
                wallnormalmap[1] = Texture.buildNormalMap(new WoodenToyFactory().buildWallNormalMap(6).image);
                wallnormalmap[2] = Texture.buildNormalMap(new WoodenToyFactory().buildWallNormalMap(6).image);
                wallnormalmap[3] = Texture.buildNormalMap(new WoodenToyFactory().buildWallNormalMap(6).image);
                wallnormalmap[4] = Texture.buildNormalMap(new WoodenToyFactory().buildWallNormalMap(6).image);
                break;
            case THEME_REALWOOD:
                walltexture = buildTexture("textures/gimp/wood/BucheHell.png");
                pillarmaterial = Material.buildLambertMaterial(buildTexture("textures/gimp/wood/BucheDunkel.png"));
                sokobanboxgeo = ShapeGeometry.buildBox(sokobanboxsize, sokobanboxsize, sokobanboxsize, null);
                sokobanboxmaterial = Material.buildLambertMaterial(buildTexture("textures/gimp/wood/BucheMedium.png"));
                //22.3.17: Wandmaterial auch fuer Boden statt Ground.png
                groundmaterial = Material.buildPhongMaterialWithNormalMap(buildTexture("textures/gimp/wood/BucheHell.png"),
                        Texture.buildNormalMap(MazeModelFactory.buildEdgeNormalmap().image));
                wallnormalmap = null;
                walltexture = buildTexture("textures/realwood/Wall.png");

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

    private Texture buildTexture(String s) {
        return Texture.buildBundleTexture("data", s);
    }

    public static MazeSettings init(int mode) {
        if (st == null) {
            st = new MazeSettings(mode);
        }
        return st;
    }

    public static MazeSettings getSettings() {
        /*if (st == null) {
            st = new MazeSettings(mode);
        }*/
        return st;
    }
}
