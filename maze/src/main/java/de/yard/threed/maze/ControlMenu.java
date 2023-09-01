package de.yard.threed.maze;

import de.yard.threed.core.Color;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.BaseRequestRegistry;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.gui.ControlMenuBuilder;
import de.yard.threed.engine.gui.GuiGrid;
import de.yard.threed.engine.gui.Icon;
import de.yard.threed.engine.platform.common.Request;

public class ControlMenu implements ControlMenuBuilder {

    /**
     * Non VR Control menu for maze.
     * <p>
     * Created by thomass on 23.02.17.
     */
    public GuiGrid buildControlMenu(Camera camera) {

        // 4.10.18: pyramid like for having more icons.
        GuiGrid controlmenu = GuiGrid.buildForCamera(camera, 2, 5, 3, Color.BLACK_FULLTRANSPARENT, true);

        //bottom
        controlmenu.addButton(0, 0, 1, Icon.ICON_LEFTARROW, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(BaseRequestRegistry.TRIGGER_REQUEST_LEFT));
        });
        /*28.5.21:better no autosolve. Its too complex and might never end. controlmenu.addButton( 1, 0, 1, Icon.IconCharacter(0), () -> {
            SystemManager.putRequest(new Request(RequestRegistry.TRIGGER_REQUEST_AUTOSOLVE, null));
        });*/
        controlmenu.addButton(2, 0, 1, Icon.ICON_DOWNARROW, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(BaseRequestRegistry.TRIGGER_REQUEST_BACK));
        });
        // 20.4.21: No longer a open main menu item, but close control menu
        //controlmenu.addButton(/*new Request(RequestType.register("Menu")),*/ 3, 0, 1, Icon.ICON_MENU, () -> {
        controlmenu.addButton(3, 0, 1, Icon.ICON_CLOSE, () -> {
            //openCloseMenu();
            InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_CONTROLMENU));
        });
        controlmenu.addButton(4, 0, 1, Icon.ICON_RIGHTARROW, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(BaseRequestRegistry.TRIGGER_REQUEST_RIGHT));
        });

        //mid
        controlmenu.addButton(1, 1, 1, Icon.ICON_TURNLEFT, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(BaseRequestRegistry.TRIGGER_REQUEST_TURNLEFT));
        });
        // 27.5.21: 'P': pull instead of undo
        controlmenu.addButton(2, 1, 1, Icon.IconCharacter(15), () -> {
            InputToRequestSystem.sendRequestWithId(new Request(MazeRequestRegistry.TRIGGER_REQUEST_PULL));
        });
        controlmenu.addButton(3, 1, 1, Icon.ICON_TURNRIGHT, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(BaseRequestRegistry.TRIGGER_REQUEST_TURNRIGHT));
        });

        //top
        controlmenu.addButton(2, 2, 1, Icon.ICON_UPARROW, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(BaseRequestRegistry.TRIGGER_REQUEST_FORWARD));
        });

        return controlmenu;
    }

}
