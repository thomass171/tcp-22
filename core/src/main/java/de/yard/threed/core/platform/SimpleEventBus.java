package de.yard.threed.core.platform;

import de.yard.threed.core.Event;
import de.yard.threed.core.platform.NativeEventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple home brew implementation ready for C# and GWT
 * Nur single threaded und ohne wait beim poll. Eigentlich nur fuer Tests, aber immerhin.
 *
 * 28.42.20 Das muss aber doch nicht unbedingt ein Singleton sein.
 * <p>
 * <p>
 * Created by thomass on 28.12.16.
 */
public class SimpleEventBus implements NativeEventBus {
    //private static SimpleEventBus instance = null;
    private List<Event> events = new ArrayList<Event>();

    public SimpleEventBus() {

    }

   /* public static NativeEventBus getInstance() {
        if (instance == null) {
            instance = new SimpleEventBus();
        }
        return instance;
    }*/

    @Override
    public void publish(Event evt/*Type eventtype, Object payload*/) {
        events.add(evt);
        //payloads.add(payload);
    }

    @Override
    public Event poll(int timeoutmillis) {
        if (events.size() == 0) {
            return null;
        }
        Event evt = events.get(0);
        events.remove(0);
        return evt;
    }

    @Override
    public int getEventCount() {
        return events.size();
    }

    @Override
    public void clear() {
        events.clear();

    }


}
