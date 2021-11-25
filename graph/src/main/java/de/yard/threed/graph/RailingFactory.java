package de.yard.threed.graph;

import de.yard.threed.core.Degree;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.CustomGeometry;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.ProportionalUvMap;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Shape;
import de.yard.threed.engine.Texture;
import de.yard.threed.engine.UvMap1;
import de.yard.threed.engine.avatar.VehicleFactory;
import de.yard.threed.engine.geometry.ShapeGeometry;

import java.util.ArrayList;
import java.util.List;


/**
 * Gedanklich in x/y, als Vector3 in y=0 Ebene.
 * 8.12.16: Groessenangaben alle in Meter.
 * <p>
 * <p>
 * Created by thomass on 28.11.16.
 */
public class RailingFactory {
    //public static float trackwidthH0 = 0.0165f;
    //das ist deutlich unsauber, aber die Loc muss headheight hoeher.
    public static float headheight = 0;

    /**
     * Beispiel 1
     * Wie OSM in der x/y Ebene.
     *
     * @return
     */
    public static Graph buildRailSample1() {
        return buildRailSample1(-1);
    }

    public static Graph buildRailSample1(int extension) {
        //TODO Groessenordnugn abhaengig von trackwidth. Aber nicht unnötig zu viel ändern. Das hier ist ja auch Test/Referenz
        int x = 50, y1 = 50, y2 = 150;
        Graph graph = new Graph();
        addOval(graph, new Vector2(x, y1), new Vector2(x, y2), RailingDimensions.innerarcradius, "i");
        addOval(graph, new Vector2(x, y1), new Vector2(x, y2), RailingDimensions.outerarcradius, "o");

        int layer = 0;
        switch (extension) {
            case 0:
                GraphEdge branchr = addBranch(graph, graph.getEdge(1), 13, false, false, null);
                GraphEdge branchl = addBranch(graph, graph.getEdge(7), 13, true, false, null);
                GraphEdge extr = GraphUtils.extendWithEdge(graph, branchr, 14, layer);
                GraphEdge extl = GraphUtils.extendWithEdge(graph, branchl, 14, layer);
                // y-Wert experimentell ermittelt, passt so etwa. TODO mathematisch korrekt.
                //31.3.17: Die 0.8f sind falsch. Nur um setArc aufrufen zu koennen. 19.4.17:jetzt negativ. TODO
                float angle = -0.8f;
                connectNodes(graph, extl.to, extr.to, new Vector2(x - RailingDimensions.innerarcradius - RailingDimensions.trackdistance / 2, y1 + 13 + 12), angle);
                // Weiche auf der anderen Seite

                branchl = addBranch(graph, graph.getEdge(4), 13, true, false, null);
                // TODO Der offset ist noch experimentell 
                GraphEdge joiner = addBranch(graph, graph.getEdge(10), (y2 - y1) - 20, true, true, branchl.getTo());
                //join(graph,branchl.getTo(),graph.getEdge(10));

                break;
        }
        return graph;
    }

    /**
     * Ein gerades Stück. Für Tests.
     *
     * @return
     */
    public static Graph buildRailSample2() {
        int x = 50, y1 = 50, y2 = 150;
        Graph graph = new Graph();
        GraphNode start = GraphFactory.addNode(graph, x, y1, "");
        GraphNode next = GraphFactory.addNode(graph, x, y2, "");
        GraphEdge edge = graph.connectNodes(start, next);

        return graph;
    }

    /**
     * Einfach ein Halbkreis. Für Tests der Normalen.
     *
     * @return
     */
    public static Graph buildRailSample3() {
        int x = 50, y1 = 50, y2 = 150;
        Graph graph = new Graph();
        GraphNode start = GraphFactory.addNode(graph, x, y1, "");
        GraphNode next = GraphFactory.addNode(graph, x, y2, "");
        GraphEdge edge = graph.connectNodes(start, next);
        setArc(edge, new Vector3(50, 0, -100), 50, -MathUtil2.PI_2);
        return graph;
    }

