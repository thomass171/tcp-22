package de.yard.threed.graph;

import de.yard.threed.core.Degree;
import de.yard.threed.core.MathUtil;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Ich weiss erstmal keinen besseren Namen.
 * Enthaelt auch den fuer den Graphen gültigen backreference Vector.
 * 24.4.18: GraphRotation hier aufgenommen
 * 22.2.2020:Es ist ein konzeptionelles Problem, dass der Graph dafür sorgen soll, dass Vehicle richtig stehen. Dann gehen nested Graphs(Orbit) nicht.
 * Es müsste aber doch gehen, dass ein EDDK Vehicle von einer Kugel umrundet wird.
 * Ich fürchte, diese Klasse ist krude.
 *
 * <p>
 * Created on 16.03.18.
 */
public abstract class GraphOrientation {
    Log logger = Platform.getInstance().getLog(GraphOrientation.class);

    public GraphOrientation() {
    }

    /**
     * different eg. for earth and moon
     */
    //public abstract Quaternion getBaseRotation(Vector3 location);

    /**
     * Basis Rotation in Bezug auf ein Vehicle?
     * Nee, das waere zu speziell, weil es ja auch fuer Outline sein soll. Oder doch, und Outline muss angepasst werden.
     * Aber es kann die Rotation fuer ein -z Vehicle sein. Das ist abwaerttskompatibel und passt zu referenceback.
     * Ist nur fuer FG erforderlich? Warum auch immer.
     * 19.2.2020 Fuer einen universellen 3D graph muss das aber mal klar definiert werden.
     * 22.2.2020:Es ist ein konzeptionelles Problem, dass der Graph dafür sorgen soll, dass Vehicle richtig stehen. Dann gehen nested Graphs(Orbit) nicht.
     * Es müsste aber doch gehen, dass ein EDDK Vehicle von einer Kugel umrundet wird.
     *
     * @return
     */
    public abstract Quaternion getForwardRotation();

    /*public abstract Vector3 getReferenceBack(Vector3 location);*/

    public static GraphOrientation buildForZ0() {
        return new GraphOrientationZ0();
    }

    public static GraphOrientation buildForY0() {
        return new GraphOrientationY0()/*Default()*/;
    }

    public static GraphOrientation buildDefault() {
        return new GraphOrientationY0()/*Default()*/;
    }

    public static GraphOrientation buildForFG() {
        return new GraphOrientationFG();
    }

    /**
     * 29.3.18 Der upVector kann/muss abhaengig von der Edge sein.
     *
     * @return
     */
    public abstract Vector3 getUpVector(GraphEdge edge);

    public abstract String getName();

