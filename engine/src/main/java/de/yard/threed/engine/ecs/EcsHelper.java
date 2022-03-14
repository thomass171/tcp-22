package de.yard.threed.engine.ecs;

import java.util.List;

public class EcsHelper {

    public static List<EcsEntity> findEntitiesByName(String name) {
        return SystemManager.findEntities(e -> name.equals(e.getName()));
    }

    public static List<EcsEntity> findEntitiesByComponent(String tag) {
        return SystemManager.findEntities(e -> e.getComponent(tag) != null);
    }
}
