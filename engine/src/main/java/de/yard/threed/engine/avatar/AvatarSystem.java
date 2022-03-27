package de.yard.threed.engine.avatar;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformHelper;
import de.yard.threed.engine.Geometry;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.ObserverComponent;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ViewPoint;
import de.yard.threed.engine.ecs.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.common.*;
import de.yard.threed.engine.vr.VrInstance;

import java.util.List;

/**
 * Avatar administration.
 * <p>
 * Building the avatar isType the step USER_REQUEST_JOIN->USER_EVENT_JOINED.
 * <p>
 * 22.11.21: The name AvatarSystem is confusing. In fact its a player system. And the player just might have an avatar model (body).
 * <p>
 * Created by thomass on 20.11.20.
 */

public class AvatarSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(AvatarSystem.class);

    boolean avatarsystemdebuglog = true;

    //15.5.21 boolean enableNearView = false;

    public static LocalTransform initialTransform = null;
    boolean enableObserverComponent = false;
    private AvatarBuilder avatarBuilder = null;
    private boolean isFirstJoin = true;
    private LocalTransform viewTransform;

    /**
     *
     */
    public AvatarSystem(boolean enableObserverComponent) {
        super(new String[]{"AvatarComponent"}, new RequestType[]{UserSystem.USER_REQUEST_JOIN}, new EventType[]{UserSystem.USER_EVENT_JOINED});

        this.enableObserverComponent = enableObserverComponent;

    }

    public AvatarSystem() {
        this(false);
    }

    public static AvatarSystem buildFromArguments() {
        boolean enableNearView = false;

        Boolean b;
        if ((b = PlatformHelper.getBooleanSystemProperty("argv.enableNearView")) != null) {
            enableNearView = (boolean) b;
        }
        return new AvatarSystem(false);
    }

    public void setAvatarBuilder(AvatarBuilder avatarBuilder) {
        this.avatarBuilder = avatarBuilder;
    }

    public void setViewTransform(LocalTransform viewTransform) {
        this.viewTransform = viewTransform;
    }

    /**
     *
     */
    @Override
    public void update(EcsEntity entity, EcsGroup group, double tpf) {
    }

    @Override
    public boolean processRequest(Request request) {
        if (true) {
            logger.debug("got request " + request.getType());
        }

        if (request.getType().equals(UserSystem.USER_REQUEST_JOIN)) {

            int userEntityId = ((int) request.getPayloadByIndex(0));
            Boolean forLogin = (Boolean) request.getPayloadByIndex(1);
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
            // Attach the oberver to the avatar. Is the connection to observer good located here?
            // 19.11.21: Should be independant from ObserverComponent? Probably. If there is an oberver, attach it to avatar
            // This is also reached for bot and MP joining.
            // 14.2.22 Attach observer independent from VR. But only to the first player (for now)
            if ((boolean) forLogin && Observer.getInstance() != null && isFirstJoin) {
                logger.debug("Attaching oberserver " + Observer.getInstance().getTransform() + " to avatar " + avatarNode.getTransform());
                Observer.getInstance().getTransform().setParent(avatarNode.getTransform());

                // In non VR the position might need to be raised to head height and view direction slightly down. (eg in maze)
                if (viewTransform != null && VrInstance.getInstance() == null) {

                    // MazeScene.rayy now is covered by avatarbuilder
                    //LocalTransform viewTransform = avatarBuilder.getViewTransform();
                    Observer.getInstance().initFineTune(viewTransform/*getSettings().getViewpoint()*/.position/*.add(new Vector3(0, MazeScene.rayy, 0))*/);
                    // Rotation for looking slightly down.
                    Observer.getInstance().getTransform().setRotation(viewTransform/*getSettings().getViewpoint()*/.rotation);
                }
                isFirstJoin = false;
            }
            //avatar.avatarE.setName("Player");
            logger.debug(userEntity.getName() + " joined");

            SystemManager.sendEvent(new Event(UserSystem.USER_EVENT_JOINED, new Payload(userEntity)));

            return true;
        }
        return false;
    }

    @Override
    public void process(Event evt) {
        logger.debug("got event " + evt.getType());
    }

    /**
     * Build avatar for user. No observer change here.
     */
    private SceneNode buildAvatarForUserEntity(EcsEntity user) {
        logger.debug("Building avatar for player " + user);

        //Avatar av = new Avatar(user, avatarBuilder);
        SceneNode mainNode;
        if (avatarBuilder == null) {
            // simple green cube avatar
            mainNode = new SceneNode();
            // Simple green cube for testing.
            mainNode.setMesh(new Mesh(Geometry.buildCube(0.1f, 0.1f, 0.1f), Material.buildBasicMaterial(Color.GREEN)));
        } else {
            mainNode = avatarBuilder.buildAvatar(user);
        }
        mainNode.setName("Avatar");

        user.scenenode = mainNode;
        user.addComponent(new TeleportComponent(mainNode));
        user.addComponent(new AvatarComponent());
        Scene.getCurrent().addToWorld(mainNode);
        return mainNode;
    }
}
