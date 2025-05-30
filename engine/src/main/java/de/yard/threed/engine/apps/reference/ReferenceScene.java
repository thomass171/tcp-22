package de.yard.threed.engine.apps.reference;

import de.yard.threed.core.*;
import de.yard.threed.core.geometry.ProportionalUvMap;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PreparedModel;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.platform.*;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.testutil.RuntimeTestUtil;
import de.yard.threed.engine.*;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.engine.apps.WoodenToyFactory;
import de.yard.threed.core.geometry.Primitives;
import de.yard.threed.engine.gui.*;
import de.yard.threed.engine.AbstractMaterialFactory;
import de.yard.threed.engine.loader.DefaultMaterialFactory;
import de.yard.threed.engine.loader.PortableModelBuilder;
import de.yard.threed.engine.loader.CustomShaderMaterialFactory;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;
import de.yard.threed.engine.platform.common.*;

import de.yard.threed.engine.geometry.ShapeGeometry;

import java.util.ArrayList;
import java.util.List;

import de.yard.threed.engine.test.AsyncTest;
import de.yard.threed.engine.test.Base3DTest;
import de.yard.threed.engine.test.MainTest;

/**
 * Fuer die Reference Scene.
 * <p>
 * Date: 14.07.14
 * <p>
 * Eine Referenz Scene, die auch in Tests verwendet wird. 30.11.15: Bzw. die Tests laufen bei Taste 'v', weil sich eine echte Scene eigentlich nicht
 * in Tests einbinden laesst. Und weil sich nur auf die Art die verschiedenen Platformen testen lassen.
 * <p>
 * Objekte:
 * 1) Quaderturm mit drei Quadern
 * 2) Quaderturm mit zwei Quadern (30 GRad gedreht)
 * 3) Eine Flasche (erstmal Leiste ) 90 Gr nach links und dann  45 Gr nach hinten/vorne gedreht und dann verschoben, so dass der Mittelpunkt des Flaschenbodens in (....) liegt.
 * Nicht skaliert.
 * <p>
 * Tasten:
 * <p>
 * a: Scenenode wechselweise adden/removen
 * b: Animated move of green(top) box from tower1 to tower 2
 * v: Referenztests (war mal t). Es sollen etwa 4 FM kommen. Bei Erfolg kommt nachher ein grüner Cube, bei Fail ein roter.
 * f: enable/disable FPS Controller
 * c: enable/disable FPS Controller fuer weisse Box
 * m: Menu cycle: (Gridmenupanel), SecondMenu(greencube). Could also be used for help page. Shifted cycles material.
 * e: cycle effects (eg. blend modes, transparency): toggle 'river wall' plane transparency.
 * l: toggle hiddencube layer
 * L: cycle lightNode
 * r: cycle layer rendered
 * s: cycle shading of earth (nicht fertig)
 * <p>
 * 9.3.16: Toggable FPS Controller added. Dann geht aber je nach Einstellung im FPS z.B. Pickingray nicht (wegen Mausmovehandling)
 * 22.7,16: Ein Zugriff auf externe Resourcen (z.B. ueber ModelSamples) soll von hier nicht erfolgen, nur Bundled. Externe gibts im Showroom.
 * 15.9.16: Jetzt auch mit FPS Controller für die weisse Box (unabhaengig von Camera). Die Box bleibt in ihrem local space.
 * 23.9.17: Backround left 'loc' added (instead of "windturbine")
 * 23.9.17: Hud shows 'Hud' in first line, picking ray hit object name in second line,
 * current material index in third line, current light in 4th line.
 * 5.12.18: VR Controller können hier nicht verwendet werden, weil es keine Avatar gibt/geben soll. ISt aber doof. TODO Controller ohne Avatar.
 * 2.5.19: left tower flat shaded.
 * 19.2.25: After extending the custom shader "SimpleTexture" with textureMatrix and normalMatrix, we intensify using these shader for better
 * testing of lighting. So now we use different materials and cycle material by 'shift-M'.
 * right tower uses custom shading while left tower keeps platform shader.
 * 03.03.25: model "waldo" from "bluebird" added for testing bundle subpath
 */
public class ReferenceScene extends Scene {
    static Log logger = Platform.getInstance().getLog(ReferenceScene.class);
    public ArrayList<SceneNode> towerright = new ArrayList<SceneNode>();
    public ArrayList<SceneNode> tower2 = new ArrayList<SceneNode>();

    public SceneNode flasche;
    public SceneNode pyramideblf;
    SceneNode multimatcube, earth, cubeWithoutNormals;
    public StepController controller;
    //EngineObjectFactory eof;
    SceneNode pickerobject = null;
    Vector3 pickeroriginalscale;
    Geometry movingboxgeo;
    Hud hud;
    MenuCycler menuCycler = null;
    FirstPersonController fps, fpswb;
    static final String MOVEBOXNAME = "right 2";
    Quaternion wbrotation;
    Vector3 wbposition;
    Bundle databundle;
    SceneNode locomotive;
    int modelindex = 0;
    // small centered button at bottom, thats always visible (by attaching to FOV camera) for stepping.
    public GuiGrid controlMenu;
    // der liegt eigentlich unter der Plane und ist nur mit deferredRendering seines Layers zu sehen.
    public SceneNode hiddencube;
    // deferred camera for inventory
    Camera deferredcamera;
    boolean usedeferred = true;
    //layer fuer hiddencube. Conflicts with menu hudcube layer?
    //Wohl nicht, FovElement ist immer Layer 1
    //layer 2->9, damit es nicht zufaellig funktioniert
    int HIDDENCUBELAYER = 9;//1 << 8;
    SceneNode lightNode;
    int shading = NumericValue.SMOOTH;
    RequestType REQUEST_CLOSE = RequestType.register(1008, "close");
    RequestType REQUEST_CYCLE = RequestType.register(1009, "cycle");
    // Outside VR inventory is in HIDDENCUBELAYER of derredcamera
    ControlPanel inventory;
    ControlPanel controlPanel;
    Color controlPanelBackground = new Color(128, 193, 255, 128);
    String vrMode = null;
    int lightIndex = 0;
    int materialIndex = 0;
    GeneralHandler[] lightCycle;
    AbstractMaterialFactory[] materialCycle;
    int renderedLayer = -1;
    public static Vector3 INITIAL_CAMERA_POSITION = new Vector3(0, 5, 11);
    double DEFERRED_CAMERA_NEAR = 4.0;
    // far needs to cover hiddencube position in world space (its not attached)
    double DEFERRED_CAMERA_FAR = 15.0;
    Audio elevatorPing;
    boolean remoteShuttleTriggered = false;
    GalleryWall galleryWall;
    int effectCycle = 0;
    Color[] leftTowerColors = new Color[]{new Color(1.0f, 0, 1.0f), Color.WHITE};
    Color[] rightTowerColors = new Color[]{Color.RED, new Color(1.0f, 1.0f, 0), Color.GREEN};
    boolean relativeBundlePathModelTriggered = false;

