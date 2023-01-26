package de.yard.threed.core;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Payload is either 'by index' or 'by name'
 * 17.1.23: Payload needs to be ready to be transferred over network (serialized). So it should not contain complex objects (possibly containing references).
 * <p>
 * 26.3.19
 */
public class Payload {

    // by index
    public Object[] o = null;
    // by name
    public Map<String, Object> values = null;

    public Payload(Map<String, Object> values) {
        this.values = values;
    }

    public Payload(Object[] payload) {
        o = payload;
    }

    public Payload(Object payload0, Object payload1) {
        o = new Object[]{payload0, payload1};
    }

    public Payload(Object payload0, Object payload1, Object payload2) {
        o = new Object[]{payload0, payload1, payload2};
    }

    public Payload(Object payload0, Object payload1, Object payload2, Object payload3) {
        o = new Object[]{payload0, payload1, payload2, payload3};
    }

    public Payload(Object payload0, Object payload1, Object payload2, Object payload3, Object payload4) {
        o = new Object[]{payload0, payload1, payload2, payload3, payload4};
    }

    public Payload(List<Object> objs) {
        o = (Object[]) objs.toArray(new Object[0]);
    }

    /*public boolean isByIndex() {
        return o != null;
    }

    public int size() {

        return o.length;
    }*/

    public Object get(int index) {
        if (o == null) {
            throw new RuntimeException("no index based payload");
        }
        return o[index];
    }

    public Object get(String name) {
        if (values == null) {
            throw new RuntimeException("no name based payload");
        }
        return values.get(name);
    }

    public void encode(Packet packet) {
        if (o != null) {
            for (int i = 0; i < o.length; i++) {
                //Object o = payload.get(i);
                packet.add("p" + i, encodeObject(o[i]));
            }
        } else {
            for (String key : values.keySet()) {
                packet.add("p_" + key, encodeObject(values.get(key)));
            }
        }
    }

    private String encodeObject(Object o) {
        if (o == null) {
            Platform.getInstance().getLog(Payload.class).warn("null payload class ");
            return null;
        } else if (o instanceof String) {
            return ("s:" + (String) o);
        } else if (o instanceof Integer) {
            return ("i:" + ((Integer) o));
        } else if (o instanceof Boolean) {
            return ("b:" + ((Boolean) o));
        } else {
            Platform.getInstance().getLog(Payload.class).warn("unknown payload class " + o.getClass().getName());
            return ("");
        }
    }

    public static Payload decode(Packet packet) {

        Payload payload;

        if (packet.isByIndex()) {
            List<Object> objs = new ArrayList<Object>();
            for (int i = 0; i < 1000; i++) {
                String s_p = packet.getValue("p" + i);
                if (s_p == null) {
                    break;
                }
                objs.add(decodeObject(s_p));
            }
            payload = new Payload(objs);
        } else {
            Map<String, Object> m = new HashMap<String, Object>();
            for (String s : packet.getData()) {
                if (StringUtils.startsWith(s, "p_")) {
                    String[] parts = s.split("=");
                    m.put(StringUtils.substring(parts[0], 2), decodeObject(parts[1]));
                }
            }
            payload = new Payload(m);
        }
        return payload;
    }

    private static Object decodeObject(String s_p) {

        if (s_p.length() == 0) {
            // empty string will start with 's:'
            Platform.getInstance().getLog(Payload.class).warn("null payload class ");
            return null;
        }

        switch (StringUtils.charAt(s_p, 0)) {
            case 's':
                return (StringUtils.substring(s_p, 2));
            case 'i':
                return (new Integer(Util.atoi(StringUtils.substring(s_p, 2))));
            case 'b':
                return (new Boolean(StringUtils.toLowerCase(StringUtils.substring(s_p, 2)).equals("true")));
            default:
                throw new RuntimeException("invalid content in packet");
        }
    }

    @Override
    public String toString() {
        String s = "";
        if (o != null) {
            for (Object obj : o) {
                s += obj + ",";
            }
        }
        if (values != null) {
            s += values;
        }
        return s;
    }

}
