package de.yard.threed.engine.util;

import de.yard.threed.core.Util;
import de.yard.threed.core.platform.NativeAttribute;
import de.yard.threed.core.platform.NativeAttributeList;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.NativeNodeList;
import de.yard.threed.core.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomass on 04.04.17.
 */
public class XmlHelper {
    public static String getAttribute(NativeNode node, String attrname) {
        NativeAttributeList attrs = node.getAttributes();
        NativeAttribute attr = attrs.getNamedItem(attrname);
        if (attr == null) {
            return null;
        }
        return attr.getValue();
    }

    public static float getFloatAttribute(NativeNode node, String attrname, float defaultvalue) {
        String s = getAttribute(node, attrname);
        if (s == null || StringUtils.length(s) == 0) {
            return defaultvalue;
        }
        float f = Util.atof(s);
        return f;
    }

    public static int getIntAttribute(NativeNode node, String attrname, int defaultvalue) {
        String s = getAttribute(node, attrname);
        if (s == null || StringUtils.length(s) == 0) {
            return defaultvalue;
        }
        return Util.atoi(s);
    }

    public static boolean getBooleanAttribute(NativeNode node, String attrname, boolean defaultvalue) {
        String s = getAttribute(node, attrname);
        if (s == null || StringUtils.length(s) == 0) {
            return defaultvalue;
        }
        return StringUtils.startsWith(s, "1") ? true : false;
    }

    public static List<NativeNode> getChildren(NativeNode node, String tag) {
        NativeNodeList allchildren = node.getChildNodes();
        List<NativeNode> children = new ArrayList<NativeNode>();
        for (int i = 0; i < allchildren.getLength(); i++) {
            NativeNode item = allchildren.getItem(i);
            if (tag.equals(item.getNodeName())) {
                children.add(item);
            }
        }
        return children;
    }

    public static List<NativeNode> getChildNodeList(NativeNode node, String maintag, String subtag) {
        List<NativeNode> mainlist = XmlHelper.getChildren(node, maintag);
        if (mainlist.size() == 0) {
            return new ArrayList<NativeNode>();
        }
        return XmlHelper.getChildren(mainlist.get(0), subtag);

    }

    public static String getChildValue(NativeNode nativeNode, String tag) {
        List<NativeNode> c = getChildren(nativeNode, tag);
        //return c.get(0).getNodeValue();
        if (c.size()==0){
            return null;
        }
        return c.get(0).getTextValue();
    }

    /*public static String getTextValue(NativeNode nativeNode, String tag) {
        
        return c.get(0).getTextValue();
    }*/
}
