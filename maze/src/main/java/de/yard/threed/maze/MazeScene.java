package de.yard.threed.maze;


import de.yard.threed.core.*;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.*;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.avatar.AvatarABuilder;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.gui.ButtonDelegate;
import de.yard.threed.engine.gui.ControlPanel;
import de.yard.threed.engine.util.IntProvider;
import de.yard.threed.engine.util.RandomIntProvider;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.common.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Scene for all maze games.
 * <p>
 * Created on 22.10.18.
 */
public class MazeScene extends Scene {
    static Log logger = Platform.getInstance().getLog(MazeScene.class);
    Light light;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    //10.11.20 replaced by loaded event boolean loadcompleted = false;
    //10.11.20 replaced by loaded event boolean gamestarted = false;

    static IntProvider rand = new RandomIntProvider();
    static int HUDLAYER = 9;
    // in VR 0, sonst die übliche bekannte Höhe. Ohne VR war das immer 0.6 unter diesem Namen
    //Ray Oberkante zum Test genau auf Pillaroberkante mit rayy = Pillar.HEIGHT - 0.15f
    public static double rayy = 0.6f;
    Map<String, ButtonDelegate> buttonDelegates = new HashMap<String, ButtonDelegate>();
    static VrInstance vrInstance;

    public MazeScene() {
    }

