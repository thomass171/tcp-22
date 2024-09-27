package de.yard.threed.core.geometry;

import de.yard.threed.core.Degree;
import de.yard.threed.core.SmartArrayList;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;

import de.yard.threed.core.platform.Log;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.MathUtil2;


import java.util.ArrayList;
import java.util.List;

/**
 * Primitives, die nicht ueber die Platform angelegt werden.
 * Siehe auch wiki.
 * Die Geometrien sollen alle SimpleGeometry sein. Die ist komplett weiterverwendbar und alles steckt präzise drin. Und die passt eigentlich auch gut zu CSG.
 * Und das passt auch gut dazu, dass die Geos hier liebevoll von Hand geklöppelt sind. Da soll dann kein Smoother oder sowas mehr drüberlaufen.
 * <p>
 * Die Faces bzw. Vertices laufen CCW, mit Blick auf die Vorderseite:
 * <p/>
 * 3 - 2
 * 0 - 1
 * Ist zwar im Prinzip belanglos, aber besser einheitlich wegen Vorstellung.
 * <p>
 * 6.3.21: Hier gilt im Prinzip das gleiche wie bei {@link de.yard.threed.engine.avatar.VehiclePmlFactory}. Bevorzugt PML statt native Model erzeugen.
 * Aber da hier eh eine SimpleGeometry erzeugt wird, was die Basis fuer PML ist, ist das hier voellig OK.
 * <p>
 * Created by thomass on 09.03.17.
 */
public class Primitives {
    static Vector3 zpnormal = new Vector3(0, 0, 1);
    static Vector3 ypnormal = new Vector3(0, 1, 0);
    static Vector3 xpnormal = new Vector3(1, 0, 0);
    static Log logger = Platform.getInstance().getLog(Primitives.class);

    /**
     * Eigentlich ja composed aus 6 Planes.
     *
     * @param width
     * @param height
     * @param depth
     * @return
     */
    public static SimpleGeometry buildBox(double width, double height, double depth) {
        return ComposedPrimitives.buildBox(width, height, depth);
    }

    /**
     * Die Geo läuft nachher über prepareGeometry(). Ist damit eigentlich keine im Sinne meines Primitive Verstaendnisses.
     * TODO: Die Box hat mit Normalmap eine "kaputte" Seite. Evtl. hat das Smoothing eine Macke.
     * Diese Methode ist zum Test des Smoothing.
     * 8.9.24: Never used? Remove because replaced by either primives or other
     * @param width
     * @param height
     * @param depth
     * @return
     */
   /* public static SimpleGeometry buildBoxWithSmoothing(double width, double height, double depth) {
        //Platform platform = ((Platform) Platform.getInstance());
        double w2 = width / 2;
        double h2 = height / 2;
        double d2 = depth / 2;
        List</*7.2.18 Native* /Vector3> vertices = new ArrayList</*7.2.18 Native* /Vector3>();
        FaceList faces = new FaceList();

        vertices.add(new Vector3(-w2, -h2, d2));
        vertices.add(new Vector3(-w2, h2, d2));
        vertices.add(new Vector3(w2, h2, d2));
        vertices.add(new Vector3(w2, -h2, d2));
        vertices.add(new Vector3(-w2, -h2, -d2));
        vertices.add(new Vector3(-w2, h2, -d2));
        vertices.add(new Vector3(w2, h2, -d2));
        vertices.add(new Vector3(w2, -h2, -d2));

        Vector2 uv0 = new Vector2(0, 0);
        Vector2 uv1 = new Vector2(0, 1);
        Vector2 uv2 = new Vector2(1, 1);
        Vector2 uv3 = new Vector2(1, 0);

        faces.faces.add(new Face3(0, 3, 2, uv0, uv3, uv2));
        faces.faces.add(new Face3(2, 1, 0, uv2, uv1, uv0));
        faces.faces.add(new Face3(7, 6, 2, uv0, uv3, uv2));
        faces.faces.add(new Face3(2, 3, 7, uv2, uv1, uv0));
        faces.faces.add(new Face3(7, 4, 6, uv0, uv3, uv2));
        faces.faces.add(new Face3(5, 6, 4, uv2, uv1, uv0));
        faces.faces.add(new Face3(4, 0, 1, uv0, uv3, uv2));
        faces.faces.add(new Face3(1, 5, 4, uv2, uv1, uv0));
        faces.faces.add(new Face3(1, 2, 6, uv0, uv3, uv2));
        faces.faces.add(new Face3(6, 5, 1, uv2, uv1, uv0));
        faces.faces.add(new Face3(4, 7, 3, uv0, uv3, uv2));
        faces.faces.add(new Face3(3, 0, 4, uv2, uv1, uv0));
        // TODO: 01.12.16:ob das mit den uvs hinhaut ist fraglich, denn es fehlen Edges und damit eine Vertexduplizierung. umstellen auf smoothingmap
        SimpleGeometry geo = GeometryHelper.prepareGeometry(vertices, new SmartArrayList<FaceList>(faces), null, false, new Degree(30), false, null).get(0);
        return geo;
    }*/

