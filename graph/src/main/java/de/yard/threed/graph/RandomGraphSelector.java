package de.yard.threed.graph;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.util.IntProvider;

/**
 * Created by thomass on 20.12.16.
 */
public class RandomGraphSelector implements GraphSelector {
    private Log logger = Platform.getInstance().getLog(RandomGraphSelector.class);
    IntProvider rand;

    public RandomGraphSelector(IntProvider rand) {
        this.rand = rand;
    }

    /**
     * zufällig eine der Edges an einer Node liefern. "to" und "from" gleichberechtigt. Die aktuelle aber nur liefern, wenn es keine andere gibt. Oder besser nie.
     * 18.7.17: Ob nach der Umstellung auf Returntyp GrapgPathSegment die enternode stimmt, ist unklar.
     */
    @Override
    public GraphPathSegment findNextEdgeAtNode(GraphEdge incomingedge, GraphNode n) {
        GraphEdge newedge;

        if (n.edges.size() == 0) {
            logger.warn("no edges: inconsistent graph?");
            return null;
        }
        if (n.edges.size() == 1) {
            // kann nur die eigene sein
            return null;
        }
        // so eine Schleife ist aber nicht sehr effizient.
        int abortcounter = 0;
        do {
            if (abortcounter++ > 2000) {
                return new GraphPathSegment(n.edges.get(0),n.edges.get(0).from);
            }
            int nextint = rand.nextInt();
            int next = nextint % n.edges.size();
            //logger.debug("nextint=" + nextint + ",next=" + next);
            newedge = n.edges.get(next);
        } while (newedge == incomingedge);
        return  new GraphPathSegment(newedge,newedge.from);

    }
}
