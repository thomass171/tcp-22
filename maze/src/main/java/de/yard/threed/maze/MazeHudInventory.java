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
 * Unten rechts ausserhalb VR.
 */
public class MazeHudInventory implements MazeInventory {

    private ControlPanelArea bulletCountArea;
    ControlPanel controlPanel;
    // cache bullet value to avoid texture inflation
    private int bullets = -1;
    private Color basecolor = MazeSettings.hudColor;
    private TextTexture textTexture;

    public MazeHudInventory(Camera deferredcamera, Dimension dimension) {
        controlPanel = ControlPanelHelper.buildInventoryForDeferredCamera(deferredcamera, dimension, basecolor);

        // occupy mid third of inventory for now
        bulletCountArea = controlPanel.addArea(new Vector2(0, 0), new DimensionF(controlPanel.getSize().getWidth() / 3, controlPanel.getSize().getHeight()), null);
        textTexture = new TextTexture(basecolor);
    }

    @Override
    public void setBullets(int count) {
        if (bullets == count) {
            // no change
            return;
        }
        if (count == 0) {
            bulletCountArea.setTexture(textTexture.getTextureForText("-"));
        } else {
            bulletCountArea.setTexture(textTexture.getTextureForText("" + count));
        }
        bullets = count;
    }
}
