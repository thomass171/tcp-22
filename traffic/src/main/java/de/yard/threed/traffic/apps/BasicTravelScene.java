package de.yard.threed.traffic.apps;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformHelper;
import de.yard.threed.engine.*;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.*;
import de.yard.threed.engine.gui.*;
import de.yard.threed.engine.platform.ProcessPolicy;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.traffic.AbstractTerrainBuilder;
import de.yard.threed.traffic.FlatTerrainSystem;
import de.yard.threed.traffic.GraphBackProjectionProvider;
import de.yard.threed.traffic.GraphTerrainSystem;

import de.yard.threed.traffic.LightDefinition;
import de.yard.threed.traffic.RequestRegistry;
import de.yard.threed.traffic.RoundBodyCalculations;
import de.yard.threed.traffic.SimpleVehicleLoader;
import de.yard.threed.traffic.SphereProjections;
import de.yard.threed.traffic.SphereSystem;
import de.yard.threed.traffic.TrafficGraph;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.traffic.TrafficSystem;
import de.yard.threed.traffic.TrafficVrControlPanel;
import de.yard.threed.traffic.VehicleComponent;
import de.yard.threed.traffic.VehicleLauncher;
import de.yard.threed.traffic.VehicleLoader;
import de.yard.threed.traffic.config.ConfigNode;
import de.yard.threed.traffic.config.ConfigNodeList;
import de.yard.threed.traffic.config.SceneConfig;
import de.yard.threed.traffic.config.VehicleConfig;
import de.yard.threed.traffic.geodesy.GeoCoordinate;
import de.yard.threed.trafficcore.model.SmartLocation;
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
 * 26.10.18: Jetzt mal einheitlich für Traffic definierte Keycodes (vor allem usermode):
 * (P)ause
 * (S)tart Einen DefaultTravel startem, in der Regel ein Rundflug
 * (M)enu
 * (R)eset
 * (H)elp. Brauchts vielleicht nicht mehr seit Toggle/Browsemenü?
 * (L)oad vehicle. Das naechste aus der Config das noch nicht geladen wurde. Sollte erst nicht mehr dabei sein und wird im UserMode nicht unbedingt gebraucht.
 * Seit controlmenu weg ist aber ganz praktisch.
 * (E) run tests. internal.
 * (CursorTasten) für View Left/Right/Up/Down, ohne vorher F drücken zu müssen. Geht über ObserverSystem.
 * (X/Y/Z) für tuning der Avatarposition
 * <p>
 * Folgende KEys sind da bewusst nicht bei: ,
 * <p>
 * Start Sequence:
 * - Splash screen und "Loading" (transparent) mit progress. In the background scenery isType loading.
 * - Vehicle isType loaded by argument "initialVehicle".
 * - When vehicle isType available fadeout, teleport, fadein.
 * - "Click for Start" Button. Das ist dann quasi der 's' Key.
 * <p>
 * <p>
 * <p>
 * <p>
 * 4.10.21: MA37: Renamed TrafficCommon->BasicTravelScene und nicht mehr abstract. Auch standalone fuer tiles ohne FG.
 * Created on 28.09.18.
 */
public class BasicTravelScene extends Scene implements RequestHandler {
    //Die instance loggt über getLog()
    static Log logger = Platform.getInstance().getLog(BasicTravelScene.class);
    // The default TravelScene has no hud.
    protected Hud hud;
    protected boolean visualizeTrack = false;
    protected boolean enableFPC = false, enableNearView = false;
    protected SceneConfig sceneConfig;
    //Manche Requests können wegen zu spezieller Abhängigkeiten nicht von ECS bearbeitet werden, sondern müssen aber auf App Ebene laufen.
    public RequestQueue requestQueue = new RequestQueue();

