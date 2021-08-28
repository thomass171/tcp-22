package de.yard.threed.platform.jme;

import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.*;


import de.yard.threed.javacommon.JALog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomass on 25.05.15.
 * <p/>
 * Das ist in JME am ehesten ein Spatial, auf jeden fall eine Superklasse von Geometry (was unser MEsh ist).
 * Oder doch besser Node. Der hat dann auch einen append.
 * <p/>
 * 26.1.16: Wegen des Umgangs mit Parents implementiert der jetzt ObjectBase3D statt Base3D
 */
public class JmeSpatial implements /*Native*/NativeTransform {
    //TODO 9.11.15: Wenn spatial die Superklasse einer Geometrie ist,halten wir hier die ganze Geometrie vor. Das koennte
    //Ressourcenverschwendung sein.
    Spatial spatial;
    Log logger = new JALog(/*LogFactory.getLog(*/JmeSpatial.class);
    // Die rootnode hat implizit die 0
    static int uniqueid = 1;
    // Die SceneNode, deren Komponente dies Object3D ist
    // MA17: auch ueber Neuinstanz liefern
    //private JmeSceneNode parentscenenode;
    // 5.1.17: MErken ob hier ein Mesh als Child drinhaengt, denn das ist kein Child im Sinne der Platform.
    //6.10.17 jetzt ueber userdata public int meshholderindex = -1;
    //4.11.19: Warum kann ich mir den Mesh nicht einfach hier merken statt in einer Liste? Weil diese Wrapper flüchtig sind!
    //JmeMesh mesh=null;

    JmeSpatial(Spatial s, JmeSceneNode parentscenenode, boolean existing) {
        //MA17 this.parentscenenode = parentscenenode;
        this.spatial = s;
        //Die Id nicht immer neu vergeben, denn die muss ja evtl. schon gesetzt sein.
        if (!existing) {
            spatial.setUserData("uniqueid", uniqueid++);
            //MA17 Platform.getInstance().native2nativeobject3d.put((Integer) spatial.getUserData("uniqueid"), this);
        }
    }

    /*Spatial isType abstract public JmeObject3D() {
        this(new Spatial());
    }*/

    @Override
    public void rotateOnAxis(Vector3 axis, double angle) {
        //GWT.log("spatial=" + spatial + ",angle=" + angle);
        //spatial.getLocalTransform().rogetTranslation().(offset.vector3);
        Quaternion r = new Quaternion();
        r.fromAngleAxis((float) angle, JmeVector3.toJme(axis));
        Quaternion q = spatial.getLocalRotation();
        q = q.mult(r);
        spatial.setLocalRotation(q);

    }

    @Override
    public void translateOnAxis(Vector3 axis, double distance) {
        Vector3f v = getTranslateOnAxisMoveVector(spatial.getLocalRotation(), JmeVector3.toJme(axis), (float) distance);
        //spatial.setLocalTranslation((spatial.getLocalTransform().getTranslation().add(v)));
        spatial.move(v);
    }

    public static Vector3f getTranslateOnAxisMoveVector(Quaternion localrotation, Vector3f axis, float distance) {
        Vector3f offset = (axis).clone();
        // 15.12.15: Ein translate muss die aktuelle Rotation beruecksichtigen. Zumindest macht es
        // ThreeJs so. Unity wahrscheinlich auch. Der move in JME macht es nicht.
        // 17.12.15: Also selber machen, ist durch Base3DTest bestätigt
        offset = localrotation.mult(offset);
        Vector3f v = offset.mult(distance);
        return v;
    }

    @Override
    public void setScale(Vector3 scale) {
        Vector3f v = JmeVector3.toJme(scale);
        spatial.setLocalScale((float) v.x, (float) v.y, (float) v.z);
    }

    public void scale(Vector3 scale) {
        Vector3f v = JmeVector3.toJme(scale);
        spatial.scale((float) v.x, (float) v.y, (float) v.z);
    }

