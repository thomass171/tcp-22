package de.yard.threed.engine.gui;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.engine.platform.common.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Ein gridartiges FovElement. Gut geeignet um ButtonAreas darzustellen.
 * Kann direkt vor die Camera gehangen werden an die nearPlane. Dann sind die Dimensionen entsprechend sehr klein.
 * Buttons können auch Requests auslösen.
 * 26.11.19: Besser Request statt RequestType, damit auch Parameter mitkommen können.
 * <p>
 * 7.10.19: Saudoofe Hierarchie.
 */
public class GuiGrid extends SceneNode/*FovElementPlane*/ implements Menu {
    private double buttonzpos;
    Log logger = Platform.getInstance().getLog(GuiGrid.class);
    public int mode;
    int columns;
    int rows;
    public static Color GREEN_SEMITRANSPARENT = new Color(0, 1, 0, 0.5f);
    public static Color GRAY_NONTRANSPARENT = new Color(0.8f, 0.8f, 0.8f, 1f);
    public static Color BLACK_FULLTRANSPARENT = new Color(0, 0, 0, 0f);
    List<GridButton> buttons = new ArrayList<GridButton>();
    // cellwidth, cellheight ist inklusive margin
    public double cellwidth;
    public double cellheight;
    private double margin;
    BgElement bg;

   /* public GuiGrid(Camera camera) {
        this(camera, 0);
    }*/

    /**
     * mode 1 = zentriert und fast bildschirmfuellend, aber so dass das grid quadratische cells hat. Geeignet für eingeblendete Menus.
     * mode 2 = zentriert am unteren Rand auch mit quadratischen cells. Geeignet fuer Controlbuttons.
     * mode 3 = zentriert und fast bildschirmfuellend,immer mit einer Spalte. Geeignet für eingeblendete Menus.
     * mode 4 = oben links
     * <p>
     * 7.3.17: Die Cellgroesse anhand der Screensize zu berechnen, ist schwer zu verallgemeinern. Besser die Groesse fest verdrahten.
     * 02.10.19: Verwendet keine deferred Camera und ist deshalb doch wohl deprecated?
     */
    public GuiGrid(/*Camera camera,*/DimensionF dimension, double zpos, double buttonzpos, int mode, int columns, int rows, Color background) {
        //super(/*camera*/dimension,zpos);
        bg = new BgElement(dimension, zpos, this, background);
        if (mode == 3) {
            columns = 1;
        }
        attach(bg);
        this.mode = mode;
        this.columns = columns;
        this.rows = rows;
        this.buttonzpos = buttonzpos;
        //margin durch ausprobieren
        margin = 0.0002f;
        margin = 0.0004f;
        setName("Gui Grid");
        bg.buildFovElement(/*camera,*/ null);

    }


    public void setText(int line, String text) {

        ImageData textimage = Text.buildTextImage(text, Color.YELLOW, Color.TRANSPARENT);
        //Die Texttextur ist ausser der Schrift durchsichtig. Das wird im Hud aber z.Z. nicht benutzt
        textimage.setTransparentToColor(bg.background);
        bg.image.overlayImage(textimage, 40, 40 + line * 40);
        Texture texture = new Texture(bg.image);
        // BasicMaterial, damit Beleuchtung keine Rolle spielt.
        //8.10.17: wie bei Hud kein Effect
        Material mat = Material.buildBasicMaterial(texture, /*Effect.buildUniversalEffect()*/null, true);
        bg.element.getMesh().updateMaterial(mat);

    }


    /**
     * y=0 ist unten
     * gridspan ist kompliziert(?)
     * 11.10.-19:wWirklich? Nur die x Ausrichtung stimmt noch nicht ganz
     */
    public void addButton(/*4.10.19 String*/ Request/*Type*/ command, int x, int y, int gridspan, Texture texture) {
        //buttons.add(mkGridButton(command, getButtonSize(1), getButtonTranslation(x, y), buttonzpos));
        addButton(command, x, y, gridspan, texture, null);
    }

