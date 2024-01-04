package de.yard.threed.traffic.config;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Vector3;

import java.util.Map;

/**
 * Konfiguration eines Vehicle. Currently only for XML.
 * 24.4.18: Soll wirklich nur ein Datencontainer ohne grosse Logik sein, weil es Attribut im VehicleComponent ist.
 * 29.10.21:Jetzt das interface dazu zur Abstraktion..
 * renamed from VehicleConfig to VehicleDefinition to better meet naming convetions.
 */
public interface VehicleDefinition {



    public String getBundlename();

    public String getName();
    /*public String getName() {
        return XmlHelper.getAttribute(nativeNode, "name");
    }*/

    public String getModelfile() ;

    public String getAircraftdir() ;

    public String getType() ;

    public String getModelType() ;

    public double getZoffset() ;

    /*public Vector3 getPilotPosition() {
        return new Vector3(0.16f, -0.14f, 0.236f);
    }*/

    public Map<String,LocalTransform> getViewpoints();
    
    public String[] getOptionals();


    public double getMaximumSpeed() ;

    public double getAcceleration() ;

    public double getApproachoffset();

    /**
     * Returns number of vehicles to create initially
     * 27.10.23: Deprecated because this is not a vehicle but a scenery setup property.
     */
    @Deprecated
    public int getInitialCount() ;

    /**
     * Hier ist der Default rue
     * @return
     */
    public boolean getUnscheduledmoving() ;

    public double getTurnRadius() ;

    public String getLowresFile();

    Float getWingspread();

    Vector3 getWingPassingPoint();

    Vector3 getLeftWingApproachPoint();

    Vector3 getCateringDoorPosition();

    Vector3 getRearPoint();
}
