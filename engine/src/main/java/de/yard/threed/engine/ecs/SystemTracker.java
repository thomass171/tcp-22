package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.Packet;

/**
 * Track information about whats happening
 */
public interface SystemTracker {

    void eventsSentToClients();

    void packetReceivedFromClient(Packet packet);

    void packetReceivedFromServer(Packet packet);

    /**
     * Not for events from network.
     * @param evt
     */
    void eventProcessed(Event evt);

    void report();
}
