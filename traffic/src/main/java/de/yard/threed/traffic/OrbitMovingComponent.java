package de.yard.threed.traffic;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.ecs.EcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.graph.GraphSelector;

/**
 * Eine Component, die sich in einem Orbit bewegt bzw. daran gebunden/fixiert ist.
 * Für Satelliten, Monde,Planeten (Spheres),Vehicle.
 *
 * Auf die velocity hat des keinen Einfluss.
 * Analog GraphMovingComponent.
 *
 * Kann unterschiedlich beschrieben werden, Z.B. als Ebene durch Zentrum mit einem Radius.
 * Oder mit zwei Winkeln und Radius
 * Oder als Grosskreis.
 * Oder mit zwei Vektoren e1 und e2, die einen Kreis aufspannen. Ist vielleicht am geeignetsten. Zusammen mit der Normele rotiert e1 dann einfach um die Normale.
 * Das scheint schick.
 * <p>
 * <p>
 * Created by thomass on 18.10.19.
 *
 * @Deprecated 4.4.20: das ist doch eine Variante GraphMovingComponent
 */
public class OrbitMovingComponent extends EcsComponent {
    static Log logger = Platform.getInstance().getLog(OrbitMovingComponent.class);
    public static String TAG = "OrbitMovingComponent";
    private Transform mover;
    private GraphSelector selector;

    /**
     *
     * @param mover
     */
    @Deprecated
    public OrbitMovingComponent(Transform mover) {
        this.mover = mover;

    }

    @Override
    public String getTag() {
        return TAG;
    }


    /**
     * Die Position vorsetzen. Positiver Wert vorwaerte, negativer rückwärts. Dabei ist die Richtung
     *
     * @param amount
     */
    public void moveForward(double amount) {

    }

    public static OrbitMovingComponent getOrbitMovingComponent(EcsEntity e) {
        OrbitMovingComponent gmc = (OrbitMovingComponent) e.getComponent(OrbitMovingComponent.TAG);
        return gmc;
    }





    public void setPosRot(LocalTransform posRot) {
        {
            mover.setPosRot(posRot);
        }
    }

    public Vector3 getPosition() {
       {
            return mover.getPosition();
        }
    }
    
}