package de.yard.threed.sceneserver.testutils;

import de.yard.threed.core.platform.NativeSocket;
import lombok.extern.slf4j.Slf4j;

/**
 * A websocket client.
 */
@Slf4j
public class WSClient {

    public static NativeSocket connectToServer(String host, int port)  {
        String uri = "ws://" + host + ":" + port + "/connect";

        // same optimistic(?) exception catching like in  Platform.connectToServer()
        try {
            ClientWebSocket wsSocket=new ClientWebSocket(uri);
            return wsSocket;
        } catch (Exception e) {
            log.error("connect() failed:" + e.getMessage());
            return null;
        }
    }
}
