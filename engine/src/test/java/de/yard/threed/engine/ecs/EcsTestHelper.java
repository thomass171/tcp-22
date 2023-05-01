package de.yard.threed.engine.ecs;

import de.yard.threed.core.Packet;
import de.yard.threed.core.platform.NativeEventBus;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.engine.testutil.EventFilter;
import de.yard.threed.engine.util.BooleanMethod;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Really needed? 16.2.23: Since its getting extended further: yes.
 * <p>
 * 21.3.20
 */
public class EcsTestHelper {

    /**
     * Process "seconds" frames with a tpf of 1 each.
     */
    public static void processSeconds(int seconds) {

        SceneRunnerForTesting sceneRunner = (SceneRunnerForTesting) AbstractSceneRunner.getInstance();
        for (int i = 0; i < seconds; i++) {
            sceneRunner.singleUpdate(1);
        }
    }

    /**
     * until there is no more
     */
    public static void processRequests() {
        processUntil(() -> SystemManager.getRequestCount() == 0);
    }

    public static void processUntil(BooleanMethod booleanMethod) {
        processUntil(booleanMethod, 1.0, 1000);
    }

    public static void processUntil(BooleanMethod booleanMethod, double tpf, int maxCycles) {
        Platform pl = (Platform) Platform.getInstance();
        SceneRunnerForTesting sceneRunner = (SceneRunnerForTesting) AbstractSceneRunner.getInstance();

        int cntr = 0;

        do {
            sceneRunner.singleUpdate(tpf);
        } while (!booleanMethod.isTrue() && cntr++ < maxCycles);
        if (cntr >= maxCycles) {
            throw new RuntimeException("max cycles reached");
        }
    }

    public static List<Event> getEventHistory() {
        NativeEventBus eventbus = Platform.getInstance().getEventBus();
        //tests muessen immer diesen nehmen if (eventbus instanceof SimpleEventBus) {
        return ((SimpleEventBusForTesting) eventbus).getEventHistory();
        //}
        //return new ArrayList<Event>();
    }

    public static List<Event> getEventsFromHistory(EventType eventType) {
        List<Event> l = getEventHistory();
        return l.stream().filter(e -> e.getType() == eventType).collect(Collectors.toList());

    }

    /**
     * Request packets are silently ignored
     */
    public static List<Event> toEventList(List<Packet> packets) {
        List<Event> result = new ArrayList<Event>();
        for (Packet packet : packets) {
            if (DefaultBusConnector.isEvent(packet)) {
                Event evt = DefaultBusConnector.decodeEvent(packet);
                if (evt == null) {
                    throw new RuntimeException("decode failed");
                }
                result.add(evt);
            }
        }
        return result;
    }

    public static List<Event> filterEventList(List<Event> list, EventFilter filter) {

        List<Event> result = new ArrayList<Event>();
        for (Event e : list) {
            if (filter == null || filter.matches(e)) {
                result.add(e);
            }
        }
        return result;
    }
}