    /**
     * Kugel mit "Nordpol" im positivem y und "Suedpol" im negativen y. UV (0,0) ist am Suedpol.
     * Analog zu ShapeGeometry.Sphere.
     * Die horizonzalsegments gelten für die roundness, d.h. je kleiner die roundness, umso kleiner
     * sind die einzelnen Winkelstuecke.
     * <p/>
     * Center in 0,0,0
     * Der erste Vertex ist "oben". Es wird bogenweise gebaut, immer von oben nach unten. hsegs ist Segemente pro Bogen. wsegs ist die Anzahl Bögen.
     * Wenn nur Teilkugeln gebaut werden, beziehen sich die UVs trotzdem immer auf eine ganze Kugel. D.h. Eine Halbkugel mit Weltkarte ist ein
     * durchgeschnittener Globus.
     * Der hangle beginnt mit 0 von oben; der wangle beginnt rechts (x) nach hinten.
     * Die UV Naht ist rechts.
     *
     * @return
     */
    public static SimpleGeometry buildSphereGeometry(double radius, int wsegs, double wstartangle, double wspanangle, int hsegs, double hstartangle, double hspanangle) {
        //Platform platform = ((Platform) Platform.getInstance());
        float PI2 = (float) (Math.PI * 2);
        float PI = (float) Math.PI;
        List<Vector2> uvs = new ArrayList<Vector2>();
        List<Vector3> vertices = new ArrayList<Vector3>();
        IndexList indexes = new IndexList();
        List<Vector3> normals;
        double hstepangle = hspanangle / hsegs;
        double wstepangle = wspanangle / wsegs;

        normals = new ArrayList<Vector3>();

        for (int y = 0; y <= wsegs; y++) {
            for (int x = 0; x <= hsegs; x++) {
                double wangle = wstartangle + y * wstepangle;
                double hangle = hstartangle + x * hstepangle;
                double u = wangle / MathUtil2.PI2;
                double v = 1 - hangle / MathUtil2.PI;

                Vector3 vertex = new Vector3(
                        (float) (radius * Math.cos(wangle) * Math.sin(hangle)),
                        (float) (radius * Math.cos(hangle)),
                        (float) (-radius * Math.sin(wangle) * Math.sin(hangle)));

                //logger.debug("" + vertices.size() + ": wangle=" + wangle + ",hangle=" + hangle + ",u=" + u + ",v=" + v);
                vertices.add(vertex);
                normals.add(vertex.normalize());
                uvs.add(new Vector2(u, v));
            }
        }

        for (int y = 0; y < wsegs; y++) {

            for (int x = 0; x < hsegs; x++) {
                int a = y * (hsegs + 1) + x;
                int b = y * (hsegs + 1) + x + 1;
                int c = (y + 1) * (hsegs + 1) + x + 1;
                int d = (y + 1) * (hsegs + 1) + x;

                Vector3 v1 = vertices.get(a);
                Vector3 v2 = vertices.get(b);
                Vector3 v3 = vertices.get(c);

                //An die Pole kommt nur ein einziges Face3. Pole können wegen Teilkugeln nicht am Segmentindex erkannt werden.
                //if (MathUtil.floatEquals((float) Math.abs(v1.getY()), radius)) {
                if (MathUtil2.areEqual(Math.abs(v1.getY()), radius)) {
                    // "Nordpol"
                    indexes.add(a, b, c);

                    //} else if (MathUtil.floatEquals((float) Math.abs(v2.getY()), radius)) {
                } else if (MathUtil2.areEqual(Math.abs(v2.getY()), radius)) {
                    //"Suedpol"
                    indexes.add(a, b, d/*14.3.17c*/);

                } else {
                    indexes.add(a, b, d);
                    indexes.add(b, c, d);
                }
            }
        }
        return new SimpleGeometry(vertices, indexes.getIndices(), uvs, normals);
    }

