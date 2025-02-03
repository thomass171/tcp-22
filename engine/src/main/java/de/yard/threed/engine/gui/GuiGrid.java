package de.yard.threed.engine.gui;

import de.yard.threed.core.*;
import de.yard.threed.core.geometry.ProportionalUvMap;
import de.yard.threed.core.geometry.UvMap1;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.engine.platform.common.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A grid of planes (like FovElement) for displaying buttons for user interaction. Can be located directly behind the near plane of a camera
 * or attached to a dedicated deferred camera.
 * <p>
 * Depending on the mode of usage (eg. button) the effective plane size might differ from the available total nearplane size.
 * <p>
 * The buttons are locatod according to mode and x/y.
 * <p>
 * Buttons can also trigger Requests.
 * 26.11.19: Better use Request instead of RequestType for passing parameter
 * <p>
 * 7.10.19: Because of its weird implementation using FovElement* is deprecated by {@link ControlPanel} and {@link GenericControlPanel}.
 */
public class GuiGrid extends SceneNode implements Menu {
    private double buttonzpos;
    Log logger = Platform.getInstance().getLog(GuiGrid.class);
    public int mode;
    int columns;
    int rows;
    public static Color GREEN_SEMITRANSPARENT = new Color(0, 1, 0, 0.5f);
    public static Color GRAY_NONTRANSPARENT = new Color(0.8f, 0.8f, 0.8f, 1f);
    List<GridButton> buttons = new ArrayList<GridButton>();
    // cellwidth, cellheight ist inklusive margin
    private double cellwidth;
    private double cellheight;
    private double margin;
    private BgElement bg;
    private ControlPanel cp;
    private DimensionF nearPlaneDimension, backPlaneDimension;
    private double zpos;
    public static double MAIN_Z_OFFSET = 0.0001;
    public static double MAIN_BUTTON_Z_OFFSET = 0.00001;

    /**
     * mode 1 = centered und almost full screen, aber so dass das grid quadratische cells hat. Geeignet für eingeblendete Menus.
     * mode 2 = centered at lower border auch mit quadratischen cells. Suitable for Controlbuttons.
     * mode 3 = centered und almost full screen,immer mit einer Spalte. Suitable for popup menus.
     * mode 4 = top left
     * <p>
     * 7.3.17: Die Cellgroesse anhand der Screensize zu berechnen, ist schwer zu verallgemeinern. Besser die Groesse fest verdrahten.
     */
    public GuiGrid(DimensionF nearPlaneDimension, double zpos, double buttonzpos, int mode, int columns, int rows, Color background) {
        this(nearPlaneDimension, zpos, buttonzpos, mode, columns, rows, background, false);
    }

    public GuiGrid(DimensionF nearPlaneDimension, double zpos, double buttonzpos, int mode, int columns, int rows, Color background, boolean useControlPanel) {

        this.nearPlaneDimension = nearPlaneDimension;
        if (mode == 3) {
            columns = 1;
        }
        this.mode = mode;
        this.columns = columns;
        this.rows = rows;
        this.buttonzpos = buttonzpos;
        this.zpos = zpos;
        //margin by probing
        margin = 0.0002f;
        margin = 0.0004f;

        calcSizes(nearPlaneDimension);

        if (useControlPanel) {
            // zoffsetforCompnents should be lower than MAIN_Z_OFFSET
            cp = new ControlPanel(backPlaneDimension, GuiGrid.buildBackplaneMaterial(background), 0.00001);
            attach(cp);
        } else {
            bg = new BgElement(nearPlaneDimension, zpos, this, background);
            attach(bg);
        }

        setName("Gui Grid");
        if (bg != null) {
            bg.buildFovElement(null);
        }
    }

    public DimensionF getNearPlaneDimension() {
        return nearPlaneDimension;
    }

    public DimensionF getBackPlaneDimension() {
        return backPlaneDimension;
    }

    public DimensionF getCellDimension() {
        return new DimensionF(cellwidth, cellheight);
    }

    /**
     * y=0 ist unten
     * gridspan ist kompliziert(?)
     * 11.10.-19:wWirklich? Nur die x Ausrichtung stimmt noch nicht ganz
     */
    public void addButton(/*4.10.19 String*/ Request/*Type*/ command, int x, int y, int gridspan, Texture texture) {
        addButton(command, x, y, gridspan, texture, null);
    }

    /**
     * 11.10.19: gridspan wieder aufgenommen, geht aber noch nicht ganz von der Ausrichtung.
     */
    public void addButton(Request command, int x, int y, int gridspan,/* String*/Text text) {
        addButton(command, x, y, gridspan, text, null);
    }

