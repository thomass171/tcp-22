package de.yard.threed.engine.apps.showroom;


import de.yard.threed.core.Color;
import de.yard.threed.core.DimensionF;
import de.yard.threed.core.Point;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.geometry.Primitives;
import de.yard.threed.core.geometry.ProportionalUvMap;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.AmbientLight;
import de.yard.threed.engine.Audio;
import de.yard.threed.engine.AudioClip;
import de.yard.threed.engine.DirectionalLight;
import de.yard.threed.engine.GenericGeometry;
import de.yard.threed.engine.Geometry;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.Light;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.ObserverSystem;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Texture;
import de.yard.threed.engine.apps.vr.VrSceneHelper;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.FirstPersonMovingComponent;
import de.yard.threed.engine.ecs.FirstPersonMovingSystem;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.gui.ButtonDelegate;
import de.yard.threed.engine.gui.ControlPanel;
import de.yard.threed.engine.gui.ControlPanelHelper;
import de.yard.threed.engine.gui.ControlPanelMenu;
import de.yard.threed.engine.gui.DefaultMenuProvider;
import de.yard.threed.engine.gui.Hud;
import de.yard.threed.engine.gui.Icon;
import de.yard.threed.engine.gui.Indicator;
import de.yard.threed.engine.gui.MenuCycler;
import de.yard.threed.engine.gui.MenuItem;
import de.yard.threed.engine.gui.MenuProvider;
import de.yard.threed.engine.gui.SpinnerControlPanel;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.engine.vr.VrInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A general VR ready ECS based scene. Also a good template for a scene.
 * Combines FirstPersonScene and VrScene and focuses on user interaction.
 * Has control panel at left controller in VR and a HUD with toggable menu/control menu otherwise.
 * <p>
 * The bar is at height 1m. Depending on the VR configuration (sitting/standing), the sitting view position is
 * * initially at the bars height.
 * * The default avatar (green cube) is at same height as the bar, also in VR (independent from vr y-offset).
 * Provides finetune with (shift) x/y/z.
 * <p>
 * Left controller teleports/moves (only?), right controller controls:
 * - scale down red cube (or mouse click). (scale up via menu/control menu)
 * - Via CP at left controller:
 * -- menu toggle
 * -- finetune up/down
 * -- info
 * -- indicator on/off
 * <p>
 * Ground in y=0 layer.
 * Doesn't use gridteleporter because that is too focused on a fix grid like in maze.
 * <p>
 * Created on 20.01.24.
 */
public class ShowroomScene extends Scene {
    static Log logger = Platform.getInstance().getLog(ShowroomScene.class);
    static VrInstance vrInstance;
    SceneNode bar, box1, ground, platform, secondBar, wall;
    String userName = "user";
    EcsEntity avatar;
    Map<String, ButtonDelegate> buttonDelegates = new HashMap<String, ButtonDelegate>();
    Hud hud;
    SceneNode markedmodel = null;
    private double MAINPLANE_SIZE = 25;
    private double WALLHEIGHT = 1.7;
    private boolean speedSet = false;
    private Audio elevatorPing;
    Color controlPanelBackground = new Color(128, 193, 255, 128);
    MenuItem[] menuitems;

    public ShowroomScene() {
    }

