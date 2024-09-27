package de.yard.threed.core.geometry;

import de.yard.threed.core.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A helper class for normal calculation. Typically exists for a single facelist.
 * Its a map of vertex index to connected Face3 and independent from the location of the vertex (vertices
 * at other indexes might have the same location).
 *
 * <p>
 * Siehe Wiki.
 * <p>
 * 8.9.24: According to a comment in testRotatedRectangle(), SmoothingMap didn't solve the railing artifact issue.
 * So is it really useful? Yes, it is.
 * Renamed from SmoothingMap to VertexMap because its not only for smoothing.
 * <p>
 * Created by thomass on 15.12.16.
 */
public class VertexMap {
    public Map<Integer, List<Face3>> map = new HashMap<Integer, List<Face3>>();
    // key was replaced by values. One vertex can be replaced several times.
    Map<Integer, List<Integer>> replaceMap = new HashMap<>();

    /**
     * Create a SmoothingMap for each facelist.
     * Avoid sharing vertices across facelists. So "vertices" might be extended here.
     *
     * @param vertices
     * @param faces3
     * @return
     */
    public static List<VertexMap> prepareVertexMaps(List<Vector3> vertices, List<Face3List> faces3) {
        List<VertexMap> smoothingMaps = new ArrayList<>();
        for (Face3List fl : faces3) {
            smoothingMaps.add(buildSmoothingMap(vertices, fl, smoothingMaps));
        }
        return smoothingMaps;
    }

    public void add(int index, Face3 face) {
        List<Face3> list = map.get(index);
        if (list == null) {
            list = new ArrayList<Face3>();
            map.put(index, list);
        }
        list.add(face);
    }

    public static VertexMap buildSmoothingMap(List<Vector3> vertices, Face3List faces, List<VertexMap> knownVertexMaps) {
        VertexMap smoothingMap = new VertexMap();
        // for (Face3List fl : faces) {
        for (Face gface : faces.faces) {
            Face3 face = (Face3) gface;
            face.index0 = addToMap(vertices, face.index0, face, smoothingMap, knownVertexMaps);
            face.index1 = addToMap(vertices, face.index1, face, smoothingMap, knownVertexMaps);
            face.index2 = addToMap(vertices, face.index2, face, smoothingMap, knownVertexMaps);
                /*smoothingMap.add(face.index0, face);
                smoothingMap.add(face.index1, face);
                smoothingMap.add(face.index2, face);*/
        }
        //}
        return smoothingMap;
    }

    /**
     * Vertex replacement here is only done for splitting several face lists, not for later normal building.
     */
    private static int addToMap(List<Vector3> vertices, int index, Face3 face, VertexMap smoothingMap, List<VertexMap> knownVertexMaps) {
        boolean alreadyInOtherList = false;
        for (VertexMap sm : knownVertexMaps) {
            if (sm.map.keySet().contains(index)) {
                alreadyInOtherList = true;
            }
        }
        if (alreadyInOtherList) {
            int replacingIndex = smoothingMap.duplicateIndex(vertices, index, false);

            index = replacingIndex;
        }
        smoothingMap.add(index, face);
        return index;
    }

    public int duplicateIndex(List<Vector3> vertices, int index, boolean force) {
        int replacingIndex;
        if (replaceMap.containsKey(index) && !force) {
            // already replaced. Without 'force', there is only one
            replacingIndex = replaceMap.get(index).get(0);
        } else {
            vertices.add(vertices.get(index));
            replacingIndex = vertices.size() - 1;
            if (!replaceMap.containsKey(index)) {
                replaceMap.put(index, new ArrayList<Integer>());
            }
            replaceMap.get(index).add(replacingIndex);
        }
        return replacingIndex;
    }

    /**
     * Without modifying face itself!
     */
    public int decoupleFace(List<Vector3> vertices, int index, int facePosition) {
        Face3 face = map.get(index).get(facePosition);
        map.get(index).remove(facePosition);

        int replacingIndex = duplicateIndex(vertices, index,true);
        map.put(replacingIndex, new ArrayList<>());
        map.get(replacingIndex).add(face);
        return  replacingIndex;
    }

    public int getOriginal(int duplicatedOrOriginalIndex) {
        for (int key:replaceMap.keySet()){
            if (key==duplicatedOrOriginalIndex || replaceMap.get(key).contains(duplicatedOrOriginalIndex)){
                return key;
            }
        }

        return -1;
    }

    /**
     * But doen't remove the face from its current list! Needs to be done by caller.
     */
    public void hookFaceToNewVertex(Face3 face, int index, List<Vector3> vertices) {
        int replacingIndex = this.duplicateIndex(vertices, index, true);
        this.map.put(replacingIndex, new ArrayList<>());
        this.map.get(replacingIndex).add(face);
        face.replaceIndex(index, replacingIndex);
    }

    public int duplicateVertex(int index, List<Vector3> vertices) {
        int replacingIndex = this.duplicateIndex(vertices, index, true);
        this.map.put(replacingIndex, new ArrayList<>());
        //this.map.get(replacingIndex).add(face);
        //face.replaceIndex(index, replacingIndex);
        return replacingIndex;
    }

    public void hookFaceToVertex(Face3 face, int index, int replacingIndex) {
        //int replacingIndex = this.duplicateIndex(vertices, index, true);
        //this.map.put(replacingIndex, new ArrayList<>());
        this.map.get(replacingIndex).add(face);
        face.replaceIndex(index, replacingIndex);
    }
}
