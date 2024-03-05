package de.yard.threed.traffic.apps;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.RuntimeTestUtil;
import de.yard.threed.engine.*;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.*;
import de.yard.threed.engine.gui.*;
import de.yard.threed.engine.platform.ProcessPolicy;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.traffic.AbstractTerrainBuilder;
import de.yard.threed.traffic.FlatTerrainSystem;
import de.yard.threed.traffic.GraphBackProjectionProvider;
import de.yard.threed.traffic.GraphTerrainSystem;

import de.yard.threed.traffic.GraphVisualizationSystem;
import de.yard.threed.traffic.LightDefinition;
import de.yard.threed.traffic.RequestRegistry;
import de.yard.threed.traffic.EllipsoidCalculations;
import de.yard.threed.traffic.SphereSystem;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.TrafficSystem;
import de.yard.threed.traffic.TrafficVrControlPanel;
import de.yard.threed.traffic.VehicleComponent;
import de.yard.threed.traffic.config.SceneConfig;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.graph.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.*;
import de.yard.threed.traffic.config.VehicleConfigDataProvider;
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
 * MA37: Entwicklung zu einer BasicTravelScene.
 * <p>
 * Konsolidierung von (Flat)TravelScene (früher OsmSceneryScene und GroundServicesScene) und RailingScene
 * in eine einzige Scene. Die TravelScene kann dann von hier ableiten und TerrainSystem zur Darstellung von FG
 * Sceneries verwenden.
 * <p>
 * Verzichtet auf das unübersichtliche TrafficWorld. Nutzt aber eine simplere Vehicleconfig.
 * <p>
 * Ob Flat (y0 oder z0) oder 3D sollte hier eigentlich egal bzw. transparent sein.
 * <p>
 * Features sind:
 * - Flat und Earth 3D (aber ohne 2D Projection, das ergibt sich aus dem Tile/Sphere)
 * - Standalone und als MP Server
 * - Keine Dependency zu FG bzw GPL. FG Modelle müssen von einer ableitenden Scene verwendet werden mit einer Art FG Plugin.
 * - Teleport zu POIs und in Vehivles, Cockpits. Alternativ auch Moving (Helikopter like?).
 *
 * <p>
 * Options:
 * enableFPC: FPC movement, no Teleporting, no Observer, no initialVehicle(why?),no Avatar (to avoid attaching to vehicles?).
 *
 * <p>
 * Funktionsweise:
 * 1) wie gehabt löst ein EVENT_LOCATIONCHANGED das Laden von Terrain und Graphen aus (innerhalb einer Sphere).
 * <p>
 * Offene Fragen:
 * - ist EVENT_LOCATIONCHANGED 2D/3D?
 * - Was ist denn initial? Eine Location? Oder ein Tile? Eine Sphere? Hilft Single/MultiTile Sphere?
 * - Gibt es eine 2DProjection? Vielleicht einfacher besser nicht. Der Routebuilder muesste dann abstrhiert werden.
 *
 * <p>
 * Usermode
 * 26.10.18: Generic traffic keycodes (vor allem usermode):
 * (S)tart Einen DefaultTravel startem, in der Regel ein Rundflug
 * (M)TouchSegment1: Open/Toggle optional menu in (VR has control panel at left writst)
 * (L)oad vehicle. Das naechste aus der Config das noch nicht geladen wurde. Sollte erst nicht mehr dabei sein und wird im UserMode nicht unbedingt gebraucht.
 * Seit controlmenu weg ist aber ganz praktisch.
 * (V) run tests. internal.
 * (CursorKeys) for view Left/Right/Up/Down in non VR (via ObserverSystem).
 * (X/Y/Z) for fine tuning of avatar position
 * <p>
 * Folgende KEys sind da bewusst nicht bei: ,
 * <p>
 * Start Sequence:
 * - Splash screen und "Loading" (transparent) mit progress. In the background scenery isType loading.
 * - Vehicle is loaded by argument "initialVehicle".
 * - When vehicle is available fadeout, teleport, fadein.
 * - "Click for Start" Button. Das ist dann quasi der 's' Key.
 * <p>
 * 4.10.21: MA37: Renamed TrafficCommon->BasicTravelScene und nicht mehr abstract. Auch standalone fuer tiles ohne FG.
 * This is also super class of TravelScene.
 * 14.11.23: never really existing 'help' and 'reset' removed. Could be added to menu.
 * 05.03.24: control menu added to be prepared for touch screens.
 * Created on 28.09.18.
 */
