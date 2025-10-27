package de.yard.threed.engine.apps;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.*;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.*;
import de.yard.threed.core.geometry.Primitives;
import de.yard.threed.engine.gui.*;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.core.geometry.SimpleGeometry;

import java.util.List;

/**
 * A simple model preview scene.
 * <p>
 * Verwendet das FG Koordinatensystem?
 * z-axis points up, y to the left(isn't it right?) and x-axis to the viewpoint.
 * Die Camera steht auf positivem x und blickt Richtung negativem X.
 * No ECS to keep it simple.
 * 5.9.25: Rotation node decoupled, ambient light added
 * Keys:
 * - 'x'(shift): moves camera forward(backward)
 * -'+','-': scale up/down
 * cur/up/down/pg/up/down: rotate
 */
public class ModelPreviewScene extends Scene {
    public Log logger = Platform.getInstance().getLog(ModelPreviewScene.class);
    Light light;
    public double scale = 1;
    public SceneNode scaleNode, modelHolderNode;
    public int major = 4;
    String[] modellist;
    Hud hud;
    double rotationspeed = 10;
    SceneNode ground = null;
    public double elapsedsec = 0;
    private double cameraDistance = 120;
    protected SceneNode selectedObject = null;
    MenuCycler menuCycler = null;
    public MessageBox msgBox = null;

