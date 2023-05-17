package de.yard.threed.maze;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.EcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;

/**
 * A bullet is a special case of an item.
 * <p>
 * Created by thomass on 08.04.21.
 */
public class BulletComponent extends ItemComponent {
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
    // Only orthogonal directions make things easier for now
    private Direction direction;
    private Vector3 vdirection;
    static String TAG = "BulletComponent";

    public BulletComponent(int owner) {
        super(owner);
        state = 0;
        // a new bullet related to an owner should initially be hidden because its in the inventory of the owner.
        // But hide() is not yet available. So its up to the creatr to hide it.
    }

    public void launchBullet(Direction direction, String origin) {
        if (origin == null) {
            throw new RuntimeException("origin must not be null");
        }
        logger.debug("Launching bullet");
        this.direction = direction;
        this.vdirection = MazeUtils.direction2Vector3(direction);
        this.origin = origin;
        state = 1;
        setOwner(-1);
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
        return vdirection.multiply(tpf * movementSpeed);
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isFlying() {
        return state == 1;
    }

    public boolean isOnGround() {
        return state == 3;
    }

    public void locateToGround(Point p) {
        state = 3;
        setLocation(p);
    }
}
