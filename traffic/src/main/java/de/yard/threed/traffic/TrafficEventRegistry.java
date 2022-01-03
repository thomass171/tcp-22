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
    public static EventType GROUNDNET_EVENT_LOADED = new EventType("GROUNDNET_EVENT_LOADED");
    public static EventType TRAFFIC_EVENT_GRAPHLOADED = new EventType("TRAFFIC_EVENT_GRAPHLOADED");

    public static EventType TRAFFIC_EVENT_VEHICLELOADED = new EventType("TRAFFIC_EVENT_VEHICLELOADED");

    /**
     * Ein Aircraft irgendwo ankommen lassen. Es wird unweit der deastination positioniert, um Zeit zu sparen. Das muss
     * aber nicht immer so bleiben.
     * TODO request statt event
     */
    public static EventType TRAFFIC_EVENT_AIRCRAFT_ARRIVE_REQUEST = new EventType("TRAFFIC_EVENT_AIRCRAFT_ARRIVE_REQUEST");

    //TODO request statt event
    public static EventType TRAFFIC_EVENT_VEHICLEMOVEREQUEST = new EventType("TRAFFIC_EVENT_VEHICLEMOVEREQUEST");

    /**
     * Das EVENT_POSITIONCHANGED kommt nur beim Teleporten, nicht bei jedem Movement. Das
     * EVENT_LOCATIONCHANGED hingegen zeigt an, dass die Umgebung gewechselt wird. Das geschieht nicht beim Teleport, zumindest nicht
     * so automatisch.
     * Ein wegmoven zu einem anderen Airport wird damit nicht so ohne weiteres wie in FG zu einem UnLoad/Load führen. Denn woran soll
     * man das festmachen. Es gibt ja nicht wie in FG ein main aircraft, auf das sich alles bezieht.
     * 27.3.20: Es ist wohl unklar, wann genau ein EVENT_LOCATIONCHANGED. Zumindest mal beim Init, um nachfolgendes Laden wie groundnet und terrain zu machen. Das
     * Groundnet geht jetzt aber ueber Request, weil ja nicht bekannt ist, wann Terrain wirklich da ist. Request kann dann warten.
     */
    //In TeleporterSystem public static EventType EVENT_POSITIONCHANGED = new EventType("EVENT_POSITIONCHANGED");
    public static EventType EVENT_LOCATIONCHANGED = new EventType("EVENT_LOCATIONCHANGED");

    public static Event buildLOCATIONCHANGED(GeoCoordinate sgGeod, /*27.21.21AirportConfig airportConfig,*/ Tile tilename, BundleResource tileResource) {
        return new Event(EVENT_LOCATIONCHANGED, new Payload(sgGeod, /*airportConfig,*/ tilename, tileResource));
    }

    /**
     *
     */
    public static EventType TRAFFIC_EVENT_AIRPORT_LOADED = new EventType("TRAFFIC_EVENT_AIRPORT_LOADED");

}
