package de.yard.threed.maze;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.Assert;
import de.yard.threed.engine.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.ecs.*;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.engine.gui.Hud;
import de.yard.threed.engine.platform.common.*;

import java.util.ArrayList;
import java.util.List;

import static de.yard.threed.maze.MazeModelFactory.BULLET_BUILDER;


/**
 * Controls movement and the "GridState".
 * <p>
 * Connection between an {@link MazeLevel} and state handling via ECS.
 * <p>
 * Created by thomass on 14.02.17.
 */
public class MazeMovingAndStateSystem extends DefaultEcsSystem {
    Log logger = Platform.getInstance().getLog(MazeMovingAndStateSystem.class);
    public static String TAG = "MazeMovingAndStateSystem";

    // Alle Events queuen, weil sie evtl. unpassend (z.B. während Movement) kommen. Einfach ignorieren
    // ist riskant, weil sie auch fuer Replay, etc kommen können, wo jedes Event wichtig ist.
    // 30.10.20: Gibt es das nicht uebergreifend? Genau, dass ist doch ein Asbachkonzept.
    // 7.4.21: Wenn alle Events Requests sind, kann das wohl weg.
    //7.4.21 SimpleQueue eventqueue = new SimpleQueue();

    // gesetzt, wenn Autosolver aktiv ist.
    // 14.4.21: Ersetzt ducrh ReplaySystem
    List<GridMovement> solution = null;
    boolean mazeMovingAndStateSystemdebuglog = true;

    protected MazeTheme mazeTheme;

    private Hud helphud = null;

    public MazeMovingAndStateSystem(MazeTheme mazeTheme) {
        super(new String[]{"MazeMovingComponent"}, new RequestType[]{MazeRequestRegistry.TRIGGER_REQUEST_BACK,
                        MazeRequestRegistry.TRIGGER_REQUEST_TURNLEFT, MazeRequestRegistry.TRIGGER_REQUEST_FORWARD,
                        MazeRequestRegistry.TRIGGER_REQUEST_TURNRIGHT, MazeRequestRegistry.MAZE_REQUEST_LOADLEVEL,
                        MazeRequestRegistry.TRIGGER_REQUEST_AUTOSOLVE, UserSystem.USER_REQUEST_JOIN, MazeRequestRegistry.TRIGGER_REQUEST_UNDO,
                        MazeRequestRegistry.TRIGGER_REQUEST_VALIDATE, MazeRequestRegistry.TRIGGER_REQUEST_HELP,
                        MazeRequestRegistry.TRIGGER_REQUEST_RESET,
                        MazeRequestRegistry.TRIGGER_REQUEST_FORWARDMOVE,
                        MazeRequestRegistry.TRIGGER_REQUEST_LEFT,
                        MazeRequestRegistry.TRIGGER_REQUEST_RIGHT,
                        MazeRequestRegistry.TRIGGER_REQUEST_PULL,
                        MazeRequestRegistry.TRIGGER_REQUEST_RELOCATE,
                        MazeRequestRegistry.TRIGGER_REQUEST_TELEPORT,
                        MazeRequestRegistry.TRIGGER_REQUEST_KICK,
                },
                new EventType[]{
                        BaseEventRegistry.EVENT_USER_ASSEMBLED});
        this.mazeTheme = mazeTheme;
    }

    public static MazeMovingAndStateSystem buildFromArguments(MazeTheme mazeTheme) {
        return new MazeMovingAndStateSystem(mazeTheme);
    }

    @Override
    public void init() {
    }

    public void frameinit() {

        // Wait for grid to be available. Might be loaded async.
        if (SystemState.state == 0 && MazeDataProvider.getGrid() != null) {

            loadLevel();
            //10.11.20 ob der hier gut ist, muss sich noch zeigen.
            MoveRecorder.init(/*movingsystem.* /currentstate*/);

            SystemState.state = SystemState.STATE_READY_TO_JOIN;
        }
    }

