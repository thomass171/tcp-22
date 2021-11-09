package de.yard.threed.engine.avatar;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.TeleportComponent;
import de.yard.threed.core.Color;

/**
 * Einfach nur eine SceneNode/Entity, um damit die Camera analog anderer Objekte versetzen zu können und um eine
 * Abstraktion für eine Person zu haben, die Cockpit/View Plätze wechselt. Für VR ist diese Abstraktion auch
 * ganz intuitiv, weil die Controller dran sind und XYZ-Justierung geht.
 * <p>
 * Optional mit oder ohne ECS nutzbar.
 * <p>
 * Der Avatar hat als Defaultblickrichtung den OpenGL Default, d.h. nach -z mit y up. Wenn man das anders braucht, gibt es
 * die Builder.
 * <p>
 * Die VR Controller sind hier, weil man bei VR besser einen Referenzpunkt hat, an den man die Controller haengt.
 * Dafuer muss der Avatar gar nicht sichtbar sein. Ohne ginge es wohl auch, ist aber halt praktisch.
 * <p>
 * Wenn der Avatar sichtbar sein soll, kann man ja ein Mesh dranhaengen. Optional hier ein kleiner grüner Würfel.
 * <p>
 * 27.11.20 Smmothly move to AvatarSystem/AvatarComponent.
 * 11.5.21: Avatar is nothing more than the visual representation of a player. Everything view related is in {@link Observer}. (MA35)
 * Könnte auch direkt SceneNode sein?
 */
public class Avatar {
    Log logger = Platform.getInstance().getLog(Avatar.class);
    //2.11.20: Eigentlich sollten entities nicht ausserhalb ECS vorgehalten werden. Gibt es einen besonderen Grund?
    @Deprecated
    public EcsEntity avatarE;
    public TeleportComponent pc;
    // 10.?.2020: AvatarComponent enthaelt jetzt alle Avatardaten.
    public AvatarComponent ac;
    // 3.5.21: Die Controller aber doch besser nicht. Die sind doch Teil des Models(??).
    //11.5.21boolean vrEnabled = false;
    Observer observer;
    private SceneNode mainNode;
    private boolean hasMesh = false;

    public Avatar(Camera camera, Quaternion orientation, boolean withecs) {


        ac = new AvatarComponent();
        mainNode = new SceneNode();
        mainNode.setName("Avatar");
        //11.5.21ac.vrcarrier = new SceneNode();
        //11.5.21ac.vrcarrier.setName("VR Carrier");
        //11.5.21ac.mainNode.attach(ac.vrcarrier);

        //Rotator spiegelt die 6DOF des Headsets und ist nicht mit vrcarrier kombiniert. Wer weiss, ob sich dadurch nicht
        //die XYZ order ändert.
        //11.5.21ac.rotator = new SceneNode();
        //11.5.21ac.rotator.setName("Rotator");
        //11.5.21ac.rotator.getTransform().setParent(ac.vrcarrier.getTransform());
        //11.5.21ac.rotator.getTransform().setRotation(orientation);
        /*11.5.21if (!vrEnabled) {
            if (camera != null) {
                //MP has no camera
                camera.attachToModel(ac.rotator.getTransform());
                //Camera Carrier über Rotator exakt an VR Carrier.
                camera.getCarrier().getTransform().setPosition(new Vector3(0, 0, 0));
            }
            //7.5.21 this.ac.camera = camera;
        }*/
        if (camera != null) {
            // 24.10.21: Das ist doch fuer Backwardcompat, also deprecated?
            observer = Observer.buildForCamera(camera);
            camera.attachToModel(mainNode.getTransform());
        }

        if (withecs) {
            pc = new TeleportComponent(mainNode);
            avatarE = new EcsEntity(mainNode, pc);
            avatarE.setName("Avatar");
            //9.3.21: das fehlte doch immer schon:
            avatarE.addComponent(ac);
        }
        //MA33 probeController();

        //5.5.21 ac.vrposition = new Vector3(0, -AvatarComponent.bestPracticeRiftvryoffset, 0);

        //11.5.21ac.buildFaceNode();

    }

    /**
     * MA35: Constructor without camera and in generel more simple.
     *
     * @return
     */
    public Avatar() {
        ac = new AvatarComponent();
        mainNode = new SceneNode();
        mainNode.setName("Avatar");


    }

    public static Avatar buildDefault(Camera camera) {
        return new Avatar(camera, new Quaternion(), true);
    }

    /**
     * Avatar without ECS for simple test scenes.
     *
     * @param camera
     * @return
     */
    public static Avatar buildSimple(Camera camera) {
        return new Avatar(camera, new Quaternion(), false);
    }

    /**
     * Avatar rotieren zu Blickrichtung -x und z up, was die Default FG Aircraft Model Orientierung ist.
     * TODO: ist die Rotation verfifiziert?
     *
     * @return
     */
    public static Avatar buildForFlightGearModelOrinetation(Camera camera) {
        return new Avatar(camera, Quaternion.buildFromAngles(new Degree(90), new Degree(0), new Degree(90)), true);
    }

