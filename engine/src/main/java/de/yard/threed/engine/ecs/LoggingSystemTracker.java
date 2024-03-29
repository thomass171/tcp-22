package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.Packet;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.platform.common.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation doing nothing.
 */
public class LoggingSystemTracker implements SystemTracker {

    private String latestMessage = "";
    private List<Packet> fromNetwork = new ArrayList<Packet>();
    private List<Packet> toNetwork = new ArrayList<Packet>();
    private List<Event> eventsProcessed = new ArrayList<Event>();
    public List<Request> requestsPut = new ArrayList<Request>();
    private Map<String, Integer> tags = new HashMap<String, Integer>();
    private List<String> modelsBuilt = new ArrayList<String>();

    @Override
    public void eventsSentToClients() {

    }

    @Override
    public void packetReceivedFromNetwork(Packet packet) {
        fromNetwork.add(packet);
    }

    @Override
    public void packetSentToNetwork(Packet packet) {
        toNetwork.add(packet);
    }

    @Override
    public void eventProcessed(Event evt) {
        eventsProcessed.add(evt);
    }

    @Override
    public void report() {
        String msg = "" + fromNetwork.size() + " packets from network, " + toNetwork.size() + " to network";
        if (!msg.equals(latestMessage)) {
            Platform.getInstance().getLog(LoggingSystemTracker.class).info(msg);
            latestMessage = msg;
        }
    }

    @Override
    public void requestPut(Request request) {
        requestsPut.add(request);
    }

    @Override
    public List<Request> getRequests() {
        return requestsPut;
    }

    public void tag() {
        long current = Platform.getInstance().currentTimeMillis();
        tags.put("eventsProcessed", eventsProcessed.size());
    }

    public List<Event> getLatestEventsProcessed() {
        if (tags.get("eventsProcessed") == null) {
            // without tag just return all
            return eventsProcessed;
        }
        return eventsProcessed.subList((int)tags.get("eventsProcessed"), eventsProcessed.size());
    }

    @Override
    public List<Event> getEventsProcessed() {
        return eventsProcessed;
    }

    @Override
    public List<Packet> getPacketsReceivedFromNetwork() {
        return fromNetwork;
    }

    public List<Packet> getPacketsSentToNetwork() {
        return toNetwork;
    }

    @Override
    public void modelBuilt(String fullName) {
        modelsBuilt.add(fullName);
    }

    @Override
    public List<String> getModelsBuilt() {
        return modelsBuilt;
    }

}
