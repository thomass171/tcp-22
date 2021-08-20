package de.yard.threed.javacommon;


import de.yard.threed.core.platform.NativeText;
import org.w3c.dom.Text;

/**
 * Created by thomass on 11.09.15.
 */
public class JavaXmlText extends JavaXmlNode implements NativeText {

    JavaXmlText(Text text) {
        super(text);
    }

    @Override
    public String getData() {
        return ((Text)node).getData();
    }
}
