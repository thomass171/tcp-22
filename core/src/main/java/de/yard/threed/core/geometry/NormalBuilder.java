package de.yard.threed.core.geometry;

import de.yard.threed.core.Vector3;

import java.util.List;
import java.util.Map;

/**
 * More abstract/generic replacement for SmoothingMap.
 */
public interface NormalBuilder {
    /**
     * Might duplicate vertices and change faces if normals cannot be shared across
     * faces/vertices (in case of no full smooth shading).
     * Returns normals for vertices by index
     */
    Map<Integer, Vector3> calculateVertexNormals(List<Vector3> vertices, Face3List faces, VertexMap vertexMap);
}
