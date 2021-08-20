package de.yard.threed.engine.platform;

import de.yard.threed.engine.SceneNode;

/**
 * Created by thomass on 14.09.16.
 */
public interface ProcessPolicy {
    SceneNode process(SceneNode node, String filename/*MA31, Options opt*/);
}