    /**
     * 14.12.16: Die Richtung der Kante steht ja fest, aber für die Ermittlung der kompletten Rotation brauchts auch einen up-Vektor.
     * 19.04.17: Aus GraphEdge hier hin, denn die Rotation ist keine Angelegenheit des Graphen. Erstmal static (fuer Tests) bis das rund ist.
     * 02.05.17: Immer einen upVector annehmen. Damit keinen referenceback mehr.(??) Eine Edge in Richtung (0, 0, -1) mit upvector nach y führt zur Identityrotation.
     * Das ist dann quasi eine Art Defaultrotation des Graphen. Hmm, das mit dem upvector in 3D ist aber fraglich, oder?
     * 15.3.18: Weil es doch Sache des Graphen ist, zumindets in weiten Teilen, wider zurück nach GraphEdge? Naja. wird aber nur bei Movement gebraucht.
     * Bevor ich das nochmal dahin verschieben will: Die Orientierung eines Vehicle auf einem Graph ist KEINE Graphfunktionalität, bestenfalls
     * eines GraphPath.
     * 29.3.18: Fuer Edges
     * ausserhalb der plane Ebene hat mann dann aber immer noch keine definierte Rotation, z.B. edges
     * parallel zum upVector.
     * 29.3.18: Hier muss doch ein upVector mit rein. Dann lass ich das auch mit der base/edgelocal Rotation. Ich glaube,
     * den referenceback brauche ich dann nicht mehr? Diese Rotation hat als Referenz dann, dass ein Vehicle
     * richtig steht. Und kann man das dann noch fuer Outline nehmen? Ich glaube nicht.
     * 22.2.2020: Hier stimmt das Konzept mit Vehicle einfach nicht. Bzw. diese Methode ist nur für spezielle Vehicles geeignet. Abstrahiert über RotationProvider.
     * Für outline sollte es dann aber doch eher nicht (mehr) genutzt werden.
     *
     * @return
     */
    public Quaternion get3DRotation(boolean reverseorientation, Vector3 effectivedirection, GraphEdge edge/*, GraphRotation orientation*/) {
        //logger.debug("get3DRotation: edgeposition=" + edgeposition + ",reverseorientation="+reverseorientation+",effectivedirection="+effectivedirection);

        if (reverseorientation) {
            effectivedirection = effectivedirection.negate();
        }

        //10.5.18 boolean useup = true;
        //if (useup) {
        //Quaternion forwardrotation = new Quaternion(new Degree(0),new Degree(90),new Degree(0));
        //effectivedirection = effectivedirection.rotate(forwardrotation);
        Quaternion forwardrotation = this/*baseRotation*/.getForwardRotation();

        Vector3 up = this/*baseRotation*/.getUpVector(edge);
        //Quaternion uprotation = new Quaternion(new Degree(0),new Degree(90),new Degree(0));
        //up = up.rotate(uprotation);
        // effectivedirection = effectivedirection.rotate(baserotation);
        Quaternion rotation = Quaternion.buildLookRotation(effectivedirection.negate(), up);
        Quaternion localr = new Quaternion();
        //return baser.multiply(edger).multiply(localr);
        //16.3.18 Reihenfolge gefaellt mir so besser:19.2.20: Aber ist das auch richtig? mal anders rum versuchen. Damit stimmt SolarSystem dann. Ich blick nicht mehr durch.
        return localr.multiply(rotation).multiply(forwardrotation);
        //return forwardrotation.multiply(localr.multiply(rotation));

        //return localr.multiply(rotation);
        /*} else {
            //from zu verwenden ist aber nicht ganz sauber. to waere aber auch nicht besser
            Quaternion baser = new Quaternion();

            baser = this.getBaseRotation(edge.from.getLocation());

            // Es mahct kaienen Sinn das in die edge zu verschieben, weil es keine lokale Rotation gibt.
            Quaternion edger = null;
            edger = getLocal3DRotation(edge, effectivedirection);//Quaternion.buildQuaternion(getLocalReferenceBack(edge.from.getLocation()), effectivedirection);
            //edger =     edge.get3DRotation(edgeposition,reverseorientation);
//edger=new Quaternion();
            // TODO in Edge? graphRotation.getLocalRotation(0); local vielleicht nicht hier, weil outline das auch verwendet.
            Quaternion localr = new Quaternion();
            //return baser.multiply(edger).multiply(localr);
            //16.3.18 Reihenfolge gefaellt mir so besser:
            return localr.multiply(edger).multiply(baser);
        }*/
    }

    /**
     * Liefert die Rotation an der Stelle im "graph space" (obwohl der Begriff fragwürdig ist?), also ohne Beruecksichtigung einer baserotation des Graph.
     * Je nach dem aus Sicht from->to oder reverse.
     * Die lokale Rotattion muss immer mit der referecne in baserotation gemacht werden!
     *
     * @return
     */
    /*10.5.18 public Quaternion getLocal3DRotation(GraphEdge edge, Vector3 effectivedirection) {
        Vector3 rb = this.getReferenceBack(edge.from.getLocation());
        return Quaternion.buildQuaternion(rb, effectivedirection);
    }*/

