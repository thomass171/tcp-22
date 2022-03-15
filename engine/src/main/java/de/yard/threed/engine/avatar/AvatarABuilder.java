package de.yard.threed.engine.avatar;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.loader.PortableModelList;

public class AvatarABuilder implements AvatarBuilder {

    private int colorIndex = -1;
    String[] avatarColors = new String[]{"darkgreen", "red"};
    private LocalTransform offset;

    public AvatarABuilder(LocalTransform offset) {
        this.offset = offset;
    }

    @Override
    public SceneNode buildAvatar() {
        PortableModelList pml = AvatarPmlFactory.buildAvatarA(nextColor());
        SceneNode model = pml.buildModel(null, null);
        model.getTransform().setPosition(offset.position);
        model.getTransform().setRotation(offset.rotation);
        return new SceneNode(model);
    }


    private String nextColor() {
        if (avatarColors == null) {
            return null;
        }
        colorIndex++;
        if (colorIndex >= avatarColors.length) {
            colorIndex = 0;
        }
        return avatarColors[colorIndex];
    }
}
