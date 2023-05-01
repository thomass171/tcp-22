package de.yard.threed.sceneserver;

import de.yard.threed.core.Packet;
import de.yard.threed.core.Pair;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
    // Using Vector does not protect against ConcurrentModificationException. Using CopyOnWriteArrayList
    // will create a sub copy each time an iterator is used.
    private List<ClientConnection> clientConnectionList = new CopyOnWriteArrayList();
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
     * Endless listen for new clients connecting.
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

                addClientConnection(new ClientConnection(new ServerUnixSocket(clientSocket)));

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

    public void publishPacketToClients(Packet packet, String connectionId) {

        Iterator<ClientConnection> iter = clientConnectionList.iterator();
        while (iter.hasNext()) {
            ClientConnection cc = iter.next();
            if (connectionId == null || connectionId.equals(cc.getConnectionId())) {
                cc.sendPacket(packet);
            }
        }
    }

    public List<Pair<Packet, String>> getPacketsFromClients() {

        int cnt = 0;

        List<Pair<Packet, String>> result = new ArrayList<Pair<Packet, String>>();

        Iterator<ClientConnection> iter = clientConnectionList.iterator();
        while (iter.hasNext()) {

            ClientConnection cc = iter.next();
            Packet packet;

            while ((packet = cc.getPacket()) != null) {
                result.add(new Pair(packet, cc.getConnectionId()));
                cnt++;
            }
        }
        if (cnt > 0) {
            logger.debug("Read {} packets from {} clients", cnt, clientConnectionList.size());
        }
        return result;
    }

    public void addConnectionFromWebsocket(ClientConnection clientConnection) {
        addClientConnection(clientConnection);
    }

    public int getClientConnectionCount() {
        return clientConnectionList.size();
    }

    /**
     * Checks for closed connections and removes the first(!) found.
     * This is returned for further processing.
     *
     *
     * @return connectionId of closed and removed connection
     */
    public String discardClosedConnection() {

        Iterator<ClientConnection> iter = clientConnectionList.iterator();
        while (iter.hasNext()) {

            ClientConnection cc = iter.next();
            if (cc.isClosed()) {
                logger.debug("Discarding closed connection");
                clientConnectionList.remove(cc);
                return cc.getConnectionId();
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

    private void addClientConnection(ClientConnection clientConnection) {
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

}
