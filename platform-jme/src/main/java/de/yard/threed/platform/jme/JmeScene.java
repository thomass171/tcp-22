package de.yard.threed.platform.jme;

import com.jme3.app.SimpleApplication;
import com.jme3.input.FlyByCamera;
import com.jme3.light.Light;
import com.jme3.scene.Spatial;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeLight;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.NativeScene;

import de.yard.threed.core.Dimension;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.javacommon.JALog;

import java.util.ArrayList;
import java.util.List;

/**
 * Braucht die rootNode bzw. den scenegraph aus der SimpleApplication
 * <p/>
 * 30.11.15: Damit muss das aber ein Singleton sein. Und andere
 * nutzen das aus, um an die Rootnode zu kommen.
 * <p/>
 * <p/>
 * Created by thomass on 25.05.15.
 */
public class JmeScene implements NativeScene {
    Log logger = new JALog(JmeScene.class);
    SimpleApplication app;
    FlyByCamera flyCam;
    private boolean enableModelCameracalled;
    private static JmeScene instance;
    private String uniqueName;
    private static int uniqueid = 1;

    private JmeScene(SimpleApplication app, FlyByCamera flyCam) {
        this.app = app;
        this.flyCam = flyCam;
    }

    public static void init(SimpleApplication app, FlyByCamera flyCam) {
        instance = new JmeScene(app, flyCam);
    }

    public static JmeScene getInstance() {
        return instance;
    }

    public void add(NativeSceneNode objtoadd) {
        app.getRootNode().attachChild(((JmeSceneNode) objtoadd).object3d.spatial);
    }

    /*public void add(NativeMesh objtoadd) {
        app.getRootNode().attachChild(((JmeMesh) objtoadd).spatial);
    }*/

    /*15.6.16 @Override
    public void remove(NativeMesh objtoremove) {
        removeFromScene(objtoremove);
    }*/

    /*21.7.16 @Override
    public void remove(NativeSceneNode objtoremove) {
        removeFromScene(objtoremove);
    }*/

    /**
     * Beim Remove muss der Parent angegebn werden. Der ist nicht unbedingt die rootnode.
     */
    /*21.7.16 private void removeFromScene(NativeObject3D objtoremove) {
        NativeObject3D parent = objtoremove.getParent();
        if (parent == null) {
            app.getRootNode().detachChild(((JmeSceneNode) objtoremove).spatial);
        } else {
           parent.remove(objtoremove);
        }
        sceneupdater.remove(objtoremove);
    }*/
    public void add(NativeLight light) {
        Spatial rootnode = app.getRootNode();
        int cntbefore = rootnode.getLocalLightList().size();
        rootnode.addLight(((JmeLight) light).light);
        int cntafter = rootnode.getLocalLightList().size();
        logger.debug("light added." + cntbefore + "->" + cntafter);
    }

    public void remove(NativeLight light) {
        Light l = ((JmeLight) light).light;
        Spatial rootnode = app.getRootNode();
        int cntbefore = rootnode.getLocalLightList().size();
        rootnode.removeLight(l);
        int cntafter = rootnode.getLocalLightList().size();
        if (cntbefore == cntafter) {
            logger.warn("light not removed:" + l + ", cnt");
        }
        //TODO 29.4.19: remove DirectionalLightShadowRenderer?
        logger.debug("light removed." + cntbefore + "->" + cntafter);
    }

    /*@Override
    public void addActionListener(int[] keycode, NativeActionListener actionListener) {
        this.actionlistener.put(keycode, actionListener);
    }*/

  /*2.3.16  @Override
    public void addAnalogListener(int keycode, NativeAnalogListener actionListener) {
        this.analoglistener.put(keycode, actionListener);
    }*/

    /*2.3.16 @Override
    public void addMouseMoveListener(NativeMouseMoveListener lis) {
        mousemovelistener.add(lis);
    }*/

    /**
     * Die uebergebene Camera wird ignoriert, weil es eh die einzig existierende sein muesste.
     */
    /*29.9.18@Override
    public void enableModelCamera(NativeSceneNode model, NativeCamera nativecamera, Vector3 position, Vector3 lookat) {
        enableModelCameracalled = true;
        flyCam.setEnabled(false);
        // Die Camera selber bleibt wohl, nur der Control aendert sich, oder?
        // CameraNode ist evtl. besser geeignet, um mit einem target zusammen zu moven.
        // Infos dazu: http://wiki.jmonkeyengine.org/doku.php/jme3:advanced:making_the_camera_follow_a_character
        boolean usechase = false;
        if (usechase) {
            ChaseCamera chaseCam = new ChaseCamera(app.getCamera(),
                    ((JmeSceneNode) model).object3d.spatial, app.getInputManager());
        } else {
            Camera camera = app.getCamera();
            if (model == null) {
                camera.setLocation(((JmeVector3) position).vector3);
                // Was ist denn der richtige up-Vektor?
                // 6.11.15 Nehmen wird den Default aus der Camera, der hat sich mit dem Stepcontroller
                // bewaehrt.
                camera.lookAt(((JmeVector3) lookat).vector3, JmeCamera.defaultup);
            } else {
                // Camera an ein Model hängen
                /* / CameraNode verschoben nach JmeCamera

                CameraNode cameraNode = new CameraNode("Camera Node", camera);

                cameraNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
                // 01.09.15 ein setLocation hat die cameraNode nicht. Aber die posotion und lookat beziehen sich doch eh auf die
                // Camera. Die Node Translation muss auch auch gesetzt werden, sonst geht die ModelCamera nicht.
                cameraNode.setLocalTranslation(((JmeVector3) position).vector3);
                //01.09.2015: Die uebergebene Position scheint (zumindest bei MazeScene, zu klein zu sein. Irgendwas
                //stimmt da noch nicht. Aber im grossen und ganzen passt das.
                //cameraNode.setLocalTranslation(new Vector3f(0, 2, 4));
                //14.10.15: Jetzt stimmt es aber wohl ganz gut
                //01.09.2015: Man muss den lookat auf der CamraNode nochmal( oder nur da?) setzen, sonst ist der Blick und auch
                // die Bewegungsrichtung völlig falsch.
                cameraNode.lookAt(((JmeVector3) lookat).vector3, Vector3f.UNIT_Y);
                //cameraNode.lookAt(((JmeModel) model).getNode().getLocalTranslation(), Vector3f.UNIT_Y);
                ((JmeModel) model).getNode().attachChild(cameraNode);
                //nur zum Testen camera.setFrustumPerspective(60,800f/600f,1,1000);
                ** /
            }
        }
    }*/
    @Override
    public Dimension getDimension() {
        //ist bei JME scheinbar nicht änderbar
        return AbstractSceneRunner.getInstance().dimension;
    }

    public void attachCameraToModel() {

    }

    com.jme3.scene.Node getRootNode() {
        return app.getRootNode();
    }

    synchronized public String getUniqueName() {
        return "name" + uniqueid++;
    }
}
