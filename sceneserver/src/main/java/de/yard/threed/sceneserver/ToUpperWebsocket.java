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

@ServerEndpoint("/toUpper")
@Slf4j
public class ToUpperWebsocket {

    @OnOpen
    public void onOpen(Session session) {
        log.debug(String.format("WebSocket opened: %s", session.getId()));
    }

    @OnMessage
    public void onMessage(String txt, Session session) throws IOException {
        log.debug(String.format("Message received: %s", txt));
        session.getBasicRemote().sendText(txt.toUpperCase());
    }

    @OnClose
    public void onClose(CloseReason reason, Session session) {
        log.debug(String.format("Closing a WebSocket (%s) due to %s", session.getId(), reason.getReasonPhrase()));
    }

    @OnError
    public void onError(Session session, Throwable t) {
        log.error(String.format("Error in WebSocket session %s%n", session == null ? "null" : session.getId()), t);
    }
}

