package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.test.testutil.TestUtil;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.traffic.config.VehicleDefinition;
import de.yard.threed.traffic.config.XmlVehicleDefinition;
import de.yard.threed.trafficcore.config.AirportDefinition;
import de.yard.threed.trafficcore.config.LocatedVehicle;
import de.yard.threed.trafficcore.model.Vehicle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Needs 'traffic' bundle to be deployed.
 */
public class TrafficConfigTest {

    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine", "traffic"}, new SimpleHeadlessPlatformFactory());

    @Test
    public void testXmlLocomotive() throws Exception {

        EngineTestFactory.addTestResourcesBundle();
        //String xmlfile = "traffic/src/test/resources/xml-locomotive.xml";
        BundleResource xmlfile = new BundleResource("xml-locomotive.xml");

        TrafficConfig trafficConfig = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("test-resources"), xmlfile);
        TrafficSystem trafficSystem = new TrafficSystem();
        XmlVehicleDefinition.convertVehicleDefinitions(trafficConfig.getVehicleDefinitions()).forEach(vd->trafficSystem.addKnownVehicle(vd));

        VehicleDefinition vc = trafficSystem.findVehicleDefinitionsByName("Locomotive").get(0);
        assertNotNull(vc);
        Assertions.assertEquals("loc-lowres.xml", vc.getLowresFile());

        //returns null. Better wmpty list? TODO assertEquals(0, trafficConfig.getLights());

        LocalTransform localTransform = trafficConfig.getBaseTransformForVehicleOnGraph();
        assertNotNull(localTransform);
        assertEquals(0.16666667, localTransform.position.getY());

        assertEquals(1, trafficConfig.getVehicleDefinitionCount());
        assertNotNull(trafficConfig.getVehicleDefinition(0));
    }

    @Test
    public void testVehicleDefinitions() throws Exception {

        EngineTestFactory.addTestResourcesBundle();

        TrafficConfig trafficConfig = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("test-resources"), new BundleResource("vehicle-definitions.xml"));
        TrafficSystem trafficSystem = new TrafficSystem();
        XmlVehicleDefinition.convertVehicleDefinitions(trafficConfig.getVehicleDefinitions()).forEach(vd->trafficSystem.addKnownVehicle(vd));

        VehicleDefinition vc = trafficSystem.findVehicleDefinitionsByName("FollowMe").get(0);
        assertNotNull(vc);
        Assertions.assertEquals("fgaddon/followme.xml", vc.getModelfile());

        List<Vehicle> vehicles = trafficConfig.getVehicleListByName("VehiclesWithCockpit");
        assertNotNull(vehicles);
        assertEquals(5,vehicles.size());
        assertEquals(0,vehicles.get(0).initialCount);
        assertEquals(3,vehicles.get(1).initialCount);

        VehicleDefinition c172p = trafficSystem.findVehicleDefinitionsByName("c172p").get(0);
        assertNotNull(c172p.getViewpoints().get("Captain"));
        Assertions.assertEquals(16, c172p.getOptionals().length);

        assertEquals(1,trafficConfig.findVehicleDefinitionsByName("c172p").size());

    }

    @Test
    public void testAirportDefinitions() throws Exception {

        EngineTestFactory.addTestResourcesBundle();

        TrafficConfig trafficConfig = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("test-resources"), new BundleResource("airport-definitions.xml"));

        AirportDefinition ac = trafficConfig.findAirportDefinitionsByIcao("EDDK").get(0);
        assertNotNull(ac);
        Assertions.assertEquals("A20", ac.getHome());

        List<LocatedVehicle> locatedVehicles = ac.getVehicles();
        assertEquals(3, locatedVehicles.size());
        assertEquals("737-800 AB", locatedVehicles.get(1).getName());
        assertEquals("parkpos:B_8", locatedVehicles.get(1).getLocation().getLocation());

        assertEquals("groundnet:184-183", ac.getLocations().get(0).getLocation());

        assertNotNull(trafficConfig.getPoiByName("pname1"));
    }

    @Test
    public void testLoc() throws Exception {

        TrafficConfig trafficConfig = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic"), new BundleResource("tiles/locomotive.xml"));
        TrafficSystem trafficSystem = new TrafficSystem();
        XmlVehicleDefinition.convertVehicleDefinitions(trafficConfig.getVehicleDefinitions()).forEach(vd->trafficSystem.addKnownVehicle(vd));

        VehicleDefinition vc = trafficSystem.findVehicleDefinitionsByName("loc").get(0);
        assertNotNull(vc);
        Assertions.assertEquals("loc", vc.getName());
        Assertions.assertEquals("data", vc.getBundlename());
        Assertions.assertNull(vc.getLowresFile());

        Assertions.assertEquals("models/loc.gltf", vc.getModelfile());
        Assertions.assertNull(vc.getAircraftdir());
        Assertions.assertEquals(VehicleComponent.VEHICLE_RAILER, vc.getType());
        Assertions.assertEquals(0, vc.getZoffset());

        TestUtil.assertVector3(new Vector3(1, 1, 0), vc.getViewpoints().get("Driver").position);
        TestUtil.assertQuaternion("", Quaternion.buildFromAngles(new Degree(0), new Degree(90), new Degree(0)), vc.getViewpoints().get("Driver").rotation);
        TestUtil.assertVector3(new Vector3(9, 4, 0), vc.getViewpoints().get("BackSide").position);
        TestUtil.assertQuaternion("", Quaternion.buildFromAngles(new Degree(0), new Degree(90), new Degree(0)), vc.getViewpoints().get("BackSide").rotation);

        Assertions.assertEquals(0, vc.getOptionals().length);
        Assertions.assertEquals(21, vc.getMaximumSpeed());
        Assertions.assertEquals(4, vc.getAcceleration());
        Assertions.assertEquals(0, vc.getApproachoffset());
        Assertions.assertEquals(1, vc.getInitialCount());
        Assertions.assertTrue(vc.getUnscheduledmoving());
        Assertions.assertEquals(0, vc.getTurnRadius());
        Assertions.assertNull(vc.getWingspread());
        Assertions.assertNull(vc.getWingPassingPoint());
        Assertions.assertNull(vc.getLeftWingApproachPoint());
        Assertions.assertNull(vc.getCateringDoorPosition());
        //TODO Assertions.assertNull( vc.getRearPoint());
    }

    @Test
    public void testDemo() throws Exception {

        TrafficConfig trafficConfig = TrafficConfig.buildFromBundle(BundleRegistry.getBundle("traffic"), new BundleResource("tiles/Demo.xml"));
        TrafficSystem trafficSystem = new TrafficSystem();
        XmlVehicleDefinition.convertVehicleDefinitions(trafficConfig.getVehicleDefinitions()).forEach(vd->trafficSystem.addKnownVehicle(vd));

        // 'loc' should be available via 'include'
        VehicleDefinition vc = trafficSystem.findVehicleDefinitionsByName("loc").get(0);
        assertNotNull(vc);
        Assertions.assertEquals("loc", vc.getName());

    }
}

