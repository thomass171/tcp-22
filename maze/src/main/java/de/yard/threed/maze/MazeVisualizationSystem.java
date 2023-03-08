package de.yard.threed.maze;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformHelper;
import de.yard.threed.engine.GridTeleportDestination;
import de.yard.threed.engine.GridTeleporter;
import de.yard.threed.engine.PointerHandler;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.apps.vr.VrSceneHelper;
import de.yard.threed.engine.ecs.AnimationComponent;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsHelper;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.gui.Icon;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
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
    public static String TAG = "MazeVisualizationSystem";
    //TODO irgendwie lokal in entity. 9.4.21: view in entity?
    static public MazeView view;

    public GridTeleporter gridTeleporter;
    SceneNode fireTargetMarker;
    // fireMode 1: Use a target marker at a wall to indicate fire target. Fire targets are difficult to display correctly at all walls
    // due to different wall orientations. Not completely implemented.
    // fireMode 2: Highlight target like boxes are highlighted. This is visually more consistent.
    int vrFireMode = 2;
    // a grid teleporter is used in VR and for testing.
    private boolean gridTeleporterEnabled;
    // give left and right controller same functions
    private boolean distinctLeftRightVrControllerEnabled = false;

    /**
     *
     */
    public MazeVisualizationSystem() {
        super(new String[]{}, new RequestType[]{}, new EventType[]{EventRegistry.EVENT_MAZE_LOADED});

        Boolean b;
        if ((b = Platform.getInstance().getConfiguration().getBoolean("enableMazeGridTeleporter")) != null) {
            gridTeleporterEnabled = (boolean) b;
        }
    }

    @Override
    public void process(Event evt) {
        if (mazevisualizationsystemdebuglog) {
            logger.debug("got event " + evt.getType());
        }

        if (evt.getType().equals(EventRegistry.EVENT_MAZE_LOADED)) {
            //wird scheitern, wenn noch kein Login und damit kein Ray.
            // gridname is not really needed currently to load the grid from the data provider
            Grid grid = MazeDataProvider.getGrid();
            createView(grid);

            // VR und zum Testen
            if (MazeScene.vrInstance != null) {

                if (vrFireMode == 1) {
                    //wall center is on ground level. So raise marker to have it above ground
                    //fireTargetMarker = VrSceneHelper.buildGroundMarker(Icon.ICON_DESTINATION);
                    //fireTargetMarker= ModelSamples.buildCube(1.5,Color.GREEN);
                    fireTargetMarker = MazeModelFactory.buildFireTargetMarker();
                    fireTargetMarker.getTransform().setPosition(new Vector3(0, 1.2, 0.7));
                    Scene.getCurrent().addToWorld(fireTargetMarker);
                }
                // Once gridTeleporterEnabled was set here in general. But since it is CPU consuming leave it up to the user.
                //gridTeleporterEnabled = true;
            }

            if (gridTeleporterEnabled) {
                logger.debug("Building GridTeleporter");
                SceneNode locationMarker = VrSceneHelper.buildGroundMarker(Icon.ICON_CLOSE);
                Scene.getCurrent().addToWorld(locationMarker);
                SceneNode directionMarker = VrSceneHelper.buildGroundMarker(Icon.ICON_UPARROW);
                Scene.getCurrent().addToWorld(directionMarker);
                gridTeleporter = new GridTeleporter(locationMarker, directionMarker);
            }

            ((InputToRequestSystem) SystemManager.findSystem(InputToRequestSystem.TAG)).addPointerHandler(this);
        }
    }

    /**
     * Check VR pointer destination. Intended for VR only.
     * <p>
     * Left pointer shows teleport option marker (when gridTeleporter is enabled).
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
        // Optionally do it not every frame to save CPU cycles. Tracking a ray might be time consuming.
        // Unclear for now what good setting is.
        if (AbstractSceneRunner.getInstance().getFrameCount() % 5 != 0) {
            return;
        }

        Point myLocation = MoverComponent.getMoverComponent(mainplayer).getLocation();
        if (!distinctLeftRightVrControllerEnabled || left) {
            GridState state = MazeUtils.buildGridStateFromEcs();
            MoverComponent mc = MoverComponent.getMoverComponent(mainplayer);

            if (gridTeleporter != null) {
                // show possible teleport destinations. This is a very time consuming process, in a browser
                // up to 500ms for 189 candidates.
                List<SceneNode> tileCandidates = getValidTeleportDestinationTiles(mc, state, "Pointer");

                for (SceneNode tile : tileCandidates) {
                    gridTeleporter.updateDestinationMarker(ray, tile, MazeDimensions.GRIDSEGMENTSIZE);
                }
            }
        }
        if (!distinctLeftRightVrControllerEnabled || !left) {
            // right pointer marks/highlights hit objects.

            for (EcsEntity box : MazeUtils.getPlayerOrBoxes(true)) {
                double hitBoxScale = 1.2;
                // TODO don't hit boxes behind wall.
                if (ray.intersects(box.getSceneNode(), true)) {
                    box.getSceneNode().getTransform().setScale(new Vector3(hitBoxScale, hitBoxScale, hitBoxScale));
                } else {
                    // reset
                    box.getSceneNode().getTransform().setScale(new Vector3(1, 1, 1));
                }
            }
            if (MazeUtils.playerHasBullets()) {
                if (vrFireMode == 1) {
                    hideFireTargetMarker();
                    for (SceneNode wall : view.terrain.getWalls().values()) {

                        List<NativeCollision> intersections = ray.getIntersections(wall, true);
                        if (intersections.size() > 0) {
                            updateFireTargetMarker(ray, wall, intersections);
                        }
                    }
                }
                if (vrFireMode == 2) {
                    double hitMonsterScale = 1.5;
                    double monsterOriginScale = 1.2;
                    for (EcsEntity player : MazeUtils.getPlayerOrBoxes(false)) {

                        AnimationComponent ac = AnimationComponent.getAnimationComponent(player);
                        // AvatarA has no animation for now
                        if (ac != null) {
                            if (MoverComponent.getMoverComponent(player).getLocation().onSameAxis(myLocation) &&
                                    ray.intersects(player.getSceneNode(), true)) {
                                //player.getSceneNode().getTransform().setScale(new Vector3(hitMonsterScale, hitMonsterScale, hitMonsterScale));
                                ac.setMarkedEnabled(true);
                            } else {
                                // reset
                                //player.getSceneNode().getTransform().setScale(new Vector3(monsterOriginScale, monsterOriginScale, monsterOriginScale));
                                ac.setMarkedEnabled(false);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Derive Request from VR trigger event. For VR only!
     * Used when the trigger on the controller really was triggered.
     * Left pointer triggers teleport.
     * Right triggers either
     * 1) box push or
     * 2) fire bullet
     * In any case, check action according to priority, which is
     * 1) box push
     * 2) fire
     * 3) teleport
     * and only trigger the first possible action.
     */
    @Override
    public Request getRequestByTrigger(int userEntityId, Ray ray, boolean left) {
        logger.debug("getRequestByTrigger,left=" + left + ",ray=" + ray);

        EcsEntity mainplayer = MazeUtils.getMainPlayer();
        if (mainplayer == null) {
            //not yet joined?
            return null;
        }
        if (ray == null) {
            //no ray no pointer
            return null;
        }
        MoverComponent mc = MoverComponent.getMoverComponent(mainplayer);
        Point myLocation = mc.getLocation();
        GridState state = MazeUtils.buildGridStateFromEcs();

        if (!distinctLeftRightVrControllerEnabled || !left) {
            // right controller triggered
            for (EcsEntity box : MazeUtils.getBoxes()) {
                // Kick only applies to a box in direction of orientation. So check that intersected box is really one in direction
                // And be sure sure the box is really intersected
                GridMover nextBox = state.findNextBox(mc.getLocation(), mc.getGridOrientation(), Grid.getInstance().getMazeLayout());
                if (nextBox != null && box.getId() == nextBox.getId() && ray.intersects(box.getSceneNode(), true)) {
                    return RequestRegistry.buildKick(userEntityId);
                }
            }
            // look for hit avatar or just a destination wall? Outside VR fire is always possible (in direction of orientation), even without target.
            // To have it similar here and to avoid non orthogonal bullet movement requirements, check for one of the four possible directions. So
            // players orientation doesn't matter for firing in VR.
            // Due to semi walls etc all this might not be accurate enough?
            // See vrFireMode: use avatar hit instead of wall.
            if (vrFireMode == 1) {
                List<NativeCollision> intersectedWalls = ray.getIntersections(new ArrayList<SceneNode>(view.terrain.getWalls().values()), true);
                logger.debug("intersectedWalls=" + intersectedWalls.size());
                if (intersectedWalls.size() > 0) {
                    Point wallLocation = MazeUtils.vector2Point(intersectedWalls.get(0).getPoint());
                    Point playerLocation = MoverComponent.getMoverComponent(EcsHelper.findEntityById(userEntityId)).getLocation();
                    if (playerLocation.onSameAxis(wallLocation)) {
                        Direction direction = Direction.of(playerLocation, wallLocation);
                        return BulletSystem.buildFireRequest(userEntityId, direction);
                    }
                }
            }
            if (vrFireMode == 2) {
                for (EcsEntity player : MazeUtils.getPlayerOrBoxes(false)) {

                    Point loc = MoverComponent.getMoverComponent(player).getLocation();
                    // dont't hit own avatar
                    if (player.getId() != mainplayer.getId() && loc.onSameAxis(myLocation) &&
                            ray.intersects(player.getSceneNode(), true)) {
                        // In VR the target direction might differ from current player orientation.
                        Direction targetDirection = Direction.of(myLocation, loc);

                        return BulletSystem.buildFireRequest(userEntityId, targetDirection);
                    }
                }
            }
        }
        if (!distinctLeftRightVrControllerEnabled || left) {
            // left controller triggered. Try teleport.
            if (gridTeleporter != null) {
                // only check for valid teleport destinations
                List<SceneNode> tileCandidates = getValidTeleportDestinationTiles(mc, state, "trigger");
                for (SceneNode tile : tileCandidates) {
                    GridTeleportDestination transform = gridTeleporter.updateDestinationMarker(ray, tile, MazeDimensions.GRIDSEGMENTSIZE);
                    if (transform != null) {
                        Point p = MazeUtils.vector2Point(transform.getTransform().position);
                        return RequestRegistry.buildRelocate(userEntityId, p, GridOrientation.fromDirection(transform.getDirection()));
                    }
                }
            }
        }
        return null;
    }

    public void setGridTeleporterEnabled(boolean enabled) {
        gridTeleporterEnabled = enabled;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    private List<SceneNode> getHitWalls() {
        return null;
    }

    /**
     *
     */
    private List<SceneNode> getValidTeleportDestinationTiles(MoverComponent mc, GridState state, String useCase) {
        List<GridMovement> moveOptions = mc.getMoveOptions(state, Grid.getInstance().getMazeLayout());

        List<SceneNode> tileCandidates = new ArrayList<SceneNode>();
        for (GridMovement m : moveOptions) {
            if (m.isRelocate()) {
                SceneNode n = view.terrain.getTiles().get(m.getRelocateTarget());
                if (n == null) {
                    logger.warn("No tile for field " + m.getRelocateTarget());
                } else {
                    tileCandidates.add(n);
                }
            }
        }
        logger.debug("found " + moveOptions.size() + " move options resulting in " + tileCandidates.size() + " teleport target tile candidates for " + useCase);
        return tileCandidates;
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

        Configuration configuration = Platform.getInstance().getConfiguration();
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
