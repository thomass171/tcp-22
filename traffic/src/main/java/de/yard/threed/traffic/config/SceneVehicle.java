package de.yard.threed.traffic.config;

import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.engine.util.XmlNode;
import de.yard.threed.trafficcore.model.SmartLocation;

/**
 * Created on 06.03.18.
 */
public class SceneVehicle extends ConfigNode {
    public SceneVehicle(NativeNode n) {
        super(n);
    }

    public SmartLocation getLocation() {
        XmlNode c = XmlHelper.getChild(nativeNode,XmlVehicleConfig.LOCATION, 0);
        if (c == null) {
            return null;
        }
        String s = XmlHelper.getStringValue(c.nativeNode);
        if (s == null) {
            return null;
        }
        return new SmartLocation(s);
    }

    /*super class 20.3.19 public String getName() {
        return XmlHelper.getAttribute(nativeNode, TrafficWorldConfig.NAME);
    }*/

}
