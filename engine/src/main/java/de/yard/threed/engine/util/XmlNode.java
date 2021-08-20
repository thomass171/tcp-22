package de.yard.threed.engine.util;

import de.yard.threed.core.Util;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.StringUtils;

import java.util.List;

/**
 * Utility class for XML Handling. To be extended.
 * <p>
 * Created on 04.03.18.
 */
public class XmlNode {
    protected NativeNode nativeNode;

    public XmlNode(NativeNode nativeNode) {
        this.nativeNode = nativeNode;
    }

    public Integer getIntValue() {
        String s = nativeNode.getTextValue();// XmlHelper.getChildValue(nativeNode, tag);
        if (s == null) {
            return null;
        }
        return Util.parseInt(s);
    }

    public Float getFloatValue() {
        String s = nativeNode.getTextValue();//XmlHelper.getChildValue(nativeNode, tag);
        if (s == null) {
            return null;
        }
        return Util.parseFloat(s);
    }

    /**
     * @param tag
     * @return
     */
    public XmlNode getChild(String tag, int index) {
        List<NativeNode> c = XmlHelper.getChildren(nativeNode, tag);
        //return c.get(0).getNodeValue();
        if (c.size() <= index) {
            return null;
        }
        return new XmlNode(c.get(index));
    }

    public Vector2 getVector2Value() {
        String[] p = StringUtils.split(nativeNode.getTextValue(), ",");
        if (p.length != 2) {
            return null;
        }
        return new Vector2(Util.parseFloat(p[0]), Util.parseFloat(p[1]));
    }

    public Vector3 getVector3Value() {
        String[] p = StringUtils.split(nativeNode.getTextValue(), ",");
        if (p.length != 3) {
            return null;
        }
        return new Vector3(Util.parseFloat(p[0]), Util.parseFloat(p[1]), Util.parseFloat(p[2]));
    }

    public String getStringValue() {
        return nativeNode.getTextValue();
    }
}
