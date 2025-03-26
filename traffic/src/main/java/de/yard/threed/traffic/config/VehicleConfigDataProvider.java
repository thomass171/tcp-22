package de.yard.threed.traffic.config;

import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.DataProvider;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.traffic.TrafficConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Some systems (GroundServiceSystem) need to know both the available service vehicles and the dimensions of aircrafts.
 * Should be set during startup (events might also be an option)
 * 20.3.25: Merged into TrafficSystem. Only finder remain here as static, eg. for tests
 */
public class VehicleConfigDataProvider /*implements DataProvider */{
    //27.12.21TrafficWorldConfig tw;
    // TrafficConfig tw;
   // List<VehicleDefinition> vehicleDefinitions;

    /*public VehicleConfigDataProvider(List<VehicleDefinition> /*TrafficConfig* / tw) {
        if (tw == null) {
            Platform.getInstance().getLog(VehicleConfigDataProvider.class).error("Setting null tw!");
        }
        this.vehicleDefinitions = tw;
    }*/

    /**
     * Finds by name basically.
     * 24.11.23: From AircraftConfigProvider find by type additionally.
     *
     * @param parameter
     * @return VehicleDefinition
     */
    /*@Override
    public Object getData(Object[] parameter) {
        String vehicleName = (String) parameter[0];

        if (vehicleDefinitions == null) {
            //25.11.23 return new LocConfig();
            Platform.getInstance().getLog(VehicleConfigDataProvider.class).error("tw not set! Vehicle cannot be resolved!");
            return null;
        }
        if (vehicleName == null) {
            return findVehicleDefinitionsByModelType((String) parameter[1]).get(0);
        }
        //27.12.21 VehicleConfig vconfig = tw.getVehicleConfig(vehicleName);

        //30.10.23: xsd layout getter
        List<VehicleDefinition> vconfig = findVehicleDefinitionsByName(vehicleName);
        if (vconfig.size() > 0) {
            return vconfig.get(0);
        }
        Platform.getInstance().getLog(VehicleConfigDataProvider.class).warn(
                "vehicle " + vehicleName + " not found in " + vehicleDefinitions.size() + " known.");

        //30.10.23: Legacy getter
        //26.11.23 no longer
        vconfig = null;//ConfigHelper.getVehicleConfig(tw, vehicleName);
        return vconfig;
    }*/

    /**
     * Was in TafficWorldConfig with name 'getAircraftConfiguration' once.
     */
    /*public VehicleDefinition getByType(String type) {
        List<VehicleDefinition> vconfig = tw.findVehicleDefinitionsByModelType(type);
        if (vconfig.size() > 0) {
            return vconfig.get(0);
        }
        return null;

    }*/
    public static List<VehicleDefinition> findVehicleDefinitionsByName(List<VehicleDefinition> vehicleDefinitions, String name) {
        //List<NativeNode> result = XmlHelper.filter(getVehicleDefinitions(topNodes),
        //        n -> name.equals(XmlHelper.getStringAttribute(n, "name")));
        //List<NativeNode> result = XmlHelper.filter(getVehicleDefinitions(topNodes),
        List<VehicleDefinition> result = new ArrayList<VehicleDefinition>();
        for (VehicleDefinition vd : vehicleDefinitions) {
            if (name.equals(vd.getName())) {
                result.add(vd);
            }
        }
        return result;//convertVehicleDefinitions(result);
    }

    public static List<VehicleDefinition> findVehicleDefinitionsByNameFromXml(List<NativeNode> vds, String name) {
        return findVehicleDefinitionsByName(XmlVehicleDefinition.convertVehicleDefinitions(vds), name);
    }

    public static List<VehicleDefinition> findVehicleDefinitionsByModelType(List<VehicleDefinition> vehicleDefinitions, String modeltype) {
        //List<NativeNode> result = XmlHelper.filter(getVehicleDefinitions(topNodes),
        //       n -> modeltype.equals(XmlHelper.getStringAttribute(n, "modeltype")));
        //return convertVehicleDefinitions(result);
        List<VehicleDefinition> result = new ArrayList<VehicleDefinition>();
        for (VehicleDefinition vd : vehicleDefinitions) {
            if (modeltype.equals(vd.getModelType())) {
                result.add(vd);
            }
        }
        return result;
    }

    public static List<VehicleDefinition> findVehicleDefinitionsByModelTypeFromXml(List<NativeNode> vds, String modeltype) {
        return findVehicleDefinitionsByModelType(XmlVehicleDefinition.convertVehicleDefinitions(vds), modeltype);
    }
}
