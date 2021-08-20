package de.yard.threed.engine;

import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

/**
 * Date: 26.08.14
 */
public class MoveAnimation extends Animation {
    Log logger = Platform.getInstance().getLog(MoveAnimation.class);
    private Vector3 targetposition;
    private  Vector3 startposition;
    private  Vector3 animationtranslate;
    int stepstogo, steps;
    SceneNode moveobject;
    Quaternion animationStartQuaternion;
    Quaternion animationTargetQuaternion;

    /**
     * 31.8.16: Nicht mehr die Anzahl steps, sondern die Laufzeitdauer angeben. Die Animation errechnet dann aus den FPS die erforderliche anzahl steps.
     * Und start pos/rotation ueberhaupt nicht mehr uebergeben, die sind doch in der node.
     */
    public MoveAnimation(SceneNode moveobject, /*Vector3 startposition,*/ Vector3 targetposition,/*Quaternion  animationStartQuaternion,*/Quaternion animationTargetQuaternion, int duration) {
        steps = 5;//TODO berechnen
        this.moveobject = moveobject;
        this.startposition = moveobject.getTransform().getPosition();//startposition;
        this.targetposition = targetposition;
       stepstogo = steps;
        this.animationStartQuaternion = moveobject.getTransform().getRotation();//animationStartQuaternion;
        this.animationTargetQuaternion = animationTargetQuaternion;
        animationtranslate = targetposition.subtract(startposition);
        //logger.debug("animationtranslate vor divide=" + animationtranslate.dump("\n"));
        animationtranslate = animationtranslate.divideScalar(steps);
        //logger.debug("animationtranslate nach divide=" + animationtranslate.dump("\n"));

    }

    @Override
    public boolean process(boolean forward) {
        //logger.debug("process");
        float animationslerp = (float)(steps - stepstogo + 1) * 1.0f / steps;
        Quaternion intermediaterotation = animationStartQuaternion.slerp(animationTargetQuaternion,animationslerp);
       // logger.debug("" + (steps-stepstogo) + ": intermediateQuaternion=" + intermediaterotation.dump(" ") + ", animationslerp=" + animationslerp);

        moveobject.getTransform().setRotation(intermediaterotation);
        //moveobject.translate(animationtranslate,/*animationtranslate.length()*/1);
        moveobject.getTransform().setPosition(moveobject.getTransform().getPosition().add(animationtranslate));
        stepstogo--;
        /*11.12.15 try {
            Thread.sleep(100); //TODO
        } catch (/*Interrupted* /Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/
        if (stepstogo == 0) {
            logger.debug("Animation terminated");
            //logger.debug("currentmodel m=\n" +((moveobject.getTransform().getLocalModelMatrix()).dump("\n")));

        }
        return stepstogo == 0;
    }

    @Override
    public String getName() {
        return "??";
    }
}
