package de.yard.threed.traffic.apps;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.RuntimeTestUtil;
import de.yard.threed.engine.*;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.*;
import de.yard.threed.engine.gui.*;
import de.yard.threed.engine.platform.ProcessPolicy;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.traffic.AbstractSceneryBuilder;
import de.yard.threed.traffic.BuilderRegistry;
import de.yard.threed.traffic.FlatTerrainSystem;
import de.yard.threed.traffic.GraphBackProjectionProvider;
import de.yard.threed.traffic.GraphTerrainSystem;

import de.yard.threed.traffic.GraphVisualizationSystem;
import de.yard.threed.traffic.MoonSceneryBuilder;
import de.yard.threed.traffic.RequestRegistry;
import de.yard.threed.trafficcore.EllipsoidCalculations;
import de.yard.threed.traffic.FreeFlyingSystem;
import de.yard.threed.traffic.ScenerySystem;
import de.yard.threed.traffic.SphereSystem;
import de.yard.threed.traffic.TrafficSystem;
import de.yard.threed.traffic.TrafficVrControlPanel;
import de.yard.threed.traffic.VehicleComponent;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.graph.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.*;
import de.yard.threed.trafficcore.model.Vehicle;
import de.yard.threed.engine.util.NearView;
import de.yard.threed.engine.util.RandomIntProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic generic scene for traffic scene definitions like "traffic:tiles/Wayland.xml" and "traffic:tiles/Demo.xml".
 * <p>
 * Was (Flat)TravelScene and TrafficCommon once (also merged former OsmSceneryScene, GroundServicesScene,RailingScene)
 * to a single scene.
 * <p>
 * Loads terrain und graphs from a sphere.
 * Other TravelScene can extends from here and use TerrainSystem for showing FG Sceneries.
 * <p>
 * No more using a complex TrafficWorld, but a more simple Vehicleconfig.
 * <p>
 * Flat (y0 or z0) or 3D shouldn't make a difference here or should be transparent (via a projection callback?).
 * <p>
 * Features are:
 * - Flat und Earth 3D (depending on the initial Tile/Sphere)
 * - This is also super class of FG TravelScenes.
 * - No Dependency to FG or GPL. Extending FG classes know how to load a FG model.
 * - Teleport to POIs and to vehicles, Cockpits.
 *
 * <p>
 * Options:
 * enableFPC: FPC movement, no Teleporting, no Observer, no initialVehicle(why?),no Avatar (to avoid attaching to vehicles?).
 *
 * <p>
 * 26.10.18: Generic traffic keycodes:
 * not yet here(needs FlightSystem)! (S)tart a default trip, at an airport typically a round trip. Applies to 'current' vehicle and only if it is graph bound.
 * (M)TouchSegment1: Open/Toggle optional menu in (VR has control panel at left wrist)
 * (L)oad vehicle. Applies to next configured but not yet loaded vehicle.
 * (V) run tests. internal.
 * (CursorKeys) for view Left/Right/Up/Down in non VR (via ObserverSystem).
 * (X/Y/Z) for fine tuning of avatar position
 * <p>
 * Start Sequence (in future):
 * - Splash screen und "Loading" (transparent) mit progress. In the background scenery is loading.
 * - Vehicle is loaded by argument "initialVehicle".
 * - When vehicle is available fadeout, teleport, fadein.
 * - "Click for Start" Button. Das ist dann quasi der 's' Key.
 * <p>
 * <p>
 * 14.11.23: never really existing 'help' and 'reset' removed. Could be added to menu.
 * 05.03.24: control menu added to be prepared for touch screens.
 * Created on 28.09.18.
 */
public class BasicTravelScene extends Scene {
    // Don't define a logger here by getLog(). Might result in NPE.
    // The default TravelScene has no hud.
    protected Hud hud;
    protected boolean visualizeTrack = false;
    protected boolean enableFPC = false, enableNearView = false;
    //Manche Requests können wegen zu spezieller Abhängigkeiten nicht von ECS bearbeitet werden, sondern müssen aber auf App Ebene laufen.

    //22.10.21 wird noch einmal gebraucht
    protected TeleporterSystem teleporterSystem;
    RandomIntProvider rand = new RandomIntProvider();

