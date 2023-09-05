package de.yard.threed.engine.ecs;


import de.yard.threed.core.Degree;
import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Point;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.BaseEventRegistry;
import de.yard.threed.engine.BaseRequestRegistry;
import de.yard.threed.engine.FirstPersonController;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;

/**
 * <p>
 * Created by thomass on 26.08.23.
 */
public class FirstPersonMovingSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(FirstPersonMovingSystem.class);
    public static String TAG = "FirstPersonMovingSystem";
    boolean firstpersonmovingsystemdebuglog = true;

    /**
     *
     */
    private FirstPersonMovingSystem() {
        super(new String[]{FirstPersonMovingComponent.TAG}, new RequestType[]{BaseRequestRegistry.TRIGGER_REQUEST_FORWARD},
                new EventType[]{BaseEventRegistry.EVENT_USER_ASSEMBLED});
    }

    public static FirstPersonMovingSystem buildFromConfiguration() {
        return new FirstPersonMovingSystem();
    }

    /**
     * @param group
     */
    @Override
    public void init(EcsGroup group) {
        if (group != null) {
            FirstPersonMovingComponent gmc = (FirstPersonMovingComponent) group.cl.get(0);
        }
    }
static    Point lastpoint;
    /**
     *
     */
    @Override
    final public void update(EcsEntity entity, EcsGroup group, double tpf) {

        FirstPersonMovingComponent tmc = (FirstPersonMovingComponent) group.cl.get(0);

        if (tmc.hasAutomove()) {
            //moveForward(entity, gmc, vc, tpf * vc.movementSpeed);
            //logger.debug("new position of "+entity.getName()+entity.getId()+" isType "+gmc.getPosition());
        }

        Point point = Input.getMouseMove();
        if (point !=null) {
            if (lastpoint!=null) {

                logger.debug("mouse move " + point);
int x=point.getX()-lastpoint.getX();
int y=point.getY()-lastpoint.getY();
                Matrix4 m4 = entity.getSceneNode().getTransform().getLocalModelMatrix();

                Vector3 refVector = new Vector3(0, 1, 0);
                Quaternion q = m4.extractQuaternion();
                Degree d=new Degree(0);
                if (x < 0){
                 d=new Degree(1);
                }
                if (x > 0){
                    d=new Degree(-1);
                }
                Degree dy=new Degree(0);
                if (y < 0){
                    dy=new Degree(-1);
                }
                if (y > 0){
                    dy=new Degree(1);
                }

                m4 = m4.multiply(Matrix4.buildRotationMatrix(
                        Quaternion.buildRotationY(d).multiply(Quaternion.buildRotationX(dy))));
                //entity.getSceneNode().getTransform().setPosition(m4.extractPosition());
                entity.getSceneNode().getTransform().setRotation(m4.extractQuaternion());
            }
            lastpoint=point;
        }
    }


    @Override
    public boolean processRequest(Request request) {
        if (firstpersonmovingsystemdebuglog) {
            logger.debug("got request " + request.getType());
        }
        if (request.isType(BaseRequestRegistry.TRIGGER_REQUEST_FORWARD)) {
            int userEntityId = (int) request.getUserEntityId();

            EcsEntity userEntity = EcsHelper.findEntityById(userEntityId);
            FirstPersonMovingComponent fpmc = FirstPersonMovingComponent.getFirstPersonMovingComponent(userEntity);
            //FirstPersonController.moveForward(userEntity.getSceneNode().getTransform(), 0.2);

            Matrix4 m4 = userEntity.getSceneNode().getTransform().getLocalModelMatrix();

            Vector3 refVector = new Vector3(0,0,-1);
            m4 = m4.multiply(Matrix4.buildTransformationMatrix(refVector,m4.extractQuaternion()));
            //userEntity.getSceneNode().getTransform().setPosition(m4.extractPosition());
            //userEntity.getSceneNode().getTransform().setRotation(m4.extractQuaternion());
fpmc.moveForward(1);
          //  movedirection = orientation.forward +
            return true;
        }
        return false;
    }

    @Override
    public void process(Event evt) {
        if (firstpersonmovingsystemdebuglog) {
            logger.debug("got event " + evt.getType());
        }
        if (evt.getType().equals(BaseEventRegistry.EVENT_USER_ASSEMBLED)) {
            int userEntityId = (int) ((Integer) evt.getPayload().get("userentityid"));
            EcsEntity userEntity = EcsHelper.findEntityById(userEntityId);

            userEntity.addComponent(new FirstPersonMovingComponent(userEntity.getSceneNode().getTransform()));
        }
    }
}
