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
    private volatile boolean terminateflag = false;
    private volatile boolean terminated = false;
    private volatile boolean aborted = false;
    java.net.ServerSocket serverSocket;
    private List<ClientConnection> clientConnectionList = new ArrayList();
    private static ClientListener instance;

    private ClientListener(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void init(String host, int port) {
        if (instance == null) {
            instance = new ClientListener(host, port);
        } else {
            throw new RuntimeException(" ClientListener already inited");
        }
    }

    public static ClientListener getInstance() {
        if (instance == null) {
            throw new RuntimeException("no instance of ClientListener");
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

                clientConnectionList.add(new ClientConnection(new ServerUnixSocket(clientSocket)));

            }
        } catch (Exception e) {
            if (terminateflag) {
                logger.debug("Expected exception due to closed socket due to terminate");
                terminated = true;
            } else
                e.printStackTrace();
            aborted = true;
        } finally {
        }

        if (terminated) {
            logger.info("terminated");
        }
        if (aborted) {
            logger.info("aborted");
        }
        logger.info("Thread ended");
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

    /**
     * TODO sync
     * @return
     */
    public List<ClientConnection> getClientConnections() {
        return clientConnectionList;
    }

    public void addConnection(ClientConnection clientConnection){
        this.clientConnectionList.add(clientConnection);
    }

    private void waitForTerminate() {

        int cnt = 0;

        while (!terminated) {
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
     * Needs optimization. TODO and sync
     */
    public List<NativeSocket> getSockets() {

        List<NativeSocket> sockets = new ArrayList<NativeSocket>();
        for (ClientConnection connection : getClientConnections()) {
            sockets.add(connection.getSocket());
        }
        return sockets;
    }

    public ClientConnection discardClosedConnection() {

        Iterator<ClientConnection> iter = clientConnectionList.iterator();
        while (iter.hasNext()) {
            // TODO risk of ava.util.ConcurrentModificationException. Occured once
            ClientConnection cc = iter.next();
            if (cc.isClosed()) {
                logger.debug("Discarding closed connection");
                clientConnectionList.remove(cc);
                return cc;
            }
        }
        return null;
    }

    public void checkLiveness() {
        if (terminated) {
            throw new RuntimeException("ClientListener terminated");
        }
        if (aborted) {
            throw new RuntimeException("ClientListener aborted");
        }
    }
}
