package de.yard.threed.platform.webgl;

import com.google.gwt.xml.client.Node;
import de.yard.threed.core.platform.NativeAttribute;
import de.yard.threed.core.platform.NativeAttributeList;
import com.google.gwt.xml.client.NamedNodeMap;


/**
 * Created by thomass on 11.09.15.
 */
public class GwtAttributeList implements NativeAttributeList {
    NamedNodeMap attrlist;

    GwtAttributeList(NamedNodeMap attrlist){
        this.attrlist = attrlist;
    }

    @Override
    public NativeAttribute getItem(int i) {
        if (attrlist == null){
            return null;
        }
        return new GwtAttribute(attrlist.item(i));
    }

    @Override
    public int getLength() {
        if (attrlist == null){
            return 0;
        }return attrlist.getLength();
    }

    @Override
    public NativeAttribute getNamedItem(String name) {
        if (attrlist == null){
            return null;
        }
        Node n = attrlist.getNamedItem(name);
        if (n == null){
            return null;
        }
        return new GwtAttribute(n);
    }
}
