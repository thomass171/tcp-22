package de.yard.threed.engine.gui;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.geometry.ProportionalUvMap;
import de.yard.threed.core.geometry.UvMap1;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.ImageData;
import de.yard.threed.core.Color;
import de.yard.threed.core.ImageFactory;


/**
 * Im Prinzip einfach nur eine Plane mit generierter Textur.
 * 6.10.19: Entkoppelt von Camera. Dafuer ist mehr der attach zustaendig.
 * <p/>
 * Created by thomass on 15.12.15.
 */
public class Hud extends FovElementPlane {
    static Log logger = Platform.getInstance().getLog(Hud.class);
    ImageData image;
    // gruener Hintergrund, der durch Transparenz blasser erscheint.
    Color basecolor = new Color(0, 1, 0, 0.5f);
    private int mode;

    /**
     * mode 0 = rechts oben
     * mode 1 = zentriert und fast bildschirmfuellend.
     * <p>
     * 23.4.21: Der Constructor is independent from camera and nearplane.
     *
     * @param mode
     */
    public Hud(DimensionF dimension, double zpos, int mode) {
        super(dimension, zpos);
        this.mode = mode;
        buildFovElement(null);
        setName("Hud");
    }

    /**
     * HUD with own deferred rendering camera at any near distance.
     * 6.10.19: Fuer deferredcamera soll der Aufrufer die doch reinstecken.
     * 16.12.22: Its build camera dependent, so it should also be attached here.
     */
    public static Hud buildForCameraAndAttach(Camera camera, int mode) {

        DimensionF dimension = camera.getNearplaneSize();
        int level = 0;
        double zpos = -camera.getNear() - 0.0001f + (level * 0.00001f);

        Hud hud = new Hud(dimension, zpos, mode);
        camera.getCarrier().attach(hud);
        return hud;
    }

    /**
     * Der Text wirkt irgendwie unnötig groß, aber das liegt wohl in der Nature des Fonts mit 32 Pioxel Höhe.
     * 28.4.21: Hier mit dem Overlay zu arbeiten, ist doch deprecated (siehe Inventory).
     *
     * @param line
     * @param text
     */
    public void setText(int line, String text) {
        //25.9.17: das ist doof, den String zu verlaengern damit alte Reste verschwenden.
        //Der gelieferte Text ist 32 Pixel hoch
        ImageData textimage = Text.buildTextImage(text + "               ", Color.RED, Color.BLACK_FULLTRANSPARENT);
        Texture texture;
        boolean fortest = false;
        if (fortest) {
            texture = new Texture(textimage);
        } else {
            //Die Texttextur ist ausser der Schrift durchsichtig. Das wird im Hud aber z.Z. nicht benutzt
            textimage.setTransparentToColor(basecolor);
            image.overlayImage(textimage, 40, 40 + line * 40);
            texture = new Texture(image);
        }
        // BasicMaterial, damit Beleuchtung keine Rolle spielt.
        // 8.10.17: Dafuer muss ich doch keinen Effect bemuehen.
        Material mat = Material.buildBasicMaterial(texture, /*Effect.buildUniversalEffect()*/ 0.5);
        Mesh mesh = element.getMesh();
        if (mesh != null) {
            // might be null when headless?
            mesh.updateMaterial(mat);
        }

    }

    /**
     * Creates a text texture with background color. full transparent is currently not used.
     *
     * 28.4.21: Without overlay, which is the better approach.
     * Die Groesse der Textur ist wohl len*32x32.
     *
     * @param text
     * @param backGround
     * @return
     */
    public static Texture buildTextureForText(String text, Color color, Color backGround) {
        //25.9.17: das ist doof, den String zu verlaengern damit alte Reste verschwenden.
        //Der gelieferte Text ist 32 Pixel hoch
        ImageData textimage = Text.buildTextImage(text + "", color, Color.BLACK_FULLTRANSPARENT);
        Texture texture;
        boolean fortest = false;
        if (fortest) {
            texture = new Texture(textimage);
        } else {
            // The base text texture is full transparent. Das wird im Hud aber z.Z. nicht benutzt
            textimage.setTransparentToColor(backGround);
            texture = new Texture(textimage);
            //logger.debug("textimage pixel0=" + textimage.pixel[0]);
        }
        return texture;
    }

    public void clear() {
        Material mat = buildMat();
        element.getMesh().updateMaterial(mat);
    }

    @Override
    public Material buildMat() {
        image = ImageFactory.buildSingleColor(256, 256, basecolor);
        Texture texture = new Texture(image);
        // BasicMaterial, damit Beleuchtung keine Rolle spielt.
        // 8.10.17: Dafuer muss ich doch keinen Effect bemuehen.
        Material mat = Material.buildBasicMaterial(texture, 0.5);
        return mat;
    }

    @Override
    public DimensionF getSize(DimensionF nearplaneSize) {
        DimensionF size = new DimensionF(nearplaneSize.getWidth() / 4, nearplaneSize.getHeight() / 4);
        switch (mode) {
            case 1:
                size = new DimensionF(3 * nearplaneSize.getWidth() / 4, 3 * nearplaneSize.getHeight() / 4);
                break;
        }
        return size;
    }

    @Override
    public Vector2 getXyTranslation(DimensionF nearplaneSize) {
        // Statt mathematisch korrekt um ein Achtel ein Neuntel verschieben, damit ein kleiner Rand bleibt.
        double xtranslate = nearplaneSize.getWidth() / 4 + nearplaneSize.getWidth() / 9/*-1.8f*/;
        double ytranslate = nearplaneSize.getHeight() / 4 + nearplaneSize.getHeight() / 9/*-1.8f*/;
        switch (mode) {
            case 1:
                xtranslate = 0;//nearplaneSize.getWidth() / 4 + nearplaneSize.getWidth()/2;
                ytranslate = 0;//nearplaneSize.getHeight() / 4 + nearplaneSize.getHeight() / 2/*-1.8f*/;
                break;
        }
        return new Vector2(xtranslate, ytranslate);
    }

    @Override
    public UvMap1 getUvMap() {
        return /*3.5.21 UvMap1.rightRotatedTexture*/new ProportionalUvMap();
    }
}
