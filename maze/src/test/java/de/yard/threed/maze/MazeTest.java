package de.yard.threed.maze;

import de.yard.threed.core.Vector3;
import de.yard.threed.engine.Observer;
import de.yard.threed.core.Point;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.*;
import de.yard.threed.core.InitMethod;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.core.Payload;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import org.junit.Test;

import java.util.List;

import static de.yard.threed.maze.RequestRegistry.*;
import static org.junit.Assert.*;

/**
 * Tests using ECS (See GridTest for tests without ECS).
 */
public class MazeTest {

    static final int INITIAL_FRAMES = 10;

    SceneRunnerForTesting sceneRunner;

    ReplaySystem replaySystem;

    SceneNode observerDummy;

    /**
     * Wegen parameter nicht als @Before
     */
    private void setup(String levelname) {
        InitMethod initMethod = new InitMethod() {
            @Override
            public void init() {
                SystemManager.reset();
                // No visualization to reveal model-view coupling.
                SystemManager.addSystem(new MazeMovingAndStateSystem(levelname));
                SystemManager.addSystem(new UserSystem());
                SystemManager.addSystem(new AvatarSystem());
                SystemManager.addSystem(new BulletSystem());
                replaySystem = new ReplaySystem();
                SystemManager.addSystem(replaySystem);
            }
        };

        TestFactory.initPlatformForTest( new String[]{"engine", "maze", "data"}, new PlatformFactoryHeadless(), initMethod);

        sceneRunner = (SceneRunnerForTesting) AbstractSceneRunner.instance;
        observerDummy = new SceneNode();
        Observer.buildForTransform(observerDummy.getTransform());
        sceneRunner.runLimitedFrames(INITIAL_FRAMES);


    }

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

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());
        assertEquals("number of entities (2 boxes)", 2, SystemManager.findEntities((EntityFilter) null).size());

        assertTrue(SystemState.readyToJoin());

        SystemManager.putRequest(new Request(UserSystem.USER_REQUEST_LOGIN, new Payload("")));

        sceneRunner.runLimitedFrames(5);
        assertEquals(INITIAL_FRAMES + 5, sceneRunner.getFrameCount());

        assertEquals("number of entites (2 boxes+avatar)", 2 + 1, SystemManager.findEntities((EntityFilter) null).size());

        assertEquals("initial orientation", new GridOrientation().toString(), MazeUtils.getPlayerorientation().toString());
        assertEquals("initial location", new Point(6, 1).toString(), MazeUtils.getPlayerposition().toString());

        replaySystem.addRequests(new Request(TRIGGER_REQUEST_TURNRIGHT, null));
        replaySystem.addRequests(new Request(TRIGGER_REQUEST_FORWARD, null));
        replaySystem.addRequests(new Request(TRIGGER_REQUEST_TURNLEFT, null));
        replaySystem.addRequests(new Request(TRIGGER_REQUEST_FORWARD, null));

        // 30 is not sufficient
        sceneRunner.runLimitedFrames(50);

        EcsEntity player = MazeUtils.getMainPlayer();
        MoverComponent mc = MoverComponent.getMoverComponent(player);
        TestUtil.assertPoint("player location", new Point(7, 2), mc.getLocation());

        replaySystem.addRequests(new Request(TRIGGER_REQUEST_FORWARD, null));
        replaySystem.addRequests(new Request(TRIGGER_REQUEST_TURNLEFT, null));
        sceneRunner.runLimitedFrames(50);
        TestUtil.assertPoint("player location", new Point(7, 3), mc.getLocation());
        TestUtil.assertVector3("player location", MazeUtils.point2Vector3(new Point(7, 3)), player.getSceneNode().getTransform().getPosition());

        replaySystem.addRequests(new Request(TRIGGER_REQUEST_KICK, null));
        sceneRunner.runLimitedFrames(50);
        TestUtil.assertPoint("player location", new Point(7, 3), mc.getLocation());
        TestUtil.assertVector3("player location", MazeUtils.point2Vector3(new Point(7, 3)), player.getSceneNode().getTransform().getPosition());

        replaySystem.addRequests(new Request(TRIGGER_REQUEST_PULL, null));
        sceneRunner.runLimitedFrames(50);
        TestUtil.assertPoint("player location", new Point(7, 3), mc.getLocation());
        TestUtil.assertVector3("player location", MazeUtils.point2Vector3(new Point(7, 3)), player.getSceneNode().getTransform().getPosition());

    }

    /**
     * +---------+
     * |         |
     * |O -| | - |
     * |   | |   |
     * | --+ +-+ |
     * |    x    |
     * +---------+
     */
    @Test
    public void testGrid1() {
        //grid1.txt
        setup("maze/grid1.txt");

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());
        assertEquals("number of entities (0 boxes)", 0, SystemManager.findEntities((EntityFilter) null).size());

        assertTrue(SystemState.readyToJoin());

        SystemManager.putRequest(new Request(UserSystem.USER_REQUEST_LOGIN, new Payload("")));

        sceneRunner.runLimitedFrames(5);
        assertEquals(INITIAL_FRAMES + 5, sceneRunner.getFrameCount());

        assertEquals("number of entites (avatar)", 1, SystemManager.findEntities((EntityFilter) null).size());

        List<EcsEntity> players = MazeUtils.getPlayer();
        assertEquals("number of player (avatar)", 1, players.size());

        EcsEntity player = players.get(0);
        MoverComponent mc = MoverComponent.getMoverComponent(player);
        assertNotNull("player.MoverComponent", mc);
        Point start = new Point(5, 1);
        TestUtil.assertPoint("start point", start, mc.getLocation());
        // Die Mover liegen alle auf y0!
        Vector3 startLoc = MazeUtils.point2Vector3(new Point(5, 1));
        TestUtil.assertVector3("xyz start", startLoc, mc.getMovable().getPosition());
        //da fehlt doch was by y??
        TestUtil.assertFloat("camera.world.y", 0.6 + 0.75, observerDummy.getTransform().getWorldModelMatrix().extractPosition().getY());

        SystemManager.putRequest(new Request(TRIGGER_REQUEST_FIRE, new Payload("")));
        sceneRunner.runLimitedFrames(1);

        // no balls without bot
        //assertEquals("number of entites (avatar+ball)", 2, SystemManager.findEntities((EntityFilter) null).size());
        assertEquals("number of entites (avatar)", 1, SystemManager.findEntities((EntityFilter) null).size());

    }

    /**
     * ###############
     * #     %       #
     * #  #        # #
     * #  ## D # D## #
     * #  #   ###  # #
     * #     @ #     #
     * #    #D ####  #
     * #   ###   #D  #
     * #             #
     * ###############
     */
    @Test
    public void testArea15x10() {

        setup("maze/Area15x10.txt");

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());
        assertEquals("number of entities (1 bot, 4 diamonds, 2 balls)", 7, SystemManager.findEntities((EntityFilter) null).size());
        assertEquals("number of entities (2 balls, 4 diamonds)", 2 + 4, SystemManager.findEntities(new ItemFilter()).size());

        assertTrue(SystemState.readyToJoin());

        SystemManager.putRequest(new Request(UserSystem.USER_REQUEST_LOGIN, new Payload("")));

        sceneRunner.runLimitedFrames(5);
        assertEquals(INITIAL_FRAMES + 5, sceneRunner.getFrameCount());

        assertEquals("number of entites (1 bot, 4 diamonds, 2 balls+avatar+3 balls)", 7 + 4, SystemManager.findEntities((EntityFilter) null).size());
        assertEquals("number of entities (5 balls, 4 diamonds)", 5 + 4, SystemManager.findEntities(new ItemFilter()).size());

        List<EcsEntity> players = MazeUtils.getPlayer();
        assertEquals("number of player (bot+avatar)", 2, players.size());

        EcsEntity bot = players.get(0);
        MoverComponent mc = MoverComponent.getMoverComponent(bot);
        assertNotNull("MoverComponent", mc);
        assertEquals("bot name", "Bot", bot.getName());

        EcsEntity player = players.get(1);
        mc = MoverComponent.getMoverComponent(player);
        assertNotNull("MoverComponent", mc);
        assertEquals("player name", "Player", player.getName());

        List<EcsEntity> inventory = MazeUtils.getInventory(player);
        assertEquals("inventory size", 3, inventory.size());
        assertEquals("bullets", 3, MazeUtils.getBullets(player).size());

        SystemManager.putRequest(new Request(TRIGGER_REQUEST_FIRE, new Payload("")));
        sceneRunner.runLimitedFrames(1);
        assertEquals("bullets", 2, MazeUtils.getBullets(player).size());

        assertEquals("number of entites (avatar+bot+4 diamonds+5 balls)", 1 + 1 + 4 + 5, SystemManager.findEntities((EntityFilter) null).size());


    }

    /**
     * Title=2
     * ############
     * #..  #     ###
     * #..  # $  $  #
     * #..  #$####  #
     * #..    @ ##  #
     * #..  # #  $ ##
     * ###### ##$ $ #
     *   # $  $ $ $ #
     *   #    #     #
     *   ############
     */
    @Test
    public void testDavidJoffe2() {
        //grid1.txt
        setup("skbn/DavidJoffe.txt:2");

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());
        assertEquals("number of entities (0 boxes)", 10, SystemManager.findEntities((EntityFilter) null).size());

        assertTrue(SystemState.readyToJoin());

        SystemManager.putRequest(new Request(UserSystem.USER_REQUEST_LOGIN, new Payload("")));

        sceneRunner.runLimitedFrames(5);
        assertEquals(INITIAL_FRAMES + 5, sceneRunner.getFrameCount());

        assertEquals("number of entites (avatar+boxes)", 1+10, SystemManager.findEntities((EntityFilter) null).size());

    }
}