package de.yard.threed.javacommon;

import de.yard.threed.core.platform.NativeElement;
import de.yard.threed.core.platform.NativeNodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Created by thomass on 11.09.15.
 */
public class JavaXmlElement extends JavaXmlNode implements NativeElement {
    JavaXmlElement(Node node){
        super(node);
    }

    @Override
    public String getAttribute(String name) {
        return ((Element)node).getAttribute(name);
    }


    @Override
    public NativeNodeList getElementsByTagName(String name) {
        return new JavaXmlNodeList(((Element)node).getElementsByTagName(name));
    }


    
}
