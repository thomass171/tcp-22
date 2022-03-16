package de.yard.threed.maze;

import de.yard.threed.core.EventType;
import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.*;

import java.util.List;

/**
 * <p>
 * Created by thomass on 09.04.21.
 */

public class BulletSystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(BulletSystem.class);

    boolean bulletsystemdebuglog = false;

    /**
     *
     */
    public BulletSystem() {
        super(new String[]{BulletComponent.TAG}, new RequestType[]{RequestRegistry.TRIGGER_REQUEST_FIRE}, new EventType[]{});
    }

    @Override
    public void update(EcsEntity entity, EcsGroup group, double tpf) {
        BulletComponent bc = BulletComponent.getBulletComponent(entity);
        Vector3 offset = new Vector3();
        Grid grid = Grid.getInstance();
        SceneNode ball = entity.getSceneNode();

        switch (bc.state) {
            case 1:
                offset = bc.getOffset(tpf);
                moveForward(ball, offset);
                checkCollision(entity, bc, grid.getMazeLayout(), MazeUtils.getPlayerOrBoxes(false));
                break;

        }


        if (bulletsystemdebuglog) {
            logger.debug("ball position= " + ball.getTransform().getPosition() + ", offset=" + offset + ", tpf=" + tpf);
        }
    }

    @Override
    public boolean processRequest(Request request) {
        if (bulletsystemdebuglog) {
            logger.debug("got request " + request.getType());
        }
        if (request.isType(RequestRegistry.TRIGGER_REQUEST_FIRE)) {

            EcsEntity player = EcsHelper.findEntityById((int) request.getUserEntityId());
            MoverComponent mv = MoverComponent.getMoverComponent(player);
            List<EcsEntity> bullets = MazeUtils.getBullets(player);

            //InventoryComponent ic = InventoryComponent.getInventoryComponent(player);
            BulletComponent bc;
            if ((bc = pickBullet(bullets, mv.getLocation())) != null) {

                //SceneNode ball = buildSimpleBall(0.3, Color.YELLOW, mv.getLocation());
                //EcsEntity e = new EcsEntity(ball);
                //BulletComponent bulletComponent = new BulletComponent(mv.getGridOrientation().getDirectionForMovement(GridMovement.Forward), playername);
                bc.launchBullet(mv.getGridOrientation().getDirectionForMovement(GridMovement.Forward), player.getName());
                //e.addComponent(bulletComponent);
            }
            //das event kann ich mir sparen. Ic muss ja es ins inventory sehen.
            //SystemManager.sendEvent(new Event(EventRegistry.EVENT_BULLET_FIRED, new Payload(playername)));
            return true;
        }
        return false;
    }

    private BulletComponent pickBullet(List<EcsEntity> bullets, Point startLocation) {
        if (bullets.size() > 0) {
            BulletComponent bc = BulletComponent.getBulletComponent(bullets.get(0));
            ItemComponent ic = ItemComponent.getItemComponent(bullets.get(0));
            //bc.bulletCount--;
            //ic.needsRefresh = true;
            ic.collectedBy(-1);
            bullets.get(0).scenenode.getTransform().setPosition(MazeUtils.point2Vector3(startLocation).add(new Vector3(0, 1.25, 0)));
            return bc;
        }
        return null;
    }

    private void moveForward(SceneNode ball, Vector3 offset) {
        Vector3 loc = ball.getTransform().getPosition();
        Vector3 testloc = new Vector3(loc.getX(), loc.getY(), loc.getZ());
        //Vector3 rot = Matrix4.buildRotationMatrix(getRotation()).getColumn(2);
        //Vector3 rot = new Vector3(direction.getX(), 0, direction.getZ()).normalize();

        //testloc = testloc.add(rot.multiply(singlestep));
        if (true /*!world.detectCollision(testloc*/) {
            loc = loc.add(offset);
            ball.getTransform().setPosition(loc);
        }
    }

    private void checkCollision(EcsEntity bullet, BulletComponent bc, MazeLayout layout, List<EcsEntity> players) {
        SceneNode ball = bullet.getSceneNode();
        Vector3 loc = ball.getTransform().getPosition();
        Point ballLocation = MazeUtils.vector2Point(loc);
        //TODO check for skipped fields by large tpf
        if (layout.isWallAt(ballLocation)) {
            bc.state = 2;
            // lay down the ball on the field in front of the wall
            locateToGround(bullet, ballLocation.add(bc.getDirection().getReverted().getPoint()));

        }
        for (EcsEntity player : players) {
            MoverComponent mc = MoverComponent.getMoverComponent(player);
            // don't hit myself
            if (!player.getName().equals(bc.origin)) {
                if (mc.getLocation().equals(ballLocation)) {
                    logger.debug("Hit detected of '" + player.getName() + "' with bullet by '" + bc.origin + "'");
                    bc.state = 2;

                    Point p = new Point(2, 2);
                    SystemManager.putRequest(RequestRegistry.buildRelocate(player.getName(), p, null));
                    locateToGround(bullet, ballLocation);

                }
            }
        }
    }

    /**
     * Put the ball to the center of a field (half in ground), so it is ready to be picked again.
     */
    private void locateToGround(EcsEntity bullet, Point p) {
        SceneNode ball = bullet.getSceneNode();
        BulletComponent bc = BulletComponent.getBulletComponent(bullet);

        ball.getTransform().setPosition(MazeUtils.point2Vector3(p));

        bc.locateToGround(p);
    }
}