    @Override
    public Vector3 getScale() {
        // clone um unbeabsichtigtes Veraendern zu vermeiden
        return JmeVector3.fromJme(spatial.getLocalScale().clone());
    }

    @Override
    public void setPosition(Vector3 pos) {
        spatial.setLocalTranslation((JmeVector3.toJme(pos)));
    }

    @Override
    public Vector3 getPosition() {
        return JmeVector3.fromJme(spatial.getLocalTransform().getTranslation().clone());
    }

    @Override
    public de.yard.threed.core.Quaternion getRotation() {
        //TODO: oder aus transform?
        return JmeQuaternion.fromJme(spatial.getLocalRotation().clone());
    }

    @Override
    public void setRotation(de.yard.threed.core.Quaternion q) {
        spatial.setLocalRotation((JmeQuaternion.toJme(q)));
    }

    /**
     * @return
     */
    @Override
    public Matrix4 getLocalModelMatrix() {
        //02.01.16: Warum selberechnen? Weil JME scheinbar keine Matrix4 vorhält,
        //sondern immer mit transforms arbeitet.
        Matrix4f store = new Matrix4f();
        store.setTransform(spatial.getLocalTranslation(), spatial.getLocalScale(), spatial.getLocalRotation().toRotationMatrix());
        //store.multLocal(spatial.getLocalRotation());
        //Matrix4f scalestore = new Matrix4f();
        //scalestore.scale(spatial.getLocalScale());
        return JmeMatrix4.fromJme(store);
        //return buildMatrix4FromTransform(spatial.getLocalTransform());
    }

    /**
     * @return
     */
    @Override
    public Matrix4 getWorldModelMatrix() {
        return JmeMatrix4.fromJme(spatial.getLocalToWorldMatrix(null));
    }


    //@Override
    /*14.11.16 public boolean isSame(NativeObject3D obj) {
        logger.debug("spatial.name=" + spatial.getName());
        logger.debug("obj.name=" + ((JmeObject3D) obj).spatial.getName());
        return spatial.getName().equals(((JmeObject3D) obj).spatial.getName());
    }*/


    @Override
    public NativeTransform getParent() {
        if (spatial.getParent() == null) {
            return null;
        }
        //return new JmeSpatial/*SceneNode*/(spatial.getParent(),true);
        Spatial parent = ((Node) spatial).getParent();
        /*MA17Object o = parent.getUserData("uniqueid");
        if (o == null) {
            // Dass die root node keine Id hat, ist richtig, daher nicht loggen
            if (!"Root Node".equals(parent.getName())) {
                logger.warn("no uniqueid in object");
            }
            return null;
        }*/
        //MA17 return Platform.getInstance().findObject3DById(((Integer) o));
        if ("Root Node".equals(parent.getName())) {
            return null;
        }
        return new JmeSpatial(parent, null, true);
    }

    @Override
    public void setParent(NativeTransform/*Object*/ parent) {
        if (parent == null) {
            ((Node) spatial).removeFromParent();
            // 28.11.18: an root hängen, nicht einfach parentlos lassen. Zumindest fuer Camera/stepcontroller ist das wichtig.
            JmeSceneRunner.getInstance().rootnode.attachChild(spatial);
        } else {
            if (parent == this){
                //9.3.21: Erkennt aber nicht zuverlaessig Recursion.
                throw new RuntimeException("self parent");
            }
            ((Node) ((JmeSpatial) parent).spatial).attachChild(spatial);
        }

    }

    /*@Override
    public void add(NativeModel obj) {
        ((Node) spatial).attachChild(((JmeModel) obj).spatial);
    }*/

    /**
     * 23.1.17: Hier muss der meshholder beachtet werden, der beim Count rausgerechnet wurde.
     *
     * @param index
     * @return
     */
    @Override
    public NativeTransform getChild(int index) {
        /*6.10.17 if (meshholderindex != -1 && index >= meshholderindex) {
            index++;
        }
        Spatial child = ((Node) spatial).getChild(index);
        if (child == null) {
            //debug stop
            child = null;
        }
        Integer id = (Integer) child.getUserData("uniqueid");
        if (id == null) {
            id = null;
        }
        return Platform.getInstance().findObject3DById((id));*/
        return getChildren().get(index);
    }