    // nur bei Flat, in 3D null! 7.10.21 static bis es wegkommt
    //25.9.20 protected DefaultTrafficWorld trafficWorld;
    //22.10.21 wird noch einmal gebraucht
    protected TeleporterSystem teleporterSystem;
    RandomIntProvider rand = new RandomIntProvider();
    int nextlocationindex = 0;
    protected String vehiclelistname = "GroundServices";
    /*31.1.22 RequestType REQUEST_RESET = new RequestType("Reset");
    RequestType REQUEST_HELP = new RequestType("Help");
    RequestType REQUEST_MENU = new RequestType("Menu");
    RequestType REQUEST_CYCLE = new RequestType("Cycle");
    RequestType REQUEST_LOAD = new RequestType("Load");
    RequestType REQUEST_PLAY = new RequestType("Play");
    RequestType REQUEST_PLUS = new RequestType("+");
    RequestType REQUEST_MINUS = new RequestType("-");*/
    protected NearView nearView = null;
    // 17.10.21: per argument oder default EDDK
    String tilename = null;
    VrInstance vrInstance;
    Map<String, ButtonDelegate> buttonDelegates = new HashMap<String, ButtonDelegate>();
    ControlPanel leftControllerPanel = null;

    @Override
    public void init(boolean forServer) {
        logger.debug("init BasicTravelScene");
        processArguments();

        vrInstance = VrInstance.buildFromArguments();

        SystemManager.addSystem(new TrafficSystem(getVehicleLoader()));

        SystemManager.addSystem(new SphereSystem(getRbcp(), getGraphBackProjectionProvider(), getCenter(), getSceneConfig()));
        ((SphereSystem) SystemManager.findSystem(SphereSystem.TAG)).setDefaultLightDefinition(getLight());
        SystemManager.addSystem(new GraphMovingSystem());
        SystemManager.addSystem(new GraphTerrainSystem(getTerrainBuilder()));
        //SystemManager.addSystem(new GroundServicesSystem());
        if (enableFPC) {
        } else {
            // Verhalten aus FltaTravelScene. Wenn FPC, dann keine Teleport. TravelScene hatte dafuer freecam?
            teleporterSystem = new TeleporterSystem();
            //anim ist zu ruckelig/fehlerhaft
            teleporterSystem.setAnimated(false);
            SystemManager.addSystem(teleporterSystem);

            SystemManager.addSystem(new ObserverSystem(), 0);
        }
        SystemManager.addSystem(new UserSystem());
        SystemManager.addSystem(new AvatarSystem(!forServer), 0);

        //visualizeTrack soll auch im usermode verfuegbar sein.
        /*if (visualizeTrack) {
            SystemManager.addSystem(new GraphVisualizationSystem(new SimpleGraphVisualizer(world)));
        }*/
        for (EcsSystem ecsSystem : getCustomTerrainSystems()) {
            SystemManager.addSystem(ecsSystem);
        }

        // Observer concept: Den Observer kann es direkt noch vor login/join geben. Er zeigt dann z.B. einen Overview.
        // Bei login/join kann er dann an den Avatar? Auch für VR? (MA35) Oder nie? Oder unabhaengig/doppelt?
        // server has no camera.
        if (!forServer) {
            Observer.buildForDefaultCamera();
        }

        commoninit();

        customInit();
        postInit();
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
       /*21.3.19  String argv_enableusermode = ((Platform) Platform.getInstance()).getSystemProperty("argv.enableUsermode");
        //argv_enableusermode="1";
        if (!Util.isFalse(argv_enableusermode)) {
            usermode = new Usermode();
        }*/


        String argv_visualizeTrack = (Platform.getInstance()).getSystemProperty("argv.visualizeTrack");
        if (Util.isTrue(argv_visualizeTrack)) {
            visualizeTrack = true;
        }

        Boolean b;
        if ((b = PlatformHelper.getBooleanSystemProperty("argv.enableFPC")) != null) {
            //FPC heisst: kein Teleporting, kein Observer, kein initialVehicle,kein Avatar.
            enableFPC = (boolean) b;
        }

        String argv_initialVehicle = (Platform.getInstance()).getSystemProperty("argv.initialVehicle");
        if (argv_initialVehicle != null) {
            if (enableFPC) {
                logger.info("Ignoring initialVehicle due to FPC");
            } else {
                Request request;
                request = new Request(RequestRegistry.TRAFFIC_REQUEST_LOADVEHICLE, new Payload(argv_initialVehicle));
                requestQueue.addRequest(request);
            }
        }

        String argv_vehiclelistname = (Platform.getInstance()).getSystemProperty("argv.vehiclelist");
        if (argv_vehiclelistname != null) {
            vehiclelistname = argv_vehiclelistname;
        }

        if ((b = PlatformHelper.getBooleanSystemProperty("argv.enableNearView")) != null) {
            enableNearView = (boolean) b;
        }

        // Parameter basename gibt es eigentlich nur in 2D. 7.10.21: Aber das wird hier jetzt einach mal als Request sent,
        // wenns nicht relevant oder ungueltig ist, verfaellt es halt. Und ich fuehre auch wieder den Deault EDDK ein.

        tilename = Platform.getInstance().getSystemProperty("argv.basename");

        if (tilename == null) {
            tilename = getDefaultTilename();
        }
        logger.debug("using tilename " + tilename);
    }

