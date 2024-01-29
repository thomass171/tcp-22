package de.yard.threed.engine.gui;

import de.yard.threed.core.Color;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.Vector2;
import de.yard.threed.engine.Material;

/**
 * label-(read only)value
 */
public class PropertyPanel extends ControlPanel {

    ControlPanelArea valueArea;
    TextTexture textTexture;

    public PropertyPanel(DimensionF size, double margin, Material mat, String label) {
        super(new DimensionF(size.width, size.height), mat, 0.01);

        double textareawidth = size.width / 2.0;

        // text has no margin yet.
        textTexture = new TextTexture(Color.LIGHTGRAY);
        ControlPanelArea labelArea = addArea(new Vector2(-textareawidth/2.0, 0), new DimensionF(textareawidth, size.height), null);
        // empty string fails due to length 0
        labelArea.setTexture(textTexture.getTextureForText(label, Color.RED));

        textTexture = new TextTexture(Color.LIGHTGRAY);
        valueArea = addArea(new Vector2(textareawidth/2.0, 0), new DimensionF(textareawidth, size.height), null);
        // empty string fails due to length 0
        valueArea.setTexture(textTexture.getTextureForText(" ", Color.RED));
    }

    public void setValue(String value) {
        valueArea.setTexture(textTexture.getTextureForText(value, Color.RED));
    }
}

