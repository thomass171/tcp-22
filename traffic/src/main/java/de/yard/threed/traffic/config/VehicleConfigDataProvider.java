package de.yard.threed.traffic.config;

import de.yard.threed.core.Util;
import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.engine.ecs.DataProvider;

public class VehicleConfigDataProvider implements DataProvider {
    //27.12.21TrafficWorldConfig tw;
    NativeDocument tw;

    public VehicleConfigDataProvider(NativeDocument tw) {
        this.tw = tw;
    }

    @Override
    public Object getData(Object[] parameter) {
        String vehicleName = (String) parameter[0];

        if (tw == null) {
            return new LocConfig();
        }
        //27.12.21 VehicleConfig vconfig = tw.getVehicleConfig(vehicleName);

        //30.10.23: xsd layout getter
        VehicleConfig vconfig = ConfigHelper.getVehicleDefinition(tw, vehicleName);
        if (vconfig != null) {
            return vconfig;
        }
        //30.10.23: Legacy getter
        vconfig = ConfigHelper.getVehicleConfig(tw, vehicleName);
        return vconfig;
    }
}
