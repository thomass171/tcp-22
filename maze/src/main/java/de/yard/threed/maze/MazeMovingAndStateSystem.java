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


/**
 * Steuert die Bewegung und damit auch den "GridState".
 * <p>
 * Connection between an {@link MazeLevel} and state handling via ECS.
 * <p>
 * Created by thomass on 14.02.17.
 */
public class MazeMovingAndStateSystem extends DefaultEcsSystem {
    Log logger = Platform.getInstance().getLog(MazeMovingAndStateSystem.class);
    //MA32 GridState currentstate;
    //nextstate != null isType indicator for ongoing movement.
    //private GridState nextstate;
    //MazeLayout layout;
    //10.11.20 private MazeView view;
    // Alle Events queuen, weil sie evtl. unpassend (z.B. während Movement) kommen. Einfach ignorieren
    // ist riskant, weil sie auch fuer Replay, etc kommen können, wo jedes Event wichtig ist.
    // 30.10.20: Gibt es das nicht uebergreifend? Genau, dass ist doch ein Asbachkonzept.
    // 7.4.21: Wenn alle Events Requests sind, kann das wohl weg.
    //7.4.21 SimpleQueue eventqueue = new SimpleQueue();

    // gesetzt, wenn Autosolver aktiv ist.
    // 14.4.21: Ersetzt ducrh ReplaySystem
    List<GridMovement> solution = null;
    boolean mazeMovingAndStateSystemdebuglog = true;

    protected MazeSettings st;

    private Hud helphud = null;
    String initialMaze = null;
    List<Point> usedLaunchPositions = new ArrayList<Point>();

    public MazeMovingAndStateSystem() {
        super(new String[]{"MazeMovingComponent"}, new RequestType[]{RequestRegistry.TRIGGER_REQUEST_BACK,
                        RequestRegistry.TRIGGER_REQUEST_TURNLEFT, RequestRegistry.TRIGGER_REQUEST_FORWARD,
                        RequestRegistry.TRIGGER_REQUEST_TURNRIGHT, RequestRegistry.MAZE_REQUEST_LOADLEVEL,
                        RequestRegistry.TRIGGER_REQUEST_AUTOSOLVE, /*3.3.22 not needed here UserSystem.USER_REQUEST_JOIN,*/ RequestRegistry.TRIGGER_REQUEST_UNDO,
                        RequestRegistry.TRIGGER_REQUEST_VALIDATE, RequestRegistry.TRIGGER_REQUEST_HELP,
                        RequestRegistry.TRIGGER_REQUEST_RESET,
                        RequestRegistry.TRIGGER_REQUEST_FORWARDMOVE,
                        RequestRegistry.TRIGGER_REQUEST_LEFT,
                        RequestRegistry.TRIGGER_REQUEST_RIGHT,
                        RequestRegistry.TRIGGER_REQUEST_PULL,
                        RequestRegistry.TRIGGER_REQUEST_RELOCATE,
                        RequestRegistry.TRIGGER_REQUEST_TELEPORT,
                        RequestRegistry.TRIGGER_REQUEST_KICK,
                },
                new EventType[]{
                        UserSystem.USER_EVENT_LOGGEDIN, UserSystem.USER_EVENT_JOINED});

        //10.4.21: TODO in init(), vorher die Settings aber decouplen
        //abstractMaze = new SimpleMaze();
        st = MazeSettings.init(MazeSettings.MODE_SOKOBAN);
    }

    public MazeMovingAndStateSystem(String initialMaze) {
        this();
        this.initialMaze = initialMaze;
    }

    public static MazeMovingAndStateSystem buildFromArguments() {
        String argv_initialMaze = ((Platform) Platform.getInstance()).getSystemProperty("argv.initialMaze");
        return new MazeMovingAndStateSystem(argv_initialMaze);
    }

