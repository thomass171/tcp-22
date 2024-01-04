package de.yard.threed.engine.util;

import de.yard.threed.core.Util;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
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
    public static String getStringAttribute(NativeNode node, String attrname, String defaultvalue) {
        NativeAttributeList attrs = node.getAttributes();
        NativeAttribute attr = attrs.getNamedItem(attrname);
        if (attr == null) {
            return defaultvalue;
        }
        return attr.getValue();
    }

    public static String getStringAttribute(NativeNode node, String attrname) {
        return getStringAttribute(node, attrname, null);
    }

    public static float getFloatAttribute(NativeNode node, String attrname, float defaultvalue) {
        String s = getStringAttribute(node, attrname, null);
        if (s == null || StringUtils.length(s) == 0) {
            return defaultvalue;
        }
        float f = Util.atof(s);
        return f;
    }

    public static int getIntAttribute(NativeNode node, String attrname, int defaultvalue) {
        String s = getStringAttribute(node, attrname, null);
        if (s == null || StringUtils.length(s) == 0) {
            return defaultvalue;
        }
        return Util.atoi(s);
    }

    public static boolean getBooleanAttribute(NativeNode node, String attrname, boolean defaultvalue) {
        String s = getStringAttribute(node, attrname, null);
        if (s == null || StringUtils.length(s) == 0) {
            return defaultvalue;
        }
        //return StringUtils.startsWith(s, "1") ? true : false;
        return Util.parseBoolean(s);
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

    public static List<NativeNode> getChildren(List<NativeNode> nodeList, String tag) {
        List<NativeNode> children = new ArrayList<NativeNode>();
        for (int i = 0; i < nodeList.size(); i++) {
            NativeNode item = nodeList.get(i);
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
        if (c.size() == 0) {
            return null;
        }
        return c.get(0).getTextValue();
    }

    public static XmlNode getChild(NativeNode nativeNode, String tag, int index) {
        List<NativeNode> c = XmlHelper.getChildren(nativeNode, tag);
        //return c.get(0).getNodeValue();
        if (c.size() <= index) {
            return null;
        }
        return new XmlNode(c.get(index));
    }

    /*public static String getTextValue(NativeNode nativeNode, String tag) {
        
        return c.get(0).getTextValue();
    }*/

    public static Integer getIntValue(NativeNode nativeNode) {
        String s = nativeNode.getTextValue();// XmlHelper.getChildValue(nativeNode, tag);
        if (s == null) {
            return null;
        }
        return Util.parseInt(s);
    }

    public static Float getFloatValue(NativeNode nativeNode) {
        String s = nativeNode.getTextValue();//XmlHelper.getChildValue(nativeNode, tag);
        if (s == null) {
            return null;
        }
        return Util.parseFloat(s);
    }

    public static Boolean getBooleanValue(NativeNode nativeNode) {
        String s = nativeNode.getTextValue();
        if (s == null) {
            return null;
        }
        return Util.parseBoolean(s);
    }

    public static String getStringValue(NativeNode nativeNode) {
        return nativeNode.getTextValue();
    }


    public static Vector2 getVector2Value(NativeNode nativeNode) {
        String[] p = StringUtils.split(nativeNode.getTextValue(), ",");
        if (p.length != 2) {
            return null;
        }
        return new Vector2(Util.parseFloat(p[0]), Util.parseFloat(p[1]));
    }

    public static Vector3 getVector3Value(NativeNode nativeNode) {
        String[] p = StringUtils.split(nativeNode.getTextValue(), ",");
        if (p.length != 3) {
            return null;
        }
        return new Vector3(Util.parseFloat(p[0]), Util.parseFloat(p[1]), Util.parseFloat(p[2]));
    }

    public static List<NativeNode> filter(List<NativeNode>  nodes, XmlFilter filter) {
        List<NativeNode> result = new ArrayList<NativeNode>();
        for (int i = 0; i < nodes.size(); i++) {
            NativeNode n = nodes.get(i);
            if (filter.matches(n)) {
                result.add(n);
            }
        }
        return result;
    }

}
