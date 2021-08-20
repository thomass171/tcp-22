package de.yard.threed.core;




/**
 * Klassifierung von verschiedenen Events. Heisst nicht "id", weil das zu sehr auf ein konkretes Event hindeutet.
 * <p>
 * Das ist auch die zentrale Registratur fuer verfuegbare Eventtypen.
 * 7.5.19: Es gibt aber noch die neuere {@link EventRegistry}. Viele EventTypes werden wird nur noch deprecated verwendet.
 *
 * <p>
 * Created by thomass on 27.12.16.
 */
public class EventType {
    public int type;
    String label = "";
    // 477 willkuerlich
    private static int uniquetype = 477;
    public static EventType MODELLOAD = new EventType();
    //Fuer FG model zu kompley aufzudroeseln public static EventType XMLMODELLOADED = new EventType();
    // Das Model ist dann DVK konform direkt auch in der Scene.
    public static EventType MODELLOADED = new EventType();
    public static EventType EVENT_NODECREATED = new EventType();
    public static EventType EVENT_NODEPARENTCHANGED = new EventType();
    public static EventType EVENT_NODECHANGED = new EventType();
    public static EventType EVENT_MATLIBCREATED = new EventType();


    public EventType() {
        this.type = uniquetype++;
    }

    public EventType(String label) {
        this();
        this.label = label;
    }

    @Override
    public boolean equals(Object evt) {
        return ((EventType) evt).type == type;
    }

    public final int getType() {
        return type;
    }

    public final String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "" + type+"("+label+")";
    }
}
