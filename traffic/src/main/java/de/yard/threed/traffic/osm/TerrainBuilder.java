package de.yard.threed.traffic.osm;

import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.BasicGeometry;
import de.yard.threed.core.geometry.CustomGeometry;
import de.yard.threed.core.Degree;
import de.yard.threed.engine.GenericGeometry;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.core.Quaternion;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Texture;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.graph.GraphEdge;
import de.yard.threed.graph.GraphFactory;
import de.yard.threed.graph.GraphOrientation;
import de.yard.threed.graph.GraphPathSegment;
import de.yard.threed.core.platform.Log;

import de.yard.threed.core.Color;
import de.yard.threed.core.geometry.FaceList;
import de.yard.threed.core.geometry.FaceN;

import java.util.ArrayList;
import java.util.List;

/**
 * Erstmal alles in einer Ebene (z=0). Seit/Mit outline aber auch in anderen Ebenen.
 * <p>
 * Die Vertices fuer die Objekte im Terrain werden statisch (also im local space) angepasst. Es findet keine local->world Transformation zur Laufzeit statt.
 * 7.2.18: Nicht mehr nur z=0. Geht per Shaperstellung pro Edge (mit extrudiertem Bogen) oder per outline.
 * Der outline Ansatz ist wohl eher ein grundsaetzlich anderer (Terrain vs Graphdarstellung), oder? Ja, siehe Wiki.
 * Darum bevorzuge ich jetzt Outline.
 * <p>
 * Created by thomass on 15.09.16.
 */
public class TerrainBuilder {
    private static Log logger = Platform.getInstance().getLog(TerrainBuilder.class);
    // Wir nehmen eine z=0 Ebene an. Eine Rotation ist dann nicht erforderlich, wenn ein Vector exakt nach rechts zeigt.
    static Vector3 refVectorForRotation = new Vector3(1, 0, 0);
    public static boolean useoutline = true;

    /**
     *
     */
    /*7.2.18 ist doch schon obsolet public static SceneNode buildRoad(Vector3 from, Vector3 to, float width, Color color) {
        
        CustomGeometry geo = buildRoadGeometry(from, to, width);
        Material mat;
        if (color != null) {
            mat = Material.buildLambertMaterial(color);
        } else {
            mat = Material.buildLambertMaterial((Texture.buildBundleTexture("data", "flusi/asphalt.png")));
        }
        Mesh mesh = new Mesh(geo, mat);
        SceneNode model = new SceneNode();
        model.setMesh(mesh);
        return model;
    }*/

    /**
     * Not only for roads but also taxiway marker and travel graphs (2D z0 plane).
     * 9.12.21 Renamed buildRoad()->buildEdgeArea()
     */
    public static SceneNode buildEdgeArea(GraphEdge edge, double width, Color color, double elevation,GraphOrientation graphOrientation) {
        CustomGeometry cgeo = buildRoadGeometry(edge, width,graphOrientation);
        GenericGeometry geo = GenericGeometry.buildGenericGeometry(cgeo);
        Material mat;
        if (color != null) {
            mat = Material.buildLambertMaterial(color);
        } else {
            // 16.12.21: "data-old:flusi/asphalt.png" replaced
            mat = Material.buildLambertMaterial((Texture.buildBundleTexture("data", "textures/gimp/Ground.png")));
        }
        Mesh mesh = new Mesh(geo, mat);
        SceneNode model = new SceneNode();
        model.setMesh(mesh);
        Vector3 p = model.getTransform().getPosition();
        p = new Vector3(p.getX(), p.getY(), elevation);
        model.getTransform().setPosition(p);
        return model;
    }

