package de.yard.threed.engine.apps;

import de.yard.threed.core.Color;
import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.DirectionalLight;
import de.yard.threed.engine.Light;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.ObserverSystem;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.FirstPersonMovingSystem;
import de.yard.threed.engine.ecs.GrabbingComponent;
import de.yard.threed.engine.ecs.GrabbingSystem;
import de.yard.threed.engine.ecs.InputToRequestSystem;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.SystemState;
import de.yard.threed.engine.ecs.UserSystem;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.engine.vr.VrInstance;

import java.util.List;

/**
 * A mix of ModelPreviewScene and ShowroomScene, but prepared to be used in AR
 * with object grabbing. So needs ECS for having each model as entity.
 * No self movement. All objects are located on a grid, so no browsing through models.
 * And no object rotations.
 * Scale for each object should lead to a model size of appx. 30cm, what should be
 * a good experience in AR.
 * <p>
 * This is the base scene extended in tcp-flightgear.
 * <p>
 * Uses typical OpenGL coordinate system: y-axis points up, x to the right and z-axis to the viewpoint.
 * <p>
 * No ground for now because we don't know the exact elevation of models.
 * <p>
 * TODO: add light movement?
 */
public class GalleryScene extends Scene {
    public Log logger = Platform.getInstance().getLog(GalleryScene.class);
    String[] modellist;
    String vrMode = null;
    static VrInstance vrInstance;
    protected EcsEntity avatar = null;
    String userName = "user";

    /**
     *
     */
    public String[] getModelList() {
        return new String[]{
                "pcm:loc;scale=0.1",
                "pcm:bike;scale=0.15",
                "pcm:mobi;scale=0.1",
                "engine:plane-darkgreen.gltf;scale=0.2",
                "engine:sphere-orange.gltf;scale=0.1",
                "pcm:avatarA;scale=0.2",
                // have one that fails for error handling testing.
                // but the error box cannot be displayed due to async
                // "scae" is no typo!
                "engine:yy.gltf;scae=0.2"
        };
    }

    @Override
    public String[] getPreInitBundle() {
        return new String[]{"engine", "data"};
    }

    public void customInit() {
    }

    @Override
    public void init(SceneMode sceneMode) {

        vrMode = Platform.getInstance().getConfiguration().getString("vrMode");

        // Observer can exist before login/join for showing eg. an overview.
        // After login/join it might be attched to an avatar.
        Observer observer = Observer.buildForDefaultCamera();

        vrInstance = VrInstance.buildFromArguments();

        InputToRequestSystem inputToRequestSystem = new InputToRequestSystem();
        SystemManager.addSystem(inputToRequestSystem);

        // ObserverSystem also needed in VR
        ObserverSystem observerSystem = new ObserverSystem();
        SystemManager.addSystem(observerSystem);

        if (sceneMode.isServer()) {
            SystemManager.addSystem(new UserSystem());
            // AvatarSystem handles join. Use default avatar.
            AvatarSystem avatarSystem = AvatarSystem.buildFromArguments();
            SystemManager.addSystem(avatarSystem);
        }

        GrabbingSystem grabbingSystem = GrabbingSystem.buildFromConfiguration();
        GrabbingSystem.addDefaultKeyBindings(inputToRequestSystem);
        SystemManager.addSystem(grabbingSystem);

        // no menus for now

        if (vrInstance != null) {
            // Even in VR the observer will be attached to avatar later
            observer.attach(vrInstance.getController(0));
            observer.attach(vrInstance.getController(1));

            /*ControlPanel leftControllerPanel = buildVrControlPanel(buttonDelegates);
            // position and rotation of VR controlpanel is controlled by property ...
            inputToRequestSystem.addControlPanel(leftControllerPanel);
            vrInstance.attachControlPanelToController(vrInstance.getController(0), leftControllerPanel);

            // VR debugPanel additionally at left controller.
            vrDebugPanel = VrDebugPanel.buildVrDebugPanel();
            vrDebugPanel.getTransform().setPosition(new Vector3(0, 0.4, 0.1));
            // No need to add to inputToRequestSystem because it only displays
            leftControllerPanel.attach(vrDebugPanel);*/

            // FPS makes no sense in AR
            if (!vrInstance.isAR()) {
                SystemManager.addSystem(FirstPersonMovingSystem.buildFromConfiguration());
                FirstPersonMovingSystem.addDefaultKeyBindingsforContinuousMovement(inputToRequestSystem);
            }

        } else {

            SystemManager.addSystem(FirstPersonMovingSystem.buildFromConfiguration());
            FirstPersonMovingSystem.addDefaultKeyBindingsforContinuousMovement(inputToRequestSystem);
        }

        modellist = getModelList();

        addLight();

        SmartModelLoader.init();

        customInit();

        loadAllModel();
        //addToWorld(ModelSamples.buildAxisHelper(50));

        SystemState.state = SystemState.STATE_READY_TO_JOIN;

        // Send login request in both monolith and client mode
        if (sceneMode.isClient()) {
            // last init statement. Queue login request for main user
            SystemManager.putRequest(UserSystem.buildLoginRequest(userName, ""));
        }
    }

    @Override
    public void initSettings(Settings settings) {
        settings.targetframerate = 10;
        settings.aasamples = 4;
        settings.vrready = true;
    }

    private void addLight() {
        Light light = new DirectionalLight(Color.WHITE, new Vector3(0, 3, 2));
        addLightToWorld(light);
    }

    private void loadAllModel() {
        for (int i = 0; i < modellist.length; i++) {
            addModel(modellist[i], getGridLocation(i));
        }
    }

    private Vector3 getGridLocation(int index) {
        int gridsize = 4;
        // fitting to preferred model size 30cm
        double gridFieldSize = 0.4;
        int x = index % gridsize;
        int y = index / gridsize;
        double offset = (double) gridsize * gridFieldSize / 2.0;
        if (gridsize % 2 == 0) {
            offset -= gridFieldSize / 2.0;
        }
        Vector3 v = new Vector3(-offset + x * gridFieldSize, 0,
                offset - y * gridFieldSize);
        return v;
    }

    private void addModel(String modelDefinition, Vector3 position) {

        SmartModelLoader.loadAndScaleModelByDefinitions(modelDefinition, result -> {
            if (result.getNode() != null) {
                SceneNode model;
                model = new SceneNode(result.getNode());
                model.getTransform().setPosition(position);
                addToWorld(model);
                EcsEntity entity = new EcsEntity(model);
                entity.setName(result.getNode().getName());
                entity.addComponent(new GrabbingComponent());
                addToWorld(entity.getSceneNode());
            }
        });
    }

    @Override
    public void update() {

        if (avatar == null) {
            // Get avatar and init position
            List<EcsEntity> candidates = SystemManager.findEntities(e -> userName.equals(e.getName()));
            if (candidates.size() > 0 && candidates.get(0).getSceneNode() != null) {
                avatar = candidates.get(0);
                // don't be too far away for grabbing in VR
                avatar.getSceneNode().getTransform().setPosition(new Vector3(0, 1, 1.5));
                avatar.getSceneNode().getTransform().setRotation(Quaternion.buildRotationX(new Degree(-40)));
            }
        }

        customUpdate();
    }

    /**
     * needed for override currently.
     */
    public void customUpdate() {
    }
}
