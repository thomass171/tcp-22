package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Packet;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeEventBus;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.platform.common.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * From http://www.richardlord.net/blog/what-is-an-entity-framework.
 * <p>
 * Singleton by making all elements static.
 * Thread safe by syncing all methods (TODO make variables private).
 * Important for concurrent access in sceneserver/servermanager.
 * <p>
 * Created by thomass on 28.11.16.
 */
public class SystemManager {

    static private List<EcsSystem> systems = new ArrayList<EcsSystem>();
    static private List<EcsEntity> entities = new ArrayList<EcsEntity>();
    //3.1.23 private static SystemManager instance = null;
    //1.8.17: Es kann aber mehrere Listener fuer ein Event geben
    static private Map<EventType, List<EcsSystem>> eventhandler = new HashMap<EventType, List<EcsSystem>>();
    static public boolean isinited = false;
    static private boolean paused = false;
    static public String DATAPROVIDERELEVATION = "Elevation";
    static private Map<String, DataProvider> dataprovider = new HashMap<String, DataProvider>();
    static private Map<String, EcsService> services = new HashMap<String, EcsService>();
    //11.10.19: Die Requests sollten auch ueber den EventBus gehen. TODO ja, 20.3.20. 12.10.21: Aber Requests haben Handler.Hmm.
    static private RequestQueue requestQueue = new RequestQueue();
    static public DefaultBusConnector busConnector = null;
    static private List<Event> netEvents = new ArrayList<Event>();
    static private List<Request> netRequests = new ArrayList<Request>();
    static private SystemTracker systemTracker = new DefaultSystemTracker();

    static public synchronized void addSystem(EcsSystem system, int priority) {
        systems.add(system);
        // 27.12.16: Hier der init ist doch doof, dann kann er es doch selber sofort machen.
        //Tja, brauch ich den wirklich? system.init();
        RequestType[] requestlist;
        if ((requestlist = system.getRequestType()) != null) {
            for (RequestType evt : requestlist) {
                requestQueue.addHandler(evt, system);
            }
        }
        EventType[] eventlist;
        if ((eventlist = system.getEventType()) != null) {
            for (EventType evt : eventlist) {
                List<EcsSystem> l = eventhandler.get(evt);
                if (l == null) {
                    l = new ArrayList<EcsSystem>();
                    eventhandler.put(evt, l);
                }
                l.add(system);
            }

        }

    }

    static public synchronized void initSystems() {
        //4.4.17: fuer entities witzlos, weil es keine neuen entities handled. Aber das Sydstem selber soll/kann auch nochmal einen init machen 
        //im update ist es aber auch doof. 18.4.17: Besser ist es da aber doch. Denn es koennen doch jederzeit Components dazukommen.
        if (isinited) {
            //27.3.20 nur mal um das klarzumachen
            throw new RuntimeException("already inited");
        }
        getLogger().info("init " + systems.size() + " systems and " + entities.size() + " entities");
        // systems inits might create entities
        for (EcsSystem system : systems) {
            //16.4.21: New explicit call for system init. Passing null group isType confusing. TODO remove group null call
            system.init();
            system.init(null);
        }
        for (EcsEntity entity : entities) {
            initEntity(entity);
        }
        isinited = true;
            /*if (system instanceof UpdateBasedEcsSystem) {
                UpdateBasedEcsSystem ubs = (UpdateBasedEcsSystem) system;
                for (EcsEntity entity : entities) {
                    // erstmal dynamsich matchen.
                    /*EcsGroup group = getMatchingGroup(ubs, entity);
                    if (group != null) {
                        system.init(group);
                    }* /
                    EcsGroup group = entity.getGroup(system.getGroupId());
                    if (group != null) {
                        system.init(group);
                    }
                }
            }*/

    }

    static public synchronized void addSystem(EcsSystem system) {
        addSystem(system, 0);
    }

