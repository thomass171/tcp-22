package de.yard.threed.maze;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ModelBuilder;
import de.yard.threed.engine.ModelBuilderRegistry;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.avatar.AvatarABuilder;
import de.yard.threed.engine.avatar.AvatarComponent;
import de.yard.threed.engine.avatar.TeamColor;
import de.yard.threed.engine.ecs.AnimationComponent;
import de.yard.threed.engine.ecs.EcsEntity;

/**
 * For login player, bots and monster
 */
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
        if (key == null) {
            return null;
        }
        if (!StringUtils.startsWith(key, AVATAR_BUILDER)) {
            return null;
        }
        if (StringUtils.contains(key, ".")) {
            // Derive avatar style/color from team membership.
            // should be a team color
            String subName = StringUtils.substringAfter(key, AVATAR_BUILDER + ".");
            TeamColor teamColor = TeamColor.fromName(subName);
            if (teamColor == null) {
                getLogger().warn("team color not found: " + subName);
                //teamColor = TeamColor.TEAMCOLOR_BLUE;
            }
            // teamcolor needs to be final
            return (destinationNode, entity) -> destinationNode.attach(buildAvatar(entity, teamColor));
        }
        return (destinationNode, entity) -> destinationNode.attach(buildAvatar(entity, null));
    }

    /**
     * Heads up when using EcsEntity. In client mode it might not contain expected components.
     */
    public SceneNode buildAvatar(EcsEntity player, TeamColor teamColor) {

        SceneNode avatar;

        if (teamColor != null) {
            avatar = avatarABuilder.buildAvatar(player, teamColor);
        } else {
            // Assume it is a monster
            // decouple monster transform from scale for avoiding math effects and to make a hit monster markable by scaling without changing its position.
            SceneNode monster = mazeModelFactory.buildMonster();
            monster.getTransform().setScale(new Vector3(1.2, 1.2, 1.2));

            avatar = new SceneNode(monster);
            // should be appx in head height (where bullets fly)
            avatar.getTransform().setPosition(new Vector3(0, 1.3, 0));
            avatar.getTransform().setRotation(Quaternion.buildRotationY(new Degree(-90)));
            avatar = new SceneNode(avatar);
            player.addComponent(new AnimationComponent(monster));
        }
        // used as marker
        player.addComponent(new AvatarComponent());
        return avatar;
    }

    private static Log getLogger() {
        return Platform.getInstance().getLog(MazeAvatarBuilder.class);
    }
}
