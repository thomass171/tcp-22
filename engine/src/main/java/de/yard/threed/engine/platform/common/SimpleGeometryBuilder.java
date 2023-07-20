package de.yard.threed.engine.platform.common;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.CustomGeometry;
import de.yard.threed.engine.geometry.IndexList;

import java.util.ArrayList;
import java.util.List;

/**
 * Build a geometry step by step. Somehow its a {@link CustomGeometry}, but not that confuse.
 * 19.7.23
 */
public class SimpleGeometryBuilder {

    List<Vector2> uvs = new ArrayList<Vector2>();
    List<Vector3> vertices = new ArrayList<Vector3>();
    List<Vector3> normals = new ArrayList<Vector3>();
    IndexList indexes = new IndexList();

    public int addVertex(Vector3 v, Vector3 n, Vector2 uv) {
        vertices.add(v);
        normals.add(n);
        uvs.add(uv);
        return vertices.size() - 1;
    }

    public void addFace(int a, int b, int c) {
        indexes.add(a, b, c);
    }

    public SimpleGeometry getGeometry() {
        return new SimpleGeometry(vertices,uvs,normals,indexes.getIndices());
    }
}
