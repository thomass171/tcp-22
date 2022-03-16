package de.yard.threed.engine.ecs;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.List;

public class EcsHelper {

    private static Log logger = Platform.getInstance().getLog(EcsHelper.class);

    public static List<EcsEntity> findAllEntities() {
        return SystemManager.findEntities(null);
    }

    public static List<EcsEntity> findEntitiesByName(String name) {
        return SystemManager.findEntities(e -> name.equals(e.getName()));
    }

    public static List<EcsEntity> findEntitiesByComponent(String tag) {
        return SystemManager.findEntities(e -> e.getComponent(tag) != null);
    }

    public static EcsEntity findEntityById(int id) {
        List<EcsEntity> candidates = SystemManager.findEntities((e) -> e.getId() == id);
        if (candidates.size() == 0) {
            return null;
        }
        if (candidates.size() > 1) {
            logger.error("inconsistency: Multiple entity id " + id);
        }
        return candidates.get(0);
    }
}
