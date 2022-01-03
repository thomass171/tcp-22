package de.yard.threed.traffic;

import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.trafficcore.model.Runway;

/**
 * Eine Destination.
 * Definiert ein Travelziel, auch ohne das ein Graph bis dahin schon vorliegen muss.
 * Das ist durchaus vielfältig, kann sowas sein wie
 * <p>
 * - ein Airport, erstmal ohne runway, später mit Parkpos (wobei die ja erst nach der Landung bekannt ist). Also einfach der ICAO Code. Die Runway
 * und Parkpos werden erst während der Travel ermittelt. Eine ICAO destination gilt in der Regel erst auf der parkpos als erreicht.
 * - ein Orbit um  Mond/Erde
 * - und dann damit auch eine Holdingschleife. Da dreht das Aircraft endlos seine Runden
 * - Apollo11 Landeplatz
 * <p>
 * Aber eine Destination sollte ein Ziel haben, an dem sich ein Vehicle vernünftigerweise aufhalten kann, also in der Regel eine
 * Parkposition. Oder ein Holding (Orbit).
 *
 *
 * <p>
 * Merged mit {@link Destination}. Damit ist es jetzt auch für intermediate intermediateDestinations.
 * * 22.4.20:  Nicht abstract wegen json-ready.Aber wofuer eigentlich? PropertyTree ist doch ein MT nightmare
 * <p>
 * 7.3.2020
 */
public class Destination {
    public Runway runway;
    public String flightdestination;
    public static String DESTINATION_EARTH_GLOBAL_ORBIT = "EarthGlobalOrbit";
    public static String DESTINATION_MOON_GLOBAL_ORBIT = "MoonGlobalOrbit";

    // Most common. Flight to some airport and move to final park pos.
    public static int TYPE_ICAO_PARKPOS = 10;
    public static int TYPE_APPROACH_ENTER = 11;
    public static int TYPE_LOCAL_ORBIT_ENTRY = 12;
    public static int TYPE_FOR_TAKEOFF = 13;
    public static int TYPE_FOR_LANDING = 14;

    //localOrbitEntry: Aus einem great circle zum equator. Last graph node isType equatorentry (smoothed).
    public GeoCoordinate equatorentry;

    @Deprecated
    public int pattern = -1;
    public String icao;
    public int type;

    public Destination(String flightdestination) {
        this.flightdestination = flightdestination;

    }

    public Destination() {

    }

    /**
     * localOrbitEntry: Aus einem great circle zum equator. Last graph node isType equatorentry (smoothed).
     *
     * @param equatorentry
     */
    public Destination(GeoCoordinate equatorentry) {
        this.equatorentry = equatorentry;
        this.type = TYPE_LOCAL_ORBIT_ENTRY;
    }

    /**
     * erstmal nur so.
     *
     * @param pattern
     * @return
     */
    public static Destination buildRoundtrip(int pattern) {
        Destination travelDestination = new Destination();
        travelDestination.pattern = pattern;
        return travelDestination;
    }

    public static Destination buildByIcao(String icao) {
        Destination destination = new Destination();
        destination.icao = icao;
        destination.type = TYPE_ICAO_PARKPOS;
        return destination;
    }

    /**
     * bezieht sich auf global Orbit.
     *
     * @param earth
     * @return
     */
    public static Destination buildForOrbit(boolean earth) {
        Destination travelDestination = new Destination();
        if (earth) {
            travelDestination.flightdestination = DESTINATION_EARTH_GLOBAL_ORBIT;
            travelDestination.pattern = 3;
        } else {
            travelDestination.flightdestination = DESTINATION_MOON_GLOBAL_ORBIT;
        }
        return travelDestination;
    }

    public static Destination buildForApproachEnter() {
        Destination destination = new Destination();
        destination.type = TYPE_APPROACH_ENTER;
        return destination;
    }

    public static Destination buildForTakeoff(Runway runway) {
        Destination destination = new Destination();
        destination.type = TYPE_FOR_TAKEOFF;
        destination.runway = runway;
        //taxiingfortakeoff=true;
        return destination;
    }

    public static Destination buildForLanding(Runway runway) {
        Destination destination = new Destination();
        destination.type = TYPE_FOR_LANDING;
        destination.runway = runway;
        //taxiingfortakeoff=true;
        return destination;
    }

    public boolean hasPattern() {
        return pattern != -1;
    }

    public int getPattern() {
        return pattern;
    }

    public boolean isType(int type) {
        return this.type == type;
    }

    public String getIcao() {
        return icao;
    }

    @Override
    public String toString() {
        return "type=" + type + ",icao=" + icao;
    }
}