    /**
     * Before calling update() in each ECS System, the incoming requests and events are processed.
     *
     * @param tpf
     */
    static public synchronized void update(double tpf) {
        if (paused) {
            return;
        }
        // 21.3.19: Requests are processed before events without any special reason. And published to the net.
        requestQueue.process(busConnector);
        // Requests received from net. Requests not processed stay in the list.
        requestQueue.processRequestsFromNetwork(netRequests);

        // process non entity state events inclusive sending to network
        List<Event> eventsToProcess = new ArrayList<Event>();
        NativeEventBus eb = Platform.getInstance().getEventBus();
        Event evt;
        //getLogger().debug("update: processing " + eb.getEventCount() + " events");
        // MA46: Defer new events from being processed in this cycle
        while ((evt = eb.poll(0)) != null) {
            eventsToProcess.add(evt);
        }
        for (Event evt1 : eventsToProcess) {
            processEvent(evt1);
            if (busConnector != null) {
                // client server ping pong is avoided by not putting network events into the local bus
                busConnector.pushEvent(evt1);
            }
            systemTracker.eventProcessed(evt1);
        }
        for (Event et : netEvents) {
            processEvent(et);
        }
        netEvents.clear();
        // Send entity states after regular events to have latest changes.
        // Doing it before regular events to give the client the chance to build the model before a regular event arrives that needs
        // a model (like EVENT_USER_ASSEMBLED) seems better, but isn't really a benefit. This might be argued depending on the situation this way or the other.
        if (DefaultBusConnector.entitySyncEnabled && busConnector != null && busConnector.isServer()) {
            //getLogger().debug("Sending state events for " + entities.size() + " entities");
            for (EcsEntity entity : entities) {
                Event entityEvent = DefaultBusConnector.buildEntitiyStateEvent(entity);
                if (entityEvent != null) {
                    busConnector.pushEvent(entityEvent);
                }
            }
        }

        for (EcsSystem system : systems) {
           /* if (AbstractSceneRunner.getInstance().getFrameCount()<10) {
                getLogger().debug("Updating " + system.getTag());
            }*/
            long starttime = Platform.getInstance().currentTimeMillis();
            system.frameinit();
            if (system.updatepergroup) {
                int cnt = processEntityGroups(system.getGroupId(), (entity, group) -> {
                    //6.4.17: init der group geht erst jetzt, um zur Runtime hinzugekommene auch zu handeln.
                    // TODO Das mit dem init ist anfaellig fuer vergessen. Geht das nicht anders.
                    // Bloed ist es vor allem, weil es erst im Folgeframe gemacht wird und eine neue ScendeNode dann kurzzeitig an falscher position ist.
                    //init mal im addEnttity versuchen
                            /*if (!group.isInited(group)) {
                                ubs.init(group);
                                group.inited = true;
                            }*/
                    system.update(entity, group, tpf);
                });
                long took = Platform.getInstance().currentTimeMillis() - starttime;
                // 14.3.24: Threshold 500->100
                if (took > 100) {
                    // should be warn as it probably leads to bad user experience
                    getLogger().warn("update of " + system.getTag() + " for " + cnt + " groups/entities took " + took + " ms");
                }
            } else {
                system.update(null, null, tpf);
                long took = Platform.getInstance().currentTimeMillis() - starttime;
                if (took > 100) {
                    // should be warn as it probably leads to bad user experience
                    getLogger().warn("update of " + system.getTag() + " took " + took + " ms");
                }
            }
        }
    }

    static private void processEvent(Event evt) {
        //getLogger().debug("processEvent " + evt);
        List<EcsSystem> handler = eventhandler.get(evt.getType());
        if (handler != null) {
            for (EcsSystem ebs : handler) {
                ebs.process(evt);
            }
        }
    }
    /**
     * Group of components related to system.
     * Hängt vom System ab, bzw. davon wie es arbeitet.
     *
     * @param system
     * @param entity
     * @return
     */
    /*MA31 dependency zu graph, muesste wenn generisch sein private static EcsGroup getMatchingGroup(EcsSystem system, EcsEntity entity) {
       /* if (system instanceof AnimationUpdateSystem){
            if (entity.hasComponent("AnimationComponent")){
                return new EcsGroup(c);
            }
        }else if (system instanceof MovingSystem){
            //Maze
            if (entity.hasComponent("Mover")){
                return new EcsGroup(c);
            }
        }else
        /*for (EcsComponent c : entity.components) {
            if (c instanceof AnimationComponent && system instanceof AnimationUpdateSystem) {
                return new EcsGroup(c);
            }
            //Maze
            if (c instanceof Mover && system instanceof MovingSystem) {
                return new EcsGroup(c);
            }
           /* if (c instanceof GraphMovingComponent && system instanceof GraphMovingSystem) {* /

        }* /
        EcsGroup grp = null;
        if ((grp = GraphMovingSystem.matches(entity.components)) != null) {
            return grp;
        }
        return null;
    }*/