    /**
     * *
     * 1) Ein Rechteck als road shape anlegen
     * 2) die Vertices abhängig von der Rotation und auch Position berechnen. D.h., das Objekt kann nachher statisch in die Scene.
     * 3) Die Resultate in die Geo eintragen
     * <p>
     * Vertexorder ist
     * 0 - 3
     * 1 - 2
     * aber Obacht! Es wird rotiert.
     * <p>
     * 8.2.17: Das gibt es jetzt ueber Graph.outline; darum deprecated
     * 29.3.17: So ganz ist outline doch nicht geeignet. Kann (noch?) keine Bögen. Darum nicht mehr deprecated.
     * 7.2.18: nur fuer Test public.
     *
     * @param from
     * @param to
     * @param width
     * @return
     */
    public static CustomGeometry buildRoadGeometry(Vector3 from, Vector3 to, double width) {
        double w2 = width / 2;
        double len = Vector3.getDistance(from, to);
        double len2 = len / 2;
        Vector3 linksoben = new Vector3(-len2, w2, 0);
        Vector3 linksunten = new Vector3(-len2, -w2, 0);
        Vector3 rechtsunten = new Vector3(len2, -w2, 0);
        Vector3 rechtsoben = new Vector3(len2, w2, 0);
        if (len > 0.00001f) {

            //17.7.17: der up scheint mir richtig. Mit getRotation fehlt aber das Stück nach links(??)
            //18.7.17:das scheint alles richtig. Trozdem ist das mit der 3D Roatation nicht korrekt.
            Quaternion rotation = Vector3.getRotation(refVectorForRotation, to.subtract(from));
            Degree angle = Vector2.angle(new Vector2(), new Vector2(refVectorForRotation.getX(), refVectorForRotation.getY()), new Vector2(to.getX() - from.getX(), to.getY() - from.getY()), false);
            angle = new Degree(angle.getDegree() - 90);
            rotation = getRotationZfromHeading(angle);

            linksoben = linksoben.multiply(rotation);
            linksunten = linksunten.multiply(rotation);
            rechtsunten = rechtsunten.multiply(rotation);
            rechtsoben = rechtsoben.multiply(rotation);
        } else {
            // nicht möglich weil keine Rotation berechenbar. Trotzdem was liefern um Fehlerbehandlung zu vereinfachen.
            logger.warn("segment too small");
        }
        Vector3 half = to.subtract(from).multiply(0.5f);
        Vector3 destination = from.add(half);
        linksoben = linksoben.add(destination);
        linksunten = linksunten.add(destination);
        rechtsunten = rechtsunten.add(destination);
        rechtsoben = rechtsoben.add(destination);


        List<Vector3> vertices = new ArrayList<Vector3>();
        vertices.add(linksoben);
        vertices.add(linksunten);
        vertices.add(rechtsunten);
        vertices.add(rechtsoben);
        Vector2 uv0 = new Vector2();
        Vector2 uv1 = new Vector2();
        Vector2 uv2 = new Vector2();
        Vector2 uv3 = new Vector2();
        FaceList faces = new FaceList();
        //17.7.17: Wenn es dumm läuft (50% der Faelle?) sind die Faces unpassend orientiert? Darum erstmal twosided? Geht abr nicht so einfach.
        //und dürfte auch nicht die Lösung sein. Das ist doch eh komplett in z0! Das Problem war eine falsche Rotation.
        //TODO uvs sind eh falsch
        faces.faces.add(new FaceN(0, 1, 2, 3, uv0, uv1, uv2, uv3));
        CustomGeometry geo = new BasicGeometry(vertices, faces);
        return geo;
    }

    public static Quaternion getRotationZfromHeading(Degree heading) {
        return Quaternion.buildRotationZ(MathUtil2.getDegreeFromHeading(heading));
    }

    /**
     * Fuer Boegen und Geraden.
     * Alles auch in z=0 Ebene.
     *
     * @param edge
     * @param width
     * @return
     */
    public static CustomGeometry buildRoadGeometry(GraphEdge edge, double width,GraphOrientation graphOrientation) {
        if (useoutline) {
            return buildGeometryFromOutline(edge, width,graphOrientation );
        } else {
            if (edge.getCenter() == null) {
                //Gerade. TODO auch ueber Primitveroutine
                return (buildRoadGeometry(edge.from.getLocation(), edge.to.getLocation(), width));
            }
            // Bogen: So einen ähnliche Algorithmus gibt es auch schon bei Railing. Es gibt verschiedenste Wege. Vielleicht braeuchte es eine PolygonLib
            return (GraphFactory.buildGraphGeometry(edge, width, 16));
        }
    }

    /**
     * v0 liegt immer rechts bei "from". Und dann CCW.
     * @param edge
     * @param width
     * @return
     */
    private static CustomGeometry buildGeometryFromOutline(GraphEdge edge, double width,GraphOrientation graphOrientation) {
        List<GraphPathSegment> path = new ArrayList<GraphPathSegment>();
        path.add(new GraphPathSegment(edge,edge.from));

        //List<Vector3> outline = graphOrientation.getOutline(edge.from,width / 2, edge.to, edge,null);
        //List<Vector3> leftoutline = graphOrientation.getOutline(edge.from,-width / 2, edge.to, edge,null);
        List<Vector3> outline = graphOrientation.getOutline(path,width / 2, -1);
        List<Vector3> leftoutline = graphOrientation.getOutline(path,-width / 2,-1);
        for (int i = leftoutline.size() - 1; i >= 0; i--) {
            outline.add(leftoutline.get(i));
        }
        // jetzt ist es ein Polygon 
        FaceList faces = new FaceList();
        //TODO  uvs sind  falsch
        int cnt = outline.size();
        int[] indices = new int[cnt];
        Vector2[] uvs = new Vector2[cnt];
        for (int i = 0; i < cnt; i++) {
            //reverse sieht es schlecht aus, aber zumindest sieht man was.
            //indices[i] = cnt-i-1;
            indices[i] = i;
            uvs[i] = new Vector2();
        }
        faces.faces.add(new FaceN(indices, uvs));
        CustomGeometry geo = new BasicGeometry(outline, faces);
        return geo;//GenericGeometry.buildGenericGeometry(geo);
    }
}