    public void addButton(Request command, int x, int y, GuiTexture icon) {
        addButton(command, x, y, 1, icon, null);
    }

    public GridButton addButton(int x, int y, int gridspan, GuiTexture icon, ButtonDelegate buttonDelegate) {
        return addButton(null, x, y, gridspan, icon, buttonDelegate);
    }

    public GridButton addButton(/*4.10.19 String*/ Request/*Type*/ command, int x, int y, int gridspan, GuiTexture icon, ButtonDelegate buttonDelegate) {
        if (bg != null) {
            GridButton btn = new GridButton(command, getButtonSize(gridspan), getButtonTranslation(x, y), icon, buttonDelegate, buttonzpos);
            buttons.add(btn);
            attach(btn);
            return btn;
        }

        Vector2 p = getButtonTranslation(x, y);
        //p=new Vector2(0,0);
        ControlPanelArea area = cp.addArea(p, getButtonSize(gridspan), buttonDelegate);
        area.setTexture(icon.getTexture(), icon.getUvMap());

        return null;
    }

    public GridButton addButton(Request/*Type*/ command, int x, int y, int gridspan, Texture texture, ButtonDelegate buttonDelegate) {
        if (bg != null) {
            GridButton btn = new GridButton(command, getButtonSize(gridspan), getButtonTranslation(x, y), texture, /*3.5.21 UvMap1.rightRotatedTexture*/new ProportionalUvMap(), buttonDelegate, buttonzpos);
            buttons.add(btn);
            attach(btn);
            return btn;
        }
        ControlPanelArea area = cp.addArea(getButtonTranslation(x, y), getButtonSize(gridspan), buttonDelegate);
        area.setTexture(texture);
        return null;
    }

    private Vector2 getButtonTranslation(int x, int y) {
        Vector2 v;
        if (bg != null) {
            v = new Vector2(-bg.getElementsize().getWidth() / 2 + cellwidth / 2 + cellwidth * x,
                    -bg.getElementsize().getHeight() / 2 + cellheight / 2 + cellheight * y);
            // Das Grid noch dazu, weil die Buttons keine echten Childs sind
            return v.add(bg.getXyTranslation(bg.nearplaneSize));
        }

        // button is attached to backplane, so values differ from above
        v = new Vector2(-backPlaneDimension.getWidth() / 2 + cellwidth / 2 + cellwidth * x,
                -backPlaneDimension.getHeight() / 2 + cellheight / 2 + cellheight * y);
        // Das Grid noch dazu, weil die Buttons keine echten Childs sind
        return v.add(getXyTranslation(backPlaneDimension));
    }

