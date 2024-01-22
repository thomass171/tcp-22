package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.Packet;
import de.yard.threed.engine.platform.common.Request;

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
}
