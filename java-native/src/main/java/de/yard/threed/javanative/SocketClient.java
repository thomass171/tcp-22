package de.yard.threed.javanative;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper for a network socket for transferring text blocks with a queued (threaded) listener.
 * <p>
 * Not on byte level but UTF-8 text block level with empty line as separator (like mail)
 * <p>
 *
 */
public class SocketClient {
    static Logger logger = Logger.getLogger(SocketClient.class.getName());

    Socket socket;
    BufferedReader in;
    String host;
    int port;
    QueuingSocketListener queuingSocketListener;
    SocketEndpoint endpoint;

    public SocketClient(String host, int port) {

        this.host = host;
        this.port = port;
        socket = new Socket();
    }

    /**
     * connect() might block, so do it async?
     * Should not be done async because of possible race condition when sending data.
     */
    public void connect() throws IOException {
        logger.debug("Connecting to " + host + ":" + port);

        socket.connect(new InetSocketAddress("", port), 3000);
        endpoint= new SocketEndpoint(socket);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    }

    public QueuingSocketListener startListen() {
        if (in == null){
            throw new RuntimeException("not connected");
        }
        queuingSocketListener = new QueuingSocketListener(in);
        queuingSocketListener.start();
        return queuingSocketListener;
    }

    public void writePacket(List<String> packet) {
        endpoint.writePacket(packet);
    }
}
