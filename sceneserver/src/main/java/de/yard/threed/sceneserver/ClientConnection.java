package de.yard.threed.sceneserver;

import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.javanative.QueuingSocketListener;
import de.yard.threed.javanative.SocketEndpoint;
import de.yard.threed.core.Packet;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * QueuingSocketListener runs a separate thread that blocks for reading from a socket.
 */
public class ClientConnection implements NativeSocket {
    private static final Logger logger = LoggerFactory.getLogger(ClientConnection.class.getName());
    Socket clientSocket;
    boolean terminateflag = false;
    boolean terminated = false;
    QueuingSocketListener queuingSocketListener;
    SocketEndpoint endpoint;

    ClientConnection(Socket clientSocket) throws IOException {
        logger.debug("Client connected: Starting new ClientConnection");

        this.clientSocket = clientSocket;

        queuingSocketListener = new QueuingSocketListener(clientSocket);
        queuingSocketListener.start();

        endpoint = new SocketEndpoint(clientSocket);
    }

    /**
     * TODO sync?
     *
     * @param packet
     */
    public void writePacket(List<String> packet) {
        endpoint.writePacket(packet);
    }

    /**
     * Do we need this?
     */
    public void terminate() {
        logger.debug("Setting terminateflag");
        terminateflag = true;
        IOUtils.closeQuietly(clientSocket);
        // TODO: ist das warten eine gute Idee, oder gibts da besere Lösungen?
        while (!terminated) {
            try {
                Thread.sleep(1);
                logger.warn("Waiting for termination");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * TODO synced?
     *
     * @return
     */
    @Override
    public Packet getPacket() {
        List<String> s = queuingSocketListener.getPacket();
        return Packet.buildFromBlock(s);
    }

    @Override
    public void close() {
        throw new RuntimeException("not yet");
    }

    public boolean hasPacket() {
        return queuingSocketListener.hasPacket();
    }

    @Override
    public void sendPacket(Packet packet) {
        /*for (ClientConnection clientConnection : clientConnections) {
            clientConnection.writePacket(packet.getData());
        }*/
        writePacket(packet.getData());
    }

    public boolean isClosed() {
        //logger.debug("terminated="+queuingSocketListener.isTerminated());
        return queuingSocketListener.isTerminated();
    }
}


