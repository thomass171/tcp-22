package de.yard.threed.traffic.config;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.engine.util.XmlHelper;

/**
 * Ein Viewpoint in der Config.
 * 19.10.19: Jetzt auch für Pois, darum auch description.
 * Ein POI ist doch eigentlich ein Viewpoint, der nur etwas anders definiert wird und
 * eine Description hat. Viewpoiont als "Captain" am Vehicle ist zwar nicht über geocoord definierbar, aber trotzdem.
 * Tja, oder?
 * 2.12.21:Deprecaated, weil ich keine XML Modelklassen mehr verwenden moechte.
 */
@Deprecated
public class ViewpointConfig extends ConfigNode {
    public String name;
    //9.1.19: Ein Viewpoint sollte keine Smartlocation sein. Das macht z.Z. (noch?) keinen rechten Sinn.
    public LocalTransform transform;
    public String icao;

    public ViewpointConfig(NativeNode nativeNode) {
        super(nativeNode);
        name = XmlHelper.getStringAttribute(nativeNode, "name");
        icao = XmlHelper.getStringAttribute(nativeNode, "icao");
        transform = ConfigHelper.getTransform(nativeNode);
    }

}
