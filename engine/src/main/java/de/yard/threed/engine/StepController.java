package de.yard.threed.engine;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * <p>
 * 31.8.16: Sollte allgemeingültiger werden.
 * Ein Controller der einzelne festgelegte Schritte (Positionen/Blickrichtung) verwendet.
 * Nicht mehr so fixiert auf die Camera.
 * Kann an ein model attached sein oder alleinstehend. 
 * 28.3.17: Abgelöst durch TeleporterSystem/TeleportComponent. Darum eigentlich deprecated. Fuer non ECS Systeme aber immer noch ok.
 * Und TeleporterSystem verwendet Teile von hier.
 * 3.10.19: Key 'T' statt 'V'
 * 24.10.19: setNewStep in TeleportComponent kopiert, so dass TeleporterSystem hier nichts mehr nutzt.
 * 15.05.21: Weiterhin doof, dass es in TeleportComponent dupliziert ist. TODO Extrahieren/merge in StepController20 oder sowas.
 */
public class StepController {
    Log logger = Platform.getInstance().getLog(StepController.class);

    public List<Vector3> stepposition = new ArrayList<Vector3>();
    List<Vector3> steplookat = new ArrayList<Vector3>();
    List<Quaternion> steprotation = new ArrayList<Quaternion>();
    List<SceneNode> stepparent = new ArrayList<SceneNode>();
    int steppos = -1;
    private boolean useanimation = false;
    // 31.8.16: camera darf jetzt auch null sein. Dann kommt das model zum cyclen von aussen. oder doch Constructor?
    // 15.5.21: Deprecated zugunsten des Observers. Dafuer muss der lookat aber konvertiert werden.
    @Deprecated
    Camera camera;
    Observer observer;
    SceneNode node;
    private Vector3 upVector;

    @Deprecated
    public StepController(Camera camera) {
        this.camera = camera;
    }

    public StepController(Observer observer) {
        this.observer = observer;
    }

    public StepController(SceneNode node) {
        this.node = node;
    }
    
    /*public void addStep(Vector3 position, Degree yaw, Degree pitch) {
        stepposition.add(position);
        stepyaw.add(yaw);
        steppitch.add(pitch);
    } */

    /**
     * 15.5.21: TODO lookat hier konvertieren um es nicht als Sonderfall vorhalten zu müssen.
     * @param position
     * @param lookat
     */
    public void addStep(Vector3 position, Vector3 lookat) {
        stepposition.add(position);
        steplookat.add(lookat);
        steprotation.add(null);
        stepparent.add(null);
    }

    public void addStep(Vector3 position, Quaternion rot) {
        stepposition.add(position);
        steplookat.add(null);
        steprotation.add(rot);
        stepparent.add(null);
    }

    public void addStep(SceneNode parent, Vector3 position, Quaternion orientation) {
        stepposition.add(position);
        steplookat.add(null);
        steprotation.add(orientation);
        stepparent.add(parent);
    }

    public void addStep(SceneNode parent, LocalTransform posrot) {
        stepposition.add(posrot.position);
        steplookat.add(null);
        steprotation.add(posrot.rotation);
        stepparent.add(parent);
    }

    public void step(boolean keyPressed, double tpf, boolean forward) {
        if (keyPressed) {
            //logger.debug("key was pressed. currentdelta=" + tpf);

            if (stepposition.size() > 0) {
                if (forward) {
                    if (++steppos >= stepposition.size())
                        steppos = 0;
                } else {
                    if (--steppos < 0)
                        steppos = stepposition.size() - 1;
                }
                setNewStep(steppos);
            }
        }
    }

    public void stepTo(int pos) {
        steppos = pos;
        setNewStep(steppos);
    }

    private void setNewStep(int steppos) {
        setNewStep(node,stepposition.get(steppos),steprotation.get(steppos),useanimation,camera,stepparent.get(steppos),steplookat.get(steppos),upVector);
    }

    /**
     * 6.4.17: Wegen fehlendme up Vector ist steplokat eigentlich deprecated. Naja, andererseits komt er ja mit rein.
     */
    public static void setNewStep(SceneNode node,Vector3 position, Quaternion rotation, boolean useanimation, Camera camera, SceneNode stepparent,Vector3 steplookat,Vector3 upVector){
        if (useanimation && node != null) {
            // Animation dürfte nicht zwischen attached/detached gehen, bzw. nur kompliziert. 31.8.16: Darum nicht mit camera sondern nur mit Node.
            //THREED Base3D startprs = fpscamera.getPrsData();
            Animation animation = new MoveAnimation(node, position, rotation, 10);
            SceneAnimationController.getInstance().startAnimation(animation, true);
        } else {
            if (node == null) {
                // Die Camera            
                if (steplookat != null) {
                    camera.detachFromModel();
                    camera.getCarrier().getTransform().setPosition(position);
                    camera.lookAt(steplookat, upVector);
                } else {
                    if (stepparent == null) {
                        //7.4.17: sicherheitshalber auch hier detachen
                        camera.detachFromModel();
                        camera.getCarrier().getTransform().setPosition(position);
                        camera.getCarrier().getTransform().setRotation(rotation);
                    } else {
                        // erst attachen. Wichtig fuer JME
                        //TODO die Abhängigkeit erst attach dann position ist doof und sollte entschaerft werden.
                        camera.attachToModel(stepparent.getTransform());
                        camera.getCarrier().getTransform().setPosition(position);
                        camera.getCarrier().getTransform().setRotation(rotation);
                        //camera.setPosition(new Vector3(0,0,3));
                        //camera.setRotation(new Quaternion(new Degree(0), new Degree(-90), new Degree(0)));
                    }
                }
               // logger.debug("camera.position: " + camera.getPosition().dump(""));
                //logger.debug("camera.quaternion: " + camera.getRotation().dump(""));
                // logger.debug("camera.rotationx: " + camera.getRotationx());
                //logger.debug("camera.rotationy: " + camera.getRotationy());
                //logger.debug("camera.rotationz: " + camera.getRotationz());
            } else {
                node.getTransform().setPosition(position);
                node.getTransform().setRotation(rotation);
            }
        }
    }

    public void update(double tpf) {
        if (Input.GetKeyDown(KeyCode.Tab) || Input.GetKeyDown(KeyCode.T)/*fuer Android || Input.GetKeyDown(KeyCode.RightArrow)*/) {
            //logger.debug("tab key was pressed. currentdelta=" + tpf);
            if (Input.GetKey(KeyCode.Shift)){
                step(true, tpf, false);
            }else {
                step(true, tpf, true);
            }
        }
    }

    public void enableAnimation(boolean enabled) {
        useanimation = enabled;
    }

    public void setUpVector(Vector3 upVector) {
        this.upVector = upVector;
    }
}