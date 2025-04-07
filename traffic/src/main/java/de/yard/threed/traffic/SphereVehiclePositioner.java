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
    Quaternion baseRotation;

    public SphereVehiclePositioner(Vector3 position,  Quaternion baseRotation) {
        this.position = position;
        this.baseRotation=baseRotation;

    }

    /*public SphereVehiclePositioner(LatLon latLon) {
        SphereProjections projections = TrafficHelper.getProjectionByDataprovider(null);
        Vector2 projectedPosition = projections.projection.project(latLon);
    }*/

    /**
     *
     */
    @Override
    public void positionVehicle(EcsEntity vehicle) {
        SceneNode sceneNode = vehicle.getSceneNode();
        sceneNode.getTransform().setPosition(position);

        // This is the 'default rotation fits' way, at least for vehicles like 'loc'.
        Quaternion vehicleRotation = new Quaternion();//Quaternion.buildRotationX(new Degree(90));

        sceneNode.getTransform().setRotation(baseRotation);//new Quaternion());//baseRotation.multiply(vehicleRotation));

        FreeFlyingComponent bmc = new FreeFlyingComponent(sceneNode.getTransform());
        vehicle.addComponent(bmc);

       // sphereTransform.getTransform().setRotation(baseRotation);
       // sceneNode.getTransform().setParent(sphereTransform.getTransform());

    }

    @Override
    public SceneNode getDestinationNode() {
        //return sphereTransform;
        return SphereSystem.getSphereNode();
    }
}