    /**
     * Beispiel 1
     *
     * @param graph
     * @param c1
     * @param c2
     */
    private static void addOval(Graph graph, Vector2 c1, Vector2 c2, double radius, String prefix) {
        // Erstmal nur einfache, um Rotationen von Punkten zu vermeiden.
        if (c1.getX() != c2.getX()) {
            throw new RuntimeException("x!=x");
        }
        int cnt = 0;
        // unten (c1) beginnen und dann CW.
        GraphNode nstart = GraphFactory.addNode(graph, c1.getX(), c1.getY() - radius, prefix + cnt++);
        GraphNode nextn = GraphFactory.addNode(graph, c1.getX() - radius, c1.getY(), prefix + cnt++);
        GraphNode n = nstart;
        GraphEdge edge = graph.connectNodes(n, nextn);
        setArc(edge, GraphFactory.buildVector3(c1), radius, -MathUtil2.PI_2);
        n = nextn;
        nextn = GraphFactory.addNode(graph, c1.getX() - radius, c2.getY(), prefix + cnt++);
        edge = graph.connectNodes(n, nextn);
        n = nextn;
        nextn = GraphFactory.addNode(graph, c1.getX(), c2.getY() + radius, prefix + cnt++);
        edge = graph.connectNodes(n, nextn);
        setArc(edge, GraphFactory.buildVector3(c2), radius, -MathUtil2.PI_2);
        n = nextn;
        nextn = GraphFactory.addNode(graph, c1.getX() + radius, c2.getY(), prefix + cnt++);
        edge = graph.connectNodes(n, nextn);
        setArc(edge, GraphFactory.buildVector3(c2), radius, -MathUtil2.PI_2);
        n = nextn;
        nextn = GraphFactory.addNode(graph, c1.getX() + radius, c1.getY(), prefix + cnt++);
        edge = graph.connectNodes(n, nextn);
        //schliessen
        edge = graph.connectNodes(nextn, nstart);
        setArc(edge, GraphFactory.buildVector3(c1), radius, -MathUtil2.PI_2);
    }

    /**
     * Ein Weiche hinzufügen. Lieber branch als switch genannt.
     * Geht erstmal nur auf einem geraden Stück.
     * Die Kante wird am offset aufgetrennt und ein Knoten eingefügt. Daran kommt ein Bogen mit
     * neuem Endknoten. Der Endknoten wird den halben trackdistance haben, so dass es beim
     * Verbinden mit einem Gegenstück vom Gegengleis genau passt.
     * <p>
     * Als Konvention wird mal festgelegt, dass Edges 0 und 1 die quais geraden und 2 die abzweigung sind. Edge 1 ist die "parallele" zur
     * Abzweigung.
     * 19.4.17: Diese Konvention greift aber nur, wenn hieruebr explizit eine Weiche angelegt wird. Wenn einfach drei edges zusammenkommen, kann
     * das anders sein. Alternativ RailingBranchSelector.
     */
    public static GraphEdge addBranch(Graph graph, GraphEdge edge, float offset, boolean isleft, boolean reverse, GraphNode joinnode) {
        if (edge.getCenter() != null) {
            //TODO
            throw new RuntimeException("isType arc");
        }
        if (reverse) {
            // von to aus gesehen
            GraphNode branchnode = edge.split(graph, edge.getLength() - offset);
            Vector3 branchdir = edge.getDirection().negate().rotate(buildRotation(calculateAngleOfBranch(isleft)));
            branchdir = branchdir.normalize().multiply(RailingDimensions.branchlen);
            GraphNode destination = joinnode;
            if (destination == null) {
                graph.addNode("", branchnode.getLocation().add(branchdir));
            }
            GraphEdge branch = graph.connectNodes(branchnode, destination);
            return branch;
        } else {
            // von from aus
            GraphNode branchnode = edge.split(graph, offset);
            //GraphNode endnode =
            //branchnode.branch();
            //edge.branch(RailingDimensions.brancharc, RailingDimensions.branchlen);
            //4.4.17: Geht doch bestimmt auch mit GraphUtils.extend() oder branch? Oder, ist doch was ganz anderes?
            Degree angle = calculateAngleOfBranch(isleft);
            //GraphEdge branch = GraphUtils.createBranch(graph, vertex,edge,angle);

            Vector3 branchdir = edge.getDirection().rotate(buildRotation(angle));
            branchdir = branchdir.normalize().multiply(RailingDimensions.branchlen);
            GraphNode destination = graph.addNode("", branchnode.getLocation().add(branchdir));
            GraphEdge branch = graph.connectNodes(branchnode, destination);
            return branch;
        }
    }

