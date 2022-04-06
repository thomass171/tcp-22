package de.yard.threed.maze;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.engine.ecs.UserComponent;

import java.util.ArrayList;
import java.util.List;

public class MazeUtils {

    static Log logger = Platform.getInstance().getLog(MazeUtils.class);

    /**
     * player in terms of maze is an entity having a 'Mover', but not a movable box.
     * Currently, every mover not being a player is a box.*
     */
    public static List<EcsEntity> getPlayerOrBoxes(boolean forBoxes) {
        return SystemManager.findEntities(e -> {
            MoverComponent moverComponent = MoverComponent.getMoverComponent(e);
            if (moverComponent == null) {
                return false;
            }
            if (moverComponent.isPlayer() && !forBoxes) {
                return true;
            }
            if (!moverComponent.isPlayer() && forBoxes) {
                return true;
            }
            return false;
        });
    }

    public static List<EcsEntity> getBoxes() {
        return getPlayerOrBoxes(true);
    }

    public static List<EcsEntity> getAllItems() {
        return getItemsByOwner(-1);
    }

    public static List<EcsEntity> getItems(EcsEntity player) {
        return getItemsByOwner(player.getId());
    }

    private static List<EcsEntity> getItemsByOwner(int owner) {
        return SystemManager.findEntities(e -> {

            DiamondComponent diamondComponent = DiamondComponent.getDiamondComponent(e);
            if (diamondComponent != null && (owner == -1 || owner == diamondComponent.getOwner())) {
                return true;
            }
            BulletComponent bulletComponent = BulletComponent.getBulletComponent(e);
            if (bulletComponent != null && (owner == -1 || owner == bulletComponent.getOwner())) {
                return true;
            }
            return false;
        });
    }

    /**
     * Find items and bullets.
     */
    public static List<EcsEntity> getItemsByField(Point field) {
        return SystemManager.findEntities(e -> {

            DiamondComponent diamondComponent = DiamondComponent.getDiamondComponent(e);
            if (diamondComponent != null && diamondComponent.getLocation() != null && field.equals(diamondComponent.getLocation())) {
                return true;
            }
            BulletComponent bulletComponent = BulletComponent.getBulletComponent(e);
            if (bulletComponent != null && bulletComponent.getLocation() != null && field.equals(bulletComponent.getLocation())) {
                return true;
            }
            return false;
        });
    }

    public static EcsEntity getMainPlayer() {
        List<EcsEntity> players = getPlayerOrBoxes(false);
        for (EcsEntity player : players) {
            if (!"Bot".equals(player.getName())) {
                return player;
            }
        }
        return null;
    }

    public static EcsEntity findPlayerByName(String name) {
        List<EcsEntity> players = getPlayerOrBoxes(false);
        for (EcsEntity player : players) {
            if (name.equals(player.getName())) {
                return player;
            }
        }
        return null;
    }

    /**
     * There can be only one.
     */
    public static EcsEntity findBoxByField(Point field) {
        List<EcsEntity> boxes = getPlayerOrBoxes(true);
        for (EcsEntity box : boxes) {
            MoverComponent mc = MoverComponent.getMoverComponent(box);
            if (mc.getLocation().equals(field)) {
                return box;
            }
        }
        return null;
    }

    /**
     * For player and boxes.
     */
    public static Point getMoverposition(EcsEntity mover) {
        if (mover == null) {
            return null;
        }
        MoverComponent mc = MoverComponent.getMoverComponent(mover);
        return mc.getLocation();
    }

    public static GridOrientation getPlayerorientation(EcsEntity player) {
        if (player == null) {
            return null;
        }
        MoverComponent mc = MoverComponent.getMoverComponent(player);
        return mc.getGridOrientation();
    }

    public static List<Point> getBoxLocations() {
        List<Point> locations = new ArrayList<Point>();
        for (EcsEntity box : getPlayerOrBoxes(true)) {
            MoverComponent mc = MoverComponent.getMoverComponent(box);
            locations.add(mc.getLocation());
        }
        return locations;
    }

    public static Vector3 point2Vector3(Point p) {
        return MazeDimensions.getWorldElementCoordinates(p.getX(), p.getY());
    }

    public static Point vector2Point(Vector3 v) {
        return MazeDimensions.getCoordinatesOfElement(v);
    }

    public static Vector3 direction2Vector3(Direction p) {
        return point2Vector3(p.getPoint()).normalize();
    }


    public static GridMover getMoverFromListAtLocation(List<GridMover> movers, Point location) {

        //logger.debug("gridposition=" + gridposition + ",direction=" + direction + ",yaw=" + /*yaw*/0);
        for (GridMover mover : movers) {
            if (mover.getLocation().equals(location)) {
                return mover;
            }
        }
        return null;
    }

