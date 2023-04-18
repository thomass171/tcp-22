package de.yard.threed.javacommon;

import de.yard.threed.core.BlockReader;
import de.yard.threed.core.WriteException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Don't use Javas PrintWriter here because it hides IOException and thus socket closed notifications.
 */
public class SocketEndpoint {
    OutputStream out;

    public SocketEndpoint(Socket socket) throws IOException {
        out = socket.getOutputStream();
    }

    public void writePacket(List<String> packet) throws WriteException {

        BlockReader.writePacket(packet, text -> {
            try {
                out.write(text.getBytes(StandardCharsets.UTF_8));
                out.write('\n');
                out.flush();
            } catch (IOException e) {
                throw new WriteException(e.getMessage());
            }
        });
    }
}
