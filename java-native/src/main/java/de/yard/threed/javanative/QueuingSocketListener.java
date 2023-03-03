package de.yard.threed.javanative;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Multithreaded and focussing on text blocks used by scene server protocol.
 * <p>
 * Not on byte level but UTF-8 text block level with empty line as separator (like mail)
 * <p>
 * Data read are put into a queue, from where it is read by the main thread.
 */
public class QueuingSocketListener extends Thread {
    static Logger logger = LoggerFactory.getLogger(QueuingSocketListener.class.getName());

    BufferedReader in;
    BlockReader blockReader = new BlockReader();
    boolean debuglog = false;
    volatile private boolean terminated = false;
    volatile private boolean terminateflag = false;
    private String id;

    public QueuingSocketListener(BufferedReader in, String id) {

        this.in = in;
        this.id = id;
    }

    public QueuingSocketListener(Socket socket) throws IOException {

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        startListen();
    }

    /**
     * Blocking call. terminateflag might be ignored until input arrives?
     */
    private void startListen() {

        try {
            while (!terminateflag) {

                //logger.debug("");

                //out = new PrintWriter(clientSocket.getOutputStream(), true);
                //BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine;//, outputLine;

                while ((inputLine = in.readLine()) != null) {
                    if (debuglog) {
                        logger.debug("inputline=" + inputLine);
                    }
                    blockReader.add(inputLine);
                }

            }
        } catch (SocketException e) {
            // regular disconnect?
            // only log an error if it wasn't intended to terminate
            if (!terminateflag) {
                logger.debug(id + ": SocketException. Stopping thread (client closed connection?):" + e.getMessage());
            }
        } catch (IOException e) {
            // only log an error if it wasn't intended to terminate
            if (!terminateflag) {
                logger.error(id + ": IOException. Stopping thread.", e);
            }
        }
        terminated = true;
        logger.debug(id + " terminated=" + terminated);
    }

    /**
     * Threadsafe getting of incoming data.
     *
     * @return
     */
    public List<String> getPacket() {

        return blockReader.pull();
    }

    public boolean hasPacket() {
        return blockReader.hasBlock();
    }

    public void terminate() {
        terminateflag = true;
    }

    public boolean isTerminated() {
        //logger.debug(id + " terminated=" + terminated);
        return terminated;
    }
}
