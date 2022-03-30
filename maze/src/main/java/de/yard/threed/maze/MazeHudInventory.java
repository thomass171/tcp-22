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

    private ControlPanelArea bulletCountArea;
    private ControlPanelArea diamondCountArea;
    ControlPanel controlPanel;
    // cache bullet value to avoid texture inflation
    private int bullets = -1;
    private int diamonds = -1;
    private Color backgroundColor = MazeSettings.hudColor;
    private TextTexture textTexture;

    public MazeHudInventory(Camera deferredcamera, Dimension dimension) {
        controlPanel = ControlPanelHelper.buildInventoryForDeferredCamera(deferredcamera, dimension, backgroundColor, new Dimension(90, 20));
        if (controlPanel == null) {
            // headless?
            return;
        }
        double w3 = controlPanel.getSize().getWidth() / 3;
        // occupy mid third of inventory for now
        bulletCountArea = controlPanel.addArea(new Vector2(0, 0), new DimensionF(w3, controlPanel.getSize().getHeight()), null);
        diamondCountArea = controlPanel.addArea(new Vector2(-w3, 0), new DimensionF(w3, controlPanel.getSize().getHeight()), null);
        textTexture = new TextTexture(backgroundColor);
    }

    @Override
    public void setBullets(int count) {
        if (bullets == count) {
            // no change
            return;
        }
        if (bulletCountArea == null) {
            // headless?
            return;
        }
        if (count == 0) {
            bulletCountArea.setTexture(textTexture.getTextureForText("-", MazeSettings.bulletColor));
        } else {
            bulletCountArea.setTexture(textTexture.getTextureForText("" + count, MazeSettings.bulletColor));
        }
        bullets = count;
    }

    @Override
    public void setDiamonds(int count) {
        if (diamonds == count) {
            // no change
            return;
        }
        if (diamondCountArea == null) {
            // headless?
            return;
        }
        if (count == 0) {
            // don't use diamond color in inventory because its too light.
            diamondCountArea.setTexture(textTexture.getTextureForText("-", Color.LIGHTBLUE));
        } else {
            diamondCountArea.setTexture(textTexture.getTextureForText("" + count, Color.LIGHTBLUE));
        }
        diamonds = count;
    }
}