    @Override
    public void init(SceneMode sceneMode) {
        logger.debug("init ReferenceScene");
        databundle = BundleRegistry.getBundle("data");

        vrMode = Platform.getInstance().getConfiguration().getString("vrMode");
        if (vrMode != null) {
            usedeferred = false;
        }

        //Wegen deferred in JME erst camera, dann scene
        //setupScene();
        Camera camera = getDefaultCamera();
        Base3DTest.testViewMatrixDefaults(camera, logger);
        Base3DTest.testAndExplainMoveForward(camera, logger);

        camera.getCarrier().getTransform().setPosition(INITIAL_CAMERA_POSITION);
        camera.lookAt(new Vector3(0, 0, 0));
        // Second camera for deferred rendering
        if (usedeferred) {
            // There is no reason for using the main cameras near/far. Use sensitive values.
            //deferredcamera = new PerspectiveCamera(camera.getFov(), camera.getAspect(), camera.getNear(), camera.getFar());
            deferredcamera = Camera.createAttachedDeferredCamera(camera, HIDDENCUBELAYER, DEFERRED_CAMERA_NEAR, DEFERRED_CAMERA_FAR);
            //deferredcamera.setLayer(HIDDENCUBELAYER);
            deferredcamera.setName("deferred-camera");
            //deferredcamera.getCarrier().getTransform().setParent(camera.getCarrier().getTransform());
            //deferredcamera.setClearBackground(false);
            //deferredcamera.setClearDepth(true);
        }
        setupScene();

        ViewpointList tl = new ViewpointList();
        controller = new StepController(camera.getCarrierTransform(), tl);

        //auf Hoehe der move destination
        tl.addEntryForLookat(new Vector3(-3, 2, 6), new Vector3(-3, 2, 0));
        // centered before gallery wall
        tl.addEntryForLookat(new Vector3(0, 0, 2), new Vector3(0, 0, 0));
        // attached hinten an die Movebox mit Blick über die Box in Moverichtung
        // die x/y Werte scheinen sehr gross, aber auf die wirkt ja auch der scale der movinbox  (0.25?)
        tl.addEntry(new Vector3(2.5f, 2.1f, 0), Quaternion.buildFromAngles(new Degree(0), new Degree(90), new Degree(0)), getMovingbox().getTransform());
        //controller.addStep(getMovingbox(), new Vector3(10, 2.1f, 0), new Quaternion(new Degree(0), new Degree(-90), new Degree(90)));
        // von ganz oben
        tl.addEntryForLookat(new Vector3(3, 15, 0), new Vector3(2, 0, 0));
        // von hinten rechts
        tl.addEntryForLookat(new Vector3(11, 3, -11), new Vector3(0, 0, 0));
        // von hinten links (etwas weiter zurück und links um loc zu sehen)
        tl.addEntryForLookat(new Vector3(-11, 3, -13/*-11*/), new Vector3(2, 0, 0));
        // leicht links vor die Wall, damit man rechts die PYramide sieht.
        tl.addEntryForLookat(new Vector3(-0.5f, 0, 2), new Vector3(0, 0, 0));
        // weit weg wie in FG mit Blick Richtung (0,0,0) für Test picking ray.
        tl.addEntryForLookat(new Vector3(50000, 30000, 20000), new Vector3(0, 0, 0));
        // shuttle back
        tl.addEntryForLookat(new Vector3(-9.5, 2.9, -9.5), new Vector3(-8, 2, -9.5));
        // shuttle front
        tl.addEntryForLookat(new Vector3(-2, 3.9, -9.5), new Vector3(-8, 2, -9.5));
        // and now back to beginning
        tl.addEntryForLookat(new Vector3(0, 5, 11), new Vector3(0, 0, 0));

        if (vrMode == null) {
            logger.debug("Building controlMenu");
            // control menu with one single teleport/step button
            // deferred fov camera has near/far 5/6.
            Camera cameraForControlMenu = FovElement.getDeferredCamera(getDefaultCamera());
            controlMenu = GuiGrid.buildForCamera(cameraForControlMenu, 2, 1, 1, Color.BLACK_FULLTRANSPARENT, true);
            controlMenu.setName("ControlIcon");
            controlMenu.addButton(new Request(REQUEST_CYCLE), 0, 0, 1, Icon.ICON_POSITION, () -> {
                controller.step(true);
            });
            cameraForControlMenu.getCarrier().attach(controlMenu);
        }
    }

    /**
     * 13.6.21: loc GLTF resides in "data". "engine" instead of "core", which no longer exists.
     */
    @Override
    public String[] getPreInitBundle() {
        return new String[]{"engine", "data"};
    }

    /**
     * The left tower uses platform material, the right custom shader.
     * After changing the means of 'unshaded' the second left box is gray flat shaded, which is correct. Smooth shading of a box seems nonsense.
     */
    private void setupScene() {
        initMaterialCycle();
        movingboxgeo = buildTower("right", towerright, 4, 3, 1, rightTowerColors, null);
        towerright.get(0).getTransform().setPosition(new Vector3(4, 0, -3));
        addToWorld(towerright.get(0));

        buildTower("left", tower2, 3, 2, 1, leftTowerColors, null);
        tower2.get(0).getTransform().rotateY(new Degree(30));
        tower2.get(0).getTransform().setPosition(new Vector3(-3, 1, 0));
        addToWorld(tower2.get(0));

        flasche = buildFlasche(3, 0.3f, Color.BLUE);
        //Die Rotate Reihenfolge hier ist egal, weil es erstmal nur Objekteproperties sind.
        //Entscheidend ist, in welcher Reihenfolge die Rotationen in die Matrixbildung einfliessen.
        //20.5.15: In Threed ist die Reihenfolde wohl doch entscheidend, wahrscheinlich weil ThreeJs anders vorgeht.
        flasche.getTransform().rotateY(new Degree(45));
        flasche.getTransform().rotateZ(new Degree(90));
        // An der Position muesste jetzt genau die Mitte des Flaschenbodens sein.
        flasche.getTransform().setPosition(new Vector3(-2, 1, 4));
        addToWorld(flasche);

        //13.4.16: Nützlich für Rotationtests bei lefthanded.
        buildPyramideBackLeftFront();
        addToWorld(pyramideblf);

        buildMultiMaterialCube(1.5f);
        multimatcube.getTransform().rotateY(new Degree(45));
        multimatcube.getTransform().setPosition(new Vector3(-2, 1, -4));
        addToWorld(multimatcube);

        addOrReplaceEarth();


        //20.5.16: Auch ein Hud, einfach um das bei wechselnden Camerapositionen einfach mittesten zu koennen.
        //3.12.18: Jetzt mit more far near plane???
        //7.10.19: War das nicht immer eine deferred Cam? Tja, weiss nicht. Damit raucht JME aber ab. Mit Versetztem Aufruf von getDeferredCamera gehts wohl(??).
        if (vrMode == null) {
            FovElement.getDeferredCamera(getDefaultCamera());
            //deferred fov camera has near/far 5/6.
            hud = Hud.buildForCameraAndAttach(FovElement.getDeferredCamera(null), 0);
            hud.setText(1, "Hud");
        }
        // finally, after hud is available, set lighting add material to towers
        initLightCycle();
        setLight();
        updateMaterials();

        // 28.4.21: Outside VR inventory is a FovElement at deferredcamera.
        if (deferredcamera != null) {
            inventory = ControlPanelHelper.buildInventoryForDeferredCamera(deferredcamera, getDimension(), controlPanelBackground, new Dimension(300, 20));
            // occupy mid third of inventory. check for headless.
            if (inventory != null) {
                TextTexture textTexture = new TextTexture(Color.LIGHTGRAY);
                ControlPanelArea area1884 = inventory.addArea("area1884", new Vector2(0, 0), new DimensionF(inventory.getSize().getWidth() / 3, inventory.getSize().getHeight()), null);
                area1884.setTexture(textTexture.getTextureForText("1884", Color.RED));
            }
        }
        controlPanel = buildControlPanel(controlPanelBackground, this);
        // in front of left box for easy use after first teleport
        controlPanel.getTransform().setPosition(new Vector3(-3, 1.5, 2.5));
        addToWorld(controlPanel);

        //Ein Ground. Nicht als BasicMaterial, weil das je nach Platform keine Schatten darstellt.
        Material goundmat = Material.buildLambertMaterial(new Color(0.7f, 0.7f, 0.7f));
        SceneNode ground = new SceneNode(new Mesh(ShapeGeometry.buildPlane(16, 32, 1, 1), goundmat, true, true));
        ground.getTransform().setPosition(new Vector3(0, -2, 0));
        ground.setName("Ground");
        addToWorld(ground);

        buildGalleryWall();

        buildPhotoalbumPage();

        // loc nur zum Test des async gltf Ladens, aber ohne Animation um keine Abhaengigkeit zu FG zu haben.
        // A rotation or scale of loc is not needed.
        Platform.getInstance().buildNativeModelPlain(new ResourceLoaderFromBundle(new BundleResource(BundleRegistry.getBundle("data"), "models/loc.gltf")), null, (result) -> {
            locomotive = new SceneNode(result.getNode());
            if (locomotive != null) {
                //Vector3 scale = new Vector3(1.5f, 1.5f, 1.5f);
                //locomotive.getTransform().setScale(scale);
                // Don't rotate 'loc' to show the default vehicle orientation as mentioned in README.md.
                locomotive.getTransform().setPosition(new Vector3(-5, 0, -10));
                addToWorld(locomotive);
            } else {
                logger.error("loc not loaded");
            }
        }, EngineHelper.LOADER_USEGLTF);

        //12.2.18: Eine Line. Die Position ist vorläufig. Ist auch eigentlich egal.
        SceneNode line = ModelSamples.buildLine(new Vector3(-3, 3, -5), new Vector3(3, 3, -15), Color.BLUE);
        addToWorld(line);

        //a blue cube without normals. 25.9.2024: Made transparent for transparency check (Needs alpha != 1 in color).
        SimpleGeometry cuboid = Primitives.buildBox(0.5f, 0.5f, 0.5f);
        cuboid = new SimpleGeometry(cuboid.getVertices(), cuboid.getIndices(), cuboid.getUvs(), null);
        cubeWithoutNormals = new SceneNode(new Mesh(cuboid, Material.buildBasicMaterial(new Color(0.0f, 0.0f, 1.0f, 0.5f), 0.5)));
        cubeWithoutNormals.setName("CubeWithoutNormals");
        //plane is on y=-2
        cubeWithoutNormals.getTransform().setPosition(new Vector3(5, 0.5f, 0));
        addToWorld(cubeWithoutNormals);

        buildHiddenCubes();

        menuCycler = new MenuCycler(new MenuProvider[]{new MainMenuBuilder(this), new SecondMenuBuilder(this)});

        //eine Plane für Canvas
        GenericGeometry canvasgeo = new GenericGeometry(Primitives.buildPlaneGeometry(0.7f, 1.1f, 2, 1));

        NativeCanvas canvas = Platform.getInstance().buildNativeCanvas(300, 200);
        SceneNode canvasNode = new SceneNode(new Mesh(canvasgeo, Material.buildBasicMaterial(new Texture(canvas))));
        // vor der linken Box, damit es nach dem ersten step ganz gut sehen kann.
        canvasNode.getTransform().rotateX(new Degree(90));
        canvasNode.getTransform().rotateY(new Degree(-90));
        canvasNode.getTransform().setPosition(new Vector3(-2.0, 1.5, 2.5));
        canvasNode.setName("Canvas");
        addToWorld(canvasNode);

        AudioClip elevatorPingClip = AudioClip.buildAudioClipFromBundle("data", "audio/elevator-ping-01.wav");
        elevatorPing = Audio.buildAudio(elevatorPingClip);
        if (elevatorPingClip != null && elevatorPing != null) {
            elevatorPing.setVolume(0.5);
            elevatorPing.setLooping(false);
        }
        // Cesium Box
        // TODO check why the box isn't visible
        BundleResource bundleResource = new BundleResource(BundleRegistry.getBundle("engine"), "cesiumbox/BoxTextured.gltf");
        Platform.getInstance().buildNativeModelPlain(new ResourceLoaderFromBundle(bundleResource), null, new ModelBuildDelegate() {
            @Override
            public void modelBuilt(BuildResult result) {
                if (result.getNode() != null) {
                    SceneNode cesiumBox = new SceneNode(result.getNode());

                    //shuttle.getTransform().setRotation(Quaternion.buildRotationX(new Degree(180)));
                    cesiumBox.getTransform().setRotation(Quaternion.buildRotationX(new Degree(0)));
                    cesiumBox.getTransform().setScale(new Vector3(1, 1, 1));
                    cesiumBox.getTransform().setPosition(new Vector3(-2, 2.8, -4));
                    //multimatcube.getTransform().setPosition(new Vector3(-2, 1, -4));
                    addToWorld(cesiumBox);
                    logger.debug("cesiumBox node added");
                }
            }
        }, 0);

        // 20.8.24: Add two instances (objects) of GLTF AlphaBlendModeTest
        bundleResource = new BundleResource(BundleRegistry.getBundle("data"), "gltf-sample-assets/AlphaBlendModeTest/AlphaBlendModeTest.gltf");
        ModelLoader.prepareModel(new ResourceLoaderFromBundle(bundleResource), null, new ModelPreparedDelegate() {
            @Override
            public void modelPrepared(PreparedModel preparedModel) {
                if (preparedModel != null) {
                    SceneNode panel = PortableModelBuilder.buildModel(preparedModel);
                    double scale = 0.15;
                    panel.getTransform().setScale(new Vector3(scale, scale, scale));
                    panel.getTransform().setPosition(new Vector3(3.5, 1.1, 4.0));
                    addToWorld(panel);

                    SceneNode secondPanel = PortableModelBuilder.buildModel(preparedModel);
                    scale = 0.05;
                    secondPanel.getTransform().setScale(new Vector3(scale, scale, scale));
                    secondPanel.getTransform().setPosition(new Vector3(3.5, 1.6, 3.9));
                    addToWorld(secondPanel);

                    logger.debug("AlphaBlendModeTest node added");
                }
            }
        }, null);

        // The default: one default bundle loader
        RuntimeTestUtil.assertEquals("bundleResolver", 1, Platform.getInstance().bundleResolver.size());
        Platform.getInstance().bundleResolver.get(0).addBundlePath("bluebird", "../../tcp-flightgear/bundles");
        logger.debug("setupScene completed");
    }

