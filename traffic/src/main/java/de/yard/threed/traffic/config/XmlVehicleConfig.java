package de.yard.threed.traffic.config;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.engine.util.XmlHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Konfiguration eines Vehicle. Wird im Moment aus XML gelesen.
 * 24.4.18: Soll wirklich nur ein Datencontainer ohne grosse Logik sein, weil es Attribut im VehicleComponent ist.
 *
 * 27.12.21: Teilweise zwar Aircraft lastig, soll trotzdem aber nach "traffic".
 */
public class XmlVehicleConfig extends ConfigNode/*XmlNode*/ implements VehicleConfig {
    public static double DEFAULTTURNRADIUS = 10;

    public static final String INITIALCOUNT = "initialcount";
    public static final String MAXIMUMSPEED = "maximumspeed";
    public static final String ACCELERATION = "acceleration";
    public static final String TYPE = "type";
    public static final String WINGSPREAD = "wingspread";
    public static final String WINGPASSINGPOINT = "wingpassingpoint";
    public static final String LEFTWINGAPPROACHPOINT = "leftwingapproachpoint";
    //public static final String CATERINGDURATION = "cateringduration";
    //public static final String FUELINGDURATION = "fuelingduration";
    public static final String DOOR = "door";
    public static final String LOCATION = "location";
    public static final String NAME = "name";
    public static final String DELAYEDLOAD = "delayedload";
    public static final String AUTOMOVE = "automove";
    public static final String APPROACHOFFSET = "approachoffset";
    public static final String UNSCHEDULEDMOVING = "unscheduledmoving";
    public static final String TURNRADIUS = "turnradius";

    public XmlVehicleConfig(NativeNode nativeNode) {
        super(nativeNode);
    }

    public String getBundlename() {
        return XmlHelper.getChildValue(nativeNode, "bundlename");
    }

    /*public String getName() {
        return XmlHelper.getAttribute(nativeNode, "name");
    }*/

    public String getModelfile() {
        return XmlHelper.getChildValue(nativeNode, "modelfile");
    }

    public String getAircraftdir() {
        return XmlHelper.getChildValue(nativeNode, "aircraftdir");
    }

    public String getType() {
        return XmlHelper.getStringAttribute(nativeNode, "type", null);
    }

    public String getModelType() {
        return XmlHelper.getStringAttribute(nativeNode, "modeltype", null);
    }

    public double getZoffset() {
        String s = XmlHelper.getChildValue(nativeNode, "zoffset");
        if (s == null) {
            return 0;
        }
        return (double) Util.parseDouble(s);
    }

    /*public Vector3 getPilotPosition() {
        return new Vector3(0.16f, -0.14f, 0.236f);
    }*/

    public Map<String, LocalTransform> getViewpoints() {
        List<NativeNode> xmlviewpoints = XmlHelper.getChildNodeList(nativeNode, "viewpoints", "viewpoint");
        Map<String, LocalTransform> viewpoints = new HashMap<String, LocalTransform>();
        for (int i = 0; i < xmlviewpoints.size(); i++) {
            NativeNode vp = xmlviewpoints.get(i);
            viewpoints.put(XmlHelper.getStringAttribute(vp, "name", null), ConfigHelper.getTransform(XmlHelper.getChildren(vp, "transform")));
        }
        return viewpoints;
    }

    public String[] getOptionals() {
        List<NativeNode> xmloptionals = XmlHelper.getChildNodeList(nativeNode, "optionals", "optional");
        String[] optionals = new String[xmloptionals.size()];
        for (int i = 0; i < optionals.length; i++) {
            optionals[i] = xmloptionals.get(i).getTextValue();
        }
        return optionals;
    }


    public double getMaximumSpeed() {
        if (XmlHelper.getChild(nativeNode,XmlVehicleConfig.MAXIMUMSPEED, 0) == null) {
            return 0;
        }
        return (double) XmlHelper.getFloatValue(XmlHelper.getChild(nativeNode,XmlVehicleConfig.MAXIMUMSPEED, 0).nativeNode);
    }

    public double getAcceleration() {
        if (XmlHelper.getChild(nativeNode,XmlVehicleConfig.ACCELERATION, 0) == null) {
            return 0;
        }
        return (double) XmlHelper.getFloatValue(XmlHelper.getChild(nativeNode,XmlVehicleConfig.ACCELERATION, 0).nativeNode);
    }

    public double getApproachoffset() {
        return (double) XmlHelper.getFloatValue(XmlHelper.getChild(nativeNode,XmlVehicleConfig.APPROACHOFFSET, 0).nativeNode);
    }

    /**
     * Returns number of vehicles to create initially
     *
     * @return
     */
    public int getInitialCount() {
        if (XmlHelper.getChild(nativeNode,XmlVehicleConfig.INITIALCOUNT, 0) == null) {
            return 0;
        }
        return (int) XmlHelper.getIntValue(XmlHelper.getChild(nativeNode,XmlVehicleConfig.INITIALCOUNT, 0).nativeNode);
    }

    /**
     * Hier ist der Default rue
     *
     * @return
     */
    public boolean getUnscheduledmoving() {
        if (XmlHelper.getChild(nativeNode,XmlVehicleConfig.UNSCHEDULEDMOVING, 0) == null) {
            return true;
        }
        return XmlHelper.getIntValue(XmlHelper.getChild(nativeNode,XmlVehicleConfig.UNSCHEDULEDMOVING, 0).nativeNode) != 0;
    }

    public double getTurnRadius() {
        if (XmlHelper.getChild(nativeNode,XmlVehicleConfig.TURNRADIUS, 0) == null) {
            return DEFAULTTURNRADIUS;
        }
        return (double) XmlHelper.getFloatValue(XmlHelper.getChild(nativeNode,XmlVehicleConfig.TURNRADIUS, 0).nativeNode);
    }

    public String getLowresFile() {
        return getChildValue("lowresfile");
    }
}
