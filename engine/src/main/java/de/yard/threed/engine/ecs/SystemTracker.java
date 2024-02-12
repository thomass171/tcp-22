package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.Packet;
import de.yard.threed.engine.platform.common.Request;

import java.util.List;

/**
 * Track information about whats happening
 */
public interface SystemTracker {

    void eventsSentToClients();

    void packetReceivedFromNetwork(Packet packet);

    void packetSentToNetwork(Packet packet);

    /**
     * Not for events from network.
     * @param evt
     */
    void eventProcessed(Event evt);

    void report();

    void requestPut(Request request);

    List<Request> getRequests();

    List<Packet> getPacketsReceivedFromNetwork();
    List<Event> getEventsProcessed();

    void modelBuilt(String fullName);

    List<String> getModelsBuilt();
}