    protected String vehiclelistname = "GroundServices";
    protected NearView nearView = null;
    // 17.10.21: per argument oder default EDDK
    String tilename = null;
    VrInstance vrInstance;
    public Map<String, ButtonDelegate> buttonDelegates = new HashMap<String, ButtonDelegate>();
    ControlPanel leftControllerPanel = null;
    public static String DEFAULT_USER_NAME = "Freds account name";
    public TrafficSystem trafficSystem;
    // in non VR for menu and control menu
    protected Camera cameraForMenu = null;
    private String waitsForInitialVehicle = null;

    @Override
    public void init(SceneMode sceneMode) {
        getLog().debug("init BasicTravelScene with scene mode " + sceneMode.getMode());
        processArguments();

        vrInstance = VrInstance.buildFromArguments();

        trafficSystem = new TrafficSystem();
        SystemManager.addSystem(trafficSystem);

        SphereSystem sphereSystem = new SphereSystem(/*getRbcp(),*/ getGraphBackProjectionProvider()/*16.3.24, getCenter() getSceneConfig()*/);
        SystemManager.addSystem(sphereSystem);
        //((SphereSystem) SystemManager.findSystem(SphereSystem.TAG)).setDefaultLightDefinition(getLight());
        SystemManager.addSystem(new GraphMovingSystem());
        SystemManager.addSystem(new GraphTerrainSystem(getTerrainBuilder()));
        FreeFlyingSystem freeFlyingSystem = FreeFlyingSystem.buildFromConfiguration();
        SystemManager.addSystem(freeFlyingSystem);
        SystemManager.addSystem(new VelocitySystem());

        if (enableFPC) {
            FirstPersonMovingSystem firstPersonMovingSystem = FirstPersonMovingSystem.buildFromConfiguration();
            SystemManager.addSystem(firstPersonMovingSystem);
            // key bindings are done below
        } else {
            // Verhalten aus FltaTravelScene. Wenn FPC, dann keine Teleport. TravelScene hatte dafuer freecam?
            teleporterSystem = new TeleporterSystem();
            //anim ist zu ruckelig/fehlerhaft
            teleporterSystem.setAnimated(false);
            SystemManager.addSystem(teleporterSystem);
        }
        // ObserverSystem is needed for attaching observer (not conflicting with FPC due to disabled components). But also in VR?
        SystemManager.addSystem(new ObserverSystem(), 0);
        SystemManager.addSystem(new UserSystem());
        SystemManager.addSystem(new AvatarSystem(sceneMode.isClient()), 0);

        SystemManager.addSystem(new ScenerySystem(sphereSystem.world));

        if (visualizeTrack) {
            SystemManager.addSystem(new GraphVisualizationSystem(getGraphVisualizer()));
        }
        for (EcsSystem ecsSystem : getCustomTerrainSystems()) {
            SystemManager.addSystem(ecsSystem);
        }

        // Observer concept: Den Observer kann es direkt noch vor login/join geben. Er zeigt dann z.B. einen Overview.
        // Bei login/join kann er dann an den Avatar? Auch für VR? (MA35) Oder nie? Oder unabhaengig/doppelt?
        // server has no camera.
        if (sceneMode.isClient()) {
            Observer.buildForDefaultCamera();
        }

        buttonDelegates.put("info", () -> {
            VrInstance.getInstance().dumpDebugInfo();
            Observer.getInstance().dumpDebugInfo();
        });
        buttonDelegates.put("up", () -> {
            getLog().info("up");
            Observer.getInstance().fineTune(true);
        });
        buttonDelegates.put("down", () -> {
            getLog().info("down");
            Observer.getInstance().fineTune(false);
        });
        buttonDelegates.put("speedup", () -> {
        });
        buttonDelegates.put("speeddown", () -> {
        });
        buttonDelegates.put("teleport", () -> {
            IntHolder option = new IntHolder(0);
            Request request = new Request(UserSystem.USER_REQUEST_TELEPORT, new Payload(new Object[]{option}));
            SystemManager.putRequest(request);
        });

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = getProcessPolicy();

        InputToRequestSystem inputToRequestSystem = new InputToRequestSystem();
        // 5.3.24:Move keys to non VR?
        inputToRequestSystem.addKeyMapping(KeyCode.M, InputToRequestSystem.USER_REQUEST_MENU);
        //toggle auto move
        inputToRequestSystem.addKeyMapping(KeyCode.Alpha9, UserSystem.USER_REQUEST_AUTOMOVE);

        // only one system should register for ROLL*, TURN* requests to avoid conflicts. So we have to decide. Typically it will be FreeFlyingSystem.
        // Should be fixed by 'per avatar position' request.
        if (enableFPC) {
            FirstPersonMovingSystem.addDefaultKeyBindingsforContinuousMovement(inputToRequestSystem);
            FirstPersonMovingSystem.setMouseDragBindingsforMovement(inputToRequestSystem);
        } else {
            FreeFlyingSystem.addDefaultKeyBindingsforContinuousMovement(inputToRequestSystem);
            FreeFlyingSystem.setMouseDragBindingsforMovement(inputToRequestSystem);
        }

        if (vrInstance != null) {
            // Even in VR the observer will be attached to avatar later
            // Observer was inited before
            Observer observer = Observer.getInstance();
            // 1.4.22 Probably still the need to change yoffsetvr here as long as green box avatar is used?
            observer.initFineTune(vrInstance.getOffsetVR());
            observer.attach(vrInstance.getController(0));
            observer.attach(vrInstance.getController(1));

        } else {
            // nothing special (menu,hud,controlpanel) for non VR? But no in server mode where there is no camera.
            if (sceneMode.isClient()) {
                inputToRequestSystem.setControlMenuBuilder(camera -> buildControlMenuForScene(camera, false));

                // use dedicated camera for menu to avoid picking ray issues due to large/small dimensions conflicts
                cameraForMenu = FovElement.getDeferredCamera(getDefaultCamera());

                // might be null if not desired
                inputToRequestSystem.setCameraForMenu(cameraForMenu);

                // 5.3.24: menu only in non VR
                inputToRequestSystem.setMenuProvider(new DefaultMenuProvider(cameraForMenu, (Camera camera) -> {
                    /**
                     * 18.2.22 Locate menu at near plane like it was when FovElement was used. That should help
                     * to avoid menu coverage by cockpits. But more efficient might be a deferred camera.
                     * Appropriate width/height depend from 'near' to have a semi screen width. Cannot use fix values
                     * but needs to calculate. width 0.07 is quite good for near value of 0.1, leading to quotient 1.43.
                     */
                    double width = 0.07;
                    double zpos = -camera.getNear() - 0.001;
                    double buttonzpos = 0.0001;
                    width = camera.getNear() / 1.43;

                    ControlPanel m = ControlPanelHelper.buildSingleColumnFromMenuitems(new DimensionF(width, width * 0.7), zpos, buttonzpos, /*sc.*/getMenuItems(), Color.GREEN);
                    ControlPanelMenu menu = new ControlPanelMenu(m);
                    return menu;

                }));
            }
        }


        SystemManager.addSystem(inputToRequestSystem, 0);
        //6.10.21 buildToggleMenu();
        //menuCycler = new MenuCycler(new MenuProvider[]{new TravelMainMenuBuilder(this)});

        // Add MoonSceneryBuilder just to have it. Currently we don't know a better location becuase init()
        // needs to be called. If its not needed it doesn't harm.
        BuilderRegistry.add("MoonSceneryBuilder", (ObjectBuilder<AbstractSceneryBuilder>) s -> {
            AbstractSceneryBuilder moonSceneryBuilder = new MoonSceneryBuilder();
            moonSceneryBuilder.init(sphereSystem.world);
            return moonSceneryBuilder;
        });

        if (false) {
            // The axis helper dimensions fit to 'wayland'
            addToWorld(ModelSamples.buildAxisHelper(500, 10.0));
        }

        String dragControl = Platform.getInstance().getConfiguration().getString("dragControl", "movement");
        if (dragControl.equals("movement")) {
            // The default. Only one of these systems should be set up.
            if (SystemManager.findSystem(FirstPersonMovingSystem.TAG) != null) {
                FirstPersonMovingSystem.setMouseDragBindingsforMovement(inputToRequestSystem);
            }
            if (SystemManager.findSystem(FreeFlyingSystem.TAG) != null) {
                FreeFlyingSystem.setMouseDragBindingsforMovement(inputToRequestSystem);
            }
        } else if (dragControl.equals("view")) {
            if (SystemManager.findSystem(ObserverSystem.TAG) != null) {
                ObserverSystem.setMouseDragBindingsforOrientation(inputToRequestSystem);
            }
        } else {
            getLog().warn("Unknown dragControl:" + dragControl);
        }
        customInit();
        postInit(sceneMode);
    }