    /**
     * update for player and boxes (MoverComponent(s)).
     */
    @Override
    public void update(EcsEntity entity, EcsGroup group, double tpf) {

        MoverComponent mover = (MoverComponent) group.cl.get(0);

        /*7.4.21 if (!MazeVisualizationSystem.view.isMoving()) {
            Event evt = (Event) eventqueue.poll();
            if (evt != null) {
                processEvent(evt);
            }
        }*/
        //float tpf = scene.getDeltaTime();
         /*if ((movement = mover.attempMove(true, walls, currentstate)) != null) {
                nextstate = currentstate.execute(movement);
                MoveRecorder.getInstance().addMove(movement, nextstate);
            }*/
      
        /*14.2.17 if (Input.GetKeyDown(KeyCode.Space)) {
            // gequeute Bewegungen gehen noch nicht
            ray.attemptFire();
        }*/
        boolean debugcontrols = true;
        if (debugcontrols && mover.isPlayer()) {
            if (Input.GetKeyDown(KeyCode.PageUp)) {
                entity.getSceneNode().getTransform().rotateX(new Degree(-5));
                //logger.debug("Rotation now"+maze.get)
            }
            if (Input.GetKeyDown(KeyCode.PageDown)) {
                entity.getSceneNode().getTransform().rotateX(new Degree(5));
            }
            if (Input.GetKeyDown(KeyCode.Plus)) {
                entity.getSceneNode().getTransform().translateY(0.5f);
            }
            if (Input.GetKeyDown(KeyCode.Minus)) {
                entity.getSceneNode().getTransform().translateY(-0.5f);
            }
        }
        mover.update(tpf);
        // reVisualizeState() is a nice to have for avoiding inconsistencies, but with multiple movers it will
        // be difficult to find a frame where nobody moves.
        if (MazeUtils.isAnyMoving() == null) {
            reVisualizeState();
        }

        GridState currentstate = MazeUtils.buildGridStateFromEcs();
        if (!SystemState.isOver() && currentstate.isSolved(Grid.getInstance().getMazeLayout())) {
            logger.info("solved");
            Hud hud = Hud.buildForCameraAndAttach(Scene.getCurrent().getDefaultCamera(), 1);
            hud.setText(2, "Solved");

            SystemState.state = SystemState.STATE_OVER;
        }

    }

    @Override
    public boolean processRequest(Request request) {

        logger.debug("got request " + request.getType());

        if (SystemState.isOver()) {
            return true;
        }

        GridState currentstate = MazeUtils.buildGridStateFromEcs();

        if (request.getType().equals(MazeRequestRegistry.MAZE_REQUEST_LOADLEVEL)) {
            //16.4.21: For simplication not used. Just restart with other arguments
            //Integer level = (Integer) request.getPayloadByIndex(0);
            //removeLevel();
            //doLoadLevel(level);
            return true;
        }
        if (request.getType().equals(MazeRequestRegistry.TRIGGER_REQUEST_AUTOSOLVE)) {
            autosolve(currentstate);
            return true;
        }

        if (request.getType().equals(MazeRequestRegistry.TRIGGER_REQUEST_VALIDATE)) {
            validate();
            return true;
        }
        if (request.getType().equals(MazeRequestRegistry.TRIGGER_REQUEST_HELP)) {
            help();
            return true;
        }
        if (request.getType().equals(MazeRequestRegistry.TRIGGER_REQUEST_RESET)) {
            reset();
            return true;
        }
        if (request.getType().equals(MazeRequestRegistry.TRIGGER_REQUEST_RELOCATE) ||
                request.getType().equals(MazeRequestRegistry.TRIGGER_REQUEST_TELEPORT)) {
            // 6.4.22: 'Relocate' can only be a system request (hit by bullet) any more
            relocateOrTeleport(request.getType(), request.getPayload(), currentstate);
            return true;
        }
        if (request.getType().equals(UserSystem.USER_REQUEST_JOIN)) {

            int userEntityId = request.getUserEntityId();

            EcsEntity userEntity = EcsHelper.findEntityById(userEntityId);

            logger.debug("User/Bot '" + userEntity.getName() + "' joining");

            SystemManager.sendEvent(processJoin(userEntityId));

            return true;
        }
        return processSolutionOrUserRequest(currentstate, request);
    }

    @Override
    public String getTag() {
        return TAG;
    }

