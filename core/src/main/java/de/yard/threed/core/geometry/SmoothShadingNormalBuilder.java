package de.yard.threed.core.geometry;

import de.yard.threed.core.Degree;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmoothShadingNormalBuilder implements NormalBuilder {

    Log logger = Platform.getInstance().getLog(SmoothShadingNormalBuilder.class);

    Degree crease = null;
    Float cosCreaseAngle = null;

    public SmoothShadingNormalBuilder(Degree crease) {
        this.crease = crease;

        if (crease != null) {
            if (crease.getDegree() <= 0)
                cosCreaseAngle = 1.0f;
            else if (180 <= crease.getDegree())
                cosCreaseAngle = -1.0f;
            else
                cosCreaseAngle = (float) Math.cos(crease.toRad());
        }
    }

    /**
     * @param vertices
     * @param faces
     * @return
     */
    @Override
    public Map<Integer, Vector3> calculateVertexNormals(List<Vector3> vertices, Face3List faces, VertexMap vertexMap) {

        // 2024 approach
        int originalVertices = vertices.size();
        for (int index = 0; index < originalVertices; index++) {
            List<Face3> facesofvertex = vertexMap.map.get(index);
            // Es gibt vielleicht vertices ohne Faces. Evtl. Line oder sowas.
            if (facesofvertex != null && facesofvertex.size() > 0) {
                List<List<Integer>> groups = groupFacesByAngle(facesofvertex, cosCreaseAngle);
                // more inutitive, keep the initial
                // start at end, because faces are removed. Otherwise group indices no longer fit.
                for (int gr = groups.size() - 1; gr > 0; gr--) {
                    // duplicate vertices and hook pages to new vertices.
                    // Only group 0 uses original vertices
                    reArrangeFacesOfOneGroup(vertices, facesofvertex, index, groups.get(gr), vertexMap);
                }
            }
        }

        Map<Integer, Vector3> normals = calculateSmoothVertexNormals(vertexMap);
        return normals;

    }

    /**
     * Eine Berechnung der Vertexnormalen mit Smoothing. Die Normalenvektoren der Faces werden
     * dafuer zunaechst im Vertex addiert und danach normalisiert.
     * <p/>
     * Algorithmus from http://schemingdeveloper.com/2014/10/17/better-method-recalculate-normals-unity/
     * <p/>
     * Dieser einfache Algorithmus geht aber nicht, wenn das smoothing von einem smoothingangle oder einer smoothinfgroup abhaengt.
     * <p/>
     * Wenn Eckpunkte nicht gesmoothed werden (können), werden Vertices dupliziert.
     * <p>
     * 02.12.16: Hier wird davon ausgegangen, dass die Faces ALLER Listen eine gemeinsame Fläche bilden (unter Berücksichtigung von crease/grouping).
     * Es werden keine Vertices mit denselben Werten zusammengefasst. Dafuer muss der Aufrufer sorgen. Das würde wegen evtl. unterschiedlicher uv Werte
     * ja auch gar nicht gehen.
     * <p>
     * 15.12.16: Jetzt mit allgemeingültiger SmoothingMap statt crease.
     * 8.9.24: crease is considered in subsequent step. But is this good?
     * 10.9.24: But assuming that ALL facelists build a common surface ignores the purpose of having multiple face lists. In that case we
     * could flatten it immediately. The use case wheel/disc/capsule is a good counterexample.
     * Returned normals aren't normalized yet.
     *
     * @return
     */
    private Map<Integer, Vector3> calculateSmoothVertexNormals(VertexMap vertexMap) {

        Map<Integer, Vector3> normals = new HashMap<Integer, Vector3>();

        for (int index : vertexMap.map.keySet()) {
            for (Face3 face : vertexMap.map.get(index)) {
                // Mehrere normale in der Face sind obselet.
                Vector3 facenormal = face.normal;
                if (normals.containsKey(index)) {
                    normals.put(index, MathUtil2.add(normals.get(index), facenormal));
                } else {
                    normals.put(index, facenormal);
                }
                //normals[face.index1] = MathUtil2.add(normals[face.index1], normal0);
                //normals[face.index2] = MathUtil2.add(normals[face.index2], normal0);
            }
        }
        /*normalized later List<Vector3> normallist = new ArrayList<Vector3>();
        for (int i = 0; i < normals.keySet(); i++) {
            normallist.add(normals[i].normalize());
        }*/
        return normals;

    }

    /**
     * Fuer ein Face, das dem Vertex vindex zugeordnet ist, pruefen, ob es anhand seiner Normale ueberhaupt dazu passt.
     * groupsofvertex ist die Liste der Vertexindizes, die durch duplizieren schon entstanden sind.
     * Vergleich immer zum "ersten" Face in seiner Gruppe.
     *
     * @return
     */
   /*25.9.24 @Deprecated
    public static int getgroupForFace(Face3 f, int vindex, List<Integer> groupsofvertex, List<Vector3> vertices, float cosCreaseAngle, VertexMap vertexMap) {
        Face3 facetocheck = f;//smoothingMap.get(faceindex);
        //Wenn in einer Gruppe ein Face gefunden wird, das zum geprueften passt, reicht das
        for (int group : groupsofvertex) {
            Face3 erstesface = vertexMap.map.get(group).get(0);
            double dot = MathUtil2.getDotProduct(erstesface.normal, facetocheck.normal);
            double lengths = erstesface.normal.length() * facetocheck.normal.length();
            if (cosCreaseAngle * lengths <= dot) {
                // Winkel ist innerhalb der Toleranz
                // Ok put that into the current set. Die Gruppe koennte eine neue sein. Wenn dieses Face dazukommt sicherstellen, dass das Face auch auf die neue
                // Gruppe zeigt.
                if (vindex != group) {
                    //Debug stop
                    vindex = vindex;
                }
                f.replaceIndex(vindex, group);
                if (vindex != group) {
                    vertexMap.add(group, f);
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
        vertexMap.add(grp, f);

        return grp;
    }
*/
    /**
     * new approach
     * Returns a list of face lists (by their index in vertexMaps list) that should be smoothed together.
     * A face can only belong to one group.
     */
    public List<List<Integer>> groupFacesByAngle(List<Face3> facesofvertex, Float cosCreaseAngle) {

        List<List<Integer>> groups = new ArrayList<>();
        groups.add(new ArrayList<>());
        if (cosCreaseAngle == null) {
            for (int i = 0; i < facesofvertex.size(); i++) {
                groups.get(0).add(i);
            }
            return groups;
        }
        Face3 erstesface = facesofvertex.get(0);
        // put first face into first group
        groups.get(0).add(0);
        for (int i = 1; i < facesofvertex.size(); i++) {
            Face3 facetocheck = facesofvertex.get(i);
            // find a group to which the face fits
            boolean groupFound = false;
            for (int gr = 0; gr < groups.size(); gr++) {
                List<Integer> currentGroup = groups.get(gr);
                // Comparing to the first face of a group only might not be really accurate. Might be improved in the future.
                Face3 firstFaceInGroup = facesofvertex.get(currentGroup.get(0));
                double dot = MathUtil2.getDotProduct(firstFaceInGroup.normal, facetocheck.normal);
                double lengths = erstesface.normal.length() * facetocheck.normal.length();
                if (cosCreaseAngle * lengths <= dot && !groupFound) {
                    // Winkel ist innerhalb der Toleranz. Put into same group. But only once do avoid having it multiple times
                    // in different lists.
                    currentGroup.add(i);
                    groupFound = true;
                }
            }
            if (!groupFound) {
                // create new group for the current face
                groups.add(new ArrayList<>());
                groups.get(groups.size() - 1).add(i);
            }
        }
        //}
        // create new group; duplicate Vertex
        return groups;
    }

    /**
     *
     */
    public void reArrangeFacesOfOneGroup(List<Vector3> vertices, List<Face3> facesofvertex, int index, List<Integer> group, VertexMap vertexMap) {
        List<Face3> extractedFromVertex = new ArrayList<>();

        // new index for that group
        int replacingIndex = vertexMap.duplicateVertex(index, vertices);

        for (int faceIndex = 0; faceIndex < group.size(); faceIndex++) {
            if (group.get(faceIndex) >= facesofvertex.size()) {
                // happens in reality in 'jetway:Rotunda1'. Reason is unclear. Postponed for now.
                logger.error("group size exceeding. Aborting smoothing");
            } else {
                Face3 face = facesofvertex.get(group.get(faceIndex));
                // hook face to the new vertex at "index".
                vertexMap.hookFaceToVertex(face, index, replacingIndex);
                extractedFromVertex.add(face);
            }
        }
        for (Face3 face3 : extractedFromVertex) {
            facesofvertex.remove(face3);
        }
    }
}


