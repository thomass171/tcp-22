package de.yard.threed.core.testutil;

import de.yard.threed.core.Event;
import de.yard.threed.core.platform.SimpleEventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple bus with history
 * <p>
 * Created by thomass on 28.4.20.
 */
public class SimpleEventBusForTesting extends SimpleEventBus {
    // private static SimpleEventBusForTesting instance = null;
    List<Event> history = new ArrayList<>();

    public SimpleEventBusForTesting() {
        super();
    }

   /* public static NativeEventBus getInstance() {
        if (instance == null) {
            instance = new SimpleEventBusForTesting();
        }
        return instance;
    }*/

    @Override
    public Event poll(int timeoutmillis) {

        Event evt = super.poll(timeoutmillis);
        if (evt != null) {
            history.add(evt);
        }
        return evt;
    }

    @Override
    public void clear() {
        super.clear();
        history.clear();
    }

    public List<Event> getEventHistory() {
        return history;
    }
}
