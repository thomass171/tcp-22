package de.yard.threed.platform.webgl;

import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Ableiten von FocusPanel statt HTMLPanel, um MouseListener dranhaengn zu koennen.
 *
 * Created by thomass on 16.05.15.
 */
public class CanvasPanel extends FocusPanel {
    public CanvasPanel(String elementid) {
        //super("");
        getElement().setId(elementid);

    }
}
