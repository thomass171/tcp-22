package de.yard.threed.engine.avatar;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Geometry;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.ModelBuilderRegistry;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.ObserverComponent;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ViewPoint;
import de.yard.threed.engine.ecs.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.*;
import de.yard.threed.engine.vr.VrInstance;

import java.util.List;

/**
 * Avatar administration.
 * <p>
 * Building the avatar is the result of a USER_EVENT_JOINED? But the process of joining should be
 * done in the game logic. "Joining" makes the entity ready for playing, but no observer yet. (observer attaching is part of a client system).
 * For convenience and backward compatibility a short-cut-join however is still used here as default.
 * <p>
 * 22.11.21: The name AvatarSystem is confusing. In fact its a player system. And the player just might have an avatar model (body).
 * 13.02.23: TODO Avatar building should be the result of JOINED, not the
 * 15.02.23: observer handling decoupled to ObserverSystem.
 * <p>
 * Created by thomass on 20.11.20.
 */

public class AvatarSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(AvatarSystem.class);
    public static String TAG = "AvatarSystem";
    boolean avatarsystemdebuglog = true;

    //15.5.21 boolean enableNearView = false;

    public static LocalTransform initialTransform = null;
    boolean enableObserverComponent = false;
    String builderName;
    ModelBuilderRegistry modelBuilderRegistry;
    private boolean useShortCutJoin = true;

    /**
     *
     */
    public AvatarSystem(boolean enableObserverComponent) {
        super(new String[]{"AvatarComponent"}, new RequestType[]{UserSystem.USER_REQUEST_JOIN}, new EventType[]{});
        this.enableObserverComponent = enableObserverComponent;
    }

    public AvatarSystem() {
        this(false);
    }

    public static AvatarSystem buildFromArguments() {
        boolean enableNearView = false;

        Boolean b;
        if ((b = Platform.getInstance().getConfiguration().getBoolean("enableNearView")) != null) {
            enableNearView = (boolean) b;
        }
        return new AvatarSystem(false);
    }

    public void setAvatarBuilder(String builderName, ModelBuilderRegistry modelBuilderRegistry) {
        this.builderName = builderName;
        this.modelBuilderRegistry = modelBuilderRegistry;
    }

    /**
     *
     */
    @Override
    public void update(EcsEntity entity, EcsGroup group, double tpf) {
    }

    @Override
    public boolean processRequest(Request request) {
        if (avatarsystemdebuglog) {
            logger.debug("got request " + request);
        }

        if (request.getType().equals(UserSystem.USER_REQUEST_JOIN)) {

            if (useShortCutJoin) {
                int userEntityId = ((int) request.getPayloadByIndex(0));
                Boolean forLogin = (Boolean) request.getPayloadByIndex(1);

                EcsEntity userEntity = shortCutJoin(userEntityId);

                /*if (attachObserver(forLogin, isFirstJoin, userEntity, viewTransform)){
                    isFirstJoin=false;
                }*/

                //avatar.avatarE.setName("Player");
                logger.debug("User '" + userEntity.getName() + "' joined");

                SystemManager.sendEvent(buildUserJoinedEvent(userEntity));

                return true;
            }
        }
        return false;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static Event buildUserJoinedEvent(EcsEntity userEntity) {
        return new Event(UserSystem.USER_EVENT_JOINED, new Payload().add("userentityid", userEntity.getId()));
    }

    /**
     * Do a join, which makes a player a participant in the game:
     * - create components needed
     * - build and locate(?) avatar.
     * <p>
     * The login process created the user entity.
     */
    private EcsEntity shortCutJoin(int userEntityId) {
        logger.debug("shortCutJoin userEntityId"+userEntityId);

        EcsEntity userEntity = EcsHelper.findEntityById(userEntityId);
        SceneNode avatarNode = buildAvatarForUserEntity(userEntity);

        TeleportComponent tc = TeleportComponent.getTeleportComponent(userEntity);
        if (tc != null) {
            DataProvider viewpointsDataProvider = SystemManager.getDataProvider("viewpoints");
            if (viewpointsDataProvider == null) {
                logger.debug("no viewpointsDataProvider");
            } else {
                //TODO dataprovider need kind of typing/enum?
                List<ViewPoint> viewPoints = (List<ViewPoint>) viewpointsDataProvider.getData(new String[]{});
                if (viewPoints == null) {
                    logger.debug("no teleport viewpoints to add to avatar");
                } else {
                    for (ViewPoint vc : viewPoints) {
                        tc.addPosition(vc.name, vc.transform);
                    }
                }
            }
        }
        if (enableObserverComponent) {
            //25.10.21 From FlatTravel. Not used in maze.
            ObserverComponent oc = new ObserverComponent(Scene.getCurrent().getDefaultCamera().getCarrierTransform());
            oc.setRotationSpeed(40);
            userEntity.addComponent(oc);
        }
        return userEntity;
    }



    /**
     * Build avatar for user. No observer change here.
     * The avatar is quite independent from the observer.
     */
    private SceneNode buildAvatarForUserEntity(EcsEntity user) {
        logger.debug("Building avatar for player " + user);

        //Avatar av = new Avatar(user, avatarBuilder);
        SceneNode mainNode;
        if (builderName == null || modelBuilderRegistry == null) {
            // simple green cube avatar
            mainNode = new SceneNode();
            // Simple green cube for testing.
            mainNode.setMesh(new Mesh(Geometry.buildCube(0.1f, 0.1f, 0.1f), Material.buildBasicMaterial(Color.GREEN)));
            // used as marker
            user.addComponent(new AvatarComponent());
        } else {
            mainNode = user.buildSceneNodeByModelFactory(builderName, new ModelBuilderRegistry[]{modelBuilderRegistry});
        }
        mainNode.setName("Avatar");

        user.scenenode = mainNode;
        user.addComponent(new TeleportComponent(mainNode));

        Scene.getCurrent().addToWorld(mainNode);
        return mainNode;
    }
}
