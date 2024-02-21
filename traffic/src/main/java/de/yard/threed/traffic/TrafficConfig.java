package de.yard.threed.traffic;

import de.yard.threed.core.CharsetException;
import de.yard.threed.core.Color;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Util;
import de.yard.threed.core.XmlException;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.NativeNodeList;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.config.XmlAirportDefinition;
import de.yard.threed.trafficcore.config.AirportDefinition;
import de.yard.threed.traffic.config.ConfigAttributeFilter;
import de.yard.threed.traffic.config.ConfigHelper;
import de.yard.threed.traffic.config.ConfigNodeList;
import de.yard.threed.traffic.config.PoiConfig;
import de.yard.threed.traffic.config.SceneConfig;
import de.yard.threed.traffic.config.XmlVehicleDefinition;
import de.yard.threed.trafficcore.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * Successor of TrafficWorldConfig but generic (without model classees) for XSD based traffic config.
 * Could also be considered a SphereConfig.
 */
public class TrafficConfig {

    static Log logger = Platform.getInstance().getLog(TrafficConfig.class);
    List<NativeNode> /*27.12.21vehicles,*/ airports, scenes/*, pois*/;
    //public NativeDocument tw;
    private List<NativeNode> topNodes;

    private TrafficConfig(List<NativeNode> topNodes/*NativeDocument tw*/) {
        this.topNodes = topNodes;
    }

    public static TrafficConfig buildFromBundle(Bundle bundle, BundleResource configfile) {

        if (bundle == null) {
            logger.error("bundle is null");
            return null;
        }
        //BundleData data = bundle.getResource(configfile);
        List<NativeNode> data = readFromBundle(bundle, configfile.getPath(), configfile.getName());
        if (data == null) {
            return null;
        }
        return new TrafficConfig(data);
    }

    private static List<NativeNode> readFromBundle(Bundle bnd, ResourcePath currentPath, String configfile) {
        String pathPrefix = "";
        if (!currentPath.getPath().equals("")) {
            pathPrefix = currentPath.getPath() + "/";
        }
        BundleData xml = bnd.getResource(pathPrefix + configfile);
        if (xml == null) {
            logger.error("config not found: " + configfile + ",path=" + currentPath);
            return null;
        }
        try {
            return readFromXml(bnd, currentPath, xml.getContentAsString());
        } catch (CharsetException e) {
            // TODO improved eror handling
            throw new RuntimeException(e);
        }
    }

