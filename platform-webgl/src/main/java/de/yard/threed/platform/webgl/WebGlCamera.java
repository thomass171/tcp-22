package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Point;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.*;
import de.yard.threed.engine.Transform;
import de.yard.threed.core.Dimension;


import de.yard.threed.engine.platform.common.RayHelper;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomass on 25.04.15.
 * <p/>
 * Das auch von WebGlObject3D abzuleiten ist einfach praktisch, weil es
 * ThreeJs auch so macht und damit die ganzen Basisfunktionen (z.B. setPosition) schon da sind.
 * 16.11.16: Jetzt aber nicht mehr abeleitet wegen ECS.
 * 26.1.17: Not implementing NativeTransform, because its just a component of a SceneNode(carrier) like eg. light.
 * 04.05.18: Fuer VR wie in Unity noch einen Adapter (carrier), an dem der VR Input wieder gespiegelt wird. Die WebGL Camera ist dann am Carrier
 * und alle translate/rotate Operationen laufen auf dem carrier. Vermeidet hoffentlich das Springen.
 * 29.09.18: Sinnvoll ist das wohl, vermeidet aber nicht das springen. Das springen sieht man übrigens auch in ThreeJS demo rollercoaster.
 */
public class WebGlCamera implements NativeCamera/*, NativeTransform*/ {
    Log logger = new WebGlLog(/*LogFactory.getLog(*/WebGlCamera.class.getName());
    //JavaScriptObject camera;
    JavaScriptObject attachedto = null;
    protected RayHelper rayHelper;
    private double aspect, near, fov, far;
    /*private*/ WebGlObject3D object3d, carrier;
    private boolean clearDepth = false;
    public boolean enabled = true;

    private WebGlCamera(JavaScriptObject threejscamera) {
        //super(camera);
        object3d = new WebGlObject3D(threejscamera);
        rayHelper = new RayHelper(this);
        carrier = new WebGlObject3D(WebGlObject3D.buildObject3D());
        //'real' name might be set later. For now just set a default name.
        carrier.setName("Camera Carrier");
        //carrier in scene kommt spaeter
        WebGlScene.webglscene.add(object3d.object3d);
        //carrier.add(object3d);
        object3d.setParent(carrier);
        //((EngineHelper) Platform.getInstance()).addCamera(this);
        AbstractSceneRunner.getInstance().addCamera(this);
    }

    public static WebGlCamera buildPerspectiveCamera(WebGlScene webglscene, double fov, double aspect, double near, double far) {
        WebGlCamera ca = new WebGlCamera(buildPerspectiveCameraNative(fov, aspect, near, far));
        ca.aspect = aspect;
        ca.fov = fov;
        ca.near = near;
        ca.far = far;
        // 20.5.16: Die camera auch in die Scene haengen. Das wurde bisher wohl nicht gemacht und ist auch nicht
        // unbedingt erforderlich. Für die Model an der Camera (z.B. HUD) ist es aber wichtig.
        webglscene.add(/*webglcamera.*/ca.getCarrier());

        return ca;
    }

    public JavaScriptObject getThreeJsCamera() {
        return object3d.object3d;
    }

    @Override
    public NativeSceneNode getCarrier() {
        WebGlSceneNode n = new WebGlSceneNode(carrier.object3d, true);
        return n;
    }

    /**
     * Just setting the flag. Depth buffer clearing is done in WebGLRenderer.clear().
     */
    @Override
    public void setClearDepth(boolean clearDepth) {
        this.clearDepth = clearDepth;
    }

    @Override
    public void setClearBackground(boolean clearBackground) {

    }

    @Override
    public void setEnabled(boolean b) {
        enabled = b;
    }

    @Override
    public void setFar(double far) {
        logger.warn("not yet");
    }

    //@Override
    public Vector3 getPosition() {
        return carrier/*object3d*/.getPosition();
    }

    /**
     * Arbeitet auf dem carrier.
     *
     * @param vector3
     */
    //@Override
    public void setPosition(Vector3 vector3) {
        //logger.debug("setPosition " + vector3.getX() + "," + vector3.getY() + "," + vector3.getZ());
        //logger.debug("camera parent isType " + object3d.getParent());
        carrier/*object3d*/.setPosition(vector3);
        //logger.debug("setPosition cam position " + object3d.getPosition().getX() + "," + object3d.getPosition().getY() + "," + object3d.getPosition().getZ());
        //logger.debug("setPosition carrier position " + carrier.getPosition().getX() + "," + carrier.getPosition().getY() + "," + carrier.getPosition().getZ());
    }