    /**
     * Auch eine Weiche, aber das Gegenstück zu Branch. Hier wird kein neuer Knoten mehr fuer den Branch angelegt, sondern nur die verbindende Kante.
     * Ein split wird aber schon gemacht.
     *
     */
   /* public static void join(Graph graph, GraphNode node, GraphEdge edge){
        GraphEdge joiner = graph.connectNodes(no, destination);edge.to
    }*/

    /**
     * Der Winkel zum Branch Destination Knoten vom track, aus dem er gebranched wird.
     * Skizze 13?
     *
     * @return
     */
    public static Degree calculateAngleOfBranch(boolean isleft) {
        // Gegenkathete durch Hypotenuse
        return Degree.buildFromRadians((float) Math.asin((RailingDimensions.trackdistance / 2) / RailingDimensions.branchlen) * ((isleft) ? 1 : -1));
    }

    /**
     * Zwei Nodes mit einem Bogen verbinden.
     *
     * @param from
     * @param to
     * @param center
     * @return
     */
    public static GraphEdge connectNodes(Graph graph, GraphNode from, GraphNode to, Vector2 center, float angle) {
        if (center == null) {
            // berechnen. TODO
            // mathematisch knifflig. Skizze 13
            // 29.3.17: siehe auch GraphUtils.smoothnode()
        }
        Vector3 center3d = GraphFactory.buildVector3(center);
        double radius = to.getLocation().subtract(center3d).length();
        GraphEdge edge = graph.connectNodes(from, to);
        setArc(edge, center3d, radius, angle);
        return edge;
    }

    /**
     * @param angle
     * @return
     */
    private static Quaternion buildRotation(Degree angle) {
        return Quaternion.buildFromAngles(new Degree(0), angle, new Degree(0));
    }


    /**
     * Ein gerades Stück. Läuft entlang der z-Achse. Center in der Mitte am Boden.
     */
    public static SceneNode buildRails(double len) {

        CustomGeometry geo = RailingFactory.buildRailGeometry(len);
        Material mat = Material.buildLambertMaterial((Texture.buildBundleTexture("data", ("textures/gimp/wood/BucheHell.png"))));
        Mesh mesh = new Mesh(geo, mat);
        SceneNode model = new SceneNode();
        model.setMesh(mesh);
        return model;
    }

    /**
     * Ein Bogenstück um y rotiert. Center in der Kreismittelpunkt.
     */
    public static SceneNode buildRails(Degree len, double radius) {
        CustomGeometry geo = RailingFactory.buildRailGeometry(len, radius);
        Material mat = Material.buildLambertMaterial((Texture.buildBundleTexture("data", ("textures/gimp/wood/BucheHell.png"))));
        Mesh mesh = new Mesh(geo, mat);
        SceneNode model = new SceneNode();
        model.setMesh(mesh);
        return model;
    }

    /**
     * Ein gerades Stück.
     *
     * @param len
     * @return
     */

    public static ShapeGeometry buildRailGeometry(double len) {
        Shape shape = buildRailShape(VehicleFactory.wheelwidth);
        ShapeGeometry g = new ShapeGeometry(shape, len, 1);
        return g;
    }

    /**
     * Ein Bogen um y rotiert.
     *
     * @param len
     * @return
     */
    public static ShapeGeometry buildRailGeometry(Degree len, double radius, int segments) {
        Shape shape = buildRailShape(VehicleFactory.wheelwidth);
        // 28.11.16: Ob das verschieben des Shape konzeptionell das Wahre ist, muss sich erst noch zeigen.
        shape = shape.translateX(radius);
        // Ueber einen TeilKreis extrudieren. 8.12.16: Über Circle, löst aber nicht die Darstellungsunsauberkeiten an den Kanten.
        // 19.12.16: Die sind auch schwer zu lösen, weil ein smoothshading geometrieübergeifend stattfinden müsste.
        //Path path = SegmentedPath.buildHorizontalArc(len, radius, segments);
        List<UvMap1> ml = new ArrayList<UvMap1>();
        ml.add(new ProportionalUvMap());
        ShapeGeometry g = ShapeGeometry.buildByCircleRotation(shape, segments, (float) len.toRad(), ml);
        //ShapeGeometry g = new ShapeGeometry(shape, path, false, ml);
        return g;
    }

