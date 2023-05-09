package de.yard.threed.sceneserver;

import de.yard.threed.core.WriteException;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.core.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

/**
 * Client connection either via socket or websocket.
 */
public class ClientConnection {
    private static final Logger logger = LoggerFactory.getLogger(ClientConnection.class.getName());
    private NativeSocket socket;
    private String connectionId;
    private OffsetDateTime connectedAt;
    private static int id = 10000;

    public ClientConnection(NativeSocket clientSocket) {
        logger.debug("Client connected: Starting new ClientConnection");
        this.socket = clientSocket;
        synchronized (this) {
            this.connectionId = "c" + id++;
        }
        this.connectedAt = OffsetDateTime.now();
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
        socket = null;
    }

    public NativeSocket getSocket() {
        return socket;
    }

    public void sendPacket(Packet packet) {
        try {
            // socket might have been closed already.
            if (socket != null) {
                socket.sendPacket(packet);
            }
        } catch (WriteException e) {
            logger.warn("Write to socket failed (connection will be closed): " + e.getMessage());
            close();
        }
    }

    public boolean isClosed() {
        //logger.debug("terminated="+queuingSocketListener.isTerminated());
        return socket == null;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public OffsetDateTime getConnectedAt() {
        return connectedAt;
    }
}


