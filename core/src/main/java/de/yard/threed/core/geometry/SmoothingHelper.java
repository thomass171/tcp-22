package de.yard.threed.core.geometry;

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
     * Just prepare a SmoothingMap. No normals are calculated yet.
     *
     * Die einfache Form des Smoothing. Die Normale eines Vertex ergibt sich aus dem Durchschnitt der daran
     * grenzenden Faces. Einfach die Faces durchgehen und die Indizes mappen.
     * Die Zugehörigkeit zu einer Facelist spielt hier keine Rolle! Das ist der Standard Algorithmus zur Normalenberechnung, der frueher
     * in GeometryHelper.calculateSmoothVertexNormals() war.
     *
     * 7.9.24: This will lead to visual artifacts for geometries like
     * - closed tapes
     * - boxes? (edges are smoothed, but shouldn't)
     *
     */
    public static VertexMap buildStandardSmoothingMap(/*8.9.24 List<Vector3> vertices,*/ List<Face3List> faces) {
        VertexMap vertexMap = new VertexMap();
        for (Face3List fl : faces) {
            for (Face gface : fl.faces) {
                Face3 face = (Face3) gface;
                vertexMap.add(face.index0, face);
                vertexMap.add(face.index1, face);
                vertexMap.add(face.index2, face);
            }
        }
        return vertexMap;
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


    private static void registerFace(List<List<Face>> facesatvertex, Face face) {
        for (int i : face.getIndices()) {
            facesatvertex.get(i).add(face);
        }
    }


}
