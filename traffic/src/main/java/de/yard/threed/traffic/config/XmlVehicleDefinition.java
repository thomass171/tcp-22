package de.yard.threed.traffic.config;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.engine.util.XmlHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Konfiguration eines Vehicle. Wird im Moment aus XML gelesen.
 * 24.4.18: Soll wirklich nur ein Datencontainer ohne grosse Logik sein, weil es Attribut im VehicleComponent ist.
 *
 * 27.12.21: Teilweise zwar Aircraft lastig, soll trotzdem aber nach "traffic".
 * renamed from XmlVehicleConfig to XmlVehicleDefinition to better meet naimg convetions.
 *
 * 27.11.23: Setting default values here for unconfigured values seems not the best location.
 */
public class XmlVehicleDefinition extends ConfigNode/*XmlNode*/ implements VehicleDefinition {
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

    public XmlVehicleDefinition(NativeNode nativeNode) {
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
        List<NativeNode> xmlviewpoints = XmlHelper.getChildren(nativeNode, "viewpoint");
        Map<String, LocalTransform> viewpoints = new HashMap<String, LocalTransform>();
        for (int i = 0; i < xmlviewpoints.size(); i++) {
            NativeNode vp = xmlviewpoints.get(i);
            viewpoints.put(XmlHelper.getStringAttribute(vp, "name", null), ConfigHelper.getTransform(XmlHelper.getChildren(vp, "transform")));
        }
        return viewpoints;
    }

    public String[] getOptionals() {
        List<NativeNode> xmloptionals = XmlHelper.getChildren(nativeNode, "optional");
        String[] optionals = new String[xmloptionals.size()];
        for (int i = 0; i < optionals.length; i++) {
            optionals[i] = xmloptionals.get(i).getTextValue();
        }
        return optionals;
    }


    public double getMaximumSpeed() {
        if (XmlHelper.getChild(nativeNode, XmlVehicleDefinition.MAXIMUMSPEED, 0) == null) {
            return 0;
        }
        return (double) XmlHelper.getFloatValue(XmlHelper.getChild(nativeNode, XmlVehicleDefinition.MAXIMUMSPEED, 0).nativeNode);
    }

    public double getAcceleration() {
        if (XmlHelper.getChild(nativeNode, XmlVehicleDefinition.ACCELERATION, 0) == null) {
            return 0;
        }
        return (double) XmlHelper.getFloatValue(XmlHelper.getChild(nativeNode, XmlVehicleDefinition.ACCELERATION, 0).nativeNode);
    }

    public double getApproachoffset() {
        return (double) XmlHelper.getFloatValue(XmlHelper.getChild(nativeNode, XmlVehicleDefinition.APPROACHOFFSET, 0).nativeNode);
    }

    /**
     * Returns number of vehicles to create initially
     *
     * @return
     */
    public int getInitialCount() {
        if (XmlHelper.getChild(nativeNode, XmlVehicleDefinition.INITIALCOUNT, 0) == null) {
            return 0;
        }
        return (int) XmlHelper.getIntValue(XmlHelper.getChild(nativeNode, XmlVehicleDefinition.INITIALCOUNT, 0).nativeNode);
    }

    /**
     * Here default is true!
     *
     * @return
     */
    public boolean getUnscheduledmoving() {
        if (XmlHelper.getChild(nativeNode, XmlVehicleDefinition.UNSCHEDULEDMOVING, 0) == null) {
            return true;
        }
        return XmlHelper.getBooleanValue(XmlHelper.getChild(nativeNode, XmlVehicleDefinition.UNSCHEDULEDMOVING, 0).nativeNode).booleanValue();
    }

    public double getTurnRadius() {
        if (XmlHelper.getChild(nativeNode, XmlVehicleDefinition.TURNRADIUS, 0) == null) {
            return DEFAULTTURNRADIUS;
        }
        return (double) XmlHelper.getFloatValue(XmlHelper.getChild(nativeNode, XmlVehicleDefinition.TURNRADIUS, 0).nativeNode);
    }

    public String getLowresFile() {
        return getChildValue("lowresfile");
    }

    // 20.11.23: five properties merged from GroundServiceAircraftConfig
    @Override
    public Vector3 getCateringDoorPosition() {
        if (XmlHelper.getChild(nativeNode, DOOR, 0) == null) {
            return null;
        }

        Vector3 p ;//= doors.get(0);
        p = XmlHelper.getVector3Value(XmlHelper.getChild(nativeNode, DOOR,0).nativeNode);
        //hier die ac Transformation? Wie auch immer. Das Minus ist fuer rechte Tür.
        //return new Vector3(p.getX(),p.getZ(),-p.getY());
        return new Vector3(p.getX(), -p.getY(), p.getZ());
    }

    @Override
    public Vector3 getWingPassingPoint() {
        if (XmlHelper.getChild(nativeNode, WINGPASSINGPOINT, 0) == null) {
            return null;
        }

        //ein Stueck nach vorne rechts
        //float yoffset = 8;
        //float xoffset = 3;
        //Vector3 p = new Vector3(-xoffset, wingspread / 2 + yoffset, 0);
        //return Vector3.buildFromVector2(wingpassingpoint);
        return Vector3.buildFromVector2(XmlHelper.getVector2Value(XmlHelper.getChild(nativeNode,WINGPASSINGPOINT,0).nativeNode));
    }

    /**
     * Auch erstmal so aus der Lameng.
     * @return
     */
    @Override
    public Vector3 getLeftWingApproachPoint() {
        if (XmlHelper.getChild(nativeNode, LEFTWINGAPPROACHPOINT, 0) == null) {
            return null;
        }

        //float yoffset = 8;
        //float xoffset = 13;
        //Vector3 p = new Vector3(xoffset, -wingspread / 4 , 0);
        //return Vector3.buildFromVector2(leftwingapproachpoint);
        return Vector3.buildFromVector2(XmlHelper.getVector2Value(XmlHelper.getChild(nativeNode,LEFTWINGAPPROACHPOINT,0).nativeNode));
    }

    /**
     * point appx. 5 meter behind the aircraft
     * Auch erstmal so aus der Lameng.
     *
     * TODO make it a config property.
     * @return
     */
    public Vector3 getRearPoint() {

        double xoffset = 28;
        Vector3 p = new Vector3(xoffset, 0 , 0);
        return p;
    }

    @Override
    public LocalTransform getTransform() {
        return  ConfigHelper.getTransform(XmlHelper.getChildren(nativeNode, "transform"));
    }

    @Override
    public Float getWingspread() {
        if (XmlHelper.getChild(nativeNode, WINGSPREAD, 0) == null) {
            return null;
        }
        return XmlHelper.getFloatValue(XmlHelper.getChild(nativeNode,WINGSPREAD,0).nativeNode);
    }

    public static List<VehicleDefinition> convertVehicleDefinitions(List<NativeNode> vehicleDefinitions) {
        List<VehicleDefinition> result = new ArrayList<VehicleDefinition>();
        for (int i = 0; i < vehicleDefinitions.size(); i++) {
            result.add(new XmlVehicleDefinition(vehicleDefinitions.get(i)));
        }
        return result;
    }
}