    @Override
    public void init() {

        // only for system init
        if (initialMaze == null) {
            initialMaze = "skbn/SokobanWikipedia.txt";
        }
        String fileContent;
        String title;
        if (StringUtils.startsWith(initialMaze, "##")) {
            // directly grid definition
            fileContent = initialMaze;
            title = "on-the-fly";
        } else {
            String name = StringUtils.substringBeforeLast(initialMaze, ".");
            name = StringUtils.substringAfterLast(name, "/");
            String filename = StringUtils.substringBeforeLast(initialMaze, ":");
            fileContent = MazeUtils.readMazefile(filename/*, name*/);

            title = StringUtils.substringAfterLast(initialMaze, ":");
        }
        loadLevel(fileContent, title);
        //10.11.20 ob der hier gut ist, muss sich noch zeigen.
        MoveRecorder.init(/*movingsystem.* /currentstate*/);

        // 10.4.21: richtige Stelle?
        SystemState.state = SystemState.STATE_READY_TO_JOIN;
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
            Hud hud = Hud.buildForCamera(Scene.getCurrent().getDefaultCamera(), 1);
            hud.setText(2, "Solved");
            Scene.getCurrent().getDefaultCamera().getCarrier().attach(hud);

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

        if (request.getType().equals(RequestRegistry.MAZE_REQUEST_LOADLEVEL)) {
            //16.4.21: For simplication not used. Just restart with other arguments
            //Integer level = (Integer) request.getPayloadByIndex(0);
            //removeLevel();
            //doLoadLevel(level);
            return true;
        }
        if (request.getType().equals(RequestRegistry.TRIGGER_REQUEST_AUTOSOLVE)) {
            autosolve(currentstate);
            return true;
        }

        if (request.getType().equals(RequestRegistry.TRIGGER_REQUEST_VALIDATE)) {
            validate();
            return true;
        }
        if (request.getType().equals(RequestRegistry.TRIGGER_REQUEST_HELP)) {
            help();
            return true;
        }
        if (request.getType().equals(RequestRegistry.TRIGGER_REQUEST_RESET)) {
            reset();
            return true;
        }
        if (request.getType().equals(RequestRegistry.TRIGGER_REQUEST_RELOCATE) ||
                request.getType().equals(RequestRegistry.TRIGGER_REQUEST_TELEPORT)) {
            // 6.4.22: 'Relocate' can only be a system request (hit by bullet) any more
            relocateOrTeleport(request.getType(), request.getPayload(), currentstate);
            return true;
        }
        return processSolutionOrUserRequest(currentstate, request);
    }


    public MazeSettings getSettings() {
        return st;
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

        if (evt.getType().equals(UserSystem.USER_EVENT_JOINED)) {
            if (Grid.getInstance() == null) {
                throw new RuntimeException("unexpected flow. state no readtojoin?");
            }
            // Init player (entity already created) and publish "new player". But not before maze was loaded.
            // 1.4.21: Avatar was build by AvatarSystem (and attached to world and previously created user entity).

            EcsEntity playerEntity = (EcsEntity) evt.getPayloadByIndex(0);
            MazeLayout layout = Grid.getInstance().getMazeLayout();
            Point launchPosition = layout.getNextLaunchPosition(usedLaunchPositions);
            // for now only one player teams
            Team team = new Team(usedLaunchPositions.size(), Util.buildList(launchPosition));
            if (launchPosition != null) {
                joinPlayer(playerEntity, launchPosition, team);
            } else {
                logger.warn("Rejecting join request due to too may players. Currently " + usedLaunchPositions.size());
            }
        }
    }

