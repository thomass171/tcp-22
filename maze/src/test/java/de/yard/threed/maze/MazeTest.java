package de.yard.threed.maze;

import com.github.tomakehurst.wiremock.WireMockServer;
import de.yard.threed.core.Event;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.Observer;
import de.yard.threed.core.Point;
import de.yard.threed.engine.ObserverSystem;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.*;
import de.yard.threed.core.InitMethod;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.testutil.SceneRunnerForTesting;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.maze.testutils.EmptyBotAIBuilder;
import de.yard.threed.maze.testutils.MazeTestUtils;
import de.yard.threed.maze.testutils.TestingBotAiBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static de.yard.threed.maze.MazeRequestRegistry.*;
import static de.yard.threed.maze.MazeTheme.THEME_TRADITIONAL;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests using ECS (See GridTest for tests without ECS).
 * But NO visualization to reveal model-view coupling.
 */
@Slf4j
public class MazeTest {

    static final int INITIAL_FRAMES = 10;

    SceneRunnerForTesting sceneRunner;

    ReplaySystem replaySystem;

    SceneNode observerDummy;

    private BotSystem botSystem = null;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setup() {
        SystemState.state = 0;
        MazeDataProvider.reset();
        SystemManager.reset();

        wireMockServer = new WireMockServer(wireMockConfig().port(8089));
        wireMockServer.start();
    }

    /**
     * Due to parameter not in @Before
     */
    private void setupTest(String levelname, BotAiBuilder botAiBuilder, boolean replaceMonsterWithPlayer) {
        InitMethod initMethod = new InitMethod() {
            @Override
            public void init() {

                MazeTheme st = MazeTheme.buildFromIdentifier(THEME_TRADITIONAL);

                // No visualization to reveal model-view coupling.
                SystemManager.addSystem(new MazeMovingAndStateSystem(st));
                SystemManager.addSystem(new UserSystem());

                AvatarSystem avatarSystem = new AvatarSystem();
                avatarSystem.setAvatarBuilder(MazeAvatarBuilder.AVATAR_BUILDER, new MazeAvatarBuilder(st.getMazeModelFactory()));
                avatarSystem.disableShortCutJoin();
                SystemManager.addSystem(avatarSystem);

                ObserverSystem observerSystem = new ObserverSystem();
                observerSystem.setViewTransform(MazeScene.getViewTransform(st));
                SystemManager.addSystem(observerSystem);

                SystemManager.addSystem(new BulletSystem());
                replaySystem = new ReplaySystem();
                SystemManager.addSystem(replaySystem);
                if (botAiBuilder != null) {
                    botSystem = new BotSystem(false, botAiBuilder);
                    SystemManager.addSystem(botSystem);
                }


            }
        };

        EngineTestFactory.initPlatformForTest(new String[]{"engine", "maze", "data"}, new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting()), initMethod,
                ConfigurationByEnv.buildDefaultConfigurationWithEnv(new HashMap<>()));

        if (replaceMonsterWithPlayer) {
            String fileContent = MazeUtils.readMazefile(levelname).replace("M", "P");
            MazeDataProvider.init(fileContent);
        } else {
            MazeDataProvider.init(levelname);
        }

        sceneRunner = (SceneRunnerForTesting) AbstractSceneRunner.instance;
        observerDummy = new SceneNode();
        Observer.buildForTransform(observerDummy.getTransform());
        sceneRunner.runLimitedFrames(INITIAL_FRAMES, 0.1, levelname.contains("http") ? 100 : 0);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void testSokobanWikipedia() {
        runSokobanWikipedia(false);
    }

    @Test
    public void testSokobanWikipediaRemoteGrid() throws Exception {
        MazeTestUtils.mockHttpGetSokobanWikipedia(wireMockServer);
        runSokobanWikipedia(true);
    }

