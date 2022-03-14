package de.yard.threed.engine.avatar;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformHelper;
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

    public static LocalTransform initialTransform = null;
    boolean enableObserverComponent = false;

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
            EcsEntity userEntity = SystemManager.findEntities((e) -> e.getId() == userEntityId).get(0);
            Avatar avatar = buildAvatarForUserEntity(userEntity);

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
            // 14.2.22 Attach observer independent from VR
            if ((boolean) forLogin && Observer.getInstance() != null) {
                logger.debug("Attaching oberserver to avatar");
                Observer.getInstance().getTransform().setParent(avatar.getSceneNode().getTransform());
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
     * 15.5.21: MA35: camera nicht mehr am Avatar, sondern Observer
     * Trennung ist aber knifflig. Darum erstmal Avatar nur fuer Bots, aber nicht main player. Geht aber nicht so einfach, also doch auch fuer player?
     */
    private static Avatar buildAvatarForUserEntity(EcsEntity user) {
        logger.debug("Building avatar for player " + user);

        Avatar av = new Avatar(user);
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