    @Override
    public Matrix4 getProjectionMatrix() {
        return WebGlMatrix4.fromWebGl(new WebGlMatrix4(getProjectionMatrix(object3d.object3d)));
    }

    @Override
    public Matrix4 getViewMatrix() {
        //logger.debug("carrier position:"+carrier.getPosition());
        return WebGlMatrix4.fromWebGl(new WebGlMatrix4(getViewMatrix(object3d.object3d)));
    }

    //@Override
    public Matrix4 getWorldModelMatrix() {
        return WebGlMatrix4.fromWebGl(new WebGlMatrix4(WebGlObject3D.getMatrixWorld(carrier/*object3d*/.object3d)));
    }

    //@Override
    public Matrix4 getLocalModelMatrix() {
        return (Matrix4) Util.notyet();
    }

    //@Override
    public Quaternion getRotation() {
        return WebGlQuaternion.fromWebGl(new WebGlQuaternion(getRotation(carrier/*object3d*/.object3d)));
    }

    //@Override
    public void setRotation(Quaternion quaternion) {
        //logger.debug("setRotation " + quaternion.getX() + "," + quaternion.getY() + "," + quaternion.getZ()+ "," + quaternion.getW());
        carrier/*object3d*/.setRotation(quaternion);
    }

    //@Override
    public void detach() {
        if (attachedto != null) {
            WebGlObject3D.remove(attachedto, carrier/*object3d*/.object3d);
            // 20.5.16: Die camera auch in die Scene haengen. Das wurde bisher wohl nicht gemahct und ist auch nicht
            // unbedingt erforderlich. Für die Model an der Camera (z.B. HUD) ist es aber wichtig.
            ((WebGlScene) WebGlSceneRenderer.getInstance().scene.scene).add(carrier/*object3d*/.object3d);

        }
        attachedto = null;
    }

    //@Override
    public void attach(NativeTransform/*SceneNode*/ model) {
        //((WebGlModel) model).add(/*((WebGlCamera)camera).*/object3d);
        WebGlObject3D.add(((WebGlObject3D) model).object3d,/*((WebGlCamera)camera).*/carrier/*object3d*/.object3d);
        attachedto = ((WebGlObject3D) model).object3d;
    }

    //@Override
    public void translateOnAxis(Vector3 axis, double distance) {
        //logger.debug("translateOnAxis");
        carrier/*object3d*/.translateOnAxis(axis, distance);
    }

    //@Override
    public void rotateOnAxis(Vector3 axis, double angle) {
        carrier/*object3d*/.rotateOnAxis(axis, angle);
    }

    @Override
    public double getNear() {
        return near;
    }

    @Override
    public double getFar() {
        return far;
    }

    @Override
    public double getAspect() {
        return aspect;
    }

    @Override
    public double getFov() {
        return fov;
    }

    @Override
    public NativeRay buildPickingRay(NativeTransform realViewPosition, Point mouselocation) {
        Dimension screendimensions = AbstractSceneRunner.getInstance().dimension;
        // 8.4.16: WebGl kann auch weitgehend selber den REay bestimmen. Evtl. mal umstellen.
        NativeRay ray = rayHelper.buildPickingRay(new Transform(realViewPosition), mouselocation.getX(), mouselocation.getY(), screendimensions);
        return ray;
    }

    // @Override
    /*public NativeTransform getTransform() {
        return this;
    }*/

    /**
     * "object3d" is "the camera"
     */
    @Override
    public Vector3 getVrPosition(boolean dumpInfo) {
        if (dumpInfo) {
            logger.debug("getVrPosition: parents:");
            NativeTransform parent = object3d.getParent();
            while (parent != null) {
                logger.debug("getVrPosition: parent position:" + parent.getPosition());
                parent = parent.getParent();
            }
        }
        return (object3d.getPosition());
    }

