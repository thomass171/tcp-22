package de.yard.threed.server;

import de.yard.threed.core.Packet;
import de.yard.threed.core.platform.NativeSocket;

import java.util.List;

public class ServerSocket implements NativeSocket {

    List<ClientConnection> clientConnections;

    public ServerSocket(List<ClientConnection> clientConnections) {
        this.clientConnections = clientConnections;
    }

    @Override
    public void sendPacket(Packet packet) {
        for (ClientConnection clientConnection : clientConnections) {
            clientConnection.writePacket(packet.getData());
        }
    }

    /**
     * not needed currently
     *
     * @return
     */
    @Override
    public Packet getPacket() {
        return null;
    }
}
