package de.yard.threed.maze;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.ModelBuilder;
import de.yard.threed.engine.ModelBuilderRegistry;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.avatar.AvatarABuilder;
import de.yard.threed.engine.avatar.AvatarComponent;
import de.yard.threed.engine.ecs.AnimationComponent;
import de.yard.threed.engine.ecs.EcsEntity;

public class MazeAvatarBuilder implements ModelBuilderRegistry {

    public static String AVATAR_BUILDER = "avatarbuilder";

    AvatarABuilder avatarABuilder;
    MazeModelFactory mazeModelFactory;

    public MazeAvatarBuilder(MazeModelFactory mazeModelFactory) {
        // AvatarA needs to be raised above ground (maze is in xz plane) to have the view point in head height for viewing other avatars eye to eye.
        // But since it only has a height of appx 1, we scale it up to avoid a hover effect. And it should not be to close to the ground to have the home marker
        // visible.
        avatarABuilder = new AvatarABuilder(new LocalTransform(new Vector3(0, 0.5, 0), Quaternion.buildRotationY(new Degree(-90)), new Vector3(1.2, 1.3, 1.2)));
        this.mazeModelFactory = mazeModelFactory;
    }

    @Override
    public ModelBuilder lookupModelBuilder(String key) {
        if (!AVATAR_BUILDER.equals(key)) {
            return null;
        }
        return (destinationNode, entity) -> destinationNode.attach(buildAvatar(entity));
    }

    public SceneNode buildAvatar(EcsEntity player) {
        BotComponent botComponent = BotComponent.getBotComponent(player);

        SceneNode avatar;

        if (botComponent == null || !botComponent.isMonster()) {
            avatar = avatarABuilder.buildAvatar(player);
        } else {
            // decouple monster transform from scale for avoidng math effects and to make a hit monster markable by scaling without changing its position.
            SceneNode monster = mazeModelFactory.buildMonster();
            monster.getTransform().setScale(new Vector3(1.2, 1.2, 1.2));

            avatar = new SceneNode(monster);
            // should be appx in head height (where bullets fly)
            avatar.getTransform().setPosition(new Vector3(0, 1.3, 0));
            avatar.getTransform().setRotation(Quaternion.buildRotationY(new Degree(-90)));
            avatar = new SceneNode(avatar);
            player.addComponent(new AnimationComponent(monster));
        }
        // used ar marker
        player.addComponent(new AvatarComponent());
        return avatar;
    }
}
