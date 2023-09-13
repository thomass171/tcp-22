package de.yard.threed.core.geometry;

import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;

import de.yard.threed.core.platform.Platform;

import java.util.List;

/**
 * Created by thomass on 14.07.16.
 */
public class SmoothingHelper {
    static Log logger = Platform.getInstance().getLog(SmoothingHelper.class);

    SmoothingHelper() {

    }

    /**
     * Die einfache Form des Smoothing. Die Normale eines Vertex ergibt sich aus dem Durchschnitt der daran
     * grenzenden Faces. Einfach die Faces durchgehen und die Indizes mappen.
     * Die Zugehörigkeit zu einer Facelist spielt hier keine Rolle! Das ist der Standard Algorithmus zur Normalenberechnung, der frueher
     * in GeometryHelper.calculateSmoothVertexNormals() war.
     *
     * @param vertices
     * @param faces
     * @return
     */
    public static SmoothingMap buildStandardSmoothingMap(List</*7.2.18 Native*/Vector3> vertices, List<Face3List> faces) {
        SmoothingMap smoothingMap = new SmoothingMap();
        for (Face3List fl : faces) {
            for (Face gface : fl.faces) {
                Face3 face = (Face3) gface;
                // Mehrere normale in der Face sind obselet.
                //Vector3 normal0 = face.normal;
                smoothingMap.add(face.index0, face);
                smoothingMap.add(face.index1, face);
                smoothingMap.add(face.index2, face);
                //normals[face.index0] = MathUtil2.add(normals[face.index0], normal0);
                //normals[face.index1] = MathUtil2.add(normals[face.index1], normal0);
                //normals[face.index2] = MathUtil2.add(normals[face.index2], normal0);
            }
        }
        return smoothingMap;
    }

    /**
     * Die Vertices durchgehen und pruefen, inwieweit die daran liegenden Faces anhand des crease Wertes zusammengehoeren und zu smoothen sind. Wenn Faces
     * nicht dazugehoeren, durch Vertex duplizieren eine neue Group bilden und dann die Pruefung fuer die restlichen Faces durchführen.
     * Die Faces muessen schon Normale enthalten.
     * TODO: crease verallgemeinern auf grouping.
     */
   /*15.12.16 in smoothingmap jetzt verallgemeinert public List<Vector3> calculateSmoothVertexNormalsByGroups(List<Vector3> vertices, List<Face3List> faces, Degree crease) {
        if (crease.degree <= 0)
            cosCreaseAngle = 1;
        else if (180 <= crease.degree)
            cosCreaseAngle = -1;
        else
            cosCreaseAngle = (float) Math.cos(crease.toRad());

        facesatvertex = new ArrayList<List<Face>>();
        for (int i = 0; i < vertices.size(); i++) {
            facesatvertex.add(new ArrayList<Face>());
        }
        for (Face3List fl : faces) {
            for (Face gface : fl.faces) {
                Face3 face = (Face3) gface;
                registerFace(face);
            }
        }

        // Initialbefuellung
        List<Vector3> normals = new ArrayList<Vector3>();
        for (int i = 0; i < vertices.size(); i++) {
            normals.add(Platform.getInstance().buildVector3(0, 0, 0));
        }
        int vcnt = vertices.size();
        // number of vertices might grow
        for (int i = 0; i < vcnt; i++) {
            List<Face> fav = facesatvertex.get(i);
            // Es gibt vielleicht vertices ohne Faces. Evtl. Line oder sowas.
            if (fav.size() > 0) {
                // erstmal gibt es nur die eine Gruppe
                List<Integer> groupsofvertex = new ArrayList<Integer>();
                groupsofvertex.add(i);
                normals.set(i, fav.get(0).normal);
                // Pruefung erst ab dem zweiten Face
                for (int j = 1; j < fav.size(); j++) {
                    Face f = fav.get(j);
                    int group = getgroupForFace(f, i, groupsofvertex, vertices, normals);
                    normals.set(group, MathUtil2.add(normals.get(group), f.normal));
                }
            } else {
                logger.warn("vertex without face");
            }
        }

        for (int i = 0; i < normals.size(); i++) {
            normals.set(i, MathUtil2.normalize(normals.get(i)));
        }
        return normals;
    }*/

    /**
     * Fuer ein Face, das dem Vertex vindex zugeordnet ist, pruefen, ob es anhand seiner Normale ueberhaupt dazu passt.
     * groupsofvertex ist die Liste der Vertexindizes, die durch duplizieren schon entstanden sind.
     * Vergleich immer zum "ersten" Face in seiner Gruppe.
     *
     * @return
     */
    public static int getgroupForFace(Face3 f, int vindex, List<Integer> groupsofvertex, List</*7.2.18 Native*/Vector3> vertices, float cosCreaseAngle, SmoothingMap smoothingMap) {
        Face3 facetocheck = f;//smoothingMap.get(faceindex);
        //Wenn in einer Gruppe ein Face gefunden wird, das zum geprueften passt, reicht das
        for (int group : groupsofvertex) {
            Face3 erstesface = smoothingMap.map.get(group).get(0);
            double dot = MathUtil2.getDotProduct(erstesface.normal, facetocheck.normal);
            double lengths = erstesface.normal.length() * facetocheck.normal.length();
            if (cosCreaseAngle * lengths <= dot) {
                // Winkel ist innerhalb der Toleranz
                // Ok put that into the current set. Die Gruppe koennte eine neue sein. Wenn dieses Face dazukommt sicherstellen, dass das Face auch auf die neue
                // Gruppe zeigt.
                if (vindex != group){
                    //Debug stop
                    vindex=vindex;
                }
                f.replaceIndex(vindex, group);
                if (vindex != group) {
                    smoothingMap.add(group, f);
                    return group;
                }
                return -1;
            }
        }
        // create new group; duplicate Vertex
        int grp = vertices.size();
        Vector3 v = vertices.get(vindex);
        vertices.add(new Vector3(v.getX(), v.getY(), v.getZ()));
        //normals.add(Platform.getInstance().buildVector3(0, 0, 0));
        // Index im Face anpassen
        facetocheck.replaceIndex(vindex, grp);
        groupsofvertex.add(grp);
        smoothingMap.add(grp, f);

        return grp;
    }

    private static void registerFace(List<List<Face>> facesatvertex, Face face) {
        for (int i : face.getIndices()) {
            facesatvertex.get(i).add(face);
        }
    }


}