    /**
     * Eine Outline entlang eines Graphen erzeugen.
     * Liefert die Liste der NodePunkte ab hier bei offset=0, sonst eine etwas versetzte Linie links (offset negativ) oder rechts (offset positiv) davon.
     * Das duerfte aber relativ sein.
     * Geht (erstmal) nicht mit Verzweigungen und nur in 2D. In 3D bräuchte man einen upVector? Es wird von y=0 ausgegangen.
     * Geht auch noch nicht mit Bögen.
     * Berechnungen sind aber in 3D.
     * Wenn es keine edge gibt, wird ueberhaupt kein Punkt geliefert, weil keine Orinetierung berechnet werden kann.
     * Es kann bei zu grossem offset bzw zu kleinem nodeabstand oder zu starken Kruemmungen die Linepunkte "die falsche Reihenfolge" haben.
     * 7.2.18: Wenn auf einen Abzweiger (Knoten mit mehr als einem Nachfolger) getroffen wird, wird abgebrochen. Das scheint schlüssig.
     * 9.2.18: Jetzt auch für einfache Bögen.
     * 16.3.18: Aus GraphNode hierhin verschoben, um den Graph von der Orientierung zu entkoppeln.
     * 10.4.18: Andere Signatur: Jetzt kommt hier ein Path bzw. der Kern davon rein. Das macht es vielleicht etwas eingaengiger. Mit layer laesst sich auf ein LAyer beschraenken (-1 fuer alle).
     * 30.8.18: Flexibler, z.B. über interface (GraphPathSegment->LineSegment) geht das nur schwer, weil arcs auch behandelt werden, zumindest rusimentär.
     * 04.04.19: Es gibt jetzt auch eine 2D Variante (OutlineBuilder)
     *
     * @param offset
     * @return
     */
    public List<Vector3> getOutline(List<GraphPathSegment> path, double offset, int layer) {
        GraphNode lastnode;
        GraphEdge edge;
        if (path != null && path.size() == 1 && path.get(0).edge.getArc() != null) {
            GraphPathSegment arcsegment = path.get(0);
            edge = arcsegment.edge;
            lastnode = arcsegment.getLeaveNode();
            //9.2.18: Reichlich Sonderlocke fuer einfachen arc. Geht nur mit neuer Definition.
            if (lastnode != null && edge.getArc() != null) {
                return buildArcOutline(edge, offset, 16, arcsegment.getEnterNode().equals(edge.getTo()));
            }
        }

        List<Vector3> line = new ArrayList<Vector3>();
        if (path.size() == 0) {
            return line;
        }

        GraphNode startnode;
        int idx = 0;

        edge = path.get(0).edge;
        idx++;
        startnode = path.get(0).enternode;

        // erster Punkt
        Quaternion rotation;
        Vector3 dir = edge.getEffectiveOutboundDirection(startnode);

        double offsettouse = getOffsetToUse(edge, offset, layer);
        Vector3 outpoint = getEndOutlinePoint(startnode, edge, dir, offsettouse);
        line.add(outpoint);


        while (edge != null) {
            //7.2.18: Nicht immer auf to gehen.
            GraphNode nextnode = edge.getOppositeNode(startnode);
            // TODO: besserer Notaus
            if (nextnode == null/*basenode*/) {
                //circle?
                break;
            }
            if (path != null && idx > path.size()) {
                break;
            }
            // Winkel aus letztem dir und aktuellem

            Vector3 nextdir = null;
            GraphEdge nextedge = null;
            if (idx < path.size()) {
                nextedge = path.get(idx).edge;
                idx++;
            }
            if (nextedge != null) {
                //13.4.18: nextdir ist doch voellig falsch!
                //nextdir = edge.getEffectiveBeginDirection();
                nextdir = nextedge.getEffectiveOutboundDirection(nextnode);
                Degree angle = Degree.buildFromRadians((float) Vector3.getAngleBetween(dir, nextdir) / 2);
                Vector3 kp = null;
                if (angle.getDegree() > 0.05f) {
                    kp = Vector3.getCrossProduct(dir, nextdir).normalize();
                }
                //angle = new Degree(angle.getDegree() - 90);

                if (MathUtil.mathvalidate) {
                    if (!nextdir.isValid()) {
                        throw new RuntimeException("NaN in nextdir");
                    }
                    if (!dir.isValid()) {
                        throw new RuntimeException("NaN in dir");
                    }
                    if (Float.isNaN((float) angle.getDegree())) {
                        throw new RuntimeException("NaN in angle.degree");
                    }
                }

                // Wenn es eine Layereinschraenkung gibt, echten outline nur erstellen, wenn beide Edges zu dem Layer gehören.
                offsettouse = offset;
                if (layer != -1 && (edge.getLayer() != layer || nextedge.getLayer() != layer)) {
                    offsettouse = 0;
                }
                //11.4.18: Um den Upvector der GraphRotation rotieren. Oder besser das Kreuzprodukt wegen Winkel > 180.
                //Ausser bei kleinen Winkeln. Dann doch per up. 
                Vector3 offsetv;
                if (kp == null) {
                    //dir/nextdir parallel
                    rotation = Quaternion.buildQuaternionFromAngleAxis(angle, this.getUpVector(edge));
                    offsetv = Vector3.getCrossProduct(dir, this.getUpVector(edge));
                } else {
                    rotation = Quaternion.buildQuaternionFromAngleAxis(angle, kp);
                    //offsetv = Vector3.getCrossProduct(dir, kp);
                    offsetv = Vector3.getCrossProduct(dir, this.getUpVector(edge));
                }
                offsetv = offsetv.normalize().multiply(offsettouse);
                Vector3 outlinepoint = nextnode.getLocation().add(offsetv.rotate(rotation));
                if (MathUtil.mathvalidate) {
                    if (!outlinepoint.isValid()) {
                        throw new RuntimeException("NaN in outlinepoint");
                    }
                }
                line.add(outlinepoint);
                //logger.debug("outline at " + nextnode.getLocation() + " isType " + outlinepoint + " with angle " + angle);
            } else {
                // letzter Punkt
                dir = edge.getEffectiveInboundDirection(nextnode);
                offsettouse = getOffsetToUse(edge, offset, layer);
                outpoint = getEndOutlinePoint(nextnode, edge, dir, offsettouse);
                line.add(outpoint);
            }
            // naechsten Schritt vorbereiten.
            dir = nextdir;
            edge = nextedge;
            startnode = nextnode;
        }

        if (MathUtil.mathvalidate) {
            //validieren
            for (int i = 0; i < line.size(); i++) {
                Vector3 v = line.get(i);
                if (!v.isValid()) {
                    throw new RuntimeException("NaN in line.i=" + i);
                }
            }
        }
        return line;
    }

