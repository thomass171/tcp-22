package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.engine.platform.common.*;

/**
 * Evtl. Interface?
 * 27.12.16: Sollten eventhandler nicht disjunkt zu updater sein?
 * Created by thomass on 16.09.16.
 */
public abstract class EcsSystem implements RequestHandler {
    //Log logger = Platform.getInstance().getLog(EcsSystem.class);
    protected String groupId = null;
    //nur zur besseren Analyse/Debug
    public String name;
    EventType[] eventtype = null;
    RequestType[] requesttype = null;
    boolean updatepergroup = true;

    public EcsSystem(String[] comptags) {
        this.name = name;
        groupId = EcsGroup.registerGroup(comptags);
        updatepergroup = true;
        eventtype = null;
    }

    public EcsSystem(EventType[] eventtype) {
        this.eventtype = eventtype;
    }

    public EcsSystem(String[] comptags, EventType[] eventtype) {
        groupId = EcsGroup.registerGroup(comptags);
        updatepergroup = true;
        this.eventtype = eventtype;
    }

    public EcsSystem(String[] comptags, RequestType[] requesttype, EventType[] eventtype) {
        groupId = EcsGroup.registerGroup(comptags);
        updatepergroup = true;
        this.eventtype = eventtype;
        this.requesttype = requesttype;
    }

    public EcsSystem() {
        updatepergroup = false;
    }

    public EcsSystem(RequestType[] requesttype, EventType[] eventtype) {
        updatepergroup = false;
        this.eventtype = eventtype;
        this.requesttype = requesttype;
    }

    /**
     * 16.4.21: For avoiding confusion. This ist the init() of the system.
     */
    public abstract void init();

    /**
     * Brauchts einen init? Zumindest die Eventbasierten haben sonst keine Initialisierung.
     * Wird erstmalig und einmalig mit null aufgerufen, weil es ja nicht unbedingt Entites dazu gibt.
     * 16.4.21: Now we have a parameterless init()
     *
     * @param group
     */
    public abstract void init(EcsGroup group);

    //public abstract Log getLogger();

    public String getGroupId() {
        if (groupId == null) {
            //groupid=null;
            //      getLogger().warn("groupid isType null in system "+name);
        }
        return groupId;
    }

    /**
     * Wird mit entity=null aufgerufen, wenn das System als "not updatepergroup" definiert ist.
     *
     * 3.4.18: Manchmal ist es wirklich sinnvoll, im Update auch auf andere Components zugreifen zu koennen.
     * Die sind einfach nicht immer disjukt (z.B:PropertComponent). Aber entity steht dafuer auch schon in der group.
     *
     * @param group
     * @param tpf the time from the last frame in seconds
     * @return
     */
    public abstract void update(EcsEntity entity, EcsGroup group, double tpf);

    /**
     * Events this system subscribed. null, if no.
     */
    public EventType[] getEventType() {
        return eventtype;
    }

    /**
     * Ein Event verarbeiten.
     *
     * @param evt
     */
    public abstract void process(Event evt);

    /**
     * Requests, fuer das dieses System zustaendig ist. null, wenn fuer keins.
     */
    public RequestType[] getRequestType() {
        return requesttype;
    }

    /**
     * Ein Request verarbeiten.
     * Return true if request was processed.
     */
    public abstract boolean processRequest(Request request);

    public abstract void frameinit();

    /**
     * 17.5.21:Should be abstract in some future like in EcsComponent.
     * @return
     */
    public String getTag(){
        return null;
    }
}
