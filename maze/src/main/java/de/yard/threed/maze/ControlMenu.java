package de.yard.threed.maze;

import de.yard.threed.engine.Camera;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.gui.ControlMenuBuilder;
import de.yard.threed.engine.gui.GuiGrid;
import de.yard.threed.engine.gui.Icon;
import de.yard.threed.engine.platform.common.Request;

public class ControlMenu implements ControlMenuBuilder {

    /**
     * Non VR Control Menu fuer Maze.
     * <p>
     * Created by thomass on 23.02.17.
     */
    public GuiGrid buildControlMenu(Camera camera) {
        // 7 Spalten (damits kleiner wird) und 2 Zeilen. Nicht mehr erfoderlich. Darum 3.
        // 4.10.18: Jetzt Pyramide wegen mehr Icons.
        GuiGrid controlmenu = GuiGrid.buildForCamera(camera, 2, 5, 3, GuiGrid.BLACK_FULLTRANSPARENT);
        int p = 0;

        //unten
        controlmenu.addButton(/*new Request(new RequestType("Left")),*/ 0, 0, 1, Icon.ICON_LEFTARROW, () -> {
            SystemManager.putRequest(new Request(RequestRegistry.TRIGGER_REQUEST_LEFT, null));
        });
        /*28.5.21:das lassen wir mal controlmenu.addButton( 1, 0, 1, Icon.IconCharacter(0), () -> {
            SystemManager.putRequest(new Request(RequestRegistry.TRIGGER_REQUEST_AUTOSOLVE, null));
        });*/
        controlmenu.addButton(/*new Request(new RequestType("Down")),*/ 2, 0, 1, Icon.ICON_DOWNARROW, () -> {
            SystemManager.putRequest(new Request(RequestRegistry.TRIGGER_REQUEST_BACK, null));
        });
        // 20.4.21: No longer mani menu, but close control menu
        //controlmenu.addButton(/*new Request(new RequestType("Menu")),*/ 3, 0, 1, Icon.ICON_MENU, () -> {
        controlmenu.addButton(/*new Request(new RequestType("Menu")),*/ 3, 0, 1, Icon.ICON_CLOSE, () -> {
            //openCloseMenu();
            SystemManager.putRequest(new Request(InputToRequestSystem.USER_REQUEST_CONTROLMENU, null));
        });
        controlmenu.addButton(/*new Request(new RequestType("Right")),*/ 4, 0, 1, Icon.ICON_RIGHTARROW, () -> {
            SystemManager.putRequest(new Request(RequestRegistry.TRIGGER_REQUEST_RIGHT, null));
        });

        //mitte
        controlmenu.addButton(/*new Request(new RequestType("TurnLeft")),*/ 1, 1, 1, Icon.ICON_TURNLEFT, () -> {
            SystemManager.putRequest(new Request(RequestRegistry.TRIGGER_REQUEST_TURNLEFT, null));
        });
        // 27.5.21: Pull instead of undo
        controlmenu.addButton(/*new Request(new RequestType("Undo")), */2, 1, 1, Icon.IconCharacter(15), () -> {
            SystemManager.putRequest(new Request(RequestRegistry.TRIGGER_REQUEST_PULL, null));
        });
        controlmenu.addButton(/*new Request(new RequestType("TurnRight")),*/ 3, 1, 1, Icon.ICON_TURNRIGHT, () -> {
            SystemManager.putRequest(new Request(RequestRegistry.TRIGGER_REQUEST_TURNRIGHT, null));
        });

        //oben
        controlmenu.addButton(/*new Request(new RequestType("Up")),*/ 2, 2, 1, Icon.ICON_UPARROW, () -> {
            SystemManager.putRequest(new Request(RequestRegistry.TRIGGER_REQUEST_FORWARD, null));
        });

        return controlmenu;
    }

}