    /**
     * 11.10.19: gridspan wieder aufgenommen, geht aber noch nicht ganz von der Ausrichtung.
     */
    public void addButton(Request/*Type*/ command, int x, int y, int gridspan,/* String*/Text text) {
        //buttons.add(mkGridButton(command, getButtonSize(gridspan), getButtonTranslation(x, y), buttonzpos));
        addButton(command, x, y, gridspan, text, null);
    }

    public void addButton(/*4.10.19 String*/ Request/*Type*/ command, int x, int y, GuiTexture icon) {
        addButton(command, x, y, 1, icon, null);
    }

    public GridButton addButton(int x, int y, int gridspan, GuiTexture icon, ButtonDelegate buttonDelegate) {
        return addButton(null, x, y, gridspan, icon, buttonDelegate);
    }

    public GridButton addButton(/*4.10.19 String*/ Request/*Type*/ command, int x, int y, int gridspan, GuiTexture icon, ButtonDelegate buttonDelegate) {
        GridButton btn = new GridButton(command, getButtonSize(gridspan), getButtonTranslation(x, y), icon, buttonDelegate, buttonzpos);
        buttons.add(btn);
        attach(btn);
        return btn;
    }

    public GridButton addButton(Request/*Type*/ command, int x, int y, int gridspan, Texture texture, ButtonDelegate buttonDelegate) {
        GridButton btn = new GridButton(command, getButtonSize(gridspan), getButtonTranslation(x, y), texture, /*3.5.21 UvMap1.rightRotatedTexture*/new ProportionalUvMap(), buttonDelegate, buttonzpos);
        buttons.add(btn);
        attach(btn);
        return btn;
    }

    private Vector2 getButtonTranslation(int x, int y) {
        Vector2 v = new Vector2(-bg.getElementsize().getWidth() / 2 + cellwidth / 2 + cellwidth * x,
                -bg.getElementsize().getHeight() / 2 + cellheight / 2 + cellheight * y);
        // Das Grid noch dazu, weil die Buttons keine echten Childs sind
        return v.add(bg.getXyTranslation(bg.nearplaneSize));
    }

    private DimensionF getButtonSize(int gridspan) {
        return new DimensionF(cellwidth * gridspan - 2f * margin, cellheight - 2f * margin);
    }

    private GridButton getClickedButton(Ray pickingray) {
        if (pickingray == null) {
            logger.debug("pickingray isType null");
            return null;
        }
        for (GridButton bn : buttons) {
            List<NativeCollision> intersects = pickingray.getIntersections(bn);
            if (intersects.size() > 0) {
                return bn;
            }
        }
        return null;
    }

    /**
     * In welchem Zustand ist denn dies GuiGrid danach? Doch nicht mehr verwendbar.
     * 3.10.19: Ist das so erforderlich?
     */
    public void remove() {
        SceneNode.removeSceneNode(this);
        for (GridButton b : buttons) {
            SceneNode.removeSceneNode(b);
        }
        buttons.clear();
    }

  /*  //@Override
    public VRController getController1() {

        return null;
    }*/

    //@Override
    public SceneNode getNode() {
        return this;
    }

    /**
     * Wird aus update aufgerufen. Geht wegen der Verwendung von getMouseClick() nur in dem Frame, in dem
     * der Mausbutton released wurde.
     * Arbeitet for convenience auch mit null input.
     * 4.10.19: Deprecated zugunsten Ray MEthode
     */
   /*@Override
    @Deprecated
    public /*4.10.19 String* / Request checkForClickedButton(Point mouselocation) {
        if (mouselocation != null) {
            // Mausbutton released
            int x = mouselocation.getX();
            int y = mouselocation.getY();
            //logger.debug("Mouse Click at " + mouselocation);
            Ray pickingray = camera.buildPickingRay(mouselocation);
            return checkForClickedArea(pickingray);
        }
        return null;
    }*/

