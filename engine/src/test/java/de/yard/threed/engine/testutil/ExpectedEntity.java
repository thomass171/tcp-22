package de.yard.threed.engine.testutil;

import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsHelper;

import java.util.List;

public class ExpectedEntity {

    public String name;
    public int expectedCount;

    public ExpectedEntity(String name, int expectedCount) {
        this.name = name;
        this.expectedCount = expectedCount;
    }

    public static boolean contains(List<EcsEntity> entityList, ExpectedEntity expectedEntity) {
        return EcsHelper.filterList(entityList, e -> e.getName() != null && e.getName().equals(expectedEntity.name)).size() == expectedEntity.expectedCount;
    }
}
