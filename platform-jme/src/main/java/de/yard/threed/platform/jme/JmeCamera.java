package de.yard.threed.platform.jme;

import com.jme3.math.Matrix4f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.control.CameraControl;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Point;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.*;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.Transform;


import de.yard.threed.core.Color;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.engine.platform.common.RayHelper;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.javacommon.JALog;

/**
 * Das ist erstmal? nur ein Dummy, weil JME ein anderes Camera Konzept hat.
 * Wird aber auch fuer die "main Camera" genutzt.
 * Und auch fuer zusaetzliche (deferred)
 * 12.06.15: Das koennte doch zusammenpassen
 * <p/>
 * 23.2.16: Die CameraNode gibt es jetzt immer (hier). Rotation/Position beziehen sich dann immer auf die CameraNode. Nee, nur wenn attached.
 * 20.5.16: Um Model in den Cameraspace haengen zu koennen (HUD), waere eine immer existierende CameraNode natürlich praktisch. Nochmal pruefen!
 * War mal Ableitung von JmeSpatial, jetzt aber nicht mehr.
 * 26.1.17: Implementiert NativeTransform wegen der merkwürdigen Camera in JME.
 * MA29: Doch immer CameraNode weil Camera Componentn ist. Das scheint totaler Kokelores. Dann doch weiterhin als NativeTransform um selber eigene SceneNode zu sein.
 * Die Cameranode scheint nicht als SceneNode geeigent! Darum nehm ich hier jetzt auch sowas wie carrier. Muss aber intern bleiben? Wer ist denn parent?
 * 23.9.19: Doch nicht NativeTransform wegen Component? Offenbar.
 * <p/>
 * <p/>
 * Created by thomass on 11.06.15.
 */
public class JmeCamera implements NativeCamera/*, NativeTransform */ {
    static Log logger = new JALog(JmeCamera.class);
    Camera camera;
    public static final Vector3f defaultup = new Vector3f(0, 1, 0);
    //node when attached. 3.12.18: Do h wieder immer, denn ohne kann es keinen carrier geben.
    CameraNode cameraNode = null;
    //   private boolean isattached = false;
    Node rootnode;
    // Mirror Matrix, weil JME die Rotation spiegelt
    // Die Werte sind durch Ausprobieren entstanden.
    Matrix4f mirror = new Matrix4f(-1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, -1, 0,
            0, 0, 0, 1);
    protected RayHelper rayHelper;
    //Ein an der Camera haengendes Model (HUD). Es haengt aber nicht wirklich an der Camera. 
    // 26.11.18: Stimmt der Kommentar noch? Ja, der stimmt noch.
    JmeSceneNode cameramodel;
    //MA29: camera immer ein Component(Child) einer Node. 
    JmeSceneNode carrier;
    // Viewport cannot be derived from camera.
    private ViewPort viewport;
    // fov is not stored in JME natively. So keep it here for getter
    private double fov;

    JmeCamera(Camera camera, Node rootnode, double fov, double aspect, double near, double far, Color backgroundColor, ViewPort pviewport) {
        this.fov = fov;
        if (camera == null) {
            //additional camera viewport (https://wiki.jmonkeyengine.org/jme3/advanced/multiple_camera_views.html)
            //clone() appears to be the preferred way
            //camera=new Camera();
            camera = JmeSceneRunner.getInstance().jmecamera.camera.clone();
            pviewport = JmeSceneRunner.getInstance().simpleApplication.getRenderManager().createMainView("PiP", camera);
            pviewport.setClearFlags(false, true, true);
            //attachscene wird später für Layer gemacht.
            //pviewport.attachScene(rootnode);
            pviewport.setBackgroundColor(PlatformJme.buildColor(Settings.backgroundColor));

        }
        this.camera = camera;
        this.viewport = pviewport;
        this.rootnode = rootnode;
        camera.setFrustumPerspective((float) fov, (float) aspect, (float) near, (float) far);

        //camera.set
        //die Rotation initialisieren, weil JME ja spiegelt
        setRotation(JmeQuaternion.fromJme(new com.jme3.math.Quaternion()));
        rayHelper = new RayHelper(this);
        cameramodel = new JmeSceneNode("Cameramodel");
        rootnode.attachChild(cameramodel.object3d.spatial);

        //buildCameraNode();
        //fixRotation();

        //rootnode.attachChild(cameraNode);
        carrier = new JmeSceneNode("Main Camera Carrier");
        attach(carrier.getTransform());
        //rootnode.attachChild(carrier.object3d.spatial);
        carrier.getTransform().setParent(Scene.getCurrent().getWorld().getTransform().transform);
        //Platform.getInstance().addCamera(this);
        AbstractSceneRunner.getInstance().addCamera(this);

    }