    @Override
    public int getChildCount() {
        return getChildren().size();
    }

    /**
     * MA17
     *
     * @return
     */
    @Override
    public List<NativeTransform> getChildren() {
        List<NativeTransform> l = new ArrayList<NativeTransform>();
        int cnt = ((Node) spatial).getChildren().size();

        for (int i = 0; i < cnt; i++) {
            //6.10.17 if (meshholderindex == -1 || i != meshholderindex) {
            Spatial child = ((Node) spatial).getChild(i);
            // Geometry ist das mesh child. 24.9.19: Auch nicht die CameraNode mitzaehlen. Das ist ja eine Component.
            //11.11.19 nach neuer Bewertung mit Unity doch wieder mitzaehlen. 14.11.19: Doch nicht wegen ThreeJS
            if (!(child instanceof Geometry) && !(child instanceof CameraNode)) {

                if (child == null) {
                    //debug stop
                    child = null;
                } else {
                    //Ich leg einen  Wrapper einfach neu an. 
                /*Integer id = (Integer) child.getUserData("uniqueid");
                if (id == null) {
                    id = null;
                }
                l.add(Platform.getInstance().findObject3DById((id)));*/
                    l.add(new JmeSpatial(child, null, true));
                }
            }

        }
        return l;
    }

    /**
     * Einer der Children muss das Child fuer den Mesh sein.
     * @return
     */
    public JmeMesh getMeshHolder() {
        int cnt = ((Node) spatial).getChildren().size();
        for (int i = 0; i < cnt; i++) {
            Spatial child = ((Node) spatial).getChild(i);
            // Geometry ist das mesh child.
            if (child instanceof Geometry) {
                Integer meshindex = child.getUserData("meshholder");
                return JmeMesh.meshes.get(meshindex);
            }
        }
        return null;
    }

    @Override
    public NativeSceneNode getSceneNode() {
        //return Platform.getInstance().findSceneNodeById(((Integer) spatial.getUserData("uniqueid")));
        //MA17return parentscenenode;
        Spatial parent = spatial;
        return new JmeSceneNode(parent);
    }

    public void add(NativeTransform obj) {
        //spatial.
       /*15.6.16  if (obj instanceof JmeMesh) {
            JmeMesh mesh = (JmeMesh) obj;
            if (mesh.container != null) {
                ((Node) spatial).attachChild(mesh.container.spatial);

            } else {
                ((Node) spatial).attachChild(mesh.spatial);
            }
        } else {*/
        if (obj instanceof JmeCamera) {
            //26.11.18: das geht so nicht, weil CameraNode gebraucht wird.
            Util.notyet();
            //JmeCamera cam = (JmeCamera) obj;
            //((Node) spatial).attachChild(cam);
        } else {
            JmeSpatial mesh = (JmeSpatial) obj;
            ((Node) spatial).attachChild(mesh.spatial);
        }
        //}
    }

    Node getNode() {
        return (Node) /*super. */spatial;
    }
    
    /*@Override
    public NativeObject3D find(String name) {
        Spatial sp = find((Node) spatial,name);
        if (sp != null) {
            // retrieve existing SceneNode instead of creating new one
            JmeSceneNode sn = sp.getUserData("scenenodewrapper");
            if (sn == null){
                throw new RuntimeException("scenenodewrapper not set in spatial");
            }
            return sn;
        }
        return null;
    }*/

    public Spatial find(Spatial spa, String name) {
        if (spa.getName().equals(name)) {
            return spa;
        }
        if (spa instanceof Node) {
            Node n = ((Node) spa);
            for (Spatial sp : n.getChildren()) {
                Spatial sp1 = find(sp, name);
                if (sp1 != null) {
                    return sp1;
                }
            }
        }
        return null;
    }

