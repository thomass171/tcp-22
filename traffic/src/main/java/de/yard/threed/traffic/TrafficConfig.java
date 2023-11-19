package de.yard.threed.traffic;

import de.yard.threed.core.Color;
import de.yard.threed.core.Util;
import de.yard.threed.core.XmlException;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.traffic.config.ConfigAttributeFilter;
import de.yard.threed.traffic.config.ConfigNodeList;
import de.yard.threed.traffic.config.PoiConfig;
import de.yard.threed.traffic.config.SceneConfig;
import de.yard.threed.traffic.config.XmlVehicleConfig;
import de.yard.threed.trafficcore.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * Successor of TrafficWorldConfig but generic (without model classees) for XSD based traffic config.
 * Could also be considered a SphereConfig.
 */
public class TrafficConfig {

    Log logger = Platform.getInstance().getLog(TrafficConfig.class);
    List<NativeNode> /*27.12.21vehicles,*/ airports, scenes, pois;
    public NativeDocument tw;

    public TrafficConfig(NativeDocument tw) {
        this.tw = tw;
    }

    /**
     *
     */
    public TrafficConfig(String bundle, String configfile) {

        Bundle bnd = BundleRegistry.getBundle(bundle);
        if (bnd == null) {
            logger.error("bundle not found:" + bundle);
            // andere Fehlerbehdnalung?
            return;
        }
        if (!bnd.contains(configfile)) {
            logger.error("config file not found:" + configfile);
            // andere Fehlerbehdnalung?
            return;
        }
        BundleData xml = bnd.getResource(configfile);


        try {
            tw = Platform.getInstance().parseXml(xml.getContentAsString());
        } catch (XmlException e) {
            e.printStackTrace();
        }
        if (tw == null) {
            logger.error("parsing xml failed:" + xml);
            return;
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
    }


    /*public static TrafficConfig readDefault() {
        return readConfig("data-old","TrafficWorld.xml"/*"Cockpit"* /);
    }*/

    public static TrafficConfig readConfig(String bundle, String configfile) {
        return new TrafficConfig(bundle, configfile);
    }

    /**
     * From global aircraft list.
     * @return
     */
    /*1.3.18 public AircraftConfig getAircraftConfig(String name) {
        for (int i=0;i< aircrafts.size();i++){
            if (name.equals(XmlHelper.getAttribute(aircrafts.get(i),"name"))){
                return new AircraftConfig(aircrafts.get(i));
            }
        }
        return null;
    }*/


    /**
     * From global vehicle list.
     * Das ist noch mischmasch mit metadaten.
     *
     * @return
     */


    /**
     * From current scene.
     * @return
     */
   /* public int getAircraftCount() {
        return sceneaircrafts.size();
    }*/

    /**
     * From current scene.
     *
     * @return
     */
    /*

    public SceneVehicle getSceneVehicle(int index) {
        return new SceneVehicle(scenevehicles.get(index));
    }*/

    /**
     * From current scene.
     * @return
     */
    /*public SmartLocation getVehicleLocation(int index) {
        //     for (int i=0;i<sceneaircrafts.size();i++){
        //        if (sceneaircrafts.get(i).isObject().getString("name").equals(aircraftname)){
        List<NativeNode> locations = XmlHelper.getChildren(scenevehicles.get(index), "location");
        if (locations.size() == 0) {
            return null;
        }
        NativeNode location = locations.get(0);
        return new SmartLocation(location.getNodeValue());
      /*      }
        }
        return null;* /
    }*/

    /*public String getAircraftName(int index) {
        return XmlHelper.getAttribute(sceneaircrafts.get(index), "name");
    }*/

    /**
     * Nicht sauber modelliert, aber flexibel.
     *
     * @return
     */
   /* public Map<String,String> getVehicleAttributes(int index) {
        return getVehicleAttributes(scenevehicles,index);
        
    }

    public static Map<String,String> getVehicleAttributes( List<NativeNode> vehicles, int index) {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("name", XmlHelper.getAttribute(vehicles.get(index), "name"));
        attributes.put("delayedload", XmlHelper.getAttribute(vehicles.get(index), "delayedload"));
        List<NativeNode> locations = XmlHelper.getChildren(vehicles.get(index), "location");
        if (locations.size() != 0) {
            NativeNode location = locations.get(0);
            attributes.put("location", location.getTextValue());
        }
        return attributes;
    }

    public static SmartLocation getVehicleLocation(List<NativeNode> vehicles, int index){
        Map<String, String> attributes = getVehicleAttributes(vehicles,index);
        return new SmartLocation(attributes.get("location"));
    }*/
    public SceneConfig getScene(String scenename) {
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
    }

    public PoiConfig getPoi(String scenename) {
        SceneConfig sceneconfig = null;
        NativeNode scene = getNodeByName(scenename, pois);
        if (scene == null) {
            //already logged
            return null;
        }
        return new PoiConfig(scene);

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
        List<ConfigNodeList> vehiclelists = ConfigNodeList.build(tw, "vehiclelists", "vehicles", "vehicle", filter);
        if (vehiclelists.size() == 0) {
            return null;
        }
        //28.10.21 Entkoppeln von XML
        ConfigNodeList cnl = vehiclelists.get(0);
        List<Vehicle> vl = new ArrayList<Vehicle>();
        for (int i = 0; i < cnl.size(); i++) {
            vl.add(new Vehicle(cnl.get(i).getName(),
                    XmlHelper.getBooleanAttribute(cnl.get(i).nativeNode, XmlVehicleConfig.DELAYEDLOAD, false),
                    XmlHelper.getBooleanAttribute(cnl.get(i).nativeNode, XmlVehicleConfig.AUTOMOVE, false),
                    XmlHelper.getStringAttribute(cnl.get(i).nativeNode, XmlVehicleConfig.LOCATION)));
        }
        return vl;
    }

    public ConfigNodeList getLocationListByName(String name) {
        ConfigAttributeFilter filter = new ConfigAttributeFilter("name", name, false);
        List<ConfigNodeList> locationlists = ConfigNodeList.build(tw, "locationlists", "locations", "location", filter);
        if (locationlists.size() == 0) {
            return null;
        }
        return locationlists.get(0);
    }


    public List<NativeNode> getViewpoints() {
        List<NativeNode> xmlVPs = XmlHelper.getChildren(tw, "viewpoint");
        return xmlVPs;
    }

    public LightDefinition[] getLights() {
        List<NativeNode> xmlLights = XmlHelper.getChildren(tw, "light");

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
}