    //@Override
    public Vector3 getPosition() {
        if (cameraNode == null) {
            return JmeVector3.fromJme(camera.getLocation());
        } else {
            return JmeVector3.fromJme(cameraNode.getLocalTransform().getTranslation().clone()/*camera.getLocation()*/);
        }
    }

    // @Override
    public void setPosition(Vector3 pos) {
        if (cameraNode == null) {
            camera.setLocation(JmeVector3.toJme(pos));
        } else {
            //23.2.16
            cameraNode.setLocalTranslation(JmeVector3.toJme(pos));
            camera.setLocation(new Vector3f());
        }
    }

    @Override
    public Matrix4 getViewMatrix() {
        // ueber cameraNode? scheint es erstmal keinen Bedarf für zu geben.
        // 22.3.16: Das ist irgendwie unklar.
        camera.updateViewProjection();
        Matrix4/*f*/ camviewmatrix = JmeMatrix4.fromJme(camera.getViewMatrix());
        if (cameraNode != null) {
            //camviewmatrix = cameraNode.getLocalToWorldMatrix(null).mult(camviewmatrix);
            //camviewmatrix = camviewmatrix.mult(cameraNode.getLocalToWorldMatrix(null));
            //return new JmeMatrix4(cameraNode.getLocalToWorldMatrix(null));
            //unklar, wie das zu berechnen ist. Ueber die world inverse geht es bestimmt, aber es muesste auch direkter gehen.
            /*Jme*/
            Matrix4 m4 = /*(JmeMatrix4)*/ getWorldModelMatrix();
            camviewmatrix = m4.getInverse();//matrix4.invert();
        }
        return /*new JmeMatrix4*/(camviewmatrix);
    }

    /**
     * Das kennt JMe eigentlich gar nicht, darum nachbilden bzw. selber berechnen.
     * Hier muss auch wieder die Camera Rotation gespiegelt werden.
     * 27.2.17: getLocation(), getRotation(), etc. liefern world coordinates? Hat die JME Camera nur world coordinates? Laut Source gibt es sonst nichts!
     * 28.2.17: Der MovingBoxTest in ReferenceScene spricht dagegen. Da ist location (0,0,0). Auch scheinen nur lokale sinnvoll, wer aendert sonst die Location
     * bei einer bewegten attached camera? -->Die camera local location nicht verwenden, nur die cameraNode.
     *
     * @return
     */
    //@Override
    public Matrix4 getWorldModelMatrix() {
        //MathUtil2.b
        Matrix4 cameralocal = MathUtil2.buildMatrix(MathUtil2.buildTranslationMatrix(JmeVector3.fromJme(camera.getLocation())),
                JmeMatrix4.fromJme(mirrorRotation(camera.getRotation()).toRotationMatrix(new Matrix4f())),
                JmeMatrix4.fromJme(new Matrix4f()));
        if (cameraNode == null) {
            return cameralocal;
        } else {
            // 27.2.17: Oh Mann. Was ein Gewürge. Ich nehm einfach mal die "locals". Buttons im Maze gehen dann. Siehe Header, das kann es auch nicht sein.
            // Aber wenn es eine CameraNode gibt, ist die CameraLocation immer (0,0,0), egal was JME da reinschreibt (und warum und wo?).
            // Fuer Rotation gilt analog dasselbe (allerdings muss gespiegelt werden). Jetzt geht RefScene und Mazebuttons.
            Matrix4 nodeworld = JmeMatrix4.fromJme(cameraNode.getLocalToWorldMatrix(new Matrix4f()).mult(mirror));
            //return ((JmeMatrix4)nodeworld).multiply(cameralocal);
            //return cameralocal;
            return nodeworld;
        }
    }