    /**
     * To be overridden by extending class.
     */
    public String getDefaultTilename() {
        // null leads to 3D
        return null;
    }

    protected void initHud() {
        //5.10.18: Hud braucht viel Speicher (und damit auch (GC) CPU). Darum optional. Aber per default an.
        //31.3.20: Seit dem Einbau einer deferred Cam bzw. nearview scheint das nicht mehr zu gehen. Offenbar muss man jetzt selber den attach machen.
        String argv_enableHud = (Platform.getInstance()).getSystemProperty("argv.enableHud");
        if (!Util.isFalse(argv_enableHud)) {
            hud = Hud.buildForCamera(getDefaultCamera(), 0);
            getDefaultCamera().getCarrier().attach(hud);
        }
    }

    protected void commoninit() {

        buttonDelegates.put("reset", () -> {
            logger.info("reset");
        });
        buttonDelegates.put("info", () -> {
            VrInstance.getInstance().dumpDebugInfo();
            Observer.getInstance().dumpDebugInfo();
        });
        buttonDelegates.put("up", () -> {
            logger.info("up");
            Observer.getInstance().fineTune(true);
        });
        buttonDelegates.put("down", () -> {
            logger.info("down");
            Observer.getInstance().fineTune(false);
        });
        buttonDelegates.put("speedup", () -> {
        });
        buttonDelegates.put("speeddown", () -> {
        });
        buttonDelegates.put("teleport", () -> {
            IntHolder option = new IntHolder(0);
            Request request = new Request(UserSystem.USER_REQUEST_TELEPORT, new Payload(option));
            SystemManager.putRequest(request);
        });

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = getProcessPolicy();

        InputToRequestSystem inputToRequestSystem = new InputToRequestSystem(new DefaultMenuProvider(getDefaultCamera(), () -> {
            /**
             * 18.2.22 Locate menu at near plane like it was when FovElement was used. That should help to avoid menu coverage by cockpits.
             */
            double width = 0.07;
            double zpos = -getDefaultCamera().getNear() - 0.001;
            double buttonzpos = 0.0001;

            ControlPanel m = ControlPanelHelper.buildSingleColumnFromMenuitems(new DimensionF(width, width * 0.7), zpos, buttonzpos, /*sc.*/getMenuItems(), Color.GREEN);
            ControlPanelMenu menu = new ControlPanelMenu(m);
            return menu;

        }));
        inputToRequestSystem.addKeyMapping(KeyCode.M, InputToRequestSystem.USER_REQUEST_MENU);
        //toggle auto move
        inputToRequestSystem.addKeyMapping(KeyCode.Alpha9, UserSystem.USER_REQUEST_AUTOMOVE);

        if (vrInstance != null) {
            // Even in VR the observer will be attached to avatar later
            // Observer was inited before
            Observer observer = Observer.getInstance();
            // 1.4.22 Probably still the need to change yoffsetvr here as long as green box avatar is used?
            observer.initFineTune(vrInstance.getOffsetVR());
            observer.attach(vrInstance.getController(0));
            observer.attach(vrInstance.getController(1));

        } else {
            // nothing special (menu,hud,controlpanel) for non VR?
        }


        SystemManager.addSystem(inputToRequestSystem, 0);
        //6.10.21 buildToggleMenu();
        //menuCycler = new MenuCycler(new MenuProvider[]{new TravelMainMenuBuilder(this)});

        requestQueue.addHandler(RequestRegistry.TRAFFIC_REQUEST_LOADVEHICLE, this);
        //Geht per FlightSystem requestQueue.addHandler(RequestRegistry.TRAFFIC_REQUEST_AIRCRAFTDEPARTING, this);


    }