    /**
     * Für VR muss die Camera (eigentlich der Carrier) niedriger, weil der VR Treiber einen y Wert um 1.3 liefert.
     * 19.10.18: Abhaengig davon, ob der Avatar sichtbar ist, muss man vielleicht auch den Avatar statt
     * der Camera(carrier) versetzen.
     * 22.10.18: Ja, das ist besser, weil die Controller dann mitgehen.
     * 15.8.19: Dieser toggle deprecated, weil die XYZ Justierung besser ist.
     * 4.10.19: Verklehrt ist so eine automatische y Versetzung aber nicht. XYZ kann man ja noch zusätzlich machen.
     * Aber dafuer gibt es lower und raise.
     * 9.10.19: Es waere mittlerweile wohl auch nicht ricvhtig, die mainNode zu positionieren.
     */
    /*@Deprecated
    private void toggleVR() {
        if (vrdown) {
            vrdown = false;
        } else {
            vrdown = true;
        }
        logger.debug("toggleVR: vrdown=" + vrdown);
        if (position != null && !vrdown) {
            mainNode.getTransform().setPosition(position);
            return;
        }
        if (vrposition != null && vrdown) {
            mainNode.getTransform().setPosition(vrposition);
            return;
        }
        //adjustVR((vrdown) ? new Vector3(0, -vryoffset, 0) : new Vector3(0, -vryoffset, 0));
    }*/

    /**
     * Für das automatische VR lowern. (Der "main move")
     */
    /*11.5.21public void lowerVR() {
        if (ac.vrdown) {
            return;
        }
        logger.debug("Lowering avatar for VR");
        ac.vrdown = true;
        ac.adjustVR(/*new Vector3(0, -vryoffset, 0)* /);
    }*/

    /**
     * Für das automatische VR raise (Der "main move")
     */
    /*11.5.21public void raiseVR() {

        if (!ac.vrdown) {
            return;
        }
        logger.debug("Raising avatar for VR");
        ac.vrdown = false;
        ac.adjustVR(/*new Vector3(0, vryoffset, 0)* /);
    }*/

    /**
     * Needed only for non ECS?
     * MA35
     */
    public void update() {
        if (observer!=null) {
            observer.update();
        }
    }

    /**
     * An die Camera statt Avatar, damit raise/lower mitmachen. Das koppelt aber die Rotation
     * 15.8.19:jetzt am Carrier, damit avatar (in maze) positioniert werden kann.
     *
     * @param controller
     */
    /*3.5.21 no longer neede? private void attach(VRController controller) {
        //camera.attach(controller.getTransform());
        ac.vrcarrier.attach(controller);
    }*/

    /**
     * Die Position des Viewpoint setzen. Das ist ausserhalb VR identisch zum Setzen der Camera. In VR greift noch ein Offset,
     * um die Floorerhöhung durch VR wieder auszugleichen.
     * 9.10.19: vrposition nicht mehr reingeben sondern nur intern setzen. Dann kann man Teleport besser verwenden.
     * wie steht denn setPosition zu Teleport? z.B in CockpitScene? Naja, ist halt die Position der mainNode.
     */
    public void setPosition(Vector3 position/*, Vector3 vrposition*/) {
        //7.4.21 wird z.Z. eh nicht verwendet this.ac.position = position;
        //this.vrposition = vrposition;
        mainNode.getTransform().setPosition(position);
    }

    /**
     * 15.5.21: Das ist fragwürdig, denn kippen dürfte/könnte merkwürdige Effekte haben.
     * @param rotation
     */
    public void setRotation(Quaternion rotation) {
        mainNode.getTransform().setRotation(rotation);
    }

    public SceneNode getSceneNode() {
        return mainNode;
    }

    /**
     * Simpler Body zum Testen. Normal ist er grün, bei vr low ist er gelb.
     */
    public void enableBody() {
        hasMesh = true;
        setMesh();
    }


    /*11.5.21public SceneNode getFaceNode() {
        return ac.getFaceNode();
    }*/

    public void setBestPracticeRiftvryoffset(double offset) {
     /*5.5.21    logger.debug("setBestPracticeRiftvryoffset:" + offset);
        AvatarComponent.bestPracticeRiftvryoffset = offset;
        ac.vrposition = new Vector3(0, -AvatarComponent.bestPracticeRiftvryoffset, 0);*/
    }

    private void setMesh() {
        if (hasMesh) {
           /* if (vrdown) {
                mainNode.setMesh(new Mesh(Geometry.buildCube(0.1f, 0.1f, 0.1f), Material.buildBasicMaterial(Color.YELLOW)));
            } else */{
                mainNode.setMesh(new Mesh(Geometry.buildCube(0.1f, 0.1f, 0.1f), Material.buildBasicMaterial(Color.GREEN)));
            }
        }
    }

    public void dumpDebugInfo() {
        logger.info("mainNode.y=" + mainNode.getTransform().getPosition().getY());
        //logger.info("vrcarrier.y=" + ac.vrcarrier.getTransform().getPosition().getY());
        //logger.info("rotator.y=" + ac.rotator.getTransform().getPosition().getY());
        //logger.info("offsetstep=" + ac.offsetstep);
    }

    public SceneNode getNode() {
        return mainNode;
    }
}