    private DimensionF getButtonSize(int gridspan) {
        return new DimensionF(cellwidth * gridspan - 2f * margin, cellheight - 2f * margin);
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

    public SceneNode getNode() {
        return this;
    }

    /**
     * 30.12.19: Es ist doch nur konsequent, hier parallel zum Delegate den Request zu verschicken statt ihn zurückzuliefern.
     * Nee, wegen Entkopplung ECS gehen Requests auch ueber Delegates.
     * button.command ist damit eigentlich hinfällig.
     *
     * @param pickingray
     * @return true if a button/area was clicked, false otherwise
     */
    @Override
    public boolean checkForClickedArea(Ray pickingray) {
        logger.debug("guigrid picking ray is " + pickingray);

        if (pickingray == null) {
            logger.debug("pickingray is null");
        }
        if (bg != null) {
            for (GridButton bn : buttons) {
                // not the button node itself but the fov element will be hit
                List<NativeCollision> intersects = pickingray.getIntersections(bn.element, false);
                if (intersects.size() > 0) {
                    logger.debug("button clicked ");
                    if (bn.buttonDelegate != null) {
                        bn.buttonDelegate.buttonpressed();
                    }
                    return true;

                }
            }
            return false;
        }
        return cp.checkForClickedArea(pickingray);
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
     * GuiGrid at near plane distance intended to be attached to a camera. Its not attached here!
     * 6.10.19: For deferredcamera the caller should just provide it. However, near/far are supposed zpos used here.
     * 3.4.21: Not suitable for VR.
     * 16.12.22: Passing the camera is confusing because it might differ from the camera where it is attached.
     */
    public static GuiGrid buildForCamera(Camera camera, int mode, int columns, int rows, Color background, boolean useControlPanel) {
        //PerspectiveCamera deferredcamera = FovElement.getDeferredCamera(camera);
        DimensionF dimension = camera.getNearplaneSize();
        //int level = 0;
        double zpos = -camera.getNear() - MAIN_Z_OFFSET /*+(level * 0.00001f)*/;
        //double zpos = camera.getNear() + 0.1;
        //level = 1;
        double buttonzpos = -camera.getNear() - MAIN_Z_OFFSET + MAIN_BUTTON_Z_OFFSET;

        DimensionF worldPlaneSize = camera.getPlaneSize(zpos);
        //logger.debug("worldPlaneSize=" + worldPlaneSize+ " for zpos "+ zpos);

        DimensionF worldBackplaneSize = worldPlaneSize;//ControlPanelHelper.buildDimensionByPixel(worldPlaneSize, screenDimensionInPixel, inventorySizeInPixel);
        //logger.debug("worldBackplaneSize=" + worldBackplaneSize);
        if (worldBackplaneSize == null) {
            // headless?
            return null;
        }
        if (useControlPanel) {
            // ControlPanels fitting z offset.
            // zpos = -camera.getNear() - 0.1;
        }

        GuiGrid hud = new GuiGrid(dimension, zpos, buttonzpos, mode, columns, rows, background/*, GuiGrid.BLACK_FULLTRANSPARENT*/, useControlPanel);
        //18.12.22 use layer of camera hud/*5.12.18 .element*/.getTransform().setLayer(FovElement.LAYER);
        // move it to expected location.
        // zpos is negative because in the OpenGL camera space the z axis of the frustum runs into the negative part.
        if (hud.cp != null) {
            // move panel where backplane was in legacy grid. Depends on mode.
            /*hud.getTransform().setPosition(new Vector3(worldPlaneSize.width / 2 - worldBackplaneSize.getWidth() / 2,
                    -worldPlaneSize.height / 2 + worldBackplaneSize.getHeight() / 2, -zpos));*/
            Vector2 xytranslation = hud.getXyTranslation(hud.getNearPlaneDimension());
            hud.cp.getTransform().setPosition(new Vector3(xytranslation.getX(), xytranslation.getY(), zpos));
            // also set slayer of camera
            camera.getCarrier().attach(hud);
            hud.getTransform().setLayer(camera.getLayer());
        } else {
            hud/*5.12.18 .element*/.getTransform().setLayer(FovElement.LAYER);
        }
        return hud;
    }

    public static GuiGrid buildForCamera(Camera camera, int mode, int columns, int rows, Color background) {
        return buildForCamera(camera, mode, columns, rows, background, false);
    }

    /**
     * Alternative zu BrowsMenu. Used outside tcp-22.
     */
    public static GuiGrid buildSingleColumnFromMenuitems(DimensionF dimension, double zpos, double buttonzpos, MenuItem[] menuitems) {
        // this.camera = camera;
        /*this.dimension = dimension;
        this.menuitems = menuitems;
        this.zpos = zpos;
        this.buttonzpos = buttonzpos;*/
        int rows = menuitems.length;
        GuiGrid guiGrid = new GuiGrid(dimension, zpos, buttonzpos, 3, 1, rows, Color.BLACK_FULLTRANSPARENT);

        for (int i = 0; i < rows; i++) {
            guiGrid.addButton(null, 0, rows - 1 - i, 1, menuitems[i].guiTexture, menuitems[i].buttonDelegate);
        }
        return guiGrid;
    }

    /**
     * @param nearplaneSize
     * @return
     */
    private void calcSizes(DimensionF nearplaneSize) {
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

        backPlaneDimension = elsize;
        //return elsize;
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

    /**
     * Like BgElement.
     *
     * @return
     */
    private static Material buildBackplaneMaterial(Color background) {
        ImageData image = ImageFactory.buildSingleColor(256, 256, background);
        Texture texture = new Texture(image);
        // Double transparence? Color and material?
        Material mat = Material.buildBasicMaterial(texture, 0.5);
        return mat;
    }

    public double getZpos() {
        return zpos;
    }

    public double getButtonZpos() {
        return buttonzpos;
    }
}

@Deprecated
class BgElement extends FovElementPlane {
    public ImageData image;
    // gruener Hintergrund, der durch Transparenz blasser erscheint.
    public Color background;
    GuiGrid guiGrid;

    public BgElement(DimensionF nearPlaneDimension, double zpos, GuiGrid guiGrid, Color background) {
        super(nearPlaneDimension, zpos);
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

        // BasicMaterial, damit Beleuchtung keine Rolle spielt.
        //8.10.17: wie bei Hud kein Effect. Fuer trasnparent muss die Color den richtigen Alpha haben.
        Material mat = Material.buildBasicMaterial(texture, 0.5);
        return mat;
    }

    @Override
    public DimensionF getSize(DimensionF nearplaneSize) {


        //DimensionF elsize = guiGrid.calcElementSize(nearplaneSize);
        DimensionF elsize = guiGrid.getBackPlaneDimension();
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