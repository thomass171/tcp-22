package de.yard.threed.traffic.config;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.ParsingHelper;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.engine.ViewPoint;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.trafficcore.config.AirportDefinition;

import java.util.ArrayList;
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
                double[] d = ParsingHelper.getTriple(angle);
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

        double[] p = ParsingHelper.getTriple(s);
        if (p.length != 3) {
            return null;
        }
        return new Vector3(p[0], p[1], p[2]);
    }

    public static ViewPoint buildViewpoint(NativeNode nn) {
        LocalTransform transform = ConfigHelper.getTransform(nn);
        return new ViewPoint(XmlHelper.getStringAttribute(nn, "name"), transform);
    }

}