    /**
     * 30.12.19: Es ist doch nur konsequent, hier parallel zum Delegate den Request zu verschicken statt ihn zurückzuliefern.
     * Nee, wegen Entkopplung ECS gehen Requests auch ueber Delegates.
     * button.command ist damit eigentlich hinfällig.
     *
     * @param pickingray
     * @return true if a button/area was clicked, false otherwise
     */
    @Override
    public /*4.10.19 String*/ /*30.12.19 Request*/ boolean checkForClickedArea(Ray pickingray) {
        logger.debug("guigrid picking ray isType " + pickingray);

        GridButton button = getClickedButton(pickingray);
        if (button != null) {
            //command might be null
            logger.debug("button clicked ");
            if (button.buttonDelegate != null) {
                button.buttonDelegate.buttonpressed();
            }
            return true;
            //if (button.command != null) {
            //SystemManager.putRequest(button.command);
            //return (button.command);
            //}
        }
        return false;
    }

    /**
     * position starting with 1.
     * Das macht evtl. nur bei einspaltigen menus Sinn.
     */
    public void checkForSelectionByKey(int position) {
        if (position >= 1 && position <= buttons.size()) {
            GridButton button = buttons.get(position - 1);
            if (button.buttonDelegate != null) {
                button.buttonDelegate.buttonpressed();
            }
        }
    }

    /**
     * Eigentlich nur ein einzeiliges GuiGrid mit horinzontaler unterer Ausrichtung.
     * <p>
     * Created by thomass on 28.9.18.
     */
    /*6.10.19 public static GuiGrid buildControlMenu(Camera camera, int columns) {
        GuiGrid menu = new GuiGrid(camera, 2, columns, 1, GuiGrid.BLACK_FULLTRANSPARENT);
        return menu;
    }*/

    /**
     * GuiGrid at near plane distance intended to be attached to camera.
     * 6.10.19: Fuer deferredcamera soll der Aufrufer die doch reinstecken.
     * 3.4.21: Ein solches Menu an der nearplane wird für VR unbrauchbar sein.
     */
    public static GuiGrid buildForCamera(Camera camera, int mode, int columns, int rows, Color background) {
        //PerspectiveCamera deferredcamera = FovElement.getDeferredCamera(camera);
        DimensionF dimension = camera.getNearplaneSize();
        int level = 0;
        double zpos = -camera.getNear() - 0.0001f + (level * 0.00001f);
        level = 1;
        double buttonzpos = -camera.getNear() - 0.0001f + (level * 0.00001f);
        GuiGrid hud = new GuiGrid(dimension, zpos, buttonzpos, mode, columns, rows, background/*, GuiGrid.BLACK_FULLTRANSPARENT*/);
        hud/*5.12.18 .element*/.getTransform().setLayer(FovElement.LAYER);
        return hud;
    }

    /**
     * Alternative zu BrowsMenu.
     */
    public static GuiGrid buildSingleColumnFromMenuitems(DimensionF dimension, double zpos, double buttonzpos, MenuItem[] menuitems) {
        // this.camera = camera;
        /*this.dimension = dimension;
        this.menuitems = menuitems;
        this.zpos = zpos;
        this.buttonzpos = buttonzpos;*/
        int rows = menuitems.length;
        GuiGrid guiGrid = new GuiGrid(dimension, zpos, buttonzpos, 3, 1, rows, GuiGrid.BLACK_FULLTRANSPARENT);

        for (int i = 0; i < rows; i++) {
            guiGrid.addButton(null, 0, rows - 1 - i, 1, menuitems[i].guiTexture, menuitems[i].buttonDelegate);
        }
        return guiGrid;
    }

