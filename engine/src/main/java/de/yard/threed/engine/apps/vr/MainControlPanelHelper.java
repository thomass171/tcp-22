package de.yard.threed.engine.apps.vr;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.gui.ControlPanel;
import de.yard.threed.engine.gui.Icon;
import de.yard.threed.engine.gui.Indicator;
import de.yard.threed.core.Color;
import de.yard.threed.core.DimensionF;
import de.yard.threed.engine.gui.SpinnerControlPanel;

/**
 * A panel permanently attached to the scene. Consists of
 *
 * - several property panels for VR values
 *
 * Erstmal noch nicht
 */
public class MainControlPanelHelper {
    static Log logger = Platform.getInstance().getLog(MainControlPanelHelper.class);

    // panel with 3 rows (dimesion 0.6x0.3)
    // rows must be quite narrow to have a proper property panel with text area large enough
    private static double PropertyControlPanelWidth = 0.6;
    private static double PropertyControlPanelRowHeight = 0.1;
    private static double PropertyControlPanelMargin = 0.005;
    private static Color controlPanelBackground = new Color(255, 217, 102, 128);

    public static ControlPanel buildMainControlPanel() {
        Color backGround = controlPanelBackground;
        Material mat = Material.buildBasicMaterial(backGround, false);

        DimensionF rowsize = new DimensionF(PropertyControlPanelWidth, PropertyControlPanelRowHeight);
        ControlPanel cp = new ControlPanel(new DimensionF(PropertyControlPanelWidth, 3 * PropertyControlPanelRowHeight), mat, 0.01);

        // top line: property yontrol
        cp.add(new Vector2(0, PropertyControlPanelRowHeight / 2 + PropertyControlPanelRowHeight / 2),
                new SpinnerControlPanel(rowsize, PropertyControlPanelMargin, mat,null));

        // mid line: a indicator
        Indicator indicator = Indicator.buildGreen(0.03);
        // half in ground
        cp.addArea(new Vector2(0, 0), new DimensionF(PropertyControlPanelWidth / 4,
                PropertyControlPanelRowHeight), null);
        cp.attach(indicator);

        // bottom line:  a button
        cp.addArea(new Vector2(0, -PropertyControlPanelRowHeight/*PropertyControlPanelWidth/2,PropertyControlPanelRowHeight/2)*/), new DimensionF(PropertyControlPanelWidth,
                PropertyControlPanelRowHeight), () -> {
            logger.debug("area clicked");
            indicator.toggle();
        }).setIcon(Icon.ICON_POSITION);

        return cp;
    }
}
