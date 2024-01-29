package de.yard.threed.engine.apps.showroom;

import de.yard.threed.core.Color;
import de.yard.threed.engine.BaseRequestRegistry;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.gui.ControlMenuBuilder;
import de.yard.threed.engine.gui.GuiGrid;
import de.yard.threed.engine.gui.Icon;
import de.yard.threed.engine.platform.common.Request;

public class ShowroomControlMenuBuilder implements ControlMenuBuilder {

    ShowroomScene showroomScene;

    public ShowroomControlMenuBuilder(ShowroomScene showroomScene){
        this.showroomScene=showroomScene;
    }

    /**
     * Non VR Control menu.
     * <p>
     */
    public GuiGrid buildControlMenu(Camera camera) {

        GuiGrid controlmenu = GuiGrid.buildForCamera(camera, 2, 5, 1, Color.BLACK_FULLTRANSPARENT, true);

        controlmenu.addButton(0, 0, 1, Icon.ICON_HORIZONTALLINE, () -> {
            showroomScene.scale(showroomScene.box1.getSceneNode(),0.9);
        });
        controlmenu.addButton(2, 0, 1, Icon.ICON_PLUS, () -> {
            showroomScene.scale(showroomScene.box1.getSceneNode(),1.1);
        });
        controlmenu.addButton(3, 0, 1, Icon.ICON_CLOSE, () -> {
            //openCloseMenu();
            InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_CONTROLMENU));
        });
        return controlmenu;
    }

}