    /**
     * Warum JME eine 180 Grad y-Rotation liefert, ist unklar.
     * Daher ermittel ich die Rotation aus der Inversen der Viewmatrix. Das musste dann die Rotation
     * in der CameraTranformationMatrix sein. Oder einfach umrechnen. Ist aber nur im Nachhinein einfach.
     *
     * @return
     */
    // @Override
    public Quaternion getRotation() {
        com.jme3.math.Quaternion q;
        boolean frommatrix = false;
        if (!frommatrix) {
            if (cameraNode == null) {
                q = camera.getRotation();
            } else {
                q = cameraNode.getLocalRotation().clone()/*camera.getRotation()*/;
            }
            q = mirrorRotation(q);
        } else {
            /*JmeMatrix4*/
            Matrix4f vm = JmeMatrix4.toJme(getViewMatrix());
            q = vm./*matrix4.*/invert().toRotationQuat();
        }
        return JmeQuaternion.fromJme(q);
    }

    private com.jme3.math.Quaternion mirrorRotation(com.jme3.math.Quaternion q) {
        Matrix4f rj = new Matrix4f();
        q.toRotationMatrix(rj);
        //logger.debug("rj="+rj.toString());
        // logger.debug("rji="+rj.invert().toString());
        q = rj.mult(mirror).toRotationQuat();
        return q;
    }

    /**
     * Wird wie beim getter gespiegelt.
     *
     * @param rotation
     */
    //@Override
    public void setRotation(Quaternion rotation) {
        logger.debug("setRotation " + rotation.getX() + "," + rotation.getY() + "," + rotation.getZ() + "," + rotation.getW());

        com.jme3.math.Quaternion q = JmeQuaternion.toJme(rotation);
        Matrix4f rj = new Matrix4f();
        q.toRotationMatrix(rj);
        q = rj.mult(mirror).toRotationQuat();

        if (cameraNode == null) {
            camera.setRotation(q);
        } else {
            cameraNode.setLocalRotation(q);
            camera.setRotation(new com.jme3.math.Quaternion());
        }
    }

    public void fixRotation() {
        Quaternion rotation = new Quaternion();
        logger.debug("fixRotation " + rotation.getX() + "," + rotation.getY() + "," + rotation.getZ() + "," + rotation.getW());

        com.jme3.math.Quaternion q = JmeQuaternion.toJme(rotation);
        Matrix4f rj = new Matrix4f();
        q.toRotationMatrix(rj);
        q = rj.mult(mirror).toRotationQuat();

        camera.setRotation(q);

    }

    @Override
    public Matrix4 getProjectionMatrix() {
        camera.updateViewProjection();
        return JmeMatrix4.fromJme(camera.getProjectionMatrix());
    }

    /*@Override
    public void lookAt(Vector3 nlookat, Vector3 upVector) {
        Vector3f lookat = ((JmeVector3) nlookat).vector3;
        // UP wird erstmal beibehalten
        // 6.11.15: Beibehalten führt aber beim StepController dazu, dass die Ursprungsposition
        // nicht mehr erreicht wird. Irgendwas gerät da durcheinander. Mit festem up gehts.
        //    camera.lookAt(((JmeVector3) lookat).vector3,camera.getUp());
        //23.2.16 lookat bei JME ist in world space. Darum hier umrechnen von local in world space
        if (cameraNode == null) {
            camera.lookAt(lookat, (upVector!=null)? ((JmeVector3) upVector).vector3 :defaultup/*camera.getUp()* /);
        } else {
            Matrix4f m = cameraNode.getParent().getLocalToWorldMatrix(null);
            lookat = m.mult(lookat);
            //Unsinn lookat.normalizeLocal();
            cameraNode.lookAt(lookat, (upVector!=null)? ((JmeVector3) upVector).vector3 :Vector3f.UNIT_Y);
        }
    }*/

