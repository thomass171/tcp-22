package de.yard.threed.sceneserver;

import de.yard.threed.core.Packet;
import de.yard.threed.core.WriteException;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.core.BlockReader;
import lombok.extern.slf4j.Slf4j;


import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.util.List;

/**
 * Websocket endpoint in the server.
 */
@Slf4j
public class ServerWebSocket implements NativeSocket {

    Session session;
    BlockReader blockReader = new BlockReader();

    /**
     * @param session
     */
    public ServerWebSocket(Session session) {
        this.session = session;
    }

    @Override
    public void sendPacket(Packet packet) throws WriteException {
        if (session == null) {
            log.warn("Ignoring send to closed socket");
            throw new WriteException("websocket was closed");
        }
        // getBasicRemote() cannot be used from other thread (this one), so use getAsyncRemote()
        RemoteEndpoint.Async async = session.getAsyncRemote();
        BlockReader.writePacket(packet.getData(), text -> async.sendText(text));
    }

    public boolean hasPacket() {
        return blockReader.hasBlock();
    }

    /**
     * Returns null if no packet is available.
     */
    public Packet getPacket() {
        //log.debug("getPacket");
        List<String> s = blockReader.pull();
        Packet packet = Packet.buildFromBlock(s);
        if (packet != null) {
            log.debug("Got packet " + packet);
        }
        return packet;
    }

    public void close() {
        // Effective close was already done (by jetty?)
        session = null;
    }

    @Override
    public boolean isPending() {
        // not sure false is correct.
        return false;
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