    /**
     * 2 columns, left column with platform shader, right column with custom shader.
     */
    private void buildGalleryWall() {
        // dimension for having each cell apx rectangular
        DimensionF gallerySize = new DimensionF(1.4f, 2.1f);

        galleryWall = new GalleryWall(gallerySize, 2, 3);
        // Centered between boxes und gedreht, damit die Wall nicht auf der Seite steht.
        galleryWall.wall.getTransform().rotateX(new Degree(90));
        galleryWall.wall.getTransform().rotateY(new Degree(-90));

        addToWorld(galleryWall.wall);

        //a maze wall with Normalmap. 25.9.19: Die Normals in der geo sind trotzdem erforderlich (wegen tangent space).
        // river, um besser Orientierung pruefen zu können.
        // 25.9.19: Has river.jpg a usable alpha channel at all?
        // 16.10.24: Probably no, so there will be no transparancy at all. The JPG file format simply does not have alpha.
        // 18.12.24: river.jpg replaced with SokobanTarget.png which has alpha channel (full transparent)
        // share textures and shader
        Texture river = Texture.buildBundleTexture("data", "images/river.jpg");
        Texture sokobanTarget = Texture.buildBundleTexture("data", "textures/SokobanTarget.png");
        Texture normalMap = Texture.buildNormalMap(new WoodenToyFactory().buildWallNormalMap(6).image);
        ShaderProgram program = ShaderPool.buildUniversalEffect();

        // row 0 (top line): opaque (nontransparent), with normalmap (not in custom shader for now)
        galleryWall.add(0, 0, Material.buildPhongMaterialWithNormalMap(river, normalMap, false),
                "opaque, platform shader, normal map");
        Material mat = Material.buildCustomShaderMaterial(program, true);
        mat.setName("gallery CustomShaderMaterial 0");
        galleryWall.add(1, 0, mat, "opaque, custom shader");
        mat.material.getUniform(Uniform.TEXTURE).setValue(river.texture);

        // row 1: transparent (not possible with river??)
        galleryWall.add(0, 1, Material.buildPhongMaterialWithNormalMap(river, normalMap, true),
                "transparent, platform shader");
        mat = Material.buildCustomShaderMaterial(program, false);
        mat.setName("gallery CustomShaderMaterial 1");
        galleryWall.add(1, 1, mat, "transparent, custom shader");
        mat.material.getUniform(Uniform.TEXTURE).setValue(river.texture);
        mat.material.getUniform(Uniform.TRANSPARENCY).setValue(Float.valueOf(0.5f));

        // row 2: transparent
        galleryWall.add(0, 2, Material.buildPhongMaterialWithNormalMap(sokobanTarget, normalMap, true),
                "transparent, platform shader");
        mat = Material.buildCustomShaderMaterial(program, false);
        mat.setName("gallery CustomShaderMaterial 2");
        galleryWall.add(1, 2, mat, "transparent, custom shader");
        mat.material.getUniform(Uniform.TEXTURE).setValue(sokobanTarget.texture);
        mat.material.getUniform(Uniform.TRANSPARENCY).setValue(Float.valueOf(0.5f));

    }

    /**
     * A control panel with 6 rows (dimesion 0.6x0.3) containing
     * - a property control value spinner for value "961"
     * - a light/dark green indicator toggled by the button below
     * - button for toggling the indicator above, playing elevator ping.
     * ...
     * <p>
     * rows must be quite narrow to have a proper property panel with text area large enough
     */
    private static double PropertyControlPanelWidth = 0.6;
    private static double PropertyControlPanelRowHeight = 0.1;
    private static double PropertyControlPanelMargin = 0.005;

