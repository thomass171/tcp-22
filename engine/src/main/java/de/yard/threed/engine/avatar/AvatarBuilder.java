package de.yard.threed.engine.avatar;

import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;

/**
 *
 */
public interface AvatarBuilder {
    /**
     * Needs/Might add AvatarComponent (as marker), AnimationComponent.
     */
    SceneNode buildAvatar(EcsEntity player);
}