    public static int distance(Point p1, Point p2) {
        return Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY());
    }

    public static GridState buildGridStateFromEcs() {
        List<GridMover> players = new ArrayList<GridMover>();
        for (EcsEntity player : MazeUtils.getPlayerOrBoxes(false)) {
            players.add(MoverComponent.getMoverComponent(player).getGridMover());
        }
        List<GridMover> boxes = buildBoxesFromEcs();
        GridState state = new GridState(players, boxes, buildItemsFromEcs());
        return state;
    }

    public static List<GridMover> buildBoxesFromEcs() {
        List<GridMover> boxes = new ArrayList<GridMover>();
        for (EcsEntity player : MazeUtils.getPlayerOrBoxes(true)) {
            boxes.add(MoverComponent.getMoverComponent(player).getGridMover());
        }
        return boxes;
    }

    public static List<GridItem> buildItemsFromEcs() {
        List<GridItem> items = new ArrayList<GridItem>();
        for (EcsEntity e : MazeUtils.getItemsByOwner(-1)) {
            items.add(getItemComponent(e));
        }
        return items;
    }

    //TODO nur Components liefern? Alle Items sind enditities?->DVK
    public static List<EcsEntity> getInventory(int owner) {
        return MazeUtils.getItemsByOwner(owner);
    }

    public static List<EcsEntity> getInventory(EcsEntity player) {
        //MoverComponent mc = MoverComponent.getMoverComponent(player);
        return getInventory(player.getId());
    }

    public static List<EcsEntity> getBullets(EcsEntity player) {
        List<EcsEntity> inventory = getInventory(player);
        return EcsEntity.filterList(inventory, e -> e.getComponent(BulletComponent.TAG) != null);
    }

    public static List<EcsEntity> getDiamonds(EcsEntity player) {
        List<EcsEntity> inventory = getInventory(player);
        return EcsEntity.filterList(inventory, e -> e.getComponent(DiamondComponent.TAG) != null);
    }

    public static List<EcsEntity> getDiamonds(int player) {
        List<EcsEntity> inventory = getInventory(player);
        return EcsEntity.filterList(inventory, e -> e.getComponent(DiamondComponent.TAG) != null);
    }

    public static String readMazefile(String filename/*, String levelname*/) {
        Bundle mazebundle = BundleRegistry.getBundle("maze");
        BundleData bundleData = mazebundle.getResource(filename);
        if (bundleData == null) {
            logger.error("maze file not found:" + filename);
            return null;
        }
        return bundleData.getContentAsString();
    }

    public static boolean playerHasBullets() {
        EcsEntity player = getMainPlayer();
        if (player != null) {
            return getBullets(player).size() > 0;
        }
        return false;
    }

    /**
     * Will only find logged in users (no bot).
     */
    public static EcsEntity getPlayerByUsername(String username) {
        // find user entity by username in UserComponent
        List<EcsEntity> candidates = SystemManager.findEntities((e) -> {
            UserComponent userComponent = UserComponent.getUserComponent(e);
            if (userComponent != null && userComponent.getUsername().equals(username)) {
                return true;
            }
            return false;
        });
        if (candidates.size() == 0) {
            return null;
        }
        if (candidates.size() > 1) {
            logger.warn("inconsistency: Multiple username " + username);
        }
        return candidates.get(0);
    }

    public static ItemComponent getItemComponent(EcsEntity e) {
        ItemComponent component = DiamondComponent.getDiamondComponent(e);
        if (component == null) {
            // then it must be a bullet
            component = BulletComponent.getBulletComponent(e);
        }
        return component;
    }

    /**
     * Return the mover if any is moving. null if nobody moves.
     */
    public static GridMover isAnyMoving() {

        MoverComponent mover;
        for (EcsEntity e : MazeUtils.getPlayerOrBoxes(false)) {
            mover = MoverComponent.getMoverComponent(e);
            if (mover.isMoving()) {
                return mover;
            }
        }

        for (EcsEntity e : MazeUtils.getPlayerOrBoxes(true)) {
            mover = MoverComponent.getMoverComponent(e);
            if (mover.isMoving()) {
                return mover;
            }
        }
        return null;
    }

    public static List<EcsEntity> getPlayer() {
        return getPlayerOrBoxes(false);
    }

    /*not needed for now public static void setItemCollectedByPlayer(EcsEntity e, EcsEntity user) {
        ItemComponent ic = getItemComponent(e);
        ic.collectedBy(user.getId());
    }*/
}
