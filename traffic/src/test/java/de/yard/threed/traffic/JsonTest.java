package de.yard.threed.traffic;

import de.yard.threed.core.platform.Platform;

import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.trafficcore.model.Airport;
import de.yard.threed.trafficcore.model.Runway;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * Moved here from engine to avoid traffic-core dependency in engine.
 * Date: 27.06.20
 */
public class JsonTest {

    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine"}, new SimpleHeadlessPlatformFactory());

    @Test
    public void testSampleJson() {

        Platform platform = Platform.getInstance();

        Airport airport = buildAirport();

        String json = JsonUtil.toJson(airport);
        platform.getLog(JsonTest.class).debug("json=" + json);
        Airport ap = JsonUtil.toAirport(json);

        Assertions.assertEquals( airport.getIcao(), ap.getIcao(),"icao");
        Assertions.assertEquals( 2, ap.getRunways().length,"Runways().length");
        Assertions.assertEquals( airport.getRunways()[1].fromLat, ap.getRunways()[1].fromLat,"fromLat");

    }

    private Airport buildAirport() {
        Airport airport = new Airport("EEEE", 50.5, 7.7);
        Runway[] runways = new Runway[]{
                new Runway(51.5, 8.8, "F0", 51.6, 8.7, "T0", 44),
                new Runway(52.5, 9.8, "F1", 52.6, 9.7, "T1", 22.2)
        };
        airport.setRunways(runways);
        airport.setGroundNetXml("<?xml version=\"1.0\"?>\n" +
                "<groundnet>\n" +
                "  <version>1</version>\n" +
                "  <parkingList>\n" +
                "        <Parking index=\"0\" type=\"gate\" name=\"A15\" lat=\"N50 52.550096\" lon=\"E7 7.232618\" heading=\"207.83\"  radius=\"19\" pushBackRoute=\"66\" airlineCodes=\"\" />\n" +
                "        <Parking index=\"40\" type=\"ga\" name=\"Military_Ramp_98\" lat=\"N50 51.490171\" lon=\"E7 7.734365\" heading=\"273.49\"  radius=\"8\" pushBackRoute=\"240\" airlineCodes=\"\" />\n" +
                " </parkingList>\n" +
                " <TaxiNodes>\n" +
                "        <node index=\"41\" lat=\"N50 51.763223\" lon=\"E7 7.324679\" isOnRunway=\"0\" holdPointType=\"PushBack\"  />\n" +
                "        <node index=\"240\" lat=\"N50 51.488519\" lon=\"E7 7.761440\" isOnRunway=\"0\" holdPointType=\"PushBack\"  />\n" +
                " </TaxiNodes>\n" +
                " <TaxiWaySegments>\n" +
                "        <arc begin=\"41\" end=\"143\" isPushBackRoute=\"0\" name=\"\"  />\n" +
                "        <arc begin=\"58\" end=\"30\" isPushBackRoute=\"1\" name=\"Cargo_Ramp_W23\"  />\n" +
                "        <arc begin=\"240\" end=\"40\" isPushBackRoute=\"1\" name=\"Military_Ramp_98\"  />\n" +
                " </TaxiWaySegments>\n" +
                "</groundnet>\n");
        return airport;
    }
}
