package de.yard.threed.engine.testutil;

import de.yard.threed.core.Packet;
import de.yard.threed.core.platform.NativeSocket;

public class SocketMock implements NativeSocket {
    @Override
    public void sendPacket(Packet packet) {

    }

    @Override
    public Packet getPacket() {
        return null;
    }

    @Override
    public void close() {

    }
}
