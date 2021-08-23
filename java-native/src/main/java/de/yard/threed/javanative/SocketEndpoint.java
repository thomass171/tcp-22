package de.yard.threed.javanative;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class SocketEndpoint {
    PrintWriter out;

    public SocketEndpoint(Socket socket) throws IOException {
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void writePacket(List<String> packet) {
        for (String s : packet) {
            out.println(s);
        }
        // empty line as delimiter
        out.println();
    }

}