    /**
     * 29.12.23 Not hardcoded but can be overridden.
     */
    public GraphVisualizer getGraphVisualizer() {
        return new SimpleGraphVisualizer(getWorld());
    }

    public GeoCoordinate getCenter() {
        return null;
    }

    /**
     * To be overridden by extending class.
     */
    public void customInit() {
    }

    /**
     * Wer das so nicht braucht, soll es overriden.
     * 7.10.21
     *
     * @return
     */
    public EcsSystem[] getCustomTerrainSystems() {
        return new EcsSystem[]{new FlatTerrainSystem(/*20.10.21null, null*/)};
    }

    protected void processArguments() {


        String argv_visualizeTrack = Platform.getInstance().getConfiguration().getString("visualizeTrack");
        if (Util.isTrue(argv_visualizeTrack)) {
            visualizeTrack = true;
        }

        Boolean b;
        if ((b = Platform.getInstance().getConfiguration().getBoolean("enableFPC")) != null) {
            enableFPC = (boolean) b;
            getLog().info("Setting enableFPC");
        }

        String argv_initialVehicle = Platform.getInstance().getConfiguration().getString("initialVehicle");
        if (argv_initialVehicle != null) {
            // 16.11.23: Why shouldn't we load the vehicle?
            if (enableFPC) {
                getLog().info("Ignoring initialVehicle due to FPC");
            } else {
                // 7.3.25 'request send' moved to update()
                waitsForInitialVehicle = argv_initialVehicle;
            }
        }

        String argv_vehiclelistname = Platform.getInstance().getConfiguration().getString("vehiclelist");
        if (argv_vehiclelistname != null) {
            vehiclelistname = argv_vehiclelistname;
        }

        if ((b = Platform.getInstance().getConfiguration().getBoolean("enableNearView")) != null) {
            enableNearView = (boolean) b;
        }

        // Parameter basename is for 2D and 3D.

        tilename = Platform.getInstance().getConfiguration().getString("basename");

        if (tilename == null) {
            tilename = getDefaultTilename();
        }
        getLog().debug("using tilename " + tilename);
        customProcessArguments();
    }

