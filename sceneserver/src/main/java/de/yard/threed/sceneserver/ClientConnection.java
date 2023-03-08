package de.yard.threed.sceneserver;

import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.core.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client connection either via socket or websocket.
 */
public class ClientConnection {
    private static final Logger logger = LoggerFactory.getLogger(ClientConnection.class.getName());
    NativeSocket socket;

    public ClientConnection(NativeSocket clientSocket) {
        logger.debug("Client connected: Starting new ClientConnection");
        this.socket = clientSocket;
    }

    /**
     * @param packet
     */
    public void writePacket(Packet packet) {
        socket.sendPacket(packet);
    }

    /**
     * TODO synced?
     *
     * @return
     */
    public Packet getPacket() {
        return socket.getPacket();
    }

    public void close() {
        socket.close();
    }

    public NativeSocket getSocket() {
        return socket;
    }

    public void sendPacket(Packet packet) {
        writePacket(packet);
    }

    public boolean isClosed() {
        //logger.debug("terminated="+queuingSocketListener.isTerminated());
        return socket == null;
    }
}


