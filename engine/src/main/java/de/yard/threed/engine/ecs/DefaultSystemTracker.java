package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.Packet;
import de.yard.threed.engine.platform.common.Request;

/**
 * Default implementation doing nothing.
 */
public class DefaultSystemTracker implements SystemTracker {
    @Override
    public void eventsSentToClients() {
    }

    @Override
    public void packetReceivedFromNetwork(Packet packet) {
    }

    @Override
    public void packetSentToNetwork(Packet packet) {
    }

    @Override
    public void eventProcessed(Event evt) {

    }

    @Override
    public void report() {
    }

    @Override
    public void requestPut(Request request) {
    }
}
