package de.yard.threed.traffic;

import de.yard.threed.engine.platform.common.RequestType;

/**
 * 21.3.19: Abgrenzung Request/Event.
 * 
 * Created on 01.03.18.
 */
public class RequestRegistry {

    /**
     * Ein (noch nicht geladenes?) Vehicle aus der Configuration laden. Entweder ein spezielles über den Namen oder einfach das nächste.
     * 26.3.20: Die Location ist auch eine uebergebene oder die naechste.
     * 24.11.20: Deprecated, weil der nicht in einem System läuft. Stattdessen gibts den 2er.
     * 29.10.21: Und ein Request to load all vehicles for a single graph (payload0,groundnet payload1). Needs to wait unitl
     * everything is ready, eg. Elevation available.(// 27.3.20: Und die Vehicles brauchen ja auch Terrain wegen der Elevation. Das soll bei client/server aber nicht mehr so sein.
     *             // 12.5.20: Doch, die brauchen ja auch ein richtige Elevation, also passend zum Client
     * 18.1.23: TRAFFIC_REQUEST_LOADVEHICLES loads vehicles from a list (additional to initial vehicle? No! There is only one load vehicles when terrain is available!)
     */
    @Deprecated
    public static RequestType TRAFFIC_REQUEST_LOADVEHICLE = new RequestType("TRAFFIC_REQUEST_LOADVEHICLE");
    //used in other projects:
    public static RequestType TRAFFIC_REQUEST_LOADVEHICLE2 = new RequestType("TRAFFIC_REQUEST_LOADVEHICLE2");
    public static RequestType TRAFFIC_REQUEST_LOADVEHICLES = new RequestType("TRAFFIC_REQUEST_LOADVEHICLES");

    /**
     * Payload: String(icao)
     */
    public static RequestType TRAFFIC_REQUEST_LOADGROUNDNET = new RequestType("TRAFFIC_REQUEST_LOADGROUNDNET");

    /**
     * Ein Aircraft will departen. Dann muss
     * a) evtl. der Servicepoint gecleaned werden
     * b) ein Schedule fuer taxiing und travel angelegt werden.
     * Es wird noch kein kompletter Graph erzeugt.
     */
    public static RequestType TRAFFIC_REQUEST_AIRCRAFTDEPARTING = new RequestType("TRAFFIC_REQUEST_AIRCRAFTDEPARTING");

    /**
     * Wenn kein Aircraft dabei ist, wird erst eins erstellt und fährt dann erst ein. Nee, besser nicht mischen.
     * Das wird sonst zu uebersichtlich.
     * 13.3.19: Jetzt pro Service und mit Parameter des Service.
     */
    public static RequestType TRAFFIC_REQUEST_AIRCRAFTSERVICE = new RequestType("TRAFFIC_REQUEST_AIRCRAFTSERVICE");

    /**
     * Ein Vehicle soll sich an eine andere Position bewegen.  By TravelHelper.spawnMoving().
     * 6.3.2020: Das muesste für Roundtrips, Orbit, Moon, aber auch GroundnetService gleichermassen geeignet sein. Obwohl dafuer vielleicht besser
     * ein TRAFFIC_REQUEST_VEHICLE_TRAVEL geeignet ist, und das hier wirklich für innerhalb eines Graph? Naja, mal sehn. per TravelHelper.spawnTravel()
     * Ich
     *
     * Payload enthält nur die Destination. Der PAth wird später erst ermittelt.
     * 7.5.19
     */
    public static RequestType TRAFFIC_REQUEST_VEHICLE_MOVE = new RequestType("TRAFFIC_REQUEST_VEHICLE_MOVE");

    /**
     * Verallgemeinerung von TRAFFIC_REQUEST_VEHICLE_MOVE.
     * Das beisst sich aber mit TRAFFIC_REQUEST_AIRCRAFTDEPARTING, was auch eigentlich sinnvoller ist!
     * 7.3.2020
     */
    //public static RequestType TRAFFIC_REQUEST_VEHICLE_TRAVEL = new RequestType("TRAFFIC_REQUEST_VEHICLE_TRAVEL");

    /**
     * Einen Airport(Groundnet) bei Annäherung zu laden klingt zwar gut, aber wie soll man feststellen, welche Airports gerade so in der Nähe sind.
     * Und evtl. will ein Aircraft da ja gar nicht hin, sondern fliegt drüber. Darum auf jeden Fall auch mal ein Request dafür.
     * 16.5.20: Zur Vereinfachung erstmal weglassen und nur mit TRAFFIC_REQUEST_LOADGROUNDNET arbeiten. Das ist aber nicht wirklich einfacher, weil es zumindest
     * zwei verschiedene REST Servies sind? Aber warum wird das nicht einfach einer? Amen.
     */
    //public static RequestType TRAFFIC_REQUEST_LOADAIRPORT = new RequestType("TRAFFIC_REQUEST_LOADAIRPORT");

    /**
     * Gehoert hier evtl. gar nicht hin. Erstmal ohne und stattdessen basename per EVENT_LOCATIONCHANGED
     * 7.10.21
     */
    //public static RequestType USER_REQUEST_TILE_LOAD = new RequestType("USER_REQUEST_TILE_LOAD");
}
