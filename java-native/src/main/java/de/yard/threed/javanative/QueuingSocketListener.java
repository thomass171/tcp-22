package de.yard.threed.javanative;

import org.apache.log4j.Logger;

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
 * Multithreaded und auf die MP Textbloecke ausgelegt.
 * <p>
 * Not on byte level but UTF-8 text block level with empty line as separator (like mail)
 * <p>
 * Die gelesenenen Daten kommen in eine Queue, aus die dann ein nicht multithreaded thread die Daten abrufen kann.
 */
public class QueuingSocketListener extends Thread {
    static Logger logger = Logger.getLogger(QueuingSocketListener.class.getName());

    BufferedReader in;
    List<String> lines = new Vector<>();
    List<List<String>> blocks = new Vector<>();

    public QueuingSocketListener(BufferedReader in) {

        //  this.socket = socket;
        // in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.in = in;
    }

    public QueuingSocketListener(Socket socket) throws IOException {

        //  this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    }

    @Override
    public void run() {
        startListen();
    }

    private void startListen() {
        boolean terminateflag = false;

        try {
            //16.2.21 connect();

            while (!terminateflag) {

                //logger.debug("");

                //out = new PrintWriter(clientSocket.getOutputStream(), true);
                //BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine;//, outputLine;

                while ((inputLine = in.readLine()) != null) {
                    logger.debug("inputline=" + inputLine);
                    if (inputLine.length() == 0) {
                        logger.debug("found block end");
                        blocks.add(lines);
                        lines = new ArrayList<>();
                    } else {
                        lines.add(inputLine);
                    }
                }

            }
        } catch (SocketException e) {
            logger.debug("SocketException. Stopping thread:"+e.getMessage());
            return;
        } catch (IOException e) {
            logger.error("IOException. Stopping thread.", e);
            return;
        }

    }


    /**
     * blockierendes Lesen.
     */
    private String readStringByDelimiter() throws IOException {
        String line = in.readLine();
        logger.debug("read line:" + line);
        return line;
    }

    /**
     * Threadsafe getting of incoming data.
     *
     * @return
     */

    public List<String> getPacket() {
        List<String> block = new ArrayList<>();

        if (blocks.size() == 0) {
            return null;
        }
        //TODO more than Vector needed to make MT safe?
        block = blocks.remove(0);

        return block;
    }

    public boolean hasPacket() {
        return blocks.size()>0;
    }
}