    private static ControlPanel buildControlPanel(Color backGround, ReferenceScene rs) {
        Material mat = Material.buildBasicMaterial(backGround, null);

        DimensionF rowsize = new DimensionF(PropertyControlPanelWidth, PropertyControlPanelRowHeight);
        ControlPanel cp = new ControlPanel(new DimensionF(PropertyControlPanelWidth, 3 * PropertyControlPanelRowHeight), mat, 0.01);
        Indicator indicator;
        IconSetPanel iconSetPanel;

        int rows = 6;
        // top line: property control
        DoubleHolder doubleSpinnedValue = new DoubleHolder(961.2);
        cp.add(new Vector2(0,
                        ControlPanelHelper.calcYoffsetForRow(5, rows, PropertyControlPanelRowHeight)),
                new SpinnerControlPanel(rowsize, PropertyControlPanelMargin, mat, new NumericSpinnerHandler(0.7, value -> {
                    if (value != null) {
                        doubleSpinnedValue.setValue(value.doubleValue());
                    }
                    return Double.valueOf(doubleSpinnedValue.getValue());
                }, null, new NumericDisplayFormatter(1)), Color.RED));

        // mid line: a indicator on the left side, a transformable subtexture on the left right side
        indicator = Indicator.buildGreen(0.03);
        iconSetPanel = new IconSetPanel(PropertyControlPanelRowHeight);
        SceneNode mnode = new SceneNode(iconSetPanel.mesh);
        cp.addArea(new Vector2(-PropertyControlPanelWidth / 4, ControlPanelHelper.calcYoffsetForRow(4, rows, PropertyControlPanelRowHeight)), new DimensionF(PropertyControlPanelWidth / 2,
                PropertyControlPanelRowHeight), null).attach(indicator);
        cp.addArea(new Vector2(PropertyControlPanelWidth / 8, ControlPanelHelper.calcYoffsetForRow(4, rows, PropertyControlPanelRowHeight)), new DimensionF(PropertyControlPanelRowHeight,
                PropertyControlPanelRowHeight), null).attach(mnode);

        // next line:  a button
        cp.addArea(new Vector2(0, ControlPanelHelper.calcYoffsetForRow(3, rows, PropertyControlPanelRowHeight)), new DimensionF(PropertyControlPanelWidth,
                PropertyControlPanelRowHeight), () -> {
            logger.debug("area clicked");
            indicator.toggle();
            rs.elevatorPing.play();
            iconSetPanel.move();

        }).setIcon(Icon.ICON_POSITION);

        // int value spinner
        IntHolder labeledSpinnedValue = new IntHolder(10);
        cp.add(new Vector2(0,
                        ControlPanelHelper.calcYoffsetForRow(2, rows, PropertyControlPanelRowHeight)),
                new LabeledSpinnerControlPanel("int", rowsize, PropertyControlPanelMargin, mat, new NumericSpinnerHandler(1, value -> {
                    if (value != null) {
                        labeledSpinnedValue.setValue(value.intValue());
                    }
                    return Double.valueOf(labeledSpinnedValue.getValue());
                }, null, new NumericDisplayFormatter(0)), Color.RED));

        // degree value spinner, starting at 15
        IntHolder degreeSpinnedValue = new IntHolder(15);
        cp.add(new Vector2(0,
                        ControlPanelHelper.calcYoffsetForRow(1, rows, PropertyControlPanelRowHeight)),
                new LabeledSpinnerControlPanel("degree", rowsize, PropertyControlPanelMargin, mat, new NumericSpinnerHandler(5, value -> {
                    if (value != null) {
                        degreeSpinnedValue.setValue(value.intValue());
                    }
                    return Double.valueOf(degreeSpinnedValue.getValue());
                }, 360, new NumericDisplayFormatter(0)), Color.RED));

        // time value spinner (minutes from midnight) , starting at 08:00
        IntHolder timeSpinnedValue = new IntHolder(8 * 60);
        cp.add(new Vector2(0,
                        ControlPanelHelper.calcYoffsetForRow(0, rows, PropertyControlPanelRowHeight)),
                new LabeledSpinnerControlPanel("time", rowsize, PropertyControlPanelMargin, mat,
                        new NumericSpinnerHandler(15, value -> {
                            if (value != null) {
                                timeSpinnedValue.setValue(value.intValue());
                            }
                            return Double.valueOf(timeSpinnedValue.getValue());
                        }, 24 * 60, new TimeDisplayFormatter()), Color.RED));

        return cp;
    }

    private void addOrReplaceEarth() {
        if (earth != null) {
            SceneNode.removeSceneNode(earth);
            earth = null;
        }
        buildEarth(1.2f);
        earth.getTransform().setPosition(new Vector3(2, 2, -6));
        addToWorld(earth);
    }

    private void setLight() {
        if (lightNode != null) {
            SceneNode.removeSceneNode(lightNode);
        }
        lightCycle[lightIndex].handle();
    }

    private void setDefaultLight(boolean ambient) {
        //22.3.17: No more AmbientLight as default for better testing DirectionalLight.
        // light roughly from viewpoint where the camera is 45 degrees down. So pyramid is well lighted.

        //und es gibt einige Schatten nach unten. Aber nicht zu sehr in Blickrichtung der Camera, sonst sieht man keine Schatten.

        //TODO 2.4.19: Bei JME macht der Wechsel des Lichts den Schatten dunkler/schwärzer. Komisch??.
        //29.4.19: Der Remove von Directional geht in JME offenbar nicht. Sieht zumindest so aus, wenn man mit ambient startet. Da kommt man nicht mehr hin.wegen DirectionalLightShadowRenderer?

        Light light;
        if (ambient) {
            light = new AmbientLight(new Color(0x40, 0x40, 0x40));
        } else {
            light = new DirectionalLight(new Color(0xee, 0xee, 0xee), new Vector3(0, 1, 1));
        }
        this.lightNode = addLightToWorld(light);
    }


    /**
     * Die Verkleinerung der aufstehenden Bloecke mit scale, um das auch zu nutzen.
     * Prinzipiell koennte man natuerlich auch die base* Angaben verkleiner.
     * <p/>
     * Liefert die Geometrie zurueck.
     * 19.2.25: Misleading parameter 'flatshaded' replaced by 'base box is unshaded, other are shaded'. Flat shading should be used in any case (instead of
     * smooth shading) because we use cubes.
     */
    public static Geometry buildTower(String basename, ArrayList<SceneNode> towerlist, double baselength, double basewidth,
                                      double baseheight, Color[] color, AbstractMaterialFactory materialFactory) {
        SceneNode tower = null;
        //Mesh basetower = null;
        double scale = 1;
        Geometry cuboid = null;

        // Start with base box
        for (int i = 0; i < color.length; i++) {
            cuboid = Geometry.buildCube(baselength, baseheight, basewidth);

            Material mat = null;
            if (materialFactory != null) {
                PortableMaterial pm = new PortableMaterial("no-name", color[i]);
                mat = materialFactory.buildMaterial(null, pm, null, true);
            }
            SceneNode newtower = new SceneNode(new Mesh(cuboid, mat, true, true));
            newtower.setName(basename + " " + i);
            newtower.getTransform().scale(new Vector3(scale, scale, scale));
            // trasnalte um meine hoehe (halbe des Vorganegers) + meine halbe
            newtower.getTransform().setPosition(new Vector3(0, (i == 1000) ? 0 : (baseheight * scale + baseheight * scale / 2), 0));
            //newtower.setWireframe(true);
            if (tower != null) {
                tower.attach(newtower);
            }
            /*if (basetower == null) {
                basetower = newtower;
            } */
            towerlist.add(newtower);
            tower = newtower;
            // Ab i=1 ist der Parent, zu dem ein neuer halbierter Block kommt, ja schon halbiert.
            // Darum scale auf 0,5 setzen und nicht weiter verkleinern. Sonst sind die weiteren nur noch
            // ein Viertel/Achtel usw.
            if (i == 0)
                scale /= 2;

        }
        return cuboid;
    }

    /**
     * Erstmal als Cuboid
     */
    private SceneNode buildFlasche(double height, double diameter, Color color) {
        Geometry cuboid = Geometry.buildCube(diameter, height, diameter);
        // 29.8.16: Flasche vorerst wireframe, um so etwas zu haben und weil sie sonst (noch) keinen weiteren Zweck auss der Orientierung hat.
        SceneNode flasche = new SceneNode(new Mesh(cuboid, null/*Material.buildBasicMaterial(color)*/, true, true));
        // Den Boden hochziehen. Das wird oben durch setzen der Position zwar wieder veraendert. Aber die Rotationen
        // muessten dann anders laufen. NeeNee, das ist ja alles als Property im Objekt und
        // wird erst bei Ermittlung der ModelMatrix herangezogen. Also, das hier ist witzlos.
        //flasche.translate(new Vector3(0, height/2, 0));
        flasche.setName("Flasche");
        return flasche;
    }

    /**
     * A cube with the layer of the deferredcamera, but not attached to it.
     * 21.10.19 make it smaller.
     */
    private void buildHiddenCubes() {
        double size = 0.5;
        Geometry cuboid = Geometry.buildCube(size, size, size);
        hiddencube = new SceneNode(new Mesh(cuboid, Material.buildBasicMaterial(Color.ORANGE)));
        hiddencube.setName("HiddenCube");
        //plane liegt auf -2
        hiddencube.getTransform().setPosition(new Vector3(0, -4, 0));
        addToWorld(hiddencube);

        cuboid = Geometry.buildCube(size / 2, size / 2, size / 2);
        SceneNode hiddencubechild = new SceneNode(new Mesh(cuboid, Material.buildBasicMaterial(new Color(255, 0xBF, 0))));
        hiddencubechild.setName("HiddenCubeChild");
        //guckt dann oben halb raus.
        hiddencubechild.getTransform().setPosition(new Vector3(0, size / 2, 0));
        hiddencube.attach(hiddencubechild);
        //setlayer is recursive
        hiddencube.getTransform().setLayer(HIDDENCUBELAYER);
    }

    /**
     * Das ist der Würfel aus (aus http://solutiondesign.com/blog/-/blogs/webgl-and-three-js-texture-mappi-1/) mit einer
     * rein farbigen Seite.
     */
    private void buildMultiMaterialCube(double size) {
        multimatcube = ModelSamples.buildTexturedCube(size, materialCycle[materialIndex]);
    }

