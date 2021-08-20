package de.yard.threed.maze;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.EcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;

/**
 * TODO: merge nach ItemComponent?
 * Created by thomass on 08.04.21.
 */
public class BulletComponent extends EcsComponent {
    //1=flying
    //2=falling after hit
    //3=lying around
    public int state = 0;
    // player firing the bullet
    public String origin;
    Log logger = Platform.getInstance().getLog(BulletComponent.class);
    // Der Speed muss zur Skalierung der Szene passen. Abhaengig davon kann 10 zu
    // schnell oder zu langsam sein.
    private float movementSpeed = 10.0f; //move 10 units per getSecond
    private Vector3 direction;
    public static boolean debugmovement = false;
    static String TAG = "BulletComponent";

    public BulletComponent(Direction direction, String origin) {
        launchBullet(direction,origin);
    }

    public BulletComponent() {

        state = 0;
    }

    public void launchBullet(Direction direction, String origin) {
        this.direction = MazeUtils.direction2Vector3(direction);
        this.origin = origin;
        state = 1;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static BulletComponent getBulletComponent(EcsEntity e) {
        BulletComponent m = (BulletComponent) e.getComponent(BulletComponent.TAG);
        return m;
    }

    public Vector3 getOffset(double tpf) {
        return direction.multiply(tpf * movementSpeed);
    }
}
