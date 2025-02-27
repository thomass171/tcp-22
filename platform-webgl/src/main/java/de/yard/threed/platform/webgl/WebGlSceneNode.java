package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import de.yard.threed.core.platform.*;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomass on 25.04.15.
 * <p/>
 * ThreeJs verwendet Quaternions und hat keine diskreten Rotationswerte fuer einzelne Achsen.
 * 25.2.16: No longer extends Base3D but Object3D now.
 * 15.11.16:
 * No longer an extension of any ThreeJs object. Now its just a simple container object, containing a ThreeJs object.
 * The ThreeJs object also provides the transform and might contain a mesh as child(!). (Really?, ist doch parallel. 7.10.17: yes, really).
 * 23.3.17: Light analog.
 */
public class WebGlSceneNode implements NativeSceneNode {
   static Log logger = new WebGlLog(WebGlSceneNode.class.getName());

    // a light attached to this node
    WebGlLight light;
    WebGlObject3D object3d;

    WebGlSceneNode(JavaScriptObject nativeobject3d) {
        this.object3d = new WebGlObject3D(nativeobject3d, this, false);
        //super(mesh);
        if (WebGlScene.webglscene == null) {
            logger.error("no webgl scene");
        }
        //weil der add ein setParent wurde. Muss noch ausgefeilt werden.
        //Geadded wird hier genau genommen das object3d.
        //20.10.18: Always add? It might already exist in the scene.
        WebGlScene.webglscene.add(this);
    }

    WebGlSceneNode(JavaScriptObject nativeobject3d, boolean exists) {
        this.object3d = new WebGlObject3D(nativeobject3d, this, true);
    }

    public WebGlSceneNode(String name) {
        this(WebGlObject3D.buildObject3D());
    }

    @Override
    public void setMesh(NativeMesh pmesh) {
        WebGlMesh mesh = (WebGlMesh) getMesh();
        if (mesh != null) {
            // bestehendes Mesh entfernen
            object3d.remove(mesh);
        }
        mesh = (WebGlMesh) pmesh;

        int cnt = object3d.add(mesh/*object3d*/);
        //object3d.meshholderindex=cnt-1;
        //WebGlObject3D.setMeshholder(mesh.object3d.object3d);
    }

    @Override
    public NativeMesh getMesh() {
        return object3d.getMeshHolder();
    }

    /**
     * 23.3.17: Aehnlich MEsh. Light ist eigentlich aber eigenstaendig.
     * Noch unfertig, erstmal wieder in Scene .
     */
    @Override
    public void setLight(NativeLight plight) {
        if (light != null) {
            // bestehendes light entfernen
            removeLight();
        }
        light = (WebGlLight) plight;
        //Node n = (Node) object3d.spatial;
        // n.addLight(light.light/*spatial*/);
        WebGlScene.webglscene.add(plight);

    }

    private void removeLight() {
        if (light == null) {
            return;
        }
        WebGlScene.webglscene.remove(light);
        light = null;
    }

    @Override
    public int getUniqueId() {
        return object3d.getId();
    }

    @Override
    public void destroy() {
        removeLight();

        // Gegenstueck zum obigen scnee add
        WebGlScene.webglscene.remove(this);

    }

    @Override
    public boolean isDestroyed() {
        return object3d == null;
    }

    @Override
    public void setName(String name) {
        object3d.setName(name);
    }

    @Override
    public String getName() {
        return object3d.getName();
    }

    @Override
    public NativeTransform getTransform() {
        return object3d;
    }


    /**
     * Wenn dies der Carrier ist, gibt es ein Child das Camera ist. So wie mit MEsh
     *
     * @return
     */
    @Override
    public NativeCamera getCamera() {
        for (NativeCamera nc : AbstractSceneRunner.getInstance().getCameras()) {
            WebGlCamera c = (WebGlCamera) nc;
            if (c.carrier.isSame(object3d)) {
                return c;
            }
        }
        return null;
    }

    /**
     * 20.8.24: Additional platform finder. Might be more efficient by using threejs finder?
     */
    public List<NativeSceneNode> findNodeByName(String name) {
        List<NativeSceneNode> nodelist = new ArrayList<NativeSceneNode>();
        // 3.1.18: Also check 'this'.
        if (name.equals(this.getName())) {
            nodelist.add(this);
        }
        for (NativeTransform child : object3d.getChildren()) {
            if (child != null) {
                NativeSceneNode csn = child.getSceneNode();
                if (csn != null) {
                    nodelist.addAll(((WebGlSceneNode)csn).findNodeByName(name));
                }
            }
        }
        return nodelist;
    }
}
