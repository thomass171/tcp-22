package de.yard.threed.graph;

import de.yard.threed.core.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * 28.10.18: Rename, weil nicht unbedingt railing spezifisch.
 * 
 * Created by thomass on 20.12.16.
 */
public class RailingBranchSelector implements GraphSelector {
    /**
     * An der Node muss es drei edges geben, das hat der Validator sichergestellt.
     * NeeNee, das gilt nur fuer Branches. Der Selector wird aber an jeder Node aufgerufen.
     * Aber mehr als 3 geht nicht.
     * 
     * @param incomingedge
     * @param node
     * @return
     */
    @Override
    public GraphPathSegment findNextEdgeAtNode(GraphEdge incomingedge, GraphNode node) {
        // nur sicherheitshalber pruefen.
        if (node.edges.size() > 3) {
            throw new RuntimeException("> 3");
        }
        //29.3.17: getEffectiveDirectionAtNode hat jetzt wieder den revert. Passt das hier dann noch?
        //wahrscheinlich nicht, die Lok bleibt stehen. Und Tests scheitern. Wieder zurueck. Ob das so das Wahre ist?
        Vector3 effectiveincomingdir = incomingedge.getEffectiveInboundDirection(node);
        List<GraphEdge> candidates = new ArrayList<GraphEdge>();
        for (int i = 0; i < node.edges.size(); i++) {
            GraphEdge e = node.edges.get(i);
            //Nicht auf die Node gehen, ueber die ich reingekommen bin
            if (e != incomingedge) {
                Vector3 effectivedir = e.getEffectiveOutboundDirection(node);
                /*float dot = Vector3.getDotProduct(effectiveincomingdir, effectivedir);
                if (dot >= -0.000001f) {
                    // dann geht es mit bis zu 90 Grad in dieselbe Richtung. Damit auch Halbkreise gelaufen werden. Groesser als Halbkreis geht nicht. TODO Das muss in den Validator.
                    // 19.4. was soll das heissen? Wieso 90 Grad um Halbkreis. Das koennte ueberholt sein. Darum jetzt ueber angle
                    candidates.add(e);
                }*/
                double angle = Vector3.getAngleBetween(effectiveincomingdir, effectivedir);
                if (angle < 0.001f) {
                    // das betrachten wir mal als quais geradeaus. Railing hat doch hoffentlich wohl nur arcs an den branches.
                    candidates.add(e);
                }
            }
           
        }
        if (candidates.size()==0){
            return null;
        }
        //18.7.17: Stimmt die enternode wohl?
        return new GraphPathSegment(candidates.get(0),candidates.get(0).from);
    }
    
    
}
