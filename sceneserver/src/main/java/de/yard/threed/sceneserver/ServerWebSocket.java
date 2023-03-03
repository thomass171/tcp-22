package de.yard.threed.sceneserver;

import de.yard.threed.core.Packet;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.javanative.BlockReader;
import lombok.extern.slf4j.Slf4j;


import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.util.List;

@Slf4j
public class ServerWebSocket implements NativeSocket {

    Session session;
    BlockReader blockReader = new BlockReader();

    /**
     *
     *
     * @param session
     */
    public ServerWebSocket(Session session) {
        this.session = session;
    }

    @Override
    public void sendPacket(Packet packet) {
        if (session == null) {
            log.warn("Ignoring send to closed socket");
            return;
        }
        // getBasicRemote() cannot be used from other thread (this one), so use getAsyncRemote()
        RemoteEndpoint.Async async = session.getAsyncRemote();
        BlockReader.writePacket(packet.getData(), text -> async.sendText(text));
    }

    public boolean hasPacket() {
        return blockReader.hasBlock();
    }

    public Packet getPacket() {
        //log.debug("getPacket");
        List<String> s = blockReader.pull();
        Packet packet = Packet.buildFromBlock(s);
        log.debug("Got packet " + packet);
        return packet;
    }

    public void close() {
        // Effective close was already done (by jetty?)
        session = null;
    }

    public boolean isClosed() {
        return session == null;
    }

    public void closed() {
        log.debug("Closed");

        //TODO?? socket.close();
        session = null;
    }

    public void addLine(String line) {
        blockReader.add(line);
    }
}
