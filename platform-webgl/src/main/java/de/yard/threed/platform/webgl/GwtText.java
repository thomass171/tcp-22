package de.yard.threed.platform.webgl;

import com.google.gwt.xml.client.Text;
import de.yard.threed.core.platform.NativeText;

/**
 * Created by thomass on 11.09.15.
 */
public class GwtText extends GwtNode implements NativeText {

    GwtText(Text text) {
        super(text);
    }

    @Override
    public String getData() {
        return  ((Text)node).getData();
    }
}
