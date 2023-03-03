package de.yard.threed.sceneserver;

import de.yard.threed.core.Packet;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.javanative.BlockReader;
import de.yard.threed.javanative.QueuingSocketListener;
import de.yard.threed.javanative.SocketEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * A traditional socket.
 * QueuingSocketListener runs a separate thread that blocks for reading from a socket.
 */
@Slf4j
public class ServerUnixSocket implements NativeSocket {
    private static final Logger logger = LoggerFactory.getLogger(ServerUnixSocket.class.getName());

    BlockReader blockReader = new BlockReader();
    Socket clientSocket;
    QueuingSocketListener queuingSocketListener;
    SocketEndpoint endpoint;
    boolean terminateflag = false;
    boolean terminated = false;

    ServerUnixSocket(Socket clientSocket) throws IOException {
        logger.debug("Client connected via socket: Starting new ClientConnection");

        this.clientSocket = clientSocket;

        queuingSocketListener = new QueuingSocketListener(clientSocket);
        queuingSocketListener.start();

        endpoint = new SocketEndpoint(clientSocket);
    }

    @Override
    public void sendPacket(Packet packet) {
        endpoint.writePacket(packet.getData());
    }

    // @Override
    public Packet getPacket() {
        //log.debug("getPacket");
        List<String> s = queuingSocketListener.getPacket();
        return Packet.buildFromBlock(s);
    }

    //@Override
    public void close() {
        throw new RuntimeException("not yet");

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

    public boolean isClosed() {
        return queuingSocketListener.isTerminated();
    }

    public void closed() {
        log.debug("Closed");

        //TODO?? socket.close();

    }

    public void addLine(String line) {
        blockReader.add(line);
    }
}
