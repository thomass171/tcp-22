package de.yard.threed.engine.vr;

import de.yard.threed.core.Color;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.gui.ControlPanel;
import de.yard.threed.engine.gui.ControlPanelHelper;
import de.yard.threed.engine.gui.PropertyPanel;

public class VrDebugPanel extends ControlPanel {

    static private double panelWidth = 0.6;
    static private double panelRowHeight = 0.1;
    static private double panelMargin = 0.005;
    static private int rows = 3;

    PropertyPanel xPanel, yPanel, zPanel;

    /**
     *
     */
    private VrDebugPanel(Material background) {
        super(new DimensionF(panelWidth, rows * panelRowHeight), background, 0.01);

        DimensionF rowsize = new DimensionF(panelWidth, panelRowHeight);

        xPanel = new PropertyPanel(rowsize, panelMargin, background, "x");
        add(new Vector2(0, ControlPanelHelper.calcYoffsetForRow(2, rows, panelRowHeight)), xPanel);
        yPanel = new PropertyPanel(rowsize, panelMargin, background, "y");
        add(new Vector2(0, ControlPanelHelper.calcYoffsetForRow(1, rows, panelRowHeight)), yPanel);
        zPanel = new PropertyPanel(rowsize, panelMargin, background, "z");
        add(new Vector2(0, ControlPanelHelper.calcYoffsetForRow(0, rows, panelRowHeight)), zPanel);
    }

    public void update() {

        VrInstance vrInstance = VrInstance.getInstance();
        if (vrInstance != null) {
            VRController controller = vrInstance.getController(0);
            // controller not emulated?
            if (controller != null) {
                Vector3 p = controller.getWorldPosition();

                xPanel.setValue("" + p.getX());
                yPanel.setValue("" + p.getY());
                zPanel.setValue("" + p.getZ());
            }
        }
    }

    public static VrDebugPanel buildVrDebugPanel() {
        Color backGround = new Color(128, 193, 255, 128);
        Material mat = Material.buildBasicMaterial(backGround, null);
        return new VrDebugPanel(mat);
    }
}
