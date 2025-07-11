package de.yard.threed.traffic.config;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.ParsingHelper;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.trafficcore.EllipsoidCalculations;
import de.yard.threed.traffic.flight.FlightLocation;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.core.platform.NativeNode;


/**
 * 19.10.19: Ein POI ist doch eigentlich ein Viewpoint, der nur etwas anders definiert wird und
 * eine Description hat. Naja, andererseits ist er immer global und nicht relativ zu  Vehikel.
 */
public class PoiConfig extends ConfigNode {
    public Degree longitude, latitude, heading, pitch;
    public double elevation;

    public PoiConfig(NativeNode nativeNode) {
        super(nativeNode);
        double[] d = ParsingHelper.getTriple(nativeNode.getTextValue());
        longitude = new Degree(d[0]);
        latitude = new Degree(d[1]);
        elevation = d[2];
        heading = new Degree(d[3]);
        pitch = new Degree(d[4]);
    }

    public GeoCoordinate getGeod() {
        return new GeoCoordinate(latitude, longitude, elevation);
    }

    public String getSphere() {
        return XmlHelper.getStringAttribute(nativeNode,"sphere", null);
    }

    public LocalTransform getTransform(EllipsoidCalculations rbcp) {
        LocalTransform posrot = new FlightLocation(getGeod(), heading, pitch).toPosRot(rbcp);
        return posrot;
    }
}
