package de.yard.threed.core;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Eine 2D Variante des GraphOrientation.getOutline() und damit hoffentlich deutlich einfacher.
 * Ist auch nicht graphbasiert und muss deswegen auch keine arcs berücksichtigen.
 * <p>
 * Liefert die Liste der NodePunkte ab hier bei offset=0, sonst eine etwas versetzte Linie links (offset negativ) oder rechts (offset positiv) davon.
 * Das duerfte aber relativ sein.
 * 5.4.19: Die Nutzung von outline ist nur bei bestimmten lines möglich, man denke nur an U und V Shapes. Das ist eine Prinzipfrage. Allerdings
 * dürfte das schwer zuverlässig erkennbar sein. Offen, ob es geprüft werden sollte und ob dann null geliefert wird.
 */
public class OutlineBuilder {
    static Log logger = Platform.getInstance().getLog(OutlineBuilder.class);

    public static List<Vector2> getOutline(List<Vector2> path, double offset) {

        List<Vector2> line = new ArrayList<Vector2>();
        if (path.size() == 0) {
            return line;
        }

        Vector2 startnode, node;
        int idx = 0;

        idx++;
        startnode = path.get(0);
        node = path.get(1);
        // erster Punkt
        Quaternion rotation;
        //Vector2 dir = nextnode.subtract(startnode).normalize();

        Vector2 outpoint = getOutlinePoint(null, startnode, node, offset);
        line.add(outpoint);

        idx = 2;
        while (node != null) {
            Vector2 nextedge = null;
            if (idx < path.size()) {
                nextedge = path.get(idx);
                idx++;
            }
            if (nextedge != null) {
                /*verschoben nach unten nextdir = nextedge.subtract(nextnode).normalize();
                Vector2 effectivedir = dir.add(nextdir).normalize();
                Vector2 outlinepoint =  getEndOutlinePoint(nextnode,effectivedir,offset);*/
                Vector2 outlinepoint = getOutlinePoint(startnode, node, nextedge, offset);
                line.add(outlinepoint);
                //logger.debug("outline at " + nextnode + "("+idx+") is " + outlinepoint + " with effectivedir " + effectivedir);
            } else {
                // letzter Punkt
                outpoint = getOutlinePoint(startnode, node, null, offset);
                line.add(outpoint);
                break;
            }
            // naechsten Schritt vorbereiten.
            startnode = node;
            node = nextedge;
        }
        return line;
    }

    /**
     * Den Outlinepunkt an einer node ermitteln.
     * Vector2 sind hier just Coordinates.
     */
    public static Vector2 getOutlinePoint(Vector2 from, Vector2 nextnode/*node*/, Vector2 nextedge/*to*/, double offset) {
        Vector2 effectivedir;
        if (from == null) {
            effectivedir = nextedge.subtract(nextnode).normalize();
        } else {
            if (nextedge == null) {
                effectivedir = nextnode.subtract(from).normalize();
            } else {

                Vector2 dir = nextnode.subtract(from).normalize();
                Vector2 nextdir = nextedge.subtract(nextnode).normalize();
                effectivedir = dir.add(nextdir).normalize();
            }
        }
        Vector2 outlinepoint = getEndOutlinePoint(nextnode, effectivedir, offset);
        return outlinepoint;
    }

    /**
     * Outline Point an einer node für inbound/outbound direction.
     *  links (offset negativ) oder rechts (offset positiv)
     */
    public static Vector2 getOutlinePointFromDirections(Vector2 dir0, Vector2 node, Vector2 dir1, double offset) {
        Vector2 effectivedir;
        effectivedir = dir0.normalize().add(dir1.normalize()).normalize();
        Vector2 outlinepoint = getEndOutlinePoint(node, effectivedir, offset);
        return outlinepoint;
    }

    /**
     * public fuer Test
     */
    public static Vector2 getEndOutlinePoint(Vector2 node, /*GraphEdge edge,*/ Vector2 dirOfEdge, double offset) {
        Vector2 outv = new Vector2(0, offset);
        return node.add(dirOfEdge.normalize().rotate(new Degree(-90)).scale(offset));
    }

}
