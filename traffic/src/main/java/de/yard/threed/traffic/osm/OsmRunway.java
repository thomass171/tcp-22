package de.yard.threed.traffic.osm;


import de.yard.threed.core.Degree;

import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.trafficcore.model.Runway;

/**
 * Soll nur die aus apt.dat her bekannten Daten kennen, d.h. vor allem keine Elevation.
 *
 * Die icao.ils.xml als Quelle reicht nicht, weil sie keine Länge enthält.
 * 28.2.2020: Aber die *.threshold.xml könnte für einen Trafficgraph geignet sein, wenn man von den dortigen Punkten
 * einen Weg ins groundnet sucht.
 *
 * 5.5.20: Deprecated zugunsten von {odel.Runway}, wobei das wegen der Sonderpunkte erstmal gar nicht so einfach geht.
 * <p>
 * <runway>
 * <ils>
 * <lon>7.12070600000001</lon>
 * <lat>50.859192</lat>
 * <rwy>24</rwy>
 * <hdg-deg>244.34</hdg-deg>
 * <elev-m>70.41</elev-m>
 * <nav-id>IKOW</nav-id>
 * </ils>
 * </runway>
 * <runway>
 * <ils>
 * <lon>7.16906400000001</lon>
 * <lat>50.8528670000001</lat>
 * <rwy>14L</rwy>
 * <hdg-deg>137.58</hdg-deg>
 * <elev-m>92.05</elev-m>
 * <nav-id>IKES</nav-id>
 * </ils>
 * <ils>
 * <lon>7.12643900000001</lon>
 * <lat>50.8822860000001</lat>
 * <rwy>32R</rwy>
 * <hdg-deg>317.55</hdg-deg>
 * <elev-m>70.10</elev-m>
 * <nav-id>IKEN</nav-id>
 * </ils>
 * </runway>
 */
@Deprecated
public class OsmRunway {
    public Degree heading;
    public String name;
    //TODO das mit der enternode ist doch zu statisch?
    public String enternodefromgroundnet;
    //der entrypoint ist zur Nutzung im groundnet um den groundnet graph rein zu verlängern.
    public GeoCoordinate /*coor,*/ from, to, entrypoint, holdingpoint, takeoffpoint, touchdownpoint;
    double width;
    int len;
    Runway runway;

    public static Runway eddk14L(/* gibts in meta data noch nicht TerrainElevationProvider elevationprovider*/) {
        Runway   runway = new Runway(50.88046900,007.12907500,"14L",50.85519400,007.16569200,"?",60.05);
        OsmRunway osmrunway = new OsmRunway();
osmrunway.runway=runway;

        //runway.name = "14L";
        osmrunway.from=GeoCoordinate.fromLatLon(runway.getFrom(),0);
        osmrunway.to=GeoCoordinate.fromLatLon(runway.getTo(),0);
        //6.5.20 runway.heading = new Degree(137.58f);
        //3814 isType official length. 350 added wegen ils hinter ende
        double ilsoffset = 350;
        //6.5.20 runway.len = 3814;
        //oder 187?
        osmrunway.enternodefromgroundnet = "188";
        // postition isType ils beyond end of runway
        //runway.coor = new SGGeod(new FGDegree(7.169064f), new FGDegree(50.852867f), 92.05f);
        //runway.from = runway.coor.applyCourseDistance(runway.heading.reverse(), runway.len + ilsoffset);
        //runway.to = runway.coor.applyCourseDistance(runway.heading, -ilsoffset);
        //from und to aus apt.dat. apt.dat hat aber Elevation nur pro Airport 
        //6.5.20 runway.from = new SGGeod(new Degree(007.12907500f), new Degree(50.88046900f), 92.05f);
        //6.5.20 runway.to = new SGGeod(new Degree(007.16569200f), new Degree(50.85519400f), 92.05f);
        //6.5.20 runway.width = 60.05f;

        //TODO die 65 passen nur fuer EDDK. Aber das lass ich bis zur Shuttlelandung erstmal so fix.
        /*osmrunway.holdingpoint = SGGeod.fromLatLon(RunwayHelper.calcHoldingPoint(runway));//.from.applyCourseDistance(runway.heading, 65);
        osmrunway.entrypoint = SGGeod.fromLatLon(RunwayHelper.calcEntrypoint(runway));//.holdingpoint.applyCourseDistance(runway.heading.reverse(), 15);
        //start evtl. speziell fuer C172/EDDK
        osmrunway.takeoffpoint = SGGeod.fromLatLon(RunwayHelper.calcTakeoffPoint(runway));//.from.applyCourseDistance(runway.heading, 600);
        osmrunway.touchdownpoint = SGGeod.fromLatLon(RunwayHelper.calcTouchdownpoint(runway));//.from.applyCourseDistance(runway.heading, 200);
        osmrunway.heading=RunwayHelper.calcHeading(runway);*/
        return runway;
    }


}