    /**
     * Ein paar CompassNeedles dazu, um Orientierungen zu visualisieren.
     * Die Needle gibt es in ThreeJS nicht.
     *
     * @param radius
     */
    private void buildEarth(double radius) {
        // Earth hat radius 1
        earth = ModelSamples.buildEarth();
        // eine unrotierte Needle, auf die man direkt sieht
        SceneNode needle = PortableModelBuilder.buildModel(ModelSamples.buildCompassNeedle(0.3f, 0.1f), null);
        needle.getTransform().setPosition(new Vector3(0, 0, 1.01f));
        earth.attach(needle);
        // und eine links halbhoch, wo manchmal Europa ist.
        needle = PortableModelBuilder.buildModel(ModelSamples.buildCompassNeedle(0.3f, 0.1f), null);
        Vector3 pos = new Vector3(-0.75f, 0.75f, 0);
        needle.getTransform().setPosition(pos);
        // Warum man die direction nicht negieren muss, ist nicht ganz klar. Klappt aber.
        // Vector3 direction = new Vector3().subtract(pos);
        Vector3 up = new Vector3(0, 1, 0);
        needle.getTransform().setRotation((MathUtil2.buildLookRotation(pos, up)));
        earth.attach(needle);
        // earth.castShadow = true;
        //earth.receiveShadow = true;
    }

    /**
     * Erst nach hinten und dann nach links drehen.
     * Dann liegt sie auf der x-Achse.
     * und dann nach vorner rechts schieben.
     * Die Spitze liegt dann genau auf der z-Achse.
     * Ist auch fuer ReferenceTests.
     * Bei JME hat der Cylinder eine andere Ausrichtung. Darum sieht er anders aus. Fuer die Tests sollte
     * das egal sein, bzw. die Tests sollten so gestaltet sein, dass es egal ist.
     * Damit man auch die Rotation um ihre Achse erkennen zu können, bekommt die Pyramide eine kleine "Flagge" in Richtung positivem x.
     * Nicht wireframe und mit Phong, um die Surface Normalen zu testen.
     */
    private void buildPyramideBackLeftFront() {
        double xangle = -Math.PI / 2;
        double yangle = Math.PI / 2;
        double radiusTop = 0, radiusBottom = 0.6f, height = 3;
        int radialSegments = 16;
        Geometry pyramidgeo = Geometry.buildCylinderGeometry(radiusTop, radiusBottom, height, radialSegments);
        Material pyramidmat = Material.buildPhongMaterial(new Color(0xFF, 0XDE, 0x14));
        //pyramidmat.setWireframe(true);
        Mesh pymesh = new Mesh(pyramidgeo, pyramidmat, true, true);
        //pymesh.setName("Pyramide");
        pyramideblf = new SceneNode(pymesh);
        pyramideblf.setName("Pyramide");
        // Die Rotation macht ThreeJs dann wohl mit seiner Euler DefaultOrder XYZ
        // Die Rotation um z ist weil nach der x Rotation die y-Achse ja z ist.
        // Interessanterweise ist die EulerOrder bei Platform OpenGl offenbar unterschiedlich zu ThreeJS. Da muss die zweite Rotation nicht nach z.
        // Als Testzweck ist es wichtig, setRotation zu verwenden statt setRotateStatus(). Das macht ja schon die Flasche.
        Quaternion rotation = Quaternion.buildFromAngles(xangle, yangle, 0);
        pyramideblf.getTransform().setRotation(rotation);
        //pyramideblf.rotation.z = yangle;//Math.PI/2;
        //pyramideblf.rotation.x = xangle;//-Math.PI/2;
        // Die Position nicht absolut, sondern mit translate setzen, um das fuer rotierte Objekte zu testen.  
        //pyramideblf.setPosition(new Vector3(height / 2, 0, 3));
        pyramideblf.getTransform().translateY(-height / 2);
        pyramideblf.getTransform().translateX(-3);

        Geometry flaggeo = Geometry.buildCube(radiusBottom / 3, radiusBottom / 3, radiusBottom / 3);
        Material flagmat = Material.buildBasicMaterial(new Color(0xFF, 0x77, 0x00));
        SceneNode flag = new SceneNode(new Mesh(flaggeo, flagmat));
        pyramideblf.attach(flag);
        flag.setName("Pyramide Flag");
        flag.getTransform().setPosition(new Vector3(radiusBottom, -height / 2, 0));
    }

    /**
     * Photo album custom shader not display correctly in Unity and ThreeJS yet. So don't use it for now? Causes nasty ThreeJS error messages.
     */
    private void buildPhotoalbumPage() {

        Material mat = null;
        Texture[] textures = null;
        //ShapeGeometry cubegeometry = ShapeGeometry.buildPlane(3, 3, 1, 1);
        textures = new Texture[]{Texture.buildBundleTexture("data", "images/lake.jpg"), Texture.buildBundleTexture("data", "images/mountain-lake.jpg")};
        // Texture Shading
        ShaderProgram program = ShaderPool.buildPhotoalbumEffect();
        mat = Material.buildCustomShaderMaterial(program, true);
        mat.material.getUniform("u_texture0").setValue(textures[0].texture);
        mat.material.getUniform("u_texture1").setValue(textures[1].texture);
        mat.setName("photo album material");
        //mat = Material.buildPhongMaterial(textures[0]);
        //3.5.21 eine wall by simple plane above
        SceneNode simplewall = new SceneNode(new Mesh(Primitives.buildSimpleXYPlaneGeometry(1.1, 1.8, new ProportionalUvMap()), mat));
        simplewall.getTransform().setPosition(new Vector3(0, 2, -2));
        simplewall.setName("Photoalbum XY plane Wall");
        addToWorld(simplewall);
    }

    public SceneNode getMovingbox() {
        return towerright.get(2);
    }

