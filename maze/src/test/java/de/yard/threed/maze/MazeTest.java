package de.yard.threed.maze;

import de.yard.threed.core.Event;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
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
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.maze.testutils.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.yard.threed.maze.RequestRegistry.*;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests using ECS (See GridTest for tests without ECS).
 * But NO visualization to reveal model-view coupling.
 */
public class MazeTest {

    static final int INITIAL_FRAMES = 10;

    SceneRunnerForTesting sceneRunner;

    ReplaySystem replaySystem;

    SceneNode observerDummy;

    /**
     * Wegen parameter nicht als @Before
     */
    private void setup(String levelname, boolean withBotSystem) {
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
                if (withBotSystem) {
                    SystemManager.addSystem(new BotSystem());
                }
            }
        };

        TestFactory.initPlatformForTest(new String[]{"engine", "maze", "data"}, new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting()), initMethod);

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

        setup("skbn/SokobanWikipedia.txt", false);

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());
        assertEquals(2, SystemManager.findEntities((EntityFilter) null).size(), "number of entities (2 boxes)");

        assertTrue(SystemState.readyToJoin());

        SystemManager.putRequest(UserSystem.buildLoginRequest("", ""));

        sceneRunner.runLimitedFrames(5);
        assertEquals(INITIAL_FRAMES + 5, sceneRunner.getFrameCount());
        List<Event> loginEvents = EcsTestHelper.getEventsFromHistory(UserSystem.USER_EVENT_LOGGEDIN);
        assertEquals(1, loginEvents.size());
        int userEntityId = (Integer) loginEvents.get(0).getPayloadByIndex(2);

        assertEquals(2 + 1, SystemManager.findEntities((EntityFilter) null).size(), "number of entites (2 boxes+avatar)");

        assertEquals(new GridOrientation().toString(), MazeUtils.getPlayerorientation(MazeUtils.getMainPlayer()).toString(), "initial orientation");
        assertEquals(new Point(6, 1).toString(), MazeUtils.getPlayerposition(MazeUtils.getMainPlayer()).toString(), "initial location");

        replaySystem.addRequests(new Request(TRIGGER_REQUEST_TURNRIGHT, userEntityId));
        replaySystem.addRequests(new Request(TRIGGER_REQUEST_FORWARD, userEntityId));
        replaySystem.addRequests(new Request(TRIGGER_REQUEST_TURNLEFT, userEntityId));
        replaySystem.addRequests(new Request(TRIGGER_REQUEST_FORWARD, userEntityId));

        // 30 is not sufficient
        sceneRunner.runLimitedFrames(50);

        EcsEntity player = MazeUtils.getMainPlayer();
        MoverComponent mc = MoverComponent.getMoverComponent(player);
        TestUtil.assertPoint("player location", new Point(7, 2), mc.getLocation());

        replaySystem.addRequests(new Request(TRIGGER_REQUEST_FORWARD, userEntityId));
        replaySystem.addRequests(new Request(TRIGGER_REQUEST_TURNLEFT, userEntityId));
        sceneRunner.runLimitedFrames(50);
        TestUtil.assertPoint("player location", new Point(7, 3), mc.getLocation());
        TestUtil.assertVector3("player location", MazeUtils.point2Vector3(new Point(7, 3)), player.getSceneNode().getTransform().getPosition());

        replaySystem.addRequests(new Request(TRIGGER_REQUEST_KICK, userEntityId));
        sceneRunner.runLimitedFrames(50);
        TestUtil.assertPoint("player location", new Point(7, 3), mc.getLocation());
        TestUtil.assertVector3("player location", MazeUtils.point2Vector3(new Point(7, 3)), player.getSceneNode().getTransform().getPosition());

        replaySystem.addRequests(new Request(TRIGGER_REQUEST_PULL, userEntityId));
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
        setup("maze/grid1.txt", false);

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());
        assertEquals(0, SystemManager.findEntities((EntityFilter) null).size(), "number of entities (0 boxes)");

        assertTrue(SystemState.readyToJoin());

        SystemManager.putRequest(UserSystem.buildLoginRequest("", ""));

        sceneRunner.runLimitedFrames(5);
        assertEquals(INITIAL_FRAMES + 5, sceneRunner.getFrameCount());

        assertEquals(1, SystemManager.findEntities((EntityFilter) null).size(), "number of entites (avatar)");

        List<EcsEntity> players = MazeUtils.getPlayerOrBoxes(false);
        assertEquals(1, players.size(), "number of player (avatar)");

        EcsEntity player = players.get(0);
        MoverComponent mc = MoverComponent.getMoverComponent(player);
        assertNotNull(mc, "player.MoverComponent");
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
        assertEquals(1, SystemManager.findEntities((EntityFilter) null).size(), "number of entites (avatar)");

    }

    /**
     * ###############
     * #     @       #
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

        setup("maze/Area15x10.txt", true);

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());
        assertEquals(4, EcsHelper.findAllEntities().size(), "number of entities (4 diamonds)");
        assertEquals(4, MazeUtils.getItems(-1).size(), "number of entities (4 diamonds)");

        assertTrue(SystemState.readyToJoin());

        SystemManager.putRequest(UserSystem.buildLoginRequest("", ""));

        sceneRunner.runLimitedFrames(5);
        assertEquals(INITIAL_FRAMES + 5, sceneRunner.getFrameCount());

        assertEquals(1 + 1 + 4 + 2 * 3, EcsHelper.findAllEntities().size(), "number of entites (1 player + 1 bot, 4 diamonds, 2 * 3 balls)");
        assertEquals(2 * 3 + 4, MazeUtils.getItems(-1).size(), "number of entities (6 balls, 4 diamonds)");

        List<EcsEntity> players = MazeUtils.getPlayerOrBoxes(false);
        assertEquals(2, players.size(), "number of player (player+bot)");

        EcsEntity player = players.get(0);
        MoverComponent mc = MoverComponent.getMoverComponent(player);
        assertNotNull(mc, "MoverComponent");
        assertEquals("User0", player.getName(), "player name");

        EcsEntity bot = players.get(1);
        mc = MoverComponent.getMoverComponent(bot);
        assertNotNull(mc, "MoverComponent");
        // a bot is just a user
        assertEquals("User1", bot.getName(), "bot name");

        List<EcsEntity> inventory = MazeUtils.getInventory(player);
        assertEquals(3, inventory.size(), "inventory size");
        assertEquals(3, MazeUtils.getBullets(player).size(), "bullets");

        SystemManager.putRequest(new Request(TRIGGER_REQUEST_FIRE, new Payload("")));
        sceneRunner.runLimitedFrames(1);
        assertEquals(3 - 1, MazeUtils.getBullets(player).size(), "bullets");
        assertEquals(1 + 1 + 4 + 2 * 3, EcsHelper.findAllEntities().size(), "number of entites (player+bot+4 diamonds+2*3 balls)");


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
     * _ # $  $ $ $ #
     * _ #    #     #
     * _ ############
     */
    @Test
    public void testDavidJoffe2() {
        //grid1.txt
        setup("skbn/DavidJoffe.txt:2", false);

        assertEquals(INITIAL_FRAMES, sceneRunner.getFrameCount());
        assertEquals(10, SystemManager.findEntities((EntityFilter) null).size(), "number of entities (0 boxes)");

        assertTrue(SystemState.readyToJoin());

        SystemManager.putRequest(UserSystem.buildLoginRequest("", ""));

        sceneRunner.runLimitedFrames(5);
        assertEquals(INITIAL_FRAMES + 5, sceneRunner.getFrameCount());

        assertEquals(1 + 10, SystemManager.findEntities((EntityFilter) null).size(), "number of entites (avatar+boxes)");

    }

    /**
     * ##########
     * #   @    #
     * #   # #  #
     * #   # #  #
     * #    @   #
     * ##########
     */
    @Test
    public void testSimpleMultiplayer() {

        setup("maze/Maze-P-Simple.txt", false);

        // no boxes, no player
        assertEquals(0, SystemManager.findEntities((EntityFilter) null).size(), "number of entities");
        assertEquals(0, MazeUtils.getPlayer().size(), "number of player");
        assertNull(MazeUtils.getMainPlayer());

        assertTrue(SystemState.readyToJoin());
        SystemManager.putRequest(UserSystem.buildLoginRequest("u0", ""));
        sceneRunner.runLimitedFrames(5);

        assertEquals(1 + 3, SystemManager.findEntities((EntityFilter) null).size(), "number of entites (one player+3 bullets)");
        assertEquals(1, MazeUtils.getPlayer().size(), "number of player");
        assertNotNull(MazeUtils.getMainPlayer());
        EcsEntity user0 = MazeUtils.getPlayerByUsername("u0");
        assertNotNull(user0);
        assertEquals(Direction.N.toString(), MazeUtils.getPlayerorientation(user0).getDirection().toString(), "user0 initial orientation");

        SystemManager.putRequest(UserSystem.buildLoginRequest("u1", ""));
        sceneRunner.runLimitedFrames(5);

        assertEquals(2 + 2 * 3, SystemManager.findEntities((EntityFilter) null).size(), "number of entites (two player+2*3 bullets)");
        assertEquals(2, MazeUtils.getPlayer().size(), "number of player");
        assertNotNull(MazeUtils.getMainPlayer());
        EcsEntity user1 = MazeUtils.getPlayerByUsername("u1");
        assertNotNull(user1);
        assertEquals(Direction.E.toString(), MazeUtils.getPlayerorientation(user1).getDirection().toString(), "user1 initial orientation");

        // don't expect 3rd user (however, login should be possible)
        SystemManager.putRequest(UserSystem.buildLoginRequest("u2", ""));
        sceneRunner.runLimitedFrames(5);
        assertEquals(2, MazeUtils.getPlayer().size(), "number of player");

        assertPosition(user1, new Point(4, 4));

        SystemManager.putRequest(new Request(TRIGGER_REQUEST_FORWARD, user1.getId()));
        sceneRunner.runLimitedFrames(3, 0);
        assertTrue(MazeUtils.isAnyMoving());
        // 5 seems to be sufficient for completing the move
        sceneRunner.runLimitedFrames(5);
        assertPosition(user1, new Point(5, 4));
        assertFalse(MazeUtils.isAnyMoving());

    }

    /**
     * ##########
     * #   @    #
     * #   # #  #
     * #   # #  #
     * #    @   #
     * ##########
     */
    @Test
    public void testSimpleMultiplayerWithBot() {

        setup("maze/Maze-P-Simple.txt", true);

        // no boxes, no player
        assertEquals(0, SystemManager.findEntities((EntityFilter) null).size(), "number of entities");
        assertEquals(0, MazeUtils.getPlayer().size(), "number of player");
        assertNull(MazeUtils.getMainPlayer());

        assertTrue(SystemState.readyToJoin());
        SystemManager.putRequest(UserSystem.buildLoginRequest("u0", ""));
        sceneRunner.runLimitedFrames(5);

        assertEquals(1 + 1 + 2 * 3, SystemManager.findEntities((EntityFilter) null).size(), "number of entites (one player + one bot + 2*3 bullets)");
        assertEquals(2, MazeUtils.getPlayer().size(), "number of player");
        assertNotNull(MazeUtils.getMainPlayer());
        EcsEntity user0 = MazeUtils.getPlayerByUsername("u0");
        assertNotNull(user0);
        EcsEntity user1 = MazeUtils.getPlayerByUsername("bot0");
        assertNotNull(user1);

    }

    void assertPosition(EcsEntity user, Point point) {
        MoverComponent mc = MoverComponent.getMoverComponent(user);
        assertNotNull(mc, "user1.MoverComponent");
        TestUtil.assertPoint(" point", point, mc.getLocation());
    }

}