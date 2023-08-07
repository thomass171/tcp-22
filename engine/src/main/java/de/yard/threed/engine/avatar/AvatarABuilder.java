package de.yard.threed.engine.avatar;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.loader.PortableModelList;

/**
 * Build an 'A' avatar. Textures exist with four different colors (see TeamColor).
 */
public class AvatarABuilder {

    private Log logger = Platform.getInstance().getLog(AvatarABuilder.class);

    private LocalTransform offset;

    public AvatarABuilder(LocalTransform offset) {
        this.offset = offset;
    }

    public SceneNode buildAvatar(EcsEntity player, TeamColor teamColor) {
        String color = teamColor.getColor();
        logger.debug("Building avatar A with color " + color + " for player " + player);
        PortableModelList pml = AvatarPmlFactory.buildAvatarA(color);
        SceneNode model = pml.createPortableModelBuilder().buildModel(null, null);
        model.getTransform().setPosition(offset.position);
        model.getTransform().setRotation(offset.rotation);
        model.getTransform().setScale(offset.scale);
        return new SceneNode(model);
    }
}
