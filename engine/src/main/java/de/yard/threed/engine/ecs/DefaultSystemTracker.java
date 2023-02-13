package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.Packet;

/**
 * Default implementation doing nothing.
 */
public class DefaultSystemTracker implements SystemTracker {
    @Override
    public void eventsSentToClients() {

    }

    @Override
    public void packetReceivedFromClient(Packet packet) {

    }

    @Override
    public void packetReceivedFromServer(Packet packet) {

    }

    @Override
    public void eventProcessed(Event evt) {

    }

    @Override
    public void report() {
    }
}
