package de.yard.threed.engine.apps.vr;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.engine.geometry.Primitives;
import de.yard.threed.engine.gui.*;
import de.yard.threed.core.Color;
import de.yard.threed.core.DimensionF;
import de.yard.threed.engine.platform.common.SimpleGeometry;

import java.util.Map;


public class VrSceneHelper {
    static Log logger = Platform.getInstance().getLog(VrSceneHelper.class);

    // panel with 3 rows (dimesion 0.6x0.3)
    // rows must be quite narrow to have a proper property panel with text area large enough
    private static double PropertyControlPanelWidth = 0.6;
    private static double PropertyControlPanelRowHeight = 0.1;
    private static double PropertyControlPanelMargin = 0.005;
    private static Color controlPanelBackground = new Color(255, 217, 102, 128);

    /**
     * A general purpose control panel permanently attached to the left controller. Consists of
     * <p>
     * - button for opening a main menu
     * --- menu toggle
     * - finetune up/down
     * - info
     * - indicator on/off
     * <p>
     * Für Tests als Duplicate auch "im Raum".
     */
    public static ControlPanel buildControllerControlPanel(Map<String, ButtonDelegate> buttonDelegates) {
        Color backGround = controlPanelBackground;
        Material mat = Material.buildBasicMaterial(backGround, false);

        DimensionF rowsize = new DimensionF(PropertyControlPanelWidth, PropertyControlPanelRowHeight);
        double midElementWidth4 = PropertyControlPanelWidth / 4;
        double m4_2 = midElementWidth4 / 2;
        double h = PropertyControlPanelRowHeight;
        double h2 = PropertyControlPanelRowHeight / 2;

        ControlPanel cp = new ControlPanel(new DimensionF(PropertyControlPanelWidth, 3 * PropertyControlPanelRowHeight), mat);

        // top line: property yontrol
        cp.add(new Vector2(0, PropertyControlPanelRowHeight / 2 + PropertyControlPanelRowHeight / 2),
                ControlPanelHelper.buildPropertyControlPanel(rowsize, PropertyControlPanelMargin, mat));

        // mid line 4 elements : a indicator, a relocate/rest button
        Indicator indicator = Indicator.buildGreen(0.03);
        // half in ground
        cp.addArea(new Vector2(-midElementWidth4 - m4_2, 0), new DimensionF(midElementWidth4, PropertyControlPanelRowHeight), null).attach(indicator);
        cp.addArea(new Vector2(-m4_2, 0), new DimensionF(midElementWidth4, PropertyControlPanelRowHeight), () -> {
            logger.debug("area clicked");
            indicator.toggle();
        }).setIcon(Icon.ICON_LEFTARROW);
        cp.addArea(new Vector2(m4_2, 0), new DimensionF(midElementWidth4, PropertyControlPanelRowHeight), buttonDelegates.get("reset")).setIcon(Icon.ICON_POSITION);
        cp.addArea(new Vector2(midElementWidth4 + m4_2, 0), new DimensionF(midElementWidth4, PropertyControlPanelRowHeight), buttonDelegates.get("up")).setIcon(Icon.ICON_UPARROW);


        // bottom line:  one indicator and 3 buttons
        Indicator indicatorb = Indicator.buildRed(0.03);
        // half in ground
        cp.addArea(new Vector2(-midElementWidth4 - m4_2, -h), new DimensionF(midElementWidth4, PropertyControlPanelRowHeight), null).attach(indicatorb);
        cp.addArea(new Vector2(-m4_2, -h), new DimensionF(midElementWidth4, PropertyControlPanelRowHeight), buttonDelegates.get("mainmenu")).setIcon(Icon.ICON_MENU);
        cp.addArea(new Vector2(m4_2, -h), new DimensionF(midElementWidth4, PropertyControlPanelRowHeight), buttonDelegates.get("info")).setIcon(Icon.ICON_HELP);
        cp.addArea(new Vector2(midElementWidth4 + m4_2, -h), new DimensionF(midElementWidth4, PropertyControlPanelRowHeight), buttonDelegates.get("down")).setIcon(Icon.ICON_DOWNARROW);

        return cp;
    }

    public static SceneNode buildRedBox() {
        Geometry geometry = Geometry.buildCube(0.15f, 0.15f, 0.15f);
        SceneNode box1 = new SceneNode(new Mesh(geometry, Material.buildLambertMaterial(new Color(0xFF, 00, 00))));
        box1.setName("red box");
        box1.getTransform().setPosition(new Vector3(-1, 1, -2));
        return box1;
    }

    public static SceneNode buildBalken() {
        SceneNode balken = new SceneNode(new Mesh(Geometry.buildCube(0.1f, 0.1f, 1), Material.buildLambertMaterial(Texture.buildBundleTexture("data", "images/river.jpg"))));
        balken.setName("balken");
        balken.getTransform().setPosition(new Vector3(0, VrScene.BALKENYPOSITION, -2));
        return balken;
    }

    public static SceneNode buildGround() {
        Geometry geometry = Geometry.buildPlaneGeometry(25, 25, 1, 1);
        SceneNode ground = new SceneNode(new Mesh(geometry, Material.buildLambertMaterial(new Color(0x88, 0x88, 0x88))));
        ground.getTransform().setPosition(new Vector3(0, 0, 0));
        ground.setName("Ground");
        return ground;
    }

    public static SceneNode buildGroundMarker(Icon icon) {
        SimpleGeometry geometry = Primitives.buildSimpleXZPlaneGeometry(0.3, 0.3,icon.getUvMap());
        SceneNode box1 = new SceneNode(new Mesh(geometry,Material.buildBasicMaterial(icon.getTexture())));
        box1.setName("destination marker");
        box1.getTransform().setPosition(new Vector3(-1, 1, -24));
        return box1;
    }
}
