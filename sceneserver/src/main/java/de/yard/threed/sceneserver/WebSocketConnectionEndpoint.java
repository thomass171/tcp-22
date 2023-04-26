package de.yard.threed.sceneserver;

import lombok.extern.slf4j.Slf4j;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

@ServerEndpoint("/connect")
@Slf4j
public class WebSocketConnectionEndpoint {

    Map<String,ServerWebSocket> connections = new TreeMap<>();

    @OnOpen
    public void onOpen(Session session) {
        log.debug("session {} opened", session.getId());
        ServerWebSocket socket = new ServerWebSocket(session);
        ClientConnection connection = new ClientConnection(socket);
        connections.put(session.getId(),socket);
        ClientListener.getInstance().addConnectionFromWebsocket(connection);
    }

    @OnMessage
    public void onMessage(String txt, Session session) throws IOException {
        log.debug("Message received: {}", txt);
        //session.getBasicRemote().sendText(txt.toUpperCase());
        connections.get(session.getId()).addLine(txt);
    }

    @OnClose
    public void onClose(CloseReason reason, Session session) {
        log.debug(String.format("Closing a WebSocket (%s) due to %s", session.getId(), reason.getReasonPhrase()));
        connections.get(session.getId()).closed();
    }

    @OnError
    public void onError(Session session, Throwable t) {
        log.error(String.format("Error in WebSocket session %s%n", session == null ? "null" : session.getId()), t);
        //TODO close??
    }
}

