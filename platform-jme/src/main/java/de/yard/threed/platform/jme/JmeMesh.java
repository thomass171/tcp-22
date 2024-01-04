package de.yard.threed.platform.jme;


import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.util.TangentBinormalGenerator;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeGeometry;
import de.yard.threed.core.platform.NativeMaterial;
import de.yard.threed.core.platform.NativeMesh;

import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.javacommon.JALog;

import java.util.HashMap;
import java.util.Map;

/**
 * Im Sinne von JME ist ein Mesh eine Geometry.
 * 9.11.15 Naa? Das passt aber nicht so ganz. Denn man kann super() nicht mit einem JME Mesh
 * aufrufen. Andererseits wird aber nachher ein JME Mesh in eine JME Geometry reingesteckt.
 * Irgendwie scheint es tatsächlich so zu sein.
 * <p/>
 * 29.4.16: Teile des Mesh erstellen deswegen nach JmeGeometry verschoben.
 * <p/>
 * <p/>
 * 15.6.16: Auch nicht mehr von Object3D abgelkeitet
 * Created by thomass on 25.05.15.
 */
public class JmeMesh /*15.6.16 extends JmeObject3D*/ implements NativeMesh {
    //Geometry geometry;
    static Log logger = new JALog(/*LogFactory.getLog(*/JmeMesh.class);
    JmeSceneNode container = null;
    // Das Vorhalten des mat ist bequem wegen getMaterial, das Vorhalten der Geometry koennte aber
    // Resourcen verchwenden.
    // Geometry ist ein Spatial und das implementiert Collidable.
    Geometry geometry;
    JmeMaterial mat;
    //6.10.17: Die MEshes vorhalten, um sie liefern zu koennen. Doofe Kruecke erstmal , Aber JME hat eh keinen Focus.
    // 4.11.19: Warum so eine Liste? Ich kenn das Mesh doch. Weil die Wrapper evtl. flüchtig sind! Und userdata kann nur integer speichern.
    //Lieber map als Liste, damit es nicht endlos wächst. Mit eigenem JME build koennte man es aber doch dort als userdata unterbringen.
    public static int meshid = 1;
    public static Map<Integer, JmeMesh> meshes = new HashMap<>();
    
    JmeMesh(Geometry geometry, JmeMaterial mat) {
        this.mat = mat;
        this.geometry = geometry;
        //super(geometry);
    }


    /*29.4.16 public static JmeMesh buildGeometry(JmeGeometry geometry, JmeMaterial mat, boolean castShadow, boolean receiveShadow) {
        Geometry geom = new Geometry(JmeScene.getInstance().getUniqueName(), geometry.geometry);
        geom.setMaterial(mat.material);
        geom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);//TODO parameter
        if (mat.isTransparent()) {
            geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        }
        return new JmeMesh(geom);
    }*/

    /**
     * Ein leeres Mesh als Containerkruecke, weil ein echtes Jme Mesh(Spatial) keine Childs hat.
     */
    public static JmeMesh buildContainerMesh(JmeSceneNode container) {
        JmeMesh m = new JmeMesh(null, null);
        m.container = container;
        return m;
    }

    /**
     * Aus http://wiki.jmonkeyengine.org/doku.php/jme3:advanced:custom_meshes
     */
    public static JmeMesh buildMesh(JmeGeometry geo, JmeMaterial mat, boolean castShadow, boolean receiveShadow, boolean isLine) {
        Mesh mesh = geo.geometry;
        if (isLine){
            mesh.setMode(Mesh.Mode.Lines);
        }
        Geometry geom = new Geometry(((PlatformJme) Platform.getInstance()).getUniqueName(), mesh);
        geom.setMaterial(mat.material);
        if (castShadow) {
            if (receiveShadow) {
                geom.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
            } else {
                geom.setShadowMode(RenderQueue.ShadowMode.Cast);
            }
        } else {
            if (receiveShadow) {
                geom.setShadowMode(RenderQueue.ShadowMode.Receive);
            } else {
                geom.setShadowMode(RenderQueue.ShadowMode.Off);
            }
        }
        if (mat.isTransparent()) {
            geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        }
        // 23.3.17: Warum er das braucht ist unklar, aber damit gehen normal maps.
        // 25.9.19: Und normals braucht er dafuer auch. Warum wohl?
        if (mat.hasNormalMap) {
            TangentBinormalGenerator.generate(mesh);
        }
        return new JmeMesh(geom, mat);
    }

    public void updateMesh(NativeGeometry nativeGeometry,/*List<Vector3> vertices, List<Face3List> faces,*/ NativeMaterial material/*, List<Vector3> normals*/) {
        /*if (faces != null) {
            throw new RuntimeException("not supported for faces");
        }*/
        JmeGeometry geo = (JmeGeometry) nativeGeometry;
        if (geo != null/*vertices != null || faces != null*/) {


            Mesh mesh = geometry.getMesh();//((Geometry) spatial).getSceneNode();
            //JmeGeometry.setMeshBuffer(mesh, vbo.vertices.toArray(new Vector3f[0]), vbo.getIndexes(), vbo.uvs.toArray(new Vector2f[0]), vbo.normals.toArray(new Vector3f[0]));
            geometry.setMesh(geo.geometry);
        }
        if (material != null) {
            geometry.setMaterial(((JmeMaterial) material).material);
            this.mat = (JmeMaterial) material;
            if (((JmeMaterial) material).isTransparent()) {
                /*spatial.*/
                geometry.setQueueBucket(RenderQueue.Bucket.Transparent);
            }
        }
    }

    @Override
    public NativeMaterial getMaterial() {
        return mat;
    }

    @Override
    public void setBoxColliderSizeHint(Vector3 size) {
        // ignored, JME scheint das nicht zu brauchen
    }

    @Override
    public NativeSceneNode getSceneNode() {
        return null;
    }
}
