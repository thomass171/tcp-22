package de.yard.threed.engine.platform.common;

import de.yard.threed.core.Matrix4;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;

import de.yard.threed.core.Vector2Array;

import de.yard.threed.core.Vector3Array;
import de.yard.threed.core.platform.Platform;

import java.util.List;

/**
 * Geometrie quasi im VBO Format. Keine Faces. Kennt schon die Normalen und UVs.
 * 16.2.16
 * 6.3.16 und optional eine Normalliste.
 * 18.7.16: normals jetzt Pflicht.
 * 23.11.16: Was ist denn die Abgrenzung zu einer CustomGeometry? Eine SimpleGeometry ist quasi fertig, um sie in die Platform zu stecken.
 * Mit einer CustomGeometry kann noch gearbeitet werden. Wenn sie dann fertig ist, wird daraus eine SimpleGeometry gemacht.
 * Vor allem aber enthält eine SimpleGeometry FloatBuffer, was einen deutlichen Performanceschub bringt.
 * 9.3.17: So ganz simple ist das nicht, siehe Primitives. Die verwenden auch SimpleGeometry und werten die damit auf.
 * Mit SimpleGeometry kann man jetzt composen. Arbeitet allerdings auf den Native Objekten. Das ist nicht so ganz schön.
 * 25.7.18: Es gibt hier keine obligatorische Validierung (mit Exception oder sowas), die Geo ist wie sie ist und die Platform versucht das beste
 * draus zu machen. Wens interessiert, der kann trotzdem validate() aufrufen.
 */
public class SimpleGeometry {
    Log logger = Platform.getInstance().getLog(SimpleGeometry.class);
    private Vector3Array vertices;
    private Vector3Array normals;
    private int[] indices;
    private Vector2Array uvs;

    /**
     * leere Geometrie.
     */
    public SimpleGeometry() {
        this.vertices = Platform.getInstance().buildVector3Array(0);
        this.indices = new int[0];
        this.uvs = Platform.getInstance().buildVector2Array(0);
        this.normals = Platform.getInstance().buildVector3Array(0);
    }

    public SimpleGeometry(List</*7.2.18 Native*/Vector3> pvertices, Face3List faces, List<Vector3> pnormals) {
        this.vertices = Platform.getInstance().buildVector3Array(pvertices.size());
        this.indices = new int[faces.faces.size() * 3];
        this.uvs = Platform.getInstance().buildVector2Array(pvertices.size());
        int vcnt = vertices.size();
        int idx = 0;

        for (int i = 0; i < pvertices.size(); i++) {
            this.vertices.setElement(i, pvertices.get(i));
        }
        this.normals = Platform.getInstance().buildVector3Array(pnormals.size());
        ;
        for (int i = 0; i < pnormals.size(); i++) {
            this.normals.setElement(i, pnormals.get(i));
        }

        for (Face f : faces.faces) {
            Face3 f3 = (Face3) f;
            indices[idx++] = f3.index0;
            indices[idx++] = f3.index1;
            indices[idx++] = f3.index2;
            if (f3.uv != null && f3.uv[0] != null) {
                // dann muessen die anderen beiden uvs auch gesetzt sein.
                uvs.setElement(f3.index0, (float) f3.uv[0].getX(), (float) f3.uv[0].getY());
                uvs.setElement(f3.index1, (float) f3.uv[1].getX(), (float) f3.uv[1].getY());
                uvs.setElement(f3.index2, (float) f3.uv[2].getX(), (float) f3.uv[2].getY());
            } else {
                uvs.setElement(f3.index0, 0, 0);
                uvs.setElement(f3.index1, 0, 0);
                uvs.setElement(f3.index2, 0, 0);
            }

        }
        // Luecken im uv Vector fuellen
        vcnt = uvs.size();
        for (int i = 0; i < vcnt; i++) {
            if (uvs.getElement(i) == null) {
                uvs.setElement(i, 0, 0);
            }
        }
        //for (Face gface : /*fl.*/faces.faces) {
        //    Face3 face = (Face3) gface;
        //vbo.addTriangle(0, faces);//face.index0, face.index1, face.index2);
        // }

    }

    public SimpleGeometry(List<Vector3> pvertices, int[] pindices, List<Vector2> puvs, List<Vector3> pnormals) {
        this.indices = pindices;
        this.uvs = Platform.getInstance().buildVector2Array(puvs.size());
        for (int i = 0; i < puvs.size(); i++) {
            this.uvs.setElement(i, (float) puvs.get(i).getX(), (float) puvs.get(i).getY());
        }
        this.vertices = Platform.getInstance().buildVector3Array(pvertices.size());
        for (int i = 0; i < pvertices.size(); i++) {
            this.vertices.setElement(i, pvertices.get(i));
        }
        this.normals = Platform.getInstance().buildVector3Array(pnormals.size());
        for (int i = 0; i < pnormals.size(); i++) {
            this.normals.setElement(i, pnormals.get(i));
        }
    }

