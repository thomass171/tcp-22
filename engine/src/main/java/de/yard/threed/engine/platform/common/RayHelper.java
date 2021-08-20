package de.yard.threed.engine.platform.common;

import de.yard.threed.core.Dimension;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Transform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCamera;

import de.yard.threed.core.platform.NativeRay;

/**
 * Fuer alle Platformen, die nicht selber einen Picking Ray bilden koennen.
 * <p/>
 * Created by thomass on 07.04.16.
 */
public class RayHelper {
    //Der Mouse Vector bei Click in die Bildschirmitte, laut http://stackoverflow.com/questions/11036106/three-js-projector-and-ray-objects
    public  Vector3 CENTERMOUSEVECTOR;
    Log logger = Platform.getInstance().getLog(RayHelper.class);
    NativeCamera camera;

    public RayHelper(NativeCamera camera) {
        this.camera = camera;
        CENTERMOUSEVECTOR = new Vector3(0,0,0.5f);
    }

    /**
     * Das ist "einfach" das Gegenteil der Projektion.
     *
     * @param vector
     * @return
     */
    Vector3 unprojectVector(Vector3 vector) {
        //Matrix4 unprojectionmatrix = camera.getViewMatrix().getInverse().multiply(camera.getProjectionMatrix().getInverse());
        Matrix4 m1 = MathUtil2.getInverse(camera.getViewMatrix());
        Matrix4 m2 = MathUtil2.getInverse(camera.getProjectionMatrix());
        Matrix4 unprojectionmatrix = m1.multiply(m2);
        //logger.debug("unprojectionmatrix=\n" + unprojectionmatrix.dump("\n"));

        return project(vector, unprojectionmatrix);
    }

    /**
     * Ermittelt aus den relativen Mauskoordinaten den mouseVector im Bereich -1:1 (links oben),-1:1,-1:1.
     * Der MouseVector ist eigentlic ein 2D Vector?
     * y wird als von oben nach oben zaehlend betrachtet! So wie z.B. ThreeJS und OpenGL
     * Die Plattform JME konvertiert die Koordinate, weil  Lwjgl y=0 unten hat.
     * 26.11.14: So ganz exakt scheint mir die Rechnung nicht zu stimmen. Fuer 0 schon, aber der
     * Vollausschlag muss ja WIDTH/HEIGHT -1 sein, und damit kommt man nicht genau auf -1 oder 1.
     * 5.1.16: Jetzt scheint es mir aber fuer den o.a. Wertebereich zu stimmen (Tests u.a. in ReferenceScene).
     * <p/>
     * 8.4.16: Wegen Unity und offenbarer OpenGL Konvention (0,0) links unten jetzt y hier getauscht.
     * <p>
     * Liefert bei Position in der Bildmitte ein (0,0,0.5)
     */
    public Vector3 buildMouseVector(int x, int y, Dimension screedimension) {
        // Das ist gut erklaert in http://stackoverflow.com/questions/11036106/three-js-projector-and-ray-objects
        // ThreeJs verwendet für z -1 und 1 (??). Aber laut stackoverflow ist 0.5 ganz gut.

        Vector3 mouseVector =new Vector3(((float) x / screedimension.width) * 2 - 1,
                /*kein y Tausch 1 - */((float) y / screedimension.height) * 2 - 1,
                0.5f);
        //logger.debug("mouseVector="+mouseVector);
        return mouseVector;
    }

    /**
     * Ermittelt aus den relativen Mauskoordinaten den Picking Ray.
     * Der Origin des Ray ist immer in der Camera.
     * 4.11.19: Bei WebVR kann es bei Firefox sein, dass der reale Viewpoint auch mit disAbled VR(!) 1.7m hoeher ist als die Camera Position ausweist!
     * Das kann man nicht erkennen. Darum lieber den reale Viewpoint mit reingeben.
     */
    public NativeRay buildPickingRay(Transform realViewPosition, int x, int y, Dimension screedimension) {
        Vector3 mouseVector = buildMouseVector(x, y, screedimension);
        return buildPickingRay(realViewPosition, mouseVector);
    }

    public NativeRay buildPickingRay(Transform realViewPosition, Vector3 mouseVector) {

        Vector3 campos = camera.getCarrier().getTransform().getPosition();
        //27.2.17: Die world position muss verwendet werden
        campos = MathUtil2.extractPosition(realViewPosition/*camera.getCarrier().getTransform()*/.getWorldModelMatrix());
        // Code aus http://stackoverflow.com/questions/11036106/three-js-projector-and-ray-objects nachgebaut.
        // Das ist auch irgendwie eingängiger.
        //var raycaster = projector.pickingRay( mouseVector.clone(), camera );

        //  logger.debug("mouseVector vor unproject " + mouseVector.dump(" ") + ", width=" + screedimension.width + ",x=" + x);
        mouseVector = this.unprojectVector(mouseVector);
        //logger.debug("unprojected mouseVector=" + mouseVector);

        // find direction from vector to end
        Vector3 direction = MathUtil2.subtract(mouseVector, campos);
        direction = direction.normalize();
        //logger.debug("direction="+direction);

        // Der Origin ist die Camera Position
        NativeRay pickingray = Platform.getInstance().buildRay(campos, direction);
        //logger.debug("built pickingray=" + pickingray+" for x="+x+",y="+y+", direction ="+direction);

        return pickingray;
    }

    /**
     * Der Algorithmus stammt aus ThreeJS.
     * TODO: Was ist hier der mathematische Unterbau, vor allem wegen des perspective divide?
     * 23.11.14: Und die ist SEHR aehnlich zu Matrix4.transform. Das muss bestimmt zusammengelegt werden.
     * 29.11.15: Ist aber nur ähnlich. Der mathemtische Unterbau ist wichtig. JME macht das (fuer Ray Picking) ähnlich. Vielleicht ergibt sich der Unterschied daraus,
     * ob der Vector in der Camera beginnt oder auf der Leinwand.
     */
    static public Vector3 project(Vector3 v, Matrix4 projectionmatrix) {
        double d = 1 / (projectionmatrix.getElement(3, 0) * v.getX() + projectionmatrix.getElement(3, 1) * v.getY() + projectionmatrix.getElement(3, 2) * v.getZ() + projectionmatrix.getElement(3, 3)); // perspective divide
        double nx = (projectionmatrix.getElement(0, 0) * v.getX() + projectionmatrix.getElement(0, 1) * v.getY() + projectionmatrix.getElement(0, 2) * v.getZ() + projectionmatrix.getElement(0, 3)) * d;
        double ny = (projectionmatrix.getElement(1, 0) * v.getX() + projectionmatrix.getElement(1, 1) * v.getY() + projectionmatrix.getElement(1, 2) * v.getZ() + projectionmatrix.getElement(1, 3)) * d;
        double nz = (projectionmatrix.getElement(2, 0) * v.getX() + projectionmatrix.getElement(2, 1) * v.getY() + projectionmatrix.getElement(2, 2) * v.getZ() + projectionmatrix.getElement(2, 3)) * d;
        return new Vector3(nx, ny, nz);
    }
}
