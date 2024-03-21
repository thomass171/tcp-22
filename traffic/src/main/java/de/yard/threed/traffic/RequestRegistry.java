package de.yard.threed.traffic;

import de.yard.threed.core.Payload;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;

/**
 * 21.3.19: Abgrenzung Request/Event.
 * <p>
 * Created on 01.03.18.
 */
public class RequestRegistry {

    /**
     * Load a vehicle from configuration. Either by name (property "initialVehicle") or just the next.
     * Location also is by parameter or just the next by configuration.
     * Typically this is a vehicle with a cockpit to where the user can teleport.
     * Needs to wait until everything is ready, eg. terrain and elevation available.
     * (Das soll bei client/server aber nicht mehr so sein.
     * // 12.5.20: Doch, die brauchen ja auch ein richtige Elevation, also passend zum Client
     *
     * 31.10.23: TRAFFIC_REQUEST_LOADVEHICLE no longer deprecated. Its triggered eg. by property "initialVehicle".
     * 20.3.24: Optional parameter 'initialRoute' added. Main reason is that TrafficSystem.trafficgraphs map has no space for multiple
     * graph in same cluster. But the future might provide a better idea.
     */
    public static RequestType TRAFFIC_REQUEST_LOADVEHICLE = RequestType.register(4001, "TRAFFIC_REQUEST_LOADVEHICLE");

    public static Request buildLoadVehicle(int userEntityId, String name, String smartLocation, String initialRoute) {
        return new Request(TRAFFIC_REQUEST_LOADVEHICLE, new Payload()
                .add("name", name)
                .add("location", smartLocation)
                .add("initialRoute", initialRoute)
        );
    }

    /**
     * 18.1.23: TRAFFIC_REQUEST_LOADVEHICLES loads all vehicles from a list (additional to initial vehicle? No! There is only
     * one load vehicles when terrain is available!) for a single graph (payload0,groundnet payload1). Might also be triggered multiple when several graphs are loaded.
     * Typically TRAFFIC_REQUEST_LOADVEHICLES is used during set up of a scene to populate a traffic graph (eg. groundnet)
     * after it has been loaded. Still has no userid.
     * 27.2.24: TRAFFIC_REQUEST_LOADVEHICLES second parameter deprecated groundnet removed. vehiclelist cannot be a parameter,
     * because not all systems that fire this request (eg. GroundServicesSystem) know the vehicles.
     */
    public static RequestType TRAFFIC_REQUEST_LOADVEHICLES = RequestType.register(4003, "TRAFFIC_REQUEST_LOADVEHICLES");
    public static Request buildLoadVehicles(TrafficGraph trafficGraph) {
        return new Request(TRAFFIC_REQUEST_LOADVEHICLES, new Payload(new Object[]{trafficGraph}));
    }

    /**
     * Payload: String(icao),23.2.24: Intentionally without bundle and filename. The processor should have a kind of lookup by icao.
     * For now has no userid.
     */
    public static RequestType TRAFFIC_REQUEST_LOADGROUNDNET = RequestType.register(4004, "TRAFFIC_REQUEST_LOADGROUNDNET");

    public static Request buildLoadGroundnet(/*int userEntityId,*/ String icao) {
        return new Request(TRAFFIC_REQUEST_LOADGROUNDNET, new Payload().add("icao", icao));
    }

    /**
     * A Aircraft wants to depart. This might include
     * a) clearing the service point
     * b) a schedule for taxing and move to runway
     * c) start flying a flight plan
     * A graph is used step by step.
     * A vehicle might already be loacated on a runway. Then no taxiing ist done.
     */
    public static RequestType TRAFFIC_REQUEST_AIRCRAFTDEPARTING = RequestType.register(4005, "TRAFFIC_REQUEST_AIRCRAFTDEPARTING");

    /**
     * Wenn kein Aircraft dabei ist, wird erst eins erstellt und fährt dann erst ein. Nee, besser nicht mischen.
     * Das wird sonst zu uebersichtlich.
     * 13.3.19: Jetzt pro Service und mit Parameter des Service.
     */
    public static RequestType TRAFFIC_REQUEST_AIRCRAFTSERVICE = RequestType.register(4006, "TRAFFIC_REQUEST_AIRCRAFTSERVICE");

    /**
     * Ein Vehicle soll sich an eine andere Position bewegen.  By TravelHelper.spawnMoving().
     * 6.3.2020: Das muesste für Roundtrips, Orbit, Moon, aber auch GroundnetService gleichermassen geeignet sein. Obwohl dafuer vielleicht besser
     * ein TRAFFIC_REQUEST_VEHICLE_TRAVEL geeignet ist, und das hier wirklich für innerhalb eines Graph? Naja, mal sehn. per TravelHelper.spawnTravel()
     * Ich
     * <p>
     * Payload enthält nur die Destination. Der PAth wird später erst ermittelt.
     * 7.5.19
     */
    public static RequestType TRAFFIC_REQUEST_VEHICLE_MOVE = RequestType.register(4007, "TRAFFIC_REQUEST_VEHICLE_MOVE");

    /**
     * Verallgemeinerung von TRAFFIC_REQUEST_VEHICLE_MOVE.
     * Das beisst sich aber mit TRAFFIC_REQUEST_AIRCRAFTDEPARTING, was auch eigentlich sinnvoller ist!
     * 7.3.2020
     */
    //public static RequestType TRAFFIC_REQUEST_VEHICLE_TRAVEL = RequestType.register("TRAFFIC_REQUEST_VEHICLE_TRAVEL");

    /**
     * Einen Airport(Groundnet) bei Annäherung zu laden klingt zwar gut, aber wie soll man feststellen, welche Airports gerade so in der Nähe sind.
     * Und evtl. will ein Aircraft da ja gar nicht hin, sondern fliegt drüber. Darum auf jeden Fall auch mal ein Request dafür.
     * 16.5.20: Zur Vereinfachung erstmal weglassen und nur mit TRAFFIC_REQUEST_LOADGROUNDNET arbeiten. Das ist aber nicht wirklich einfacher, weil es zumindest
     * zwei verschiedene REST Servies sind? Aber warum wird das nicht einfach einer? Amen.
     */
    //public static RequestType TRAFFIC_REQUEST_LOADAIRPORT = RequestType.register("TRAFFIC_REQUEST_LOADAIRPORT");

    /**
     * Gehoert hier evtl. gar nicht hin. Erstmal ohne und stattdessen basename per EVENT_LOCATIONCHANGED
     * 7.10.21
     */
    //public static RequestType USER_REQUEST_TILE_LOAD = RequestType.register("USER_REQUEST_TILE_LOAD");
}
