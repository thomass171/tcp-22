package de.yard.threed.engine;

import java.util.List;

/**
 * 27.4.16: Zur Entkopplung.
 * 
 * Created by thomass on 30.01.15.
 */
public interface AnimatedModel {
    public List<Animation> getAnimations();
    public void processAnimationStep(int value);
}