    /**
     *
     */
    public String[] getModelList() {
        return new String[]{
                "pcm:loc",
                "pcm:bike",
                "pcm:mobi",
                "engine:plane-darkgreen.gltf",
                "engine:sphere-orange.gltf",
                // 5
                "pcm:avatarA",
                "ac:sample",
                "ac:hard-coded"
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
        modellist = getModelList();

        addLight();

        // Wegen FG Modelorientierung von etwas erhöht auf z=0 aus Osten in Richtung Westen blicken
        // Die Hoehe ist etwas schwierig universell
        getDefaultCamera().getCarrier().getTransform().setPosition(new Vector3(cameraDistance, 0, cameraDistance / 4.0));
        getDefaultCamera().lookAt(new Vector3(0, 0, 0), new Vector3(0, 0, 1));

        hud = Hud.buildForCameraAndAttach(getDefaultCamera(), 0);

        SmartModelLoader.init();

        customInit();

        scaleNode = new SceneNode();
        modelHolderNode = new SceneNode();
        modelHolderNode.getTransform().setParent(scaleNode.getTransform());
        modelHolderNode.setName("modelHolderNode");
        addToWorld(scaleNode);

        newModel();
        addToWorld(ModelSamples.buildAxisHelper(50));

        menuCycler = new MenuCycler(new MenuProvider[]{
                new DefaultMenuProvider(getDefaultCamera(), (Camera camera) -> {
                    ShaderDebugMenu sdm = new ShaderDebugMenu(this);
                    Menu menu = sdm.buildMenu(camera);
                    return menu;
                })
        });
        msgBox = new MessageBox(Color.ORANGE);
    }

    @Override
    public void initSettings(Settings settings) {
        settings.targetframerate = 10;
        settings.aasamples = 4;
    }

    private void redraw() {
        // model maybe not loaded yet, but safe to scale
        scaleNode.getTransform().setScale(new Vector3(scale, scale, scale));
        updateHud();
    }

    private void addLight() {
        Light light = new DirectionalLight(Color.WHITE, new Vector3(0, 3, 2));
        addLightToWorld(light);
        // 5.9.25: Also ambient to avoid complete darkness
        addLightToWorld(new AmbientLight(new Color(0.3f, 0.3f, 0.3f)));
    }

    private void cycleMajor(int inc, int cnt) {
        major += inc;
        if (major < 0) {
            major = cnt - 1;
        }
        if (major >= cnt) {
            major = 0;
        }
        logger.info("cycled to major " + major);
        newModel();
    }

    private void newModel() {
        if (modelHolderNode.getTransform().getChildCount() > 0) {
            SceneNode.removeSceneNode(modelHolderNode.getTransform().getChild(0).getSceneNode());
        }
        scale = 1.0;
        //redCube.getTransform().setPosition(new Vector3(1000, 1000, 1000));
        addModel();
        updateHud();
    }

    private void updateHud() {
        if (hud != null) {
            hud.clear();
            hud.setText(1, "major: " + major);
            hud.setText(2, "scale: " + scale);
            hud.setText(3, "distance: " + cameraDistance);
            hud.setText(4, "selected: " + ((selectedObject == null) ? "" : selectedObject.getName()));
        }
    }

    private void addModel() {
        String modelname = modellist[major];
        final SceneNode destination = new SceneNode();
        SmartModelLoader.loadAndScaleModelByDefinitions(modelname, result -> {

            if (result.getNode() != null) {
                SceneNode model = new SceneNode(result.getNode());
                logger.info("Building imported model. scale=" + scale);

                model.getTransform().setParent(modelHolderNode.getTransform());
            } else {
                //cube as a 'not loaded indicator'
                logger.warn("Showing cube ");
                ModelSamples.buildCube(10, new Color(0xCC, 00, 00)).getTransform().setParent(modelHolderNode.getTransform());
            }
        });
    }

    /*6.9.24 @Override
    public Dimension getPreferredDimension() {
        return new Dimension(1200, 900);
        //return new Dimension(800, 600);
    }*/


    @Override
    public void update() {
        double tpf = getDeltaTime();

        if (Input.getKeyDown(KeyCode.Alpha1)) {
            logger.debug("key '1' was pressed. currentdelta=" + tpf);
            cycleMajor(1, modellist.length);
        }
        if (Input.getKeyDown(KeyCode.Alpha2)) {
            logger.debug("key '2' was pressed. currentdelta=" + tpf);
            cycleMajor(-1, modellist.length);
        }
        if (Input.getKeyDown(KeyCode.Plus)) {
            scale *= 1.5f;
            //logger.debug("scale=" + scale);
            redraw();
        }
        if (Input.getKeyDown(KeyCode.Minus)) {
            scale *= 1f / 1.5f;
            //logger.debug("scale=" + scale);
            redraw();
        }
        if (Input.getKeyDown(KeyCode.KEY_G)) {
            toggleGround();
        }
        if (Input.getKeyDown(KeyCode.KEY_D)) {
            logger.info("model tree:" + modelHolderNode.dump("", 1));
        }
        if (Input.getKey(KeyCode.KEY_LEFT)) {
            scaleNode.getTransform().rotateZ(new Degree(rotationspeed * 1f));
        }
        if (Input.getKey(KeyCode.KEY_RIGHT)) {
            scaleNode.getTransform().rotateZ(new Degree(rotationspeed * -1f));
        }
        if (Input.getKey(KeyCode.KEY_PAGEDOWN)) {
            scaleNode.getTransform().rotateY(new Degree(rotationspeed * 1f));
        }
        if (Input.getKey(KeyCode.KEY_PAGEUP)) {
            scaleNode.getTransform().rotateY(new Degree(rotationspeed * -1f));
        }
        if (Input.getKey(KeyCode.KEY_UP)) {
            scaleNode.getTransform().rotateX(new Degree(rotationspeed * 1f));
        }
        if (Input.getKey(KeyCode.KEY_DOWN)) {
            scaleNode.getTransform().rotateX(new Degree(rotationspeed * -1f));
        }
        if (Input.getKeyDown(KeyCode.KEY_R)) {
            // For testing memory consumption?
            BundleRegistry.clear();
        }
        if (Input.getKeyDown(KeyCode.X)) {
            // 10%
            double d = -cameraDistance / 10.0;
            if (Input.getKey(KeyCode.Shift)) {
                d = -d;
            }
            cameraDistance += d;
            getDefaultCamera().getCarrier().getTransform().setPosition(new Vector3(cameraDistance, 0, cameraDistance / 4.0));
            updateHud();
        }

        Point mouselocation = Input.getMouseUp();
        if (mouselocation != null) {
            // Mousebutton released
            checkForPickingRay(mouselocation);
        }
        menuCycler.update(mouselocation);

        msgBox.hideIfExpired();

        elapsedsec += tpf;
        customUpdate();

    }

    /**
     * to be overridden
     */
    public void customUpdate() {
    }

    private void toggleGround() {
        if (ground == null) {
            SimpleGeometry geo = Primitives.buildPlaneGeometry(1000, 1000, 1, 1);
            Material mat;
            mat = Material.buildLambertMaterial(Color.GRAY);
            ground = new SceneNode(new Mesh(geo, mat));
            ground.getTransform().rotateX(new Degree(90));
            ground.setName("Ground");
            addToWorld(ground);
        } else {
            SceneNode.removeSceneNode(ground);
            ground = null;
        }
    }

    public static SceneNode buildErrorIndicator() {
        SceneNode redCube = new SceneNode(new Mesh(Geometry.buildCube(0.3, 0.3, 0.3), Material.buildBasicMaterial(Color.RED)));
        return redCube;
    }

    public void setRotationSpeed(double rotationspeed) {
        this.rotationspeed = rotationspeed;
    }

    private void checkForPickingRay(Point mouselocation) {
        int x = mouselocation.getX();
        int y = mouselocation.getY();
        //logger.debug("Mouse moved to x" + x + ", y=" + y);
        Ray pickingray = getMainCamera().buildPickingRay(getMainCamera().getCarrier().getTransform(), mouselocation);
        //logger.debug("built pickingray=" + pickingray + " for x=" + x + ",y=" + y + ", dimension=" + ((Platform) Platform.getInstance()).getDimension());
        List<NativeCollision> intersects = pickingray.getIntersections();
        if (intersects.size() > 0) {
            SceneNode foundModelObject = findIntersectedObject(intersects);
            if (foundModelObject != null) {
                selectedObject = foundModelObject;
            }
        } else {
            logger.debug("no intersection found");
            selectedObject = null;
        }
        updateHud();
    }

    /**
     * Only consider parts of the model, but no hud or menu parts
     */
    private SceneNode findIntersectedObject(List<NativeCollision> intersects) {
        String names = "";
        for (int i = 0; i < intersects.size(); i++) {
            names += "," + intersects.get(i).getSceneNode().getName();
        }
        for (NativeCollision intersect : intersects) {
            SceneNode firstIntersect = new SceneNode(intersect.getSceneNode());
            if (isPartOfPreviewModel(firstIntersect)) {
                logger.debug("" + intersects.size() + " intersections detected: " + names + ", getFirst = " + firstIntersect.getName());
                return firstIntersect;
            }
        }
        return null;
    }

    private boolean isPartOfPreviewModel(SceneNode n) {
        if ("modelHolderNode".equals(n.getName())) {
            return true;
        }
        if (n.getParent() == null) {
            return false;
        }
        return isPartOfPreviewModel(n.getParent());
    }
}


/**
 * Dedicated to CustomShaderMaterial. Just a draft for now, not yet tested due to missing model with that shader.
 */
class ShaderDebugMenu {
    public Log logger = Platform.getInstance().getLog(ShaderDebugMenu.class);
    ModelPreviewScene modelPreviewScene;

