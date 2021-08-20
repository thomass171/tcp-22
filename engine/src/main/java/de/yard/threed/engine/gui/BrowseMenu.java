package de.yard.threed.engine.gui;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.DimensionF;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;

/**
 * Ein Menu zum blaettern.
 * T.B.C.
 * <p>
 * 7.10.19: Auch von Camera getrennt.
 * <p>
 * 26.11.19: Deprecated, weil es friemelig/konfus ist und mit MenuCycler vielleicht eine gute Alternative besteht. Und in VR
 * sollte es eh eine Art Panel (z.B: CDU) geben.
 * Created on 23.11.18.
 */
@Deprecated
public class BrowseMenu {
    Log logger = Platform.getInstance().getLog(BrowseMenu.class);
    public GuiGrid grid;
    int page = 0;
    //Camera camera;
    MenuItem[][] menuitems;
    //row0 ist up button, row6 down
    static int rows = 7;
    GridButton[] buttons = new GridButton[rows - 2];
    RequestType REQUEST_UP = new RequestType("up");
    RequestType REQUEST_DOWN = new RequestType("down");
    DimensionF dimension;
    double zpos, buttonzpos;

    public BrowseMenu(/*Camera camera,*/DimensionF dimension, double zpos, double buttonzpos, MenuItem[][] menuitems) {
        // this.camera = camera;
        this.dimension = dimension;
        this.menuitems = menuitems;
        this.zpos = zpos;
        this.buttonzpos = buttonzpos;
        buildGrid();
    }

    public void remove() {
        grid.remove();
    }

    public static BrowseMenu buildForCamera(Camera camera, MenuItem[][] menuitems) {
        //PerspectiveCamera deferredcamera = FovElement.getDeferredCamera(camera);
        DimensionF dimension = camera.getNearplaneSize();
        int level = 0;
        double zpos = -camera.getNear() - 0.0001f + (level * 0.00001f);
        level = 1;
        double buttonzpos = -camera.getNear() - 0.0001f + (level * 0.00001f);
        BrowseMenu browseMenu = new BrowseMenu(dimension, zpos, buttonzpos, menuitems);
        browseMenu.grid.getTransform().setLayer(FovElement.LAYER);
        return browseMenu;
    }

    private void showPage() {
        logger.debug("showing page");
        MenuItem[] mi = menuitems[page];
        for (int i = 0; i < mi.length; i++) {
            buttons[i].setIcon(mi[i].guiTexture);
            buttons[i].setDelegate(mi[i].buttonDelegate);
            //buttons[i].setRequest(mi[i].request);
        }
    }

    private void cyclePage(int inc) {
        int newpage = page + inc;
        if (newpage < 0) {
            newpage = menuitems.length - 1;
        }
        if (newpage >= menuitems.length) {
            newpage = 0;
        }
        page = newpage;
        showPage();
    }

    private void buildGrid() {
        grid = new GuiGrid/*.buildForCamera(camera,*/(dimension, zpos, buttonzpos, 3, 1, rows, GuiGrid.BLACK_FULLTRANSPARENT);
        grid.addButton(new Request(REQUEST_UP), 0, 6, 1, Icon.ICON_UPARROW, () -> {
            logger.debug("up");
            cyclePage(-1);
        });
        for (int i = 0; i < rows - 2; i++) {
            buttons[i] = grid.addButton(null, 0, rows - 2 - i, 1, Texture.buildBundleTexture("data", "images/river.jpg"), null);
        }
        grid.addButton(new Request(REQUEST_DOWN), 0, 0, 1, Icon.ICON_DOWNARROW, () -> {
            logger.debug("down");
            cyclePage(1);
        });
        //menu.addButton("close", 0, 1, "Close");
        showPage();
    }

    /*public Request checkForClickedButton(Point mouselocation) {
        return grid.checkForClickedArea(camera.buildPickingRay(mouselocation));
    }*/

    public boolean checkForButtonByRay(Ray ray) {
       return grid.checkForClickedArea(ray);
    }
}
