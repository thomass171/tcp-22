package de.yard.threed.platform.jme;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.BufferUtils;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.NativeGeometry;
import de.yard.threed.core.Vector2Array;
import de.yard.threed.core.Vector3Array;
import de.yard.threed.engine.platform.common.SimpleGeometry;


/**
 * Ist im JME Verstaendnis eigentlich ein Mesh
 */
public class JmeGeometry implements NativeGeometry {
    Mesh geometry;

    private JmeGeometry(Mesh geometry) {
        this.geometry = geometry;
    }

    public static JmeGeometry buildCubeGeometry(float width, float height, float depth) {
        // Bei JME sind die Parameter die Extensions vom Center in JEDE Richtung auf der Achse
        return new JmeGeometry(new Box(width / 2, height / 2, depth / 2));
    }

    @Override
    public String getId() {
        Util.notyet();
        return null;
    }

    public static NativeGeometry buildSphereGeometry(float radius, int wsegs, int hsegs) {
        return new JmeGeometry(new Sphere(wsegs, hsegs, radius));
    }

    public static NativeGeometry buildCylinderGeometry(float radiusBottom, float radiusTop, float height, int radialSegments) {
        // geschlossen oder offen? erstmal offen
        return new JmeGeometry(new Cylinder(2, radialSegments, radiusBottom, radiusTop, height, false, false));
    }

    /*public static JmeGeometry buildPlaneGeometry(float width, float height, int widthSegments, int heightSegments) {
        return new JmeGeometry(buildNativePlaneGeometry(width, height, widthSegments, heightSegments));
    }

    public static JmeGeometry buildTubeGeometry(NativeCurve path, int segments, float radius, int radialSegments, boolean closed, boolean debug) {
        return new JmeGeometry(buildNativeTubeGeometry(path, segments, radius, radialSegments, closed, debug));
    }*/

    public static JmeGeometry buildGeometry(SimpleGeometry simpleGeometry) {
        return JmeGeometry.buildGeometry(simpleGeometry.getVertices(), /*new SmartArrayList<Face3List>(*/simpleGeometry.getIndices(), simpleGeometry.getUvs(), simpleGeometry.getNormals());
    }

    /**
     * Aus http://wiki.jmonkeyengine.org/doku.php/jme3:advanced:custom_meshes
     */
    public static JmeGeometry buildMesh(Vector3f[] vertices, int[] indexes, Vector2f[] texCoord, Vector3f[] normals/*, JmeMaterial mat, boolean castShadow, boolean receiveShadow*/) {
        Mesh mesh = new Mesh();
        setMeshBuffer(mesh, vertices, indexes, texCoord, normals);
        return new JmeGeometry(mesh);
    }

    public static void setMeshBuffer(Mesh mesh, Vector3f[] vertices, int[] indexes, Vector2f[] texCoord, Vector3f[] normals) {
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        if (texCoord != null) {
            if (texCoord.length != vertices.length) {
                throw new RuntimeException("texccord wrong:" + texCoord.length + "!=" + vertices.length);//TODO so ein Fehler muss anders behandelt werden. Und auch nicht hier, sondern schon moeglichst plattformuebergreifend
            }
            mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
        }
        mesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indexes));

        if (normals != null) {
            if (normals.length != vertices.length) {
                throw new RuntimeException("normals wrong");//TODO so ein Fehler muss anders behandelt werden. Und auch nicht hier, sondern schon moeglichst plattformuebergreifend
            }
            mesh.setBuffer(VertexBuffer.Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
        }

        //To render the mesh in the scene, we need to pre-calculate the bounding volume of our new mesh:
        mesh.updateBound();
    }

    public static JmeGeometry buildGeometry(Vector3Array vertices,/*List<* /Face3List*/int[] indices, Vector2Array uvs, Vector3Array normals) {
        //JmeVBO vbo = moveToNative(vertices, indices,uvs, normals);
        Vector3f[] arr = buildVector3Array(vertices);
        return buildMesh(arr, indices, buildVector2Array(uvs), (normals!=null)?buildVector3Array(normals):null);
    }

    private static Vector3f[] buildVector3Array(Vector3Array v) {
        Vector3f[] arr = new Vector3f[v.size()];
        for (int i = 0; i < v.size(); i++) {
            arr[i] = (JmeVector3.toJme(v.getElement(i)));
        }
        return arr;
    }

    private static Vector2f[] buildVector2Array(Vector2Array v) {
        Vector2f[] arr = new Vector2f[v.size()];
        for (int i = 0; i < v.size(); i++) {
            Vector2 v2 = v.getElement(i);
            arr[i] = new Vector2f((float)v2.getX(),(float)v2.getY());
        }
        return arr;
    }
    
    /*23.9.19 static JmeVBO moveToNative(Vector3Array vertices, /*List<* /Face3List* /int[] faces, Vector2Array uvs, Vector3Array normals) {
        JmeVBO vbo = new JmeVBO();
        GeometryHelper.buildVBOandTriangles(vertices, faces, uvs, vbo, normals);
        return vbo;
    }*/
}