    /**
     * Join a new player.
     */
    private void joinPlayer(EcsEntity playerEntity, Point launchPosition, Team team) {
        //MA35 hier mal jetzt trennen zischen bot avatar und eigenem (obserser). Also in VR kein Avatar fuer main Player. Ohne VR schon, weil damit die Blickrotation einfacher
        //ist.
        // 14.2.22: More consistent approach. Independent from VR mode have a avatar and observer independent from each other, but
        // observer always attached to avatar (in AvatarSystem).
        MazeLayout layout = Grid.getInstance().getMazeLayout();
        MoverComponent mover;
        mover = new MoverComponent(playerEntity.scenenode.getTransform(), true, launchPosition, layout.getInitialOrientation(launchPosition), team);
        usedLaunchPositions.add(launchPosition);
        /*Now in AvatarSystem if (MazeScene.vrInstance == null) {

            Observer.getInstance().initFineTune(getSettings().getViewpoint().position.add(new Vector3(0, MazeScene.rayy, 0)));
            // Rotation for looking slightly down.
            Observer.getInstance().getInstance().getTransform().setRotation(getSettings().getViewpoint().rotation);

        }*/
        playerEntity.addComponent(mover);

        if (MazeVisualizationSystem.view != null) {
            /*15.5.21 das ist doch auch Asbach Kruecke
                MazeVisualizationSystem.view.setRayPosition(startpos);
                MazeVisualizationSystem.view.setRayRotation(new Degree(0));            */
        }
        mover.updateMovable();

        // 11.11.20: Raising the camera must be done again since splitting to system. Reason isType unclear, well the Avatar drops the position, so its obvious, but
        // why didn't that occur before?
        //15.5.21: Scene.getCurrent().getMainCamera().getCarrier().getTransform().setPosition(new Vector3(0, st.simplerayheight / 2 + 0.3f, 0.2f/*0/*+0.4f*/));

        InputToRequestSystem.setPayload0(playerEntity.getName());

        //avatar.avatarE.addComponent(new InventoryComponent());
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
        if (request.getType().equals(RequestRegistry.TRIGGER_REQUEST_TURNRIGHT)) {
            attemptRotate(currentstate, mover, false);
        } else if (request.getType().equals(RequestRegistry.TRIGGER_REQUEST_TURNLEFT)) {
            attemptRotate(currentstate, mover, true);
        } else if (request.getType().equals(RequestRegistry.TRIGGER_REQUEST_BACK)) {
            foundStuff = attemptMove(currentstate, mover, GridMovement.Back, user.getId());
        } else if (request.getType().equals(RequestRegistry.TRIGGER_REQUEST_FORWARD)) {
            foundStuff = attemptMove(currentstate, mover, GridMovement.Forward, user.getId());
        } else if (request.getType().equals(RequestRegistry.TRIGGER_REQUEST_UNDO)) {
            undo(currentstate, mover, Grid.getInstance().getMazeLayout());
        } else if (request.getType().equals(RequestRegistry.TRIGGER_REQUEST_FORWARDMOVE)) {
            // 10.4.21: Wer triggered denn einen TRIGGER_REQUEST_FORWARDMOVE? Nur der Replay?
            foundStuff = attemptMove(currentstate, mover, GridMovement.ForwardMove, user.getId());
        } else if (request.getType().equals(RequestRegistry.TRIGGER_REQUEST_LEFT)) {
            foundStuff = attemptMove(currentstate, mover, GridMovement.Left, user.getId());
        } else if (request.getType().equals(RequestRegistry.TRIGGER_REQUEST_RIGHT)) {
            foundStuff = attemptMove(currentstate, mover, GridMovement.Right, user.getId());
        } else if (request.getType().equals(RequestRegistry.TRIGGER_REQUEST_PULL)) {
            // pull kommt evtl auch bei undo? 27.5.21: jetzt auch eigenstaendig. UNDO hat aber sein eigenes Event.
            foundStuff = attemptMove(currentstate, mover, GridMovement.Pull, user.getId());
        } else if (request.getType().equals(RequestRegistry.TRIGGER_REQUEST_KICK)) {
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
     * TODO Fehlerbehandlung
     */
    private void loadLevel(String fileContent, String name) {
        logger.debug("loadLevel ");

        Grid grid;
        try {
            List<Grid> grids = Grid.loadByReader(new StringReader(fileContent));
            if (grids.size() > 1 && name != null) {
                grid = Grid.findByTitle(grids, name);
            } else {
                grid = grids.get(0);
            }

        } catch (InvalidMazeException e) {
            logger.error("load error: InvalidMazeException:" + e.getMessage());
            return;
        }
        Grid.setInstance(grid);

        for (Point b : grid.getBoxes()) {
            //EcsEntity box = MazeMovingAndStateSystem.buildSokobanBox(b.getX(), b.getY());
            //static EcsEntity buildSokobanBox(int x, int y) {
            SceneNode p = MazeModelBuilder.buildSokobanBox(/*b.getX(), b.getY()*/);
            EcsEntity box = new EcsEntity(p);
            MoverComponent mover = new MoverComponent(p.getTransform()/*this*/, false, b, new GridOrientation(), null);
            mover.setLocation(b);
            box.addComponent(mover);
            //return box;

            Scene.getCurrent().addToWorld(box.scenenode);
        }
        for (Point b : grid.getDiamonds()) {
            SceneNode p = MazeModelBuilder.buildDiamond();
            EcsEntity diamond = new EcsEntity(p);
            Vector3 dp = MazeUtils.point2Vector3(b);
            dp = new Vector3(dp.getX(), 0.8, dp.getZ());
            p.getTransform().setPosition(dp);
            // no diamond owner initially
            diamond.addComponent(new DiamondComponent(b));
            Scene.getCurrent().addToWorld(diamond.scenenode);
        }
        // only create bullets if we have bots (2 per bot). Bullets for player are created at join time
        /* TODO 14.3.22 Hmm for (Point b : grid.getBots()) {
            //SceneNode p = MazeModelBuilder.buildSokobanBox();
            SceneNode body = MazeModelBuilder.buildSimpleBody(MazeSettings.getSettings().simplerayheight, MazeSettings.getSettings().simpleraydiameter, Color.ORANGE);
            EcsEntity bot = new EcsEntity(body);

            bot.setName("Bot");
            MoverComponent mover = new MoverComponent(body.getTransform(), true, b, new GridOrientation());
            mover.setLocation(b);
            bot.addComponent(mover);
            //bot.addComponent(new InventoryComponent());
            Scene.getCurrent().addToWorld(bot.scenenode);
            createBullets(2, bot.getId());
        }*/


        SystemState.state = SystemState.STATE_READY_TO_JOIN;
        SystemManager.sendEvent(new Event(EventRegistry.EVENT_MAZE_LOADED, new Payload(grid)));

        //11.4.16 addTestObjekte();
        logger.debug("load completed");
    }

    private void createBullets(int cnt, int owner) {
        for (int i = 0; i < cnt; i++) {
            SceneNode ball = MazeModelBuilder.buildSimpleBall(0.3, MazeSettings.bulletColor);
            EcsEntity e = new EcsEntity(ball);
            BulletComponent bulletComponent = new BulletComponent(owner);
            e.addComponent(bulletComponent);
            e.setName("bullet");
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
     */
    private void help() {
        logger.debug("help");
        if (helphud == null) {
            helphud = Hud.buildForCamera(Scene.getCurrent().getDefaultCamera(), 1);
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
            gridOrientation = GridOrientation.fromDirection(StringUtils.charAt(orientation, 0));
            if (gridOrientation == null) {
                logger.warn("invalid orientation:" + orientation);
                return;
            }
        }

        GridMovement movement = (type == RequestRegistry.TRIGGER_REQUEST_RELOCATE) ? GridMovement.buildRelocate(p, gridOrientation) : GridMovement.buildTeleport(p, gridOrientation);

        MoverComponent mc = MoverComponent.getMoverComponent(player);

        attemptMove(currentstate, mc, movement, userEntityId);
        // attemptMove does not change orientation
        if (gridOrientation != null) {
            //otherwise keep orientation
            mc.setOrientation(gridOrientation);
        }
    }
}