    /**
     * ####
     * ###  ####
     * #     $ #
     * # #  #$ #
     * # . .#@ #
     * #########
     */
    public void runSokobanWikipedia(boolean remoteGrid) {

        setupTest(remoteGrid ? "http://localhost:" + wireMockServer.port() + "/mazes/1" : "skbn/SokobanWikipedia.txt", null, false);

        assertEquals(2, SystemManager.findEntities((EntityFilter) null).size(), "number of entities (2 boxes)");

        assertTrue(SystemState.readyToJoin());

        SystemManager.putRequest(UserSystem.buildLoginRequest("", ""));

        sceneRunner.runLimitedFrames(5);
        List<Event> loginEvents = EcsTestHelper.getEventsFromHistory(UserSystem.USER_EVENT_LOGGEDIN);
        assertEquals(1, loginEvents.size());
        int userEntityId = (Integer) loginEvents.get(0).getPayload().get("userentityid");

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
        TestUtils.assertPoint(new Point(7, 2), mc.getLocation(), "player location");

        replaySystem.addRequests(new Request(TRIGGER_REQUEST_FORWARD, userEntityId));
        replaySystem.addRequests(new Request(TRIGGER_REQUEST_TURNLEFT, userEntityId));
        sceneRunner.runLimitedFrames(50);
        TestUtils.assertPoint(new Point(7, 3), mc.getLocation(), "player location");
        TestUtils.assertVector3(MazeUtils.point2Vector3(new Point(7, 3)), player.getSceneNode().getTransform().getPosition(), "player location");

        replaySystem.addRequests(new Request(TRIGGER_REQUEST_KICK, userEntityId));
        sceneRunner.runLimitedFrames(50);
        TestUtils.assertPoint(new Point(7, 3), mc.getLocation(), "player location");
        TestUtils.assertVector3(MazeUtils.point2Vector3(new Point(7, 3)), player.getSceneNode().getTransform().getPosition(), "player location");

        replaySystem.addRequests(new Request(TRIGGER_REQUEST_PULL, userEntityId));
        sceneRunner.runLimitedFrames(50);
        TestUtils.assertPoint(new Point(7, 3), mc.getLocation(), "player location");
        TestUtils.assertVector3(MazeUtils.point2Vector3(new Point(7, 3)), player.getSceneNode().getTransform().getPosition(), "player location");

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
        setupTest("maze/grid1.txt", null, false);

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
        TestUtils.assertPoint(start, mc.getLocation(), "start point");
        // Die Mover liegen alle auf y0!
        Vector3 startLoc = MazeUtils.point2Vector3(new Point(5, 1));
        TestUtils.assertVector3(startLoc, mc.getMovable().getPosition(), "xyz start");
        //da fehlt doch was by y??
        Assertions.assertEquals(0.6 + 0.75, observerDummy.getTransform().getWorldModelMatrix().extractPosition().getY(), 0.000001, "camera.world.y");

        SystemManager.putRequest(MazeRequestRegistry.buildFireRequest(player.getId(), mc.getGridOrientation().getDirectionForMovement(GridMovement.Forward)));
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

        setupTest("maze/Area15x10.txt", new SimpleBotAiBuilder(), false);

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
        SystemManager.putRequest(MazeRequestRegistry.buildFireRequest(player.getId(), MoverComponent.getMoverComponent(player).getGridOrientation().getDirectionForMovement(GridMovement.Forward)));
        sceneRunner.runLimitedFrames(1);
        assertEquals(3, MazeUtils.getBullets(player).size(), "bullets");
        MazeTestUtils.ecsWalk(sceneRunner, player, true, new Point(6, 5));

        // but from regular field is should be possible
        SystemManager.putRequest(MazeRequestRegistry.buildFireRequest(player.getId(), MoverComponent.getMoverComponent(player).getGridOrientation().getDirectionForMovement(GridMovement.Forward)));
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
        setupTest("skbn/DavidJoffe.txt:2", null, false);

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
     * #   P    #
     * #  D# #  #
     * #   # #  #
     * #    P D #
     * ##########
     */
    @Test
    public void testSimpleMultiplayerWithoutBotSystem() {

        setupTest("maze/Maze-P-Simple.txt", null, true);

        List<EcsEntity> users = initMaze(new Point[]{new Point(5, 1), new Point(4, 4)}, new Direction[]{Direction.N, Direction.E},
                new Point[]{}, 2, 2);
        EcsEntity user0 = users.get(0);
        EcsEntity user1 = users.get(1);

        // firing from home field should be ignored
        SystemManager.putRequest(MazeRequestRegistry.buildFireRequest(user0.getId(), MoverComponent.getMoverComponent(user0).getGridOrientation().getDirectionForMovement(GridMovement.Forward)));
        sceneRunner.runLimitedFrames(1);
        assertEquals(3, MazeUtils.getBullets(user0).size(), "bullets");

        // Step forward user0 and fire again
        MazeTestUtils.ecsWalk(sceneRunner, user0, true, new Point(5, 2));
        SystemManager.putRequest(MazeRequestRegistry.buildFireRequest(user0.getId(), MoverComponent.getMoverComponent(user0).getGridOrientation().getDirectionForMovement(GridMovement.Forward)));
        sceneRunner.runLimitedFrames(1);
        assertEquals(3 - 1, MazeUtils.getBullets(user0).size(), "bullets");
        assertEquals(3, MazeUtils.getBullets(user1).size(), "bullets");

        List<EcsEntity> flyingBullets = MazeTestUtils.getFlyingBullets();
        assertEquals(1, flyingBullets.size());
        EcsEntity flyingBullet = flyingBullets.get(0);
        BulletComponent bc = BulletComponent.getBulletComponent(flyingBullet);
        assertNotNull(bc);
        EcsTestHelper.processUntil(() -> {
            return !bc.isFlying();
        }, 0.1, 1000);
        assertTrue(bc.isOnGround());

        // Step forward user1 and pick up the ball.
        MazeTestUtils.ecsWalk(sceneRunner, user1, true, new Point(5, 4));
        assertEquals(3 + 1, MazeUtils.getBullets(user1).size(), "bullets");

        // Step forward user0
        MazeTestUtils.ecsWalk(sceneRunner, user0, true, new Point(5, 3));
        // But shouldn't reach field of user1
        MazeTestUtils.ecsWalk(sceneRunner, user0, false, new Point(5, 3));


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

        TestingBotAiBuilder testingBotAiBuilder = new TestingBotAiBuilder();

        setupTest("maze/Maze-P-Simple.txt", testingBotAiBuilder, false);

        assertNull(MazeUtils.getMainPlayer());

        List<EcsEntity> users = initMaze(new Point[]{new Point(5, 1)}, new Direction[]{Direction.N},
                new Point[]{new Point(4, 4)}, 2, 1);

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
    public void testCollectWithoutBotSystem() throws Exception {

        setupTest("maze/Maze-P-Simple.txt", null, false);

        List<EcsEntity> users = initMaze(new Point[]{new Point(5, 1)}, new Direction[]{Direction.N},
                new Point[]{/*no bots without botsystem*/}, 2, 1);
        EcsEntity user0 = users.get(0);

        MazeTestUtils.ecsWalk(TRIGGER_REQUEST_LEFT, sceneRunner, user0, true, new Point(4, 1));
        MazeTestUtils.ecsWalk(TRIGGER_REQUEST_LEFT, sceneRunner, user0, true, new Point(3, 1));
        MazeTestUtils.ecsWalk(TRIGGER_REQUEST_FORWARD, sceneRunner, user0, true, new Point(3, 2));
        assertEquals(3, MazeUtils.getInventory(user0).size(), "inventory (3 bullets)");

        MazeTestUtils.ecsWalk(TRIGGER_REQUEST_FORWARD, sceneRunner, user0, true, new Point(3, 3));
        assertEquals(3 + 1, MazeUtils.getInventory(user0).size(), "inventory (3 bullets, 1 diamond)");

        GridState currentstate = MazeUtils.buildGridStateFromEcs();
        assertFalse(currentstate.isSolved(Grid.getInstance().getMazeLayout()));
        MazeTestUtils.ecsWalk(buildRelocate(user0.getId(), new Point(7, 1), GridOrientation.fromDirection("S")), sceneRunner, user0, false, new Point(7, 1));
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
    public void testM30x20WithMonsterWithBotSystem() {

        TestingBotAiBuilder testingBotAiBuilder = new TestingBotAiBuilder();

        setupTest("maze/Maze-M-30x20.txt", testingBotAiBuilder, false);

        List<EcsEntity> users = initMaze(new Point[]{new Point(28, 3)}, new Direction[]{Direction.N},
                new Point[]{new Point(21, 11),
                        new Point(22, 11),
                        new Point(23, 11),
                }, 0, 1);

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
     * BotSystem will start a bot for remaining player.
     */
    @Test
    public void testBotWithBotSystem() throws Exception {

        TestingBotAiBuilder testingBotAiBuilder = new TestingBotAiBuilder();

        setupTest("##########\n" +
                "#  #@#D  #\n" +
                "#        #\n" +
                "#        #\n" +
                "#   @    #\n" +
                "##########", testingBotAiBuilder, false);

        List<EcsEntity> users = initMaze(new Point[]{new Point(4, 1)}, new Direction[]{Direction.N},
                new Point[]{new Point(4, 4)}, 1, 2);

        EcsEntity user = users.get(0);
        assertNotNull(user);
        EcsEntity bot = users.get(1);
        assertNotNull(bot);
        assertEquals(3, MazeUtils.getBullets(bot).size());

        // bot must leave home field to make firing possible. With real AI it will wait real time for next move.
        // TestingAI is deterministic, so its not a question of time until the bot moves forward and fires.
        testingBotAiBuilder.ais.get(0).nextRequest = new Request(TRIGGER_REQUEST_FORWARD, bot.getId());
        sceneRunner.runLimitedFrames(5);
        testingBotAiBuilder.ais.get(0).nextRequest = new Request(TRIGGER_REQUEST_FIRE, bot.getId());
        sceneRunner.runLimitedFrames(5);

        // Anyway, user is on home field and cannot be hit.
        assertTrue(MazeUtils.getBullets(bot).size() < 3);
        MazeTestUtils.assertPositionAndOrientation(bot, new Point(4, 3), GridOrientation.fromDirection("S"));

        // wait for bullet that might still be on its way. Dont use high tpf. Makes bullet disappear and miss hit.
        // TODO smarter wait
        //EcsTestHelper.processSeconds(200);
        sceneRunner.runLimitedFrames(200, 0.1);
        // TODO check bullet location

        // disable bot auto move, step forward and fire at bot
        log.debug("Disabling bot AI");
        BotComponent.getBotComponent(bot).setBotAI(new EmptyBotAIBuilder().build());
        MazeTestUtils.ecsWalk(TRIGGER_REQUEST_FORWARD, sceneRunner, user, true, new Point(4, 2));
        SystemManager.putRequest(MazeRequestRegistry.buildFireRequest(user.getId(), null));

        // wait for hitting bot and check relocate of bot to home position
        sceneRunner.runLimitedFrames(200, 0.1);
        MazeTestUtils.assertPositionAndOrientation(bot, new Point(4, 4), GridOrientation.fromDirection("S"));
    }

    /**
     * Without diamonds its solved immediately.
     * BotSystem will start a bot for remaining player.
     */
    @Test
    public void testMultiBot() throws Exception {

        setupTest("##########\n" +
                "###   D  #\n" +
                "#@@      #\n" +
                "#        #\n" +
                "#   @    #\n" +
                "##########", new EmptyBotAIBuilder(), false);

        initMaze(new Point[]{new Point(4, 1)}, new Direction[]{Direction.N},
                new Point[]{new Point(1, 3),
                        new Point(2, 3)}, 1, 3);

        assertEquals(3, MazeUtils.getPlayer().size(), "number of player");
        EcsEntity user = MazeUtils.getPlayerByUsername("u0");
        assertNotNull(user);
        EcsEntity bot0 = MazeUtils.getPlayer().get(1);
        assertNotNull(bot0);
        EcsEntity bot1 = MazeUtils.getPlayer().get(2);
        assertNotNull(bot1);
        assertEquals(3, MazeUtils.getBullets(bot0).size());

        // bot must/will leave home field to make firing possible. By default it will wait real time for next move (but only with SimpleBotAI, which
        // isn't used in tests).

        MazeTestUtils.assertPosition(bot0, new Point(1, 3));
        assertEquals(Direction.E.toString(), MazeUtils.getPlayerorientation(bot0).getDirection().toString(), "bot0 initial orientation");
        MazeTestUtils.assertPosition(bot1, new Point(2, 3));
        assertEquals(Direction.E.toString(), MazeUtils.getPlayerorientation(bot1).getDirection().toString(), "bot1 initial orientation");

        DeterministicBotAI dbAI1 = new DeterministicBotAI(new Request[]{new Request(TRIGGER_REQUEST_FORWARD)});
        BotComponent.getBotComponent(bot1).setBotAI(dbAI1);
        // wait until both bots move one step. bot1 needs to move first.
        // bot in test with DeterministicBotAI will not wait real time for next move.
        EcsTestHelper.processRequests();

        DeterministicBotAI dbAI0 = new DeterministicBotAI(new Request[]{new Request(TRIGGER_REQUEST_FORWARD)});
        BotComponent.getBotComponent(bot0).setBotAI(dbAI0);
        EcsTestHelper.processRequests();

        MazeTestUtils.assertPosition(bot0, new Point(2, 3));
        MazeTestUtils.assertPosition(bot1, new Point(3, 3));


    }

    /**
     * Without diamonds its solved immediately.
     * BotSystem will start a bot for monster.
     */
    @Test
    public void testMultiMonsterTeamsWithBotSystem() throws Exception {

        setupTest("##########\n" +
                "###   D  #\n" +
                "#MM      #\n" +
                "#  @     #\n" +
                "#   M M  #\n" +
                "##########", new EmptyBotAIBuilder(), false);

        initMaze(new Point[]{new Point(3, 2)}, new Direction[]{Direction.N},
                new Point[]{new Point(4, 1),
                        new Point(6, 1),
                        new Point(1, 3),
                        new Point(2, 3)
                }, 1, 1);

        EcsEntity user = MazeUtils.getPlayerByUsername("u0");
        assertNotNull(user);
        EcsEntity bot0 = MazeUtils.getPlayer().get(1);
        assertNotNull(bot0);
        EcsEntity bot1 = MazeUtils.getPlayer().get(2);
        assertNotNull(bot1);
        assertEquals(3, MazeUtils.getBullets(bot0).size());

        // bot must/will leave home field to make firing possible. By default it will wait real time for next move (but only with SimpleBotAI, which
        // isn't used in tests).

        MazeTestUtils.assertPosition(user, new Point(3, 2));
        assertEquals(Direction.N.toString(), MazeUtils.getPlayerorientation(bot0).getDirection().toString(), "bot0 initial orientation");
        MazeTestUtils.assertPosition(bot0, new Point(4, 1));
        assertEquals(Direction.N.toString(), MazeUtils.getPlayerorientation(bot0).getDirection().toString(), "bot0 initial orientation");
        MazeTestUtils.assertPosition(bot1, new Point(6, 1));
        assertEquals(Direction.N.toString(), MazeUtils.getPlayerorientation(bot1).getDirection().toString(), "bot1 initial orientation");
    }

    /**
     * Launch a number of login player (next fails to join), check items.
     * Might have option to add bots some day, but currently (7/23) we have no player bots, only bot monster.
     * <p>
     * "initialPositions" might not contain bots and no monster.
     *
     * @return list of player (incl. monster(and bots?)
     */
    private List<EcsEntity> initMaze(Point[] initialPositions, Direction[] initialOrientation, Point[] expectedBotPositions,
                                     int diamonds, int expectedLoginPositions) {
        List<EcsEntity> users = new ArrayList<>();

        // no boxes, no player, only diamonds
        assertEquals(diamonds, SystemManager.findEntities((EntityFilter) null).size(), "number of entities");
        assertEquals(0, MazeUtils.getPlayer().size(), "number of player");
        assertNull(MazeUtils.getMainPlayer());

        assertEquals(expectedLoginPositions, Grid.getInstance().getMazeLayout().getStartPositionCount(true));
        if (botSystem != null) {
            assertEquals(initialPositions.length + expectedBotPositions.length, Grid.getInstance().getMazeLayout().getStartPositionCount(false));
        }

        assertTrue(SystemState.readyToJoin());

        for (int uindex = 0; uindex < initialPositions.length; uindex++) {
            SystemManager.putRequest(UserSystem.buildLoginRequest("u" + uindex, ""));
            sceneRunner.runLimitedFrames(5);

            EcsEntity user = MazeUtils.getPlayerByUsername("u" + uindex);
            assertNotNull(user);
            assertEquals(initialOrientation[uindex].toString(), MazeUtils.getPlayerorientation(user).getDirection().toString(), "initial orientation for " + uindex);
            users.add(user);

            // if botsystem exists it will start monster immediately.
            if (botSystem != null) {
                for (int bindex = 0; bindex < expectedBotPositions.length; bindex++) {

                    int uCount = initialPositions.length + bindex + 1;
                    List<EcsEntity> bots = EcsHelper.findEntitiesByName("Bot" + bindex);
                    if (bots.size() == 0) {
                        fail("Bot" + bindex + " not found");
                    }
                    EcsEntity monster = bots.get(0);
                    assertNotNull(monster);
                    //assertEquals(initialOrientation[mindex].toString(), MazeUtils.getPlayerorientation(user).getDirection().toString(), "initial orientation for " + uindex);
                    users.add(monster);

                }
            } else {
                if (expectedBotPositions.length > 0) {
                    fail("Cannot expect monster without botSystem");
                }
            }
            int uCount = uindex + 1 + expectedBotPositions.length;
            assertEquals(uCount + (uCount * 3) + diamonds, SystemManager.findEntities((EntityFilter) null).size(), "number of entites (" + uCount + " player + 3 bullets each+" + diamonds + " diamond) at uindex " + uindex);
            assertEquals(uCount, MazeUtils.getPlayer().size(), "number of player at uindex " + uindex);
            assertNotNull(MazeUtils.getMainPlayer());

        }

        // observer should stay attached to first player
        assertEquals("Avatar", Observer.getInstance().getTransform().getParent().getSceneNode().getName(), "parent of observer");
        // only native transforms are static
        assertEquals(users.get(0).scenenode.getTransform().transform, Observer.getInstance().getTransform().getParent().transform, "parent of observer");

        // don't expect additional user (however, login should be possible)
        SystemManager.putRequest(UserSystem.buildLoginRequest("u" + initialPositions.length, ""));
        sceneRunner.runLimitedFrames(5);
        assertEquals(initialPositions.length + expectedBotPositions.length, MazeUtils.getPlayer().size(), "number of player");
        // all user still on their start position and have 3 bullets
        for (int uindex = 0; uindex < initialPositions.length; uindex++) {
            MazeTestUtils.assertPosition(users.get(uindex), initialPositions[uindex]);
            assertEquals(3, MazeUtils.getBullets(users.get(uindex)).size(), "bullets");
        }
        for (int mindex = 0; mindex < expectedBotPositions.length; mindex++) {
            MazeTestUtils.assertPosition(users.get(1 + mindex), expectedBotPositions[mindex]);
        }
        assertEquals((initialPositions.length + expectedBotPositions.length) * 3 + diamonds, MazeUtils.getAllItems().size(), "(x*3 bullets, 2 diamond)");


        return users;
    }
}