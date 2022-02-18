package de.yard.threed.maze;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.gui.*;
import de.yard.threed.core.Color;
import de.yard.threed.core.DimensionF;

import java.util.Map;

public class MazeVrControlPanel extends ControlPanel {

    static Log logger = Platform.getInstance().getLog(MazeVrControlPanel.class);

    private static double ControlPanelWidth = 0.6;
    private static double ControlPanelRowHeight = 0.1;
    private static int ControlPanelRows = 3;
    private static double[] ControlPanelColWidth = new double[]{0.1, 0.2, 0.2, 0.1};
    private static double ControlPanelMargin = 0.005;
    private static Color controlPanelBackground = MazeSettings.hudColor;

    TextTexture boxCountTextTexture;
    ControlPanelArea boxCountArea;

    /**
     * A maze 3x3 control panel permanently attached to the left controller. Consists of
     * <p>
     * - three inventory counter
     * - finetune up/down
     * - info
     * TODO health indicator
     * +---+-------------------+
     * +cnt0+             up
     * |cnt1
     * |cnt2              down
     * -----------------------
     * <p>
     * Für Tests als Duplicate auch "im Raum" darstellbar.
     * <p>
     * Solange es noch keine Box,Diamond. Bullet iconsgibt, Buchstaben dafuer nehmen.
     */
    public MazeVrControlPanel(Map<String, ButtonDelegate> buttonDelegates) {

        super(new DimensionF(ControlPanelWidth, ControlPanelRows * ControlPanelRowHeight), Material.buildBasicMaterial(controlPanelBackground, false), 0.01);

        PanelGrid panelGrid = new PanelGrid(ControlPanelWidth, ControlPanelRowHeight, ControlPanelRows, ControlPanelColWidth);

        //ControlPanel cp = new ControlPanel(new DimensionF(ControlPanelWidth, ControlPanelRows * ControlPanelRowHeight), mat);

        // top line:
        //cp.add(new Vector2(0, PropertyControlPanelRowHeight / 2 + PropertyControlPanelRowHeight / 2),
        //        ControlPanelHelper.buildPropertyControlPanel(rowsize, PropertyControlPanelMargin, mat));
        addArea(panelGrid.getPosition(0, 2), new DimensionF(ControlPanelColWidth[0], ControlPanelRowHeight), null).setIcon(Icon.IconCharacter(1));
        addArea(panelGrid.getPosition(3, 2), new DimensionF(ControlPanelColWidth[3], ControlPanelRowHeight), buttonDelegates.get("up")).setIcon(Icon.ICON_UPARROW);

        // mid line 4 elements : a indicator, a relocate/rest button
        Indicator indicator = Indicator.buildGreen(0.03);
        // half in ground
/*        cp.addArea(new Vector2(-midElementWidth4 - m4_2, 0), new DimensionF(midElementWidth4, ControlPanelRowHeight), null).attach(indicator);
        cp.addArea(new Vector2(-m4_2, 0), new DimensionF(midElementWidth4, ControlPanelRowHeight), () -> {
            logger.debug("area clicked");
            indicator.toggle();
        }).setIcon(Icon.ICON_LEFTARROW);
        cp.addArea(new Vector2(m4_2, 0), new DimensionF(midElementWidth4, ControlPanelRowHeight), buttonDelegates.get("reset")).setIcon(Icon.ICON_POSITION);
*/
        addArea(panelGrid.getPosition(0, 1), new DimensionF(ControlPanelColWidth[0], ControlPanelRowHeight), null).setIcon(Icon.IconCharacter(3));
        addArea(panelGrid.getPosition(3, 1), new DimensionF(ControlPanelColWidth[3], ControlPanelRowHeight), buttonDelegates.get("pull")).setIcon(Icon.IconCharacter(15));


        // bottom line:  one indicator and 3 buttons
        Indicator indicatorb = Indicator.buildRed(0.03);
        // half in ground
  /*      cp.addArea(new Vector2(-midElementWidth4 - m4_2, -h), new DimensionF(midElementWidth4, ControlPanelRowHeight), null).attach(indicatorb);
        cp.addArea(new Vector2(-m4_2, -h), new DimensionF(midElementWidth4, ControlPanelRowHeight), buttonDelegates.get("mainmenu")).setIcon(Icon.ICON_MENU);
        cp.addArea(new Vector2(m4_2, -h), new DimensionF(midElementWidth4, ControlPanelRowHeight), buttonDelegates.get("info")).setIcon(Icon.ICON_HELP);
        */
        boxCountTextTexture = new TextTexture(controlPanelBackground);
        addArea(panelGrid.getPosition(0, 0), new DimensionF(ControlPanelColWidth[0], ControlPanelRowHeight), null).setIcon(Icon.IconCharacter(23));
        boxCountArea = addArea(panelGrid.getPosition(1, 0), new DimensionF(ControlPanelColWidth[1], ControlPanelRowHeight), null);
        boxCountArea.setTexture(boxCountTextTexture.getTextureForText("-"));
        addArea(new Vector2(panelGrid.midx[3], panelGrid.midy[0]), new DimensionF(ControlPanelColWidth[3], ControlPanelRowHeight), buttonDelegates.get("down")).setIcon(Icon.ICON_DOWNARROW);

        //return cp;
    }

    public void setBoxesCount(int value) {
        boxCountArea.setTexture(boxCountTextTexture.getTextureForText("" + value));
    }
}
