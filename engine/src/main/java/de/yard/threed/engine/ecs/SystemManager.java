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
 * Aus http://www.richardlord.net/blog/what-is-an-entity-framework
 * <p>
 * Created by thomass on 28.11.16.
 */
public class SystemManager {
    static Log logger = Platform.getInstance().getLog(SystemManager.class);

    private static List<EcsSystem> systems = new ArrayList<EcsSystem>();
    private static List<EcsEntity> entities = new ArrayList<EcsEntity>();
    private static SystemManager instance = null;
    //1.8.17: Es kann aber mehrere Listener fuer ein Event geben
    private static Map<EventType, List<EcsSystem>> eventhandler = new HashMap<EventType, List<EcsSystem>>();
    public static boolean isinited = false;
    private static boolean paused = false;
    public static String DATAPROVIDERELEVATION = "Elevation";
    private static Map<String, DataProvider> dataprovider = new HashMap<String, DataProvider>();
    //11.10.19: Die Requests sollten auch ueber den EventBus gehen. TODO ja, 20.3.20. 12.10.21: Aber Requests haben Handler.Hmm.
    private static RequestQueue requestQueue = new RequestQueue();

    /* private SystemManager(){ }

    public static SystemManager getInstance(){
    if (instance==null){
        instance = new SystemManager();
    }
        return instance;
    }*/

    public static void addSystem(EcsSystem system, int priority) {
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

    public static void initSystems() {
        //4.4.17: fuer entities witzlos, weil es keine neuen entities handled. Aber das Sydstem selber soll/kann auch nochmal einen init machen 
        //im update ist es aber auch doof. 18.4.17: Besser ist es da aber doch. Denn es koennen doch jederzeit Components dazukommen.
        if (isinited) {
            //27.3.20 nur mal um das klarzumachen
            throw new RuntimeException("already inited");
        }
        logger.info("init " + systems.size() + " systems and " + entities.size() + " entities");
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


    public static void addSystem(EcsSystem system) {
        addSystem(system, 0);
    }

    /**
     * Vor dem eigentlichen update() werden erst anliegende Requests und Events verarbeitet.
     *
     * @param tpf
     */
    public static void update(double tpf) {
        if (paused) {
            return;
        }
        // 21.3.19: Requests einfach mal vor den Events.
        requestQueue.process();

        NativeEventBus eb = Platform.getInstance().getEventBus();
        Event evt;
        while ((evt = eb.poll(0)) != null) {
            List<EcsSystem> handler = eventhandler.get(evt.getType());
            if (handler != null) {
                for (EcsSystem ebs : handler) {
                    ebs.process(evt);
                }
            }
        }
        for (EcsSystem system : systems) {
            //if (system instanceof UpdateBasedEcsSystem) {
            //   UpdateBasedEcsSystem ubs = (UpdateBasedEcsSystem) system;
            system.frameinit();
            if (system.updatepergroup) {
                processEntityGroups(system.getGroupId(), (entity, group) -> {
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

            } else {
                system.update(null, null, tpf);
            }
            //}
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
     * For testing
     */
    public static void reinit() {
        logger.error("not yet");
    }

    /**
     * eigentlich nur für Tests
     */
    public static void reset() {
        //nicht hier, weil Teil der Platform. ((Platform)  Platform.getInstance()).getEventBus().clear();
        requestQueue.reset();
        eventhandler.clear();
        systems.clear();
        entities.clear();
        dataprovider.clear();
        isinited = false;
    }


    public void removeSystem(EcsSystem system) {
        //system.end();
        systems.remove(system);
    }

    public static void sendEvent(Event evt) {
        Platform.getInstance().getEventBus().publish(new Event(evt.getType(), evt.payload));
    }

    /**
     * 7.4.21: Schon vor dem init() aufrufbar, um schon mal Requests aus einem init() und nicht aus dem update() einreihen zu koennen.
     *
     * @param request
     */
    public static void putRequest(Request request) {
        /*7.4.21 if (!isinited){
            throw new RuntimeException("not inited");
        }*/
        requestQueue.addRequest(request);
    }

    public static void addEntity(EcsEntity entity) {
        entities.add(entity);
        //7.4.17:Mal hier den init fuer neue versuchen
        //18.4.17: Das ist aber doch irgendwie zu frueh, weil es noch keine Component gibt.
        /*mal im addcomponent if (isinited){
            initEntity(entity);
        }*/
    }

    public static void initEntity(EcsEntity entity) {
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


    public static void removeEntity(EcsEntity ecsEntity) {
        entities.remove(ecsEntity);
    }

    public static void pause() {
        paused = !paused;
    }

    public static List<EcsEntity> findEntities(String[] tags) {
        List<EcsEntity> result = new ArrayList<EcsEntity>();
        for (EcsEntity e : entities) {
            boolean match = true;
            for (String tag : tags) {
                if (e.getComponent(tag) == null) {
                    match = false;
                    break;
                }
            }
            if (match) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * So eine MEthode gabs doch schon mal? Find ich aber nicht mehr. Generischer list filter jetzt in EcsEntity.
     * Filter might be null, so returning all.
     * 9.3.18
     *
     * @return
     */
    public static List<EcsEntity> findEntities(EntityFilter filter) {
        return EcsEntity.filterList(entities, filter);
    }

    public static void processEntityGroups(String groupid, EcsGroupHandler ecsGroupHandler) {
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

            }
        }
    }

    public static void putDataProvider(String name, DataProvider provider) {
        dataprovider.put(name, provider);
    }

    public static DataProvider getDataProvider(String name) {
        return dataprovider.get(name);
    }

    public static int getEventCount() {
        return ( Platform.getInstance()).getEventBus().getEventCount();
    }

    public static int getRequestCount() {
        return requestQueue.getRequestCount();
    }

    /**
     * Only for tests
     * TODO use injection for tests like for eventbus
     * @return
     */
    public static Request getRequest(int i) {
        return requestQueue.getRequest(i);
    }

    public static void publishPacket(Packet packet) {

        String evt = packet.getValue("event");
        if (evt == null) {
            logger.error("no event in packet");
            return;
        }
        if (evt.equals(UserSystem.USER_REQUEST_LOGIN.getLabel())) {
//Event evt=new Event(E)
            putRequest(new Request(UserSystem.USER_REQUEST_LOGIN, null));
        }
    }

    public static EcsSystem findSystem(String tag) {
        for (EcsSystem system : systems) {
            if (tag.equals(system.getTag())) {
                return system;
            }
        }
        return null;
    }
}
