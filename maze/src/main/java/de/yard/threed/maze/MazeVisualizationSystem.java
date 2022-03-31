package de.yard.threed.maze;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.GridTeleportDestination;
import de.yard.threed.engine.GridTeleporter;
import de.yard.threed.engine.PointerHandler;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.apps.vr.VrSceneHelper;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.gui.Icon;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;


import java.util.ArrayList;
import java.util.List;

/**
 * Maze visualization. Not really needed for playing. Only visual. No collision detection isType used.
 * <p>
 * 11.11.20: Nur noch fuer das Maze (im Sinne von statischem Terrain). Der Player (generell entities wie Boxen) wird separat visualisiert.
 * <p>
 * Created by thomass on 16.09.20.
 */

public class MazeVisualizationSystem extends DefaultEcsSystem implements PointerHandler {
    private static Log logger = Platform.getInstance().getLog(MazeVisualizationSystem.class);

    boolean mazevisualizationsystemdebuglog = true;

    //TODO irgendwie lokal in entity. 9.4.21: view in entity?
    static public MazeView view;

    GridTeleporter gridTeleporter;
    SceneNode fireTargetMarker;

    /**
     *
     */
    public MazeVisualizationSystem() {
        super(new String[]{}, new RequestType[]{}, new EventType[]{EventRegistry.EVENT_MAZE_LOADED});
    }


    @Override
    public void process(Event evt) {
        if (mazevisualizationsystemdebuglog) {
            logger.debug("got event " + evt.getType());
        }

        if (evt.getType().equals(EventRegistry.EVENT_MAZE_LOADED)) {
            //wird scheitern, wenn noch kein Login und damit kein Ray.
            createView((Grid) evt.getPayloadByIndex(0));

            // VR und zum Testen
            if (MazeScene.vrInstance != null || true) {

                SceneNode locationMarker = VrSceneHelper.buildGroundMarker(Icon.ICON_CLOSE);
                Scene.getCurrent().addToWorld(locationMarker);
                SceneNode directionMarker = VrSceneHelper.buildGroundMarker(Icon.ICON_UPARROW);
                Scene.getCurrent().addToWorld(directionMarker);
                gridTeleporter = new GridTeleporter(locationMarker, directionMarker);

                //wall center is on ground level. So raise marker to have it above ground
                //fireTargetMarker = VrSceneHelper.buildGroundMarker(Icon.ICON_DESTINATION);
                //fireTargetMarker= ModelSamples.buildCube(1.5,Color.GREEN);
                fireTargetMarker = MazeModelFactory.buildFireTargetMarker();
                fireTargetMarker.getTransform().setPosition(new Vector3(0, 1.2, 0.7));
                Scene.getCurrent().addToWorld(fireTargetMarker);


                ((InputToRequestSystem) SystemManager.findSystem(InputToRequestSystem.TAG)).addPointerHandler(this);
            }
        }
    }

    /**
     * Left pointer shows teleport option marker.
     * Right pointer shows fire targets, highlights hit objects
     */
    @Override
    public void processPointer(Ray ray, boolean left) {

        EcsEntity mainplayer = MazeUtils.getMainPlayer();
        if (mainplayer == null) {
            //not yet joined?
            return;
        }
        if (ray == null) {
            //no ray no pointer
            return;
        }
        if (left) {
            GridState state = MazeUtils.buildGridStateFromEcs();
            MoverComponent mc = MoverComponent.getMoverComponent(mainplayer);

            // show possible teleport destinations
            List<GridMovement> moveOptions = mc.getMoveOptions(state, Grid.getInstance().getMazeLayout());

            logger.debug("found " + moveOptions.size() + " possible teleport targets");
            List<SceneNode> tileCandidates = new ArrayList<SceneNode>();
            for (GridMovement m : moveOptions) {
                if (m.isRelocate()) {
                    SceneNode n = view.terrain.getTiles().get(m.relocateTarget);
                    if (n == null) {
                        logger.warn("No tile for field " + m.relocateTarget);
                    } else {
                        tileCandidates.add(n);
                    }
                }
            }
            for (SceneNode tile : tileCandidates) {
                gridTeleporter.updateDestinationMarker(ray, tile, MazeDimensions.GRIDSEGMENTSIZE);
            }
        } else {
            // mark hit objects. TODO don't hit boxes behind wall.
            double scale = 1.2;
            for (EcsEntity box : MazeUtils.getPlayerOrBoxes(true)) {

                if (ray.intersects(box.getSceneNode(), true)) {
                    box.getSceneNode().getTransform().setScale(new Vector3(scale, scale, scale));
                } else {
                    // reset
                    box.getSceneNode().getTransform().setScale(new Vector3(1, 1, 1));
                }
            }
            if (MazeUtils.playerHasBullets()) {
                for (SceneNode wall : view.terrain.getWalls().values()) {

                    List<NativeCollision> intersections = ray.getIntersections(wall, true);
                    if (intersections.size() > 0) {
                        updateFireTargetMarker(ray, wall, intersections);
                    }
                }
            } else {
                hideFireTargetMarker();
            }
        }
    }


