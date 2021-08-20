package de.yard.threed.javacommon;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventSubscriber;
import de.yard.threed.core.platform.NativeEventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A "smart" MT ready event bus. Not C# oder GWT ready.
 *
 * Created by thomass on 31.08.16.
 */
public class JAEventBus implements NativeEventBus {
    private List<EventSubscriber> subscriberlist = new ArrayList<EventSubscriber>();
    private  boolean perqueue = true;
    private LinkedBlockingQueue<Event> queue = new LinkedBlockingQueue<Event>();
    private static NativeEventBus instance;

    public static NativeEventBus getInstance() {
        if (instance == null){
            instance = new JAEventBus();
        }
        return instance;
    }

    public void subscribe(Class<Object> objectClass, EventSubscriber eventSubscriber) {
        subscriberlist.add(eventSubscriber);
    }

    public void publish(Event evt) {
        if (perqueue){
            queue.add(evt);
        }else {
            for (EventSubscriber subscriber : subscriberlist) {
                subscriber.handle(evt.getType(), evt.payload);
            }
        }
    }

    @Override
    public Event poll(int timeoutmillis) {
        try {
            return queue.poll(timeoutmillis,TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getEventCount() {
        return queue.size();
    }

    @Override
    public void clear() {
        queue.clear();
    }


}