public class BasicTravelScene extends Scene /*31.10.23 implements RequestHandler */ {
    // Don't define a logger here by getLog(). Might result in NPE.
    // The default TravelScene has no hud.
    protected Hud hud;
    protected boolean visualizeTrack = false;
    protected boolean enableFPC = false, enableNearView = false;
    protected SceneConfig sceneConfig;
    //Manche Requests können wegen zu spezieller Abhängigkeiten nicht von ECS bearbeitet werden, sondern müssen aber auf App Ebene laufen.

    // nur bei Flat, in 3D null! 7.10.21 static bis es wegkommt
    //25.9.20 protected DefaultTrafficWorld trafficWorld;
    //22.10.21 wird noch einmal gebraucht
    protected TeleporterSystem teleporterSystem;
    RandomIntProvider rand = new RandomIntProvider();

    protected String vehiclelistname = "GroundServices";
    /*31.1.22
    RequestType REQUEST_MENU = RequestType.register("Menu");
    RequestType REQUEST_CYCLE = RequestType.register("Cycle");
    RequestType REQUEST_LOAD = RequestType.register("Load");
    RequestType REQUEST_PLAY = RequestType.register("Play");
    RequestType REQUEST_PLUS = RequestType.register("+");
    RequestType REQUEST_MINUS = RequestType.register("-");*/
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

    @Override
    public void init(SceneMode sceneMode) {
        getLog().debug("init BasicTravelScene with scene mode " + sceneMode.getMode());
        processArguments();

        vrInstance = VrInstance.buildFromArguments();

        trafficSystem = new TrafficSystem();
        SystemManager.addSystem(trafficSystem);

        SystemManager.addSystem(new SphereSystem(getRbcp(), getGraphBackProjectionProvider(), getCenter(), getSceneConfig()));
        ((SphereSystem) SystemManager.findSystem(SphereSystem.TAG)).setDefaultLightDefinition(getLight());
        SystemManager.addSystem(new GraphMovingSystem());
        SystemManager.addSystem(new GraphTerrainSystem(getTerrainBuilder()));
        //SystemManager.addSystem(new GroundServicesSystem());
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

        //visualizeTrack soll auch im usermode verfuegbar sein.
        /*29.12.23 was commented, but back now with abstract provider for visualizer*/
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

        if (enableFPC) {
            FirstPersonMovingSystem.addDefaultKeyBindings(inputToRequestSystem);
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
                inputToRequestSystem.setControlMenuBuilder(camera -> buildControlMenuForScene(camera));

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

        customInit();
        postInit(sceneMode);
    }

    /**
     * 29.12.23 Not hardcoded but can be overridden.
     */
    public GraphVisualizer getGraphVisualizer() {
        return new SimpleGraphVisualizer(getWorld());
    }

    public SceneConfig getSceneConfig() {
        return null;
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
       /*21.3.19  String argv_enableusermode = ((Platform) Platform.getInstance()).getSystemProperty("enableUsermode");
        //argv_enableusermode="1";
        if (!Util.isFalse(argv_enableusermode)) {
            usermode = new Usermode();
        }*/


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
                Request request;
                // no userid known. Might not be user related. TODO Maybe the request via parameter isn't used any more.
                // Its quite early to send this request now, but the receiver should wait until the time comes.
                request = RequestRegistry.buildLoadVehicle(-1, argv_initialVehicle, null);
                SystemManager.putRequest(request);
            }
        }

        String argv_vehiclelistname = Platform.getInstance().getConfiguration().getString("vehiclelist");
        if (argv_vehiclelistname != null) {
            vehiclelistname = argv_vehiclelistname;
        }

