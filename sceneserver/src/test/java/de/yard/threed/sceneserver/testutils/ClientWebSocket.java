package de.yard.threed.sceneserver.testutils;

import de.yard.threed.core.Packet;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.core.BlockReader;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
public class ClientWebSocket implements NativeSocket {

    public WebSocket webSocket;
    private BlockReader blockReader = new BlockReader();

    /**
     * @param uri
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public ClientWebSocket(String uri) throws ExecutionException, InterruptedException {

        WebSocketUpgradeHandler.Builder upgradeHandlerBuilder = new WebSocketUpgradeHandler.Builder();
        WebSocketUpgradeHandler wsHandler = upgradeHandlerBuilder.addWebSocketListener(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket websocket) {
                log.debug("onOpen");
            }

            @Override
            public void onTextFrame(String payload, boolean finalFragment, int rsv) {
                //log.debug("Got response {}", payload);
                blockReader.add(payload);
            }

            @Override
            public void onClose(WebSocket websocket, int code, String reason) {
                log.debug("onClose");
            }

            @Override
            public void onError(Throwable t) {

                //log.debug("onError", t);
                log.error("onError: " + t.getMessage());
            }
        }).build();

        webSocket = Dsl.asyncHttpClient()
                .prepareGet(uri)
                .addHeader("header_name", "header_value")
                .addQueryParam("key", "value")
                .setRequestTimeout(5000)
                .execute(wsHandler)
                .get();
    }

    @Override
    public void sendPacket(Packet packet) {
        for (String s : packet.getData()) {
            webSocket.sendTextFrame(s);
        }
        // empty line as delimiter
        webSocket.sendTextFrame("");
        log.debug("Sent packet");
    }

    @Override
    public Packet getPacket() {
        List<String> s = blockReader.pull();
        return Packet.buildFromBlock(s);
    }

    @Override
    public void close() {
        webSocket.sendCloseFrame();
        webSocket = null;
    }
}
