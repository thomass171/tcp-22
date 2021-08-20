package de.yard.threed.engine.avatar;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.ecs.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.common.*;

/**
 * Avatar administration.
 * <p>
 * Building the avatar isType the step USER_REQUEST_JOIN->USER_EVENT_JOINED.
 *
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

    /**
     * @param yoffsetVR
     * @param enableLoweredAvatar
     */
    public AvatarSystem(Double yoffsetVR, boolean enableLoweredAvatar) {
        super(new String[]{"AvatarComponent"}, new RequestType[]{UserSystem.USER_REQUEST_JOIN}, new EventType[]{UserSystem.USER_EVENT_JOINED});

        //15.5.21 this.yoffsetVR = yoffsetVR;
        //15.5.21 this.enableLoweredAvatar = enableLoweredAvatar;
    }

    public AvatarSystem() {
        this(0.0, false);
    }

    public static AvatarSystem buildFromArguments() {
        boolean enableNearView = false, enableLoweredAvatar = false;
        Double yoffsetVR;

        if (EngineHelper.getBooleanSystemProperty("argv.enableNearView")) {
            enableNearView = true;
        }
        if (EngineHelper.getBooleanSystemProperty("argv.enableLoweredAvatar")) {
            enableLoweredAvatar = true;
        }
        yoffsetVR = EngineHelper.getDoubleSystemProperty("argv.yoffsetVR");
        return new AvatarSystem(yoffsetVR, enableLoweredAvatar);
    }

    /**
     *
     */
    @Override
    public void update(EcsEntity entity, EcsGroup group, double tpf) {
        //wurde evtl. auch nach InputToRequestSystem passen wegen user input. Und dann hier nur die Requests. Waere konsistenter.
        if (avatar != null) {
            avatar.update();
        }
    }

    @Override
    public boolean processRequest(Request request) {
        if (true) {
            logger.debug("got request " + request.getType());
        }

        if (request.getType().equals(UserSystem.USER_REQUEST_JOIN)) {

            avatar = buildAvatar();
            EcsEntity a=avatar.avatarE;
            SystemManager.sendEvent(new Event(UserSystem.USER_EVENT_JOINED, new Payload(a)));

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
    private static Avatar buildAvatar() {
        logger.debug("Building avatar");
        //avatar = Avatar.buildDefault(Scene.getCurrent().getDefaultCamera());
        Avatar  av = new Avatar(/*MA35 Scene.getCurrent().getDefaultCamera()*/null, new Quaternion(), true);
        av.enableBody();
        Scene.getCurrent().addToWorld(av.getSceneNode());

        //
       /* if (Observer.getInstance()!=null) {
            Observer.getInstance().getTransform().setParent(avatar.getNode().getTransform());
        }*/

        //1.4.21 Player.init(avatar);

        /*15.5.21 if (yoffsetVR != null) {
            avatar.setBestPracticeRiftvryoffset((double) yoffsetVR);
        }*/
        /*if (enableLoweredAvatar) {
            avatar.lowerVR();
        }*/

        /*15.5.21 war eh ne Kruecke
        if (initialTransform != null) {
            av.setPosition(initialTransform.position);
            av.setRotation(initialTransform.rotation);
        }*/
        return av;
    }
}
