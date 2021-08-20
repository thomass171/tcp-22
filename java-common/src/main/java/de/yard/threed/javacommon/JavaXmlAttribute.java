package de.yard.threed.javacommon;


import de.yard.threed.core.platform.NativeAttribute;
import org.w3c.dom.Node;

/**
 * Created by thomass on 11.09.15.
 */
public class JavaXmlAttribute extends JavaXmlNode implements NativeAttribute {
    
    JavaXmlAttribute(Node attr){
        super(attr);
    }

    @Override
    public String getValue() {
        return node.getNodeValue();
    }

}
