package de.yard.threed.engine;

import de.yard.threed.core.EventType;

/**
 * Um Events nicht im System zu definieren, weil sie ja auch übergreifend verwendet werden koennten.
 * 
 * Created on 01.03.18.
 */
public class EventRegistry {
    /**
     * In irgendweinem Graph wurde ein Path angelegt. Das impliziert auch eine Erweiterung 
     * um Nodes z.B. fuer Smoothing oder besondere Bewegungen. Üblicherweise wird der Pfad
     * dann für ein Movement verwendet.
     * Für z.B. Visualisierung.
     */
    public static EventType GRAPH_EVENT_PATHCREATED = new EventType("GRAPH_EVENT_PATHCREATED");

    /**
     * Ein Path wurde abgefahren und ist damit obsolet.
     * (graph,path,vehicle)
     */
    public static EventType GRAPH_EVENT_PATHCOMPLETED = new EventType("GRAPH_EVENT_PATHCOMPLETED");

    /**
     * Payload ist layernummer. TODO der graph muss da auch rein.
     */
    public static EventType GRAPH_EVENT_LAYERCREATED = new EventType("GRAPH_EVENT_LAYERCREATED");
    public static EventType GRAPH_EVENT_LAYERREMOVED = new EventType("GRAPH_EVENT_LAYERREMOVED");

    /**
     * TODO Muesste eigentlich airportloaded heissen?
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
    public static EventType EVENT_POSITIONCHANGED = new EventType("EVENT_POSITIONCHANGED");
    public static EventType EVENT_LOCATIONCHANGED = new EventType("EVENT_LOCATIONCHANGED");

    /**
     *
     */
    public static EventType TRAFFIC_EVENT_AIRPORT_LOADED = new EventType("TRAFFIC_EVENT_AIRPORT_LOADED");

}