    private static double PropertyControlPanelWidth = 0.3;
    private static double PropertyControlPanelRowHeight = 0.1;
    private static double PropertyControlPanelMargin = 0.005;

    public ShaderDebugMenu(ModelPreviewScene modelPreviewScene) {
        this.modelPreviewScene = modelPreviewScene;
    }

    public ControlPanelMenu buildMenu(Camera camera) {
        Material mat = Material.buildBasicMaterial(Color.DARKGREEN, null);

        DimensionF rowsize = new DimensionF(PropertyControlPanelWidth, PropertyControlPanelRowHeight);
        int rows = 1;
        ControlPanel cp = new ControlPanel(new DimensionF(PropertyControlPanelWidth, rows * PropertyControlPanelRowHeight), mat, 0.01);

        // bottom line. debug mode value spinner, starting at 0
        // for some unknown reason LabeledSpinnerControlPanel desn't display the current value??
        IntHolder debugModeSpinnedValue = new IntHolder(0);
        cp.add(new Vector2(0,
                        ControlPanelHelper.calcYoffsetForRow(0, rows, PropertyControlPanelRowHeight)),
                new LabeledSpinnerControlPanel("debug mode", rowsize, PropertyControlPanelMargin, mat, new NumericSpinnerHandler(1, value -> {
                    if (value != null) {
                        debugModeSpinnedValue.setValue(value.intValue());
                    }
                    double newval = Double.valueOf(debugModeSpinnedValue.getValue());
                    updateShaderDebugMode((int) newval);
                    return newval;
                }, 2, new NumericDisplayFormatter(0)), Color.RED));

        ControlPanelHelper.positionToNearPlaneByGrid(camera, new Dimension(3, 6),
                new Point(2, 0), cp, new DimensionF(PropertyControlPanelWidth, PropertyControlPanelRowHeight * rows));

        return new ControlPanelMenu(cp);
    }

    private void updateShaderDebugMode(int debugMode) {
        if (modelPreviewScene.selectedObject == null) {
            modelPreviewScene.msgBox.showMessage("no selected object", 3000);
            return;
        }
        if (modelPreviewScene.selectedObject.getMesh() == null || modelPreviewScene.selectedObject.getMesh().getMaterial() == null) {
            modelPreviewScene.msgBox.showMessage("no mesh/material in select object", 3000);
            return;
        }
        Material material = modelPreviewScene.selectedObject.getMesh().getMaterial();
        NativeUniform u = material.material.getUniform(Uniform.DEBUG_MODE);
        if (u == null) {
            modelPreviewScene.msgBox.showMessage("no uniform '" + Uniform.DEBUG_MODE + "' in selected object material", 3000);
            return;
        }
        u.setValue(debugMode);
    }
}