    @Override
    public void setLayer(int layer) {
        // who uses layer -1?
        if (layer == -1) {
            layer = 0;
        }
        logger.debug("WebGlCamera.setLayer " + layer);
        // carrier layer is set out of platform.
        WebGlObject3D.setLayer(object3d.object3d, layer);
    }

    @Override
    public int getLayer() {
        return WebGlObject3D.decodeLayer(WebGlObject3D.getLayerMask(object3d.object3d));
    }

    @Override
    public void setName(String name) {
        logger.debug("setting name " + name);
        carrier.setName("" + name + " Carrier");
        object3d.setName(name);
    }

    @Override
    public String getName() {
        return object3d.getName();
    }

    /**
     * carrier oder nicht? In Anbetracht von HUD eher nicht?
     *
     * @param model
     */
    //@Override
    public void add(NativeTransform model) {
        if (model instanceof WebGlCamera) {
            //26.11.18: Weil die Camera an der Camera haengt, ist hier auf jeden Fall kein carrier erforderlich.
            addChild(object3d.object3d, ((WebGlCamera) model).carrier.object3d);
        } else {
            addChild(object3d.object3d, ((WebGlObject3D) model).object3d);
        }
    }

    //@Override
    public Vector3 getScale() {
        return (Vector3) Util.notyet();
    }

    //@Override
    public void setScale(Vector3 scale) {
        Util.notyet();
    }

    //@Override
    public NativeSceneNode getSceneNode() {
        return (NativeSceneNode) Util.notyet();
    }

    //@Override
    public NativeTransform getChild(int i) {
        return (NativeTransform) Util.notyet();
    }

    //@Override
    public NativeTransform getParent() {
        return object3d.getParent();
    }


    //@Override
    public void setParent(NativeTransform parent) {
        object3d.setParent(parent);
    }

    public void clearParent() {
        object3d.clearParent();
    }

    /**
     * Childs of camera, not of carrier (carrier always has one child).
     *
     * @return
     */
    //@Override
    public int getChildCount() {
        return object3d.getChildCount();
    }

    //@Override
    public List<NativeTransform> getChildren() {
        List<NativeTransform> l = new ArrayList<NativeTransform>();
        return l;
    }

    public void setAspect(double aspect) {
        setAspect(getThreeJsCamera(), aspect);
    }

    public int getClearmode() {
        if (clearDepth) {
            return 1;
        }
        return 0;
    }

    private static native JavaScriptObject buildPerspectiveCameraNative(double fov, double aspect, double near, double far)  /*-{
        var camera = new $wnd.THREE.PerspectiveCamera(fov, aspect, near, far);

        camera.position.x = 0;
        camera.position.y = 0;
        camera.position.z = 0;
        camera.name = "PerspectiveCamera";
        return camera;
    }-*/;


    private static native void lookAt(JavaScriptObject camera, JavaScriptObject direction)  /*-{
        //$wnd.alert("lookAt");
        camera.lookAt(direction);
    }-*/;

    private static native void setUp(JavaScriptObject camera, JavaScriptObject u)  /*-{
        camera.up.set(u.x,u.y,u.z);        
    }-*/;

    /**
     * 30.11.15: The view matrix. This is just the matrixWorldInverse.
     *
     * 7.3.16: Only updated during rendering, so explicitly do updateMatrixWorld().
     * 5.9.23: updateWorldMatrix() including parents added to reflect carrier changes.
     */
    private static native JavaScriptObject getViewMatrix(JavaScriptObject camera)  /*-{
        camera.updateWorldMatrix(true,false);
        camera.updateMatrixWorld();
        // updateMatrixWorld also updates 'matrixWorldInverse'
        return camera.matrixWorldInverse;
    }-*/;

    private static native JavaScriptObject getProjectionMatrix(JavaScriptObject camera)  /*-{
        return camera.projectionMatrix;
    }-*/;

    private static native JavaScriptObject getRotation(JavaScriptObject camera)  /*-{
        var q = camera.quaternion.clone();
        return q;
    }-*/;


    private static native JavaScriptObject addChild(JavaScriptObject camera, JavaScriptObject child)  /*-{
        return camera.add(child);
    }-*/;

    private static native void setAspect(JavaScriptObject camera, double aspect) /*-{
        camera.aspect = aspect;
        camera.updateProjectionMatrix();            
    }-*/;
}
