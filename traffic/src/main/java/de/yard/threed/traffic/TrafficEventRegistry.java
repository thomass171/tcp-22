package de.yard.threed.traffic;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Payload;
import de.yard.threed.core.resource.BundleResource;

import de.yard.threed.traffic.geodesy.GeoCoordinate;


/**
 * Um Events nicht im System zu definieren, weil sie ja auch übergreifend verwendet werden koennten.
 * <p>
 * Created on 01.03.18.
 */
public class TrafficEventRegistry {

    /**
     * Muesste eigentlich airportloaded heissen?
     * 12.5.20: Eher nicht, groundnet ist ein optionaler Zusatz. Tja, oder? Aber das machen doch verschiedene Systems.
     * Die beiden sind exklusiv, d.h. bei Groundnet loaded kommt nicht zusaetzlich noch trafficgraph loaded.
     * 12.5.20: GROUNDNET_EVENT_LOADED kommt ... wenn Elevation ...
     */
    public static EventType GROUNDNET_EVENT_LOADED = EventType.register(4000, "GROUNDNET_EVENT_LOADED");
    public static EventType TRAFFIC_EVENT_GRAPHLOADED = EventType.register(4001, "TRAFFIC_EVENT_GRAPHLOADED");

    public static EventType TRAFFIC_EVENT_VEHICLELOADED = EventType.register(4002, "TRAFFIC_EVENT_VEHICLELOADED");

    /**
     * Ein Aircraft irgendwo ankommen lassen. Es wird unweit der deastination positioniert, um Zeit zu sparen. Das muss
     * aber nicht immer so bleiben.
     * TODO request statt event
     */
    public static EventType TRAFFIC_EVENT_AIRCRAFT_ARRIVE_REQUEST = EventType.register(4003, "TRAFFIC_EVENT_AIRCRAFT_ARRIVE_REQUEST");

    //TODO request statt event
    public static EventType TRAFFIC_EVENT_VEHICLEMOVEREQUEST = EventType.register(4004, "TRAFFIC_EVENT_VEHICLEMOVEREQUEST");

    /**
     * EVENT_POSITIONCHANGED is triggered by teleport, not every movement. Event
     * EVENT_LOCATIONCHANGED (18.3.24 now TRAFFIC_EVENT_SPHERE_LOADED) is for sphere change. Das geschieht nicht beim Teleport, zumindest nicht
     * so automatisch.
     * EVENT_POSITIONCHANGED is the event that ScenerySystem observes for updating scenery.
     * Flying to some airport wird damit nicht so ohne weiteres wie in FG zu einem UnLoad/Load führen. Denn woran soll
     * man das festmachen. Es gibt ja nicht wie in FG ein main aircraft, auf das sich alles bezieht.
     *
     * 26.5.24: Must be defined in TeleporterSystem and not here due to packages, even though it
     * doesn't really belong there (Hmm). Probably GraphMovingSystem should also send these events from time to time, so maybe its ok to have it there.
     */

    /**
     *
     */
    public static EventType TRAFFIC_EVENT_AIRPORT_LOADED = EventType.register(4006, "TRAFFIC_EVENT_AIRPORT_LOADED");

    /**
     * Successor of EVENT_LOCATIONCHANGED
     * 16.11.23: The tile name is needed by other systems for optionally loading the config file itself. Or do we add a provider? But
     * a big data provider leads to coupling. See README.md#DataFlow
     * GeoCoordinate is the most generic position information, suitable for all currently known tiles. For 2D projected tiles the GeoCoordinate
     * might be the center?
     * 18.3.24: No more: For 3D there is no tilename but GeoCoordinate (roughly a start location).
     * 26.5.24: The semantic of 'initialPosition' is not really defined and its purpose is questionable. Projection is provided by a provider
     * and there are viewpoints which could provide initial positions. Maybe we should consider it deprecated.
     */
    public static EventType TRAFFIC_EVENT_SPHERE_LOADED = EventType.register(4007, "TRAFFIC_EVENT_SPHERE_LOADED");

    public static Event buildSPHERELOADED(BundleResource tileName, GeoCoordinate initialPosition) {
        return new Event(TRAFFIC_EVENT_SPHERE_LOADED, new Payload()
                .add("tilename", tileName == null ? null : tileName.getFullQualifiedName())
                .add("initialPosition", initialPosition == null ? null : initialPosition.toString())
        );
    }
}
