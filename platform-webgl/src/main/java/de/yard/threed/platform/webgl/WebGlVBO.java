package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Vector2Array;
import de.yard.threed.core.Vector3Array;
import de.yard.threed.core.platform.Platform;


/**
 * TODO: Die Face Normale geht hier verloren. 26.4.17: Ist das eine ThreeJs Spezialität, das der die Normalen im Face will?
 * <p/>
 * Created by thomass on 12.02.16.
 */
public class WebGlVBO /*1.8.16 implements NativeVBO*/ {
    static Log logger = Platform.getInstance().getLog(WebGlVBO.class);
    JsArray vertices = (JsArray) JavaScriptObject.createArray();
    // Die UVs muessen fuer ThreeJS in die Face
    public JsArray faceuvs = (JsArray) JavaScriptObject.createArray();
    private JsArray uvs = (JsArray) JavaScriptObject.createArray();
    JsArray normals = (JsArray) JavaScriptObject.createArray();
    JsArray triangles = (JsArray) JavaScriptObject.createArray();

    public WebGlVBO(Vector3Array vertices, int[] indices, Vector2Array uvs, Vector3Array normals) {
        //this.vertices = ((WebGlVector3Array)vertices).arr;
        //this.normals = ((WebGlVector3Array)normals).arr;
        this.vertices = buildVector3Array(vertices);
        this.normals = buildVector3Array(normals);
        for (int i = 0; i < indices.length / 3; i++) {
            int index0 = indices[i * 3 + 0];
            int index1 = indices[i * 3 + 1];
            int index2 = indices[i * 3 + 2];
            WebGlFace3 face3 = new WebGlFace3(index0, index1, index2);
            triangles.push(face3.face3);
            JsArray uv = (JsArray) JavaScriptObject.createArray();
            Vector2 uvv = uvs.getElement(index0);
            uv.push(new WebGlVector2(uvv.getX(), uvv.getY()).vector2);
            uvv = uvs.getElement(index1);
            uv.push(new WebGlVector2(uvv.getX(), uvv.getY()).vector2);
            uvv = uvs.getElement(index2);
            uv.push(new WebGlVector2(uvv.getX(), uvv.getY()).vector2);
            faceuvs.push(uv);
        }
    }
    /*@Override
    public int addRow(Vector3 vertex, Vector3 normal, Vector2 uv) {
        vertices.push(((WebGlVector3) vertex).vector3);
        normals.push(((WebGlVector3) normal).vector3);
        uvs.push(new WebGlVector2(uv.getX(), uv.getY()).vector2);
        return vertices.length() - 1;
    }

    @Override
    public void addTriangle(int ili, int index0, int index1, int index2) {
        if (ili != 0){
            //throw new RuntimeException("ili != 0");
            logger.warn("ili!=0");
            ili = 0;
        }
        WebGlFace3 face3 = new WebGlFace3(index0, index1, index2);
        triangles.push(face3.face3);
        JsArray uv = (JsArray) JavaScriptObject.createArray();
        uv.push(uvs.get(index0));
        uv.push(uvs.get(index1));
        uv.push(uvs.get(index2));
        faceuvs.push(uv);
    }*/

    private static JsArray buildVector3Array(Vector3Array v) {
        JsArray arr = (JsArray) JavaScriptObject.createArray(v.size());
        for (int i = 0; i < v.size(); i++) {
            WebGlVector3 v3 = WebGlVector3.toWebGl(v.getElement(i));
            arr.set(i, v3.vector3);
        }
        return arr;
    }

    
    public JsArray getTriangles() {
        return triangles;
    }
}
