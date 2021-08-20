package de.yard.threed.core.platform;


import de.yard.threed.core.Event;

/**
 * 
 * In Anlehnung an den Java Event Bus https://java.net/projects/eventbus/. Das mit poll/subscribe/queue/threads ist aber noch nicht der wahre Jakob.
 * 
 * Die Frage polling oder subscribe hat wohl mit dem Mechanismus zu tun. Polling passt zu einer Queue. Und ein Poller ist im Prinzip Busy bzw blockiert.
 * 
 * Created by thomass on 31.08.16.
 */
public interface NativeEventBus {
    // public static boolean perqueue = true;
    
    /*public static void subscribe(Class<Object> objectClass, EventSubscriber eventSubscriber) {
        subscriberlist.add(eventSubscriber);
    }*/

    public  void publish(Event evt/*Type eventtype, Object payload*/) ;
    public Event poll (int timeoutmillis);

    /**
     * Anzahl anliegender Events.
     * @return
     */
    int getEventCount();

    /**
     * Praktisch fuer Tests.
     */
    void clear();
}
