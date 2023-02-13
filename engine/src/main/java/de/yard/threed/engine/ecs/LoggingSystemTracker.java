package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.Packet;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation doing nothing.
 */
public class LoggingSystemTracker implements SystemTracker {

    private String latestMessage = "";
    private List<Packet> fromClient = new ArrayList<Packet>();
    private List<Packet> fromServer = new ArrayList<Packet>();
    private List<Event> eventsProcessed = new ArrayList<Event>();
    private Map<String, Integer> tags = new HashMap<String, Integer>();

    @Override
    public void eventsSentToClients() {

    }

    @Override
    public void packetReceivedFromClient(Packet packet) {
        fromClient.add(packet);
    }

    @Override
    public void packetReceivedFromServer(Packet packet) {
        fromServer.add(packet);
    }

    @Override
    public void eventProcessed(Event evt) {
        eventsProcessed.add(evt);
    }

    @Override
    public void report() {
        String msg = "" + fromClient.size() + " packets from client, " + fromServer.size() + " from server";
        if (!msg.equals(latestMessage)) {
            Platform.getInstance().getLog(LoggingSystemTracker.class).info(msg);
            latestMessage = msg;
        }
    }

    public void tag() {
        long current = Platform.getInstance().currentTimeMillis();
        tags.put("eventsProcessed", eventsProcessed.size());
    }

    public List<Event> getLatestEventsProcessed() {
        return eventsProcessed.subList(tags.get("eventsProcessed"), eventsProcessed.size());
    }

    public List<Event> getEventsProcessed() {
        return eventsProcessed;
    }

    public List<Packet> getPacketsReceivedFromServer() {
        return fromServer;
    }

}
