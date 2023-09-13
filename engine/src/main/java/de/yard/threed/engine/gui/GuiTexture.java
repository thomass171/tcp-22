package de.yard.threed.engine.gui;

import de.yard.threed.engine.Texture;
import de.yard.threed.core.geometry.UvMap1;

/**
 * Created on 23.11.18.
 */
public interface GuiTexture {
    Texture getTexture();
    UvMap1 getUvMap();
}
