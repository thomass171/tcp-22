package de.yard.threed.engine.avatar;

import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;

/**
 *
 */
public interface AvatarBuilder {
    SceneNode buildAvatar(EcsEntity player);
}