    public ProcessPolicy getProcessPolicy() {
        return null;
    }

    public MenuItem[] getMenuItems() {
        return new MenuItem[]{
                new MenuItem("teleport", () -> {
                    IntHolder option = new IntHolder(0);
                    Request request = new Request(UserSystem.USER_REQUEST_TELEPORT, new Payload(option));
                    SystemManager.putRequest(request);
                })
        };
    }

    public VehicleLoader getVehicleLoader() {
        return new SimpleVehicleLoader();
    }

    public AbstractTerrainBuilder getTerrainBuilder() {
        return null;
    }


    /**
     * Should be null as default? List could come from a tile?
     * For now "lok" like in OsmScenery.
     * <p>
     * 28.10.21
     *
     * @return
     */
    public List<Vehicle> getVehicleList() {
        List<Vehicle> vehicleList = new ArrayList<Vehicle>();
        //15.12.21 vehicleList.add(new Vehicle("loc", false,true));
        return vehicleList;//null;
    }

    public ConfigNodeList getLocationList() {
        return null;
    }

    @Override
    public String[] getPreInitBundle() {
        return new String[]{"engine", "data", "traffic"};
    }

    private void postInit() {
        //27.10.21 War sonst früher
        //8.12.21 addLight();

        SystemManager.addSystem(new InputToRequestSystem());

        //29.10.21: Damit lauchVehicles noch geht. TODO anders.
        TrafficSystem.sceneConfig = sceneConfig;
        if (sceneConfig != null) {
            TrafficSystem.baseTransformForVehicleOnGraph = sceneConfig.getBaseTransformForVehicleOnGraph();
        } else {
            //8.11.21: position part apparently ignored
            TrafficSystem.baseTransformForVehicleOnGraph = new LocalTransform(new Vector3(0, 0, 0), Quaternion.buildFromAngles(new Degree(0), new Degree(-90), new Degree(0)));
        }

        //erst jetzt, wenn ECS schon inited ist? Nee, muesste auch schon vorher gehen. Ist aber auch egal. Obwohl die Darstellung richtiger ist
        //als wenn man es im commoninit macht.
        initSpheres();

        SystemManager.putDataProvider("vehicleconfig", getVehicleConfigDataProvider());

        //7.10.21:Jetzt hier statt als erstes im Update. Aber fuer die Initialposition muss man das tile kennen. Darum als erstes Tile per
        //Sphere laden. Von da wird dann das alte "sendInitialEvents" gemacht.
        SystemManager.putRequest(new Request(SphereSystem.USER_REQUEST_SPHERE, new Payload(tilename/*17.10.21 TrafficWorld2D.basename*/, getVehicleList())));

        // Avatar anlegen (via login)
        SystemManager.putRequest(UserSystem.buildLoginRequest("Freds account name", ""));

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

    public void customUpdate(double tpf) {
    }

    protected void commonUpdate() {


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
        if (Input.GetKeyDown(KeyCode.L)) {
            //20.3.19: Ueber AppRequest loadNextConfiguredVehicle(null);
            Request request;
            request = new Request(RequestRegistry.TRAFFIC_REQUEST_LOADVEHICLE);
            requestQueue.addRequest(request);
        }

        /*24.10.21if (avatar != null) {
            //Util.nomore();
            //11.5.21avatar.update();
        }*/

        if (Input.GetKeyDown(KeyCode.H)) {
            // 11.10.18: hier auch die Statistik ausgeben
            //MA36 Statistics statistics = ( Platform.getInstance()).getStatistics();
            //MA36 getLog().info(statistics.geometries + " geometries, " + statistics.vertices + " vertices, " + statistics.textures + " textures," + statistics.calcGeometryMBs() + " MBs for geos total");
            help();
        }

        if (Input.GetKeyDown(KeyCode.R)) {
            report();
        }
        //26.10.18: 'T' ist jetzt teleport. 23.10.21: Einheitlich wie sonst 'V' statt 'E'
        if (Input.GetKeyDown(KeyCode.V)) {
            runTests();
        }

        requestQueue.process();

        //for x/y/z. Only in debug mode (MazeSettings.getSettings().debug)? Better location??
        if (Observer.getInstance() != null) {
            Observer.getInstance().update();
        }

        // VR controller might not be available until the user enters VR. So attach the controller the first time
        // they are found.
        if (vrInstance != null && vrInstance.getController(0) != null && leftControllerPanel == null) {

            leftControllerPanel = new TrafficVrControlPanel(buttonDelegates);

            vrInstance.attachControlPanelToController(vrInstance.getController(0), leftControllerPanel);

            InputToRequestSystem inputToRequestSystem = (InputToRequestSystem) SystemManager.findSystem(InputToRequestSystem.TAG);
            inputToRequestSystem.addControlPanel(leftControllerPanel);
        }
    }

    @Override
    public boolean processRequest(Request request) {
        if (request.getType().equals(RequestRegistry.TRAFFIC_REQUEST_LOADVEHICLE)) {
            // Nicht zu frueh laden, bevor es einen Graph zur Positionierung gibt.
            // 26.3.20 Erstmal pruefen, wo das Vehicle hinsoll und dann evtl. das Groundnet vorab per Request laden.
            //if (getGroundNet() == null) {
            //  return false;
            //}
            // Zugriff auf Asynchelper ist nicht ganz sauber, but make sure alle scenery isType loaded before placing a vehicle.
            if (AsyncHelper.getModelbuildvaluesSize() > 0) {
                return false;
            }
            String vehiclename = (String) request.getPayloadByIndex(0);
            SmartLocation smartLocation = (SmartLocation) request.getPayloadByIndex(1);
            if (smartLocation == null) {
                // 26.3.20 Was ware denn die naechste Location? Das ist ja jetzt alles EDDK lastig. TODO
                String icao = "EDDK";
                //27.12.21  DefaultTrafficWorld.getInstance().getConfiguration().getLocationListByName(icao).get(nextlocationindex);
                ConfigNode location = getLocationList()/*ByName(icao)*/.get(nextlocationindex);
                ;
                smartLocation = new SmartLocation(icao, XmlHelper.getStringValue(location.nativeNode));

                // 27.21.21 das ist jetzt schwierig zu pruefen. Es ist auch unklar, ob es wirklich noch noetig ist. Mal weglassen.
                /*if (DefaultTrafficWorld.getInstance().getGroundNetGraph(icao) == null) {
                    SystemManager.putRequest(new Request(RequestRegistry.TRAFFIC_REQUEST_LOADGROUNDNET, new Payload(icao)));
                    // warten und nochmal versuchen
                    return false;
                }*/
                nextlocationindex++;
            }
            /**
             * load eines Vehicle, z.B. per TRAFFIC_REQUEST_LOADVEHICLE. 24.11.20: Dafuer ist jetzt TRAFFIC_REQUEST_LOADVEHICLE2.
             * Das muss nicht unbedingt fuer Travel mit Avatar geeignet sein. Obs das ist, ist abhaengig vom Vehicle.
             * Wird erst auf Anforderung gemacht, weil
             * ein Vehicle viele Resourcen braucht und abhaengig von delayedload in der config (mit "initialVehicle" aber automatisch nach kurzer Zeit).
             * Geht nicht mehr ueber Index, sondern prüft was das nächste ist bzw. welces noch nicht geladen wurde
             * Es wird auch nicht unbedingt das naechste, sondern einfach das uebergebene geladen.
             * 26.3.20
             */
            String name = vehiclename;

            //DefaultTrafficWorld.getInstance().vehiclelist
            /*ConfigNodeList*/
            List<Vehicle> vehiclelist = TrafficSystem.vehiclelist;

            if (name == null) {
                for (int i = 0; i < vehiclelist.size(); i++) {
                    //SceneVehicle sv = sceneConfig.getVehicle(i);
                    if (findVehicleByName(vehiclelist.get(i).getName()) == null) {
                        name = vehiclelist.get(i).getName();
                        getLog().debug("found unloaded vehicle " + name);
                        break;
                    }
                }
            }
            if (name == null) {
                getLog().error("no unloaded vehicle found");
                //set request to done
                return true;
            }

            SphereProjections sphereProjections = TrafficHelper.getProjectionByDataprovider();
            //lauch c172p (or 777)
            //TrafficSystem.loadVehicles(tw, avataraircraftindex);

            VehicleConfig config = TrafficHelper.getVehicleConfigByDataprovider(name);// tw.getVehicleConfig(name);
            EcsEntity avatar = UserSystem.getInitialUser();//AvatarSystem.getAvatar().avatarE;
            VehicleLauncher.lauchVehicleByName(getGroundNet(), config/*27.12.21DefaultTrafficWorld.getInstance().getConfiguration()*/, name, smartLocation, TeleportComponent.getTeleportComponent(avatar),
                    getDestinationNode(), sphereProjections.backProjection/*getBackProjection()*/, sceneConfig, nearView, TrafficSystem.genericVehicleBuiltDelegate, getVehicleLoader());
            //aus flight: GroundServicesScene.lauchVehicleByIndex(gsw.groundnet, tw, 2, TeleportComponent.getTeleportComponent(avatar.avatarE), world, gsw.groundnet.projection);


            return true;
        }

        return false;
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
            logger.debug("name=" + e.getName() + lockinfo);
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
            logger.warn("no target entity in TC");
            return null;
        }
        return findVehicleByName(name);

    }

