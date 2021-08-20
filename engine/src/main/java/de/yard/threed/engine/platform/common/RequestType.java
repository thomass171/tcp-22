package de.yard.threed.engine.platform.common;

/**
 * Klassifierung von verschiedenen Requests. Auch für Button/Menu/Click.
 * <p>
 * 21.3.19: Abgrenzung Request/Event.
 * <p>
 */
public class RequestType {
    public int type;
    String label = "";
    // 477 willkuerlich
    //16.2.21 eigentlich doch unbrauchbar mit MP
    private static int uniquetype = 477;

    public RequestType() {
        this.type = uniquetype++;
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
}
