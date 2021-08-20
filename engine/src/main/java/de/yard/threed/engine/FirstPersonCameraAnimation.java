package de.yard.threed.engine;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * Fuer Ableitung von ModelAnimation fehlt da aber yaw and pitch.
 * Wird z.B. im Stepcontroller verwendet.
 * 9.3.16: Verwendet statt FirstPersonCamera einfach Camera.
 * Date: 23.08.14
 */
public class FirstPersonCameraAnimation extends Animation {
    Log logger = Platform.getInstance().getLog(FirstPersonCameraAnimation.class);
    private Transform target;
    private Transform start;
    private Degree startyaw, targetyaw, startpitch, targetpitch;
    int stepstogo, steps;
    Camera camera;

    public FirstPersonCameraAnimation(Camera camera, Transform start, Transform target, Degree startyaw, Degree targetyaw, Degree startpitch, Degree targetpitch, int steps) {
        this.camera = camera;
        this.start = start;
        this.target = target;
        this.startyaw = startyaw;
        this.targetyaw = targetyaw;
        this.startpitch = startpitch;
        this.targetpitch = targetpitch;
        stepstogo = steps;
        this.steps = steps;
    }

    @Override
    public boolean process(boolean forward) {
        logger.debug("process");
        Quaternion intermediaterotation = start.getRotation().slerp(target.getRotation(), 1.0f / steps);
        Vector3 intermediateposition = target.getPosition().subtract(start.getPosition().divideScalar(1.0f / steps));
        //TODO 12.6.15: set lösung von Base3d nicht mehr verfuegbar. muss aber wohl wieder rein. camera.setRotation(intermediaterotation);
        camera.getCarrier().getTransform().setPosition(start.getPosition().add(intermediateposition));
        stepstogo--;
        /*THREED TODO try {
            Thread.sleep(100); //TODO
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/
        return stepstogo == 0;
    }

    @Override
    public String getName() {
        return "??";
    }
}
