package de.yard.threed.core;

/**
 * Um nicht so viel Object benutzen zu muessen.
 * <p>
 * 26.3.19
 */
public class Payload {
    public Object[] o;

    public Payload(Object payload0) {
        o = new Object[]{payload0};
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

    @Override
    public String toString() {
        String s = "";
        for (Object obj : o) {
            s += obj + ",";
        }
        return s;
    }

}