    /**
     * 21.7.16: static und mit eigener Parentermittlung, weil nicht sicher ist, dass es auf parent aufgerufen wird.
     * 02.10.19: Wenn layer gesetzt ist, aber nicht beim Parent, kommt es hier zum Absturz. Alles reichlich  unklar.
     *
     * @param obj
     */
    /*21.7.16 @Override
    public*/
    public static void remove(JmeSceneNode obj) {
        /*15.6.16 if (obj instanceof JmeMesh) {
            JmeMesh mesh = (JmeMesh) obj;
            if (mesh.container != null) {
                ((Node) spatial).detachChild(mesh.container.spatial);

            } else {
                ((Node) spatial).detachChild(mesh.spatial);
            }
        } else {*/
        JmeSceneNode node = (JmeSceneNode) obj;
        Node parent = null;
        if (node.object3d.spatial.getParent() != null) {
            parent = node.object3d.spatial.getParent();
            node.object3d.spatial.getParent().detachChild(node.object3d.spatial);
            //((Node) spatial).detachChild(mesh.spatial);
        }
        //obj.mesh.geometry.removeFromParent();
        //2.10.19: updateGeometricState() muss angeblich sein. Crashed trotzdem. Also nicht.
        if (parent != null) {
            //parent.updateGeometricState();
        }
    }

 
    /*public static JmeMatrix4 buildMatrix4FromTransform(Transform transform) {
        return Matrix4.buildTransformationMatrix(new Vector3(new JmeVector3(transform.getTranslation())),new Quaternion(new JmeQuaternion(transform.getRotation())));
    }*/

    /**
     * Das ist eine totale Kruecke, von der man bezweifeln muss, dass sie so durchgängig funktioniert.
     * 31.10.19: Das mit dem Viewport wird jetzt wohl gehen. Aber layer muss rekursiv gesetzt werden.
     * 1.11.19:Eigentlich, beim get() einfach den den höchten parent liefern.
     * Das mit detach/attach ist undurchsichtig. Was ist bei mehrfachem mit Subgraphen?
     *
     * @param layer
     */
    @Override
    public void setLayer(int layer) {
        int currentlayer = getLayerOfSpatial(spatial);
        if (currentlayer != 0) {
            JmeCamera deferredcamera = JmeCamera.findCameraByLayer(currentlayer);
            if (deferredcamera != null) {
                deferredcamera.getViewPort().detachScene(spatial);
            } else {
                logger.warn("no camera found for current layer " + currentlayer);
            }
        }else{
            NativeCamera c = Platform.getInstance().getCameras().get(0);
                JmeCamera jmeCamera = (JmeCamera) c;
            jmeCamera.getViewPort().detachScene(spatial);
        }
        JmeCamera deferredcamera = JmeCamera.findCameraByLayer(layer);
        if (deferredcamera != null) {
            if (layer > 0) {
                deferredcamera.getViewPort().attachScene(spatial);
            }
        }
        spatial.setUserData("layer", new Integer(layer));
        //setLayerIndex((Node) spatial,layer);

    }

    /**
     * 31.10.19: Noch nicht ganz stimmig.
     */
    /*private void setLayerIndex(Node node, int layer){
        node.setUserData("layer", new Integer(layer));
        for (Spatial c:node.getChildren()){
            if (c instanceof Geometry) {
                ((Geometry)c).setUserData("layer", new Integer(layer));
            }else{
                setLayerIndex((Node) c, layer);
            }
        }
    }*/
    @Override
    public int getLayer() {

        //return /*1 <<*/ getLayerIndex();
        int layer;
        Spatial s = spatial;
        do {
            layer = getLayerOfSpatial(s);
            s = s.getParent();
        } while (layer == 0 && s != null);
        return layer;
    }

    public static int getLayerOfSpatial(Spatial spatial) {
        Integer layer = spatial.getUserData("layer");
        if (layer == null || layer == 0) {
            return 0;
        }
        return layer;
    }

}
