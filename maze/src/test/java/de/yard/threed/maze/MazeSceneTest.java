package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.maze.testutils.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Integration test.
 * Putting it all together and test interaction.
 * <p>
 * Created by thomass on 29.1.22.
 */
public class MazeSceneTest {

    SceneRunnerForTesting sceneRunner;
    static final int INITIAL_FRAMES = 10;

    /**
     * ####
     * ###  ####
     * #     $ #
     * # #  #$ #
     * # . .#@ #
     * #########
     */
    @Test
    public void testSokobanWikipedia() {
        setup("skbn/SokobanWikipedia.txt", true);

        assertEquals(2, Configuration.getDefaultConfiguration().size());

        assertTrue(SystemState.readyToJoin());

        EcsEntity user = UserSystem.getInitialUser();
        assertNotNull(user);
        // The default maze user has an empty user name
        assertEquals("", user.getName(), "user name");
        MoverComponent mc = MoverComponent.getMoverComponent(user);
        TestUtil.assertPoint("player location", new Point(6, 1), mc.getLocation());

        assertEquals(2 + 1, SystemManager.findEntities((EntityFilter) null).size(), "number of entites (2 boxes+avatar)");

        assertEquals(new GridOrientation().toString(), MazeUtils.getPlayerorientation(MazeUtils.getMainPlayer()).toString(), "initial orientation");
        assertEquals(new Point(6, 1).toString(), MazeUtils.getMoverposition(MazeUtils.getMainPlayer()).toString(), "initial location");

        Camera camera = Scene.getCurrent().getDefaultCamera();
        InputToRequestSystem inputToRequestSystem = ((InputToRequestSystem) SystemManager.findSystem(InputToRequestSystem.TAG));
        Vector3 worldPos = camera.getCarrier().getTransform().getWorldModelMatrix().extractPosition();
        // 1.35 just taken as is. Where does it come from?
        assertEquals(1.35, worldPos.getY(), 0.001, "viewpoint absolut height");
        // teleport to (7,3) for first moving of a box
        // No ray in test platform for now, so mock it.
        Ray ray = TestUtils.mockHittingRayForTeleport(new Point(7, 3), 'W');
        inputToRequestSystem.mockInput(ray, true, true);
        sceneRunner.runLimitedFrames(3);

        TestUtil.assertPoint(new Point(7, 3), mc.getLocation(), "player location after teleport");
        TestUtil.assertVector3("player location after teleport", MazeUtils.point2Vector3(new Point(7, 3)), user.getSceneNode().getTransform().getPosition());
        TestUtil.assertPoint(new Point(7, 3), MazeUtils.getMoverposition(user), "location after teleport");
        assertEquals(GridOrientation.fromDirection("W").toString(), MazeUtils.getPlayerorientation(user).toString(), "orientation after teleport (should be left/WEST)");

        // kick the (6,3) box to (5,3) now via trigger ray
        EcsEntity boxToKick = MazeUtils.findBoxByField(new Point(6, 3));
        ray = TestUtils.mockHittingRayForKick(boxToKick);
        inputToRequestSystem.mockInput(ray, true, false);
        sceneRunner.runLimitedFrames(3);
        TestUtil.assertPoint(new Point(5, 3), MazeUtils.getMoverposition(boxToKick), "box location after kick");
        // player should not move
        TestUtil.assertPoint(new Point(7, 3), mc.getLocation(), "player location after kick (unchanged)");

        // try to teleport to unreachable field
        ray = TestUtils.mockHittingRayForTeleport(new Point(3, 4), 'W');
        inputToRequestSystem.mockInput(ray, true, true);
        sceneRunner.runLimitedFrames(3);
        // player should not move
        TestUtil.assertPoint(new Point(7, 3), mc.getLocation(), "player location after kick (unchanged)");

        // kick again, but with trigger ray to box at (6,2). Shouldn't move neither box.
        EcsEntity boxToHit = MazeUtils.findBoxByField(new Point(6, 2));
        ray = TestUtils.mockHittingRayForKick(boxToHit);
        inputToRequestSystem.mockInput(ray, true, false);
        sceneRunner.runLimitedFrames(3);
        TestUtil.assertPoint(new Point(6, 2), MazeUtils.getMoverposition(boxToHit), "box shouldn't move");
        TestUtil.assertPoint(new Point(5, 3), MazeUtils.getMoverposition(boxToKick), "box shouldn't move");
        // player should not move
        TestUtil.assertPoint(new Point(7, 3), mc.getLocation(), "player location after kick (unchanged)");

        // kick the (5,3) box again (to (4,3)) via trigger ray
        boxToKick = MazeUtils.findBoxByField(new Point(5, 3));
        ray = TestUtils.mockHittingRayForKick(boxToKick);
        inputToRequestSystem.mockInput(ray, true, false);
        sceneRunner.runLimitedFrames(3);
        TestUtil.assertPoint(new Point(4, 3), MazeUtils.getMoverposition(boxToKick), "box location after kick");
        // player should not move
        TestUtil.assertPoint(new Point(7, 3), mc.getLocation(), "player location after kick (unchanged)");
        // and to (3,3)
        boxToKick = MazeUtils.findBoxByField(new Point(4, 3));
        ray = TestUtils.mockHittingRayForKick(boxToKick);
        inputToRequestSystem.mockInput(ray, true, false);
        sceneRunner.runLimitedFrames(3);
        TestUtil.assertPoint(new Point(3, 3), MazeUtils.getMoverposition(boxToKick), "box location after kick");
        // player should not move
        TestUtil.assertPoint(new Point(7, 3), mc.getLocation(), "player location after kick (unchanged)");

        // teleport to reachable field (4,3)
        ray = TestUtils.mockHittingRayForTeleport(new Point(4, 3), 'W');
        inputToRequestSystem.mockInput(ray, true, true);
        sceneRunner.runLimitedFrames(3);
        // player should not move
        TestUtil.assertPoint(new Point(4, 3), mc.getLocation(), "player location after teleport");

        // and on field (4,4)
        ray = TestUtils.mockHittingRayForTeleport(new Point(4, 4), 'S');
        inputToRequestSystem.mockInput(ray, true, true);
        sceneRunner.runLimitedFrames(3);
        // player should not move
        TestUtil.assertPoint(new Point(4, 4), mc.getLocation(), "player location after teleport");
        // and (3,4): TODO do immediatel from (4,3)
        ray = TestUtils.mockHittingRayForTeleport(new Point(3, 4), 'S');
        inputToRequestSystem.mockInput(ray, true, true);
        sceneRunner.runLimitedFrames(3);
        // player should not move
        TestUtil.assertPoint(new Point(3, 4), mc.getLocation(), "player location after teleport");
    }

    /**
     * Needs parameter, so no @Before
     */
    private void setup(String gridname, boolean gridTeleporterEnabled) {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", "de.yard.threed.maze.MazeScene");
        properties.put("argv.initialMaze", gridname);
        if (gridTeleporterEnabled) {
            properties.put("argv.enableMazeGridTeleporter", "true");
        }
        sceneRunner = SceneRunnerForTesting.setupForScene(INITIAL_FRAMES, properties, new String[]{"engine", "data", "maze"});
    }
}