    public DimensionF calcSize(DimensionF nearplaneSize) {
        // 0.008f ist fuer Android ganz gut. 3.12.18: Aber das muss doch near abhaengig berechnet werden.
        // Gar nicht so einfach. Von der Anzahl Spalten ausgehen führt zu nichts, weil die dann zu groß werden können.
        double cellsize = 0.008f;
        //erstmal wieder fix für nearsize von etwa 5x4; Ist in Unity aber noch zu klein.
        if (nearplaneSize.width > 2) {
            cellsize = 0.4;
        }
        DimensionF elsize = new DimensionF(0, 0);//nearplaneSize.getWidth() / 4, nearplaneSize.getHeight() / 4);
        switch (mode) {
            case 1:
                // aussen bleibt ein Rand
                elsize = new DimensionF(3 * nearplaneSize.getWidth() / 4, 3 * nearplaneSize.getHeight() / 4);
                // Der Maximalwert columns/rows und Minimalwert width/height bestimmt die gridgroesse
                //quadratisch machen
                if (elsize.getWidth() / columns < elsize.getHeight() / rows) {
                    cellheight = cellwidth = elsize.getWidth() / columns;
                } else {
                    cellheight = cellwidth = elsize.getHeight() / rows;
                }
                elsize = new DimensionF(cellwidth * columns, cellheight * rows);
                break;
            case 2:
                // aussen bleibt kein Rand. Die Spaltenzahl bestimmt die cellgroesse. Nee, die pixelsize.
                // 3.12.18: Wenn z.B. nur drei Icons da sind, soll sich das ja nicht ueber die ganze Breite strecken.
                cellheight = cellwidth = cellsize;
                elsize = new DimensionF(cellwidth * columns, cellheight * rows);
                break;
            case 3:
                // aussen bleibt ein Rand
                elsize = new DimensionF(3 * nearplaneSize.getWidth() / 4, 3 * nearplaneSize.getHeight() / 4);
                cellheight = cellwidth = elsize.getHeight() / rows;
                cellwidth = elsize.width;
                elsize = new DimensionF(cellwidth * columns, cellheight * rows);
                break;
            case 4:
                cellheight = cellwidth = cellsize;
                elsize = new DimensionF(cellwidth * columns, cellheight * rows);
                break;

        }
        logger.debug("elsize=" + elsize + ",cellwidth=" + cellwidth);

        return elsize;
    }

    public Vector2 getXyTranslation(DimensionF nearplaneSize) {
        // Statt mathematisch korrekt um ein Achtel ein Neuntel verschieben, damit ein kleiner Rand bleibt.
        double xtranslate = nearplaneSize.getWidth() / 4 + nearplaneSize.getWidth() / 9/*-1.8f*/;
        double ytranslate = nearplaneSize.getHeight() / 4 + nearplaneSize.getHeight() / 9/*-1.8f*/;
        switch (mode) {
            case 1:
                //zentriert
                xtranslate = 0;
                ytranslate = 0;
                break;
            case 2:
                //zentriert am unteren Rand
                xtranslate = 0;
                ytranslate = -(nearplaneSize.getHeight() / 2 - (cellheight * rows) / 2);
                break;
            case 3:
                //zentriert
                xtranslate = 0;
                ytranslate = 0;
                break;
            case 4:
                //links oben
                xtranslate = -(nearplaneSize.getWidth() / 2 - (cellwidth * columns) / 2);
                ;
                ytranslate = (nearplaneSize.getHeight() / 2 - (cellheight * rows) / 2);
                break;
        }
        Vector2 tr = new Vector2(xtranslate, ytranslate);
        //logger.debug("translation=" + tr);
        return tr;
    }

    /*public Camera getMenuCamera(){
        return camera;
    }*/
}

class BgElement extends FovElementPlane {
    public ImageData image;
    // gruener Hintergrund, der durch Transparenz blasser erscheint.
    public Color background;
    GuiGrid guiGrid;

    public BgElement(DimensionF dimension, double zpos, GuiGrid guiGrid, Color background) {
        super(dimension, zpos);
        this.guiGrid = guiGrid;
        this.background = background;


    }

    /**
     * Background
     *
     * @return
     */
    @Override
    public Material buildMat() {
        image = ImageFactory.buildSingleColor(256, 256, background);
        Texture texture = new Texture(image);
        //CustomShaderMaterial mat = new CustomShaderMaterial("basetex",texture, Effect.buildUniversalEffect(true));
        // BasicMaterial, damit Beleuchtung keine Rolle spielt.
        //8.10.17: wie bei Hud kein Effect. Fuer trasnparent muss die Color den richtigen Alpha haben.
        Material mat = Material.buildBasicMaterial(texture, /*Effect.buildUniversalEffect()*/null, true);
        return mat;
    }

    @Override
    public DimensionF getSize(DimensionF nearplaneSize) {


        DimensionF elsize = guiGrid.calcSize(nearplaneSize);
        return elsize;
    }