    @Override
    public void init(SceneMode sceneMode) {
        logger.info("init MazeScene");

        // command line arguments are handled in system builder
        Configuration configuration = Configuration.getDefaultConfiguration();
        configuration.addConfiguration(new ConfigurationByProperties(new BundleResource(BundleRegistry.getBundle("maze"), "maze.properties")), true);

        boolean isMP = false;
        if (isMP) {
            //so gehts nicht! init läuft noch
            //Platform.getInstance().
        } else {

        }


        if (false) {
            //secondray = new Player(Color.GREEN);
            //add(secondray);
        }

        // Den Observer kann es direkt noch vor login/join geben. Er zeigt dann z.B. einen Overview.
        // Bei login/join kann er dann an den Avatar? Auch für VR? (MA35) Oder nie? Oder unabhaengig/doppelt?
        Observer observer = Observer.buildForDefaultCamera();

        buttonDelegates.put("reset", () -> {
            logger.info("reset");
        });
        buttonDelegates.put("info", () -> {
            logger.info("cam vr pos=" + getDefaultCamera().getVrPosition(true));
            logger.info("cam carrier pos=" + getDefaultCamera().getCarrierPosition());
            logger.info("cam carrier parent=" + getDefaultCamera().getCarrier().getTransform().getParent());
            logger.info("observer pos, finetune=" + observer.getPosition() + "," + observer.getFinetune());
            logger.info("world pos=" + Scene.getWorld().getTransform().getPosition());

            //avatar.dumpDebugInfo();
        });
        buttonDelegates.put("mainmenu", () -> {
            //menuCycler.cycle();
        });
        buttonDelegates.put("up", () -> {
            logger.info("up");
            /*avatar*/
            Observer.getInstance().fineTune(true);
            //??vrInstance.increaseOffset(0.1);
        });
        buttonDelegates.put("down", () -> {
            logger.info("down");
            /*avatar*/
            Observer.getInstance().fineTune(false);

        });
        buttonDelegates.put("pull", () -> {
            InputToRequestSystem.sendRequestWithId(new Request(RequestRegistry.TRIGGER_REQUEST_PULL, new Payload(new Object[]{""})));
        });

        vrInstance = VrInstance.buildFromArguments();

        MazeMovingAndStateSystem movingsystem = MazeMovingAndStateSystem.buildFromArguments();
        SystemManager.addSystem(movingsystem);

        SystemManager.addSystem(new UserSystem());
        AvatarSystem avatarSystem = AvatarSystem.buildFromArguments();
        avatarSystem.setAvatarBuilder(new MazeAvatarBuilder());
        avatarSystem.setViewTransform(getViewTransform());
        SystemManager.addSystem(avatarSystem);

        InputToRequestSystem inputToRequestSystem = null;
        if (sceneMode.isClient()) {
            SystemManager.addSystem(new MazeVisualizationSystem());
            //16.4.21: Kein main menu mehr. Level change geht einfach über Neustart. Dafuer das control menu togglen.
            inputToRequestSystem = new InputToRequestSystem(/*new MainMenu(getMainCamera())*/);
            //'M' nur Provisorium? Och wieso? Man kann KEys immer als Fallback haben.
            //inputToRequestSystem.addKeyMapping(KeyCode.M, InputToRequestSystem.USER_REQUEST_MENU);
            inputToRequestSystem.addKeyMapping(KeyCode.M, InputToRequestSystem.USER_REQUEST_CONTROLMENU);
            inputToRequestSystem.addKeyMapping(KeyCode.W, RequestRegistry.TRIGGER_REQUEST_FORWARD);
            inputToRequestSystem.addKeyMapping(KeyCode.UpArrow, RequestRegistry.TRIGGER_REQUEST_FORWARD);

            inputToRequestSystem.addKeyMapping(KeyCode.S, RequestRegistry.TRIGGER_REQUEST_BACK);
            inputToRequestSystem.addKeyMapping(KeyCode.DownArrow, RequestRegistry.TRIGGER_REQUEST_BACK);

            inputToRequestSystem.addKeyMapping(KeyCode.LeftArrow, RequestRegistry.TRIGGER_REQUEST_TURNLEFT);
            inputToRequestSystem.addKeyMapping(KeyCode.RightArrow, RequestRegistry.TRIGGER_REQUEST_TURNRIGHT);

            inputToRequestSystem.addKeyMapping(KeyCode.U, RequestRegistry.TRIGGER_REQUEST_UNDO);
            inputToRequestSystem.addKeyMapping(KeyCode.V, RequestRegistry.TRIGGER_REQUEST_VALIDATE);
            inputToRequestSystem.addKeyMapping(KeyCode.H, RequestRegistry.TRIGGER_REQUEST_HELP);
            inputToRequestSystem.addKeyMapping(KeyCode.R, RequestRegistry.TRIGGER_REQUEST_RESET);
            inputToRequestSystem.addKeyMapping(KeyCode.K, RequestRegistry.TRIGGER_REQUEST_KICK);
            inputToRequestSystem.addKeyMapping(KeyCode.Space, BulletSystem.TRIGGER_REQUEST_FIRE);

            inputToRequestSystem.setSegmentRequest(0, RequestRegistry.TRIGGER_REQUEST_LEFT);
            inputToRequestSystem.setSegmentRequest(2, RequestRegistry.TRIGGER_REQUEST_RIGHT);
            inputToRequestSystem.setSegmentRequest(3, RequestRegistry.TRIGGER_REQUEST_TURNLEFT);
            inputToRequestSystem.setSegmentRequest(4, BulletSystem.TRIGGER_REQUEST_FIRE);
            inputToRequestSystem.setSegmentRequest(5, RequestRegistry.TRIGGER_REQUEST_TURNRIGHT);
            inputToRequestSystem.setSegmentRequest(7, RequestRegistry.TRIGGER_REQUEST_FORWARD);
            SystemManager.addSystem(inputToRequestSystem);
        }

        if (vrInstance != null) {
            // Even in VR the observer will be attached to avatar later
            // 1.4.22: yoffsetvr now needs to be raised here for maze specificcally. And move a bit forward to avoid seeing own avatar.(-0.15 or -0.25 not sufficient)
            observer.initFineTune(vrInstance.getOffsetVR().add(new Vector3(0, 0.6, -0.4)));
            observer.attach(vrInstance.getController(0));
            observer.attach(vrInstance.getController(1));
            rayy = 0;

            MazeVrControlPanel leftControllerPanel = new MazeVrControlPanel(buttonDelegates);
            /*LocalTransform lt = vrInstance.getCpTransform();
            if (lt != null) {
                //leftControllerPanel.getTransform().setPosition(new Vector3(-0.5, 1.5, -2.5));
                //200,90,0 are good rotations
                leftControllerPanel.getTransform().setPosition(lt.position);
                leftControllerPanel.getTransform().setRotation(lt.rotation);
                leftControllerPanel.getTransform().setScale(new Vector3(0.4, 0.4, 0.4));
            }
            vrInstance.getController(0).attach(leftControllerPanel);*/
            inputToRequestSystem.addControlPanel(leftControllerPanel);
            vrInstance.attachControlPanelToController(vrInstance.getController(0), leftControllerPanel);//.attachControlPanel(leftControllerPanel);

            InventorySystem inventorySystem = new InventorySystem();
            inventorySystem.addInventory(leftControllerPanel);
            SystemManager.addSystem(inventorySystem);
        } else {
            // regular display, non VR
            InventorySystem inventorySystem = new InventorySystem();
            SystemManager.addSystem(inventorySystem);

            if (sceneMode.isClient()) {
                inputToRequestSystem.setControlMenuBuilder(new ControlMenu());
                Camera deferredcameraForInventory = Camera.createAttachedDeferredCamera(getMainCamera(), HUDLAYER, 1.0, 10.0);
                deferredcameraForInventory.setName("deferred-camera");
                inventorySystem.addInventory(new MazeHudInventory(deferredcameraForInventory, getDimension()));

                // Optional (test)Hud that shows VR control panel via deferred camera as HUD
                if (EngineHelper.isEnabled("argv.enableHud")) {
                    ControlPanel leftControllerPanel = new MazeVrControlPanel(buttonDelegates);
                    leftControllerPanel.getTransform().setPosition(new Vector3(0.4, 0.8, -2));
                    deferredcameraForInventory.getCarrier().attach(leftControllerPanel);
                    inputToRequestSystem.addControlPanel(leftControllerPanel);
                }
            }
        }

        SystemManager.addSystem(new BulletSystem());
        SystemManager.addSystem(new BotSystem());

        addLight();
        //31.10.20 backendAdapter=new MazeLocalBackendAdapter();

        if (sceneMode.isServer() && sceneMode.isClient()) {
            // standalone. Handle like a client that connected.
            backendConnected();
        }
    }