    @Override
    public void init(SceneMode sceneMode) {
        logger.info("init FirstPersonScene");

        menuitems = new MenuItem[]{
                new MenuItem("scale up", () -> {
                    logger.debug("scale up");
                    scale(box1, 1.1);
                }),
                new MenuItem("scale down", () -> {
                    logger.debug("scale down");
                    scale(box1, 0.9);
                }),
        };

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

        // Observer can exist before login/join for showing eg. an overview.
        // After login/join it might be attched to an avatar.
        Observer observer = Observer.buildForDefaultCamera();

        vrInstance = VrInstance.buildFromArguments();

        InputToRequestSystem inputToRequestSystem = new InputToRequestSystem();
        FirstPersonMovingSystem.addDefaultKeyBindings(inputToRequestSystem);
        SystemManager.addSystem(inputToRequestSystem);

        // ObserverSystem also needed in VR
        ObserverSystem observerSystem = new ObserverSystem();
        SystemManager.addSystem(observerSystem);

        SystemManager.addSystem(new UserSystem());
        // AvatarSystem handles join. Use default avatar.
        AvatarSystem avatarSystem = AvatarSystem.buildFromArguments();
        SystemManager.addSystem(avatarSystem);
        FirstPersonMovingSystem firstPersonMovingSystem = FirstPersonMovingSystem.buildFromConfiguration();
        SystemManager.addSystem(firstPersonMovingSystem);

        if (vrInstance != null) {
            // Even in VR the observer will be attached to avatar later
            observer.attach(vrInstance.getController(0));
            observer.attach(vrInstance.getController(1));

            ShowroomVrControlPanel leftControllerPanel = new ShowroomVrControlPanel(buttonDelegates);
            // position and rotation of VR controlpanel is controlled by property ...
            inputToRequestSystem.addControlPanel(leftControllerPanel);
            vrInstance.attachControlPanelToController(vrInstance.getController(0), leftControllerPanel);
        } else {
            inputToRequestSystem.setControlMenuBuilder(new ShowroomControlMenuBuilder(this));
            inputToRequestSystem.addKeyMapping(KeyCode.M, InputToRequestSystem.USER_REQUEST_MENU);
            inputToRequestSystem.setMenuProvider(new DefaultMenuProvider(getDefaultCamera(), () -> {
                // not too large to avoid overlapping with regular control panel in non VR for avoiding click conflicts.
                ControlPanel m = ControlPanelHelper.buildSingleColumnFromMenuitems(new DimensionF(1.3, 0.7), -3, 0.01, menuitems, Color.LIGHTBLUE);
                ControlPanelMenu menu = new ControlPanelMenu(m);
                return menu;
            }));

            initHud();
            updateHud();
        }

        addLight();
        //addCube();

        box1 = VrSceneHelper.buildRedBox();
        addToWorld(box1);

        bar = VrSceneHelper.buildBar();
        addToWorld(bar);

        ground = buildGround();
        addToWorld(ground);

        platform = VrSceneHelper.buildPlatform();
        addToWorld(platform);

        secondBar = VrSceneHelper.buildSecondBar();
        addToWorld(secondBar);

        wall = buildWall();
        addToWorld(secondBar);

        AudioClip elevatorPingClip = AudioClip.buildAudioClipFromBundle("data", "audio/elevator-ping-01.wav");
        elevatorPing = Audio.buildAudio(elevatorPingClip);
        if (elevatorPingClip != null && elevatorPing != null) {
            elevatorPing.setVolume(0.5);
            elevatorPing.setLooping(false);
        }

        // ControlPanel "at" wall.
        ControlPanel controlPanel = buildControlPanel(controlPanelBackground);
        controlPanel.getTransform().setPosition(new Vector3(-1, 0.8, -12.4));
        inputToRequestSystem.addControlPanel(controlPanel);
        addToWorld(controlPanel);

        SystemState.state = SystemState.STATE_READY_TO_JOIN;

        // last init statement. Queue login request for main user
        SystemManager.putRequest(UserSystem.buildLoginRequest(userName, ""));

        logger.debug("init Scene completed");
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

    @Override
    public void update() {
        //for x/y/z.
        if (Observer.getInstance() != null) {
            Observer.getInstance().update();
        }

        if (avatar == null) {
            // Get avatar and init position
            List<EcsEntity> candidates = SystemManager.findEntities(e -> userName.equals(e.getName()));
            if (candidates.size() > 0 && candidates.get(0).getSceneNode() != null) {
                avatar = candidates.get(0);
                avatar.getSceneNode().getTransform().setPosition(new Vector3(0, VrSceneHelper.BARYPOSITION, 0));


            }
        }

        if (!speedSet && avatar != null) {
            FirstPersonMovingComponent fpmc = FirstPersonMovingComponent.getFirstPersonMovingComponent(avatar);
            if (fpmc != null) {
                fpmc.getFirstPersonTransformer().setMovementSpeed(4.3);
                fpmc.getFirstPersonTransformer().setRotationSpeed(54.0);
                speedSet = true;
            }
        }
        if (Input.getKeyDown(KeyCode.Space)) {
            cycleModel();
            updateHud();
        }

        // scale down red box, either by mouse or by right VR controller
        Point mouseClickLocation = Input.getMouseUp();
        if (mouseClickLocation != null) {
            Ray ray = Observer.getInstance().buildPickingRay(getDefaultCamera(), mouseClickLocation);
            toggleRedBox(ray, false);
        }
        if (Input.getControllerButtonDown(10)) {
            logger.debug(" found button down 10 (VR controller trigger)");
            if (vrInstance.getController(1) != null) {
                toggleRedBox(vrInstance.getController(1).getRay(), false);
            }
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

    void initHud() {
        hud = Hud.buildForCameraAndAttach(getDefaultCamera(), 0);
    }

    private void updateHud() {
        // During tests and VR the mesh in Hud might not exist
        if (hud != null) {
            hud.clear();
            hud.setText(0, "current: ");
            hud.setText(1, "");
            hud.setText(2, "aa");
        }
    }

    private void cycleModel() {
        /*List<EcsEntity> aircrafts = SystemManager.findEntities(new AircraftFilter());
        int i;
        for (i = 0; i < aircrafts.size(); i++) {
            if (aircrafts.get(i).equals(markedmodel)) {
                i++;
                break;
            }
        }
        if (i >= aircrafts.size()) {
            i = 0;
        }
        markedmodel = aircrafts.get(i);*/
    }

    private SceneNode buildGround() {
        Geometry geometry = Geometry.buildPlaneGeometry(MAINPLANE_SIZE, MAINPLANE_SIZE, 1, 1);
        SceneNode ground = new SceneNode(new Mesh(geometry, Material.buildLambertMaterial(new Color(0x88, 0x88, 0x88))));
        ground.getTransform().setPosition(new Vector3(0, 0, 0));
        ground.setName("Ground");
        return ground;
    }

    private SceneNode buildWall() {
        Texture walltexture = Texture.buildBundleTexture("data", "textures/gimp/wood/BucheHell.png");
        Material mat;
        // TODO Needs normal map to have a wall structure
        /*if (wallnormalmap != null) {
            int index = MazeScene.rand.nextInt() % wallnormalmap.length;
            mat = Material.buildPhongMaterialWithNormalMap(walltexture, wallnormalmap[index]);
        } else {*/
        mat = Material.buildLambertMaterial(walltexture);

        SceneNode wall = new SceneNode();
        for (int i = 0; i < 25; i++) {
            SceneNode plane = buildPlaneForWall(1, WALLHEIGHT, mat);
            plane.getTransform().setPosition(new Vector3(i - 12, WALLHEIGHT / 2, -MAINPLANE_SIZE / 2));
            wall.attach(plane);
        }
        return wall;
    }

    private SceneNode buildPlaneForWall(double width, double height, Material material) {

        SceneNode plane;

        SimpleGeometry geo = Primitives.buildSimpleXYPlaneGeometry(width, height, ProportionalUvMap.leftRotatedTexture);
        plane = new SceneNode(new Mesh(geo, material));
        return plane;
    }

    /**
     * panel with 3 rows (dimesion 0.6x0.3)
     * rows must be quite narrow to have a proper property panel with text area large enough
     */
    private static double PropertyControlPanelWidth = 0.6;
    private static double PropertyControlPanelRowHeight = 0.1;
    private static double PropertyControlPanelMargin = 0.005;

    private ControlPanel buildControlPanel(Color backGround) {
        Material mat = Material.buildBasicMaterial(backGround, false);

        DimensionF rowsize = new DimensionF(PropertyControlPanelWidth, PropertyControlPanelRowHeight);
        ControlPanel cp = new ControlPanel(new DimensionF(PropertyControlPanelWidth, 3 * PropertyControlPanelRowHeight), mat, 0.01);
        Indicator indicator;

        // top line: property yontrol
        cp.add(new Vector2(0, PropertyControlPanelRowHeight / 2 + PropertyControlPanelRowHeight / 2),
                new SpinnerControlPanel(rowsize, PropertyControlPanelMargin, mat, null));

        // mid line: a indicator
        indicator = Indicator.buildGreen(0.03);
        // half in ground
        cp.addArea(new Vector2(0, 0), new DimensionF(PropertyControlPanelWidth / 4,
                PropertyControlPanelRowHeight), null);
        cp.attach(indicator);

        // bottom line:  a button
        cp.addArea(new Vector2(0, -PropertyControlPanelRowHeight/*PropertyControlPanelWidth/2,PropertyControlPanelRowHeight/2)*/), new DimensionF(PropertyControlPanelWidth,
                PropertyControlPanelRowHeight), () -> {
            logger.debug("area clicked");
            indicator.toggle();
            elevatorPing.play();

        }).setIcon(Icon.ICON_POSITION);
        return cp;
    }

    public void scale(SceneNode node, double size) {
        logger.debug("scale " + size);
        Vector3 scale = box1.getTransform().getScale();
        scale = scale.multiply(size);
        node.getTransform().setScale(scale);
    }

    /**
     * resize red box.
     */
    private void toggleRedBox(Ray ray, boolean inc) {
        if (ray == null) {
            return;
        }
        List<NativeCollision> intersections = ray.getIntersections();
        logger.debug("intersections: " + intersections.size());
        for (int i = 0; i < intersections.size(); i++) {
            //logger.debug("intersection: " + intersections.get(i).getSceneNode().getName());
            if (intersections.get(i).getSceneNode().getName().equals("red box")) {
                SceneNode pickerobject = new SceneNode(intersections.get(i).getSceneNode());
                scale(pickerobject,(inc) ? 1.1f : 0.9f);
            }
        }
    }

}
