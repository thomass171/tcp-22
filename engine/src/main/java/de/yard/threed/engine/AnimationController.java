package de.yard.threed.engine;

/**
 * Created by thomass on 30.01.15.
 */
public interface AnimationController {
    /**
     * Eine Animation kann vorwärts und rückwärts laufen
     * @param animation
     * @param forward
     */
    void startAnimation(Animation animation, boolean forward);

    void pauseAnimation(Animation animation);

    void resetAnimation(Animation animation);
}