    public static SimpleGeometry buildSphereGeometry(double radius, int wsegs, int hsegs) {
        return buildSphereGeometry(radius, wsegs, 0, MathUtil2.PI2, hsegs, 0, MathUtil2.PI);
    }

    /**
     * 9.3.17: In y=0 layer like in Shapegeometry (from +z to -z). Also the vertex order.
     * 6.3.21: Fits to OpenGl coordinate system, with y from down to up.
     *
     * @return
     */
    public static SimpleGeometry buildPlaneGeometry(double width, double depth, int widthSegments, int depthSegments) {
        float w2 = (float) width / 2;
        float d2 = (float) depth / 2;
        List<Vector2> uvs = new ArrayList<Vector2>();
        List<Vector3> vertices = new ArrayList<Vector3>();
        List<Vector3> normals = new ArrayList<Vector3>();
        IndexList indexes = new IndexList();

        double segment_width = width / widthSegments;
        double segment_depth = depth / depthSegments;

        for (int iz = depthSegments; iz >= 0; iz--) {
            for (int ix = 0; ix < widthSegments + 1; ix++) {
                double x = ix * segment_width - w2;
                double z = iz * segment_depth - d2;
                vertices.add(new Vector3(x, 0, z));
                normals.add(ypnormal);
                Vector2 uv = new Vector2(1 - iz * segment_depth / depth, 1 - ix * segment_width / width);
                uvs.add(uv);
            }
        }

        for (int iz = 0; iz < depthSegments; iz++) {
            for (int ix = 0; ix < widthSegments; ix++) {

                int a = (widthSegments + 1) * iz + ix;
                int b = (widthSegments + 1) * iz + (ix + 1);
                int c = (widthSegments + 1) * (iz + 1) + (ix + 1);
                int d = (widthSegments + 1) * (iz + 1) + ix;
                indexes.add(a, b, d);
                indexes.add(b, c, d);
            }
        }
        return new SimpleGeometry(vertices, indexes.getIndices(), uvs, normals);
    }

    /**
     * 3.5.21: Wird in der z=0 Ebene gebaut, geeignet für ein FOV. Darum uv mapping auch passend für Texturen/Icons/Text ohne dass man rotieren muss.
     * 0 - 3
     * 1 - 2
     *
     * @return
     */
    public static SimpleGeometry buildSimpleXYPlaneGeometry(double width, double height, UvMap1 uvmap) {
        return buildSimplePlaneGeometry(width, height, uvmap, true);
    }

    public static SimpleGeometry buildSimpleXZPlaneGeometry(double width, double height, UvMap1 uvmap) {
        return buildSimplePlaneGeometry(width, height, uvmap, false);
    }

