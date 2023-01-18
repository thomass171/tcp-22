package de.yard.threed.engine.platform.common;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.HashMap;
import java.util.Map;

/**
 * Klassifierung von verschiedenen Requests. Auch für Button/Menu/Click.
 * <p>
 * 21.3.19: Abgrenzung Request/Event.
 * <p>
 */
public class RequestType {
    static Log logger = Platform.getInstance().getLog(RequestType.class);

    public int type;
    String label = "";
    // 477 willkuerlich
    //16.2.21 eigentlich doch unbrauchbar mit MP
    private static int uniquetype = 477;
    private static Map<Integer, RequestType> registry = new HashMap<Integer,RequestType>();

    private RequestType() {
        this.type = uniquetype++;
        registry.put(this.type,this);
    }

    public RequestType(String label) {
        this();
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
        return "" + type+"("+label+")";
    }

    public String getLabel() {
        return label;
    }

    public static RequestType findById(int type){
        RequestType requestType = registry.get(type);
        if (requestType==null){
            logger.warn("RequestType not found:" + type);
        }
        return requestType;
    }
}