    @Override
    public void update() {
        double tpf = getDeltaTime();
        controller.update(tpf);

        Point mouselocation = Input.getMouseUp();
        if (mouselocation != null) {
            // needs to use position of main camera. Deferred cameras are attached there.
            Ray mousePickingRay = getDefaultCamera().buildPickingRay(getDefaultCamera().getCarrier().getTransform(), mouselocation);
            controlMenu.checkForClickedArea(mousePickingRay);
            controlPanel.checkForClickedArea(mousePickingRay);
        }

        if (Input.getKeyDown(KeyCode.A)) {
            //
            String name = "ToggleNode";
            SceneNode n = SceneNode.findFirst(name);
            if (n == null) {
                switch (modelindex) {
                    case 0:
                        n = ModelSamples.buildEarth();
                        break;
                }
                if (n != null) {
                    n.setName(name);
                    addToWorld(n);
                    modelindex++;
                    if (modelindex >= 2) {
                        modelindex = 0;
                    }
                }
            } else {
                SceneNode.removeSceneNode(n);
            }
        }
        if (Input.getKeyDown(KeyCode.B)) {
            logger.debug("b key was pressed. currentdelta=" + tpf);
            moveBox();
        }
        // pure 'm' is handled in menuCycler
        if (Input.getKeyDown(KeyCode.M) && Input.getKey(KeyCode.Shift)) {
            cycleMaterial();
        }
        menuCycler.update(mouselocation);

        //(V)alidate statt (T)est
        if (Input.getKeyDown(KeyCode.V)) {
            // Order doesn't matter. Can be changed arbitrary.
            // 23.1.18: Maintest added
            // LayerTest runs twice to see where it breaks.
            logger.debug("v key was pressed. currentdelta=" + tpf);
            MainTest.runTest();
            ReferenceTests.testLayer(this);
            ReferenceTests.testPyramideBackLeftFront(pyramideblf);
            ReferenceTests.mvpTest(getMainCamera(), getDimension(), usedeferred);
            ReferenceTests.testOriginalScale(towerright.get(1));
            ReferenceTests.testParent(towerright.get(1), towerright.get(0));
            ReferenceTests.testParent(towerright.get(2), towerright.get(1));
            ReferenceTests.testExtracts(towerright.get(2));
            ReferenceTests.testIntersect(towerright, towerright.get(2));
            ReferenceTests.testMovingboxView(this);
            ReferenceTests.testRayFromFarAway(getDimension(), this);
            //TODO should show yellow cube when tests were skipped.
            if (!ReferenceTests.isUnity()) {
                ReferenceTests.testRay(getDimension(), getMainCamera());
            }
            ReferenceTests.testFind(this, towerright.get(2));
            ReferenceTests.testGetParent(this, towerright.get(2));
            ReferenceTests.testFindNodeByName(this);
            ReferenceTests.testJson();
            ReferenceTests.testLayer(this);
            ReferenceTests.testFirstPersonTransform(this);
            ReferenceTests.testLights(this);
            logger.info("tests completed");
            //Der AsyncTest provoziert Fehler zum Test, so dass geloggte error Meldungen dabei korrekt sind.
            new AsyncTest().runtest(this);

        }
        if (Input.getKeyDown(KeyCode.F)) {
            if (fps == null) {
                fps = new FirstPersonController(getMainCamera().getCarrier().getTransform(), true);
            } else {
                fps = null;
            }
        }
        if (fps != null) {
            fps.update(tpf);
        }
        if (Input.getKeyDown(KeyCode.C)) {
            //white box FPC
            if (fpswb == null) {
                SceneNode wb = tower2.get(1);
                wbrotation = wb.getTransform().getRotation();
                wbposition = wb.getTransform().getPosition();
                fpswb = new FirstPersonController(wb.getTransform(), false);
            } else {
                fpswb = null;
                SceneNode wb = tower2.get(1);
                wb.getTransform().setRotation(wbrotation);
                wb.getTransform().setPosition(wbposition);
            }
        }
        if (fpswb != null) {
            fpswb.update(tpf);
        }
        checkForPickingRay();


        if (Input.getKeyDown(KeyCode.E)) {
            cycleEffects();
        }
        if (Input.getKeyDown(KeyCode.L)) {
            if (Input.getKey(KeyCode.Shift)) {
                //cycle lightNode
                lightIndex++;
                if (lightIndex >= lightCycle.length) {
                    lightIndex = 0;
                }
                setLight();
            } else {
                // toggle hidden cube layer
                int layer = hiddencube.getTransform().getLayer();
                logger.debug("hiddencube.layer=" + layer);
                hiddencube.getTransform().setLayer(((layer == HIDDENCUBELAYER)) ? 0 : HIDDENCUBELAYER);
            }
        }
        if (Input.getKeyDown(KeyCode.S)) {
            cycleShading();
        }
        if (Input.getKeyDown(KeyCode.R)) {
            cycleRendering();
        }
        if (!remoteShuttleTriggered) {
            // use ... temporarily until shuttle uses textures (for testing remote texture loading). Now we have cesium box
            //String bundleUrl="http://yard.de/bundlepool/nasa";
            //String remoteModel = "shuttle-hi-res/shut.gltf";
            //Vector3 scale = new Vector3(0.003, 0.003, -0.003);
            String bundleUrl = "https://ubuntu-server.udehlavj1efjeuqv.myfritz.net/publicweb/bundlepool/fgdatabasicmodel";
            String remoteModel = "Models/Airport/Pushback/Goldhofert.gltf";
            Vector3 scale = new Vector3(0.1, 0.1, -0.1);
            AbstractSceneRunner.getInstance().loadBundle(bundleUrl, new BundleLoadDelegate() {
                @Override
                public void bundleLoad(Bundle bundle) {
                    // don't load via platform (which finally would be similar, but with possible waiting for bundle data).
                    BundleResource file = new BundleResource(bundle, remoteModel);
                    ResourcePath opttexturepath = null;
                    int loaderoptions = 0;

                    logger.debug("Bundle " + bundle.name + " loaded");
                    // build model twice with different material factories, platform and custom shader
                    buildRemoteModel(new ResourceLoaderFromBundle(file), opttexturepath, loaderoptions, new Vector3(-8, 2, -10), scale, new DefaultMaterialFactory());
                    buildRemoteModel(new ResourceLoaderFromBundle(file), opttexturepath, loaderoptions, new Vector3(-8, 2, -9), scale, new CustomShaderMaterialFactory(ShaderPool.buildUniversalEffect()));

                }
            });
            remoteShuttleTriggered = true;
        }
        if (!relativeBundlePathModelTriggered) {
            // "waldo" has advantage of texture and no XML
            String relativeModel = "Models/waldo.gltf";
            Vector3 scale = new Vector3(0.1, 0.1, -0.1);
            AbstractSceneRunner.getInstance().loadBundle("bluebird", new BundleLoadDelegate() {
                @Override
                public void bundleLoad(Bundle bundle) {
                    // directory tcp-flightgear might not be available
                    if (bundle != null) {
                        // don't load via platform (which finally would be similar, but with possible waiting for bundle data).
                        BundleResource file = new BundleResource(bundle, relativeModel);
                        ResourcePath opttexturepath = new ResourcePath("Models/Textures");
                        int loaderoptions = 0;

                        logger.debug("Bundle " + bundle.name + " loaded");
                        // build model twice with different material factories, platform and custom shader
                        // put aside Goldhofert
                        buildRemoteModel(new ResourceLoaderFromBundle(file), opttexturepath, loaderoptions, new Vector3(-7, 2, -10), scale, new DefaultMaterialFactory());
                        buildRemoteModel(new ResourceLoaderFromBundle(file), opttexturepath, loaderoptions, new Vector3(-7, 2, -9), scale, new CustomShaderMaterialFactory(ShaderPool.buildUniversalEffect()));
                    }
                }
            });
            relativeBundlePathModelTriggered = true;
        }
    }

    private void buildRemoteModel(ResourceLoaderFromBundle resourceLoaderFromBundle, ResourcePath opttexturepath, int loaderoptions, Vector3 position, Vector3 scale, AbstractMaterialFactory materialFactory) {
        ModelLoader.buildModel(resourceLoaderFromBundle, opttexturepath, loaderoptions, new ModelBuildDelegate() {
            @Override
            public void modelBuilt(BuildResult r) {
                if (r.getNode() != null) {
                    SceneNode shuttle = new SceneNode(r.getNode());

                    //shuttle.getTransform().setRotation(Quaternion.buildRotationX(new Degree(180)));
                    shuttle.getTransform().setRotation(Quaternion.buildRotationX(new Degree(0)));
                    shuttle.getTransform().setScale(scale);
                    shuttle.getTransform().setPosition(position);
                    addToWorld(shuttle);
                    logger.debug("shuttle node added");
                }
            }
        }, materialFactory);
    }

    private void cycleShading() {
        shading++;
        if (shading > NumericValue.FLAT) {
            shading = 0;
        }
        addOrReplaceEarth();
    }

    private void cycleMaterial() {
        materialIndex++;
        if (materialIndex >= materialCycle.length) {
            materialIndex = 0;
        }
        updateMaterials();
    }

    /**
     * For now only wall transparency toggle
     */
    private void cycleEffects() {
        effectCycle++;
        if (effectCycle > 1) {
            effectCycle = 0;
        }
        logger.debug("Cycling to effect " + effectCycle);
        switch (effectCycle) {
            case 0:
                galleryWall.setTransparency(false);
                break;
            case 1:
                galleryWall.setTransparency(true);
                break;
        }
    }

    private void cycleRendering() {
        switch (renderedLayer) {
            case -1:
                renderedLayer = 1;
                break;
            case 1:
                renderedLayer = HIDDENCUBELAYER;
                break;
            case 9://HIDDENCUBELAYER:
                renderedLayer = -1;
                break;
            default:
                break;
        }
        Platform.getInstance().setOption(Platform.PLATFORM_OPTION_RENDEREDLAYER, "" + renderedLayer);
    }

    private void checkForPickingRay() {
        Point mouselocation;
        if ((mouselocation = Input.getMouseMove()) != null) {
            // Die Maus hat sich bewegt. Pruefen, ob Tower getroffen wurde.
            int x = mouselocation.getX();
            int y = mouselocation.getY();
            //logger.debug("Mouse moved to x" + x + ", y=" + y);
            Ray pickingray = getMainCamera().buildPickingRay(getMainCamera().getCarrier().getTransform(), mouselocation);
            //logger.debug("built pickingray=" + pickingray + " for x=" + x + ",y=" + y + ", dimension=" + ((Platform) Platform.getInstance()).getDimension());
            if (hud != null) {
                if (pickingray.getIntersections().size() > 0) {
                    //nur den ersten Namen zeigen
                    hud.setText(2, pickingray.getIntersections().get(0).getSceneNode().getName());
                } else {
                    hud.setText(2, "");
                }
            }

            // 31.11.15 Das markieren des getroffenen Objekts ist noch nicht einheitlich wegen verschiedener Collisionphliosophien
            // 05.01.16: So unterschiedlich sind die Philosophien wohl gar nicht. Aber das mit der Liste koennte unguenstig sein,
            // denn ein Trffer der kleinen Box ist immer auch einer der Parent. Das sammel ich evtl. zu viele an. Und dann kommt
            // der Zufall dazu, welche wohl die erste gelieferte ist. Und dann wird entwder nur eine Box größer oder immer der Turm.
            // Evtl. ist die ganze Verwendung mit Model und Submodeln diesen Models fragwürdig.
            // 23.8.16: Man kann auch die root node zur picking object suche angeben.26.8.16:Im moment aber noch nicht.
            List<NativeCollision> intersects = pickingray.getIntersections(towerright/*world*/, true);
            if (intersects.size() > 0) {
                SceneNode intersect = new SceneNode(intersects.get(0).getSceneNode());
                String names = "";
                for (int i = 0; i < intersects.size(); i++) {
                    names += "," + intersects.get(i).getSceneNode().getName();
                }
                //logger.debug(""+intersects.size()+" intersections detected: "+names+", getFirst = " + intersect.getName());
                if (pickerobject == null || !intersect.getName().equals(pickerobject.getName())) {
                    logger.debug("intersection detected. intersect=" + intersect.getName() + ", pickerobject = " + ((pickerobject == null) ? "null" : pickerobject.getName()) + " at mouse position " + mouselocation);
                    // Dann hat sich was geändert
                    if (pickerobject != null) {
                        // wieder zureucksetzen
                        pickerobject.getTransform().setScale(pickeroriginalscale);
                        pickerobject = null;
                    }
                    pickerobject = intersect;
                    pickeroriginalscale = pickerobject.getTransform().getScale();
                    //logger.debug("Scaling pickerobject "+ get_type(pickerobject)+ ":"+dumpObject(pickerobject));
                    logger.debug("originalscale=" + pickeroriginalscale);
                    double pickscale = pickerobject.getTransform().getScale().getX() * 1.1f;
                    pickerobject.getTransform().setScale(new Vector3(pickscale, pickscale, pickscale));
                    //towerrechts.get(1).setScale(new Vector3(pickscale, pickscale, pickscale));

                    //logger.debug("camera.projectionMatrix=\n" + dumpMatrix4(camera.projectionMatrix));
                    //logger.debug("camera.matrixWorldInverse(aka viewmatrix)=\n" + dumpMatrix4(camera.matrixWorldInverse));
                }
            } else {
                if (pickerobject != null) {
                    // wieder zureucksetzen
                    pickerobject.getTransform().setScale(pickeroriginalscale);
                    logger.debug("pickeroriginalscale.x=" + pickeroriginalscale.getX());
                    pickerobject = null;
                }
            }
        }
        if ((mouselocation = Input.getMouseUp()) != null) {
            // Mausbutton released
            int x = mouselocation.getX();
            int y = mouselocation.getY();
            //logger.debug("Click at " + mouselocation);
            Ray pickingray = getMainCamera().buildPickingRay(getMainCamera().getCarrier().getTransform(), mouselocation);
            //5.5.21: Warum eigentlich hud?
            List<NativeCollision> intersects = pickingray.getIntersections(hud, true);
            if (intersects.size() > 0) {
                SceneNode intersect = new SceneNode(intersects.get(0).getSceneNode());
                String names = "";
                for (int i = 0; i < intersects.size(); i++) {
                    names += "," + intersects.get(i).getSceneNode().getName();
                }
                logger.debug("" + intersects.size() + " intersections detected: " + names + ", getFirst = " + intersect.getName());
            }

        }
    }