    private static SimpleGeometry buildSimplePlaneGeometry(double width, double height, UvMap1 uvmap, boolean z0Plane) {
        float w2 = (float) width / 2;
        float h2 = (float) height / 2;
        List<Vector2> uvs = new ArrayList<Vector2>();
        List<Vector3> vertices = new ArrayList<Vector3>();
        List<Vector3> normals = new ArrayList<Vector3>();
        IndexList indexes = new IndexList();
        Vector3 normal = (z0Plane) ? zpnormal : ypnormal;

        vertices.add(new Vector3(-w2, (z0Plane) ? h2 : 0, (z0Plane) ? 0 : -h2));
        normals.add(normal);
        uvs.add(uvmap.getUvFromNativeUv(new Vector2(0, 1)));

        vertices.add(new Vector3(-w2, (z0Plane) ? -h2 : 0, (z0Plane) ? 0 : h2));
        normals.add(normal);
        uvs.add(uvmap.getUvFromNativeUv(new Vector2(0, 0)));

        vertices.add(new Vector3(w2, (z0Plane) ? -h2 : 0, (z0Plane) ? 0 : h2));
        normals.add(normal);
        uvs.add(uvmap.getUvFromNativeUv(new Vector2(1, 0)));

        vertices.add(new Vector3(w2, (z0Plane) ? h2 : 0, (z0Plane) ? 0 : -h2));
        normals.add(normal);
        uvs.add(uvmap.getUvFromNativeUv(new Vector2(1, 1)));

        indexes.add(0, 1, 2);
        indexes.add(2, 3, 0);

        return new SimpleGeometry(vertices, indexes.getIndices(), uvs, normals);
    }


    /**
     * Auch für Pyramide. Kann aber nicht geschlossen werden. Wer geschlossen will, muss eine Disc dran composen.
     * Läuft entlang der y-Achse, bottom im negativen und top im positivem. height/2 ist bei y=0.
     * Die Geo kann nicht vorne wieder geschlossen werden, weil die uvs an der Naht unterschiedlich sind.
     * Anders orientiert als der Cylinder in ShapeGeometry, damit der Algorithmus der Sphere entspricht.
     * Die UV Naht ist rechts.
     *
     * @param radiusBottom
     * @param radiusTop
     * @param height
     * @param segments
     * @return
     */
    public static SimpleGeometry buildCylinderGeometry(double radiusTop, double radiusBottom, double height, int segments, double startangle, double spanangle) {
        //Platform platform = ((Platform) Platform.getInstance());
        List<Vector3> vertices = new ArrayList<Vector3>();
        List<Vector2> uvs = new ArrayList<Vector2>();
        List<Vector3> normals;
        IndexList indexes = new IndexList();

        normals = new ArrayList<Vector3>();
        double stepangle = spanangle / segments;

        for (int i = 0; i <= segments; i++) {
            double angle = startangle + i * stepangle;
            double u = angle / MathUtil2.PI2;

            //Sphere beginnt auch oben.
            //oben
            Vector3 topvertex = new Vector3(
                    (float) (radiusTop * Math.cos(u * MathUtil2.PI2)),
                    height / 2,
                    (float) (-radiusTop * Math.sin(u * MathUtil2.PI2)));
            vertices.add(topvertex);
            uvs.add(new Vector2(u, 1));

            //unten
            Vector3 bottomvertex = new Vector3(
                    (float) (radiusBottom * Math.cos(u * MathUtil2.PI2)),
                    -height / 2,
                    (float) (-radiusBottom * Math.sin(u * MathUtil2.PI2)));
            vertices.add(bottomvertex);
            uvs.add(new Vector2(u, 0));
            // Normal ist oben und unten gleich. Aber Pyramide beachten, da ist Normale nicht so trivial.
            // 16.3.17: Ob die Berechnung stimmt, ist moch nicht geklärt.
            Vector3 steigung = MathUtil2.subtract(topvertex, bottomvertex);
            Vector3 normal = MathUtil2.getCrossProduct(bottomvertex, steigung).normalize();
            normals.add(normal);
            normals.add(normal);
        }

        for (int i = 0; i < segments; i++) {
            int a = i * 2;
            int b = i * 2 + 1;
            int c = i * 2 + 3;
            int d = i * 2 + 2;
            indexes.add(a, b, d);
            indexes.add(b, c, d);
        }
        return new SimpleGeometry(vertices, indexes.getIndices(), uvs, normals);
    }

    public static SimpleGeometry buildCylinderGeometry(double radiusTop, double radiusBottom, double height, int segments) {
        return buildCylinderGeometry(radiusTop, radiusBottom, height, segments, 0, MathUtil2.PI2);
    }
}

