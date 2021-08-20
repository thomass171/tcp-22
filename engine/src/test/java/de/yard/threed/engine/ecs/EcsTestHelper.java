package de.yard.threed.engine.ecs;

import de.yard.threed.core.platform.NativeEventBus;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.engine.util.BooleanMethod;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Obs das wirklich braucht?
 * <p>
 * 21.3.20
 */
public class EcsTestHelper {
    public static void initEcs(String... systemname) {
        throw new RuntimeException("geht ueber TestFctoryx");
    }


    public static void processSeconds(int seconds) {
        /*for (int i = 0; i < seconds; i++) {
            TestHelper.processAsync();
            SystemManager.update(1);
        }*/
        Platform pl = (Platform) Platform.getInstance();
        SceneRunnerForTesting sceneRunner = (SceneRunnerForTesting) AbstractSceneRunner.getInstance();
        for (int i = 0; i < seconds; i++) {
            //sceneRunner.prepareFrame(1);
            sceneRunner.singleUpdate(1);
        }
    }

    /**
     * bis es keine mehr gibt
     */
    public static void processRequests() {
        processUntil(()->{return SystemManager.getRequestCount() == 0;});

    }

    public static void processUntil(BooleanMethod booleanMethod) {
        Platform pl = (Platform) Platform.getInstance();
        SceneRunnerForTesting sceneRunner = (SceneRunnerForTesting) AbstractSceneRunner.getInstance();

        int cntr = 0;

        do {
            //SystemManager.update(1);
            sceneRunner.singleUpdate(1);
        } while (!booleanMethod.isTrue() && cntr++ < 1000);
        if (cntr >= 1000) {
            throw new RuntimeException("not aus");
        }
    }

    public static List<Event> getEventHistory() {
        NativeEventBus eventbus = Platform.getInstance().getEventBus();
        //tests muessen immer diesen nehmen if (eventbus instanceof SimpleEventBus) {
           return ((SimpleEventBusForTesting)eventbus).getEventHistory();
        //}
        //return new ArrayList<Event>();
    }

    public static List<Event> getEventsFromHistory(EventType eventType) {
        List<Event> l = getEventHistory();
        return l.stream().filter(e->e.getType() == eventType).collect(Collectors.toList());

    }
}
