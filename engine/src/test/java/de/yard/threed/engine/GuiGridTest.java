package de.yard.threed.engine;


import de.yard.threed.core.Color;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.gui.FovElement;
import de.yard.threed.engine.gui.GuiGrid;
import de.yard.threed.engine.gui.Icon;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.core.Dimension;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;

import de.yard.threed.engine.platform.common.Settings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Created by thomass on 13.02.17.
 */
public class GuiGridTest {

    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new PlatformFactoryHeadless());

    float width = 800;
    float height = 600;

    @Test
    public void testGuiGrid() {
        Dimension d = new Dimension(400, 200);
        Camera camera = new PerspectiveCamera(Platform.getInstance().buildPerspectiveCamera(Settings.defaultfov, ((double) d.width) / (double) d.height, Settings.defaultnear, Settings.defaultfar)/* TODO MA36 new OpenGlPerspectiveCamera(*/);
        // 3 Spalten  und 2 Zeilen. 
        GuiGrid menu = GuiGrid.buildForCamera(camera, 2, 3, 2, GuiGrid.GREEN_SEMITRANSPARENT);
        int p = 0;
        menu.addButton(new Request(RequestType.register(1010, "Left")), p + 0, 0, Icon.ICON_LEFTARROW);
        menu.addButton(new Request(RequestType.register(1011, "Up")), p + 1, 1, Icon.ICON_UPARROW);
        menu.addButton(new Request(RequestType.register(1012, "Right")), p + 2, 0, Icon.ICON_RIGHTARROW);

        menu.addButton(new Request(RequestType.register(1013, "Menu")), p + 0, 1, Icon.ICON_VERTICALLINE);
        menu.addButton(new Request(RequestType.register(1014, "Undo")), p + 1, 0, Icon.IconCharacter(20));
        menu.addButton(new Request(RequestType.register(1015, "Auto")), p + 2, 1, Icon.ICON_HORIZONTALLINE);
        //irgendwie nicht richtig testbar.
    }

    @Test
    public void testMainMenuOfReferenceScene() {
        runMainMenuOfReferenceScene(false);
        runMainMenuOfReferenceScene(true);
    }

    public void runMainMenuOfReferenceScene(boolean useCP) {

        Camera camera = new PerspectiveCamera(Platform.getInstance().buildPerspectiveCamera(Settings.defaultfov, width / height, Settings.defaultnear, Settings.defaultfar));
        // what MainMenuBuilder does
        GuiGrid menu = GuiGrid.buildForCamera(camera, 1, 6, 3, GuiGrid.GREEN_SEMITRANSPARENT, useCP);

        // ref values taken as is
        assertPlanes(menu, Settings.defaultnear, new DimensionF(0.110456, 0.082842), new DimensionF(0.082842, 0.041421),
                new DimensionF(0.013807, 0.013807));

    }

    @Test
    public void testControlMenuOfReferenceScene() {
        runControlMenuOfReferenceScene(false);
        runControlMenuOfReferenceScene(true);
    }

    public void runControlMenuOfReferenceScene(boolean useCP) {

        Camera camera = new PerspectiveCamera(Platform.getInstance().buildPerspectiveCamera(Settings.defaultfov, width / height, Settings.defaultnear, Settings.defaultfar));
        // what ReferenceScene does. deferred fov camera has near/far 5/6.
        double near = 5.0;
        Camera cameraForControlMenu = FovElement.getDeferredCamera(camera);
        GuiGrid controlMenu = GuiGrid.buildForCamera(cameraForControlMenu, 2, 1, 1, Color.BLACK_FULLTRANSPARENT, useCP);
        controlMenu.setName("ControlIcon");
        controlMenu.addButton(null, 0, 0, 1, Icon.ICON_POSITION, null);
        cameraForControlMenu.getCarrier().attach(controlMenu);

        // ref values taken as is
        assertPlanes(controlMenu, near, new DimensionF(5.522847, 4.142135), new DimensionF(0.4, 0.4), new DimensionF(0.4, 0.4));
        TestUtils.assertVector2(new Vector2(0, -1.87106), controlMenu.getXyTranslation(controlMenu.getNearPlaneDimension()), "XyTranslation");

        if (useCP) {
            // with ControlPanel, buttons are child of the backplane. The invisble(?) menu is still at z=0! Strange.
            assertEquals(1, controlMenu.getTransform().getChildCount());
            assertEquals(0, controlMenu.getTransform().getPosition().getZ());
            Transform backplane = controlMenu.getTransform().getChild(0);
            assertEquals(-5.0001, backplane.getPosition().getZ());
            assertEquals(1, backplane.getChildCount());
            Transform button = backplane.getChild(0);
            assertEquals(0.00001, button.getPosition().getZ());
        } else {
            // the invisble(?) menu is at z=0! Strange. The other z values are also strange. Probably just the result of trial and error.
            assertEquals(0, controlMenu.getTransform().getPosition().getZ());
            assertEquals(2, controlMenu.getTransform().getChildCount());
            Transform backplane = controlMenu.getTransform().getChild(0);
            assertEquals(-5.0001, backplane.getPosition().getZ());
            Transform button = controlMenu.getTransform().getChild(1);
            assertEquals(-5.00009, button.getPosition().getZ());
        }
    }

    private void assertPlanes(GuiGrid menu, double near, DimensionF expectedNearPlaneDimension, DimensionF expectedEffectivePlaneDimension, DimensionF expectedCellDimension) {

        TestUtils.assertDimensionF(expectedNearPlaneDimension, menu.getNearPlaneDimension(), 0.00001, "nearPlaneDimension");

        assertEquals(-(near + GuiGrid.MAIN_Z_OFFSET), menu.getZpos());
        assertEquals(-near - GuiGrid.MAIN_Z_OFFSET + GuiGrid.MAIN_BUTTON_Z_OFFSET, menu.getButtonZpos());

        // Effective menu backplane is smaller than near plane, depending on mode.
        TestUtils.assertDimensionF(expectedEffectivePlaneDimension, menu.getBackPlaneDimension(), 0.00001, "backPlaneDimension");

        TestUtils.assertDimensionF(expectedCellDimension, menu.getCellDimension(), 0.00001, "CellDimension");
    }
}


        
