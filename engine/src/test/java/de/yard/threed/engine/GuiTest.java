package de.yard.threed.engine;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.gui.GuiGrid;
import de.yard.threed.engine.gui.Icon;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.core.Dimension;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;

import de.yard.threed.engine.platform.common.Settings;
import org.junit.jupiter.api.Test;

import static de.yard.threed.core.testutil.Assert.assertEquals;

/**
 * Created by thomass on 13.02.17.
 */
public class GuiTest {

    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine"}, new PlatformFactoryHeadless());

    @Test
    public void testGuiGrid() {
        Dimension d = new Dimension(400, 200);
        Camera camera = new PerspectiveCamera(Platform.getInstance().buildPerspectiveCamera(Settings.defaultfov, ((double) d.width) / (double) d.height, Settings.defaultnear, Settings.defaultfar)/* TODO MA36 new OpenGlPerspectiveCamera(*/);
        // 3 Spalten  und 2 Zeilen. 
        GuiGrid menu =  GuiGrid.buildForCamera(camera, 2, 3, 2,GuiGrid.GREEN_SEMITRANSPARENT);
        int p = 0;
        menu.addButton(new Request(new RequestType("Left")), p + 0, 0, Icon.ICON_LEFTARROW);
        menu.addButton(new Request(new RequestType("Up")), p + 1, 1, Icon.ICON_UPARROW);
        menu.addButton(new Request(new RequestType("Right")), p + 2, 0, Icon.ICON_RIGHTARROW);

        menu.addButton(new Request(new RequestType("Menu")), p + 0, 1, Icon.ICON_VERTICALLINE);
        menu.addButton(new Request(new RequestType("Undo")), p + 1, 0, Icon.IconCharacter(20));
        menu.addButton(new Request(new RequestType("Auto")), p + 2, 1, Icon.ICON_HORIZONTALLINE);
        //irgendwie nicht richtig testbar.
    }
}


        
