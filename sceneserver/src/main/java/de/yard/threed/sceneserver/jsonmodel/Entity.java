package de.yard.threed.sceneserver.jsonmodel;

import de.yard.threed.engine.Transform;
import de.yard.threed.engine.ecs.EcsEntity;
import lombok.Data;

@Data
public class Entity {
    int id;
    String name;
    String buildername;
    String position;

    public static Entity fromEcs(EcsEntity ecsEntity) {
        Entity e = new Entity();
        e.setId(ecsEntity.getId());
        e.setName(ecsEntity.getName());
        e.setBuildername(ecsEntity.getBuilderName());
        if (ecsEntity.getSceneNode() != null) {

            Transform transform = ecsEntity.getSceneNode().getTransform();
            e.setPosition(transform.getPosition().toSimpleString());
            //       .add("rotation", transform.getRotation())
            //     .add("scale", transform.getScale());
        }
        return e;
    }
}
