package de.yard.threed.traffic.config;

import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.trafficcore.config.AirportDefinition;
import de.yard.threed.trafficcore.config.LocatedVehicle;
import de.yard.threed.trafficcore.model.Airport;
import de.yard.threed.trafficcore.model.SmartLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * 15.3.24: No longer extend airport for better separation (maybe could be a composition). See {@link Airport} and {@link AirportDefinition}
 */
public class XmlAirportDefinition /*extends Airport*/ implements AirportDefinition {

    public static final String HOME = "home";

    NativeNode nativeNode;

    public XmlAirportDefinition(NativeNode node) {
        //TODO center
        //super(XmlHelper.getStringAttribute(node, "icao"), -200, -200);
        this.nativeNode = node;
    }

    @Override
    public List<LocatedVehicle> getVehicles() {
        List<NativeNode> raw = XmlHelper.getChildren(nativeNode, "vehicle");
        List<LocatedVehicle> result = new ArrayList<LocatedVehicle>();
        for (int i = 0; i < raw.size(); i++) {
            // Should be XmlLocatedVehicle once.
           result.add(new SceneVehicle(raw.get(i)));
        }
        return result;
    }

    @Override
    public String getHome() {
        return XmlHelper.getChildValue(nativeNode, HOME);
    }

    @Override
    public List<SmartLocation> getLocations() {
        List<NativeNode> raw = XmlHelper.getChildren(nativeNode, "location");
        List<SmartLocation> result = new ArrayList<SmartLocation>();
        for (int i = 0; i < raw.size(); i++) {
            result.add(new SmartLocation(XmlHelper.getStringValue(raw.get(i))));
        }
        return result;
    }
}
