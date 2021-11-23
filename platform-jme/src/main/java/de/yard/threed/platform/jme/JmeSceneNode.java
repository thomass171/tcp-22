package de.yard.threed.platform.jme;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import de.yard.threed.core.platform.*;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.javacommon.JALog;

/**
 * Created by thomass on 25.05.15.
 * <p/>
 * Das ist in JME am ehesten ein Spatial, auf jeden fall eine Superklasse von Geometry (was unser MEsh ist).
 * Oder doch besser Node. Der hat dann auch einen append.
 * <p/>
 * 15.6.16: Umbenannt von Model nach SceneNode.
 * 21.7.16: Alle SceneNodes werden in der Platform in einer Map vorgehalten.
 * 14.11.16: Seit Trennung von Object3D braucht er eigenen Name.
 * 23.12.16: Der Aufbau ist: SceneNode enthält ein Platform 3D Objekt, das wiederum ein Mesh enthaelt.
 * 23.3.17: Light analog.
 * 23.9.17: MA17: Doch versuchen ohne Liste und Id. Bei suchen immer neu instantiieren.
 */
public class JmeSceneNode /*16.9.16 extends JmeSpatial*/ implements NativeSceneNode {
    //4.11.19: Jetzt in Spatial JmeMesh mesh = null;
    JmeLight light = null;
    JmeSpatial object3d;
    Log logger = new JALog(JmeSceneNode.class);

    JmeSceneNode(String name) {
        /*super*/
        object3d = new JmeSpatial(new Node((name != null) ? name : JmeScene.getInstance().getUniqueName()), this, false);
    }

    /**
     * Neue Istanz für existierende Node.
     *
     * @param spat
     */
    JmeSceneNode(Spatial spat) {
        /*super*/
        object3d = new JmeSpatial(spat, this, true);
    }
    
    /*14.11.16 JmeSceneNode(Node model) {
        //Die Id nicht neu vergeben, denn die muss ja schon gesetzt sein.
        /*super* /object3d=new JmeSpatial(model,true);
        if (model == null){
            throw new RuntimeException("model node isType null");
        }
        //spatial.setUserData("uniqueid",uniqueid++);
    }*/

   /* JmeModel(Node s) {
        super(s);
        //this.model = s;
    }*/


    /**
     * 6.6.15 Ob das hier so gut ist??
     */
  /*  @Override
    public void addCamera(NativeCamera camera) {
        //((Node) spatial).a(((JmeMesh) obj).spatial);
         /*((JmeModel) model).* /getNode().attachChild(((JmeCamera)camera).cameraNode);
    }*/

    /*public static JmeMatrix4 buildMatrix4FromTransform(Transform transform) {
        return Matrix4.buildTransformationMatrix(new Vector3(new JmeVector3(transform.getTranslation())),new Quaternion(new JmeQuaternion(transform.getRotation())));
    }*/

    Node getNode() {
        return (Node) /*super. */object3d.spatial;
    }


    @Override
    public void setMesh(NativeMesh pmesh) {
        JmeMesh mesh = (JmeMesh) getMesh();
        if (mesh != null) {
            // bestehendes Mesh entfernen
            Integer meshindex = mesh.geometry.getUserData("meshholder");
            JmeMesh.meshes.remove(meshindex);
            //removeFromParent() reicht angeblich, damit GC abraeumt
            mesh.geometry.removeFromParent();
        }
        mesh = (JmeMesh) pmesh;
        Node n = (Node) object3d.spatial;
        int cnt;
        //statt meshholderindex im Objekt selber markieren.
        int meshindex = JmeMesh.meshid++;//meshes.size() - 1;
        JmeMesh.meshes.put(meshindex, mesh);
        mesh.geometry.setUserData("meshholder", new Integer(meshindex));
        if (mesh.container != null) {
            cnt = n.attachChild(mesh.container.object3d.spatial);

        } else {
            cnt = n.attachChild(mesh.geometry/*spatial*/);
        }
        //object3d.meshholderindex=cnt-1;
    }


    @Override
    public NativeMesh getMesh() {
        return object3d.getMeshHolder();
    }

    /**
     * 23.3.17: Aehnlich Mesh. Light ist eigentlich aber eigenstaendig.
     * Muss aus unbekannten Gründen in die RootNode. Vielleicht auch nur directional.
     * Auf jeden Fall ist das hier zunächst mal eine ziemliche Krücke.
     */
    @Override
    public void setLight(NativeLight plight) {
        if (light != null) {
            // bestehendes light entfernen
            removeLight();
        }
        light = (JmeLight) plight;
        //Node n = (Node) object3d.spatial;
        // n.addLight(light.light/*spatial*/);
        JmeScene.getInstance().add(plight);
    }

    private void removeLight(){
        if (light == null){
            return;
        }
        JmeScene.getInstance().remove(light);
        light = null;
    }
    
    /*@Override
    public int getUniqueId() {
        // Die root node hat implizit die 0
        if (object3d.spatial == JmeScene.getInstance().getRootNode()){
            return 0;
        }
        //14.11.16: TODO braucht der jetzt nicht eine eigene?
        return ((Integer) object3d.spatial.getUserData("uniqueid"));
    }*/

    @Override
    public void destroy() {
        if (JmeSpatial.getLayerOfSpatial(object3d.spatial) > 0) {
            //remove spatial from the corresponding camera.
            for (NativeCamera c : AbstractSceneRunner.getInstance().getCameras()) {
                JmeCamera jmeCamera = (JmeCamera) c;
                jmeCamera.getViewPort().detachScene(object3d.spatial);
            }
        }
        // 2.4.19: Muss mesh nicht auch entfernt werden?
        removeLight();
        object3d.remove(this);

    }


    @Override
    public boolean isDestroyed() {
        return object3d.spatial == null;
    }

    @Override
    public void setName(String name) {
        object3d.spatial.setName(name);
    }

    @Override
    public String getName() {
        return object3d.spatial.getName();
    }

    @Override
    public NativeTransform getTransform() {
        return object3d;
    }


    @Override
    public NativeCamera getCamera() {
        for (NativeCamera nc : AbstractSceneRunner.getInstance().getCameras()) {
            JmeCamera c = (JmeCamera) nc;
            if (c.carrier.object3d.spatial == this.object3d.spatial) {
                return c;
            }
        }
        return null;
    }
}
