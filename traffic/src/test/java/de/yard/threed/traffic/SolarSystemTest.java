package de.yard.threed.traffic;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphMovingSystem;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.javacommon.JavaBundleResolverFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;


public class SolarSystemTest {
    Platform  platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new SimpleHeadlessPlatformFactory());

    @Test
    public void testOrbits(){
        SolarSystem solarSystem = new SolarSystem();
        double distanceSunEarth= WorldGlobal.km(18000);
        double distanceEarthMoon=WorldGlobal.km(12000);

        //SystemManager.reinit();
        GraphMovingSystem graphMovingSystem=new GraphMovingSystem();
        //SystemManager.initSystems();

        SceneNode sunNode = solarSystem.build(distanceSunEarth,distanceEarthMoon);
        EcsEntity sun = EcsHelper.findEntitiesByName("Sun").get(0);
        assertNotNull(sun);
        EcsEntity earth = EcsHelper.findEntitiesByName("Earth").get(0);
        assertNotNull(earth);
        EcsEntity moon = EcsHelper.findEntitiesByName("Moon").get(0);
        assertNotNull(moon);

        EcsGroup group = earth.getGroup("GraphMovingComponent,VelocityComponent");
        graphMovingSystem.update(earth,group,0);
        group = moon.getGroup("GraphMovingComponent,VelocityComponent");
        graphMovingSystem.update(moon,group,0);
        //initial position of graph isType (0, 1, 0)
        Vector3 position = earth.scenenode.getTransform().getPosition();
        TestUtils.assertVector3(new Vector3(0, distanceSunEarth, 0),position,"initialEarthPos");
        position = moon.scenenode.getTransform().getPosition();
        TestUtils.assertVector3(new Vector3(0, distanceEarthMoon, 0),position,"initialMoonPos");

        //earth sollte durch den Graph doch keine Rotation bekommen. Naja, irgendwann mal schon.
        Quaternion earthRotation = earth.getSceneNode().getTransform().getRotation();
        TestUtils.assertQuaternion(new Quaternion(),earthRotation);
        position = moon.getSceneNode().getTransform().getWorldModelMatrix().extractPosition();
        TestUtils.assertVector3(new Vector3(0, (distanceEarthMoon+distanceSunEarth), 0),position,"initialMoonWorlPos");

        System.out.println("done");
    }


    /**
     * In y beginnend CCW mit 30Grad Offset
     */
    @Test
    public void testOrbitGraph() {
        double radius = WorldGlobal.DISTANCEMOONEARTH;
        double umfang = 2 * (float) (Math.PI * radius);
        double halbumfang = (float) (Math.PI * radius);
        double umfang4 = umfang / 4;
        double umfang12 = umfang / 12;
        // ein 30 Gradwinkel
        double sinPi6 = Math.sin(Math.PI/6);
        double cosPi6 = Math.cos(Math.PI/6);

        Graph orbit = SolarSystem.buildGlobalOrbitGraph(radius);
        GraphEdge edge0 = orbit.getEdge(0);

        Assertions.assertEquals(  4, orbit.getNodeCount(),"orbit.nodes");
        Assertions.assertEquals(  4, orbit.getEdgeCount(),"orbit.nodes");

        GraphMovingComponent gmc = new GraphMovingComponent();
        gmc.setGraph(orbit, new GraphPosition(edge0), null);
        LocalTransform posrot = GraphMovingSystem.getPosRot(gmc, null);
        TestUtils.assertVector3( new Vector3(0, WorldGlobal.DISTANCEMOONEARTH, 0), posrot.position,"start position");
        gmc.moveForward(umfang12);
        TestUtils.assertVector3( new Vector3(-sinPi6*WorldGlobal.DISTANCEMOONEARTH, cosPi6*WorldGlobal.DISTANCEMOONEARTH, 0), GraphMovingSystem.getPosRot(gmc, null).position,"position");
        gmc.moveForward(umfang4);
        TestUtils.assertVector3( new Vector3(-cosPi6*WorldGlobal.DISTANCEMOONEARTH, -sinPi6*WorldGlobal.DISTANCEMOONEARTH, 0), GraphMovingSystem.getPosRot(gmc, null).position,"position");
        gmc.moveForward(umfang4+umfang4);
        TestUtils.assertVector3( new Vector3(cosPi6*WorldGlobal.DISTANCEMOONEARTH, sinPi6*WorldGlobal.DISTANCEMOONEARTH, 0), GraphMovingSystem.getPosRot(gmc, null).position,"position");
        //jetzt wider auf den ersten Step
        gmc.moveForward(umfang4);
        TestUtils.assertVector3( new Vector3(-sinPi6*WorldGlobal.DISTANCEMOONEARTH, cosPi6*WorldGlobal.DISTANCEMOONEARTH, 0), GraphMovingSystem.getPosRot(gmc, null).position,"position");


    }

}
