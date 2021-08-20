package de.yard.threed.engine.platform.common;


import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.*;

/**
 * Aehnlich einer Eventqueue. Wird von Apps und SystemManager verwendet.
 * Nicht MT faehig.
 * 21.3.19
 */
public class RequestQueue {
    static Log logger = Platform.getInstance().getLog(RequestQueue.class);

    private List<Request> requests = new ArrayList<Request>();
    private Map<RequestType, List<RequestHandler>> requesthandler = new HashMap<RequestType, List<RequestHandler>>();

    public void process() {
        // Jeder Request nur einmal.
        List<Request> processedrequests = new ArrayList<Request>();
        //during processing new requests might be created (eg. "startFlight"). So clone. iterator doesn't help. TODO improve
        ArrayList<Request> tmpRequests = new ArrayList<Request>();
        tmpRequests.addAll(requests);
        for (Request request : tmpRequests) {

            //for (Iterator<Request> iterator = requests.iterator(); iterator.hasNext(); ) {
            //Request request = iterator.next();
            List<RequestHandler> handler = requesthandler.get(request.getType());
            if (handler != null) {
                for (RequestHandler ebs : handler) {
                    if (!processedrequests.contains(request)) {
                        if (ebs.processRequest(request)) {
                            processedrequests.add(request);
                        } else {
                            request.declined++;
                        }
                    }
                }
            }
        }
        requests.removeAll(processedrequests);
    }

    public void addHandler(RequestType evt, RequestHandler system) {
        List<RequestHandler> l = requesthandler.get(evt);
        if (l == null) {
            l = new ArrayList<RequestHandler>();
            requesthandler.put(evt, l);
        }
        l.add(system);
    }

    public void addRequest(Request request) {
        if (request == null) {
            logger.warn("Ignoring null request");
            return;
        }
        requests.add(request);
    }

    public int getRequestCount() {
        return requests.size();
    }

    public void clear() {
        requests.clear();
    }

    public void reset() {
        requests.clear();
        requesthandler.clear();
    }
}
