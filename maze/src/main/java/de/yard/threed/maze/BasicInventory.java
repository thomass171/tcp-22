package de.yard.threed.maze;

import de.yard.threed.core.Color;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.gui.ControlPanelArea;
import de.yard.threed.engine.gui.TextTexture;

public  class BasicInventory implements MazeInventory {

    static Log logger = Platform.getInstance().getLog(BasicInventory.class);

    private ControlPanelArea bulletCountArea;
    private ControlPanelArea diamondCountArea;
    public  ControlPanelArea boxesCountArea;

    // cache bullet value to avoid texture inflation
    private int bullets = -1;
    private int diamonds = -1;
    private int boxes = -1;
    public TextTexture textTexture;

    public BasicInventory(ControlPanelArea bulletCountArea,ControlPanelArea diamondCountArea,ControlPanelArea boxCountArea,  TextTexture textTexture) {
        this.bulletCountArea=bulletCountArea;
        this.diamondCountArea=diamondCountArea;
        this.boxesCountArea=boxCountArea;
        this.textTexture=textTexture;
    }

    public void setBoxesCount(int count) {
        if (boxes == count) {
            // no change
            return;
        }
        if (boxesCountArea == null) {
            // headless?
            return;
        }
        if (count == 0) {
            boxesCountArea.setTexture(textTexture.getTextureForText("-", Color.RED));
        } else {
            boxesCountArea.setTexture(textTexture.getTextureForText("" + count, Color.RED));
        }
        boxes = count;
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
