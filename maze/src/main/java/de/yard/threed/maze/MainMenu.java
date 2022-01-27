package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.Util;
import de.yard.threed.engine.*;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.gui.*;
import de.yard.threed.core.Payload;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;

/**
 * 16.4.21: Erstmal no longer used. Level change geht einfach über Neustart.
 * <p>
 * Created by thomass on 23.02.17.
 */
public class MainMenu implements MenuProvider {
    Camera camera;

    public MainMenu(Camera camera) {
        this.camera = camera;
    }

    @Override
    public Menu buildMenu() {
        //if (menu == null) {
        // 3 Spalten und 6 Zeilen. Liegt in der near plane; schlecht für VR
        GuiGrid guiGrid = GuiGrid.buildForCamera(camera, 1, 3, 6, GuiGrid.GRAY_NONTRANSPARENT);
        //
        Menu menu = new GuiGridMenu(guiGrid);

        guiGrid.addButton(new Request(new RequestType("close")), 2, 5, 1, Icon.ICON_CLOSE, () -> {
            // close myself. Might have sideeffects with invalid references to menu?
            menu.remove();
            //3.4.21 abstractMazeScene.openCloseMenu();
        });
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 3; x++) {
                // nr ab 1
                int nr = 1 + 3 * y + x;
                guiGrid.addButton(new Request(new RequestType("level" + nr)), x, 4 - y, 1, Icon.IconNumber(nr), () -> {
                    int levelnr = nr;
                    //10.11.20 abstractMazeScene.loadLevel(abstractMazeScene.abstractMaze.levellist.get(levelnr - 1));
                    //3.4.21 abstractMazeScene.openCloseMenu();
                    menu.remove();
                    SystemManager.putRequest(new Request(RequestRegistry.MAZE_REQUEST_LOADLEVEL, new Payload((nr-1))));
                });
            }
        }

        //}
        //3.4.21 camera.getCarrier().attach(menu);
        return menu;
    }

    @Override
    public Transform getAttachNode() {
        return camera.getCarrier().getTransform();
    }

    @Override
    public Ray getRayForUserClick(Point mouselocation) {
        return (Ray) Util.notyet();
    }
}