    /**
     * margin spielt hier nicht mit rein.
     * <p>
     * 0-Werte fuer xy liegen im Center.
     *
     * @param nearplaneSize
     * @return
     */
    @Override
    public Vector2 getXyTranslation(DimensionF nearplaneSize) {
        return guiGrid.getXyTranslation(nearplaneSize);

    }

    @Override
    public UvMap1 getUvMap() {
        return /*3.5.21 UvMap1.rightRotatedTexture*/new ProportionalUvMap();
    }

}

/**
 * Ein GridButton ist auch wieder ein FovElement, um per Pickingray ein Click ermitteln zu koennen.
 * Das koennte man auch per Koordinatenberechnung, aber dafuer habe ich ja nun mal den Picking Ray.
 * Er kommt etwas vor das gesamte Grid und ist nicht transparent (kein spezieller Shader).
 */
class GridButton extends FovElementPlane {
    DimensionF size;
    Vector2 translation;
    //11.10.19 lieber separat speichern, dann ist es flexibler.
    //GuiTexture icon;
    Texture texture;
    UvMap1 uvmap;
    //31.12.19/*4.10.19 String*/ Request/*Type*/ command;
    Log logger = Platform.getInstance().getLog(GridButton.class);
    ButtonDelegate buttonDelegate;

    /*11.10.19 GridButton(/*4.10.19 String* / RequestType command, DimensionF size, Vector2 translation,double buttonzpoas/*, Camera camera* /) {
        this(command, size, translation, /*camera,* / null, null,buttonzpoas);
    }*/

    GridButton(/*4.10.19 String*/ Request/*Type*/ command, DimensionF size, Vector2 translation, /*Camera camera,*/ GuiTexture icon, ButtonDelegate buttonDelegate, double buttonzpos) {
        this(command, size, translation, icon.getTexture(), icon.getUvMap(), buttonDelegate, buttonzpos);
    }

    GridButton(/*4.10.19 String*/ Request/*Type*/ command, DimensionF size, Vector2 translation, /*Camera camera,*/ Texture texture, UvMap1 uvmap, ButtonDelegate buttonDelegate, double buttonzpos) {
        super(/*camera*/size, buttonzpos);
        level = 1;
        this.size = size;
        this.translation = translation;
        //logger.debug("size=" + size + ",translation=" + translation);
        //31.12.19 this.command = command;
        //this.icon = icon;
        this.texture = texture;
        this.uvmap = uvmap;
        this.buttonDelegate = buttonDelegate;
        rebuildElement();

    }

    private void rebuildElement() {
        // Das FovElement wird rotiert, darum y/z Tausch (13.12.18: ??)
        buildFovElement(/*null,*/ new Vector3(size.getWidth(), 0.000001f, size.getHeight()));
        //setName("Button " + command);
        element.setName("Button " /*31.12.19+ command*/);
    }

    @Override
    public Material buildMat() {
        Material mat;


            mat = Material.buildBasicMaterial(texture/*icon.getTexture()*/);

        return mat;
    }

    @Override
    public DimensionF getSize(DimensionF nearplaneSize) {
        return size;
    }

    @Override
    public Vector2 getXyTranslation(DimensionF nearplaneSize) {

        return translation;
    }

    @Override
    public UvMap1 getUvMap() {
        /*UvMap1 uvmap = UvMap1.rightRotatedTexture;
        if (icon != null) {
            uvmap = icon.getUvMap();
        }*/
        return uvmap;
    }

    /**
     * Weil sich auch UVs aendern koennen, das ganze Element neu machen.
     *
     * @param icon
     */
    public void setIcon(GuiTexture icon) {
        //this.icon = icon;
        this.texture = icon.getTexture();
        this.uvmap = icon.getUvMap();
        SceneNode.removeSceneNode(element);
        element = null;
        rebuildElement();
    }

    public void setDelegate(ButtonDelegate buttonDelegate) {
        this.buttonDelegate = buttonDelegate;
    }

    /*public void setRequest(RequestType buttonDelegate) {
        this.command = new Request(buttonDelegate);
    }*/
}