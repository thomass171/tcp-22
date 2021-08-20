package de.yard.threed.javacommon;


import de.yard.threed.core.platform.NativeAttribute;
import de.yard.threed.core.platform.NativeAttributeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Created by thomass on 11.09.15.
 */
public class JavaXmlAttributeList implements NativeAttributeList {
    NamedNodeMap attrlist;

    JavaXmlAttributeList(NamedNodeMap attrlist){
        this.attrlist = attrlist;
    }

    @Override
    public NativeAttribute getItem(int i) {
        if (attrlist == null){
            return null;
        }
        return new JavaXmlAttribute/*Node*/(attrlist.item(i));
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
        return new JavaXmlAttribute/*Node*/(n);
    }
}
