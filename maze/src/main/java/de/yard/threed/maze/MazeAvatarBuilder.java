package de.yard.threed.maze;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.avatar.AvatarABuilder;
import de.yard.threed.engine.avatar.AvatarBuilder;
import de.yard.threed.engine.ecs.EcsEntity;

public class MazeAvatarBuilder implements AvatarBuilder {

    AvatarABuilder avatarABuilder;

    public MazeAvatarBuilder() {
        // AvatarA needs to be raised above ground (maze is in xz plane) to have the view point in head height for viewing other avatars eye to eye.
        // But since it only has a height of appx 1, we scale it up to avoid a hover effect. And it should not be to close to the ground to have the home marker
        // visible.
        avatarABuilder = new AvatarABuilder(new LocalTransform(new Vector3(0, 0.5, 0), Quaternion.buildRotationY(new Degree(-90)), new Vector3(1.2, 1.3, 1.2)));
    }

    @Override
    public SceneNode buildAvatar(EcsEntity player) {
        BotComponent botComponent = BotComponent.getBotComponent(player);

        SceneNode avatar;

        if (botComponent == null || !botComponent.isMonster()) {
            avatar = avatarABuilder.buildAvatar(player);
        } else {
            avatar = MazeModelBuilder.buildMonster();
            // should be appx in head height (where bullets fly)
            avatar.getTransform().setPosition(new Vector3(0, 1.3, 0));
            avatar.getTransform().setRotation(Quaternion.buildRotationY(new Degree(-90)));
            avatar.getTransform().setScale(new Vector3(1.2, 1.2, 1.2));

            avatar = new SceneNode(avatar);
        }
        return avatar;
    }
}