    /*MA31 dependency zu graph und gehört hier dann doch nicht hin public static EcsGroup matches(List<EcsComponent> components) {
        EcsGroup grp = new EcsGroup();
        for (EcsComponent c : components) {
            if (c instanceof VelocityComponent) {
                grp.add(c);
            }
            if (c instanceof GraphMovingComponent) {
                grp.add(c);
            }
        }
        if (grp.cl.size() == 2) {
            return grp;
        }
        return null;
    }*/

    /**
     * eigentlich nur für Tests
     */
    static public synchronized void reset() {
        //nicht hier, weil Teil der Platform. ((Platform)  Platform.getInstance()).getEventBus().clear();
        requestQueue.reset();
        eventhandler.clear();
        systems.clear();
        entities.clear();
        dataprovider.clear();
        services.clear();
        isinited = false;
        busConnector = null;
        netEvents.clear();
        netRequests.clear();
        systemTracker = new DefaultSystemTracker();
    }

    static public synchronized void setSystemTracker(SystemTracker psystemTracker) {
        systemTracker = psystemTracker;
        if (busConnector != null) {
            DefaultBusConnector.setSystemTracker(psystemTracker);
        }
    }

    static public synchronized void reportStatistics() {
        systemTracker.report();
    }


    public void removeSystem(EcsSystem system) {
        //system.end();
        systems.remove(system);
    }

    static public synchronized void sendEvent(Event evt) {
        Platform.getInstance().getEventBus().publish(new Event(evt.getType(), evt.payload));
    }

    static public synchronized void sendEventToClient(Event evt, String connectionId) {
        if (busConnector == null) {
            getLogger().warn("No bus connector");
            return;
        }
        busConnector.pushEvent(evt, connectionId);
    }

    /**
     * 7.4.21: Can be used before inited for adding requests from an init() and not only from update().
     *
     * @param request
     */
    static public synchronized void putRequest(Request request) {
        getLogger().debug("putRequest " + request);
        systemTracker.requestPut(request);
        requestQueue.addRequest(request);
    }

    static public synchronized void addEntity(EcsEntity entity) {
        entities.add(entity);
        //7.4.17:Mal hier den init fuer neue versuchen
        //18.4.17: Das ist aber doch irgendwie zu frueh, weil es noch keine Component gibt.
        /*mal im addcomponent if (isinited){
            initEntity(entity);
        }*/
    }

    static public synchronized void initEntity(EcsEntity entity) {
        for (EcsSystem system : systems) {
            EcsGroup group = entity.getGroup(system.getGroupId());
            if (group != null) {
                if (!group.isInited(group)) {
                    //if (!group.isInited(group)) {
                    //16.4.21: The following isType the group init, not the system!
                    system.init(group);
                    group.inited = true;
                }
            }
        }

    }


    static public synchronized void removeEntity(EcsEntity ecsEntity) {
        entities.remove(ecsEntity);
    }

    static public synchronized void pause() {
        paused = !paused;
    }

    /**
     * So eine MEthode gabs doch schon mal? Find ich aber nicht mehr. Generischer list filter jetzt in EcsEntity.
     * Filter might be null, so returning all.
     * 9.3.18
     *
     * @return
     */
    static public synchronized List<EcsEntity> findEntities(EntityFilter filter) {
        return EcsHelper.filterList(entities, filter);
    }