    private double getOffsetToUse(GraphEdge edge, double offset, int layer) {
        double offsettouse = offset;
        if (layer != -1 && edge.getLayer() != layer) {
            offsettouse = 0;
        }
        return offsettouse;
    }

    /**
     * offset positiv->rechts, zumindest in y0 Ebene.
     */
    public List<Vector3> getOutlineFromNode(GraphNode basenode, float offset) {
        GraphNode startnode = basenode;
        GraphEdge edge = startnode.getFirstFromEdge();
        List<GraphPathSegment> path = new ArrayList<GraphPathSegment>();
        path.add(new GraphPathSegment(edge, startnode));
        while (edge != null) {

            //7.2.18: Nicht immer auf to gehen.
            GraphNode nextnode = edge.getOppositeNode(startnode);
            // TODO: besserer Notaus
            if (nextnode == basenode) {
                //circle?
                break;
            }
            GraphEdge nextedge = null;
            List<GraphEdge> nextedges = nextnode.getEdgesExcept(edge);
            if (nextedges.size() == 1) {
                //nur wenn es genau eine gibt
                nextedge = nextedges.get(0);
                path.add(new GraphPathSegment(nextedge, nextnode));
            } else {
                // letzter Punkt
            }
            // naechsten Schritt vorbereiten.
            edge = nextedge;
            startnode = nextnode;
        }
        return getOutline(path, offset, -1);
    }

    /**
     * public fuer Test
     */
    public Vector3 getEndOutlinePoint(GraphNode node, GraphEdge edge, Vector3 dir, double offset) {
        //16.3.18: Edge in Referenceorientierung anlegen und dann "einfach" rotieren.
        Vector3 outv = new Vector3(offset, 0, 0);
        boolean reverseorientation = false;
        Quaternion rotation = get3DRotation(reverseorientation, dir, edge);
        return node.getLocation().add(outv.rotate(rotation));
    }

    List<Vector3> buildArcOutline(GraphEdge edge, double offset, int segments, boolean reverse) {
        GraphArc p = edge.getArc();
        List<Vector3> line = new ArrayList<Vector3>();

        Vector3 center = edge.getCenter();
        float step = 1f / segments;

        int index;
        for (int i = 0; i <= segments; i++) {
            index = i;
            if (reverse) {
                index = segments - i;
            }
            Vector3 v = p.getRotatedEx(index * step, 0);
            v = v.normalize().multiply(p.getRadius() - (reverse ? -1 : 1) * offset);
            v = center.add(v);
            line.add(v);
        }
        return line;
    }

    public static GraphOrientation buildByName(String orientation) {
        if (orientation.equals(GraphOrientationZ0.NAME)) {
            return buildForZ0();
        }
        if (orientation.equals(GraphOrientationY0.NAME)) {
            return buildForY0();
        }
        if (orientation.equals(GraphOrientationFG.NAME)) {
            return buildForFG();
        }
        throw new RuntimeException("unknown orientation " + orientation);
    }
}

