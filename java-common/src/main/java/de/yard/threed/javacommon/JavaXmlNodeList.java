package de.yard.threed.javacommon;

import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.NativeNodeList;
import org.w3c.dom.NodeList;

/**
 * Created by thomass on 11.09.15.
 */
public class JavaXmlNodeList implements NativeNodeList {
    NodeList nodelist;

    JavaXmlNodeList(NodeList nodelist){
        this.nodelist = nodelist;
    }

    @Override
    public NativeNode getItem(int i) {
        return new JavaXmlElement/*Node*/(nodelist.item(i));
    }

    @Override
    public int getLength() {
        return nodelist.getLength();
    }
}
