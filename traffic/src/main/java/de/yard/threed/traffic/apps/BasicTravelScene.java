package de.yard.threed.traffic.apps;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.*;
import de.yard.threed.engine.gui.*;
import de.yard.threed.engine.platform.ProcessPolicy;
import de.yard.threed.engine.util.XmlHelper;
import de.yard.threed.engine.vr.VrHelper;
import de.yard.threed.engine.platform.EngineHelper;
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
import java.util.List;

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
    //24.10.21 public Avatar avatar;
    //26.9.20 protected TrafficWorldConfig tw;
    protected Hud hud, helphud;
    protected boolean visualizeTrack = false;
    protected boolean enableFPC = false, enableNearView = false, enableLoweredAvatar = false;
    //6.10.21 protected MenuCycler menuCycler = null;
    //24.10.21 protected ObserverSystem viewingsystem;
    protected SceneConfig sceneConfig;
    //7.10.21 protected TrafficSystem trafficSystem;
    //7.10.21 protected AutomoveSystem automoveSystem;
    //27.10.21 protected FlightSystem flightSystem;
    //Manche Requests können wegen zu spezieller Abhängigkeiten nicht von ECS bearbeitet werden, sondern müssen aber auf App Ebene laufen.
    public RequestQueue requestQueue = new RequestQueue();

    // nur bei Flat, in 3D null! 7.10.21 static bis es wegkommt
    //25.9.20 protected DefaultTrafficWorld trafficWorld;
    //22.10.21 wird noch einmal gebraucht
    protected TeleporterSystem teleporterSystem;
    RandomIntProvider rand = new RandomIntProvider();
    int nextlocationindex = 0;
    protected String vehiclelistname = "GroundServices";
    RequestType REQUEST_RESET = new RequestType("Reset");
    RequestType REQUEST_HELP = new RequestType("Help");
    RequestType REQUEST_MENU = new RequestType("Menu");
    RequestType REQUEST_CYCLE = new RequestType("Cycle");
    RequestType REQUEST_LOAD = new RequestType("Load");
    RequestType REQUEST_PLAY = new RequestType("Play");
    RequestType REQUEST_PLUS = new RequestType("+");
    RequestType REQUEST_MINUS = new RequestType("-");
    protected NearView nearView = null;
    protected Double yoffsetVR;
    // 17.10.21: per argument oder default EDDK
    String tilename = null;


    @Override
    public void init(boolean forServer) {
        logger.debug("init BasicTravelScene");
        processArguments();

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
        // UserSystem lass ich erstmal weg
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
        //5.10.18: Hud braucht viel Speicher (und damit auch (GC) CPU). Darum optional. Aber per default an.
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
        if ((b = EngineHelper.getBooleanSystemProperty("argv.enableFPC")) != null) {
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

        if ((b = EngineHelper.getBooleanSystemProperty("argv.enableNearView")) != null) {
            enableNearView = (boolean) b;
        }
        if ((b = EngineHelper.getBooleanSystemProperty("argv.enableLoweredAvatar")) != null) {
            enableLoweredAvatar = (boolean) b;
        }
        yoffsetVR = EngineHelper.getDoubleSystemProperty("argv.yoffsetVR");

        // Parameter basename gibt es eigentlich nur in 2D. 7.10.21: Aber das wird hier jetzt einach mal als Request sent,
        // wenns nicht relevant oder ungueltig ist, verfaellt es halt. Und ich fuehre auch wieder den Deault EDDK ein.

        tilename = Platform.getInstance().getSystemProperty("argv.basename");
        /*boolean found = false;
        for (int i = 0; i < tilelist.length; i++) {
            if (tilelist[i].file.equals(tilename)) {
                major = i;
                found = true;
            }
        }
        if (tilename != null && !found) {
            logger.error("unknown tilename " + tilename);
        }*/
        if (tilename == null) {
            tilename = getDefaultTilename();
        }
        logger.debug("using tilename " + tilename);
        //erstmal nur per locationchangeSystemManager.putRequest(new Request(RequestRegistry.USER_REQUEST_TILE_LOAD,new Payload(tilename)));
    }

    public String getDefaultTilename() {
        //leads to 3D
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

        //AbstractSceneRunner.instance.httpClient = new AirportDataProviderMock();

        // Kruecke zur Entkopplung des Modelload von AC policy.
        ModelLoader.processPolicy = getProcessPolicy();

        //9.11.21 TrafficHelper.vehicleHelperDecoupler = new VehicleLauncher();
        //10.11.21 TrafficHelper.vehicleLoader = new FgVehicleLoader();

        InputToRequestSystem inputToRequestSystem = new InputToRequestSystem(new TravelMainMenuBuilder(this, getMenuItems()));
        inputToRequestSystem.addKeyMapping(KeyCode.M, InputToRequestSystem.USER_REQUEST_MENU);
        //toggle auto move
        inputToRequestSystem.addKeyMapping(KeyCode.Alpha9, UserSystem.USER_REQUEST_AUTOMOVE);
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
        return new MenuItem[]{};
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
        //1.4.21 de.yard.threed.engine.ecs.Player.init(avatar);

        /*24.10.21 if (yoffsetVR != null) {
            avatar.setBestPracticeRiftvryoffset((double) yoffsetVR);
        }*/
        /*if (enableLoweredAvatar) {
            avatar.lowerVR();
        }*/

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

        // Avatar anlegen (ohne login)
        SystemManager.putRequest(UserSystem.buildJOIN("", true));

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
            request = new Request(RequestRegistry.TRAFFIC_REQUEST_LOADVEHICLE, null);
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
            VehicleLauncher.lauchVehicleByName(getGroundNet(), config/*27.12.21DefaultTrafficWorld.getInstance().getConfiguration()*/, name, smartLocation, TeleportComponent.getTeleportComponent(/*24.10.21avatar*/AvatarSystem.getAvatar().avatarE),
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
        List<EcsEntity> vehicles = SystemManager.findEntities(new String[]{VehicleComponent.TAG});
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
        TeleportComponent tc = TeleportComponent.getTeleportComponent(/*24.10.21avatar*/AvatarSystem.getAvatar().avatarE);
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
        List<EcsEntity> aircrafts = SystemManager.findEntities(new VehicleFilter()/*AircraftFilter()*/);
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

    /*27.12.21protected GraphWorld getGraphWorld() {
        return null;
    }*/

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
}

class VehicleFilter implements EntityFilter {

    @Override
    public boolean matches(EcsEntity e) {
        VehicleComponent vc = VehicleComponent.getVehicleComponent(e);
        if (vc == null) {
            return false;
        }
        //return vc.config.getType().equals(VehicleComponent.VEHICLE_AIRCRAFT);
        return true;
    }
}

class TravelMainMenuBuilder implements MenuProvider {
    BasicTravelScene sc;
    MenuItem[] menuItems;

    TravelMainMenuBuilder(BasicTravelScene sc, MenuItem[] menuItems) {
        this.sc = sc;
        this.menuItems = menuItems;
    }

    /**
     * 3.1.20: In TravelScene kommt es wohl zu Verzerrungen, so dass die unteren nicht mehr richtig vom Ray getroffen werden.
     * Mit zpos=-8.4 und width*10 geht es wohl besser, in c172 ist das menu dann aber hinterm propeller.
     * Evtl. Bluebird statt c172p? Aber in TravelScene brauchts das menu doch eh noch nicht so, weils nicht in VR geht.
     *
     * @return
     */
    @Override
    public Menu buildMenu() {
        double width = 0.3;
        double zpos = -3;
        double buttonzpos = -0.4;

        //width=6;
        //buttonzpos=-9;

        //BrowseMenu m = new BrowseMenu(new DimensionF(width, width * 0.7), -3, -0.4, sc.menuitems);
        GuiGrid m = GuiGrid.buildSingleColumnFromMenuitems(new DimensionF(width, width * 0.7), zpos, buttonzpos, /*sc.*/menuItems);
        TravelMainMenu menu = new TravelMainMenu(m);
        return menu;
    }

    @Override
    public SceneNode getAttachNode() {
        return /*24.10.21sc.avatar*/AvatarSystem.getAvatar().getNode();//getFaceNode();
        //return sc.getDefaultCamera().getCarrier();
    }

    @Override
    public Ray getRayForUserClick(Point mouselocation) {
        if (mouselocation != null) {
            //Der Viewpoint kann bei Firefox 1.7m hoher sein als das Camera ausweist! Scheint hier trotzdem zu gehen.
            return sc.getDefaultCamera().buildPickingRay(sc.getDefaultCamera().getCarrierTransform(), mouselocation);
        }
        Ray ray = VrHelper.getController(1).getRay();
        return ray;
    }
}

/**
 * Ein Browsemenu, dass durch einen Button geöffnet/geschlossen wird.
 */
class TravelMainMenu extends GuiGridMenu {

    TravelMainMenu(GuiGrid menu) {
        super(menu);
    }


}
