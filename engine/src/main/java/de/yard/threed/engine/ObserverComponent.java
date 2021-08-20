package de.yard.threed.engine;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Util;
import de.yard.threed.engine.ecs.EcsComponent;


/**
 * Eine Component für einen Observer (Node mit attached camera oder nur camera).
 * 5.4.17: deprecated zugunsten ViewpointComponent oder zusammenlegen?. Teleport ist was anderes, da muss man schon genauer unterscheiden.
 * 20.10.17: Brauchts nicht einen UpVector? Siehe auch {@link FirstPersonController}.
 * <p>
 * Created by thomass on 24.11.16.
 */

public class ObserverComponent extends EcsComponent {
    //MA35 protected Transform observer;
    protected Observer observer;
    protected float rotationSpeed = 20.0f;
    static String TAG = "ObserverComponent";
    public boolean z0 = false;

    /**
     * Der observer kann eine Camera sein, aber auch ein Kopfobjekt mit attachter Camera, das sich dann dreht.
     * Oder eine ProxyNode.
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
        observer.incHeading(new Degree(rotationSpeed * deltatime), z0);
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

}
