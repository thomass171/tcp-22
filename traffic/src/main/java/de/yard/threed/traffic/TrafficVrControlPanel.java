package de.yard.threed.traffic;

import de.yard.threed.core.Color;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.gui.ButtonDelegate;
import de.yard.threed.engine.gui.ControlPanel;
import de.yard.threed.engine.gui.ControlPanelArea;
import de.yard.threed.engine.gui.Icon;
import de.yard.threed.engine.gui.Indicator;
import de.yard.threed.engine.gui.PanelGrid;
import de.yard.threed.engine.gui.TextTexture;

import java.util.Map;

public class TrafficVrControlPanel extends ControlPanel {

    static Log logger = Platform.getInstance().getLog(TrafficVrControlPanel.class);

    private static double ControlPanelWidth = 0.3;
    private static double ControlPanelRowHeight = 0.1;
    private static int ControlPanelRows = 3;
    private static double[] ControlPanelColWidth = new double[]{0.1, 0.1, 0.1};
    private static double ControlPanelMargin = 0.005;
    private static Color controlPanelBackground = Color.LIGHTGREEN;

    /**
     * A traffic 3x3 control panel permanently attached to the left controller. Consists of
     * <p>
     * teleport - info - finetune up
     * - speed down - up
     * - info - - finetune down
     * more to come: info, reset?, home? menu?
     * This is the default control menu quite Railing/Demo specific.
     */
    public TrafficVrControlPanel(Map<String, ButtonDelegate> buttonDelegates) {

        super(new DimensionF(ControlPanelWidth, ControlPanelRows * ControlPanelRowHeight), Material.buildBasicMaterial(controlPanelBackground, null), 0.01);

        PanelGrid panelGrid = new PanelGrid(ControlPanelWidth, ControlPanelRowHeight, ControlPanelRows, ControlPanelColWidth);

        // top line:
        addArea(panelGrid.getPosition(0, 2), new DimensionF(ControlPanelColWidth[0], ControlPanelRowHeight), buttonDelegates.get("teleport")).setIcon(Icon.ICON_TURNRIGHT);
        addArea(panelGrid.getPosition(1, 2), new DimensionF(ControlPanelColWidth[1], ControlPanelRowHeight), buttonDelegates.get("info")).setIcon(Icon.ICON_HELP);
        addArea(panelGrid.getPosition(2, 2), new DimensionF(ControlPanelColWidth[2], ControlPanelRowHeight), buttonDelegates.get("up")).setIcon(Icon.ICON_UPARROW);

        // mid line
        addArea(panelGrid.getPosition(0, 1), new DimensionF(ControlPanelColWidth[2], ControlPanelRowHeight), buttonDelegates.get("speeddown")).setIcon(Icon.ICON_HORIZONTALLINE);
        addArea(panelGrid.getPosition(1, 1), new DimensionF(ControlPanelColWidth[2], ControlPanelRowHeight), buttonDelegates.get("speedup")).setIcon(Icon.ICON_PLUS);

        // bottom line:
        addArea(panelGrid.getPosition(2, 0), new DimensionF(ControlPanelColWidth[2], ControlPanelRowHeight), buttonDelegates.get("down")).setIcon(Icon.ICON_DOWNARROW);
    }
}
