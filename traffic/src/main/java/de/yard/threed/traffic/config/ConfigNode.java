package de.yard.threed.traffic.config;

import de.yard.threed.core.Util;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.engine.util.XmlNode;

/**
 * Versuch eines generischen Ansatzes. Eine XML node die ein Attribut "name" hat.
 * Über weitere Attribute kann gefiltert werden.
 * <p>
 * Created on 09.01.19.
 */
public class ConfigNode extends XmlNode {
    public ConfigNode(NativeNode nativeNode) {
        super(nativeNode);
    }

    public boolean complies(ConfigAttributeFilter configAttributeFilter) {
        String value = XmlHelper.getStringAttribute(nativeNode, configAttributeFilter.attribute, null);
        if (configAttributeFilter.missingattributecomplies) {
            if (value == null) {
                return true;
            }
        }
        if ((configAttributeFilter.value == null) != (value == null)) {
            return false;
        }
        if (configAttributeFilter.value.equals(value)) {
            return true;
        }
        return false;
    }

    public String getName() {
        return XmlHelper.getStringAttribute(nativeNode, "name","");
    }


    public String getChildValue(String tag) {
        return XmlHelper.getChildValue(nativeNode, tag);
    }
}
