package de.yard.threed.traffic;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.DefaultEcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.core.platform.Log;
import de.yard.threed.traffic.config.VehicleDefinition;


/**
 * Fuer alles Ground(Service)Vehicle, aber auch für Aircraft. Sind in FG ja durch AI da.
 * <p>
 * Die Granularitaet der Typisierung ist uneinheitlich. Darum gibt es type und modeltype
 * <p>
 * 13.3.19 MA30 schedules: Jetzt nur noch die Gruppierung AIRCRAFT, CAR
 * 26.3.19 Das ist jetzt die zentrale Stelle, die festhält, ob ein Vehicle 'locked/busy' ist, unabhängig davon, was das konkret ist.
 * Created by thomass on 13.04.17.
 */
public class VehicleComponent extends DefaultEcsComponent {
    public Travelplan travelplan;
    Log logger = Platform.getInstance().getLog(VehicleComponent.class);
    public static String VEHICLE_CAR = "car";
    public static String VEHICLE_AIRCRAFT = "aircraft";
    // 14.12.21: Don't know a better name
    public static String VEHICLE_RAILER = "railer";
    // type,modeltype stehen in config
    public VehicleDefinition config;
    public static String TAG = "VehicleComponent";
    //public long statechangetimestamp = 0;
    public GraphNode lastdestination = null;
    //7.3.2020 gibt doch schon flightdestination
    // public TravelDestination travelDestination;
    //wird bei arrival gesetzt und bei departure genullt.
    //28.3.20. aber von wem? besser in trafficgraph. Aber manchmal ist der Graph gemsmoothed, dann ist es kein TrafficGraph mehr. Also doch besser hier.
    //vielleicht ist es verzichtbar  public String currentAirportIcao;
    public FlightRouteBuilder flightRouteBuilder = null;
    // 29.8.23
    public SceneNode teleportParentNode;

    public VehicleComponent(VehicleDefinition config/*String type,String modeltype*/) {
        this.config = config;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static VehicleComponent getVehicleComponent(EcsEntity e) {
        VehicleComponent vc = (VehicleComponent) e.getComponent(VehicleComponent.TAG);
        return vc;
    }

    public VehicleDefinition getVehicleDefinition() {
        return config;
    }
}


