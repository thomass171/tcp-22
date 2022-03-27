package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.avatar.AvatarPmlFactory;
import de.yard.threed.engine.loader.PortableMaterial;
import de.yard.threed.engine.loader.PortableModelDefinition;
import de.yard.threed.engine.loader.PortableModelList;

import java.util.ArrayList;
import java.util.List;

/**
 * Dies und das
 * <p>
 * 13.4.21
 */
public class MazeFactory {

    /**
     * For outside ECS only? Testing?
     *
     * @param initialLocation
     * @return
     */
    public static GridMover buildMover(Point initialLocation) {

        return buildMover(initialLocation, new GridOrientation(), null);
    }

    public static GridMover buildMover(Point initialLocation, GridOrientation initialOrientation, Team team) {

        return new SimpleGridMover(initialLocation, initialOrientation, team);
    }

    /**
     * For outside ECS only? Testing?
     *
     * @param initialLocation
     * @return
     */
    public static List<GridMover> buildMovers(List<Point> initialLocation) {

        Team team = null;
        List<GridMover> mover = new ArrayList<GridMover>();
        for (Point p : initialLocation) {
            mover.add(new SimpleGridMover(p, new GridOrientation(), team));
        }
        return mover;
    }

    /**
     * For outside ECS only? Testing?
     * TODO pass type of item?
     *
     * @param initialLocation
     * @return
     */
    public static List<GridItem> buildItems(List<Point> initialLocation) {

        List<GridItem> items = new ArrayList<GridItem>();
        for (Point p : initialLocation) {
            items.add(new SimpleGridItem(p));
        }
        return items;
    }

    /**
     * Just a sphere. Has no elevation above anything, so needs to be raised.
     *
     * @return
     */
    public static SceneNode buildMonster() {

        double headRadius = 0.20;

        PortableMaterial faceMaterial = new PortableMaterial("faceMaterial", "maze:textures/Face-Monster.png");

        PortableModelDefinition head = AvatarPmlFactory.buildHead(headRadius, "faceMaterial");

        PortableModelList pml = new PortableModelList(null);
        pml.addModel(head);
        pml.addMaterial(faceMaterial);

        SceneNode model = pml.buildModel(null, null);
        model.setName("Monster");
        return new SceneNode(model);
    }

}
