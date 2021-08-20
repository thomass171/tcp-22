package de.yard.threed.platform.webgl;

import com.google.gwt.xml.client.NodeList;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.NativeNodeList;

/**
 * Created by thomass on 11.09.15.
 */
public class GwtNodeList implements NativeNodeList {
    NodeList nodelist;

    GwtNodeList(NodeList nodelist){
        this.nodelist = nodelist;
    }

    @Override
    public NativeNode getItem(int i) {
        return new GwtElement(nodelist.item(i));
    }

    @Override
    public int getLength() {
        return nodelist.getLength();
    }
}
