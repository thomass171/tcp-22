package de.yard.threed.javacommon;

import de.yard.threed.core.platform.NativeElement;
import de.yard.threed.core.platform.NativeNodeList;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Created by thomass on 11.09.15.
 */
public class JavaXmlElement extends JavaXmlNode implements NativeElement {
    JavaXmlElement(Node node) {
        super(node);
    }

    /**
     * ((Element) node).getAttribute(name) returns "" for non existing attributes
     */
    @Override
    public String getAttribute(String name) {
        Attr t = ((Element) node).getAttributeNode(name);
        if (t == null) {
            return null;
        }
        return t.getValue();
    }


    @Override
    public NativeNodeList getElementsByTagName(String name) {
        return new JavaXmlNodeList(((Element) node).getElementsByTagName(name));
    }


}
