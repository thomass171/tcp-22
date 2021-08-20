package de.yard.threed.engine;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by thomass on 10.11.15.
 * 13.11.15: Erstmal als Singleton mit init Methode, bis besseres Konzept da ist.
 * 29.04.16: Das neue Konzept sieht vor, dass der Controller im RunnerHelper untergebracht ist. Dann
 * muss er auch kein Updater mehr sein.
 */
public class SceneAnimationController implements /*SceneUpdater,*/ AnimationController {
    static Log logger = Platform.getInstance().getLog(SceneAnimationController.class);

    // Die Liste enthält die gerade laufenden Animationen. Beendete werden entfernt.
    private HashMap<Animation, Boolean> animations = new HashMap<Animation, Boolean>();
    List<Animation> pausedanimations = new ArrayList<Animation>();
    public static SceneAnimationController instance = null;

    private SceneAnimationController() {

    }

    /**
     * Den Aufruf muesste man vielleicht besser irgendwo zentral unterbringen.
     * 29.4.16: Im Runnerhelper
     */
    public static void initForRunnerHelper() {
        logger.info("initForRunnerHelper");
        if (instance != null) {
            //2.5.20: Keine Exception mehr. Nur wenn es schon Daten gibt? Das gibts bei Tests aber. Ach, wie bisher. Das soll doch zentral gemacht werden.
            throw new RuntimeException("already inited");

        }
        instance = new SceneAnimationController();
    }

    public static SceneAnimationController getInstance() {
        return instance;
    }

    /**
     * TODO: dass ist nicht threadsafe
     *
     * @param animation
     */
    @Override
    public void startAnimation(Animation animation, boolean forward) {
        // Bei einer Richtungsaenderung wird die Animation einfach ueberschrieben
        this.animations.put(animation, forward);
        // evtl. Pause aufheben
        if (pausedanimations.contains(animation)) {
            pausedanimations.remove(animation);
        }
    }

    @Override
    public void pauseAnimation(Animation animation) {
        if (pausedanimations.contains(animation)) {
            pausedanimations.remove(animation);
        } else {
            pausedanimations.add(animation);
        }
    }

    @Override
    public void resetAnimation(Animation animation) {
        animation.reset();
    }


  //  @Override
    public void update() {
        updateAnimations();


    }

    private void updateAnimations() {
        List<Animation> terminated = new ArrayList<Animation>();

        for (Animation a : animations.keySet()) {
            if (!pausedanimations.contains(a)) {
                boolean completed = a.process((boolean)animations.get(a));
                logger.debug("processed animation " + a.getName() + ". completed=" + completed);
                if (completed) {
                    terminated.add(a);
                    if (a.lis != null) {
                        a.lis.animationCompleted(a);
                    }
                }
            }
        }
        for (Animation a : terminated) {
            logger.debug("Animation terminated");
            animations.remove(a);
        }
    }

    public int getRunningAnimationCnt() {
        return animations.size();
    }
}