    public SimpleGeometry(List<Vector3> pvertices, List<Vector2> puvs, List<Vector3> pnormals, int[] pindices) {
        this.indices = pindices;
        this.uvs = Platform.getInstance().buildVector2Array(puvs.size());
        for (int i = 0; i < puvs.size(); i++) {
            this.uvs.setElement(i, (float) puvs.get(i).getX(), (float) puvs.get(i).getY());
        }
        this.vertices = Platform.getInstance().buildVector3Array(pvertices.size());
        for (int i = 0; i < pvertices.size(); i++) {
            this.vertices.setElement(i, pvertices.get(i));
        }
        if (pnormals != null) {
            //2.5.19: Normale sind nicht mandatory
            this.normals = Platform.getInstance().buildVector3Array(pnormals.size());
            for (int i = 0; i < pnormals.size(); i++) {
                this.normals.setElement(i, pnormals.get(i));
            }
        }
    }

    public SimpleGeometry(Vector3Array vertices, int[] indices, Vector2Array uvs, Vector3Array normals) {
        this.vertices = vertices;
        this.indices = indices;
        this.uvs = uvs;
        this.normals = normals;
    }

    public Vector3Array getVertices() {
        return vertices;
    }

    /*public Face3List getFaces() {
        return faces;
    }*/

    public Vector3Array getNormals() {
        return normals;
    }

    public int[] getIndices() {
        return indices;
    }

    public Vector2Array getUvs() {
        return uvs;
    }

    /**
     * Die Vertices transformen. Der Rest bleibt unveraendert.
     * Normale müssen aber auch, wegen Rotation. Die Translation ist bei den Normalen egal.
     *
     * @param m
     * @return
     */
    public SimpleGeometry transform(Matrix4 m) {
        Vector3Array nvertices = Platform.getInstance().buildVector3Array(vertices.size());
        Vector3Array nnormals = Platform.getInstance().buildVector3Array(normals.size());
        for (int i = 0; i < nvertices.size(); i++) {
            nvertices.setElement(i, m.transform(vertices.getElement(i)));
            nnormals.setElement(i, m.transform(normals.getElement(i)));
        }
        return new SimpleGeometry(nvertices, indices, uvs, nnormals);
    }

    /**
     * Eine Geo hinzufuegen. Ändert nicht die aktuelle sondern liefert die Summe.
     *
     * @param geometry
     */
    public SimpleGeometry add(SimpleGeometry geometry) {
        Vector3Array nvertices = joinVector3Array(vertices, geometry.getVertices());
        Vector3Array nnormals = null;
        //2.5.19: Normale sind nicht mandatory
        if ((normals == null) != (geometry.normals == null)) {
            logger.error("inconsistent normals");
            return null;
        }
        if (normals != null) {
            nnormals = joinVector3Array(normals, geometry.getNormals());
        }
        int vcnt = vertices.size();
        Vector2Array nuvs = Platform.getInstance().buildVector2Array(uvs.size() + geometry.getUvs().size());
        for (int i = 0; i < uvs.size(); i++) {
            nuvs.setElement(i, uvs.getElement(i));
        }
        for (int i = 0; i < geometry.uvs.size(); i++) {
            nuvs.setElement(vcnt + i, geometry.uvs.getElement(i));
        }
        int offset = indices.length;
        int[] ni = new int[indices.length + geometry.indices.length];
        for (int i = 0; i < indices.length; i++) {
            ni[i] = indices[i];
        }
        for (int i = 0; i < geometry.indices.length; i++) {
            ni[i + offset] = geometry.indices[i] + vcnt;
        }
        return new SimpleGeometry(nvertices, ni, nuvs, nnormals);
    }

    /**
     * Liefert eine ValidationNotiz oder null, wenn alles OK ist.
     *
     * @return
     */
    public String validate() {
        String s = "";
        //2.5.19: Normale sind nicht mandatory
        if (normals != null && vertices.size() != normals.size()) {
            s += "vertices.size() != normals.size()";
        }
        if (vertices.size() != uvs.size()) {
            s += "vertices.size() != uvs.size()";
        }
        if (indices.length % 3 != 0) {
            s += "indices not multiple of 3";
        }
        for (int index : indices) {
            if (index < 0 || index >= vertices.size()) {
                s += "invalid index " + index;
            }
        }
        return StringUtils.length(s) == 0 ? null : s;
    }

    private static Vector3Array joinVector3Array(Vector3Array v1, Vector3Array v2) {
        int offset = v1.size();
        Vector3Array nv = Platform.getInstance().buildVector3Array(v1.size() + v2.size());
        for (int i = 0; i < v1.size(); i++) {
            nv.setElement(i, v1.getElement(i));
        }
        for (int i = 0; i < v2.size(); i++) {
            nv.setElement(i + offset, v2.getElement(i));
        }
        return nv;
    }
}
