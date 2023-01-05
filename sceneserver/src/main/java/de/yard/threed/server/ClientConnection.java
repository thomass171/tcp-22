package de.yard.threed.server;

import de.yard.threed.javanative.QueuingSocketListener;
import de.yard.threed.javanative.SocketEndpoint;
import de.yard.threed.core.Packet;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class ClientConnection /*extends Thread */ {
    private static final Logger logger = LoggerFactory.getLogger(de.yard.threed.server.ClientConnection.class.getName());
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

        endpoint=new SocketEndpoint(clientSocket);
    }

    /*public void run() {
        logger.debug("Client connected: Starting new ClientConnection");

        String s = null;
        try {
            while (!terminateflag) {

                //logger.debug("");

                //out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine;//, outputLine;

                while ((inputLine = in.readLine()) != null) {
                    logger.debug("inputline=" + inputLine);
                    //outputLine = "w";
                    //out.println(outputLine);
                }

            }
        } catch (IOException e) {
            if (terminateflag) {
                logger.debug("Expected exception due to closed socket due to terminate");
            } else
                e.printStackTrace();
        }
        IOUtils.closeQuietly(clientSocket);
        logger.debug("Closing clientConnection");
    }*/

    /**
     *
     */
    /* synchronized */
    protected void sendEvent(int var, int value) {
        logger.debug("sendEvent ");
        // try {
        //Keinen neuen writer anlegen, denn den kann man dann nicht schliessen, ohne den socket zu schliessen
        //PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        //out.printf("%s:%d=%d:\r\n", IocpConnection.responseheader, var, value);
        //out.close();
        logger.debug("Event sent ");
        /*} catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * TODO sync?
     * @param packet
     */
    public void writePacket(List<String> packet) {
        endpoint.writePacket(packet);
    }

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
    public Packet getPacket() {
        List<String> s = queuingSocketListener.getPacket();
        return Packet.buildFromBlock(s);
    }

    public boolean hasPacket() {
       return queuingSocketListener.hasPacket();
    }
}