    private void moveBox() {
        //30.5.  decomposeMatrix(flasche.getLocalModelMatrix());

        SceneNode movebox = towerright.get(2);
        // Die Parent ModelMatrix der Box
        Matrix4 parentmodel = movebox.getTransform().getParent().getWorldModelMatrix();
        Matrix4 currentmodel = (movebox.getTransform().getLocalModelMatrix());
        logger.debug("currentmodel m=\n" + currentmodel.dump("\n"));

        // Die Zielposition mir wireframe Box anzeigen
        Material markermaterial = Material.buildLambertMaterial(Color.ORANGE);
        SceneNode destinationmarker = new SceneNode(new Mesh(movingboxgeo, markermaterial));
        //markermaterial.setWireframe(true);
        markermaterial = null;
        destinationmarker.getTransform().scale(movebox.getTransform().getScale());
        SceneNode bottombox = tower2.get(1);
        double bottomheight = 0.5f;
        double y = bottomheight + bottomheight / 2;
        destinationmarker.getTransform().setPosition(new Vector3(0, y, 0));
        bottombox.attach(destinationmarker);
        Matrix4 markermodel = (destinationmarker.getTransform().getLocalModelMatrix());
        logger.debug("markermodel=\n" + markermodel.dump("\n"));
        Matrix4 markerworldmodel = (destinationmarker.getTransform().getWorldModelMatrix());
        logger.debug("markerworldmodel=\n" + markerworldmodel.dump("\n"));
        // Gesucht ist die Matrix M, die mit der movebox Parentmatrix das marker(world)model ergibt, also
        // markermodel = M * parentmodel, bzw M = markermodel * inv(parentmodel)
        // oder: markermodel = parentmodel * M,
        //     bzw inv(parentmodel) * markermodel = inv(parentmodel) * parentmodel * M
        //     bzw M = inv(parentmodel) * markermodel
        Matrix4 M = parentmodel.getInverse().multiply(markerworldmodel);
        logger.debug("M=\n" + M.dump("\n"));
        // Gegenprobe (muss markermodel geben)
        Matrix4 gegenprobemarkermodel = parentmodel.multiply(M);
        logger.debug("gegenprobemarkermodel=\n" + gegenprobemarkermodel.dump("\n"));

        Quaternion animationInitialQuaternion = currentmodel.extractQuaternion();
        Vector3 animationStartPosition = movebox.getTransform().getPosition();//getPositionFromMatrix(currentmodel);
        logger.debug("animationInitialQuaternion=" + animationInitialQuaternion.dump("\n"));
        logger.debug("animationStartPosition=" + animationStartPosition.dump("\n"));
        Quaternion animationTargetQuaternion = M.extractQuaternion();
        Vector3 animationTargetPosition = M.extractPosition();
        logger.debug("animationTargetQuaternion=" + animationTargetQuaternion.dump("\n"));
        logger.debug("animationTargetPosition=" + animationTargetPosition.dump("\n"));
        // animationtranslate = getPositionFromMatrix(M);
        Vector3 animationtranslate = new Vector3();
        animationtranslate = animationTargetPosition.subtract(animationStartPosition);
        animationtranslate = animationtranslate.multiply((1.0 / 10.0)/*animationsteps*/);

        SceneNode animationobject = movebox;
        boolean quickmove = false;
        if (quickmove) {
            //applyMatrixToObject3D(movebox,M);
            movebox.getTransform().setPosition(M.extractPosition());
            movebox.getTransform().setRotation(animationTargetQuaternion);

            Matrix4 currentmodelafter = (movebox.getTransform().getWorldModelMatrix());
            logger.debug("currentmodel (after transfer)=\n" + currentmodelafter.dump("\n"));

        } else {
            Animation animation = new MoveAnimation(movebox, /*movebox.getPosition(),*/ animationTargetPosition, /*movebox.getRotation()/*animationInitialQuaternion*/ animationTargetQuaternion, 2000/*dauer statt steps 10*/);
            SceneAnimationController.getInstance().startAnimation(animation, true);
        }
    }

    @Override
    public void initSettings(Settings settings) {
        settings.targetframerate = 10;
        settings.aasamples = 4;
        // far relativ moderat, sonst gibts in Unity zumindest keinen Schatten
        settings.far = 50f;
        settings.vrready = true;
    }

    @Override
    public Dimension getPreferredDimension() {
        if (Platform.getInstance().isDevmode()) {
            return new Dimension(800, 600);
        }
        return null;
    }

    private void initLightCycle() {
        lightCycle = new GeneralHandler[]{
                // 0: standard/initial light, no ambient
                () -> {
                    setDefaultLight(false);
                    hud.setText(4, "Light(" + Platform.getInstance().getLights().size() + "): dir         ");
                }, () -> {
            // 1: ambient
            setDefaultLight(true);
            hud.setText(4, "Light(" + Platform.getInstance().getLights().size() + "): dir ,amb        ");
        },
                // 2: point light
                () -> {
                    PointLight pointLight = new PointLight(Color.WHITE);
                    this.lightNode = addLightToWorld(pointLight);
                    hud.setText(4, "Light(" + Platform.getInstance().getLights().size() + "): point        ");
                },
                // 3: no light, just dark
                () -> {
                    hud.setText(4, "Light(" + Platform.getInstance().getLights().size() + "): no        ");

                }};
    }

    private void initMaterialCycle() {
        materialCycle = new AbstractMaterialFactory[]{
                // 0: custom shader
                new AbstractMaterialFactory() {
                    @Override
                    public Material buildMaterial(ResourceLoader resourceLoader, PortableMaterial pm, ResourcePath texturebasepath, boolean hasnormals) {
                        ShaderProgram program = ShaderPool.buildUniversalEffect();
                        Material mat = Material.buildCustomShaderMaterial(program, true);

                        mat.material.getUniform(Uniform.TEXTURED).setValue(pm.getTexture() != null);
                        mat.material.getUniform(Uniform.SHADED).setValue(pm.isShaded());

                        if (pm.getColor() != null) {
                            mat.material.getUniform(Uniform.COLOR).setValue(new Quaternion(
                                    pm.getColor().getR(),
                                    pm.getColor().getG(),
                                    pm.getColor().getB(),
                                    pm.getColor().getAlpha()));
                            mat.setName("colored CustomShaderMaterial");
                        } else if (pm.getTexture() != null) {
                            Texture texture = super.resolveTexture(pm.getTexture(), resourceLoader, texturebasepath, pm.getWraps(), pm.getWrapt(), logger);
                            mat.material.getUniform(Uniform.TEXTURE).setValue(texture.texture);
                            mat.setName("textured CustomShaderMaterial");
                        }

                        return mat;
                    }
                },
                new AbstractMaterialFactory() {
                    // 1: platform shader, shaded
                    @Override
                    public Material buildMaterial(ResourceLoader resourceLoader, PortableMaterial pm, ResourcePath texturebasepath, boolean hasnormals) {
                        // use platform material
                        Material mat;
                        if (pm.getTexture() != null) {
                            Texture texture = super.resolveTexture(pm.getTexture(), resourceLoader, texturebasepath, pm.getWraps(), pm.getWrapt(), logger);
                            mat = Material.buildPhongMaterial(texture);
                            // only for testing we really come here.
                            // mat = Material.buildPhongMaterial(Texture.buildBundleTexture("data", "images/river.jpg"));
                            mat.setName("textured platform material");
                        } else {
                            if (pm.isShaded()) {
                                mat = Material.buildPhongMaterial(pm.getColor(), 1);
                            } else {
                                mat = Material.buildBasicMaterial(pm.getColor());
                            }
                            mat.setName("colored platform material");
                        }
                        return mat;
                    }
                }
        };
    }

