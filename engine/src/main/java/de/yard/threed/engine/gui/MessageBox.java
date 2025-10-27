package de.yard.threed.engine.gui;

import de.yard.threed.core.Color;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Scene;

/**
 * Extracted from InputToRequestSystem
 */
public class MessageBox {
    private Log logger = Platform.getInstance().getLog(MessageBox.class);
    ControlPanel cp;
    private long closeAt = 0;
    private Vector3 savedScale = null;
    private ControlPanelArea textArea;
    TextTexture textTexture = new TextTexture(Color.LIGHTGRAY);

    public MessageBox(Color baseColor) {
        cp = ControlPanelHelper.buildForNearplaneBanner(Scene.getCurrent().getDefaultCamera(), Scene.getCurrent().getDimension(), baseColor);
        if (cp != null) {
            // headless?
            textArea = ControlPanelHelper.addText(cp, " ", new Vector2(0, 0), cp.getSize());
            savedScale = cp.getTransform().getScale();
        }
        hide();
    }

    public void showMessage(String msg, long durationInMillis) {

        //ControlPanelArea textArea = cp.addArea(pos, size, null);
        if (textArea != null) {
            textArea.setTexture(textTexture.getTextureForText(msg, Color.RED));
            cp.getTransform().setScale(savedScale);
        }
        closeAt = Platform.getInstance().currentTimeMillis() + durationInMillis;

    }

    public void hideIfExpired() {
        if (closeAt != 0 && closeAt < Platform.getInstance().currentTimeMillis()) {
            logger.debug("closing");
            hide();
        }
    }

    public void hide() {
        //SceneNode remove() is unclear/undefined
        //cp.remove();
        if (cp != null) {
            cp.getTransform().setScale(new Vector3());
        }
        closeAt = 0;
    }
}
