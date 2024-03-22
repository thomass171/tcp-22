package de.yard.threed.graph;

import de.yard.threed.engine.ecs.EcsTestHelper;
import de.yard.threed.engine.ecs.SystemManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <p>
 */
public class GraphMovingSystemTest {

    /**
     *
     */
    @BeforeEach
    public void setup() {
        EcsTestHelper.setup(() -> {
            SystemManager.addSystem(new GraphMovingSystem());
        });
    }

    @Test
    public void test() throws Exception {
        // t.b.c.
    }
}
