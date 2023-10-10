package de.yard.threed.engine;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * <p>
 * 31.8.16: Sollte allgemeingültiger werden.
 * Ein Controller der einzelne festgelegte Schritte (Positionen/Blickrichtung) verwendet.
 * Nicht mehr so fixiert auf die Camera.
 * Kann an ein model attached sein oder alleinstehend.
 * 28.3.17: Abgelöst durch TeleporterSystem/TeleportComponent. Darum eigentlich deprecated. Fuer non ECS Systeme aber immer noch ok.
 * Und TeleporterSystem verwendet Teile von hier.
 * 3.10.19: Key 'T' statt 'V'
 * 24.10.19: setNewStep in TeleportComponent kopiert, so dass TeleporterSystem hier nichts mehr nutzt.
 * 03.02.22: Now a shared list with TeleportComponent
 */
public class StepController {
    Log logger = Platform.getInstance().getLog(StepController.class);

    public ViewpointList viewpointList;
    private boolean useanimation = false;
    // 31.8.16: camera darf jetzt auch null sein. Dann kommt das model zum cyclen von aussen. oder doch Constructor?
    // 15.5.21: Deprecated zugunsten des Observers. Dafuer muss der lookat aber konvertiert werden.
    @Deprecated
    Camera camera;
    Observer observer;
    SceneNode node;
    Transform target;

    @Deprecated
    public StepController(Camera camera) {
        this.camera = camera;
    }

    public StepController(Transform target, ViewpointList viewpointList) {
        this.target = target;
        this.viewpointList = viewpointList;
    }

    public StepController(SceneNode node) {
        this.node = node;
    }
    
    /*public void addStep(Vector3 position, Degree yaw, Degree pitch) {
        stepposition.add(position);
        stepyaw.add(yaw);
        steppitch.add(pitch);
    } */

    public void step(boolean forward) {
        viewpointList.step(forward);
        LocalTransform transform = viewpointList.getTransform();
        boolean useanimation = false;
        setNewStep(target,null/*node,*/, transform.position, transform.rotation, useanimation, viewpointList.getParent());
    }

    public  void stepTo( int index) {

        LocalTransform transform = viewpointList.stepTo(index);
        boolean useanimation = false;
        setNewStep(target,null/*node,*/, transform.position, transform.rotation, useanimation, viewpointList.getParent());
    }

    /*private void setNewStep(int steppos) {
        setNewStep(node,stepposition.get(steppos),steprotation.get(steppos),useanimation,camera,stepparent.get(steppos),null/*steplookat.get(steppos)* /,upVector,observer);
    }*/

    /**
     * 6.4.17: Wegen fehlendme up Vector ist steplokat eigentlich deprecated. Naja, andererseits komt er ja mit rein.
     */
    public static void setNewStep(Transform target, SceneNode node, Vector3 position, Quaternion rotation, boolean useanimation,  Transform stepparent) {
        if (useanimation && node != null) {
            // Animation dürfte nicht zwischen attached/detached gehen, bzw. nur kompliziert. 31.8.16: Darum nicht mit camera sondern nur mit Node.
            //THREED Base3D startprs = fpscamera.getPrsData();
            Animation animation = new MoveAnimation(node, position, rotation, 10);
            SceneAnimationController.getInstance().startAnimation(animation, true);
        } else {
            // erst attachen. Wichtig fuer JME
            //TODO die Abhängigkeit erst attach dann position ist doof und sollte entschaerft werden.
            if (stepparent == null) {
                target.setParent(null);
            } else {
                target.setParent(stepparent);
            }
            target.setPosition(position);
            target.setRotation(rotation);
        }
    }

    public void update(double tpf) {
        if (Input.getKeyDown(KeyCode.Tab) || Input.getKeyDown(KeyCode.T)/*fuer Android || Input.GetKeyDown(KeyCode.RightArrow)*/) {
            //logger.debug("tab key was pressed. currentdelta=" + tpf);
            if (Input.getKey(KeyCode.Shift)) {
                step(false);
            } else {
                step(true);
            }
        }
    }

    public void enableAnimation(boolean enabled) {
        useanimation = enabled;
    }
}