package de.yard.threed.platform.jme;

import de.yard.threed.core.Util;
import de.yard.threed.javanative.QueuingSocketListener;
import de.yard.threed.javanative.SocketClient;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.core.Packet;

import java.io.IOException;

public class JmeSocket implements NativeSocket {
    SocketClient queuingSocketClient;
    QueuingSocketListener queuingSocketListener;

    public JmeSocket(SocketClient queuingSocketClient) throws IOException {
        this.queuingSocketClient = queuingSocketClient;
        queuingSocketClient.connect();
         queuingSocketListener = queuingSocketClient.startListen();
    }

    @Override
    public void sendPacket(Packet packet) {
        Util.notyet();
    }

    @Override
    public Packet getPacket() {
        return Packet.buildFromBlock(queuingSocketListener.getPacket());
    }
}