    public static EcsEntity findVehicleByName(String name) {
        List<EcsEntity> aircrafts = EcsHelper.findEntitiesByComponent(VehicleComponent.TAG);
        EcsEntity found = null;
        int i;
        for (i = 0; i < aircrafts.size(); i++) {
            if (aircrafts.get(i).getName().equals(name/**/)) {
                if (found != null) {
                    //besser erstmal abbrechen. Das gibt sonst schwer erkennbare Fehlfunktionen
                    throw new RuntimeException("duplicate " + name);
                }
                found = aircrafts.get(i);
            }
        }
        return found;
    }

    /**
     * Der Default
     *
     * @return
     */
    public VehicleConfigDataProvider getVehicleConfigDataProvider() {
        return new VehicleConfigDataProvider(null);
    }

    protected void reset() {
        getLog().debug("reset");
    }

    // 7.10.21 Die 8 naechsten waren mal abstract
    protected TrafficGraph/*8.5.19 GroundNet*/ getGroundNet() {
        return null;
    }

    /*29.10.21 protected GraphProjectionFlight3D getBackProjection() {
        return null;
    }*/

    protected SceneNode getDestinationNode() {
        return null;
    }

    protected void help() {
    }

    protected Log getLog() {
        return null;
    }

    protected void initSpheres() {
    }

    protected void runTests() {
    }

    public RoundBodyCalculations getRbcp() {
        return null;
    }

    public GraphProjection getBackProjection() {
        return null;
    }

    @Override
    public void initSettings(Settings settings) {
        settings.vrready = true;
    }
}