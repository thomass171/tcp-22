package de.yard.threed.traffic.config;

import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.engine.util.XmlNode;
import de.yard.threed.trafficcore.config.LocatedVehicle;
import de.yard.threed.trafficcore.model.SmartLocation;

/**
 * Created on 06.03.18.
 * 20.11.23: Splitted to interface and implementation. Should be renamed to XmlLocatedVehicle once.
 */
public class SceneVehicle extends ConfigNode implements LocatedVehicle {
    public SceneVehicle(NativeNode n) {
        super(n);
    }

    @Override
    public SmartLocation getLocation() {
        XmlNode c = XmlHelper.getChild(nativeNode, XmlVehicleDefinition.LOCATION, 0);
        if (c == null) {
            return null;
        }
        String s = XmlHelper.getStringValue(c.nativeNode);
        if (s == null) {
            return null;
        }
        return SmartLocation.fromString(s);
    }

    /*super class 20.3.19 public String getName() {
        return XmlHelper.getAttribute(nativeNode, TrafficWorldConfig.NAME);
    }*/

}
