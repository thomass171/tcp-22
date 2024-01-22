package de.yard.threed.engine.apps.showroom;

import de.yard.threed.core.Color;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.gui.ButtonDelegate;
import de.yard.threed.engine.gui.ControlPanel;
import de.yard.threed.engine.gui.Icon;
import de.yard.threed.engine.gui.PanelGrid;
import de.yard.threed.engine.gui.TextTexture;

import java.util.Map;

/**
 *
 */
public class ShowroomVrControlPanel extends ControlPanel {

    Log logger = Platform.getInstance().getLog(ShowroomVrControlPanel.class);

    private static double ControlPanelWidth = 0.6;
    private static double ControlPanelRowHeight = 0.1;
    private static int ControlPanelRows = 3;
    private static double[] ControlPanelColWidth = new double[]{0.1, 0.2, 0.2, 0.1};
    private static double ControlPanelMargin = 0.005;
    private static Color controlPanelBackground = Color.LIGHTBLUE;

    TextTexture boxCountTextTexture;

    /**
     * A maze 4x3 control panel permanently attached to the left controller. Consists of
     * <p>
     * -
     * - finetune up/down
     * - info
     *
     * +-+----+-------------------+
     * Bu|cnt0+             up
     * Di|cnt1
     * Bx|cnt2              down
     * -----------------------
     * <p>
     * Für Tests als Duplicate auch "im Raum" darstellbar.
     * <p>
     */
    public ShowroomVrControlPanel(Map<String, ButtonDelegate> buttonDelegates) {

        super(new DimensionF(ControlPanelWidth, ControlPanelRows * ControlPanelRowHeight), Material.buildBasicMaterial(controlPanelBackground, false), 0.01);

        PanelGrid panelGrid = new PanelGrid(ControlPanelWidth, ControlPanelRowHeight, ControlPanelRows, ControlPanelColWidth);

        // top line:
        addArea(panelGrid.getPosition(0, 2), new DimensionF(ControlPanelColWidth[0], ControlPanelRowHeight), null).setIcon(Icon.IconCharacter(1));
        addArea(panelGrid.getPosition(3, 2), new DimensionF(ControlPanelColWidth[3], ControlPanelRowHeight), buttonDelegates.get("up")).setIcon(Icon.ICON_UPARROW);

        // mid line 4 elements : a indicator, a relocate/rest button
        addArea(panelGrid.getPosition(0, 1), new DimensionF(ControlPanelColWidth[0], ControlPanelRowHeight), null).setIcon(Icon.IconCharacter(3));
        addArea(panelGrid.getPosition(3, 1), new DimensionF(ControlPanelColWidth[3], ControlPanelRowHeight), buttonDelegates.get("pull")).setIcon(Icon.IconCharacter(15));

        // bottom line:  one indicator and 3 buttons
        boxCountTextTexture = new TextTexture(controlPanelBackground);
        addArea(panelGrid.getPosition(0, 0), new DimensionF(ControlPanelColWidth[0], ControlPanelRowHeight), null).setIcon(Icon.IconCharacter(23));
        //boxCountArea = addArea(panelGrid.getPosition(1, 0), new DimensionF(ControlPanelColWidth[1], ControlPanelRowHeight), null);
        //boxCountArea.setTexture(boxCountTextTexture.getTextureForText("-", Color.RED));
        addArea(new Vector2(panelGrid.midx[3], panelGrid.midy[0]), new DimensionF(ControlPanelColWidth[3], ControlPanelRowHeight), buttonDelegates.get("down")).setIcon(Icon.ICON_DOWNARROW);
    }
}