    /**
     * currentBundle is needed for resolving relative includes, that point to the origin bundle.
     */
    private static List<NativeNode> readFromXml(Bundle currentBundle, ResourcePath currentPath, String xml) {
        NativeDocument tw = null;
        try {
            tw = Platform.getInstance().parseXml(xml);
        } catch (XmlException e) {
            logger.error("parsing xml failed:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
        if (tw == null) {
            logger.error("parsing xml failed:" + xml);
            return null;
        }

        /*airports = XmlHelper.getChildNodeList(tw, "airports", "airport");
        scenes = XmlHelper.getChildNodeList(tw, "scenes", "scene");
        pois = XmlHelper.getChildNodeList(tw, "pois", "poi");

        List<NativeNode> aircraftlist = XmlHelper.getChildNodeList(tw, "aircrafts", "aircraft");
        aircrafts = new HashMap<String, GroundServiceAircraftConfig>();
        for (NativeNode n : aircraftlist) {
            String type = XmlHelper.getChildValue(n, "type");
            aircrafts.put(type, new GroundServiceAircraftConfig(n));
        }*/
        List<NativeNode> resultingNodeList = new ArrayList<NativeNode>();
        //List<NativeNode> includes = XmlHelper.getChildren(tw, "include");
        NativeNodeList nodeList = tw.getRootElement().getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            //if (includes != null){
            //for (NativeNode include:includes) {
            NativeNode nn = nodeList.getItem(i);
            if (nn.getNodeName().equals("include")) {
                String ref = nn.getTextValue();
                // TODO resolve other bundle
                List<NativeNode> includeNodes = readFromBundle(currentBundle, currentPath, ref);
                if (includeNodes == null) {
                    logger.error("include failed: " + ref);
                    return null;
                }
                resultingNodeList.addAll(includeNodes);
            } else {
                resultingNodeList.add(nn);
            }
        }

        return resultingNodeList;
    }


    public List<NativeNode> getObjects() {
        return XmlHelper.getChildren(topNodes, "object");
    }

    public List<NativeNode> getTerrains() {
        return XmlHelper.getChildren(topNodes, "terrain");
    }

    public List<NativeNode> getTrafficgraphs() {
        return XmlHelper.getChildren(topNodes, "trafficgraph");
    }

    public List<NativeNode> getVehicles() {
        return XmlHelper.getChildren(topNodes, "vehicle");
    }

    public List<NativeNode> getPois() {
        return XmlHelper.getChildren(topNodes, "poi");
    }

    /*27.11.23 no scene" in XSD currently. public SceneConfig getScene(String scenename) {
        SceneConfig sceneconfig = null;
        if (scenes == null) {
            logger.error("no scenes");
        }
        NativeNode scene = getNodeByName(scenename, scenes);
        if (scene == null) {
            //already logged.
            return null;
        }
        sceneconfig = new SceneConfig(scene);
        return sceneconfig;
    }*/

    public PoiConfig getPoiByName(String scenename) {
        NativeNode p = getNodeByName(scenename, getPois());
        if (p == null) {
            //already logged
            return null;
        }
        return new PoiConfig(p);

    }

    private NativeNode getNodeByName(String nodename, List<NativeNode> list) {
        NativeNode n = null;

        for (int i = 0; i < list.size(); i++) {
            if (nodename.equals(XmlHelper.getStringAttribute(list.get(i), "name", null))) {
                return list.get(i);
            }
        }
        if (n == null) {
            logger.error("element not found: " + nodename);

        }
        return null;
    }

    /*27.12.21 mach ich nicht mehr public void add(TrafficWorldConfig config) {
        vehicles.addAll(config.vehicles);
        //TODO ausgesuchter Rest. Kann tricky sein.N
    }*/

    /*public static Vector3 getVector3(String s, Vector3 defaultvalue) {
        Vector3 v = getVector3(s);
        if (v == null) {
            v = defaultvalue;
        }
        return v;
    }*/


    public /*ConfigNodeList*/List<Vehicle> getVehicleListByName(String name) {
        ConfigAttributeFilter filter = new ConfigAttributeFilter("name", name, false);
        List<ConfigNodeList> vehiclelists = ConfigNodeList.build(topNodes, "vehicles", "vehicle", filter);
        if (vehiclelists.size() == 0) {
            return null;
        }
        //28.10.21 Prefer model
        ConfigNodeList cnl = vehiclelists.get(0);
        List<Vehicle> vl = new ArrayList<Vehicle>();
        for (int i = 0; i < cnl.size(); i++) {
            vl.add(new Vehicle(cnl.get(i).getName(),
                    XmlHelper.getBooleanAttribute(cnl.get(i).nativeNode, XmlVehicleDefinition.DELAYEDLOAD, false),
                    XmlHelper.getBooleanAttribute(cnl.get(i).nativeNode, XmlVehicleDefinition.AUTOMOVE, false),
                    XmlHelper.getStringAttribute(cnl.get(i).nativeNode, XmlVehicleDefinition.LOCATION),
                    XmlHelper.getIntAttribute(cnl.get(i).nativeNode, XmlVehicleDefinition.INITIALCOUNT, 0)));
        }
        return vl;
    }

    /**
     * 30.11.23 no longer exists this way?
     * public ConfigNodeList getLocationListByName(String name) {
     * ConfigAttributeFilter filter = new ConfigAttributeFilter("name", name, false);
     * List<ConfigNodeList> locationlists = ConfigNodeList.build(topNodes, /*"locationlists", * /"locations", "location", filter);
     * if (locationlists.size() == 0) {
     * return null;
     * }
     * return locationlists.get(0);
     * }
     */


    public List<NativeNode> getViewpoints() {
        List<NativeNode> xmlVPs = XmlHelper.getChildren(topNodes, "viewpoint");
        return xmlVPs;
    }

    public LightDefinition[] getLights() {
        List<NativeNode> xmlLights = XmlHelper.getChildren(topNodes, "light");

        LightDefinition[] lds = null;
        if (xmlLights.size() > 0) {
            lds = new LightDefinition[xmlLights.size()];
        }
        int index = 0;
        for (NativeNode nn : xmlLights) {
            Color color = Color.parseString(XmlHelper.getStringAttribute(nn, "color"));
            String direction = XmlHelper.getStringAttribute(nn, "direction");
            if (direction != null) {
                lds[index] = new LightDefinition(color, Util.parseVector3(direction));
            } else {
                lds[index] = new LightDefinition(color, null);
            }
            index++;
        }
        return lds;
    }

    /*??public List<XmlVehicleConfig> getVehicleDefinitions() {
        return ConfigHelper.getVehicleDefinitions(tw);
    }*/

    /*27.11.23:what is this? Is is needed?public int getVehicleCount() {
        List<NativeNode> vehicles = XmlHelper.getChildNodeList(topNodes, "vehicles", "vehicle");
        return vehicles.size();
    }*/

    public int getVehicleDefinitionCount() {
        List<NativeNode> vehicles = XmlHelper.getChildren(topNodes, "vehicledefinition");

        return vehicles.size();
    }

    public VehicleDefinition getVehicleDefinition(int index) {
        List<NativeNode> vehicles = XmlHelper.getChildren(topNodes, "vehicledefinition");

        return new XmlVehicleDefinition(vehicles.get(index));
    }


    public List<AirportDefinition> findAirportDefinitionsByIcao(String icao) {
        List<NativeNode> result = XmlHelper.filter(getAirportDefinitions(topNodes),
                n -> icao.equals(XmlHelper.getStringAttribute(n, "icao")));
        return convertAirportDefinitions(result);
    }

    public LocalTransform getBaseTransformForVehicleOnGraph() {
        List<NativeNode> d = XmlHelper.getChildren(topNodes, "BaseTransformForVehicleOnGraph");
        if (d.size() > 0) {
            return ConfigHelper.getTransform(d.get(0));
        }
        return null;
    }

    public List<NativeNode> getVehicleDefinitions() {
        return XmlHelper.getChildren(topNodes, "vehicledefinition");
    }

    public List<VehicleDefinition> findVehicleDefinitionsByName(String name) {
        List<NativeNode> result = XmlHelper.filter(getVehicleDefinitions(topNodes),
                n -> name.equals(XmlHelper.getStringAttribute(n, "name")));
        return XmlVehicleDefinition.convertVehicleDefinitions(result);
    }

    /**
     * For internal use only.
     */
    private static List<NativeNode> getVehicleDefinitions(List<NativeNode> topNodes) {
        List<NativeNode> vehicleDefinitions = XmlHelper.getChildren(topNodes, "vehicledefinition");
        return vehicleDefinitions;
    }

    private static List<NativeNode> getAirportDefinitions(List<NativeNode> topNodes) {
        return XmlHelper.getChildren(topNodes, "airportdefinition");
    }


    private static List<AirportDefinition> convertAirportDefinitions(List<NativeNode> vehicleDefinitions) {
        List<AirportDefinition> result = new ArrayList<AirportDefinition>();
        for (int i = 0; i < vehicleDefinitions.size(); i++) {
            result.add(new XmlAirportDefinition(vehicleDefinitions.get(i)));
        }
        return result;
    }

}
