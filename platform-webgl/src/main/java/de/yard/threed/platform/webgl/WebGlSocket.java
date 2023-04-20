package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import de.yard.threed.core.BlockReader;
import de.yard.threed.core.LinePrinter;
import de.yard.threed.core.Packet;
import de.yard.threed.core.Util;
import de.yard.threed.core.WriteException;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.core.platform.Platform;

/**
 * Created by thomass on 06.03.23.
 */
public class WebGlSocket implements NativeSocket {
    static Log logger = Platform.getInstance().getLog(WebGlSocket.class);
    JavaScriptObject webSocket;
    BlockReader blockReader = new BlockReader();
    static final int STATE_WAITING_FOR_OPEN = 0;
    static final int STATE_OPEN = 1;
    int state = STATE_WAITING_FOR_OPEN;

    private WebGlSocket(String host, int port) {
        // protocol needs to be 'ws' or 'wss', depending on how JS was loaded.
        this.webSocket = buildWebSocket(Main.usesTLS ? "wss" : "ws", host, port, this);
        addListener(webSocket, this);
    }

    public static WebGlSocket buildSocket(String host, int port) {
        logger.debug("Building websocket to " + host + ":" + port);
        WebGlSocket webGlSocket = new WebGlSocket(host, port);
        return webGlSocket;
    }

    @Override
    public void sendPacket(Packet packet) throws WriteException {
        BlockReader.writePacket(packet.getData(), text -> sendMessage(webSocket, text));
    }

    @Override
    public Packet getPacket() {
        if (blockReader.hasBlock()) {
            return Packet.buildFromBlock(blockReader.pull());
        }
        return null;
    }

    @Override
    public void close() {
//        Util.notyet();
    }

    public boolean isPending() {
        logger.debug("isPending:state= " + state);
        return state < STATE_OPEN;
    }

    public void connected() {
        state = STATE_OPEN;
    }

    public void messageReceived(String s) {
        //logger.debug("Got message " + s);
        blockReader.add(s);
    }

    /**
     *
     */
    private static native JavaScriptObject buildWebSocket(String protocol, String host, int port, WebGlSocket instance)  /*-{
        var socket = new WebSocket(protocol + '://' + host + ':' + port + "/connect");
        console.log("socket created");
        socket.addEventListener('open', function (event) {
            console.log('socket opened');
            instance.@WebGlSocket::connected()();
        });
        return socket;
    }-*/;

    private static native JavaScriptObject addListener(JavaScriptObject websocket, WebGlSocket instance)  /*-{

        websocket.addEventListener('message', function (event) {
            //console.log('Message from server ', event.data);
            instance.@WebGlSocket::messageReceived(Ljava/lang/String;)(event.data);
        });

    }-*/;

    private static native JavaScriptObject sendMessage(JavaScriptObject websocket, String msg)  /*-{
        websocket.send(msg);
    }-*/;
}