    /**
     * 4.4.17: Geht bei osmscene in machen Konstellationen irgendwie nicht. Bei FlightScene auch nicht.
     */
    //@Override
    public void detach() {
        if (cameraNode != null) {
            // rootnode.attachChild(cameraNode);
            //isattached = false;
            cameraNode.removeFromParent();
            //cameraNode.setEnabled(false);
            //??cameraNode.setCamera(null);
            cameraNode = null;
        }
    }

    /**
     * Die Camera an ein Model haengen. 26.11.18: Verwirrende Logik.
     *
     * @param model
     */
    //@Override
    public void attach(NativeTransform/*SceneNode*/ model) {
        //5.4.17: Wegen der JME Unkalrheiten bei häugfgiem Viewechsel immer erst einen detach machen.
        detach();
        //((Node) spatial).a(((JmeMesh) obj).spatial);
        buildCameraNode();

        ((JmeSpatial) model).getNode().attachChild(/*((JmeCamera)camera).*/cameraNode);
        //die Rotation initialisieren, weil JME ja spiegelt
        setRotation(JmeQuaternion.fromJme(new com.jme3.math.Quaternion()));
    }

    /**
     * model can be both camera or real model.
     *
     * @param model
     */
    //@Override
    public void add(NativeTransform/*SceneNode*/ model) {
        //cameramodel.object3d.add(((JmeSceneNode)model).object3d);
        if (model instanceof JmeCamera) {
            cameramodel.object3d.add(((JmeCamera) model).cameramodel.getTransform());
        } else {
            cameramodel.object3d.add(model);
        }
    }

    // @Override
    public void translateOnAxis(Vector3 axis, double distance) {
        Vector3f v = JmeSpatial.getTranslateOnAxisMoveVector(JmeQuaternion.toJme(getRotation()), JmeVector3.toJme(axis), (float) distance);
        setPosition(MathUtil2.add(getPosition(), JmeVector3.fromJme(v)));
    }

    private void buildCameraNode() {
        cameraNode = new CameraNode("Camera Node", camera);

        //CameraToSpatial geht nicht, weil dann die Bewegungen des Parent die Camera nicht bewegen
        cameraNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        // 01.09.15 ein setLocation hat die cameraNode nicht. Aber die posotion und lookat beziehen sich doch eh auf die
        // Camera. Die Node Translation muss auch auch gesetzt werden, sonst geht die ModelCamera nicht.
        //23.2.16 cameraNode.setLocalTranslation(((JmeVector3) getPosition()).vector3);
        //01.09.2015: Die uebergebene Position scheint (zumindest bei MazeScene, zu klein zu sein. Irgendwas
        //stimmt da noch nicht. Aber im grossen und ganzen passt das.
        //cameraNode.setLocalTranslation(new Vector3f(0, 2, 4));
        //14.10.15: Jetzt stimmt es aber wohl ganz gut
        //01.09.2015: Man muss den lookat auf der CamraNode nochmal( oder nur da?) setzen, sonst ist der Blick und auch
        // die Bewegungsrichtung völlig falsch.
        //23.2.16 cameraNode.lookAt(((JmeVector3) lookat).vector3, Vector3f.UNIT_Y);

        //cameraNode.lookAt(((JmeModel) model).getNode().getLocalTranslation(), Vector3f.UNIT_Y);
    }

    @Override
    public NativeRay buildPickingRay(NativeTransform real, Point mouselocation/*,Dimension screendimensions*/) {
        Dimension screendimensions = AbstractSceneRunner.getInstance().dimension;

        NativeRay ray = rayHelper.buildPickingRay(new Transform(real), mouselocation.getX(), mouselocation.getY(), screendimensions);
        return ray;
    }

