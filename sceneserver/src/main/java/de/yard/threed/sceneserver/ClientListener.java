package de.yard.threed.sceneserver;

import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.engine.ecs.DefaultBusConnector;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static de.yard.threed.javanative.JavaUtil.sleepMs;

/**
 * Client Listener and Registry (singleton instance).
 * Separate thread for listening for connecting clients.
 */
public class ClientListener extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ClientListener.class.getName());

    String host;
    int port;
    boolean terminateflag = false;
    private boolean terminated = false;
    java.net.ServerSocket serverSocket;
    List<ClientConnection> clientConnectionList = new ArrayList();
    private static ClientListener instance;

    private ClientListener(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private ClientListener(String host) {
        this(host, DefaultBusConnector.DEFAULT_PORT);
    }

    public static ClientListener getInstance(String host, int port) {
        if (instance == null) {
            instance = new ClientListener(host, (port == -1) ? DefaultBusConnector.DEFAULT_PORT : port);
        }
        return instance;
    }

    public static ClientListener getInstance() {
        if (instance == null) {
            throw new RuntimeException("no instance");
        }
        return instance;
    }

    /**
     * Only for testing.
     */
    public static void dropInstance() {
        if (instance != null) {
            instance.terminate();
            instance.waitForTerminate();
            instance = null;
        }
    }

    /**
     * Executor of separate thread.
     * Endless listen for clients.
     */
    public void run() {
        logger.debug("Starting");
        String s = null;
        try {
            serverSocket = new java.net.ServerSocket(port);

            while (!terminateflag) {

                // terminate will close socket and thus abort accept
                Socket clientSocket = serverSocket.accept();
                logger.debug("Client connected: ");

                clientConnectionList.add(new ClientConnection(clientSocket));

            }
        } catch (IOException e) {
            if (terminateflag) {
                logger.debug("Expected exception due to closed socket due to terminate");
            } else
                e.printStackTrace();
        } finally {

        }

        logger.info("terminated");
        terminated = true;
    }

    /**
     * dropInstance() should be used, so this is private for use by dropInstance()
     */
    private void terminate() {

        logger.debug("Setting terminateflag");
        terminateflag = true;
        if (serverSocket == null) {
            logger.debug("already terminated or not started?");
            terminated = true;
            return;
        }
        // closing socket will cause a SocketException in accept();
        IOUtils.closeQuietly(serverSocket);
        serverSocket = null;
    }

    public boolean terminated() {
        return terminated;
    }

    public List<ClientConnection> getClientConnections() {
        return clientConnectionList;
    }

    public void waitForTerminate() {

        int cnt = 0;

        while (!terminated()) {
            sleepMs(100);
            if (cnt++ > 50) {
                // dont wait more than 5 seconds
                throw new RuntimeException("ClientListener not terminated");
            }
        }
    }

    /**
     * Called each frame for publishing events/requests. But this is not a good location for checking for closed connections, because a close
     * event should be sent.
     * <p>
     * Needs optimization.
     */
    public List<NativeSocket> getSockets() {

        List<NativeSocket> sockets = new ArrayList<NativeSocket>();
        for (ClientConnection connection : getClientConnections()) {
            sockets.add(connection);
        }
        return sockets;
    }

    public ClientConnection discardClosedConnection() {

        Iterator<ClientConnection> iter = clientConnectionList.iterator();
        while (iter.hasNext()) {
            ClientConnection cc = iter.next();
            if (cc.isClosed()) {
                logger.debug("Discarding closed connection");
                clientConnectionList.remove(cc);
                return cc;
            }
        }
        return null;
    }
}