    private void updateMaterials() {
        for (int i = 0; i < tower2.size(); i++) {
            PortableMaterial pm = new PortableMaterial("no-name", leftTowerColors[i]);
            Material m = materialCycle[materialIndex].buildMaterial(null, pm, null, true);
            tower2.get(i).getMesh().setMaterial(m);
        }
        for (int i = 0; i < towerright.size(); i++) {
            PortableMaterial pm = new PortableMaterial("no-name", rightTowerColors[i]);
            Material m = materialCycle[materialIndex].buildMaterial(null, pm, null, true);
            towerright.get(i).getMesh().setMaterial(m);
        }
        multimatcube.getMesh().setMaterial(ModelSamples.buildTexturedCubeMaterial(materialCycle[materialIndex]));
        hud.setText(3, materialIndex == 0 ? "custom shader   " : "platform shader   ");
    }
}

class IconSetPanel {

    Mesh mesh;
    int fontIndex;
    Texture iconset;
    Material mat;

    public IconSetPanel(double size) {
        //GenericGeometry planeGeo = new GenericGeometry(Primitives.buildPlaneGeometry(size, size, 1, 1));
        // start with top left element and later translate ...
        GenericGeometry planeGeo = new GenericGeometry(Primitives.buildSimpleXYPlaneGeometry(size, size,
                ProportionalUvMap.buildForGridElement(16, 0, 0, false)));
        iconset = Texture.buildBundleTexture("engine", "Iconset-LightBlue.png");

        ShaderProgram program = ShaderPool.buildUniversalEffect();
        mat = Material.buildCustomShaderMaterial(program, true);
        mat.setName("IconSetPanel");
        mat.material.getUniform(Uniform.TEXTURE).setValue(iconset.texture);

        move();
        mesh = new Mesh(planeGeo, mat);
        //SceneNode wall = new SceneNode();
    }

    public void move() {
        ReferenceScene.logger.debug("move to " + fontIndex);
        Matrix3 textureMatrix = Texture.getTextureMatrixForGridElement(16, 16, fontIndex, 11);
        mat.material.getUniform(Uniform.TEXTUREMATRIX).setValue(textureMatrix);

        fontIndex++;
    }
}

class GalleryWall {

    SceneNode wall;
    DimensionF size;
    int rows, cols;

    GalleryWall(DimensionF size, int cols, int rows) {
        this.size = size;
        this.rows = rows;
        this.cols = cols;
        boolean withGrid = false;
        if (withGrid) {
            // switch height/width due to rotation
            GenericGeometry wallgeo = new GenericGeometry(Primitives.buildPlaneGeometry(size.getHeight(), size.getWidth(), 2, 1));
            //wallMat = buildWallMaterial(false);
            wall = new SceneNode(new Mesh(wallgeo, null));
        } else {
            wall = new SceneNode();
        }
        wall.setName("GalleryWall");
    }

    public void add(int col, int row, Material material, String name) {
        material.setName(name);
        double width = size.getWidth() / cols;// - 0.1;
        double height = size.getHeight() / rows;
        GenericGeometry wg = new GenericGeometry(Primitives.buildPlaneGeometry(
                width, height, 1, 1));
        SceneNode el0 = new SceneNode(new Mesh(wg, material));
        // +z is left, +x is down
        double topx = -size.getHeight() / 2 + height / 2;
        el0.getTransform().setPosition(new Vector3(topx + row * height, 0.0, col == 0 ? width / 2 : -width / 2));
        el0.setName(name);
        wall.attach(el0);
    }

    public void setTransparency(boolean transparent) {
        // TODO
    }
}

/**
 * Not really a menu, just a green cube top left with deferred camera (set up in the builder).
 */
class SecondMenu implements Menu {
    Log logger;
    SceneNode hudCube;
    ReferenceScene rs;

    SecondMenu(ReferenceScene rs) {
        this.rs = rs;
        // cube at top left. Independent from camera used in the menu builder. Layer is set later by setting parent.
        // 16.12.22: But position needs to fit to cameras near/far, so its not really independent. deffered fov camera has near/far 5/6.
        // For JME this apparently doesn't matter. Also needs scale down
        Vector3 position = new Vector3(-9, 8, -24);
        position = new Vector3(-2, 1.8, -5.5);
        SceneNode cube = ModelSamples.buildCube(2, Color.LIGHTGREEN, position);
        cube.getTransform().setScale(new Vector3(0.2, 0.2, 0.2));
        cube.setName("HudCube");
        hudCube = cube;
    }

    @Override
    public SceneNode getNode() {
        return hudCube;
    }

    @Override
    public boolean checkForClickedArea(Ray ray) {
        List<NativeCollision> hits = hudCube.getHits(ray);
        if (hits.size() > 0) {
            logger.debug("hudCube was clicked");
            return true;
        }
        return false;
    }

    @Override
    public void checkForSelectionByKey(int position) {
    }

    @Override
    public void remove() {
        SceneNode.removeSceneNode(hudCube);
        hudCube = null;
        // check its gone.
        ReferenceTests.testCycledMenu(new SceneNode[]{rs.hud, rs.controlMenu});
    }
}

class MainMenuBuilder implements MenuProvider {
    ReferenceScene rs;

    MainMenuBuilder(ReferenceScene rs) {
        this.rs = rs;
    }

    @Override
    public Menu buildMenu(Camera camera) {
        // 6 columns with 3 rows each
        GuiGrid menu = GuiGrid.buildForCamera(camera, 1, 6, 3, GuiGrid.GREEN_SEMITRANSPARENT, true);
        // mid line two buttons with image and one button with icon
        menu.addButton(null, 3, 1, 1, Texture.buildBundleTexture("data", "images/river.jpg"));
        menu.addButton(null, 2, 1, 1, Texture.buildBundleTexture("data", "images/river.jpg"));
        menu.addButton(4, 1, 1, Icon.ICON_CLOSE, () -> {
            rs.menuCycler.close();
            ReferenceTests.testCycledMenu(new SceneNode[]{rs.hud, rs.controlMenu});
        });
        // bottom row: a wide button with text
        menu.addButton(2, 0, 2, new Text("Close", Color.BLUE, Color.LIGHTBLUE), () -> {
            rs.menuCycler.close();
            ReferenceTests.testCycledMenu(new SceneNode[]{rs.hud, rs.controlMenu});
        });
        return menu;
    }

    @Override
    public Transform getAttachNode() {
        // 16.12.22: For some reason (or maybe just an accident) it is attached to the main camera. Just keep it that way.
        return rs.getDefaultCamera().getCarrier().getTransform();
    }

    /*@Override
    public Camera getCamera() {
        return rs.getDefaultCamera();
    }*/
    @Override
    public Ray getRayForUserClick(Point mouselocation) {
        if (mouselocation != null) {
            return rs.getDefaultCamera().buildPickingRay(rs.getDefaultCamera().getCarrier().getTransform(), mouselocation);
        }
        //Hat keinen Avatar. Darum geht das so nicht in VR.
        Ray ray = null;//avatar.controller1.getRay();
        return ray;
    }

    @Override
    public void menuBuilt() {
        // main menu is attached to main camera, so no change here.
        ReferenceTests.testCycledMenu(new SceneNode[]{rs.hud, rs.controlMenu});
    }
}

class SecondMenuBuilder implements MenuProvider {
    ReferenceScene rs;
    SecondMenu menu;

    SecondMenuBuilder(ReferenceScene rs) {
        this.rs = rs;
    }

    @Override
    public Menu buildMenu(Camera camera) {
        menu = new SecondMenu(rs);
        return menu;
    }

    /**
     * Should use a deferred camera to have it visible always. The carrier of the deferred camera
     * is the attach point. It will also propagate the layer.
     */
    @Override
    public Transform getAttachNode() {
        //SceneNode hudCube = FovElement.buildSceneNodeForDeferredCamera(rs.getDefaultCamera());
        //hudCube.getTransform().setLayer(hudCube.getTransform().getLayer());
        //return hudCube;
        return FovElement.getDeferredCamera(rs.getDefaultCamera()).getCarrier().getTransform();
    }

    /*@Override
    public Camera getCamera() {
        return FovElement.getDeferredCamera(null);
    }*/
    @Override
    public Ray getRayForUserClick(Point mouselocation) {
        if (mouselocation != null) {
            return rs.getDefaultCamera().buildPickingRay(rs.getDefaultCamera().getCarrier().getTransform(), mouselocation);
        }
        //No avatar, so no ray from controller (VR?)
        return null;
    }

    @Override
    public void menuBuilt() {
        ReferenceTests.testCycledMenu(new SceneNode[]{rs.hud, rs.controlMenu, new SceneNode(SceneNode.findByName("HudCube").get(0))});
    }

}

