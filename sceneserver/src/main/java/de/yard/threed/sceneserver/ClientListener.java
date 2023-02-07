package de.yard.threed.sceneserver;

import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.engine.ecs.DefaultBusConnector;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static de.yard.threed.javanative.JavaUtil.sleepMs;

/**
 * Client Listener and Registry.
 *
 * und
 * Konvertierung zwischen ECS Events/Requests und einem per Socket angebundenem Client.
 * <p>
 * Kann nicht ins ClientSystem wegen GWT/C# bzw. listen().
 * <p>
 * Ist ein eigener Thread. Auch weil es mehrere geben kann.(??) Mal schaun, die kommen alle hier rein.
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

    public static void dropInstance() {
        if (instance != null) {
            instance.terminate();
            instance.waitForTerminate();
            instance = null;
        }

    }

    /*16.2.21 das geht ja wohl nicht synchronized */
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
     * dropInstance() should be used.
     */
    /*synchronized*/
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
       /* try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
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
     * Needs optimization.
     */
    public List<NativeSocket> getSockets() {

        List<NativeSocket> sockets = new ArrayList<NativeSocket>();
        for (ClientConnection connection:getClientConnections()){
            sockets.add(connection);
        }
        return sockets;
    }
}
