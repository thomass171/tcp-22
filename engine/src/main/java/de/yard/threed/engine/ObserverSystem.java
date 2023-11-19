package de.yard.threed.engine;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.platform.common.RequestType;
import de.yard.threed.engine.vr.VrInstance;

/**
 * System for
 * 1) cursor key view direction changing of an entity with a {@link ObserverComponent}. Typically used in travelling but not in maze.
 * But not for x/y/z adjusting, which is done in {@link Observer}.
 * 2) attach the observer after joining (was in AvatarSystem before)
 * <p>
 * Not for teleporting, movement or viewports, which is something different.
 * 1.9.23: Should not host observer instance because that is not related to a specific entity.
 * <p>
 * Created by thomass on 16.09.16.
 */
public class ObserverSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(ObserverSystem.class);
    public static String TAG = "ObserverSystem";
    // workaround for attaching the observer only once to the main/first player, but not to further bots.
    private boolean isFirstJoin = true;
    // offset to attachment (player,avatar etc)
    private LocalTransform viewTransform;
    public boolean withComponent = false;

    /**
     *
     */
    public ObserverSystem(boolean withComponent) {
        super(new String[]{"ObserverComponent"}, new RequestType[]{}, new EventType[]{BaseEventRegistry.EVENT_USER_ASSEMBLED/*UserSystem.USER_EVENT_JOINED*/});
        this.withComponent = withComponent;
    }

    /**
     * Default constructor setting up for not adding a ObserverComponent to a new user.
     */
    public ObserverSystem() {
        this(false);
    }

    @Override
    public void update(EcsEntity entity, EcsGroup group, double tpf) {
        ObserverComponent oc = (ObserverComponent) group.cl.get(0);

        if (!Input.getKey(KeyCode.Shift)) {

            if (Input.getKey(KeyCode.RightArrow)) {
                oc.incHeading(-tpf);
            }
            if (Input.getKey(KeyCode.LeftArrow)) {
                oc.incHeading(tpf);
            }
            if (Input.getKey(KeyCode.UpArrow)) {
                oc.incPitch(tpf);
            }
            if (Input.getKey(KeyCode.DownArrow)) {
                oc.incPitch(-tpf);
            }
        }
    }

    @Override
    public void process(Event evt) {

        logger.debug("got event " + evt.getType());

        if (evt.getType().equals(BaseEventRegistry.EVENT_USER_ASSEMBLED)) {
            int userEntityId = (int) ((Integer) evt.getPayload().get("userentityid"));
            EcsEntity userEntity = EcsHelper.findEntityById(userEntityId);

            if (userEntity == null || userEntity.getSceneNode() == null) {
                logger.warn("cannot attach observer because no user entity with scene node found");
                // in client/server mode avatar might not yet been build? Recycle event?
                // Recycle is no good idea. Might be processed double and again sent to server.
            } else {
                boolean forLogin = true;//TODO check purpose
                if (attachObserver(forLogin, isFirstJoin, userEntity, viewTransform)) {
                    isFirstJoin = false;
                }

                //31.10.23: The following from TrafficWorldSystem. But its a good location here to add ObserverComponent
                //21.10.19 optional
                //das stammt aus railing
                //if (isRailing) {
                Transform slave = null;
                    /*31.10.23: nearview probably needs some refactoring
                    if (enableNearView) {
                        nearView = new NearView(Scene.getCurrent().getDefaultCamera(), 0.01, 20, Scene.getCurrent());
                        //damit man erkennt, ob alles an home attached ist weg von (0,0,0) und etwas höher
                        nearView.setPosition(new Vector3(-30, 10, -10));
                    }

                    //Camera bekommt auch ein ProxyTransform

                    if (nearView != null) {
                        //wirklich??slave = nearView.getCamera().getCarrier().getTransform();
                    }*/
                if (withComponent) {
                    // cannot be done always. In maze eg. its not needed and breaks Observer.
                    ObserverComponent oc = new ObserverComponent(new ProxyTransform(Scene.getCurrent().getDefaultCamera().getCarrier().getTransform().transform, slave));
                    oc.setRotationSpeed(40);
                    UserSystem.getInitialUser().addComponent(oc);
                }

            }
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public void setViewTransform(LocalTransform viewTransform) {
        this.viewTransform = viewTransform;
    }

    /**
     * A user joined.
     * Moved here from AvatarSystem.
     */
    private static boolean attachObserver(boolean forLogin, boolean isFirstJoin, EcsEntity userEntity, LocalTransform viewTransform) {
        // Attach the oberver to the avatar. Is the connection to observer good located here?
        // 19.11.21: Should be independant from ObserverComponent? Probably. If there is an oberver, attach it to avatar
        // This is also reached for bot and MP joining.
        // 14.2.22 Attach observer independent from VR. But only to the first player (for now)
        if ((boolean) forLogin && Observer.getInstance() != null && isFirstJoin) {
            SceneNode avatarNode = userEntity.getSceneNode();
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
            //isFirstJoin = false;
            return true;
        }
        return false;//isFirstJoin;
    }

}
