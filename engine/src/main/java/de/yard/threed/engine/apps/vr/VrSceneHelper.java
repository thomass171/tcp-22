package de.yard.threed.engine.apps.vr;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.core.geometry.Primitives;
import de.yard.threed.engine.gui.*;
import de.yard.threed.core.Color;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.engine.vr.VrOffsetWrapper;

import java.util.Map;


public class VrSceneHelper {
    static Log logger = Platform.getInstance().getLog(VrSceneHelper.class);

    // panel with 3 rows (dimesion 0.6x0.3)
    // rows must be quite narrow to have a proper property panel with text area large enough
    private static double ControlPanelWidth = 0.6;
    private static double ControlPanelRowHeight = 0.1;
    private static double ControlPanelMargin = 0.005;
    private static Color controlPanelBackground = new Color(255, 217, 102, 128);

    public static double PLATFORM_X_POSITION = 25 / 2 + 5 / 2;
    public static double BARYPOSITION = 1;
    public static double SECONDBARYPOSITION = 4;
    public static double PLATFORM_HEIGHT = 4;
    public static double PLATFORM_ABOVE_GROUND = PLATFORM_HEIGHT / 2;

    /**
     * A simple control panel (4 row, 4 columns) permanently attached to the left controller. Consists of
     * <p>
     * - button for opening a main menu
     * top line: cycle observer - cycle info - cycle observer
     * medium: indicator - indicator on/off - Reset - finetune up
     * bottom lime: unused indicator - menu toggle - info   finetune down
     * -
     * <p>
     * Für Tests als Duplicate auch "im Raum".
     */
    public static ControlPanel buildControllerControlPanel(Map<String, ButtonDelegate> buttonDelegates) {
        Color backGround = controlPanelBackground;
        Material mat = Material.buildBasicMaterial(backGround, null);

        int rows = 4;
        DimensionF rowsize = new DimensionF(ControlPanelWidth, ControlPanelRowHeight);
        double midElementWidth4 = ControlPanelWidth / 4;
        double m4_2 = midElementWidth4 / 2;
        double h = ControlPanelRowHeight;
        double h2 = ControlPanelRowHeight / 2;

        ControlPanel cp = new ControlPanel(new DimensionF(ControlPanelWidth, rows * ControlPanelRowHeight), mat, 0.01);

        // top line: property control for yvroffset
        cp.add(new Vector2(0, h + h2), new SpinnerControlPanel(rowsize, ControlPanelMargin, mat, new NumericSpinnerHandler(0.1, new VrOffsetWrapper()), Color.RED));

        // top mid line
        cp.addArea(new Vector2(-midElementWidth4 - m4_2, h2), new DimensionF(midElementWidth4, ControlPanelRowHeight), buttonDelegates.get("cycleLeft")).setIcon(Icon.ICON_LEFTARROW);
        cp.addArea(new Vector2(-m4_2, h2), new DimensionF(midElementWidth4, ControlPanelRowHeight), buttonDelegates.get("cycleRight")).setIcon(Icon.ICON_RIGHTARROW);

        // bottom mid line 4 elements : a indicator, a relocate/rest button
        Indicator indicator = Indicator.buildGreen(0.03);
        // half in ground
        cp.addArea(new Vector2(-midElementWidth4 - m4_2, -h2), new DimensionF(midElementWidth4, ControlPanelRowHeight), null).attach(indicator);
        cp.addArea(new Vector2(-m4_2, -h2), new DimensionF(midElementWidth4, ControlPanelRowHeight), () -> {
            logger.debug("area clicked");
            indicator.toggle();
        }).setIcon(Icon.ICON_LEFTARROW);
        cp.addArea(new Vector2(m4_2, -h2), new DimensionF(midElementWidth4, ControlPanelRowHeight), buttonDelegates.get("reset")).setIcon(Icon.ICON_POSITION);
        cp.addArea(new Vector2(midElementWidth4 + m4_2, -h2), new DimensionF(midElementWidth4, ControlPanelRowHeight), buttonDelegates.get("up")).setIcon(Icon.ICON_UPARROW);


        // bottom line:  one indicator and 3 buttons
        Indicator indicatorb = Indicator.buildRed(0.03);
        // half in ground
        cp.addArea(new Vector2(-midElementWidth4 - m4_2, -h - h2), new DimensionF(midElementWidth4, ControlPanelRowHeight), null).attach(indicatorb);
        cp.addArea(new Vector2(-m4_2, -h - h2), new DimensionF(midElementWidth4, ControlPanelRowHeight), buttonDelegates.get("mainmenu")).setIcon(Icon.ICON_MENU);
        cp.addArea(new Vector2(m4_2, -h - h2), new DimensionF(midElementWidth4, ControlPanelRowHeight), buttonDelegates.get("info")).setIcon(Icon.ICON_HELP);
        cp.addArea(new Vector2(midElementWidth4 + m4_2, -h - h2), new DimensionF(midElementWidth4, ControlPanelRowHeight), buttonDelegates.get("down")).setIcon(Icon.ICON_DOWNARROW);

        return cp;
    }

