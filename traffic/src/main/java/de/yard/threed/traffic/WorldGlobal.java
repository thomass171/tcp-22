package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LatLon;
import de.yard.threed.traffic.flight.FlightLocation;
import de.yard.threed.core.GeoCoordinate;


/**
 * Most of the values are related to Flightgear scenery, but since Flightgear should represent the real world, the values should be
 * quite generic.
 *
 * 5.1.17: New package flight for it.
 * 8.6.17: Pois are for models, not camera.
 * <p>
 * Created by thomass on 04.08.16.
 */
public class WorldGlobal {
    /**
     * Feet to Meters
     */
    public static  double SG_FEET_TO_METER = 0.3048;

    public static FlightLocation fleddkoverview =
            new FlightLocation(new GeoCoordinate(new Degree(50.876611),new Degree(7.128904),150),new Degree(285),new Degree(0));

    //Elsdorf Hoehe 0
    public static GeoCoordinate elsdorf0 = new GeoCoordinate( new Degree(50.937770f),new Degree(6.580982f), 0);
    public static POI elsdorf2000 = new POI("elsdorf2000","2000m ueber Elsdorf, Blick Richtung Osten",
            new FlightLocation(new GeoCoordinate(new Degree(50.937770f),new Degree(6.580982f),  2000* SG_FEET_TO_METER),new Degree(67),new Degree(0)));
    
    public static POI greenwich500 = new POI("greenwich500","500m ueber Greenwich (etwas östlich vom Observatorium), Blick nach Westen Richtung Observatorium",
            new FlightLocation(new GeoCoordinate( new Degree(51.477524f), new Degree(0),500),new Degree(270),new Degree(0)));
    //center des tile von Greenwich, so wie es von FG gelogged wurde.
    // 31.12.18: Seit double scheitert der FlightGearTest.testFGTileMgr (intersection). Mit longitude minimal grosser als 0 gehts. Da ist doch was faul. TODO pruefen
    //public static SGGeod greenwichtilecenter = new SGGeod(new Degree(0), new Degree(51.4775), 152.4);
    public static GeoCoordinate greenwichtilecenter = new GeoCoordinate(new Degree(51.4775),new Degree(0.00000001),  152.4);
    public static POI dahlem1300 = new POI("Dahlem 1300","etwas südlich Dahlemer Binz mit Blick auf refbtg bzw. Flugfeld Dahlemer Binz ( Norden)",
            new FlightLocation(new GeoCoordinate(new Degree(50.374), new Degree(6.531), 1300),new Degree(0),new Degree(-25)));

    public static POI eddkoverview = new POI("EDDK Overview","mit Blick auf Vorfeld",
            fleddkoverview);

    public static POI eddkoverviewfar = new POI("EDDK Overview","weiter weg mit Blick von Süden",
            new FlightLocation(new GeoCoordinate(new Degree(50.843675),new Degree(7.109709),1150),new Degree(25),new Degree(-15)));
    public static POI eddkoverviewfarhigh = new POI("EDDK Overview","wieter weg und von weit oben",
            new FlightLocation(new GeoCoordinate(new Degree(50.843675),new Degree(7.109709),11150),new Degree(25),new Degree(-65)));
    // Mit Elevation auf 777-200 zugeschnitten, daher nicht als POI geeignet. Ist auch erstmal Provisorium, bis irgendwie groundnet geht.
    public static FlightLocation eddkc4 = new FlightLocation(new GeoCoordinate(new Degree(50.879168),new Degree(7.124731),69.122488),new Degree(288.105366),new Degree(0));

    // idealisierter Erdradius
    public static double EARTHRADIUS = km(6000);
    public static double MOONRADIUS = km(1737);
    public static double DISTANCEMOONEARTH = km(384400);

    public static GeoCoordinate equator020000 = new GeoCoordinate(new Degree(0), new Degree(0), EARTHRADIUS + WorldGlobal.km(22000));
    //public static SGGeod equator020000indiocean = new SGGeod(new Degree(90), new Degree(0), EARTHRADIUS + WorldGlobal.km(5000));
    //public static SGGeod equator020000eastafrica = new SGGeod(new Degree(45), new Degree(0), EARTHRADIUS + WorldGlobal.km(5000));

    public static double km(int i) {
        return i * 1000;
    }


    // 27.12.21 from GroundnetMetadata
    public static GeoCoordinate EDDK_CENTER = GeoCoordinate.fromLatLon(new LatLon(new Degree(50.86538f), new Degree(7.139103f)),0);

}
