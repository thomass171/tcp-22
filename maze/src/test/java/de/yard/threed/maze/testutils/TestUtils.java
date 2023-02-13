package de.yard.threed.maze.testutils;

import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.engine.GridTeleporter;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;
import de.yard.threed.engine.testutil.MockedCollision;
import de.yard.threed.engine.testutil.MockedRay;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.maze.*;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.engine.testutil.TestHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static de.yard.threed.maze.RequestRegistry.TRIGGER_REQUEST_FORWARD;
import static org.junit.jupiter.api.Assertions.*;

public class TestUtils {


    public static void rotatePlayer(GridMover mc, boolean left, Point expected) {
        mc.rotate(left);
        TestUtil.assertPoint("current location", expected, mc.getLocation());
    }

   /* public static void rotatePlayer(GridMover mc, boolean left){

        GridMovement gm = mc.rotate(left);
        TestUtil.assertNotNull("GridMovement", gm);
        TestUtil.assertEquals("Orientation","West",mc.getOrientation().getOrientation().toString());
        // terminate movement
        waitForNotMoving(mc);
        TestUtil.assertFalse("isMoving", mc.isMoving());

    }*/

    @Deprecated
    public static GridMovement move(GridMover player, List<GridMover> players, List<GridMover> boxes, GridMovement gridMovement, MazeLayout layout, Point expected) {
        MoveResult moveResult = player.move(gridMovement, player.getOrientation(), new GridState(players, boxes, new ArrayList<GridItem>()), layout);
        GridMovement gm = null;
        if (moveResult != null) {
            gm = moveResult.movement;
        }
        TestUtil.assertPoint("new player location", expected, player.getLocation());
        return gm;
    }

    public static GridMovement move(GridMover player, GridState gridState, GridMovement gridMovement, MazeLayout layout, Point expected) {
        MoveResult moveResult = player.move(gridMovement, player.getOrientation(), gridState, layout);
        GridMovement gm = null;
        if (moveResult != null) {
            gm = moveResult.movement;
        }
        TestUtil.assertPoint("new player location", expected, player.getLocation());
        return gm;
    }

    @Deprecated
    public static GridMovement move(GridMover player, List<GridMover> players, List<GridMover> boxes, GridMovement gridMovement, MazeLayout layout, Point expected, GridMovement expectedGridMovement) {
        GridMovement gm = move(player, players, boxes, gridMovement, layout, expected);
        if (expectedGridMovement != null) {
            TestUtil.assertNotNull("returned GridMovement", gm);
            TestUtil.assertEquals("returned GridMovement", expectedGridMovement.toString(), gm.toString());
        } else {
            TestUtil.assertNull("returned GridMovement", gm);
        }
        return gm;
    }

    public static void move(GridMover mc, GridMovement gridMovement, GridState gridState, MazeLayout layout, Point expected) {
        mc.move(gridMovement, mc.getOrientation(), gridState, layout);
        TestUtil.assertPoint("new player location", expected, mc.getLocation());
    }

    /*public static void walkPlayerByPush(GridMover player, GridMover box, GridState gridState, MazeLayout layout, Point expected) {
        Point boxLocation = box.getLocation();
        MazeUtils.combinedMove(gridState, player, layout);
        TestUtil.assertPoint("new player location", expected, player.getLocation());
        TestUtil.assertPoint("new box location", expected.add(player.getOrientation().getDirectionForMovement(GridMovement.Forward).getPoint()), box.getLocation());
    }*/

   /* public static void movePlayer(GridMover mc, GridMovement gridMovement, GridState, MazeLayout layout){

        GridMovement gm = mc.walk(gridMovement,layout);
        TestUtil.assertNotNull("GridMovement", gm);
        // terminate movement
        waitForNotMoving(mc);
        TestUtil.assertFalse("isMoving", mc.isMoving());

    }*/

    public static String loadGrid(String name) {
        return TestHelper.getDataBundleString("maze", name);
    }

    /**
     * No ray in test platform for now. So mock it.
     */
    public static Ray mockHittingRayForTeleport(Point destinationField, char direction) {
        double offset = GridTeleporter.getCenterOffset(MazeDimensions.GRIDSEGMENTSIZE);
        Vector3 hitPoint = MazeUtils.point2Vector3(destinationField);
        switch (direction) {
            case 'N':
                hitPoint = hitPoint.add(new Vector3(0, 0, -offset));
                break;
            case 'E':
                hitPoint = hitPoint.add(new Vector3(offset, 0, 0));
                break;
            case 'S':
                hitPoint = hitPoint.add(new Vector3(0, 0, offset));
                break;
            case 'W':
                hitPoint = hitPoint.add(new Vector3(-offset, 0, 0));
                break;
        }
        Vector3 origin = new Vector3(hitPoint.getX(), 100, hitPoint.getZ());

        List<NativeCollision> collisions = new ArrayList<>();
        SceneNode groundNode = MazeVisualizationSystem.view.terrain.getTiles().get(destinationField);
        collisions.add(new MockedCollision(groundNode, hitPoint));

        return new Ray(new MockedRay(origin, hitPoint.subtract(origin), collisions));
    }