    /**
     * To be overridden by extending class.
     */
    protected void customProcessArguments() {
    }

    /**
     * To be overridden by extending class.
     * This default is used when no tilename is passed by parameter.
     * Tile name needs to be a full qualified bundle name or geo coordinate.
     */
    public String getDefaultTilename() {
        // null leads to 3D. 19.3.24: No longer! null will be ignored.
        return null;
    }

    protected void initHud() {
        //5.10.18: Hud braucht viel Speicher (und damit auch (GC) CPU). Darum optional. Aber per default an.
        //31.3.20: Seit dem Einbau einer deferred Cam bzw. nearview scheint das nicht mehr zu gehen. Offenbar muss man jetzt selber den attach machen.
        String argv_enableHud = Platform.getInstance().getConfiguration().getString("enableHud");
        if (!Util.isFalse(argv_enableHud)) {
            hud = Hud.buildForCameraAndAttach(getDefaultCamera(), 0);
        }
    }

    public MenuItem[] getMenuItems() {
        return new MenuItem[]{
                new MenuItem("teleport", () -> {
                    IntHolder option = new IntHolder(0);
                    Request request = new Request(UserSystem.USER_REQUEST_TELEPORT, new Payload(new Object[]{option}));
                    SystemManager.putRequest(request);
                })
        };
    }

    @Override
    public String[] getPreInitBundle() {
        return new String[]{"engine", "data", "traffic"};
    }

