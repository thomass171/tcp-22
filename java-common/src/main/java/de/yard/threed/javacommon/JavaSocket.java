package de.yard.threed.javacommon;

import de.yard.threed.core.Packet;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.javanative.QueuingSocketListener;
import de.yard.threed.javanative.SocketClient;

import java.io.IOException;

/**
 * Implementation for all Java platforms.
 */
public class JavaSocket implements NativeSocket {
    SocketClient socketClient;
    QueuingSocketListener queuingSocketListener;

    public JavaSocket(SocketClient socketClient) throws IOException {
        this.socketClient = socketClient;
        socketClient.connect();
        queuingSocketListener = socketClient.startListen();
    }

    @Override
    public void sendPacket(Packet packet) {
        socketClient.writePacket(packet.getData());
    }

    @Override
    public Packet getPacket() {
        return Packet.buildFromBlock(queuingSocketListener.getPacket());
    }

    @Override
    public void close() {
        socketClient.close();
    }

    public static JavaSocket build(String server, int port) {
        try {
            SocketClient queuingSocketClient = new SocketClient(server, port);
            return new JavaSocket(queuingSocketClient);
        } catch (IOException e) {
            Platform.getInstance().getLog(JavaSocket.class).error("connect() failed:" + e.getMessage());
            return null;
        }
    }
}