    public static Ray mockHittingRayForKick(EcsEntity boxToKick) {
        Point boxField = MoverComponent.getMoverComponent(boxToKick).getLocation();
        double offset = GridTeleporter.getCenterOffset(MazeDimensions.GRIDSEGMENTSIZE);
        // just mock the hit inside the box
        Vector3 hitPoint = MazeUtils.point2Vector3(boxField).add(new Vector3(0, 0.5, 0));
        Vector3 origin = new Vector3(hitPoint.getX(), 100, hitPoint.getZ());

        List<NativeCollision> collisions = new ArrayList<>();
        SceneNode groundNode = boxToKick.getSceneNode();
        collisions.add(new MockedCollision(groundNode, hitPoint));

        return new Ray(new MockedRay(origin, hitPoint.subtract(origin), collisions));
    }

    public static List<EcsEntity> getFlyingBullets() {
        return SystemManager.findEntities(e -> {
            BulletComponent bc = BulletComponent.getBulletComponent(e);
            if (bc == null || bc.state != 1) {
                return false;
            }
            return true;
        });
    }

    public static void ecsWalk(SceneRunnerForTesting sceneRunner, EcsEntity player, boolean expectedMovement, Point expectedNewLocation) {
        ecsWalk(TRIGGER_REQUEST_FORWARD, sceneRunner, player, expectedMovement, expectedNewLocation);
    }

    public static void ecsWalk(RequestType moveRequest, SceneRunnerForTesting sceneRunner, EcsEntity player, boolean expectedMovement, Point expectedNewLocation) {
        ecsWalk(new Request(moveRequest, player.getId()), sceneRunner, player, expectedMovement, expectedNewLocation);
    }

    public static void ecsWalk(Request request, SceneRunnerForTesting sceneRunner, EcsEntity player, boolean expectedMovement, Point expectedNewLocation) {
        SystemManager.putRequest(request);
        sceneRunner.runLimitedFrames(3, 0);
        if (expectedMovement) {
            assertNotNull(MazeUtils.isAnyMoving(), "isAnyMoving");
        } else {
            assertNull(MazeUtils.isAnyMoving(), "isAnyMoving:" + MazeUtils.isAnyMoving());
        }
        // 5 seems to be sufficient for completing the move
        sceneRunner.runLimitedFrames(5);
        assertPosition(player, expectedNewLocation);
        assertNull(MazeUtils.isAnyMoving(), "isAnyMoving");
    }

    public static void assertPosition(EcsEntity user, Point point) {
        MoverComponent mc = MoverComponent.getMoverComponent(user);
        assertNotNull(mc, "user1.MoverComponent");
        TestUtil.assertPoint(" point", point, mc.getLocation());
    }

    public static void assertDirection(Direction expected, Direction actual) {
        assertEquals(expected.toString(), actual.toString());
    }

    public static SceneRunnerForTesting buildSceneRunnerForMazeScene(String gridname, boolean gridTeleporterEnabled, int initial_frames) {

        HashMap<String, String> properties = new HashMap<String, String>();
        if (gridTeleporterEnabled) {
            properties.put("argv.enableMazeGridTeleporter", "true");
        }
        return buildSceneRunnerForMazeScene(gridname,properties,initial_frames);
    }

    /**
     * Also used in other modules.
     */
    public static SceneRunnerForTesting buildSceneRunnerForMazeScene(String gridname,  HashMap<String, String> additionalPproperties, int initial_frames) {

        MazeDataProvider.reset();

        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", "de.yard.threed.maze.MazeScene");
        properties.put("argv.initialMaze", gridname);
        properties.putAll(additionalPproperties);
        // buildDefaultConfigurationWithEnv is needed for HOSTDIR
        SceneRunnerForTesting sceneRunner = SceneRunnerForTesting.setupForScene(initial_frames, Configuration.buildDefaultConfigurationWithEnv(properties), new String[]{"engine", "data", "maze"});
        return sceneRunner;
    }
}
