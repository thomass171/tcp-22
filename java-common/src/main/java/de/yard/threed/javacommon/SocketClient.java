package de.yard.threed.javacommon;

import de.yard.threed.core.WriteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

/**
 * A wrapper for a network socket for transferring text blocks with a queued (threaded) listener.
 * <p>
 * Not on byte level but UTF-8 text block level with empty line as separator (like mail)
 * <p>
 */
public class SocketClient {
    static Logger logger = LoggerFactory.getLogger(SocketClient.class.getName());

    Socket socket;
    BufferedReader in;
    String host;
    int port;
    QueuingSocketListener queuingSocketListener;
    SocketEndpoint endpoint;
    // name for helping debugging und logging
    private String id = "clientsocket" + System.currentTimeMillis();

    public SocketClient(String host, int port) {

        this.host = host;
        this.port = port;
        socket = new Socket();
        //??sock.setSoTimeout(10000);
    }

    /**
     * connect() might block, so do it async?
     * Should not be done async because of possible race condition when sending data.
     */
    public void connect() throws IOException {
        logger.debug("Connecting to " + host + " on port " + port);

        socket.connect(new InetSocketAddress(host, port), 3000);
        endpoint = new SocketEndpoint(socket);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    }

    public QueuingSocketListener startListen() {
        if (in == null) {
            throw new RuntimeException("not connected");
        }
        queuingSocketListener = new QueuingSocketListener(in, id);
        queuingSocketListener.start();
        return queuingSocketListener;
    }

    public void writePacket(List<String> packet) throws WriteException {
        endpoint.writePacket(packet);
    }

    public void close() {
        logger.debug("closing socket with id " + id);
        try {
            //not working/possible in.close();
            queuingSocketListener.terminate();
            socket.close();
            while (!queuingSocketListener.isTerminated()) {
                logger.debug("Waiting for socket listener to terminate");
                Thread.sleep(100);
            }
            logger.debug("Socket closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
