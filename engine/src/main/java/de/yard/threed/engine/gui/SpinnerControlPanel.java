package de.yard.threed.engine.gui;

import de.yard.threed.core.Color;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.SpinnerHandler;
import de.yard.threed.core.Vector2;
import de.yard.threed.engine.Material;

/**
 * Icon-value-icon
 */
public class SpinnerControlPanel extends ControlPanel {

    ControlPanelArea textArea;
    TextTexture textTexture;
    SpinnerHandler handler;
    Color textColor;

    public SpinnerControlPanel(DimensionF size, double margin, Material mat, SpinnerHandler handler, Color textColor) {
        super(new DimensionF(size.width, size.height), mat, 0.01);
        this.handler = handler;
        this.textColor = textColor;

        double m2 = 2 * margin;
        double h = size.height;
        double iconsize = size.height - 2 * margin;
        double iconareasize = size.height;
        double textareawidth = size.width - 2 * iconareasize;

        addArea(new Vector2(-(textareawidth / 2 + iconareasize / 2), 0), new DimensionF(iconsize, iconsize), () -> {
            logger.debug("left arrow clicked");
            if (handler != null) {
                handler.down();
            }
            refresh();
        }).setIcon(Icon.ICON_LEFTARROW);
        // text has no margin yet.
        textTexture = new TextTexture(Color.LIGHTGRAY);
        textArea = addArea(new Vector2(0, 0), new DimensionF(textareawidth, size.height), null);
        // empty string fails due to length 0
        textArea.setTexture(textTexture.getTextureForText(" "/*""961"*/, textColor));
        refresh();

        addArea(new Vector2((textareawidth / 2 + iconareasize / 2), 0), new DimensionF(iconsize, iconsize), () -> {
            logger.debug("right arrow clicked");
            if (handler != null) {
                handler.up();
            }
            refresh();
        }).setIcon(Icon.ICON_RIGHTARROW);
    }

    private void refresh() {
        if (handler != null) {
            String value = handler.getValue();
            textArea.setTexture(textTexture.getTextureForText(value, textColor));
        }
    }

}