    static public synchronized int processEntityGroups(String groupid, EcsGroupHandler ecsGroupHandler) {
        int cnt = 0;
        for (EcsEntity entity : entities) {
            // erstmal dynamsich matchen.
                    /*EcsGroup group = getMatchingGroup(ubs, entity);
                    if (group != null) {
                        ubs.update(group, tpf);
                    }*/
            //3.4.17: Es kann nur eine Group geben.
                    /*for (EcsGroup gr : entity.getGroups(system.getGroupId())){
                        ubs.update(gr,tpf);
                    }*/
            EcsGroup group = entity.getGroup(groupid/*system.getGroupId()*/);
            if (group != null) {
                ecsGroupHandler.processGroups(entity, group);
                cnt++;
            }
        }
        return cnt;
    }

    static public synchronized void putDataProvider(String name, DataProvider provider) {
        // Allow removing provider by 'null'
        if (provider == null) {
            dataprovider.remove(name);
            return;
        }
        if (dataprovider.containsKey(name)) {
            throw new RuntimeException("duplicate provider " + name);
        }
        dataprovider.put(name, provider);
    }

    static public synchronized DataProvider getDataProvider(String name) {
        DataProvider dp = dataprovider.get(name);
        if (dp == null) {
            getLogger().warn("no data provider for '" + name + "'");
        }
        return dp;
    }

    static public synchronized void registerService(String name, EcsService service) {
        // Allow removing service by 'null'
        if (service == null) {
            services.remove(name);
            return;
        }
        if (services.containsKey(name)) {
            throw new RuntimeException("duplicate service " + name);
        }
        services.put(name, service);
    }

    static public synchronized EcsService getService(String name) {
        EcsService dp = services.get(name);
        if (dp == null) {
            getLogger().warn("no service for '" + name + "'");
        }
        return dp;
    }

    static public synchronized int getEventCount() {
        return (Platform.getInstance()).getEventBus().getEventCount();
    }

    static public synchronized int getRequestCount() {
        return requestQueue.getRequestCount();
    }

    /**
     * Only for tests
     * TODO use injection for tests like for eventbus
     *
     * @return
     */
    static public synchronized Request getRequest(int i) {
        return requestQueue.getRequest(i);
    }

    /**
     * Publish packet from network to local bus.
     */
    static public synchronized void publishPacketFromClient(Packet packet, String connectionId) {
        publishPacket(packet, connectionId);
        systemTracker.packetReceivedFromNetwork(packet);
    }

    static public synchronized void publishPacketFromServer(Packet packet) {
        // client only has one connection, so connectionId can be static
        publishPacket(packet, "c-to-s");
        systemTracker.packetReceivedFromNetwork(packet);
    }

    static private void publishPacket(Packet packet, String connectionId) {

        Request request;

        if (DefaultBusConnector.isEvent(packet)) {
            Event evt = DefaultBusConnector.decodeEvent(packet);
            if (evt != null) {
                // not needed for now evt.setConnectionId(connectionId);
                netEvents.add(evt);
            } else {
                getLogger().warn("Discarding event");
            }
        } else if ((request = DefaultBusConnector.decodeRequest(packet)) != null) {
            request.setConnectionId(connectionId);
            netRequests.add(request);
        } else {
            getLogger().error("unsupported packet (just a newline?): " + packet);
        }
    }

    static public synchronized EcsSystem findSystem(String tag) {
        for (EcsSystem system : systems) {
            if (tag.equals(system.getTag())) {
                return system;
            }
        }
        return null;
    }


    static public synchronized void setBusConnector(DefaultBusConnector pbusConnector) {
        busConnector = pbusConnector;
        // keep systemtrack synced. Hopefully this is really intended.
        DefaultBusConnector.setSystemTracker(systemTracker);
    }

    /**
     * 13.1.23: ugly workround needed for Requestqueue.
     */
    @Deprecated
    static public synchronized DefaultBusConnector getBusConnector() {
        return busConnector;
    }

    static private Log getLogger() {
        return Platform.getInstance().getLog(SystemManager.class);
    }
}
