package de.yard.threed.engine.avatar;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.ObserverComponent;
import de.yard.threed.engine.Scene;
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
    //15.5.21 Double yoffsetVR;

    //direkt deprecated weil Kruecke
    @Deprecated
    static Avatar avatar = null;
    public static LocalTransform initialTransform = null;
    boolean enableObserverComponent = false;

    /**
     * @param yoffsetVR
     * @param enableLoweredAvatar
     */
    public AvatarSystem(Double yoffsetVR, boolean enableLoweredAvatar, boolean enableObserverComponent) {
        super(new String[]{"AvatarComponent"}, new RequestType[]{UserSystem.USER_REQUEST_JOIN}, new EventType[]{UserSystem.USER_EVENT_JOINED});

        //15.5.21 this.yoffsetVR = yoffsetVR;
        //15.5.21 this.enableLoweredAvatar = enableLoweredAvatar;
        this.enableObserverComponent = enableObserverComponent;

    }

    public AvatarSystem() {
        this(0.0, false, false);
    }

    public AvatarSystem(boolean enableObserverComponent) {
        this(0.0, false, enableObserverComponent);
    }

    public static AvatarSystem buildFromArguments() {
        boolean enableNearView = false, enableLoweredAvatar = false;
        Double yoffsetVR;

        Boolean b;
        if ((b = EngineHelper.getBooleanSystemProperty("argv.enableNearView")) != null) {
            enableNearView = (boolean) b;
        }
        if ((b = EngineHelper.getBooleanSystemProperty("argv.enableLoweredAvatar")) != null) {
            enableLoweredAvatar = (boolean) b;
        }
        yoffsetVR = EngineHelper.getDoubleSystemProperty("argv.yoffsetVR");
        return new AvatarSystem(yoffsetVR, enableLoweredAvatar, false);
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

            String userEntityName = (String) request.getPayloadByIndex(0);
            Boolean forLogin = (Boolean) request.getPayloadByIndex(1);
            EcsEntity userEntity = SystemManager.findEntities(new NameFilter(userEntityName)).get(0);
            avatar = buildAvatar(userEntity);

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
            // But not in VR. VR needs ground distance.
            // This is also reached for bot and MP joining.
            if ((boolean) forLogin && Observer.getInstance() != null && !VrInstance.isEnabled()) {
                logger.debug("Attaching oberserver to avatar");
                Observer.getInstance().getTransform().setParent(AvatarSystem.getAvatar().getSceneNode().getTransform());
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

    public static Avatar getAvatar() {
        return avatar;
    }

    /**
     * 15.5.21: MA35: camera nicht mehr am Avatar, sondern Observer
     * Trennung ist aber knifflig. Darum erstmal Avatar nur fuer Bots, aber nicht main player. Geht aber nicht so einfach, also doch auch fuer player?
     * TODO ECS Erstellung muss hier aber wirklich raus. Wird dann später gemacht.
     */
    private static Avatar buildAvatar(EcsEntity player) {
        logger.debug("Building avatar for player " + player);

        Avatar av = new Avatar(player);
        av.enableBody();
        Scene.getCurrent().addToWorld(av.getSceneNode());

        //
       /* if (Observer.getInstance()!=null) {
            Observer.getInstance().getTransform().setParent(avatar.getNode().getTransform());
        }*/

        //1.4.21 Player.init(avatar);


        /*15.5.21 war eh ne Kruecke
        if (initialTransform != null) {
            av.setPosition(initialTransform.position);
            av.setRotation(initialTransform.rotation);
        }*/
        return av;
    }
}