/**
 * upVector ist dann (0,0,1)
 */
class GraphOrientationZ0 extends GraphOrientation {

    public static String NAME = "z0";

    /*@Override
    public Quaternion getBaseRotation(Vector3 location) {
        // um die x-Aches aufrichten. 29.3.18: Aber doch -90?
        return Quaternion.buildRotationX(new Degree(90));
    }*/

    @Override
    public Quaternion getForwardRotation() {
        // um die x-Aches aufrichten.
        // 19.2.20: Das ist deutlich plausibler als Identity, denn das muss ja falsch sein, weil gleich zu Default. Aber nicht -90?
        //Ich blick nicht mehr durch.
        //return Quaternion.buildRotationZ(new Degree(-90));
        return new Quaternion();
    }

    /*
    @Override
    public Vector3 getReferenceBack(Vector3 location) {
        // Als Referenzrotation lege ich einfach mal negativ z fest, so wie die Defaultblickrichtung der Camera.
        // d.h., wenn eine Kante entlang von z läuft, wird die Identityrotation geliefert.
        Vector3 referenceback = new Vector3(0, 0, -1);
//TODO nicht immer berechnen
        return referenceback.rotate(getBaseRotation(location));
    }*/

    @Override
    public Vector3 getUpVector(GraphEdge edge) {
        Vector3 up = new Vector3(0, 0, 1);
        //up.rotate(getBaseRotation());
        return up;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

/**
 * In der XZ Ebene.
 * upVector ist dann (0,1,0)
 */
class GraphOrientationY0/*Default*/ extends GraphOrientation {

    public static String NAME = "y0";

    /*@Override
    public Quaternion getBaseRotation(Vector3 position) {
        return new Quaternion();
    }
*/
    @Override
    public Quaternion getForwardRotation() {
        Quaternion rotation = new Quaternion();
        //rotation = new Quaternion(new Degree(0), new Degree(0), new Degree(0));
        return rotation;
    }

    /*@Override
    public Vector3 getReferenceBack(Vector3 position) {
        // Als Referenzrotation lege ich einfach mal negativ z fest, so wie die Defaultblickrichtung der Camera.
        // d.h., wenn eine Kante entlang von z läuft, wird die Identityrotation geliefert.
        Vector3 referenceback = new Vector3(0, 0, -1);
        return referenceback;
    }*/

    @Override
    public Vector3 getUpVector(GraphEdge edge) {
        return new Vector3(0, 1, 0);
    }

    @Override
    public String getName() {
        return NAME;
    }
}

/**
 * 22.2.2020: Das ist doch eigentlich ein universeller 3D Graph? Nee, der bezieht seinen up-Vektor von einem Mittelpunkt.
 */
class GraphOrientationFG extends GraphOrientation {

    public static String NAME = "fg";

   /* @Override
    public Quaternion getBaseRotation(Vector3 position) {
        return FlightLocation.buildRotation(SGGeod.fromCart(position));
    }*/

    @Override
    public Quaternion getForwardRotation() {
        Quaternion rotation = Quaternion.buildFromAngles(new Degree(180), new Degree(0), new Degree(-90));
        // Die Werte entstanden durch ausprobieren. :-) Vielleicht laesst sich das mal untermauern. TODO
        rotation = Quaternion.buildFromAngles(new Degree(-90), new Degree(-90), new Degree(0));
        return rotation;
    }

    /**
     * kann man das staendige Rechnen nicht optimieren? TODO
     *
     * @param location
     * @return
     */
    /*@Override
    public Vector3 getReferenceBack(Vector3 location) {
        Vector3 referenceback = FlightLocation.getNorthHeadingReference(SGGeod.fromCart(location));

        return referenceback;//.rotate(getBaseRotation(location));
    }*/

    /**
     * kann man das staendige Rechnen nicht optimieren? TODO
     *
     * @return
     */
    @Override
    public Vector3 getUpVector(GraphEdge edge) {
        Vector3 up = edge.from.getLocation()./*negate().*/normalize();
        Quaternion baserotation = new Quaternion();
        return up.rotate(baserotation);
    }

    @Override
    public String getName() {
        return NAME;
    }
}