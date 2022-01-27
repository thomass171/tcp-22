package de.yard.threed.engine.ecs;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.platform.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Ableitung von SceneNode oder Attribute ScenenNode. Erster Ansatz als Ableitung von SceneNode. Koennte final sein, weil
 * Entities nur ComponentAggregationen sein sollen.
 * Gerade die Aggregation spricht aber doch gegen Ableitung. Also erstmal nicht.
 * <p>
 * <p>
 * 28.11.16:
 * Created by thomass on 16.09.16.
 */
public final class EcsEntity {
    Log logger = Platform.getInstance().getLog(EcsEntity.class);
    //public EcsComponent position;
    public SceneNode scenenode;
    public List<EcsComponent> components = new ArrayList<EcsComponent>();
    public Map<String, EcsGroup> groups = null;
    //8.3.18: Jetzt wo es die base node gibt: Kann man helper damit ersetzen.
    @Deprecated
    public SceneNode helpernode;
    private int id;
    private static int idcounter = 1;
    boolean wranlogged;
    //nur zur besseren Analyse/Debug
    String name;
    //4.10.19 ist basenode optional?
    //22.10.19:Kann das nicht in die VehicleComponent?Nee,weg.
    //26.10.19public SceneNode basenode;
    private EcsSystem lockowner;

    public EcsEntity() {
        SystemManager.addEntity(this);
        id = idcounter++;
    }

    public EcsEntity(SceneNode sn) {
        this();
        scenenode = sn;
    }

    public EcsEntity(SceneNode sn, EcsComponent c) {
        this(sn);
        addComponent(c);
    }

    public EcsEntity(EcsComponent c) {
        this();
        addComponent(c);
    }

    public void addComponent(EcsComponent component) {
        components.add(component);
        //reinit required? Geht wohl ueber die Group
        //18.4.17: Mal versuchen
        groups = null;
        if (SystemManager.isinited) {
            SystemManager.initEntity(this);
        }
    }

    /**
     * Es kann nur eine pro groupid geben.
     *
     * @param groupid
     * @return
     */
    public EcsGroup getGroup(String groupid) {
        if (groups == null) {
            groups = EcsGroup.getMatchingGroups(components, this);
        }
        if (groupid == null) {
            if (!wranlogged) {
                //12.4.17: manchmal zu haeufig
                logger.warn("groupid isType null in entity " + name);
                wranlogged = true;
            }
        }
        return groups.get(groupid);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        return id == ((EcsEntity) o).id;
    }

    /**
     * 12.11.18: Es ist nicht definiert, was bei doppelten passiert, oder?
     *
     * @param tag
     * @return
     */
    public EcsComponent getComponent(String tag) {
        for (EcsComponent c : components) {
            if (c.getTag().equals(tag)) {
                return c;
            }
        }
        return null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    /*26.10.19public void setBasenode(SceneNode basenode) {
        this.basenode = basenode;
    }*/

    public SceneNode getSceneNode() {
        return scenenode;
    }

    public boolean isLocked() {
        return lockowner != null;
    }

    public boolean lockEntity(EcsSystem system) {
        if (lockowner != null) {
            return false;
        }
        lockowner = system;
        //statechangetimestamp = ((Platform)Platform.getInstance()).currentTimeMillis();
        return true;
    }

    public void release(EcsSystem system) {
        if (system != lockowner) {
            logger.warn("released not by owner");
        }
        lockowner = null;
        //statechangetimestamp = ((Platform)Platform.getInstance()).currentTimeMillis();
    }

    /**
     * @param system
     * @return
     */
    public boolean isLockedBy(EcsSystem system) {
        if (system == null) {
            return false;
        }
        return system == lockowner;
    }

    public String getLockOwner() {
        if (lockowner != null) {
            return lockowner.name;
        }
        return null;
    }

    /**
     * 30.4.21: Kann man immer brauchen.
     */
    public static List<EcsEntity> filterList(List<EcsEntity> list, EntityFilter filter) {

        List<EcsEntity> result = new ArrayList<EcsEntity>();
        for (EcsEntity e : list) {
            if (filter == null || filter.matches(e)) {
                result.add(e);
            }
        }
        return result;

    }
}