    /**
     * 14.4.21: Ein Request wird auch dann auf processed gesetzt, wenn es wegen einer laufenden Bewegung ignoriert wird.
     * Es wird also nicht gequeued.
     *
     * @param currentstate
     * @param request
     * @return
     */
    private boolean processSolutionOrUserRequest(GridState currentstate, Request request) {

        // 16.3.22: Isn't a solution played by ReplaySystem?
        if (solution != null) {
            // dann keine Steuerung durch Benutzer mehr
            if (solution.size() == 0) {
                // muesste solved sein.
                solution = null;

            } else {
                GridMovement nextstep = solution.get(0);
                solution.remove(0);
                //TODO abbrechen bei scheitern des attempt?
                //24.10.18: Auch per Event
                //raycontroller.attempt(nextstep);
                Object o = nextstep.getEvent();
                if (o instanceof EventType) {
                    SystemManager.sendEvent(new Event((EventType) o, null));
                } else {
                    SystemManager.putRequest(new Request((RequestType) o));
                }
            }
            //10.11.20 true oder false liefern?
            return true;
        } else {
            // A movement request might be lost here due to a current moving.
            // Once there was a queue to handle this. Ignoring lost movements is a risk, because for use cases like 'replay' its important not to loose a single movement.

            if (request.getUserEntityId() == null) {
                logger.warn("No userEntityId in request. Ignoring user request");
                return true;
            } else {
                EcsEntity ray = UserSystem.getUserByEntityId((int) request.getUserEntityId());
                if (processUserRequest(currentstate, request, ray)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param evt
     */
    @Override
    public void process(Event evt) {

        if (mazeMovingAndStateSystemdebuglog) {
            logger.debug("got event " + evt.getType());
        }

        if (evt.getType().equals(BaseEventRegistry.EVENT_USER_ASSEMBLED)) {
            // final join step still to do which needs scenenode
            //processJoin((Integer) evt.getPayload().get("userentityid"));
            EcsEntity playerEntity = EcsHelper.findEntityById((Integer) evt.getPayload().get("userentityid"));
            completeJoin(Grid.getInstance().getMazeLayout(), playerEntity/*, launchPosition, teamid*/);
        }
    }

    /**
     * Join a user/player/bot/monster.
     * Return JOINED_EVENT on success and error message on failure.
     */
    private Event processJoin(int playerEntityId) {
        if (Grid.getInstance() == null) {
            throw new RuntimeException("unexpected flow. state no readtojoin?");
        }
        // Init player (entity already created) and publish "new player". But not before maze was loaded.
        // 1.4.21: Avatar was (29.4.23 will be)  build by AvatarSystem (and attached to world and previously created user entity).

        EcsEntity playerEntity = EcsHelper.findEntityById(playerEntityId);
        MazeLayout layout = Grid.getInstance().getMazeLayout();

        // In server mode clients might connect and disconnect, so the used and available launch positions are quite dynamic.
        List<EcsEntity> currentPlayer = MazeUtils.getPlayer();
        // this is also reached for joining monster, so don't ignore monster lauch locations
        if (currentPlayer.size() >= layout.getStartPositionCount(false)) {
            logger.warn("Rejecting join request due to too may players. Currently " + currentPlayer.size());
            return BaseEventRegistry.buildUserJoinFailedEvent(playerEntity,"error");
        }
        BotComponent botComponent = BotComponent.getBotComponent(playerEntity);

        Point launchPosition = findAvailableLaunchPosition(layout, currentPlayer, botComponent!=null && botComponent.isMonster());

        int teamid;
        if (launchPosition != null) {
            teamid = layout.getTeamByHome(launchPosition);
            //completeJoin(layout, playerEntity, launchPosition, teamid);
            logger.debug("Launching player at "+launchPosition);
        } else {
            logger.warn("No start position found. too may players?. Currently " + currentPlayer.size());
            return BaseEventRegistry.buildUserJoinFailedEvent(playerEntity, "error");
        }
        MoverComponent mover = new MoverComponent(null/*playerEntity.scenenode.getTransform()*/, true, launchPosition, layout.getInitialOrientation(launchPosition), teamid);
        //usedLaunchPositions.add(launchPosition);

        playerEntity.addComponent(mover);

        return BaseEventRegistry.buildUserJoinedEvent(playerEntity);
    }

    /**
     * Also used for bots/monster.
     */
    private Point findAvailableLaunchPosition(MazeLayout layout, List<EcsEntity> currentPlayer, boolean forMonster) {
        List<Point> positionsToIgnore = new ArrayList<Point>();
        for (EcsEntity p : currentPlayer) {
            MoverComponent mc = MoverComponent.getMoverComponent(p);
            // new player is also in the list without mc
            if (mc != null) {
                positionsToIgnore.add(mc.getLocation());
            }
        }
        Point launchPosition = layout.getNextLaunchPosition(positionsToIgnore, forMonster);
        return launchPosition;
    }

    /**
     * Complete a join a new player. 28.4.23: Now after it has been assembled (avatar, scenenode).
     * mover(Component) already exists but needs the scene node.
     */
    private void completeJoin(MazeLayout layout, EcsEntity playerEntity) {

        // MazeLayout layout = Grid.getInstance().getMazeLayout();
        // MoverComponent already exists, but has no transofrm yet. Needs to be updated.
        MoverComponent mover = MoverComponent.getMoverComponent(playerEntity);
        logger.debug("Complete join of " + playerEntity.getName() + " for team " + mover.getGridMover().getTeam());

        mover.setMovable(playerEntity.scenenode.getTransform());
        // set mover to its position
        mover.updateMovable();

        InputToRequestSystem.setPayload0(playerEntity.getName());

        // In multi player every joined user gets three bullets
        if (layout.getNumberOfTeams() > 1) {
            createBullets(3, playerEntity.getId());
        }
    }

    /**
     * 14.4.21: Ein Request wird auch dann auf processed gesetzt, wenn es wegen einer laufenden Bewegung ignoriert wird.
     * Es wird also nicht gequeued.
     */
    private boolean processUserRequest(GridState currentstate, Request request, EcsEntity user) {

        MoverComponent mover = (MoverComponent) user.getComponent(MoverComponent.TAG);
        if (mover == null) {
            logger.error("no mover for user");
            return true;
        }
        List<EcsEntity> foundStuff = null;
        if (request.getType().equals(MazeRequestRegistry.TRIGGER_REQUEST_TURNRIGHT)) {
            attemptRotate(currentstate, mover, false);
        } else if (request.getType().equals(MazeRequestRegistry.TRIGGER_REQUEST_TURNLEFT)) {
            attemptRotate(currentstate, mover, true);
        } else if (request.getType().equals(MazeRequestRegistry.TRIGGER_REQUEST_BACK)) {
            foundStuff = attemptMove(currentstate, mover, GridMovement.Back, user.getId());
        } else if (request.getType().equals(MazeRequestRegistry.TRIGGER_REQUEST_FORWARD)) {
            foundStuff = attemptMove(currentstate, mover, GridMovement.Forward, user.getId());
        } else if (request.getType().equals(MazeRequestRegistry.TRIGGER_REQUEST_UNDO)) {
            undo(currentstate, mover, Grid.getInstance().getMazeLayout());
        } else if (request.getType().equals(MazeRequestRegistry.TRIGGER_REQUEST_FORWARDMOVE)) {
            // 10.4.21: Wer triggered denn einen TRIGGER_REQUEST_FORWARDMOVE? Nur der Replay?
            foundStuff = attemptMove(currentstate, mover, GridMovement.ForwardMove, user.getId());
        } else if (request.getType().equals(MazeRequestRegistry.TRIGGER_REQUEST_LEFT)) {
            foundStuff = attemptMove(currentstate, mover, GridMovement.Left, user.getId());
        } else if (request.getType().equals(MazeRequestRegistry.TRIGGER_REQUEST_RIGHT)) {
            foundStuff = attemptMove(currentstate, mover, GridMovement.Right, user.getId());
        } else if (request.getType().equals(MazeRequestRegistry.TRIGGER_REQUEST_PULL)) {
            // pull kommt evtl auch bei undo? 27.5.21: jetzt auch eigenstaendig. UNDO hat aber sein eigenes Event.
            foundStuff = attemptMove(currentstate, mover, GridMovement.Pull, user.getId());
        } else if (request.getType().equals(MazeRequestRegistry.TRIGGER_REQUEST_KICK)) {
            foundStuff = attemptMove(currentstate, mover, GridMovement.Kick, user.getId());
        } else {
            return false;
        }
        /*collecting is handled during walk if (foundStuff != null) {
            for (EcsEntity item : foundStuff) {
                MazeUtils.setItemCollectedByPlayer(item, user);
            }
        }*/
        return true;
    }

    /**
     * Try a movement (eg. walk to an non occupied field).
     * If walk is not possible, try a push (only in case of forward)
     * <p>
     * Only allows four basic movements, no ForwardMove, though that could be possible.
     * Items do not occupy a field. So items that might be located at the destination field do not affect movement.
     * Relocate is allowed while moving because its not self triggered.
     */
    private List<EcsEntity> attemptMove(GridState currentstate, MoverComponent mover, GridMovement movement, int userEntityId) {
        MazeLayout layout = Grid.getInstance().getMazeLayout();

        List<EcsEntity> foundStuff = null;
        if (mover.isMoving() && !movement.isRelocate()) {
            // Just to be sure. Shouldn't be called in this case.
            logger.warn("cannot move. still moving");
        } else {
            // try basic movement
            MoveResult mo = mover.walk(movement, currentstate, layout);
            if (mo == null) {
                // no success. Try push, but only when the basic request was 'Forward'. Otherwise boxes could be pushed by 'Back'
                if (movement.isForward()) {
                    movement = GridMovement.ForwardMove;
                    mo = mover.walk(movement, currentstate, layout);
                }
            }
            if (mo != null) {
                // successfully moved. Record it for replay and collect items on new field
                MoveRecorder.getInstance().addMove(movement/*, nextstate*/);
                //TODO 17.3.22 merge with collect in SimpleGridMover
                //24.3.22 better via event? Hmm.
                //foundStuff = MazeUtils.getItemsByField(mover.getLocation());
                for (int itemId : mo.getCollected()) {
                    SystemManager.sendEvent(InventorySystem.buildItemCollectedEvent(userEntityId, itemId));
                }
            }
        }
        return foundStuff;
    }

    /**
     * Muesste nicht "attempt" heissen, ist dann aber analog zu attemptmove.
     */
    public void attemptRotate(GridState currentstate, MoverComponent mover, boolean left) {
        GridMovement movement;
        //24.10.18 Mover mover = ((Mover) ray.components.get(0));
        if (left) {
            // gequeute Bewegungen gehen noch nicht
            if ((movement = mover.rotate(true)) != null) {
                //nextstate = currentstate.execute(movement, layout);
                MoveRecorder.getInstance().addMove(movement/*, nextstate*/);
            }
        } else {
            // gequeute Bewegungen gehen noch nicht
            if ((movement = mover.rotate(false)) != null) {
                //nextstate = currentstate.execute(movement, layout);
                MoveRecorder.getInstance().addMove(movement/*, nextstate*/);
            }
        }
    }


    /**
     * Nach Ende der Bewegung den aktuellen state fixieren und visualisieren, um (schleichende) Abweichungen zu vermeiden.
     * Wird auch fuer undo verwendet.
     * MA32: TODO Die erneute Visualisierung dient ja auch als eine Art refresh. Das sollte beibehalten werden.
     */
    public void reVisualizeState() {
        /*MA32 if (nextstate != null) {
            //logger.debug("Switching to next state");
            currentstate = nextstate;
            nextstate = null;
            MazeVisualizationSystem.view.visualizeState(currentstate);
        }*/
    }


    /**
     * Keine Pruefungen. Es wird erwartet, dass das geht.
     * TODO: Aktuelle Bewegunegen beachten. undo wird sehr schnell gemacht. Er scheint da schon mal  durcheinander zu kommen.
     * TODO 17.3.22 Needs alternate implementation. GridState is no longer containing the current state.
     *
     * @param movement
     */
    private void undo(GridState currentstate, MoverComponent mover, GridMovement movement, MazeLayout layout) {
        //24.10.18: Mover mover = ((Mover) ray.components.get(0));

        switch (movement.movement) {
            case GridMovement.FORWARD:
                mover.walk(GridMovement.Back, currentstate, layout);
                break;
            case GridMovement.BACK:
                mover.walk(GridMovement.Forward, currentstate, layout);
                break;
            case GridMovement.FORWARDMOVE:
                Point boxloc = currentstate.getPullBoxLocation(layout);
                Util.nomore();
                GridState nextstate = null;//currentstate.pull(layout);
                if (nextstate != null) {
                    Util.nomore();
                    //triggerPushPull(currentstate, mover, boxloc, GridMovement.Pull);
                }
                break;
            case GridMovement.TURNLEFT:
                mover.rotate(false);
                break;
            case GridMovement.TURNRIGHT:
                mover.rotate(true);
                break;
        }
    }

    /**
     * 15.8.19. Wegen VR mit "Avatar".
     * Es könnte sein, dass der Body jetzt zu hoch ist. Und bei VR würde er wohl auch stören.
     * Darum erstmal keinen body.
     * TODO 1.4.21: Delegate in AvatarSystem for body
     *
     * @return
     */
    /*24.1.22 public static Avatar/*EcsEntity* / buildPlayer(Camera camera) {

        Avatar avatar = Avatar.buildDefault(camera/*getMainCamera()* /);
        SceneNode body = MazeModelBuilder.buildSimpleBody(MazeSettings.getSettings().simplerayheight, MazeSettings.getSettings().simpleraydiameter, Color.ORANGE);
        //avatar.avatar.attach(body);


        return avatar/*.avatarE* /;
    }*/
    private void undo(GridState currentstate, MoverComponent mover, MazeLayout layout) {
        GridMovement lastmovement = MoveRecorder.getInstance().removeLastMove();
        if (lastmovement != null) {
            undo(currentstate, mover, lastmovement, layout);
        }
    }

    /**
     * TODO 4.10.18: Das scheint nicht (mehr) zu gehen. Der irrt einfach rum.
     */
    private void autosolve(GridState currentstate) {
        logger.debug("autosolve");
        SokobanAutosolver solver = new SokobanAutosolver(/*movingsystem.*/Grid.getInstance()/*MA32, /*movingsystem.* /currentstate*/);
        solver.solve();
        solution = solver.getSolution();
    }

    /**
     * There is no level reload, so no need to get rid of old one.
     * This is triggered when grid data was loaded (eg. from a remote server) and the data provider is ready.
     * TODO error handling
     */
    private void loadLevel() {
        logger.debug("loadLevel ");

        Grid grid = MazeDataProvider.getGrid();
        if (grid == null) {
            logger.error("load error: no grid");
            return;
        }
        Grid.setInstance(grid);

        for (Point b : grid.getBoxes()) {
            //EcsEntity box = MazeMovingAndStateSystem.buildSokobanBox(b.getX(), b.getY());
            //static EcsEntity buildSokobanBox(int x, int y) {
            //SceneNode p = MazeModelFactory.getInstance().buildSokobanBox(/*b.getX(), b.getY()*/);
            EcsEntity box = new EcsEntity();
            box.buildSceneNodeByModelFactory(MazeModelFactory.BOX_BUILDER, new ModelBuilderRegistry[]{mazeTheme.getMazeModelFactory()});
            MoverComponent mover = new MoverComponent(box.getSceneNode().getTransform()/*this*/, false, b, new GridOrientation(), -1);
            mover.setLocation(b);
            box.addComponent(mover);
            //return box;

            Scene.getCurrent().addToWorld(box.scenenode);
        }
        for (Point b : grid.getDiamonds()) {
            EcsEntity diamond = new EcsEntity();
            diamond.buildSceneNodeByModelFactory(MazeModelFactory.DIAMOND_BUILDER, new ModelBuilderRegistry[]{mazeTheme.getMazeModelFactory()});
            diamond.setName("diamond");
            Vector3 dp = MazeUtils.point2Vector3(b);
            dp = new Vector3(dp.getX(), 0.8, dp.getZ());
            diamond.getSceneNode().getTransform().setPosition(dp);
            // no diamond owner initially
            diamond.addComponent(new DiamondComponent(b));
            Scene.getCurrent().addToWorld(diamond.scenenode);
        }
        // 20.12.22: Bullets for player are created at join time. Since bots are just a kind of multiplayer, the same applies to bots.

        SystemState.state = SystemState.STATE_READY_TO_JOIN;
        SystemManager.sendEvent(MazeEventRegistry.buildMazeLoadedEvent(MazeDataProvider.getGridName(), grid.getRaw()));

        //11.4.16 addTestObjekte();
        logger.debug("load of " + MazeDataProvider.getGridName() + " completed. state = " + SystemState.getStateAsString());
    }

    private void createBullets(int cnt, int owner) {
        logger.debug("create " + cnt + " Bullets for " + owner);
        for (int i = 0; i < cnt; i++) {
            EcsEntity e = new EcsEntity();
            e.buildSceneNodeByModelFactory(BULLET_BUILDER, new ModelBuilderRegistry[]{mazeTheme.getMazeModelFactory()});
            BulletComponent bulletComponent = new BulletComponent(owner);
            e.addComponent(bulletComponent);
            e.setName("bullet");
            // a new bullet related to an owner should initially be hidden because its in the inventory of the owner.
            bulletComponent.collectedBy(owner);
        }
    }

    private void validate() {
        logger.debug("validate");
        /*1.4.21if (MazeView.ray == null) {
            logger.error("no ray in MazeView");
        }*/
        Camera camera = Scene.getCurrent().getDefaultCamera();
        Assert.assertEquals("fov", 60.0, camera.getFov(), 0.001);
        logger.debug("validate complete");

    }

    private void reset() {
        logger.debug("reset");
        Util.nomore();
        /*MA32
        GridState initialstate = MoveRecorder.getInstance().statelist.get(0);
        MoveRecorder.getInstance().reset();
        /*movingsystem.* /
        currentstate = initialstate;
        MazeVisualizationSystem.view.visualizeState(initialstate);*/
    }

    /**
     * 6.4.21 Im MP Sinne ist das hier in dieser Form aber unguenstig.
     * 16.12.22: TODO check: still used/needed/helpful?
     */
    private void help() {
        logger.debug("help");
        if (helphud == null) {
            helphud = Hud.buildForCameraAndAttach(Scene.getCurrent().getDefaultCamera(), 1);
            helphud.setText(0, "r - ");
            helphud.setText(6, "u - undo");
            helphud.setText(7, "r - reset");
            helphud.setText(9, "press h to close");
            //20.2.17? add(hud);
        } else {
            SceneNode.removeSceneNode(helphud);
            helphud = null;
        }
    }


    private void addTestObjekte() {
        Geometry cuboid = Geometry.buildCube(0.5f, MazeModelFactory.PILLARHEIGHT * 2, 0.5f);
        ShapeGeometry cubegeometry = ShapeGeometry.buildBox(0.5f, MazeModelFactory.PILLARHEIGHT * 2, 0.5f, null);

        Mesh m = new Mesh(cubegeometry, Material.buildBasicMaterial(Color.RED));
        //add(m);
    }

    /**
     * Process a relocate request (eg. for hit players) or teleport request (for VR teleport).
     * 'Relocate' can no longer be a user request, so no longer treat it like teleport. Relocate is possible
     * while player is moving.
     */
    private void relocateOrTeleport(RequestType type, Payload payload, GridState currentstate) {

        int userEntityId = (int) (Integer) payload.o[0];
        EcsEntity player = EcsHelper.findEntityById(userEntityId);
        if (player == null) {
            logger.warn("unknown player for relocateOrTeleport:" + userEntityId);
            return;
        }

        logger.debug("relocateOrTeleport " + player.getName());

        String location = (String) payload.o[1];
        Point p = Util.parsePoint(location);
        if (p == null) {
            logger.warn("invalid location:" + location);
            return;
        }

        GridOrientation gridOrientation = null;
        String orientation = (String) payload.o[2];
        if (orientation != null && StringUtils.length(orientation) > 0) {
            gridOrientation = GridOrientation.fromDirection(orientation);
            if (gridOrientation == null) {
                logger.warn("invalid orientation:" + orientation);
                return;
            }
        }

        GridMovement movement = (type == MazeRequestRegistry.TRIGGER_REQUEST_RELOCATE) ? GridMovement.buildRelocate(p, gridOrientation) : GridMovement.buildTeleport(p, gridOrientation);

        MoverComponent mc = MoverComponent.getMoverComponent(player);

        attemptMove(currentstate, mc, movement, userEntityId);
        // attemptMove does not change orientation
        if (gridOrientation != null) {
            //otherwise keep orientation
            mc.setOrientation(gridOrientation);
        }
    }
}
