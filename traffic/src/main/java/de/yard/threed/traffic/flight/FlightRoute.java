package de.yard.threed.traffic.flight;

import de.yard.threed.core.Vector3;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphNode;
import de.yard.threed.graph.GraphPath;
import de.yard.threed.graph.GraphPathConstraintProvider;
import de.yard.threed.graph.GraphPosition;
import de.yard.threed.graph.GraphUtils;
import de.yard.threed.traffic.Destination;
import de.yard.threed.traffic.EllipsoidCalculations;
import de.yard.threed.traffic.WorldGlobal;
import de.yard.threed.traffic.geodesy.MapProjection;
import de.yard.threed.traffic.geodesy.SimpleMapProjection;

/**
 * Ein Graph mit GraphPath von einer Takeoff Runway (quasi erste Edge des Graph)
 * bis zu
 * - einer landing runway (quasi letzte Edge des Graph)
 * - einem Holding, wenn die landing runway noch nicht bekannt ist.
 *
 * 29.2.2020: Obwohl es doch nur ein Graph ist, oder?
 * Es muesste noch vermerkt werden, wo das Ende des Graph ist (Runway,Holding,Orbit) um zu wissen, ob er noch fortgesetzt werden muss, z.B.
 * weil es für die Destination noch keine Elevation gibt.
 * <p>
 * Created on 21.11.18.
 */
public class FlightRoute {
    public Destination nextDestination;
    Graph graph;
    GraphPath path;
    GraphPath smoothedpath;
    //edge with from on runway
    public GraphEdge takeoffedge;
    //edge with to on runway
    GraphEdge touchdownedge;

    /**
     * Smoothen muss extra gemacht werden. Damit bleibt er prinzipiell projectbar für 2D.
     *
     * @param graph
     * @param takeoffedge
     * @param touchdownedge
     */
    public FlightRoute(Graph graph, GraphEdge takeoffedge, GraphEdge touchdownedge) {
        this.graph = graph;
        this.takeoffedge = takeoffedge;
        this.touchdownedge = touchdownedge;
        int layer = 1;
        path = GraphPath.buildFromNode(graph.getNode(0), layer);
    }

    public void smooth() {
        int layer = 1;
        GraphEdge startedge = graph.getEdge(0);
        //GraphPosition start = new GraphPosition(v1edge);
        GraphNode to = graph.getEdge(graph.getEdgeCount() - 1).getTo();

        boolean dosmooth = true;
        if (dosmooth) {

            double minimumlen = 0;
            GraphPathConstraintProvider graphPathConstraintProvider = new FlightRouteGraphPathConstraintProvider(this);

            // Das Smoothing erfolgt ohne "currentedge" direkt ab erste node als next node, so dass das Vehicle passend einfahren muss.
            smoothedpath = GraphUtils.createPathFromGraphPositionAndPath(graph, path, startedge.from, null, to, graphPathConstraintProvider, layer, true, false, null);
            // start position muss im Path gesetzt werden, weil ja ein Graphwechsel geschieht. Aber auf die smoothedge, nicht die Original.
            smoothedpath.startposition = new GraphPosition(smoothedpath.getSegment(0).edge);
        }
    }

    public double getGroundDistance(GraphNode graphNode) {
        return Vector3.getDistance(graph.getNode(0).getLocation(), graphNode.getLocation());
    }


    public Graph getGraph() {
        return graph;
    }

    public GraphPath getPath() {
        return (smoothedpath != null) ? smoothedpath : path;
    }

    public void projectGraph(MapProjection projection, EllipsoidCalculations rbcp ) {

        SimpleMapProjection.projectGraph(graph, projection,rbcp);
    }

    public boolean isSmoothed() {
        return smoothedpath!=null;
    }
}

class FlightRouteGraphPathConstraintProvider implements GraphPathConstraintProvider {
    FlightRoute flightRoute;
    // wegen des speed ist der Radius sehr gross. 
    double smoothingradius = 100;

    FlightRouteGraphPathConstraintProvider(FlightRoute flightRoute) {
        this.flightRoute = flightRoute;
    }

    @Override
    public double getMinimumLength() {
        //einfach mal so
        return 400;
    }

    @Override
    public double getSmoothingRadius(GraphNode graphNode) {
        Vector3 loc = graphNode.getLocation();
        //SGGeod g = SGGeod.fromCart(loc);
        //alles noch nicht ausgegoren
        if (flightRoute.getGroundDistance(graphNode) < 1000) {
            return 50;
        }
        if (flightRoute.getGroundDistance(graphNode) < 10000) {
            return smoothingradius;
        }
        //erstmal nur so 90km Orbit annehmen. Hmm knifflig. 100km geht noch, 1000 schon nicht mehr. TODO
        //return WorldGlobal.EARTHRADIUS+90000;
        return WorldGlobal.km(100);
    }
};

