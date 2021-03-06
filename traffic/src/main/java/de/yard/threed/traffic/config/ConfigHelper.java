package de.yard.threed.traffic.config;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.engine.util.XmlHelper;

import java.util.List;

/**
 * See also XmlHelper.
 */
public class ConfigHelper {

    /**
     * Only the getFirst element isType used. Other are ignored. Returns default if not node isType found. Never returns null.
     */
    public static LocalTransform getTransform(List<NativeNode> transforms) {
        Quaternion rotation = new Quaternion();
        Vector3 position = new Vector3();
        Vector3 scale = new Vector3(1, 1, 1);
        if (transforms.size() > 0) {
            NativeNode node = transforms.get(0);
            String s_position = XmlHelper.getChildValue(node, "position");
            if (!StringUtils.empty(s_position)) {
                position = getVector3(s_position);
            }
            String angle = XmlHelper.getChildValue(node, "angle");
            if (!StringUtils.empty(angle)) {
                double[] d = getTriple(angle);
                rotation = Quaternion.buildFromAngles(new Degree(d[0]), new Degree(d[1]), new Degree(d[2]));
            }
            String s_scale = XmlHelper.getChildValue(node, "scale");
            if (!StringUtils.empty(s_scale)) {
                scale = getVector3(s_scale);
            }
        }
        return new LocalTransform(position, rotation, scale);
    }

    public static LocalTransform getTransform(NativeNode node) {
        return getTransform(XmlHelper.getChildren(node, "transform"));
    }

    /**
     *
     */
    public static Vector3 getVector3(String s) {

        double[] p = getTriple(s);
        if (p.length != 3) {
            return null;
        }
        return new Vector3(p[0], p[1], p[2]);
    }

    public static double[] getTriple(String s) {
        String[] p = StringUtils.split(s, ",");
        double[] d = new double[p.length];
        for (int i = 0; i < p.length; i++) {
            d[i] = Util.parseDouble(p[i]);
        }
        return d;
    }

    /**
     * From global vehicle list.
     *
     * @return
     */
    public static VehicleConfig getVehicleConfig(NativeDocument tw, String name) {
        List<NativeNode>  vehicles = XmlHelper.getChildNodeList(tw, "vehicles", "vehicle");
        for (int i = 0; i < vehicles.size(); i++) {
            if (name.equals(XmlHelper.getStringAttribute(vehicles.get(i), "name", null))) {
                return new XmlVehicleConfig(vehicles.get(i));
            }
        }
        return null;
    }

    public static VehicleConfig getVehicleConfig(NativeDocument tw, int index) {
        List<NativeNode>  vehicles = XmlHelper.getChildNodeList(tw, "vehicles", "vehicle");

        return new XmlVehicleConfig(vehicles.get(index));
    }

    public static int getVehicleCount(NativeDocument tw) {
        List<NativeNode>  vehicles = XmlHelper.getChildNodeList(tw, "vehicles", "vehicle");
        return vehicles.size();
    }

}
