package de.yard.threed.traffic.config;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.traffic.VehicleComponent;

import java.util.HashMap;
import java.util.Map;


/**
 * Extracted from railing.xml. Really needed.
 */
public class LocConfig implements VehicleConfig {
    @Override
    public String getBundlename() {
        return "data";
    }

    @Override
    public String getName() {
        return "loc";
    }

    @Override
    public String getModelfile() {
        return "models/loc.gltf";
    }

    @Override
    public String getAircraftdir() {
        return null;
    }

    @Override
    public String getType() {
        return VehicleComponent.VEHICLE_RAILER;
    }

    @Override
    public String getModelType() {
        return null;
    }

    @Override
    public double getZoffset() {
        return 0;
    }

    @Override
    public Map<String, LocalTransform> getViewpoints() {
        HashMap<String, LocalTransform> vps = new HashMap<String, LocalTransform>();
        // rotation is needed because of default camera orientation along z-axis?
        vps.put("Driver", new LocalTransform(new Vector3(1, 1, 0), Quaternion.buildFromAngles(new Degree(0), new Degree(90), new Degree(0))));
        vps.put("BackSide", new LocalTransform(new Vector3(9, 4, 0), Quaternion.buildFromAngles(new Degree(0), new Degree(90), new Degree(0))));

        return vps;
    }

    @Override
    public String[] getOptionals() {
        return new String[0];
    }

    @Override
    public double getMaximumSpeed() {
        return 21;
    }

    @Override
    public double getAcceleration() {
        return 4;
    }

    @Override
    public double getApproachoffset() {
        return 0;
    }

    @Override
    public int getInitialCount() {
        return 1;
    }

    @Override
    public boolean getUnscheduledmoving() {
        return true;
    }

    @Override
    public double getTurnRadius() {
        return 0;
    }

    @Override
    public String getLowresFile() {
        return null;
    }
}