    /**
     * Das an der Camera haengende Model (HUD) an die aktuelle Cameraausrichtung anpassen. Intern in jedem Frame.
     */
    public void updateCameraModel() {

        DimensionF nearplaneSize = null;//getNearplaneSize();
        Quaternion cr/* = camera.getRotation()*/;
        Matrix4 vmi = /*new Matrix4(MathUtil2.getInverse(*/ JmeMatrix4.fromJme(camera.getViewMatrix()).getInverse();
        //logger.debug("camera position="+camera.getPosition().dump(""));
        //logger.debug("camera rotation=" + cr.dump(""));
        // logger.debug("camera viewmatrix="+camera.getViewMatrix().dump("\n"));
        //logger.debug("camera nearplanesize="+nearplaneSize);

        cr = vmi.extractQuaternion()/*.quaternion*/;
        //logger.debug("camera rotation from viewer matrix=" + cr.dump(""));

        //  cr = new Quaternion(-0.212f,cr.getY(),0,1);
        //cr = new Quaternion(new Degree(30),new Degree(50),new Degree(20));
        cameramodel.object3d.setRotation(cr);
        cameramodel.object3d.setPosition(vmi.extractPosition()/*camera.getPosition()*/);
        //translateZ
        //cameramodel.translateOnAxis(new JmeVector3 (0,0,1),-0.1f - 0.0001f/*-5f*/);

    }


    @Override
    public double getNear() {
        double near = camera.getFrustumNear();
        return near;
    }

    @Override
    public double getFar() {
        double near = camera.getFrustumFar();
        return near;
    }

    @Override
    public void setFar(double far) {
        camera.setFrustumFar(far);
    }

    @Override
    public double getAspect() {
        double aspect = (double) camera.getWidth() / camera.getHeight();
        return aspect;
    }

    @Override
    public double getFov() {
        // not available natively. Either recalculate or use saved value.
        return this.fov;
    }

    //@Override
    /*public NativeTransform getTransform() {
        return this;
    }*/

    @Override
    public Vector3 getVrPosition(boolean dumpInfo) {
        return getPosition();
    }

    @Override
    public void setLayer(int layer) {
        cameraNode.setUserData("layer", new Integer(layer));
    }

    public int getLayer() {
        Integer layer = cameraNode.getUserData("layer");
        if (layer == null) {
            return 0;
        }
        return layer;
    }

    @Override
    public void setName(String name) {
        //logger.debug("setting name "+name);
        carrier.setName(name + " Carrier");
        camera.setName(name);
        cameraNode.setName(name);
    }

    @Override
    public String getName() {
        return camera.getName();
    }

    @Override
    public NativeSceneNode getCarrier() {
        return carrier;
    }

    @Override
    public void setClearDepth(boolean clearDepth) {
        //5.3.24: why isn't this needed?
    }

    @Override
    public void setClearBackground(boolean clearBackground) {

    }

    @Override
    public void setEnabled(boolean b) {
        viewport.setEnabled(b);
    }

    //@Override
    public NativeTransform getChild(int i) {
        return (NativeTransform) Util.notyet();
    }

    //@Override
    public NativeTransform getParent() {
        return (NativeTransform) Util.notyet();
    }


    //@Override
    public void setParent(NativeTransform parent) {
        Util.notyet();
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
        //if (cameraNode!=null) {
        //  return new JmeSceneNode(cameraNode);
        //}
        return carrier;//Scene.world;
    }

    public ViewPort getViewPort() {
        return viewport;
    }

    public static JmeCamera findCameraByLayer(int layer) {
        for (NativeCamera c : AbstractSceneRunner.instance.getCameras()) {
            JmeCamera jmeCamera = (JmeCamera) c;
            if (layer == jmeCamera.getLayer()) {
                return jmeCamera;
            }
        }
        logger.warn("Camera for layer not found:" + layer);
        return null;
    }


}