    @Override
    public void backendConnected() {
        // last init statement. Queue login request for main user
        SystemManager.putRequest(UserSystem.buildLoginRequest("", ""));
    }

    @Override
    public void initSettings(Settings settings) {
        //30 fits for Unity quite well.
        settings.targetframerate = 30;
        settings.aasamples = 4;
        settings.fov = 60f;
        settings.vrready = true;
    }

    @Override
    public String[] getPreInitBundle() {
        return new String[]{"engine", "data", "maze"};
    }

    /**
     * Portrait wegen Smartphone. Ist aber nur für Dev interessant. Ansonsten soll das Gerät oder Nutzer
     * die Größe festlegen.
     *
     * @return
     */
    @Override
    public Dimension getPreferredDimension() {
        if (((Platform) Platform.getInstance()).isDevmode()) {
            return new Dimension(500, 700);
        }
        return null;
    }

    /**
     * 22.3.17: Mit ambient greift die Normalmap nicht. Ist aber alles irgendwie nicht das wahre.
     * Mit zwei "gegenüber liegenden" DirLights ist die Beleuchtung zumindest in ThreeJs ganz gut.
     * PointLight lass ich mal ganz weg.
     */
    private void addLight() {
        if (MazeSettings.getSettings().ambilight) {
            AmbientLight light = new AmbientLight(Color.WHITE);

            //pointLight.setPosition(new Vector3(0, 2, -5.5f));
            addLightToWorld(light);

        } else {
            // create a point light
            //PointLight pointLight = new PointLight(Color.WHITE);
            //pointLight.setPosition(new Vector3(0, 2, 1.5f));
            //addLightToWorld(pointLight);

            // gerade zum Testen brauche ich aber noch ein Hintergrundlicht fuer komplette Ausleuchtung
            //Ambient scheint aber zumidest bei JME nicht so gut geeignet.
            // auf directive umgestellt. Das ist gut.
            addLightToWorld(new DirectionalLight(Color.WHITE, new Vector3(2, 3, 2)));
            addLightToWorld(new DirectionalLight(Color.WHITE, new Vector3(-2, 3, -2)));

            // und noch ein Licht, sonst sind manche Walls schwarz
            //pointLight = new PointLight(Color.WHITE);
            //pointLight.setPosition(new Vector3(10, 2, 1.5f));
            //addLightToWorld(pointLight);


            // und noch ein Licht, sonst sind manche Walls schwarz
            //pointLight = new PointLight(Color.WHITE);
            //pointLight.setPosition(new Vector3(-10, 2, 1.5f));
            //addLightToWorld(pointLight);

            // und noch ein Licht, sonst sind manche Walls schwarz
            //pointLight = new PointLight(Color.WHITE);
            //pointLight.setPosition(new Vector3(0, 2, -5.5f));
            //addLightToWorld(pointLight);
        }
    }

    /**
     * no update() needed.
     *
     * @return
     */
    @Override
    public void update() {
        //for x/y/z. Only in debug mode (MazeSettings.getSettings().debug)? Better location??
        if (Observer.getInstance() != null) {
            Observer.getInstance().update();
        }
    }

    /**
     * The preferred position/rotation of the oberver.
     *
     * @return
     */
    public static LocalTransform getViewTransform() {
        LocalTransform viewTransform = MazeSettings.getSettings().getViewpoint();
        viewTransform.position = viewTransform.position.add(new Vector3(0, MazeScene.rayy, 0));
        return viewTransform;
    }
}