        if ((b = Platform.getInstance().getConfiguration().getBoolean("enableNearView")) != null) {
            enableNearView = (boolean) b;
        }

        // Parameter basename gibt es eigentlich nur in 2D. 7.10.21: Aber das wird hier jetzt einach mal als Request sent,
        // wenns nicht relevant oder ungueltig ist, verfaellt es halt. Und ich fuehre auch wieder den Deault EDDK ein.

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
     * Tile name needs to be a full qualified bundle name.
     */
    public String getDefaultTilename() {
        // null leads to 3D
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
        //27.10.21 War sonst früher
        //8.12.21 addLight();

        //1.2.24 already created in init() SystemManager.addSystem(new InputToRequestSystem());

        //29.10.21: Damit lauchVehicles noch geht. TODO anders.
        //28.11.23 scenconfig is no longer used. 'baseTransformForVehicleOnGraph' is now set in SphereSystem for now.
        /*28.11.23 TrafficSystem.sceneConfig = sceneConfig;
        if (sceneConfig != null) {
            TrafficSystem.baseTransformForVehicleOnGraph = sceneConfig.getBaseTransformForVehicleOnGraph();
        } else {
            //8.11.21: position part apparently ignored
            //28.11.23:For what is this fix value? Seems not to be any known rotation. Its for Demo/Wayland.
            TrafficSystem.baseTransformForVehicleOnGraph = new LocalTransform(new Vector3(0, 0, 0), Quaternion.buildFromAngles(new Degree(0), new Degree(-90), new Degree(0)));
        }*/

        //erst jetzt, wenn ECS schon inited ist? Nee, muesste auch schon vorher gehen. Ist aber auch egal. Obwohl die Darstellung richtiger ist
        //als wenn man es im commoninit macht.
        initSpheres();

        //27.11.23 Now in TrafficSystem SystemManager.putDataProvider("vehicleconfig", getVehicleConfigDataProvider());

        //7.10.21:Jetzt hier statt als erstes im Update. Aber fuer die Initialposition muss man das tile kennen. Darum als erstes Tile per
        //Sphere laden. Von da wird dann das alte "sendInitialEvents" gemacht.
        SystemManager.putRequest(new Request(SphereSystem.USER_REQUEST_SPHERE, new Payload(tilename/*17.10.21 TrafficWorld2D.basename*/, getVehicleList())));

        // create player/Avatar (via login)
        if (sceneMode.isClient()) {
            SystemManager.putRequest(UserSystem.buildLoginRequest(DEFAULT_USER_NAME, ""));
        }
        // 24.1.22: State ready to join now needed for 'login'
        SystemState.state = SystemState.STATE_READY_TO_JOIN;
        //sendInitialEvents(initialPosition);
    }

    /**
     * 29.4.19: Vor allem wegen Building neben ambient auch ein DirectionalLight (aus 45Grad von "SuedOst").
     * Das ist nach allgemeiner Lehre wohl die richtige Kombination; ein directional und ein ambient. In JME sind die Taxiways zu dunkel, in GWT ok.
     * Und in JME scheint ambient bei Buildings nicht zugreifen (wegen flat shading?). Die Nordseiten sind zu dunkel.
     * 27.10.21: Aus FlatTravelScene hierhin verschoben.
     */
    public LightDefinition[] getLight() {
        // quasi senkrecht von oben
        //9.5.19 DirectionalLight light = new DirectionalLight(Color.WHITE, new Vector3(0, 0, 2));
        //9.5.19 addLightToWorld(light);

        /*7.12.21 Light light = new DirectionalLight(Color.WHITE, new Vector3(3, -3, 3));
        addLightToWorld(light);
        light = new AmbientLight(Color.WHITE);
        addLightToWorld(light);*/
        return new LightDefinition[]{
                new LightDefinition(Color.WHITE, new Vector3(3, -3, 3)),
                new LightDefinition(Color.WHITE, null),
        };
    }

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


        /*7.10.21 jetzt im postinit
             if (initialPosition != null) {
            //7.5.19 zunaechst mal nur einmalig beim Start.
            sendInitialEvents(initialPosition);
             initialPosition = null;
        }*/

