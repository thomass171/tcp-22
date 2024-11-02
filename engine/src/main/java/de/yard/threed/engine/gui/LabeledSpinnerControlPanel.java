package de.yard.threed.engine.gui;

import de.yard.threed.core.Color;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.SpinnerHandler;
import de.yard.threed.core.Vector2;
import de.yard.threed.engine.Material;

/**
 * label-spinnercontrol
 */
public class LabeledSpinnerControlPanel extends ControlPanel {

    ControlPanelArea labelArea;
    TextTexture textTexture;
    Color textColor;

    public LabeledSpinnerControlPanel(String label, DimensionF size, double margin, Material mat, SpinnerHandler handler, Color textColor) {
        super(new DimensionF(size.width, size.height), mat, 0.01);
        this.textColor = textColor;

        textTexture = new TextTexture(Color.LIGHTGRAY);

        labelArea = addArea(new Vector2(-(size.width / 4), 0), new DimensionF(size.width / 2, size.height), null);
        // empty string fails due to length 0
        labelArea.setTexture(textTexture.getTextureForText(" " + label, textColor));

        SpinnerControlPanel cp = new SpinnerControlPanel(new DimensionF(size.width / 2.0, size.height), margin, mat, handler, textColor);
        add(new Vector2((size.width / 4), 0), cp);
    }
}