    public static ShapeGeometry buildRailGeometry(Degree len, double radius) {
        int segments = 256;
        return buildRailGeometry(len, radius, segments);
    }

    /**
     * Erstmal nur als grobes Schema. Der Untergund liegt auf y=0;
     * 19.12.16: Innenteil etwas anheben, Extend nur minimal und Nutbreite wheel abhaengig.
     *
     * @return
     */
    public static Shape buildRailShape(float wheelwidth) {
        float width = RailingDimensions.trackwidth;
        float width2 = RailingDimensions.trackwidth / 2;
        // willkürliche Höhe
        float height = RailingDimensions.trackwidth / 4;
        // Erweiterung unten links und rechts
        float bottomextend = width * 0.2f;//width / 2;
        // Breite der eingelassenen Nut (Schienenkopf?)
        float headwidth = wheelwidth;//width / 4;
        float headwidth2 = headwidth / 2;
        headheight = 2 * height / 3;//height / 3;
        float middleheight = headheight + (height - headheight) / 2;
        float outerwidth = RailingDimensions.trackwidth / 8;
        float innerheight = RailingDimensions.trackwidth / 6;
        Shape shape = new Shape(true);
        // links unten anfangen.
        shape.addPoint(new Vector2(-width2 - bottomextend, 0));
        shape.addPoint(new Vector2(-width2 - headwidth2 - headwidth, height));
        shape.addPoint(new Vector2(-width2 - headwidth2, height));
        shape.addPoint(new Vector2(-width2 - headwidth2, headheight));
        shape.addPoint(new Vector2(-width2 + headwidth2, headheight));
        shape.addPoint(new Vector2(-width2 + headwidth2, middleheight));

        shape.addPoint(new Vector2(+width2 - headwidth2, middleheight));
        shape.addPoint(new Vector2(+width2 - headwidth2, headheight));
        shape.addPoint(new Vector2(+width2 + headwidth2, headheight));
        shape.addPoint(new Vector2(+width2 + headwidth2, height));
        shape.addPoint(new Vector2(+width2 + headwidth2 + headwidth, height));
        shape.addPoint(new Vector2(+width2 + bottomextend, 0));

        return shape;
    }

    /**
     * Um eine Kante als Bogen zu definieren. Das laesst sich nicht in den Visualizer abstrahieren, weil die Info Bogen oder nicht
     * nicht herleitbar ist.
     * 31.3.17: Die Nutzung von toAngles ist unzuverlässig. angle muss mitgeliefert werden.
     * 10.2.18: Depreceated weil es nicht 3D kann. Dafuer gibt es einen anderen setArc().
     * 8.5.18: Verschoben aus GraphEdge nach railingFactory.Nur noch fuer
     *
     * @param center
     * @param radius
     */
    @Deprecated
    public static void setArc(GraphEdge edge, Vector3 center, double radius, double angle) {
        //12.2.18 this.center = center;
        //12.2.18this.radius = radius;
        Vector3 vf = edge.from.getLocation().subtract(center);
        Vector3 vt = edge.to.getLocation().subtract(center);
        // Der Winkel hilft mit nicht, denn der hat kein Vorzeichen. Besser die y-Achens Rotation
        // aus der Rotation nehmen
        //float cosangle = Vector3.getDotProduct(vf.normalize(), vt.normalize());
        //angle = (float) Math.acos(cosangle);
        //
        //
        //arcrotation = Vector3.getRotation(vf, vt);
       /* float[] angles = new float[3];
        //arcrotation.toAngles(angles);
        if (inZ0Ebene()){
            angle = angles[2];
        }else {
            angle = angles[1];
        }*/
        //this.angle = angle;
        float umfang = (float) (2 * Math.PI * radius);
        //O len stimmt nur in Ebene "??"
        //len = Math.abs((float) (umfang * angle / (2 * Math.PI)));
        //checklen();
        //Das mit dem up Vector ist nur Provisorium. Und da das deprecated ist, setz ich oirgendwas.
        GraphArc arcParameter = new GraphArc(center, radius, vf, false/* inZ0Ebene()*/ ? new Vector3(0, 0, 1) : new Vector3(0, 1, 0), angle);
        edge.setArc(arcParameter);
    }

}
