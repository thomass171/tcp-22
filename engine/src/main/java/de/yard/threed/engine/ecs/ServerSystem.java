package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.EventType;
import de.yard.threed.engine.platform.common.RequestType;

import java.util.ArrayList;
import java.util.List;

/**
 * system to provide information about inital events (static content, eg. scenery) and entity state changes
 * to a client in client-server mode. Events are sent after login not after join because the client might just be an observer.
 * <p>
 * Requests are transmitted by SystemManager for now.
 * <p>
 * Seems to be better to have a dedicated system instead of integrating it in SystemManager.
 * But SystemManager seems a better location for publishing entity states because it has all infos.
 * <p>
 * To some degree this is the counterpart of {@link ClientSystem ).
 * <p>
 * Created by thomass on 16.09.20.
 */

public class ServerSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(ServerSystem.class);
    public static String TAG = "ServerSystem";

    private List<Event> savedInitEvents = new ArrayList<Event>();
    private EventType[] initialEvents;

    /**
     *
     */
    public ServerSystem() {
        super(new String[]{});
    }


    private ServerSystem(String[] strings, RequestType[] requestTypes, EventType[] eventTypes, EventType[] initialEvents) {
        super(strings, requestTypes, eventTypes);
        this.initialEvents = initialEvents;
    }

    @Override
    public void process(Event evt) {
        logger.debug("got event " + evt.getType());

        if (evt.getType().equals(UserSystem.USER_EVENT_LOGGEDIN)) {

            // send events to new client only.
            String connectionId = (String) evt.getPayload().get("connectionid");
            for (Event e : savedInitEvents) {
                SystemManager.sendEventToClient(e, connectionId);
            }
        }

        if (isEventToBeSaved(evt.getType())) {
            logger.debug("Saving event");
            savedInitEvents.add(evt);
        }
    }


    public static ServerSystem buildForInitialEventsForClient(EventType[] initialEvents) {

        EventType[] listenEvents = EcsHelper.extendEventTypeArray(initialEvents, UserSystem.USER_EVENT_LOGGEDIN);
        return new ServerSystem(new String[0], new RequestType[0], listenEvents, initialEvents);
    }

    @Override
    public String getTag() {
        return TAG;
    }

    private boolean isEventToBeSaved(EventType eventType) {
        for (EventType e : initialEvents) {
            if (e.getType() == eventType.getType()) {
                return true;
            }
        }

        return false;
    }

    private void publishEntityStates() {

    }

    public List<Event> getSavedEvents() {
        return savedInitEvents;
    }
}
