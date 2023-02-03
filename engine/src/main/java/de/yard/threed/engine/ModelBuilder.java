package de.yard.threed.engine;

import de.yard.threed.engine.ecs.EcsEntity;

public interface ModelBuilder {

    /**
     * destinationNode is needed to enable async building. For the same reason there is no return value.
     * Because the success is unknown, the model building might fail. Its up the model builder to have a fallback.
     *
     * @param destinationNode
     * @param entity optional when the model is built for an entity.
     */
    void buildModel(SceneNode destinationNode, EcsEntity entity);
}
