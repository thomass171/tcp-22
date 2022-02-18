package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;


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
        setup("skbn/SokobanWikipedia.txt");

        assertTrue(SystemState.readyToJoin());

        EcsEntity user = UserSystem.getInitialUser();
        assertNotNull(user);
        assertEquals("user name", "User0", user.getName());
        MoverComponent mc = MoverComponent.getMoverComponent(user);
        TestUtil.assertPoint("player location", new Point(6, 1), mc.getLocation());

        assertEquals("number of entites (2 boxes+avatar)", 2 + 1, SystemManager.findEntities((EntityFilter) null).size());

        assertEquals("initial orientation", new GridOrientation().toString(), MazeUtils.getPlayerorientation().toString());
        assertEquals("initial location", new Point(6, 1).toString(), MazeUtils.getPlayerposition().toString());

        Camera camera = Scene.getCurrent().getDefaultCamera();
        Vector3 worldPos = camera.getCarrier().getTransform().getWorldModelMatrix().extractPosition();
        // 1.35 just taken as is. Where does it come from?
        assertEquals("viewpoint absolut height", 1.35,worldPos.getY(),0.001);
        // teleport to ... for first moving of a box
        /*No ray in test platform for now.
        Ray ray = TestUtils.getHittingRayForTeleport(new Point(7, 3),'W');
        ((InputToRequestSystem)SystemManager.findSystem(InputToRequestSystem.TAG)).mockInput(ray,true);
        sceneRunner.runLimitedFrames(3);

        TestUtil.assertPoint("player location after teleport", new Point(7, 3), mc.getLocation());
        TestUtil.assertVector3("player location after teleport", MazeUtils.point2Vector3(new Point(7, 3)), user.getSceneNode().getTransform().getPosition());
        assertEquals("location after teleport", new Point(7, 3).toString(), MazeUtils.getPlayerposition().toString());
        assertEquals("orientation after teleport (should be left/WEST)", GridOrientation.fromDirection('W').toString(), MazeUtils.getPlayerorientation().toString());
*/
    }

    /**
     * Needs parameter, so no @Before
     */
    private void setup(String gridname) {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("scene", "de.yard.threed.maze.MazeScene");
        properties.put("argv.initialMaze", gridname);
        sceneRunner = SceneRunnerForTesting.setupForScene(INITIAL_FRAMES, properties,new String[] {"engine","data","maze"});
    }
}
