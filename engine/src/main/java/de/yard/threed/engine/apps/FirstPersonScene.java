package de.yard.threed.engine.apps;


import de.yard.threed.core.Color;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.AmbientLight;
import de.yard.threed.engine.BaseRequestRegistry;
import de.yard.threed.engine.DirectionalLight;
import de.yard.threed.engine.GenericGeometry;
import de.yard.threed.engine.Geometry;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.Light;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.ModelBuilderRegistry;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.ObserverSystem;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.ClientSystem;
import de.yard.threed.engine.ecs.FirstPersonMovingSystem;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.ecs.ServerSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.geometry.Primitives;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.engine.platform.common.SimpleGeometry;
import de.yard.threed.engine.vr.VrInstance;

/**
 * A general VR and client/server ready first person scene. Also a good template for a scene.
 * <p>
 * Ground in y=0 layer.
 * <p>
 * Created on 31.08.23.
 */
public class FirstPersonScene extends Scene {
    static Log logger = Platform.getInstance().getLog(FirstPersonScene.class);
    static VrInstance vrInstance;

    public FirstPersonScene() {
    }

    @Override
    public void init(SceneMode sceneMode) {
        logger.info("init FirstPersonScene");

        // command line arguments are handled in system builder
        Configuration configuration = Platform.getInstance().getConfiguration();

        // Observer can exist before login/join for showing eg. an overview.
        // After login/join it might be attched to an avatar.
        Observer observer = Observer.buildForDefaultCamera();

        vrInstance = VrInstance.buildFromArguments();

        InputToRequestSystem inputToRequestSystem = null;
        if (sceneMode.isClient()) {
            inputToRequestSystem = new InputToRequestSystem();
            inputToRequestSystem.addKeyMapping(KeyCode.M, InputToRequestSystem.USER_REQUEST_CONTROLMENU);
            inputToRequestSystem.addKeyMapping(KeyCode.W, BaseRequestRegistry.TRIGGER_REQUEST_FORWARD);
            inputToRequestSystem.addKeyMapping(KeyCode.UpArrow, BaseRequestRegistry.TRIGGER_REQUEST_FORWARD);

            inputToRequestSystem.addKeyMapping(KeyCode.S, BaseRequestRegistry.TRIGGER_REQUEST_BACK);
            inputToRequestSystem.addKeyMapping(KeyCode.DownArrow, BaseRequestRegistry.TRIGGER_REQUEST_BACK);

            inputToRequestSystem.addKeyMapping(KeyCode.LeftArrow, BaseRequestRegistry.TRIGGER_REQUEST_TURNLEFT);
            inputToRequestSystem.addKeyMapping(KeyCode.RightArrow, BaseRequestRegistry.TRIGGER_REQUEST_TURNRIGHT);


            inputToRequestSystem.setSegmentRequest(0, BaseRequestRegistry.TRIGGER_REQUEST_LEFT);
            inputToRequestSystem.setSegmentRequest(2, BaseRequestRegistry.TRIGGER_REQUEST_RIGHT);
            inputToRequestSystem.setSegmentRequest(3, BaseRequestRegistry.TRIGGER_REQUEST_TURNLEFT);
            inputToRequestSystem.setSegmentRequest(5, BaseRequestRegistry.TRIGGER_REQUEST_TURNRIGHT);
            inputToRequestSystem.setSegmentRequest(7, BaseRequestRegistry.TRIGGER_REQUEST_FORWARD);
            SystemManager.addSystem(inputToRequestSystem);

            ObserverSystem observerSystem = new ObserverSystem();
            //observerSystem.setViewTransform(getViewTransform(st));
            SystemManager.addSystem(observerSystem);
        }
        if (sceneMode.isServer()) {
            SystemManager.addSystem(new UserSystem());
            // AvatarSystem handles join. Use default avatar.
            AvatarSystem avatarSystem = AvatarSystem.buildFromArguments();
            SystemManager.addSystem(avatarSystem);
            FirstPersonMovingSystem firstPersonMovingSystem = FirstPersonMovingSystem.buildFromConfiguration();
            SystemManager.addSystem(firstPersonMovingSystem);
        }

        if (vrInstance != null) {
            // Even in VR the observer will be attached to avatar later
            //??observer.initFineTune(vrInstance.getOffsetVR().add(new Vector3(0, 0.6, -0.4)));
            observer.attach(vrInstance.getController(0));
            observer.attach(vrInstance.getController(1));
        }

        if (sceneMode.isServer() && !sceneMode.isClient()) {
            SystemManager.addSystem(ServerSystem.buildForInitialEventsForClient(new EventType[]{}));
        }
        if (!sceneMode.isServer() && sceneMode.isClient()) {
            SystemManager.addSystem(new ClientSystem(new ModelBuilderRegistry[]{}));
        }

        addLight();
        addGround();
        addCube();

        SystemState.state = SystemState.STATE_READY_TO_JOIN;

        // Send login request in both monolith and client mode
        if (sceneMode.isClient()) {
            // last init statement. Queue login request for main user
            SystemManager.putRequest(UserSystem.buildLoginRequest("", ""));
        }
    }

    private void addGround() {
        //No BasicMaterial because it has no shadowing.
        Material goundmat = Material.buildLambertMaterial(new Color(0.7f, 0.7f, 0.7f));
        SimpleGeometry groundGeo = Primitives.buildPlaneGeometry(16, 32, 1, 1);
        SceneNode ground = new SceneNode(new Mesh(new GenericGeometry(groundGeo), goundmat, false, true));
        ground.getTransform().setPosition(new Vector3(0, 0, 0));
        ground.setName("Ground");
        addToWorld(ground);
    }

    @Override
    public void initSettings(Settings settings) {
        settings.targetframerate = 30;
        settings.aasamples = 4;
        settings.fov = 60f;
        settings.vrready = true;
    }

    @Override
    public String[] getPreInitBundle() {
        return new String[]{"engine", "data"};
    }

    /**
     * Portrait for Smartphone. But only interesting for dev. Otherwise the user should decide.
     * 23.3.23 Seems to be quite nonsens.
     */
    @Override
    public Dimension getPreferredDimension() {
        /*if (((Platform) Platform.getInstance()).isDevmode()) {
            return new Dimension(500, 700);
        }*/
        return null;
    }

    @Override
    public void update() {
        //for x/y/z.
        if (Observer.getInstance() != null) {
            Observer.getInstance().update();
        }
    }

    /**
     *
     */
    private void addLight() {

        // AmbientLight has no effect on ground
        Light light = new AmbientLight(Color.WHITE);
        light = new DirectionalLight(new Color(0xee, 0xee, 0xee), new Vector3(0, 1, 1));
        addLightToWorld(light);
    }

    private void addCube() {
        double size = 0.5;
        Geometry cuboid = Geometry.buildCube(size, size, size);
        SceneNode cube = new SceneNode(new Mesh(cuboid, Material.buildBasicMaterial(Color.ORANGE)));
        cube.getTransform().setPosition(new Vector3(0, 0, 3));
        addToWorld(cube);

    }
}