    private void postInit(SceneMode sceneMode) {

        initSpheres();

        //27.11.23 VehicleConfigDataProvider now in TrafficSystem
        //7.10.21:Load tile per Sphere laden. SphereSystem will then send initial events
        SystemManager.putRequest(new Request(SphereSystem.USER_REQUEST_SPHERE, new Payload(tilename, getVehicleList())));

        // create player/Avatar (via login)
        if (sceneMode.isClient()) {
            SystemManager.putRequest(UserSystem.buildLoginRequest(DEFAULT_USER_NAME, ""));
        }
        // 24.1.22: State ready to join now needed for 'login'
        SystemState.state = SystemState.STATE_READY_TO_JOIN;

    }

    /**
     * 29.4.19: Vor allem wegen Building neben ambient auch ein DirectionalLight (aus 45Grad von "SuedOst").
     * Das ist nach allgemeiner Lehre wohl die richtige Kombination; ein directional und ein ambient. In JME sind die Taxiways zu dunkel, in GWT ok.
     * Und in JME scheint ambient bei Buildings nicht zugreifen (wegen flat shading?). Die Nordseiten sind zu dunkel.
     * 27.10.21: Aus FlatTravelScene hierhin verschoben.
     * 14.5.24: Deprecated now because 3D also moved to config files
     */
    /*21.8.25 @Deprecated
    public LightDefinition[] getLight() {
        // quasi senkrecht von oben
        //9.5.19 DirectionalLight light = new DirectionalLight(Color.WHITE, new Vector3(0, 0, 2));
        //9.5.19 addLightToWorld(light);

        /*7.12.21 Light light = new DirectionalLight(Color.WHITE, new Vector3(3, -3, 3));
        addLightToWorld(light);
        light = new AmbientLight(Color.WHITE);
        addLightToWorld(light);* /
        return new LightDefinition[]{
                new LightDefinition(Color.WHITE, new Vector3(3, -3, 3)),
                new LightDefinition(Color.WHITE, null),
        };
    }*/
    @Override
    public void update() {
        double tpf = getDeltaTime();
        commonUpdate();
        customUpdate(tpf);
    }

    /**
     * To be overridden.
     */
    public void customUpdate(double tpf) {
    }

    private void commonUpdate() {

        if (Input.getKeyDown(KeyCode.L)) {
            // load next not yet loaded vehicle. 1.2.24: What is using this? Hangar/Cockpit isn't. But FlatAirportScene does.
            Request request = RequestRegistry.buildLoadVehicle(UserSystem.getInitialUser().getId(), null, null, null, null);
            SystemManager.putRequest(request);
        }

        if (Input.getKeyDown(KeyCode.R)) {
            report();
        }
        //26.10.18: 'T' ist jetzt teleport. 23.10.21: Einheitlich wie sonst 'V' statt 'E'
        if (Input.getKeyDown(KeyCode.V)) {
            runTests();
        }

        //for x/y/z. Only in debug mode (MazeSettings.getSettings().debug)? Better location??
        if (Observer.getInstance() != null) {
            Observer.getInstance().update();
        }

        // VR controller might not be available until the user enters VR. So attach the controller the first time
        // they are found.
        if (vrInstance != null && vrInstance.getController(0) != null && leftControllerPanel == null) {

            leftControllerPanel = buildVrControlPanel();

            vrInstance.attachControlPanelToController(vrInstance.getController(0), leftControllerPanel);

            InputToRequestSystem inputToRequestSystem = (InputToRequestSystem) SystemManager.findSystem(InputToRequestSystem.TAG);
            inputToRequestSystem.addControlPanel(leftControllerPanel);
        }

        if (waitsForInitialVehicle != null) {
            // one time runtime initialization
            // 7.3.25 'Request send' was in init() before, but that's far too early, even though the request is queued.
            // Sphere is not yet loaded at that time.
            // no userid known? Might not be user related. TODO Maybe the request via parameter isn't used any more.7.3.25 todo still valid?
            Request request = RequestRegistry.buildLoadVehicle(-1, waitsForInitialVehicle,
                    Platform.getInstance().getConfiguration().getString("initialLocation"),
                    Platform.getInstance().getConfiguration().getString("initialRoute"),
                    Platform.getInstance().getConfiguration().getString("initialHeading"));
            SystemManager.putRequest(request);
            waitsForInitialVehicle = null;
        }
    }

    /**
     * Returns a projection to convert a position on a non 3D graph to a 3D position.
     * Thats the reason for the phrase 'back', even though its not the opposite of the forward projection.
     * GraphProjectionFlight3D needs FgMath
     *
     * @return
     */
    public GraphBackProjectionProvider getGraphBackProjectionProvider() {
        return null;
    }

