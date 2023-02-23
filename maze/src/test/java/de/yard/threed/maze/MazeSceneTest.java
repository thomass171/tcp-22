package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
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
import de.yard.threed.maze.testutils.MazeTestUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


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
     * <p>
     * Tests grid teleporting by ray
     */
    @Test
    public void testSokobanWikipedia() {
        sceneRunner = MazeTestUtils.buildSceneRunnerForMazeScene("skbn/SokobanWikipedia.txt", true, INITIAL_FRAMES);

        // env + test properties + maze properties file
        assertEquals(3, Platform.getInstance().getConfiguration().size());

        assertTrue(SystemState.readyToJoin());
        MazeVisualizationSystem mazeVisualizationSystem = ((MazeVisualizationSystem) SystemManager.findSystem(MazeVisualizationSystem.TAG));
        assertNotNull(mazeVisualizationSystem.gridTeleporter);

        EcsEntity user = UserSystem.getInitialUser();
        assertNotNull(user);
        // The default maze user has an empty user name
        assertEquals("", user.getName(), "user name");
        MoverComponent mc = MoverComponent.getMoverComponent(user);
        TestUtils.assertPoint(new Point(6, 1), mc.getLocation(), "player location");

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
        Ray ray = MazeTestUtils.mockHittingRayForTeleport(new Point(7, 3), 'W');
        inputToRequestSystem.mockInput(ray, true, true);
        sceneRunner.runLimitedFrames(3);

        TestUtils.assertPoint(new Point(7, 3), mc.getLocation(), "player location after teleport");
        TestUtils.assertVector3(MazeUtils.point2Vector3(new Point(7, 3)), user.getSceneNode().getTransform().getPosition(), "player location after teleport");
        TestUtils.assertPoint(new Point(7, 3), MazeUtils.getMoverposition(user), "location after teleport");
        assertEquals(GridOrientation.fromDirection("W").toString(), MazeUtils.getPlayerorientation(user).toString(), "orientation after teleport (should be left/WEST)");

        // kick the (6,3) box to (5,3) now via trigger ray
        EcsEntity boxToKick = MazeUtils.findBoxByField(new Point(6, 3));
        ray = MazeTestUtils.mockHittingRayForKick(boxToKick);
        inputToRequestSystem.mockInput(ray, true, false);
        sceneRunner.runLimitedFrames(3);
        TestUtils.assertPoint(new Point(5, 3), MazeUtils.getMoverposition(boxToKick), "box location after kick");
        // player should not move
        TestUtils.assertPoint(new Point(7, 3), mc.getLocation(), "player location after kick (unchanged)");

        // try to teleport to unreachable field
        ray = MazeTestUtils.mockHittingRayForTeleport(new Point(3, 4), 'W');
        inputToRequestSystem.mockInput(ray, true, true);
        sceneRunner.runLimitedFrames(3);
        // player should not move
        TestUtils.assertPoint(new Point(7, 3), mc.getLocation(), "player location after kick (unchanged)");

        // kick again, but with trigger ray to box at (6,2). Shouldn't move neither box.
        EcsEntity boxToHit = MazeUtils.findBoxByField(new Point(6, 2));
        ray = MazeTestUtils.mockHittingRayForKick(boxToHit);
        inputToRequestSystem.mockInput(ray, true, false);
        sceneRunner.runLimitedFrames(3);
        TestUtils.assertPoint(new Point(6, 2), MazeUtils.getMoverposition(boxToHit), "box shouldn't move");
        TestUtils.assertPoint(new Point(5, 3), MazeUtils.getMoverposition(boxToKick), "box shouldn't move");
        // player should not move
        TestUtils.assertPoint(new Point(7, 3), mc.getLocation(), "player location after kick (unchanged)");

        // kick the (5,3) box again (to (4,3)) via trigger ray
        boxToKick = MazeUtils.findBoxByField(new Point(5, 3));
        ray = MazeTestUtils.mockHittingRayForKick(boxToKick);
        inputToRequestSystem.mockInput(ray, true, false);
        sceneRunner.runLimitedFrames(3);
        TestUtils.assertPoint(new Point(4, 3), MazeUtils.getMoverposition(boxToKick), "box location after kick");
        // player should not move
        TestUtils.assertPoint(new Point(7, 3), mc.getLocation(), "player location after kick (unchanged)");
        // and to (3,3)
        boxToKick = MazeUtils.findBoxByField(new Point(4, 3));
        ray = MazeTestUtils.mockHittingRayForKick(boxToKick);
        inputToRequestSystem.mockInput(ray, true, false);
        sceneRunner.runLimitedFrames(3);
        TestUtils.assertPoint(new Point(3, 3), MazeUtils.getMoverposition(boxToKick), "box location after kick");
        // player should not move
        TestUtils.assertPoint(new Point(7, 3), mc.getLocation(), "player location after kick (unchanged)");

        // teleport to reachable field (4,3)
        ray = MazeTestUtils.mockHittingRayForTeleport(new Point(4, 3), 'W');
        inputToRequestSystem.mockInput(ray, true, true);
        sceneRunner.runLimitedFrames(3);
        // player should not move
        TestUtils.assertPoint(new Point(4, 3), mc.getLocation(), "player location after teleport");

        // and on field (4,4)
        ray = MazeTestUtils.mockHittingRayForTeleport(new Point(4, 4), 'S');
        inputToRequestSystem.mockInput(ray, true, true);
        sceneRunner.runLimitedFrames(3);
        // player should not move
        TestUtils.assertPoint(new Point(4, 4), mc.getLocation(), "player location after teleport");
        // and (3,4): TODO do immediatel from (4,3)
        ray = MazeTestUtils.mockHittingRayForTeleport(new Point(3, 4), 'S');
        inputToRequestSystem.mockInput(ray, true, true);
        sceneRunner.runLimitedFrames(3);
        // player should not move
        TestUtils.assertPoint(new Point(3, 4), mc.getLocation(), "player location after teleport");
    }

    /**
     * A monster is a bot player.
     * ##########
     * #   M    #
     * #  D# #  #
     * #   # #  #
     * #    P D #
     * ##########
     */
    @Test
    public void test_P_Simple() {
        sceneRunner = MazeTestUtils.buildSceneRunnerForMazeScene("maze/Maze-P-Simple.txt", false, INITIAL_FRAMES);

        // env + test properties + maze properties file
        assertEquals(3, Platform.getInstance().getConfiguration().size());

        assertTrue(SystemState.readyToJoin());

        EcsEntity user = UserSystem.getInitialUser();
        assertNotNull(user);
        // The default maze user has an empty user name
        assertEquals("", user.getName(), "user name");
        MoverComponent mc = MoverComponent.getMoverComponent(user);
        TestUtils.assertPoint(new Point(5, 1), mc.getLocation(), "player location");

        assertEquals(1 + 3 + 1 + 3 + 2, SystemManager.findEntities((EntityFilter) null).size(),
                "number of entites (2player with 3 bullets each, 2 diamonds)");

        assertEquals(new GridOrientation().toString(), MazeUtils.getPlayerorientation(MazeUtils.getMainPlayer()).toString(), "initial orientation");
        assertEquals(new Point(5, 1).toString(), MazeUtils.getMoverposition(MazeUtils.getMainPlayer()).toString(), "initial location");

        List<EcsEntity> bullets = SystemManager.findEntities(e -> e.getComponent(BulletComponent.TAG) != null);
        assertEquals(6, bullets.size());
        // Initially bullets are in the inventory of the player and should be hidden
        for (EcsEntity bullet : bullets) {
            BulletComponent bulletComponent = BulletComponent.getBulletComponent(bullet);
            assertTrue(bulletComponent.isHidden());
        }

    }

    /**
     * A monster is a bot player.
     * ##############################
     * #                            #
     * #  #         # ##            #
     * # .####         #            #
     * ####            #        #####
     * #    #            #          #
     * #  ###         ####          #
     * #  #           #             #
     * #    #               MMM     #
     * #                            #
     * #  #         # ##            #
     * #  #         # ##            #
     * #  #         # ##            #
     * #  ####         #            #
     * ####            #        #####
     * #    #            #          #
     * #  ###         ####         @#
     * #  #           #             #
     * #    #                       #
     * ##############################
     */
    @Test
    public void testM_30x20() throws Exception {

        sceneRunner = MazeTestUtils.buildSceneRunnerForMazeScene("maze/Maze-M-30x20.txt", false, INITIAL_FRAMES);

        // env + test properties + maze properties file
        assertEquals(3, Platform.getInstance().getConfiguration().size());

        assertTrue(SystemState.readyToJoin());

        EcsEntity user = UserSystem.getInitialUser();
        assertNotNull(user);
        // The default maze user has an empty user name
        assertEquals("", user.getName(), "user name");
        MoverComponent mc = MoverComponent.getMoverComponent(user);
        TestUtils.assertPoint(new Point(28, 3), mc.getLocation(), "player location");

        assertEquals(4 * (1 + 3), SystemManager.findEntities((EntityFilter) null).size(),
                "number of entites (4 player with 3 bullets each)");

        EcsEntity bot0 = MazeUtils.findPlayerByName("Bot0");
        assertNotNull(bot0);
        TestUtils.assertPoint(new Point(21, 12), MoverComponent.getMoverComponent(bot0).getLocation(), "bot0 location");
        EcsEntity bot1 = MazeUtils.findPlayerByName("Bot1");
        assertNotNull(bot1);
        TestUtils.assertPoint(new Point(22, 12), MoverComponent.getMoverComponent(bot1).getLocation(), "bot1 location");
        EcsEntity bot2 = MazeUtils.findPlayerByName("Bot2");
        assertNotNull(bot2);
        TestUtils.assertPoint(new Point(23, 12), MoverComponent.getMoverComponent(bot2).getLocation(), "bot2 location");

        assertEquals(0, MoverComponent.getMoverComponent(user).getTeam());
        assertEquals(1, MoverComponent.getMoverComponent(bot0).getTeam());
        assertEquals(1, MoverComponent.getMoverComponent(bot1).getTeam());
        assertEquals(1, MoverComponent.getMoverComponent(bot2).getTeam());
    }
}