    public static SceneNode buildRedBox() {
        Geometry geometry = Geometry.buildCube(0.15f, 0.15f, 0.15f);
        SceneNode box1 = new SceneNode(new Mesh(geometry, Material.buildLambertMaterial(new Color(0xFF, 00, 00))));
        box1.setName("red box");
        box1.getTransform().setPosition(new Vector3(-1, 1, -2));
        return box1;
    }

    public static SceneNode buildBar() {
        SceneNode balken = new SceneNode(new Mesh(Geometry.buildCube(0.1f, 0.1f, 1), Material.buildLambertMaterial(Texture.buildBundleTexture("data", "images/river.jpg"))));
        balken.setName("balken");
        balken.getTransform().setPosition(new Vector3(0, BARYPOSITION, -2));
        return balken;
    }

    public static SceneNode buildGround() {
        Geometry geometry = Geometry.buildPlaneGeometry(25, 25, 1, 1);
        SceneNode ground = new SceneNode(new Mesh(geometry, Material.buildLambertMaterial(new Color(0x88, 0x88, 0x88))));
        ground.getTransform().setPosition(new Vector3(0, 0, 0));
        ground.setName("Ground");
        return ground;
    }

    /**
     * A block attached at the right edge of the ground providing a platform that is 2 meters above ground.
     */
    public static SceneNode buildPlatform() {
        Geometry geometry = Geometry.buildCube(5, PLATFORM_HEIGHT, 25);
        SceneNode ground = new SceneNode(new Mesh(geometry, Material.buildLambertMaterial(Color.DARKGREEN)));
        ground.getTransform().setPosition(new Vector3(PLATFORM_X_POSITION, 0, 0));
        ground.setName("Platform");
        return ground;
    }

    /**
     * A second bar 4 meter above ground and related to the platform (25 m right).
     *
     * @return
     */
    public static SceneNode buildSecondBar() {
        SceneNode balken = new SceneNode(new Mesh(Geometry.buildCube(0.1f, 0.1f, 1), Material.buildLambertMaterial(Texture.buildBundleTexture("data", "images/river.jpg"))));
        balken.setName("balken");
        balken.getTransform().setPosition(new Vector3(PLATFORM_X_POSITION, SECONDBARYPOSITION, -2));
        return balken;
    }

    public static SceneNode buildGroundMarker(Icon icon) {
        SimpleGeometry geometry = Primitives.buildSimpleXZPlaneGeometry(0.3, 0.3, icon.getUvMap());
        SceneNode box1 = new SceneNode(new Mesh(geometry, Material.buildBasicMaterial(icon.getTexture())));
        box1.setName("destination marker");
        box1.getTransform().setPosition(new Vector3(-1, 1, -24));
        return box1;
    }
}
