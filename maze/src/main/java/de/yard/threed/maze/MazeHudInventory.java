package de.yard.threed.maze;

import de.yard.threed.core.Vector2;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.gui.ControlPanel;
import de.yard.threed.engine.gui.ControlPanelArea;
import de.yard.threed.engine.gui.ControlPanelHelper;
import de.yard.threed.engine.gui.TextTexture;
import de.yard.threed.core.Color;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.DimensionF;

/**
 * An area at the right bottom of the display (Outside VR only).
 */
public class MazeHudInventory implements MazeInventory {

    ControlPanel controlPanel;
    private Color backgroundColor = MazeSettings.hudColor;
    private BasicInventory basicInventory;

    public MazeHudInventory(Camera deferredcamera, Dimension dimension) {
        controlPanel = ControlPanelHelper.buildInventoryForDeferredCamera(deferredcamera, dimension, backgroundColor, new Dimension(90, 20));
        if (controlPanel == null) {
            // headless?
            return;
        }
        double w3 = controlPanel.getSize().getWidth() / 3;
        // occupy mid third of inventory for now
        basicInventory = new BasicInventory(
                controlPanel.addArea(new Vector2(-w3, 0), new DimensionF(w3, controlPanel.getSize().getHeight()), null),
                controlPanel.addArea(new Vector2(0, 0), new DimensionF(w3, controlPanel.getSize().getHeight()), null),
                controlPanel.addArea(new Vector2(w3, 0), new DimensionF(w3, controlPanel.getSize().getHeight()), null),
                new TextTexture(backgroundColor));
    }

    @Override
    public void setBullets(int count) {
        if (basicInventory != null) {
            basicInventory.setBullets(count);
        }
    }

    @Override
    public void setDiamonds(int count) {
        if (basicInventory != null) {
            basicInventory.setDiamonds(count);
        }
    }
}
