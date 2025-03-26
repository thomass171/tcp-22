package de.yard.threed.traffic;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;

/**
 * For vehicles not bound to a graph.
 * Spheres can be 2D or 3D.
 */
public class SphereVehiclePositioner implements VehiclePositioner {
    Vector3 position;
    // the node for transforming the 'vehicle move space' to sphere space.
   // SceneNode sphereTransform;

    public SphereVehiclePositioner(Vector3 position) {
        this.position = position;
       /* sphereTransform = new SceneNode();
        sphereTransform.setName("sphereTransform");
        sphereTransform.getTransform().setParent(SphereSystem.getSphereNode().getTransform());*/
    }

    /**
     *
     */
    @Override
    public void positionVehicle(EcsEntity vehicle) {
        SceneNode sceneNode = vehicle.getSceneNode();
        sceneNode.getTransform().setPosition(position);
        // TODO get correct rotation. X90 is for 'mobi' in 'wayland'.
        Quaternion baseRotation = Quaternion.buildRotationX(new Degree(90));
        //baseRotation = Quaternion.buildRotationY(new Degree(-90));
        //baseRotation = Quaternion.buildFromAngles(new Degree(-90),new Degree(180),new Degree(90));
        //baseRotation = Quaternion.buildFromAngles(new Degree(-90),new Degree(0),new Degree(0));
        //sceneNode.getTransform().setRotation(baseRotation);

        // quick hack for moving mobi. TODO improve
        FreeFlyingComponent bmc = new FreeFlyingComponent(sceneNode.getTransform(), baseRotation);
        vehicle.addComponent(bmc);
        //FirstPersonMovingSystem firstPersonMovingSystem= (FirstPersonMovingSystem) SystemManager.findSystem(FirstPersonMovingSystem.TAG);

       // sphereTransform.getTransform().setRotation(baseRotation);
       // sceneNode.getTransform().setParent(sphereTransform.getTransform());

    }

    @Override
    public SceneNode getDestinationNode() {
        //return sphereTransform;
        return SphereSystem.getSphereNode();
    }
}
