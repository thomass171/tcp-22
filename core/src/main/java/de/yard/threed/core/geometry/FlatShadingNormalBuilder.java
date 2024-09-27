package de.yard.threed.core.geometry;

import de.yard.threed.core.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlatShadingNormalBuilder implements NormalBuilder {

    public FlatShadingNormalBuilder() {

    }

    /**
     * @param vertices
     * @param faces
     * @return
     */
    @Override
    //public List<Vector3> calculateSmoothVertexNormals(List<Vector3> vertices, List<Face3List> faces) {
    public Map<Integer, Vector3> calculateVertexNormals(List<Vector3> vertices, Face3List faces, VertexMap vertexMap) {

        duplicateVertices(vertices, faces, vertexMap);

        // prepare list
        // List<Vector3> normals = new ArrayList<Vector3>();
        Map<Integer, Vector3> normals = new HashMap<Integer, Vector3>();
        /*for (int i = 0; i < vertices.size(); i++) {
            normals.add(new Vector3(0, 0, 0));
        }*/

        // Now after duplicating vertices we can just use face normals.

        //SmoothingMap smoothingMap = SmoothingHelper.buildStandardSmoothingMap(faces);

        for (int index : vertexMap.map.keySet()) {
            for (Face3 face : vertexMap.map.get(index)) {
                normals.put(index, face.normal);
            }
        }

        return normals;
    }

    /**
     * Mehrfach verwendete Vertices derart duplizieren, dass jede Facelist quasi ihre eigenen hat. Erforderlich bei Kanten in der Geometrie, z.B. Boxen.
     * Die Indizes in den Facelists werden hier angepasst.
     * 5.12.16: Die Indizes in die schon vorhandenen Vertices sollen aber nicht verändert werden. Das ist für Analysen sonst total verwirrend.
     * Darum gibt es auch keine neue Liste, sondern die bestehende wird erweitert. Das heisst füer Cubes aber, dass die bisherige Symmetrie bei der uv Zuordnung
     * nicht mehr besteht? Das ist eh nur fuer generisches testen wichtig.
     * 09.09.24: The original (hard to understand and maybe wrong) logic of treating each Face3List simplified. Why shouldn't we treat
     * faces overall? Our intention is to have own vertices for each face, so why do facelists separately?
     * But facelists might not be flatten later but split into separate geos! Probably that will cause even more problems.
     * Anyway, handle it on facelist level for now.
     *
     * @param vertices
     * @param faces3
     * @param vertexMap
     * @return
     */
    private /*List<Vector3>*/void duplicateVertices(List</*7.2.18 Native*/Vector3> vertices, /*List<Face3List>*/Face3List faces3, VertexMap vertexMap) {
        // We want to avoid vertices being shared across facelists (ensured by SmoothingMap), so we have
        // owner on facelist level and face level per facelist
        // key=vertex index, value=facelistindex!
        HashMap<Integer, Integer> vertexowner = new HashMap<Integer, Integer>();

        List</*7.2.18 Native*/Vector3> nvertices = new ArrayList</*7.2.18 Native*/Vector3>();
        int facelistindex = 0;
        //for (Face3List fl : List.of(faces3)) {
        // Pro Facelist Indizes auf alte Liste auf Indizes auf neue Liste mappen.
        /*19.9.24 HashMap<Integer, Face3> facemap = new HashMap<Integer, Face3>();
        for (Face f : faces3.faces) {
            Face3 face = (Face3) f;
            face.index0 = handleVertex(vertices, face.index0, facemap, nvertices, vertexowner, facelistindex, face, vertexMap);
            face.index1 = handleVertex(vertices, face.index1, facemap, nvertices, vertexowner, facelistindex, face, vertexMap);
            face.index2 = handleVertex(vertices, face.index2, facemap, nvertices, vertexowner, facelistindex, face, vertexMap);
        }*/

        int originalVertices = vertices.size();
        for (int index = 0; index < originalVertices; index++) {
            List<Face3> fl = vertexMap.map.get(index);
            // more inutitive, keep the initial
            // might be null for empty facelists (eg. backfaces)
            while (fl != null && fl.size() > 1) {
                Face3 face = fl.get(1);

                fl.remove(1);

                // hook face to a really new vertex at "index".
                vertexMap.hookFaceToNewVertex(face, index, vertices);

            }
        }
        facelistindex++;
        //}

        /*for (int index : vertexMap.map.keySet())
            vertices.addAll(nvertices);*/
        //return nvertices;
    }

    /**
     * Returns either original or replaced index.
     */
    private int handleVertex(List<Vector3> vertices, int index, HashMap<Integer, Face3> facemap, List<Vector3> nvertices, HashMap<Integer, Integer> vertexowner, int facelistindex, Face3 face, VertexMap vertexMap) {
        //Integer i;
        if (vertexMap.map.get(index).size() <= 1) {
            // no need to change it
            return index;
        }
        // decouple other faces
        //for (int i = 1; i < vertexMap.map.get(index).size(); i++) {
        if (true) return vertexMap.decoupleFace(vertices, index, 1);
        //}



        /*9.9.24 skip for now because don't know what the purpose really is*/
        if (vertexowner.get(index) == null) {
            // Vertex not assigned to any list. We can keep it, but register for list and face.
            vertexowner.put(index, facelistindex);
            facemap.put(index, face);
            return index;
        }

        // Neumapping und evtl. neuer Vertex erforderlich
        /*if (vertexowner.get(index) == facelistindex) {
            // Vertex ist dieser Facelist zugeordnet. Kann dann auch so uebernommen werden.
            return index;
        }*/

        // known in other list or other... Need new
        Face3 f;
        if (true/*(f = facemap.get(index)) != null*/) {
            nvertices.add(vertices.get(index));
            int i = vertices.size() + nvertices.size() - 1;
            //not needed because its new facemap.put(i, f);
            return i;
        }
        facemap.put(index, face);
        // Noch kein Mapping für diesen neuen Index/Vertex vorhanden
        return index;//(int) i;
    }
}
