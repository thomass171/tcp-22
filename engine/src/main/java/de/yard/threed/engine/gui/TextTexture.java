package de.yard.threed.engine.gui;

import de.yard.threed.engine.Texture;
import de.yard.threed.core.Color;

/**
 * TODO pool/cache
 */
public class TextTexture {

    Color background;
    boolean pooled = true;

    public TextTexture(Color background) {
        this.background = background;
    }

    public Texture getTextureForText(String text, Color color) {
        Texture texture = Hud.buildTextureForText(text, color, background);
        return texture;
    }
}
