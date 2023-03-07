package de.yard.threed.javacommon;

import de.yard.threed.core.BlockReader;

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
        BlockReader.writePacket(packet, text -> out.println(text));
    }
}
