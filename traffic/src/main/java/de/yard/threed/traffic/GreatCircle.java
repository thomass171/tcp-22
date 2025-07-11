package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.graph.Graph;
import de.yard.threed.graph.GraphOrientation;
import de.yard.threed.engine.util.CircleRotator;
import de.yard.threed.trafficcore.EllipsoidCalculations;

import java.util.List;

/**
 * Orbit in Earth/FG coordinate system. (Grosskreis)
 * Höhe ergibt sich aus der entry altitude.
 *
 * 12.4.20: Umbenannt EarthOrbit->GreatCircle
 *
 * Skizze 16
 * 
 * Created on 11.11.18.
 */
public class GreatCircle {
    public Vector3 entry;
    // normal vector to the orbits plane
    public Vector3 n;

    public static GreatCircle fromDG(GeoCoordinate entry, Degree heading, EllipsoidCalculations rbcp) {
        //RoundBodyConversions rbcp = TrafficHelper.getRoundBodyConversionsProviderByDataprovider();
        return new GreatCircle(rbcp.toCart(entry,null,null),
                rbcp.toCart(GeoCoordinate.fromLatLon(rbcp.applyCourseDistance(entry,heading,1000),0),null,null).subtract(rbcp.toCart(entry,null,null)));
    }
    
    public GreatCircle (Vector3 entry, Vector3 entrydirection){
        this.entry = entry;
        n = Vector3.getCrossProduct(entry.normalize(),entrydirection.normalize());
    }

    /**
     * Die Anzahl der Segmente muss/kann in Abhängigkeit vom smoothingradis (EARTHradius+entryaltitude) berechnet werden, damit
     * die smooth begin/end dicht zusammen liegen, ohne sich aber zu ueberlappen. Knifflige Rechnung. TODO
     * @param segments
     * @return
     */
    public Graph getGraph(int segments){
        Graph graph = new Graph(GraphOrientation.buildForFG());
        addToGraph(graph,segments,false);
        return graph;
    }

    private void addToGraph(Graph graph, int segments, boolean stopAtEquator) {
        //normal negate for have vectors in order of direction? No.
        List<Vector3> rotateresult = CircleRotator.buildArcByrotate(entry,n,segments,false);
        Vector3 start = rotateresult.get(0);
        int removeIndex=-1;
        if (stopAtEquator) {
            for (int i = 0; i < rotateresult.size(); i++) {
                Vector3 v3 = rotateresult.get(i);
                if ((start.getZ() > 0 && v3.getZ() < 0)||
                        (start.getZ() < 0 && v3.getZ() > 0)) {
                    removeIndex=i;
                    break;

                }
            }
        }
        if (removeIndex!=-1){
            rotateresult = rotateresult.subList(0,removeIndex);
        }
        graph.extendFromVectorList(rotateresult);
        //close graph
        graph.connectNodes(graph.getNode(graph.getNodeCount()-1),graph.getNode(0));

    }

    public Graph getGraphToEquator(int segments) {
        Graph graph = new Graph(GraphOrientation.buildForFG());
        addToGraph(graph,segments,true);
        return graph;
    }
}
