package de.yard.threed.engine.ecs;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.ModelBuilderRegistry;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.platform.common.RequestType;


/**
 * Connection to a scene server. And back to the client.
 * <p>
 * 28.12.22: Is it really useful to have this as a dedicated system? Shouldn't the platform just extend the event bus?
 * 27.1.23: But some instance is needed to handle events that are not used in monolith mode like entity change events.
 * But no base network operation (listen, socket) here (See {@link ClientBusConnector}).
 * <p>
 * To some degree this is the counterpart of {@link ServerSystem).
 * <p>
 * Created by thomass on 16.02.21.
 */
public class ClientSystem extends DefaultEcsSystem {
    static Log logger = Platform.getInstance().getLog(ClientSystem.class);
    public static String TAG = "ClientSystem";
    private boolean clientsystemdebuglog = false;
    private ModelBuilderRegistry[] modelBuilderRegistries;

    public ClientSystem(ModelBuilderRegistry[] modelBuilderRegistries) {
        // no component relation, no "updatepergroup"
        super(new String[]{}, new RequestType[]{}, new EventType[]{BaseEventRegistry.EVENT_ENTITYSTATE});
        this.modelBuilderRegistries = modelBuilderRegistries;
    }

    @Override
    public void process(Event evt) {

        if (clientsystemdebuglog) {
            logger.debug("got event " + evt.getType());
        }

        if (evt.getType().equals(BaseEventRegistry.EVENT_ENTITYSTATE)) {
            Payload payload = evt.getPayload();
            Integer entityid = (Integer) payload.get("entityid");
            String buildername = (String) payload.get("buildername");
            Vector3 position = (Vector3) payload.get("position");
            Vector3 scale = (Vector3) payload.get("scale");
            Quaternion rotation = (Quaternion) payload.get("rotation");

            EcsEntity entity = EcsHelper.findEntityById(entityid);
            if (entity == null) {
                logger.debug("Building entity " + entityid + " with builder '" + buildername + "'");
                entity = new EcsEntity(entityid);
                if (buildername != null) {
                    if (entity.getSceneNode() == null) {
                        entity.buildSceneNodeByModelFactory(buildername, modelBuilderRegistries);
                    }
                }
            }
            SceneNode sceneNode = entity.getSceneNode();
            if (sceneNode != null && position != null) {
                // transform data is not sent in the early life phase of an entity. Maybe even never.
                Transform t = sceneNode.getTransform();
                t.setPosition(position);
                t.setRotation(rotation);
                t.setScale(scale);
            }
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }
}
