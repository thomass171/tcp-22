package de.yard.threed.maze;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.ecs.ComponentFilter;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.engine.ecs.UserComponent;
import de.yard.threed.engine.ecs.UserSystem;

import java.util.ArrayList;
import java.util.List;

public class MazeUtils {

    static Log logger = Platform.getInstance().getLog(MazeUtils.class);

    public static List<EcsEntity> getBoxes() {
        return SystemManager.findEntities(new MoverFilter(true));
    }

    public static List<EcsEntity> getItems() {
        return SystemManager.findEntities(new ItemFilter());
    }

    /**
     * player in terms of maze is an entity having a 'Mover', but not a movable box.
     *
     * @return
     */
    public static List<EcsEntity> getPlayer() {
        return SystemManager.findEntities(new MoverFilter(false));
        // currently assuem all users joined
        //return UserSystem.getAllUser();
    }

    public static EcsEntity getMainPlayer() {
        List<EcsEntity> players = getPlayer();
        for (EcsEntity player : players) {
            if (!"Bot".equals(player.getName())) {
                return player;
            }
        }
        return null;
    }

    public static EcsEntity findPlayerByName(String name) {
        List<EcsEntity> players = getPlayer();
        for (EcsEntity player : players) {
            if (name.equals(player.getName())) {
                return player;
            }
        }
        return null;
    }

    public static Point getPlayerposition(EcsEntity player ) {
        if (player == null) {
            return null;
        }
        MoverComponent mc = MoverComponent.getMoverComponent(player);
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
        for (EcsEntity box : getBoxes()) {
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


    public static GridMover/*Point*/ isBoxAtLocation(List<GridMover> boxes, Point location) {

        //logger.debug("gridposition=" + gridposition + ",direction=" + direction + ",yaw=" + /*yaw*/0);
        for (GridMover box : boxes) {
            if (box.getLocation().equals(location)) {
                return box;//destination;
            }
        }
        return null;
    }

    public static int distance(Point p1, Point p2) {
        return Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY());
    }

    public static GridState buildGridStateFromEcs() {
        List<GridMover> players = new ArrayList<GridMover>();
        for (EcsEntity player : MazeUtils.getPlayer()) {
            players.add(MoverComponent.getMoverComponent(player).getGridMover());
        }
        List<GridMover> boxes = buildBoxesFromEcs();
        GridState state = new GridState(players.size() > 0 ? players.get(0) : null, boxes, buildItemsFromEcs());
        return state;
    }

    public static List<GridMover> buildBoxesFromEcs() {
        List<GridMover> boxes = new ArrayList<GridMover>();
        for (EcsEntity player : MazeUtils.getBoxes()) {
            boxes.add(MoverComponent.getMoverComponent(player).getGridMover());
        }
        return boxes;
    }

    public static List<GridItem> buildItemsFromEcs() {
        List<GridItem> items = new ArrayList<GridItem>();
        for (EcsEntity item : MazeUtils.getItems()) {
            items.add(ItemComponent.getItemComponent(item).getGridItem());
        }
        return items;
    }

    //TODO nur Components liefern? Alle Items sind enditities?->DVK
    public static List<EcsEntity> getInventory(int owner) {
        return SystemManager.findEntities(ItemFilter.byOwner(owner));
    }

    public static List<EcsEntity> getInventory(EcsEntity player) {
        //MoverComponent mc = MoverComponent.getMoverComponent(player);
        return getInventory(player.getId());
    }

    public static List<EcsEntity> getBullets(EcsEntity player) {
        return EcsEntity.filterList(getInventory(player), new ComponentFilter(BulletComponent.TAG));
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
}
