package de.yard.threed.engine;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Util;
import de.yard.threed.engine.ecs.EcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;

/**
 * A component for an entity with cursor key view direction changing (See {@link ObserverSystem}).
 * Typically used in travelling but not in maze.
 * Not for teleporting or viewports, which is something different.
 * <p>
 * 20.10.17: UpVector needed? See also {@link FirstPersonController}.
 * <p>
 * Created by thomass on 24.11.16.
 */
public class ObserverComponent extends EcsComponent {

    protected Observer observer;
    protected float rotationSpeed = 20.0f;
    static String TAG = "ObserverComponent";

    /**
     * Der observer kann eine Camera sein, aber auch ein Kopfobjekt mit attachter Camera, das sich dann dreht.
     * Oder eine ProxyNode.
     * <p>
     * 25.10.21: What is the exact purpose of this component relating to Observer? Connecting Observer to ECS? Can it move?
     */
    public ObserverComponent(Transform observer) {

        this.observer = Observer.buildForTransform(observer);
    }

    public static ObserverComponent buildForCamera(Camera camera) {
        return new ObserverComponent(camera.getCarrier().getTransform());
    }

    public static ObserverComponent buildForDefaultCamera() {
        return buildForCamera(Scene.getCurrent().getDefaultCamera());
    }

    public void incHeading(double deltatime/*Degree inc*/) {
        //MA35FirstPersonController.incHeading(observer, new Degree(rotationSpeed * deltatime), z0);
        observer.incHeading(new Degree(rotationSpeed * deltatime));
    }

    public void incPitch(double deltatime/*Degree inc*/) {
        //FirstPersonController.incPitch(observer, new Degree(rotationSpeed * deltatime));
        observer.incPitch(new Degree(rotationSpeed * deltatime));
    }

    public void setRotationSpeed(int rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static ObserverComponent getObserverComponent(EcsEntity e) {
        ObserverComponent oc = (ObserverComponent) e.getComponent(ObserverComponent.TAG);
        return oc;
    }
}
