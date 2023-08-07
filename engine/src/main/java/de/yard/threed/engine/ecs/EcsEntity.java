package de.yard.threed.engine.ecs;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ModelBuilder;
import de.yard.threed.engine.ModelBuilderRegistry;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An ECS entity.
 * <p>
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
    // mainly for analyse/debug. No need to be unique.
    String name;
    //4.10.19 ist basenode optional?
    //22.10.19:Kann das nicht in die VehicleComponent?Nee,weg.
    //26.10.19public SceneNode basenode;
    private EcsSystem lockowner;
    private String builderName;

    public EcsEntity() {
        SystemManager.addEntity(this);
        id = idcounter++;
    }

    /**
     * Constructor for client mode only for mirroring entities.
     */
    public EcsEntity(int id) {
        if (AbstractSceneRunner.getInstance().getBusConnector() == null) {
            throw new RuntimeException("invalid usage outside client mode");
        }
        SystemManager.addEntity(this);
        this.id = id;
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
        component.setEntityId(this.getId());
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
                //12.4.17: sometimes too often. 1.2.23 don't log at all. Its valid and quite common to have no groupid
                //logger.warn("groupid is null in entity " + name);
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
     * Important for client/server to publish the model information and to recreate an entity in the client.
     * Model building might be async!
     * Returns the destination node.
     */
    public SceneNode buildSceneNodeByModelFactory(String builderName, ModelBuilderRegistry[] modelBuilderRegistries) {

        logger.debug("buildSceneNodeByModelFactory: builderName=" + builderName);
        this.scenenode = new SceneNode();
        this.scenenode.setName("entity-wrapper");
        this.builderName = builderName;
        for (ModelBuilderRegistry registry : modelBuilderRegistries) {
            ModelBuilder modelBuilder = registry.lookupModelBuilder(builderName);
            if (modelBuilder != null) {
                // potential async build
                modelBuilder.buildModel(scenenode, this);
                return this.scenenode;
            }
        }
        logger.warn("No model built. Builder name not registered:" + builderName);
        return scenenode;
    }

    public String getBuilderName() {
        return builderName;
    }

    public int getComponentCount() {
        return components.size();
    }
}
