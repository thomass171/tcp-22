package de.yard.threed.maze;

import de.yard.threed.core.Event;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.Observer;
import de.yard.threed.core.Point;
import de.yard.threed.engine.ObserverSystem;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.*;
import de.yard.threed.core.InitMethod;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.maze.testutils.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
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
                SystemManager.addSystem(new MazeMovingAndStateSystem());
                SystemManager.addSystem(new UserSystem());

                AvatarSystem avatarSystem = new AvatarSystem();
                avatarSystem.setAvatarBuilder("avatar", new MazeAvatarBuilder());
                SystemManager.addSystem(avatarSystem);

                ObserverSystem observerSystem = new ObserverSystem();
                observerSystem.setViewTransform(MazeScene.getViewTransform());
                SystemManager.addSystem(observerSystem);

                SystemManager.addSystem(new BulletSystem());
                replaySystem = new ReplaySystem();
                SystemManager.addSystem(replaySystem);
                if (withBotSystem) {
                    SystemManager.addSystem(new BotSystem(false));
                }
                MazeDataProvider.reset();
                MazeDataProvider.init(levelname);
            }
        };

        MazeSettings.init(MazeSettings.MODE_SOKOBAN);

        TestFactory.initPlatformForTest(new String[]{"engine", "maze", "data"}, new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting()), initMethod,
                Configuration.buildDefaultConfigurationWithEnv(new HashMap<>()));

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
        assertEquals(new Point(6, 1).toString(), MazeUtils.getMoverposition(MazeUtils.getMainPlayer()).toString(), "initial location");

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

        SystemManager.putRequest(BulletSystem.buildFireRequest(player.getId(), mc.getGridOrientation().getDirectionForMovement(GridMovement.Forward)));
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
        assertEquals(4, MazeUtils.getAllItems().size(), "number of entities (4 diamonds)");

        assertTrue(SystemState.readyToJoin());

        SystemManager.putRequest(UserSystem.buildLoginRequest("aaa", ""));

        sceneRunner.runLimitedFrames(5);
        assertEquals(INITIAL_FRAMES + 5, sceneRunner.getFrameCount());

        assertEquals(1 + 1 + 4 + 2 * 3, EcsHelper.findAllEntities().size(), "number of entites (1 player + 1 bot, 4 diamonds, 2 * 3 balls)");
        assertEquals(2 * 3 + 4, MazeUtils.getAllItems().size(), "number of entities (6 balls, 4 diamonds)");

        List<EcsEntity> players = MazeUtils.getPlayerOrBoxes(false);
        assertEquals(2, players.size(), "number of player (player+bot)");

        EcsEntity player = players.get(0);
        MoverComponent mc = MoverComponent.getMoverComponent(player);
        assertNotNull(mc, "MoverComponent");
        assertEquals("aaa", player.getName(), "player name");

        EcsEntity bot = players.get(1);
        mc = MoverComponent.getMoverComponent(bot);
        assertNotNull(mc, "MoverComponent");
        // a bot is not just a user
        assertEquals("Bot0", bot.getName(), "bot name");

        List<EcsEntity> inventory = MazeUtils.getInventory(player);
        assertEquals(3, inventory.size(), "inventory size");
        assertEquals(3, MazeUtils.getBullets(player).size(), "bullets");

        // firing from home field should be ignored
        SystemManager.putRequest(BulletSystem.buildFireRequest(player.getId(), MoverComponent.getMoverComponent(player).getGridOrientation().getDirectionForMovement(GridMovement.Forward)));
        sceneRunner.runLimitedFrames(1);
        assertEquals(3, MazeUtils.getBullets(player).size(), "bullets");
        TestUtils.ecsWalk(sceneRunner, player, true, new Point(6, 5));

        // but from regular field is should be possible
        SystemManager.putRequest(BulletSystem.buildFireRequest(player.getId(), MoverComponent.getMoverComponent(player).getGridOrientation().getDirectionForMovement(GridMovement.Forward)));
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
     * A monster is a bot player.
     * ##########
     * #   M    #
     * #  D# #  #
     * #   # #  #
     * #    P D #
     * ##########
     */
    @Test
    public void testSimpleMultiplayer() {

        setup("maze/Maze-P-Simple.txt", false);

        List<EcsEntity> users = initMaze_P(2);
        EcsEntity user0 = users.get(0);
        EcsEntity user1 = users.get(1);

        // firing from home field should be ignored
        SystemManager.putRequest(BulletSystem.buildFireRequest(user0.getId(), MoverComponent.getMoverComponent(user0).getGridOrientation().getDirectionForMovement(GridMovement.Forward)));
        sceneRunner.runLimitedFrames(1);
        assertEquals(3, MazeUtils.getBullets(user0).size(), "bullets");

        // Step forward user0 and fire again
        TestUtils.ecsWalk(sceneRunner, user0, true, new Point(5, 2));
        SystemManager.putRequest(BulletSystem.buildFireRequest(user0.getId(), MoverComponent.getMoverComponent(user0).getGridOrientation().getDirectionForMovement(GridMovement.Forward)));
        sceneRunner.runLimitedFrames(1);
        assertEquals(3 - 1, MazeUtils.getBullets(user0).size(), "bullets");
        assertEquals(3, MazeUtils.getBullets(user1).size(), "bullets");

        List<EcsEntity> flyingBullets = TestUtils.getFlyingBullets();
        assertEquals(1, flyingBullets.size());
        EcsEntity flyingBullet = flyingBullets.get(0);
        BulletComponent bc = BulletComponent.getBulletComponent(flyingBullet);
        assertNotNull(bc);
        EcsTestHelper.processUntil(() -> {
            return !bc.isFlying();
        }, 0.1, 1000);
        assertTrue(bc.isOnGround());

        // Step forward user1 and pick up the ball.
        TestUtils.ecsWalk(sceneRunner, user1, true, new Point(5, 4));
        assertEquals(3 + 1, MazeUtils.getBullets(user1).size(), "bullets");

        // Step forward user0
        TestUtils.ecsWalk(sceneRunner, user0, true, new Point(5, 3));
        // But shouldn't reach field of user1
        TestUtils.ecsWalk(sceneRunner, user0, false, new Point(5, 3));


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
    public void testSimpleMultiplayerWithBot() {

        setup("maze/Maze-P-Simple.txt", true);

        //ready for botsystem? initMaze_P_Simple();

        // no boxes, no player, 2 diamond
        assertEquals(2, SystemManager.findEntities((EntityFilter) null).size(), "number of entities");
        assertEquals(0, MazeUtils.getPlayer().size(), "number of player");
        assertNull(MazeUtils.getMainPlayer());

        assertTrue(SystemState.readyToJoin());
        SystemManager.putRequest(UserSystem.buildLoginRequest("u0", ""));
        sceneRunner.runLimitedFrames(5);

        assertEquals(1 + 1 + 2 * 3 + 2, SystemManager.findEntities((EntityFilter) null).size(), "number of entites (one player + one bot + 2*3 bullets)");
        assertEquals(2, MazeUtils.getPlayer().size(), "number of player");
        EcsEntity user0 = MazeUtils.getPlayerByUsername("u0");
        assertNotNull(user0);
        EcsEntity user1 = EcsHelper.findEntitiesByName("Bot0").get(0);
        assertNotNull(user1);

        assertEquals(1, SceneNode.findByName("Monster").size());
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
    public void testCollect() throws Exception {

        setup("maze/Maze-P-Simple.txt", false);

        List<EcsEntity> users = initMaze_P(2);
        EcsEntity user0 = users.get(0);
        EcsEntity user1 = users.get(1);

        TestUtils.ecsWalk(TRIGGER_REQUEST_LEFT, sceneRunner, user0, true, new Point(4, 1));
        TestUtils.ecsWalk(TRIGGER_REQUEST_LEFT, sceneRunner, user0, true, new Point(3, 1));
        TestUtils.ecsWalk(TRIGGER_REQUEST_FORWARD, sceneRunner, user0, true, new Point(3, 2));
        assertEquals(3, MazeUtils.getInventory(user0).size(), "inventory (3 bullets)");

        TestUtils.ecsWalk(TRIGGER_REQUEST_FORWARD, sceneRunner, user0, true, new Point(3, 3));
        assertEquals(3 + 1, MazeUtils.getInventory(user0).size(), "inventory (3 bullets, 1 diamond)");

        GridState currentstate = MazeUtils.buildGridStateFromEcs();
        assertFalse(currentstate.isSolved(Grid.getInstance().getMazeLayout()));
        TestUtils.ecsWalk(buildRelocate(user0.getId(), new Point(7, 1), GridOrientation.fromDirection("S")), sceneRunner, user0, false, new Point(7, 1));
        assertEquals(3 + 2, MazeUtils.getInventory(user0).size(), "inventory (3 bullets, 2 diamond)");
        assertEquals(GridOrientation.fromDirection("S").toString(), MazeUtils.getPlayerorientation(user0).toString(), "orientation after teleport (should be SOUTH)");
        assertTrue(currentstate.isSolved(Grid.getInstance().getMazeLayout()));
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
    public void testM30x20WithBot() {

        setup("maze/Maze-M-30x20.txt", true);

        //ready for botsystem? initMaze_P_Simple();

        // no boxes, 0 player, 0 diamond
        assertEquals(0, SystemManager.findEntities((EntityFilter) null).size(), "number of entities");
        assertEquals(0, MazeUtils.getPlayer().size(), "number of player");
        assertNull(MazeUtils.getMainPlayer());

        assertTrue(SystemState.readyToJoin());
        SystemManager.putRequest(UserSystem.buildLoginRequest("u0", ""));
        sceneRunner.runLimitedFrames(5);

        assertEquals(1 + 3 + 4 * 3, SystemManager.findEntities((EntityFilter) null).size(), "number of entites (1 player + 3 bot + 4*3 bullets)");
        assertEquals(4, MazeUtils.getPlayer().size(), "number of player");
        EcsEntity user0 = MazeUtils.getPlayerByUsername("u0");
        assertNotNull(user0);
        EcsEntity user1 = EcsHelper.findEntitiesByName("Bot0").get(0);
        assertNotNull(user1);

        assertEquals(12, MazeUtils.buildItemsFromEcs().size());
        GridState currentstate = MazeUtils.buildGridStateFromEcs();
        assertEquals(4, currentstate.players.size());
        assertEquals(12, currentstate.items.size());
        assertEquals(0, currentstate.boxes.size());
        assertFalse(currentstate.isSolved(Grid.getInstance().getMazeLayout()));
        assertEquals(3, SceneNode.findByName("Monster").size());
    }

    /**
     * Without diamonds its solved immediately.
     */
    @Test
    public void testBot() throws Exception {

        setup("##########\n" +
                "#  #@#D  #\n" +
                "#        #\n" +
                "#        #\n" +
                "#   @    #\n" +
                "##########", true);

        // initMaze_P not suited for bot system
        SystemManager.putRequest(UserSystem.buildLoginRequest("u0", ""));
        sceneRunner.runLimitedFrames(3);

        assertEquals(1 + 3 + 1 + 3 + 1, SystemManager.findEntities((EntityFilter) null).size(), "number of entites (one player+3 bullets+bot with bullets +1 diamond)");
        assertEquals(2, MazeUtils.getPlayer().size(), "number of player");
        EcsEntity user = MazeUtils.getPlayerByUsername("u0");
        assertNotNull(user);
        EcsEntity bot = MazeUtils.getPlayer().get(1);
        assertNotNull(bot);
        assertEquals(3, MazeUtils.getBullets(bot).size());

        // bot must/will leave home field to make firing possible. By default it will wait real time for next move.

        EcsTestHelper.processUntil(() -> {
            return MazeUtils.getBullets(bot).size() < 3;
        }, 0.1, 100000000);

        // Anyway, user is on home filed and cannot be hit.
    }

    /**
     * Without diamonds its solved immediately.
     */
    @Test
    public void testMultiBot() throws Exception {

        setup("##########\n" +
                "###   D  #\n" +
                "#@@      #\n" +
                "#        #\n" +
                "#   @    #\n" +
                "##########", true);

        // initMaze_P not suited for bot system
        SystemManager.putRequest(UserSystem.buildLoginRequest("u0", ""));
        sceneRunner.runLimitedFrames(3);

        assertEquals(1 + 3 + 2 * (1 + 3) + 1, SystemManager.findEntities((EntityFilter) null).size(),
                "number of entites (one player+3 bullets+2 bots with bullets +1 diamond)");
        assertEquals(3, MazeUtils.getPlayer().size(), "number of player");
        EcsEntity user = MazeUtils.getPlayerByUsername("u0");
        assertNotNull(user);
        EcsEntity bot0 = MazeUtils.getPlayer().get(1);
        assertNotNull(bot0);
        EcsEntity bot1 = MazeUtils.getPlayer().get(2);
        assertNotNull(bot1);
        assertEquals(3, MazeUtils.getBullets(bot0).size());

        // bot must/will leave home field to make firing possible. By default it will wait real time for next move.

        TestUtils.assertPosition(bot0, new Point(1, 3));
        assertEquals(Direction.E.toString(), MazeUtils.getPlayerorientation(bot0).getDirection().toString(), "bot0 initial orientation");
        TestUtils.assertPosition(bot1, new Point(2, 3));
        assertEquals(Direction.E.toString(), MazeUtils.getPlayerorientation(bot1).getDirection().toString(), "bot1 initial orientation");

        DeterministicBotAI dbAI0=new DeterministicBotAI(new Request[]{new Request(TRIGGER_REQUEST_FORWARD)});
        DeterministicBotAI dbAI1=new DeterministicBotAI(new Request[]{new Request(TRIGGER_REQUEST_FORWARD)});
        BotComponent.getBotComponent(bot0).setBotAI(dbAI0);
        BotComponent.getBotComponent(bot1).setBotAI(dbAI1);
        // bot by default will wait real time for next move.
        EcsTestHelper.processUntil(() -> {
            return dbAI0.index < 1 && dbAI1.index < 1;
        }, 0.1, 100000000);

        //TestUtils.assertPosition(bot0, new Point(2, 3));
        TestUtils.assertPosition(bot1, new Point(3, 3));


    }

    /**
     * Launch all player, check items
     *
     * @return list of player
     */
    private List<EcsEntity> initMaze_P(int diamonds) {
        // no boxes, no player, two diamond
        assertEquals(diamonds, SystemManager.findEntities((EntityFilter) null).size(), "number of entities");
        assertEquals(0, MazeUtils.getPlayer().size(), "number of player");
        assertNull(MazeUtils.getMainPlayer());

        assertTrue(SystemState.readyToJoin());
        SystemManager.putRequest(UserSystem.buildLoginRequest("u0", ""));
        sceneRunner.runLimitedFrames(5);

        assertEquals(1 + 3 + diamonds, SystemManager.findEntities((EntityFilter) null).size(), "number of entites (one player+3 bullets+2 diamond)");
        assertEquals(1, MazeUtils.getPlayer().size(), "number of player");
        assertNotNull(MazeUtils.getMainPlayer());
        EcsEntity user0 = MazeUtils.getPlayerByUsername("u0");
        assertNotNull(user0);
        assertEquals(Direction.N.toString(), MazeUtils.getPlayerorientation(user0).getDirection().toString(), "user0 initial orientation");
        assertEquals("Avatar", Observer.getInstance().getTransform().getParent().getSceneNode().getName(), "parent of observer");
        // only native transforms are static
        assertEquals(user0.scenenode.getTransform().transform, Observer.getInstance().getTransform().getParent().transform, "parent of observer");

        SystemManager.putRequest(UserSystem.buildLoginRequest("u1", ""));
        sceneRunner.runLimitedFrames(5);

        assertEquals(2 + 2 * 3 + diamonds, SystemManager.findEntities((EntityFilter) null).size(), "number of entites (two player+2*3 bullets+2 diamond)");
        assertEquals(2, MazeUtils.getPlayer().size(), "number of player");
        assertNotNull(MazeUtils.getMainPlayer());
        EcsEntity user1 = MazeUtils.getPlayerByUsername("u1");
        assertNotNull(user1);
        assertEquals(Direction.E.toString(), MazeUtils.getPlayerorientation(user1).getDirection().toString(), "user1 initial orientation");
        // observer should stay attached to first player
        assertEquals("Avatar", Observer.getInstance().getTransform().getParent().getSceneNode().getName(), "parent of observer");
        // only native transforms are static
        assertEquals(user0.scenenode.getTransform().transform, Observer.getInstance().getTransform().getParent().transform, "parent of observer");

        // don't expect 3rd user (however, login should be possible)
        SystemManager.putRequest(UserSystem.buildLoginRequest("u2", ""));
        sceneRunner.runLimitedFrames(5);
        assertEquals(2, MazeUtils.getPlayer().size(), "number of player");
        TestUtils.assertPosition(user1, new Point(4, 4));

        // both user still on their start position and have 3 bullets
        assertEquals(3, MazeUtils.getBullets(user0).size(), "bullets");
        assertEquals(3, MazeUtils.getBullets(user1).size(), "bullets");
        assertEquals(3 + 3 + diamonds, MazeUtils.getAllItems().size(), "(2*3 bullets, 2 diamond)");

        return Arrays.asList(user0, user1);
    }
}