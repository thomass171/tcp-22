package de.yard.threed.traffic;

import de.yard.threed.core.Vector3;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.VelocityComponent;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphFactory;
import de.yard.threed.graph.GraphMovingComponent;
import de.yard.threed.graph.GraphOrientation;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.graph.OrbitRotationProvider;


/**
 * Solange es in VR kein multipass view gibt, brauche ich echte Diemnsionen. Damit double nicht überläuft,
 * das System Erde-Sonne drastisch down skalieren. Solange es dazwischen keine Planeten gibt, dürfte
 * das gehen.
 * Die far plane muss auch weiter weg. Das könnte z-Buffer Probleme geben.
 * <p>
 * Erde-Monde bleibt aber original wegen multiplayer.
 */
public class SolarSystem {
    public SolarSystem() {

    }

    public SceneNode build(double distanceSunEarth, double distanceEarthMoon) {
        //Das ist doch eigentlich eine Hierarchie aus Config.
        //Einstieg erstmal mit Earth an 0,0,0 und Moon im Orbit. Earth uebergross zur Verdeckung der "normalen".
        //Und erstmal ohne Config
        //3.1.20 Besser dirket Sonne-Erde-Mond um die Handhabbarkeit der Dimensionen zu checken.
        TravelSphere sphere = new TravelSphere("Solar System"/*"Earth"*/);

        double moonRadius = WorldGlobal.MOONRADIUS;

        //Graph earthorbit= GraphFactory.buildCircle(WorldGlobal.km(18000)/*WorldGlobal.DISTANCEMOONEARTH*/);
        EcsEntity sun = sphere.buildSphere(moonRadius, "Sun", "images/Dangast.jpg");

        EcsEntity earth = sphere.buildSphere(WorldGlobal.EARTHRADIUS + WorldGlobal.km(900), "Earth", "images/Dangast.jpg");
        sun.scenenode.attach(earth.scenenode);
        double speed = TravelSphere.calculateSpeedFromRadius(distanceSunEarth, 60);
        buildOrbiter(earth, distanceSunEarth, speed);

        boolean withMoon = true;
        if (withMoon) {
            EcsEntity moon = sphere.buildSphere(moonRadius, "Moon", "images/Dangast.jpg");
            //moon.scenenode.getTransform().setPosition(new Vector3(0,WorldGlobal.DISTANCEMOONEARTH,0));
            earth.scenenode.attach(moon.scenenode);
            speed = TravelSphere.calculateSpeedFromRadius(distanceEarthMoon, 15);
            buildOrbiter(moon, distanceEarthMoon, speed);
        }

        //TravelSphere.add(sphere);
        //TravelSphere.add(new TravelSphere("Moon"));
        return sun.scenenode;
    }

    private void buildOrbiter(EcsEntity ecsEntity, double radius, double speed) {
        GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(ecsEntity);
        Graph orbit = buildGlobalOrbitGraph(radius);
        GraphPosition startPosition = new GraphPosition(orbit.getEdge(0));
        gmc.setGraph(orbit, startPosition, null);
        gmc.rotationProvider = new OrbitRotationProvider();
        VelocityComponent vc = VelocityComponent.getVelocityComponent(ecsEntity);
        vc.setMovementSpeed(speed);
        vc.setMaximumSpeed(speed);
        vc.setAcceleration(null);

        //-90,0,-90
        //-90,-90,0
        //90,0,0
        //alle Kombis probiert. Und nu ???
        //ecsEntity.getSceneNode().getTransform().setRotation( Quaternion.buildFromAngles(new Degree(0),new Degree(0),new Degree(0)));

        gmc.setAutomove(true);
    }


    /**
     * In z0 CCW beginnend in (0,radius,0);
     * @param radius
     * @return
     */
    public static Graph buildGlobalOrbitGraph(double radius) {
        Graph graph = new Graph(GraphOrientation.buildForZ0());

        Vector3 starte1=new Vector3(0, radius, 0);
        GraphFactory.addZ0Circle(graph, starte1,"global");
        return graph;
    }

    /**
     * TODO does not belong here; its no real orbit.
     *
     * @param graph
     * @param radius
     * @param starte1
     */
    public static void continueLocalOrbitGraph(Graph graph, double radius, Vector3 starte1) {
        GraphFactory.addZ0Circle(graph, starte1,"local");
    }

    /**
     * starte1 muss in z0 sein!
     *
     * @param starte1
     * @return
     */
    public static Graph buildLocalOrbitGraph(  Vector3 starte1) {
        Graph graph = new Graph(GraphOrientation.buildForZ0());
        GraphFactory.addZ0Circle(graph, starte1,"local");
        graph.setName("localOrbit");
        return graph;
    }
}