    private void report() {
        List<EcsEntity> vehicles = SystemManager.findEntities((e) -> VehicleComponent.getVehicleComponent(e) != null);
        for (EcsEntity e : vehicles) {
            VehicleComponent vhc = VehicleComponent.getVehicleComponent(e);
            GraphMovingComponent gmc = GraphMovingComponent.getGraphMovingComponent(e);
            //GroundServiceComponent gsc = GroundServiceComponent.getGroundServiceComponent(e);
            String lockinfo = "";
            if (e.isLocked()) {
                lockinfo = ", locked by " + e.getLockOwner();
            }
            getLog().debug("name=" + e.getName() + lockinfo);
        }
    }


    @Override
    public void initSettings(Settings settings) {
        settings.vrready = true;
    }

    /**
     * 7.10.21: Some defaults instead of using abstract. Defaults fit to 2D tiles.
     * and should be overridden by 3D and projecting scenes.
     */
    protected Log getLog() {
        return Platform.getInstance().getLog(BasicTravelScene.class);
    }

    /**
     * Custom init for extending scenes
     */
    protected void initSpheres() {
    }

    protected void runTests() {
        getLog().debug("Running tests");
        // When there was a user starting tests there should also be an observer.
        RuntimeTestUtil.assertNotNull("observer", Observer.getInstance());
    }

    public EllipsoidCalculations getRbcp() {
        return null;
    }

    public GraphProjection getBackProjection() {
        return null;
    }

    public ProcessPolicy getProcessPolicy() {
        return null;
    }

    public AbstractSceneryBuilder getTerrainBuilder() {
        return null;
    }

    /**
     * The default. Might be overridden.
     */
    public ControlPanel buildVrControlPanel() {
        return new TrafficVrControlPanel(buttonDelegates);
    }

    /**
     * The default implementation for retrieving the vehicle list. Ends
     * up in a DataProvider by SphereSystem.
     * List could come from a tile?
     * 27.2.24: Purpose should be redefined.
     * Is it per sphere/tile? or per graph? Shouldn't each system read the config on its own?
     * Or is dataprovider sufficient?
     * <p>
     * 28.10.21
     *
     * @return
     */
    public List<Vehicle> getVehicleList() {
        List<Vehicle> vehicleList = new ArrayList<Vehicle>();
        return vehicleList;
    }

    /**
     * The non VR Control menu.
     * Camera is a deferred camera defined during init.
     * 4.4.25:Merged from BasicTravelScene, TravelScene(Bluebird) and -/+ added.
     * No longer needs to be overridden for custom menu.
     * Avoid gaps if buttons aren't needed.
     * <p>
     */
    public GuiGrid buildControlMenuForScene(Camera camera, boolean withLoad) {

        int columns = 5;
        if (withLoad) {
            columns++;
        }
        GuiGrid controlmenu = GuiGrid.buildForCamera(camera, 2, columns, 1, Color.BLACK_FULLTRANSPARENT, true);

        int col = 0;
        controlmenu.addButton(col++, 0, 1, Icon.ICON_POSITION, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(UserSystem.USER_REQUEST_TELEPORT, new Payload(new Object[]{new IntHolder(0)})));
        });
        controlmenu.addButton(col++, 0, 1, Icon.ICON_MENU, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_MENU));
        });
        if (withLoad) {
            // 'L' for load
            controlmenu.addButton(col++, 0, 1, Icon.IconCharacter(11), () -> {
                SystemManager.putRequest(RequestRegistry.buildLoadVehicle(UserSystem.getInitialUser().getId(), null, null, null, null));
                //updateHud();
            });
        }
        controlmenu.addButton(col++, 0, 1, Icon.ICON_HORIZONTALLINE, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(BaseRequestRegistry.TRIGGER_REQUEST_SPEEDDOWN));
        });
        controlmenu.addButton(col++, 0, 1, Icon.ICON_PLUS, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(BaseRequestRegistry.TRIGGER_REQUEST_SPEEDUP));
        });
        controlmenu.addButton(col++, 0, 1, Icon.ICON_CLOSE, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_CONTROLMENU));
        });
        return controlmenu;
    }

}