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

    public static String KEY_NAME = "name";
    public static String KEY_POSITION = "position";
    public static String KEY_ROTATION = "rotation";
    public static String KEY_SCALE = "scale";

    // by index
    @Deprecated
    public Object[] o = null;
    // by name. TODO use string instead of object. How should type be found?
    public Map<String, Object> values = null;

    public Payload() {
        values = new HashMap<String, Object>();
    }

    public Payload(Map<String, Object> values) {
        this.values = values;
    }

    @Deprecated
    public Payload(Object[] payload) {
        o = payload;
    }

    @Deprecated
    public Payload(Object payload0, Object payload1) {
        o = new Object[]{payload0, payload1};
    }

    @Deprecated
    public Payload(Object payload0, Object payload1, Object payload2) {
        o = new Object[]{payload0, payload1, payload2};
    }

    @Deprecated
    public Payload(Object payload0, Object payload1, Object payload2, Object payload3) {
        o = new Object[]{payload0, payload1, payload2, payload3};
    }

    @Deprecated
    public Payload(Object payload0, Object payload1, Object payload2, Object payload3, Object payload4) {
        o = new Object[]{payload0, payload1, payload2, payload3, payload4};
    }

    @Deprecated
    public Payload(List<Object> objs) {
        o = (Object[]) objs.toArray(new Object[0]);
    }

    public Payload add(String key, String value) {
        // in the packet there will be no way to differ an empty string and null. So don't add null values to the packet at all.
        if (value != null) {
            values.put(key, value);
        }
        return this;
    }

    public Payload add(String key, int value) {
        values.put(key, Integer.valueOf(value));
        return this;
    }

    public Payload add(String key, Vector3 value) {
        values.put(key, value);
        return this;
    }

    public Payload addName(String value) {
        values.put(KEY_NAME, value);
        return this;
    }

    public Payload addPosition(Vector3 value) {
        values.put(KEY_POSITION, value);
        return this;
    }

    public Payload addScale(Vector3 value) {
        values.put(KEY_SCALE, value);
        return this;
    }

    public Payload add(String key, Quaternion value) {
        values.put(key, value);
        return this;
    }

    public Payload addRotation(Quaternion value) {
        values.put(KEY_ROTATION, value);
        return this;
    }

    /*public boolean isByIndex() {
        return o != null;
    }

    public int size() {

        return o.length;
    }*/

    @Deprecated
    public Object get(int index) {
        if (o == null) {
            throw new RuntimeException("no index based payload");
        }
        return o[index];
    }

    @Deprecated
    public Object get(String name) {
        if (values == null) {
            throw new RuntimeException("no name based payload");
        }
        return values.get(name);
    }

    public <T> T get(String name, ObjectBuilder<T> objectBuilder) {
        if (values == null) {
            throw new RuntimeException("no name based payload");
        }
        String value = (String) values.get(name);
        if (value == null) {
            return null;
        }
        return objectBuilder.buildFromString(value);
    }

    public int getAsInt(String name) {
        if (values == null) {
            throw new RuntimeException("no name based payload");
        }
        return Util.atoi((String) values.get(name));
    }

    public String getName() {
        return (String) get(KEY_NAME);
    }

    public Vector3 getPosition() {
        return (Vector3) get(KEY_POSITION);
    }

    public Quaternion getRotation() {
        return (Quaternion) get(KEY_ROTATION);
    }

    public Vector3 getScale() {
        return (Vector3) get(KEY_SCALE);
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
        } else if (o instanceof Vector3) {
            return ("v:" + ((Vector3) o).toSimpleString());
        } else if (o instanceof Quaternion) {
            return ("q:" + ((Quaternion) o).toSimpleString());
        } else {
            Platform.getInstance().getLog(Payload.class).warn("unknown payload class " + o/*C#.getClass().getName()*/);
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
                    String[] parts = StringUtils.split(s, "=");
                    m.put(StringUtils.substring(parts[0], 2), decodeObject(parts[1]));
                }
            }
            payload = new Payload(m);
        }
        return payload;
    }

    private static Object decodeObject(String s_p) {

        if (StringUtils.length(s_p) == 0) {
            // empty string will start with 's:'
            Platform.getInstance().getLog(Payload.class).warn("null payload class ");
            return null;
        }

        String content = StringUtils.substring(s_p, 2);
        switch (StringUtils.charAt(s_p, 0)) {
            case 's':
                return content;
            case 'i':
                return (Integer.valueOf(Util.atoi(content)));
            case 'b':
                return (Boolean.valueOf(StringUtils.toLowerCase(content).equals("true")));
            case 'v':
                return Util.parseVector3(content);
            case 'q':
                return Util.parseQuaternion(content);
            default:
                throw new RuntimeException("invalid content in packet: " + s_p);
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
