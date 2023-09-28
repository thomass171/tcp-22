package de.yard.threed.engine.platform.common;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.HashMap;
import java.util.Map;

/**
 * Classification of all types of requests. Also for Button/Menu/Click.
 * <p>
 * 21.3.19: Range limits Request/Event.
 * Range of types:
 * 1000-1999 engine
 * 2000-2999 maze
 * 3000-3999 graph
 * 4000-4999 traffic
 * <p>
 */
public class RequestType {
    static Log logger = Platform.getInstance().getLog(RequestType.class);

    public int type;
    String label = "";
    private static Map<Integer, RequestType> registry = new HashMap<Integer, RequestType>();

    private RequestType(int uniquetype) {
        this.type = uniquetype;
        if (registry.containsKey(uniquetype)) {
            throw new RuntimeException("Duplicate request id type " + uniquetype);
        }
        registry.put(this.type, this);
    }

    private RequestType(int uniquetype, String label) {
        this(uniquetype);
        this.label = label;
    }

    @Override
    public boolean equals(Object evt) {
        return ((RequestType) evt).type == type;
    }

    public final int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "" + type + "(" + label + ")";
    }

    public String getLabel() {
        return label;
    }

    public static RequestType findById(int type) {
        RequestType requestType = registry.get(type);
        if (requestType == null) {
            logger.warn("RequestType not found:" + type);
        }
        return requestType;
    }

    public static RequestType register(int type, String label) {
        RequestType requestType = registry.get(type);
        if (requestType == null) {
            return new RequestType(type, label);
        }
        if (!requestType.getLabel().equals(label)) {
            throw new RuntimeException("inconsistent RequestType " + requestType + "!=" + label);
        }
        return requestType;
    }
}