    /**
     * Derive Request from VR trigger event. For VR only!
     * Left pointer triggers teleport.
     * Right triggers either
     * 1) box push or
     * 2) fire bullet
     */
    @Override
    public Request getRequestByTrigger(int userEntityId, Ray ray, boolean left) {
        logger.debug("getRequestByTrigger,left=" + left + ",ray=" + ray);
        if (left) {
            for (SceneNode tile : view.terrain.getTiles().values()) {
                GridTeleportDestination transform = gridTeleporter.updateDestinationMarker(ray, tile, MazeDimensions.GRIDSEGMENTSIZE);
                if (transform != null) {
                    Point p = MazeUtils.vector2Point(transform.transform.position);
                    String orientation = "";
                    if (transform.direction != null) {
                        orientation = "" + transform.direction;
                    }
                    return RequestRegistry.buildRelocate(userEntityId, p, orientation);
                }
            }
        } else {
            for (EcsEntity box : MazeUtils.getPlayerOrBoxes(true)) {

                if (ray.intersects(box.getSceneNode(), true)) {
                    return RequestRegistry.buildKick();
                }
            }
            // look for hit avatar or just a destination wall? Outside VR fire is always possible (in direction of orientation), even without target.
            // To have it similar here and to avoid non orthogonal bullet movement requirements, check for one of the four possible directions. So
            // players orientation doesn't matter for firing in VR.
            List<NativeCollision> intersectedWalls = ray.getIntersections(new ArrayList<SceneNode>(view.terrain.getWalls().values()), true);
            logger.debug("intersectedWalls=" + intersectedWalls.size());
        }
        return null;
    }

    private List<SceneNode> getHitWalls(){
        return null;
    }

    /**
     * @param grid
     */
    private void createView(Grid grid) {

        if (view != null) {
            view.remove();
            view = null;
        }

        logger.debug("visualizing maze terrain");
        view = new MazeView();

        Configuration configuration = Configuration.getDefaultConfiguration();
        String terrainbuilder = configuration.getString("maze.visualization", "");
        if (terrainbuilder.equals("traditional")) {
            view.terrain = new MazeTerrain(grid.getMaxWidth(), grid.getHeight());
        } else {
            throw new RuntimeException("unknown visualization " + terrainbuilder);
        }
        view.terrain.visualizeGrid(grid);
        Scene.getCurrent().addToWorld(view.terrain.getNode());

    }

    private void updateFireTargetMarker(Ray ray, SceneNode wall, List<NativeCollision> intersections) {
        String s = "";
        for (NativeCollision collision : intersections) {
            s += collision.getPoint() + ",";
        }
        logger.debug("attaching fire target marker with " + intersections.size() + " intersections:" + s);
        // tricky: je nach dem auf welcher Seite der Wall ich stehe, muss der marker andere z-Werte und gegensätzliche Rotation haben.
        wall.attach(fireTargetMarker);
    }

    private void hideFireTargetMarker() {
        fireTargetMarker.getTransform().setParent(null);
    }
}
