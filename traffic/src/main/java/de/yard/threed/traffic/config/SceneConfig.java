package de.yard.threed.traffic.config;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.engine.util.XmlNode;

import java.util.ArrayList;
import java.util.List;

public class SceneConfig extends XmlNode {
    List<SceneVehicle> scenevehicles;

    public SceneConfig(NativeNode nativeNode) {
        super(nativeNode);
        scenevehicles = new ArrayList<SceneVehicle>();
        if (XmlHelper.getChildren(nativeNode, "vehicles").size() > 0) {
            List<NativeNode> list = XmlHelper.getChildren(XmlHelper.getChildren(nativeNode, "vehicles").get(0), "vehicle");
            for (NativeNode n : list) {
                scenevehicles.add(new SceneVehicle(n));
            }
        }
    }

    public List<ViewpointConfig> getViewpoints() {
        return getViewpoints(null);
    }

    public List<ViewpointConfig> getViewpoints(ConfigAttributeFilter configAttributeFilter) {
        List<NativeNode> xmlviewpoints = XmlHelper.getChildNodeList(nativeNode, "viewpoints", "viewpoint");
        List<ViewpointConfig> viewpoints = new ArrayList<ViewpointConfig>();
        for (int i = 0; i < xmlviewpoints.size(); i++) {
            NativeNode nvp = xmlviewpoints.get(i);
            ViewpointConfig vc = new ViewpointConfig(nvp);
            boolean usepoint = false;
            if (configAttributeFilter == null || vc.complies(configAttributeFilter)) {
                usepoint = true;
            }
            if (usepoint) {
                viewpoints.add(vc);
            }
        }
        return viewpoints;
    }

    public int getVehicleCount() {
        return scenevehicles.size();
    }

    public SceneVehicle getVehicle(int index) {
        return (scenevehicles.get(index));
    }

    public SceneVehicle getVehicleByName(String name) {
        for (SceneVehicle v : scenevehicles) {
            if (v.getName().equals(name)) {
                return v;
            }
        }
        return null;
    }

    public LocalTransform getBaseTransformForVehicleOnGraph() {
        List<NativeNode> t = XmlHelper.getChildNodeList(nativeNode, "BaseTransformForVehicleOnGraph", "transform");
        LocalTransform transform = ConfigHelper.getTransform(t);
        return transform;
    }
}