        /*if (usermode != null) {
            if (!usermode.reachedposition) {
                TeleportComponent teleportComponent = TeleportComponent.getTeleportComponent(avatar.avatarE);
                int captainpos = teleportComponent.findPoint("Captain");
                if (captainpos != -1) {
                    teleportComponent.stepTo(captainpos);
                    usermode.reachedposition = true;
                }
            }
            if (usermode.reachedposition) {
                // 26.10.18: ein menu geht manchmal (z.B. in VR oder bei Bewegung) noch nicht so gut. Darum erstmal nur mit key 's' starten.
            }
        }*/
        if (Input.getKeyDown(KeyCode.L)) {
            // load next not yet loaded vehicle. 1.2.24: What is using this? Hangar/Cockpit isn't. But FlatAirportScene does.
            Request request = RequestRegistry.buildLoadVehicle(UserSystem.getInitialUser().getId(), null, null);
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
    }

    /**
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

    /**
     * 9.10.19: Liefert das Vehicle, in dem der Avatar sitzt, ansonsten null.
     * 14.10.19 Jetzt nicht mehr ueber diese krude Suche.
     * Entity im SystemManager suchen? Eher vom Teleport. Die Frage ist, wo der Avatar gerade ist.
     *
     * @return
     */
    protected EcsEntity getAvatarVehicle() {
        TeleportComponent tc = TeleportComponent.getTeleportComponent(/*24.10.21avatar*/UserSystem.getInitialUser());
        /*SceneNode avatarparent = tc.getParent();
        String parentname = avatarparent.getName();
        getLog().debug("avatarparent=" + parentname);
        List<NativeSceneNode> navigatorresult = avatarparent.findNodeByName("navigator.gltf", true);
        if (navigatorresult.size() > 0) {
 // 3.1.20: Aircraft ermitteln, in dem Avatar sitzt. Oder irgendwie anders.
        EcsEntity aircraft = FlatTravelScene.findVehicleByName("c172p");
        if (aircraft == null) {
            aircraft = FlatTravelScene.findVehicleByName("bluebird");
        }
        */
        String name = tc.getTargetEntity();
        if (name == null) {
            getLog().warn("no target entity in TC");
            return null;
        }
        return TrafficHelper.findVehicleByName(name);

    }

    @Override
    public void initSettings(Settings settings) {
        settings.vrready = true;
    }

    /**
     * 7.10.21: Some defaults instead of using abstract. Defaults fit to 2D tiles.
     * and should be overridden by 3D and projecting scenes.
     */
    public VehicleConfigDataProvider getVehicleConfigDataProvider() {
        return new VehicleConfigDataProvider(null);
    }

    protected Log getLog() {
        return Platform.getInstance().getLog(BasicTravelScene.class);
    }

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

    public AbstractTerrainBuilder getTerrainBuilder() {
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
     * The default non VR Control menu.
     * Camera is a deferred camera defined during init.
     * Might be overridden for custom menu.
     */
    public GuiGrid buildControlMenuForScene(Camera camera) {

        GuiGrid controlmenu = GuiGrid.buildForCamera(camera, 2, 5, 1, Color.BLACK_FULLTRANSPARENT, true);

        controlmenu.addButton(0, 0, 1, Icon.ICON_POSITION, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(UserSystem.USER_REQUEST_TELEPORT, new Payload(new Object[]{new IntHolder(0)})));
        });
        controlmenu.addButton(1, 0, 1, Icon.ICON_MENU, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_MENU));
        });
        controlmenu.addButton(2, 0, 1, Icon.ICON_HORIZONTALLINE, () -> {
            //TODO incMovementSpeed
        });
        controlmenu.addButton(3, 0, 1, Icon.ICON_PLUS, () -> {
            //TODO incMovementSpeed
        });
        controlmenu.addButton(4, 0, 1, Icon.ICON_CLOSE, () -> {
            InputToRequestSystem.sendRequestWithId(new Request(InputToRequestSystem.USER_REQUEST_CONTROLMENU));
        });
        return controlmenu;
    }

}