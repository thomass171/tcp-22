package de.yard.threed.engine.apps.reference;

import de.yard.threed.core.*;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.platform.*;
import de.yard.threed.engine.*;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.engine.apps.WoodenToyFactory;
import de.yard.threed.engine.geometry.Primitives;
import de.yard.threed.engine.gui.*;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.common.*;

import de.yard.threed.engine.geometry.ShapeGeometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.yard.threed.engine.test.AsyncTest;
import de.yard.threed.engine.test.MainTest;
import de.yard.threed.core.testutil.Assert;
import de.yard.threed.core.testutil.TestUtil;

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
 * b: animierte Verschiebung der gr�nen(oberen) Box von Tower1  zu Tower 2
 * v: Referenztests (war mal t). Es sollen etwa 4 FM kommen. Bei Erfolg kommt nachher ein grüner Cube, bei Fail ein roter.
 * f: enable/disable FPS Controller
 * c: enable/disable FPS Controller fuer weisse Box
 * m: Menu cyclen (z.B. mit anclickbaren Buttons). Darueber koennte man auch eine Helppage cyclen.
 * e: Effects durchcyclen
 * l: toggle hiddencube layer
 * L: cycle lightNode
 * s: cycle shading of earth (nicht fertig)
 * <p>
 * 9.3.16: Jetzt auch mit einschaltbarem FPS Controller. Dann geht aber je nach Einstellung im FPS z.B. Pickingray nicht (wegen Mausmovehandling)
 * 22.7,16: Ein Zugriff auf externe Resourcen (z.B. ueber ModelSamples) soll von hier nicht erfolgen, nur Bundled. Externe gibts im Showroom.
 * 15.9.16: Jetzt auch mit FPS Controller für die weisse Box (unabhaengig von Camera). Die Box bleibt in ihrem local space.
 * 23.9.17: Hinten links eine Lok (MA31 "Loc" statt "windturbine")
 * 23.9.17: Hud zeigt bei hit des picking ray den object name.
 * 5.12.18: VR Controller können hier nicht verwendet werden, weil es keine Avatar gibt/geben soll. ISt aber doof. TODO Controller ohne Avatar.
 * 2.5.19: linker Tower flat shaded.
 */


public class ReferenceScene extends Scene {
    static Log logger = Platform.getInstance().getLog(ReferenceScene.class);
    public ArrayList<SceneNode> towerrechts = new ArrayList<SceneNode>();
    public ArrayList<SceneNode> tower2 = new ArrayList<SceneNode>();
    public ArrayList<SceneNode> zftowerblue = new ArrayList<SceneNode>();
    public ArrayList<SceneNode> zftowerred = new ArrayList<SceneNode>();

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
    static final String MOVEBOXNAME = "rechts 2";
    Quaternion wbrotation;
    Vector3 wbposition;
    Bundle databundle;
    SceneNode locomotive;
    int modelindex = 0;
    //ein kleiner Button unten zentriert, der immer zu sehen ist und controller step macht.
    private GuiGrid controlMenu;
    // der liegt eigentlich unter der Plane und ist nur mit deferredRendering seines Layers zu sehen.
    public SceneNode hiddencube;
    Camera deferredcamera;
    boolean usedeferred = true;
    //layer fuer hiddencube. Conflicts with menu hudcube layer?
    //Wohl nicht, FovElement ist immer Layer 1
    //layer 2->9, damit es nicht zufaellig funktioniert
    int HIDDENCUBELAYER = 9;//1 << 8;
    SceneNode lightNode;
    int shading = NumericValue.SMOOTH;
    RequestType REQUEST_CLOSE = new RequestType("close");
    RequestType REQUEST_CYCLE = new RequestType("cycle");
    // Ohne VR ist inventory auch im HIDDENCUBELAYER mit derredcamera
    ControlPanel inventory;
    ControlPanel controlPanel;
    Color controlPanelBackground = new Color(128, 193, 255, 128);
    boolean vrEnabled = false;
    int lightIndex = 0;
    GeneralHandler[] lightCycle;

    @Override
    public void init(boolean forServer) {
        logger.debug("init ReferenceScene");
        databundle = BundleRegistry.getBundle("data");

        if (EngineHelper.isEnabled("argv.enableVR")) {
            vrEnabled = true;
            usedeferred = false;
        }

        //Wegen deferred in JME erst camera, dann scene
        //setupScene();
        // Der FirstPersonControls von ThreeJS ist einfach zu unruhig,
        // weil er sich staendig bewegt.
        Camera camera = getDefaultCamera();
        camera.getCarrier().getTransform().setPosition(new Vector3(0, 5, 11));
        camera.lookAt(new Vector3(0, 0, 0));
        // Zweite Camera fuer deferred rendering
        if (usedeferred) {
            // Is there any reason for using the main cameras near/far?
            //deferredcamera = new PerspectiveCamera(camera.getFov(), camera.getAspect(), camera.getNear(), camera.getFar());
            deferredcamera = Camera.createAttachedDeferredCamera(camera, HIDDENCUBELAYER, camera.getNear(), camera.getFar());
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
        // als letztes wieder zum Anfang
        tl.addEntryForLookat(new Vector3(0, 5, 11), new Vector3(0, 0, 0));

        if (!vrEnabled) {
            //controlMenu = GuiGrid.buildControlMenu(getDefaultCamera(), 1);
            controlMenu = GuiGrid.buildForCamera(getDefaultCamera(), 2, 1, 1, Color.BLACK_FULLTRANSPARENT);
            controlMenu.setName("ControlIcon");
            controlMenu.addButton(new Request(REQUEST_CYCLE), 0, 0, 1, Icon.ICON_POSITION, () -> {
                controller.step(true);
            });
            FovElement.getDeferredCamera(getDefaultCamera()).getCarrier().attach(controlMenu);
        }
    }

    /**
     * "railing" fuer loc.
     * 13.6.21: loc GLTF liegt jetzt in "data". "core" gibt es nicht mehr, stattdessen "engine"
     *
     * @return
     */
    @Override
    public String[] getPreInitBundle() {
        return new String[]{"engine", "data"};
    }

    /**
     * 2.5.19: Der linke Tower jetzt flatshaded, der rechte weiterhin unshaded.
     */
    private void setupScene() {
        movingboxgeo = buildTower("rechts", towerrechts, 4, 3, 1, new Color[]{Color.RED, new Color(1.0f, 1.0f, 0), Color.GREEN}, false);
        towerrechts.get(0).getTransform().setPosition(new Vector3(4, 0, -3));
        addToWorld(towerrechts.get(0));

        buildTower("links", tower2, 3, 2, 1, new Color[]{new Color(1.0f, 0, 1.0f), Color.WHITE}, true);
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

        initLightCycle();
        setLight();

        //20.5.16: Auch ein Hud, einfach um das bei wechselnden Camerapositionen einfach mittesten zu koennen.
        //3.12.18: Jetzt mit more far near plane???
        //7.10.19: War das nicht immer eine deferred Cam? Tja, weiss nicht. Damit raucht JME aber ab. Mit Versetztem Aufruf von getDeferredCamera gehts wohl(??).
        if (!vrEnabled) {
            FovElement.getDeferredCamera(getDefaultCamera());
            //hud = new Hud(getDefaultCamera());
            hud = Hud.buildForCamera(FovElement.getDeferredCamera(null), 0);
            hud.setText(1, "Hud");
            FovElement.getDeferredCamera(null).getCarrier().attach(hud);
        }

        // 28.4.21: Inventory versuche ich mal einfacher als FovElement. Braucht aber (ausserhalb VR) eine deferredcamera.
        if (deferredcamera != null) {
            inventory = ControlPanelHelper.buildInventoryForDeferredCamera(deferredcamera, getDimension(), controlPanelBackground, new Dimension(300, 20));
            // occupy mid third of inventory
            TextTexture textTexture = new TextTexture(Color.LIGHTGRAY);
            inventory.addArea(new Vector2(0, 0), new DimensionF(inventory.getSize().getWidth() / 3, inventory.getSize().getHeight()), null).setTexture(textTexture.getTextureForText("1884", Color.RED));
        }
        controlPanel = buildControlPanel(controlPanelBackground);
        // in front of left box, dami man mit einem teleport gut davorsteht zur Bedienung
        controlPanel.getTransform().setPosition(new Vector3(-3, 1.5, 2.5));
        addToWorld(controlPanel);

        //Ein Ground. Nicht als BasicMaterial, weil das je nach Platform keine Schatten darstellt.
        Material goundmat = Material.buildLambertMaterial(new Color(0.7f, 0.7f, 0.7f));
        SceneNode ground = new SceneNode(new Mesh(ShapeGeometry.buildPlane(16, 32, 1, 1), goundmat, true, true));
        ground.getTransform().setPosition(new Vector3(0, -2, 0));
        ground.setName("Ground");
        addToWorld(ground);

        //eine Mazewand mit Normalmap. 25.9.19: Die Normals in der geo sind trotzdem erforderlich (wegen tangent space).
        GenericGeometry wallgeo = new GenericGeometry(Primitives.buildPlaneGeometry(0.7f, 1.1f, 2, 1));
        Material mat = buildWallMaterial(false);

        SceneNode wall = new SceneNode(new Mesh(wallgeo, mat));
        //aufrecht mittig zwischen die Boxen und gedreht, damit die Wall nicht auf der Seite steht. 
        wall.getTransform().rotateX(new Degree(90));
        wall.getTransform().rotateY(new Degree(-90));
        wall.setName("Wall");
        addToWorld(wall);

        buildPhotoalbumPage();

        // loc nur zum Test des async gltf Ladens, aber ohne Animation um keine Abhaengigkeit zu FG zu haben.
        // 5.1.17: das Laden geht hier erstmal mit dem eigenen Loader. Den Platform Loader verwendet nachher das Laden ueber key a.
        // Eine Rotation or scale of loc isType not needed.
        EngineHelper.buildNativeModel(new BundleResource(BundleRegistry.getBundle("data"), "models/loc.gltf"), null, (result) -> {
            locomotive = new SceneNode(result.getNode());
            if (locomotive != null) {
                //Vector3 scale = new Vector3(1.5f, 1.5f, 1.5f);
                //locomotive.getTransform().setScale(scale);
                //locomotive.getTransform().setRotation(Quaternion.buildRotationX(new Degree(-90)));
                locomotive.getTransform().setPosition(new Vector3(-5, 0, -10));
                addToWorld(locomotive);
            } else {
                logger.error("loc not loaded");
            }
        }, EngineHelper.LOADER_USEGLTF);

        //12.2.18: Eine Line. Die Position ist vorläufig. Ist auch eigentlich egal.
        SceneNode line = ModelSamples.buildLine(new Vector3(-3, 3, -5), new Vector3(3, 3, -15), Color.BLUE);
        addToWorld(line);

        //einen Cube ohne Normals
        SimpleGeometry cuboid = Primitives.buildBox(0.5f, 0.5f, 0.5f);
        cuboid = new SimpleGeometry(cuboid.getVertices(), cuboid.getIndices(), cuboid.getUvs(), null);
        cubeWithoutNormals = new SceneNode(new Mesh(cuboid, Material.buildBasicMaterial(Color.BLUE)));
        cubeWithoutNormals.setName("CubeWithoutNormals");
        //plane liegt auf -2
        cubeWithoutNormals.getTransform().setPosition(new Vector3(5, 0.5f, 0));
        addToWorld(cubeWithoutNormals);

        buildHiddenCubes();

        menuCycler = new MenuCycler(new MenuProvider[]{new MainMenuBuilder(this), new SecondMenuBuilder(this)});

        //eine Plane für Canvas
        GenericGeometry canvasgeo = new GenericGeometry(Primitives.buildPlaneGeometry(0.7f, 1.1f, 2, 1));
        mat = buildWallMaterial(false);

        NativeCanvas canvas = Platform.getInstance().buildNativeCanvas(300, 200);
        SceneNode canvasNode = new SceneNode(new Mesh(canvasgeo, Material.buildBasicMaterial(new Texture(canvas))));
        // vor der linken Box, damit es nach dem ersten step ganz gut sehen kann.
        canvasNode.getTransform().rotateX(new Degree(90));
        canvasNode.getTransform().rotateY(new Degree(-90));
        canvasNode.getTransform().setPosition(new Vector3(-2.0, 1.5, 2.5));
        canvasNode.setName("Canvas");
        addToWorld(canvasNode);

        logger.debug("setupScene completed");
    }

    // panel with 3 rows (dimesion 0.6x0.3)
    // rows must be quite narrow to have a proper property panel with text area large enough
    private static double PropertyControlPanelWidth = 0.6;
    private static double PropertyControlPanelRowHeight = 0.1;
    private static double PropertyControlPanelMargin = 0.005;

    private static ControlPanel buildControlPanel(Color backGround) {
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
        }).setIcon(Icon.ICON_POSITION);


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
        //22.3.17: Mal kein AmbientLight mehr, um erstmal DirectionalLight zu ergründen.
        //Das DirectionalLight jetzt schräg von vorne  scheinen lassen (45 Grad). Dann ist die Pyramide gut beleuchtet
        //und es gibt einige Schatten nach unten. Aber nicht zu sehr in Blickrichtung der Camera, sonst sieht man keine Schatten.
        //lightNode = new PointLight(Color.WHITE);
        //lightNode.setPosition(new Vector3(0, 3, 2));

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
     * 25.9.19: Hat river ueberhaupt einen brauchbaren Alpha Channel?
     *
     * @return
     */
    private Material buildWallMaterial(boolean transparent) {
        Material mat;
        // river, um besser Orientierung pruefen zu können.
        mat = Material.buildPhongMaterialWithNormalMap(Texture.buildBundleTexture("data", "images/river.jpg"),
                Texture.buildNormalMap(new WoodenToyFactory().buildWallNormalMap(6).image), transparent);
        return mat;
    }

    /**
     * Die Verkleinerung der aufstehenden Bloecke mit scale, um das auch zu nutzen.
     * Prinzipiell koennte man natuerlich auch die base* Angaben verkleiner.
     * <p/>
     * Liefert die Geometrie zurueck.
     */
    private Geometry buildTower(String basename, ArrayList<SceneNode> towerlist, double baselength, double basewidth,
                                double baseheight, Color[] color, boolean flatshaded) {
        SceneNode tower = null;
        //Mesh basetower = null;
        double scale = 1;
        Geometry cuboid = null;

        for (int i = 0; i < color.length; i++) {
            cuboid = Geometry.buildCube(baselength, baseheight, basewidth);

            Material mat;
            if (flatshaded) {
                mat = Material.buildPhongMaterial(color[i], NumericValue.FLAT);
            } else {
                mat = Material.buildBasicMaterial(color[i]);
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
     * 21.10.19 bischen kleiner
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
        //setlayer ist rekursiv
        hiddencube.getTransform().setLayer(HIDDENCUBELAYER);

    }

    /**
     * Das ist der Würfel aus (aus http://solutiondesign.com/blog/-/blogs/webgl-and-three-js-texture-mappi-1/) mit einer
     * rein farbigen Seite.
     */
    private void buildMultiMaterialCube(double size) {
        multimatcube = ModelSamples.buildTexturedCube(size);
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
        SceneNode needle = ModelSamples.buildCompassNeedle(0.3f, 0.1f).buildModel(null);
        needle.getTransform().setPosition(new Vector3(0, 0, 1.01f));
        earth.attach(needle);
        // und eine links halbhoch, wo manchmal Europa ist.
        needle = ModelSamples.buildCompassNeedle(0.3f, 0.1f).buildModel(null);
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
        HashMap<String, NativeTexture> map = new HashMap<String, NativeTexture>();
        map.put("texture0", textures[0].texture);
        map.put("texture1", textures[1].texture);
        mat = Material.buildCustomShaderMaterial(map, Effect.buildPhotoalbumEffect());
        //mat = Material.buildPhongMaterial(textures[0]);
        //3.5.21 eine wall by simple plane above
        SceneNode simplewall = new SceneNode(new Mesh(Primitives.buildSimpleXYPlaneGeometry(1.1, 1.8, new ProportionalUvMap()), mat));
        simplewall.getTransform().setPosition(new Vector3(0, 2, -2));
        simplewall.setName("Photoalbum XY plane Wall");
        addToWorld(simplewall);
    }

    public SceneNode getMovingbox() {
        return towerrechts.get(2);
    }

    @Override
    public void update() {
        double tpf = getDeltaTime();
        controller.update(tpf);

        Point mouselocation = Input.getMouseClick();
        if (mouselocation != null) {
            Ray mousePickingRay = getDefaultCamera().buildPickingRay(getDefaultCamera().getCarrier().getTransform(), mouselocation);
            controlMenu.checkForClickedArea(mousePickingRay);
            controlPanel.checkForClickedArea(mousePickingRay);
        }

        if (Input.GetKeyDown(KeyCode.A)) {
            // erstmal nur so Q&D. Kann/Soll noch ausgebaut werden. Genielamp ist z.Z. Bundled.
            // MA31: genie lamp soll raus. lieber was eigenes, aber im selben Modul?
            String name = "ToggleNode";
            SceneNode n = SceneNode.findFirst(name);
            if (n == null) {
                switch (modelindex) {
                    case 0:
                        //15.6.21 n = ModelSamples.buildGenieLamp();
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
        if (Input.GetKeyDown(KeyCode.B)) {
            logger.debug("b key was pressed. currentdelta=" + tpf);
            moveBox();
        }
        menuCycler.update(mouselocation);

        //(V)alidate statt (T)est
        if (Input.GetKeyDown(KeyCode.V)) {
            // Die Reihenfolge der Tests hat keine Bedeutung. Man kann belieibg tauschen falls einzelne mal scheitern.
            // 23.1.18: Maintest dazu
            logger.debug("v key was pressed. currentdelta=" + tpf);
            MainTest.runTest(null);
            ReferenceTests.testPyramideBackLeftFront(pyramideblf);
            ReferenceTests.mvpTest(getMainCamera(), getDimension(), usedeferred);
            ReferenceTests.testOriginalScale(towerrechts.get(1));
            ReferenceTests.testParent(towerrechts.get(1), towerrechts.get(0));
            ReferenceTests.testParent(towerrechts.get(2), towerrechts.get(1));
            ReferenceTests.testExtracts(towerrechts.get(2));
            ReferenceTests.testIntersect(towerrechts, towerrechts.get(2));
            ReferenceTests.testMovingboxView(this);
            ReferenceTests.testRayFromFarAway(getDimension(), this);
            ReferenceTests.rayTest(getDimension(), getMainCamera());
            ReferenceTests.testFind(this, towerrechts.get(2));
            ReferenceTests.testGetParent(this, towerrechts.get(2));
            ReferenceTests.testFindNodeByName(this);
            ReferenceTests.testJson();
            logger.info("tests completed");
            //Der AsyncTest provoziert Fehler zum Test, so dass geloggte error Meldungen dabei korrekt sind.
            new AsyncTest().runtest(this);

        }
        if (Input.GetKeyDown(KeyCode.F)) {
            if (fps == null) {
                fps = new FirstPersonController(getMainCamera().getCarrier().getTransform());
            } else {
                fps = null;
            }
        }
        if (fps != null) {
            fps.update(tpf);
        }
        if (Input.GetKeyDown(KeyCode.C)) {
            //white box FPC
            if (fpswb == null) {
                SceneNode wb = tower2.get(1);
                wbrotation = wb.getTransform().getRotation();
                wbposition = wb.getTransform().getPosition();
                fpswb = new FirstPersonController(wb.getTransform());
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


        if (Input.GetKeyDown(KeyCode.E)) {
            cycleEffects();
        }
        if (Input.GetKeyDown(KeyCode.L)) {
            if (Input.GetKey(KeyCode.Shift)) {
                //cycle lightNode
                lightIndex++;
                if (lightIndex >= lightCycle.length) {
                    lightIndex = 0;
                }
                setLight();
            } else {
                int layer = hiddencube.getTransform().getLayer();
                logger.debug("hiddencube.layer=" + layer);
                hiddencube.getTransform().setLayer(((layer == HIDDENCUBELAYER)) ? 0 : HIDDENCUBELAYER);
            }
        }
        if (Input.GetKeyDown(KeyCode.S)) {
            cycleShading();
        }
    }

    private void cycleShading() {
        shading++;
        if (shading > NumericValue.FLAT) {
            shading = 0;
        }
        addOrReplaceEarth();
    }

    private void cycleEffects() {

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
            List<NativeCollision> intersects = pickingray.getIntersections(towerrechts/*world*/, true);
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
        if ((mouselocation = Input.getMouseClick()) != null) {
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

        SceneNode movebox = towerrechts.get(2);
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
                () -> setDefaultLight(false), () -> {
            // 1: ambient
            setDefaultLight(true);
        },
                // 2: point light
                () -> {
                    PointLight pointLight = new PointLight(Color.WHITE);
                    this.lightNode = addLightToWorld(pointLight);
                },
                // 3: no light, just dark
                () -> {
                }};
    }
}

class ReferenceTests {
    static Log logger = Platform.getInstance().getLog(ReferenceTests.class);

    /**
     * Pickingray aus der Default Camera Position (0,5,11) genau in die Mitte der Scene (mousevector =0,0,0.5)
     */
    public static void rayTest(Dimension dim, Camera camera) {
        logger.info("rayTest");
        Ray ray = camera.buildPickingRay(camera.getCarrier().getTransform(), new Point(dim.width / 2, dim.height / 2)/*, dim*/);
        logger.debug("ray origin=" + ray.getOrigin().dump("") + " for dimension " + dim.toString());
        logger.debug("ray direction=" + ray.getDirection().dump("") + " for dimension " + dim.toString());
        logger.debug("viewer matrix=" + camera.getViewMatrix().dump("\n"));
        logger.debug("projection matrix=" + camera.getProjectionMatrix().dump("\n"));
        // Bei Blickrichtung 0,0,0 sind die Werte plausibel, x ist nichts, y leicht nach unten und z halt nach hinten
        Vector3 expecteddirection = new Vector3(0, -0.4138f, -0.91f);
        double distance = Vector3.getDistance(expecteddirection, ray.getDirection());
        if (distance > 0.1f) {
            Assert.fail("test failed: expecteddirection deviation =" + distance);
        }
        TestUtil.assertVector3("ray origin", new Vector3(0, 5, 11), ray.getOrigin());

        // Ein Ray etwas schraeg nach rechts unten (die Referenzwerte beziehen sich auf dim 800x600)
        ray = camera.buildPickingRay(camera.getCarrier().getTransform(), new Point(dim.width / 2 + 90, dim.height / 2 + 40)/*, dim*/);
        logger.debug("2.ray direction=" + ray.getDirection() + " for dimension " + dim);
        logger.debug("2.viewer matrix=" + camera.getViewMatrix().dump("\n"));
        logger.debug("2.projection matrix=" + camera.getProjectionMatrix().dump("\n"));
        expecteddirection = new Vector3(0.12313132f, -0.36020958f, -0.92470956f);
        distance = Vector3.getDistance(expecteddirection, ray.getDirection());
        if (distance > 0.1f) {
            if (!isUnity()) {
                // Die Groesse 800x600 ist bei Unity nicht so vorhanden, dann passen die Refwerte auch nicht.
                Assert.fail("test failed: expecteddirection deviation =" + distance);
            }
        }
    }

    public static void mvpTest(Camera camera, Dimension screensize, boolean usedeferred) {
        logger.info("mvpTest");
        TestUtil.assertVector3("default camera position (carrier)", new Vector3(0, 5, 11), camera.getCarrierPosition());

        // Die Referenzwerte hängen von den camra Einstellungen (z.B. aspect) ab und muessen deshalb berechnet werden.
        Matrix4 projectionmatrixexpected = Camera.createPerspectiveProjection(camera.getFov(), camera.getAspect(), camera.getNear(), camera.getFar());
        Matrix4 pm = camera.getProjectionMatrix();
        logger.debug("projection matrix=\n" + pm.dump("\n"));
        //24.9.19: Der Test scheitert, wenn das Fenster nicht 4:3 ist (800x600), z.B. in WebGL.
        TestUtil.assertMatrix4(projectionmatrixexpected, pm);

        // Die Referenzwerte stammen aus ThreeJS (default camera position)
        Matrix4 viewmatrixexpected = new Matrix4(
                1, 0, 0, 0,
                0, 0.91f, -0.414f, 0,
                0, 0.414f, 0.91f, -12.083f,
                0, 0, 0, 1);
        Matrix4 vm = camera.getViewMatrix();
        logger.debug("viewer matrix=\n" + vm.dump("\n"));
        TestUtil.assertMatrix4(viewmatrixexpected, camera.getViewMatrix());

        Quaternion expectedcamrot = new Quaternion(-0.212f, 0, 0, 0.977f);
        Matrix4 rm;
        rm = new Matrix4();
        rm = Matrix4.buildRotationMatrix(expectedcamrot);
        logger.debug("rm=" + rm.dump("\n"));
        logger.debug("rmi=" + rm.getInverse().dump("\n"));
        double[] rmiangles = new double[3];
        rm.getInverse().extractQuaternion().toAngles(rmiangles);
        logger.debug("rmiangles=" + rmiangles[0] + " " + rmiangles[1] + " " + rmiangles[2]);
        logger.debug("rmi gegenprobe1=" + Matrix4.buildRotationXMatrix(Degree.buildFromRadians(rmiangles[0])).dump("\n"));
        Quaternion camrot = camera.getCarrier().getTransform().getRotation();
        logger.debug("camrot=" + camrot.dump(""));
        double[] angles = new double[3];
        camrot.toAngles(angles);
        logger.debug("camrot angles=" + angles[0] + " " + angles[1] + " " + angles[2]);
        TestUtil.assertEquals("aspect", (double) screensize.width / screensize.height, camera.getAspect());
        TestUtil.assertEquals("fov", Settings.defaultfov, camera.getFov());
        TestUtil.assertEquals("far", 50/*Settings.defaultfar*/, camera.getFar());
        TestUtil.assertEquals("near", Settings.defaultnear, camera.getNear());
        Matrix4 cameraworldmatrix = camera.getWorldModelMatrix();
        TestUtil.assertVector3("extracted camera position scale", new Vector3(0, 5, 11), cameraworldmatrix.extractPosition());
        TestUtil.assertEquals("camera.name", "Main Camera", camera.getName());
        TestUtil.assertEquals("camera.carrier.name", "Main Camera Carrier", camera.getCarrier().getName());
        TestUtil.assertNotNull("camera.parent", camera.getCarrier().getParent());
        TestUtil.assertEquals("camera.parent.name", "World", camera.getCarrier().getParent().getName());
        //Wieso 2? Evtl. wegen GUI?. 7.10.19: Die default und die vom HUD (Layer 1 deferred). Und die xplizite deferred (layer 2) dazu.
        TestUtil.assertEquals("cameras", 2 + ((usedeferred) ? 1 : 0), AbstractSceneRunner.instance.getCameras().size());

        if (usedeferred) {
            List<Transform> camchildren = camera.getCarrier().getTransform().getChildren();
            SceneNode secondcarrier = null;
            for (int i = 0; i < camchildren.size(); i++) {
                logger.debug("camcarrier child " + i + ":" + camchildren.get(i).getSceneNode().getName());
                if (StringUtils.startsWith(camchildren.get(i).getSceneNode().getName(), "deferred")) {
                    secondcarrier = camchildren.get(i).getSceneNode();
                }
            }
            // ist 5, weil FOV, gui, button, maincamera auch drin sind, und dann noch deferred cam
            // 3.12.18: nur noch drei, fov gui button sind an eigener camera, dafuer aber hud carrier
            // 24.9.19: Aber der main carrier ist doch nicht sein eigener Child. Duerften doch nur zwei sein: deferred carrier und hud carrier. JME darf nicht seine
            // CameraNode mitzaehlen (und Unity seine Camera). Die Camera selber ist ja kein Child, sondern Component.
            // 11.11.19: Also bleiben Hud und deferred-camera Carrier, obwohl es wieder fraglich erscheint, das main camera nicht dabei ist.
            // Ich glaube, ich nehme die MainCam wieder auf (2->3). 15.11.19: Doch wieder nicht, in WebGl führt dazu Kruecken.
            TestUtil.assertEquals("camera children", 2, camchildren.size());
            SceneNode hudcarrier = FovElement.getDeferredCamera(null).getCarrier();
            // am Hud-carrier sind Hud,control//button und?? Hat der mal die Camera mitgezaehlt? Ich komm nur noch auf 2. 11.11.19: Wieder 3 durch mitzaehlen Camera
            List<Transform> hudcarrierchildren = hudcarrier.getTransform().getChildren();
            for (int i = 0; i < hudcarrierchildren.size(); i++) {
                logger.debug("hudcarrierchildren child " + i + ":" + hudcarrierchildren.get(i).getSceneNode().getName());
            }
            TestUtil.assertEquals("hud carrier children", 2/*7.10.19 3*/, hudcarrier.getTransform().getChildren().size());

            TestUtil.assertNotNull("getSecond carrier", secondcarrier);
            TestUtil.assertVector3("getSecond camera position", new Vector3(0, 0, 0), secondcarrier.getTransform().getPosition());
            TestUtil.assertQuaternion("getSecond camera rotation", new Quaternion(), secondcarrier.getTransform().getRotation());
            Camera scam = secondcarrier.getCamera();
            TestUtil.assertNotNull("getSecond carriers camera not set", scam);
            TestUtil.assertEquals("deferred camera name", "deferred-camera", scam.getName());
            logger.debug("deferred asserted");
        }
    }

    public static void testOriginalScale(SceneNode box) {
        logger.debug("box.scale=" + box.getTransform().getScale());
        Vector3 expectedscale = new Vector3(0.5f, 0.5f, 0.5f);
        if (Vector3.getDistance(expectedscale, box.getTransform().getScale()) > 0.1f) {
            throw new RuntimeException("test expectedscale failed");
        }
        /*if (box.object3d.getScale().getX() != 0.5f) {
            throw new RuntimeException("test expectedscale failed");
        }*/
    }

    public static void testParent(SceneNode m, SceneNode expectedparent) {
        if (m.getTransform().getParent() == null) {
            throw new RuntimeException("parent isType null");
        }
        SceneNode p = m.getTransform().getParent().getSceneNode();

        if (!p.getName().equals(expectedparent.getName())) {
            throw new RuntimeException("unexpected parent " + expectedparent.getName() + " of " + m.getTransform().getParent().getSceneNode().getName());
        }

    }

    public static void testExtracts(SceneNode movebox) {
        Matrix4 currentmodel = (movebox.getTransform().getLocalModelMatrix());
        logger.debug("currentmodel m=\n" + currentmodel.dump("\n"));
        Matrix4 modelexpected = new Matrix4(0.5f, 0, 0, 0,
                0, 0.5f, 0, 0.75f,
                0, 0, 0.5f, 0,
                0, 0, 0, 1);
        TestUtil.assertMatrix4(modelexpected, currentmodel);

        Vector3 scale = currentmodel.extractScale();
        TestUtil.assertVector3("movebox scale", new Vector3(0.5f, 0.5f, 0.5f), scale);

        // Die movebox hat ja keine eigene Rotation
        Matrix3 rotation = currentmodel.extractRotation();
        Matrix3 rotationexpected = new Matrix3(
                1, 0, 0,
                0, 1, 0,
                0, 0, 1);
        TestUtil.assertMatrix3("movebox rotation", rotationexpected, rotation);
        Quaternion rot = currentmodel.extractQuaternion();
        Quaternion rotexpected = new Quaternion(0, 0, 0, 1);
        TestUtil.assertQuaternion("movebox rot", rotexpected, rot);
    }

    public static void testIntersect(ArrayList<SceneNode> towerrechts, SceneNode movingbox) {
        logger.info("intersectionTest");

        // Jetzt ein Ray, der die grosse rote Box trifft und einer für die moving box.
        // Warum jeweils nur eine intersection kommt, mag ThreeJS spezifisch sein.

        Ray raycasterredbox;
        List<NativeCollision> intersects;
        // Geht in Unity trotz groesserer Toleranzen nicht, evtl. weil Ray Origin im screen statt in camera ist?
        // 2.3.17: Jetzt durch Nutzung Rayhelper aber nicht mehr. Allerdings dürften diese fixen Werte nicht zum Unity aspect passen und damit scheitern.
        if (!isUnity()) {
            raycasterredbox = new Ray(new Vector3(0, 5, 11), new Vector3(0.23f, -0.37f, -0.9f));
            intersects = raycasterredbox.getIntersections(towerrechts.get(0), true);
            //liefert 1 oder 2 
            if (intersects.size() == 0) {
                Assert.fail("no red box intersection found(1)");
            }
            //wenn er zwei findet, ist etwas unklar, welchen zu pruefen. Einer muss es sein.
            boolean boxHit = false;
            for (int index = 0; index < intersects.size(); index++) {
                logger.debug("redbox.intersect=" + (intersects.get(index).getPoint()).dump(" "));
                if (Vector3.getDistance(new Vector3(3.19f, -0.14f, -1.5f), intersects.get(index).getPoint()) < 0.1) {
                    boxHit = true;
                }
            }
            if (!boxHit) {
                Assert.fail("redbox invalid intersection(s)");
            }

            Ray raycastermovingbox = new Ray(new Vector3(0, 5, 11), new Vector3(0.26f, -0.25f, -0.93f));
            intersects = raycastermovingbox.getIntersections(movingbox, true);
            //liefert 1 oder 2 
            if (intersects.size() == 0) {
                Assert.fail("no moving box intersection found");
            }
            logger.debug("movingbox.intersect=" + (intersects.get(0).getPoint()).dump(" "));
            // Bei JME und Unity kommt es zu deutlichen Rundungsfehlern oder einfach anderen Resultaten, 
            // evtl. weil Ray Origin im screen statt in camera ist?
            // Darum groessere Toleranz. Ob das so ganz richtig ist, muss sich noch zeigen. Ist aber erstmal besser als nichts.
            // Bei WebGl ist das der Punkt 0. Alles irgendwie nicht konsistent.
            int pindex = 1;
            if (isWebGl()) {
                pindex = 0;
            }
            TestUtil.assertVector3("movingbox.intersect", new Vector3(3.9f, 1.25f, -2.95f), (intersects.get(pindex).getPoint()), 0.5f);
        }

        // Und jetzt einer von ganz weit weg, der die rote Box im Center treffen muesste.
        Vector3 redboxpos = new Vector3(4, 0, -3);
        // Unity trifft bei 1000*1000 nicht mehr, nur bis 1000*100.
        double len = 1000 * 100;
        Vector3 campos = new Vector3(4 * len, 5 * len, 11 * len);
        raycasterredbox = new Ray(campos, redboxpos.subtract(campos));
        intersects = raycasterredbox.getIntersections(towerrechts.get(0), true);
        //liefert 1 oder 2 
        if (intersects.size() == 0) {
            Assert.fail("no red box intersection found(2)");
        }
        TestUtil.assertEquals("name", "rechts 0", intersects.get(0).getSceneNode().getName());
        logger.debug("redbox.intersect=" + (intersects.get(0).getPoint()).dump(" "));
        intersects = raycasterredbox.getIntersections();
        //liefert 1 oder 2 (Ground doch vielleicht auch?)
        if (intersects.size() == 0) {
            Assert.fail("no red box intersection found(3)");
        }
        assertIntersection(intersects, "rechts 0");
    }

    private static void assertIntersection(List<NativeCollision> intersects, String expectedname) {
        for (NativeCollision nc : intersects) {
            if (nc.getSceneNode() == null) {
                Assert.fail("no mesh");
            }
            if (nc.getSceneNode().getName() == null) {
                Assert.fail("no mesh name");
            }
            if (nc.getSceneNode().getName().equals(expectedname))
                return;
        }
        Assert.fail("expected intersection found:" + expectedname);
    }

    public static void testMovingboxView(ReferenceScene rs) {
        logger.info("testMovingboxView");
        rs.controller.stepTo(1);

        Matrix4 mboxworldmatrix = (rs.getMovingbox().getTransform().getWorldModelMatrix());
        logger.debug("moving box worldmatrix=\n" + mboxworldmatrix.dump("\n"));
        Matrix4 expectedmboxworldmatrix = new Matrix4(
                0.25f, 0, 0, 4,
                0, 0.25f, 0, 1.125f,
                0, 0, 0.25f, -3,
                0, 0, 0, 1);
        TestUtil.assertMatrix4(expectedmboxworldmatrix, mboxworldmatrix);

        // Die Referenzwerte stammen aus ThreeJS(?) 
        Matrix4 cameraworldmatrix = rs.getMainCamera().getWorldModelMatrix();
        logger.debug("camera worldmatrix=\n" + cameraworldmatrix.dump("\n"));
        Matrix4 expectedcameraworldmatrix = new Matrix4(
                0, 0, 0.25f, 4.625f,
                0, 0.25f, 0, 1.65f,
                -0.25f, 0, 0, -3,
                0, 0, 0, 1);
        TestUtil.assertMatrix4(expectedcameraworldmatrix, cameraworldmatrix);

        // Die Referenzwerte stammen aus ThreeJS 
        Matrix4 viewmatrixexpected = new Matrix4(
                0, 0, -4, -12,
                0, 4, 0, -6.6f,
                4, 0, 0, -18.5f,
                0, 0, 0, 1);
        Matrix4 vm = rs.getMainCamera().getViewMatrix();
        logger.debug("viewer matrix moving box=" + vm.dump("\n"));
        TestUtil.assertMatrix4(viewmatrixexpected, vm);

        TestUtil.assertEquals("camera.name", "Main Camera", rs.getMainCamera().getName());
        TestUtil.assertEquals("camera.parent.name", "Main Camera Carrier", rs.getMainCamera().getCarrier().getName());
        TestUtil.assertEquals("camera.parent.name", "rechts 2", rs.getMainCamera().getCarrier().getParent().getName());

        // zurueck auf Anfang
        rs.controller.stepTo(rs.controller.viewpointList.size() - 1);

        //4.11.19 auch mal setMesh hier testen
        Mesh earthmesh = rs.earth.getMesh();
        TestUtil.assertNotNull("earth.mesh", earthmesh);
        rs.earth.setMesh(earthmesh);
        //12.11.19: und auch getLayer()
        int layer = rs.hiddencube.getTransform().getLayer();
        TestUtil.assertEquals("hiddencube.layer", rs.HIDDENCUBELAYER, layer);
        layer = rs.deferredcamera.getLayer();
        TestUtil.assertEquals("deferredcamera.layer", rs.HIDDENCUBELAYER, layer);
    }

    public static void testRayFromFarAway(Dimension dim, ReferenceScene rs) {
        logger.info("testRayFromFarAway");
        rs.controller.stepTo(6);
        //Nicht per Rayhelper, der ist ja in allen Platformen der gleiche
        //RayHelper rayhelper = new RayHelper(rs.getMainCamera().getNativeCamera());
        Ray pickingray = rs.getMainCamera().buildPickingRay(rs.getMainCamera().getCarrier().getTransform(), new Point(dim.width / 2, dim.height / 2));

        // die direction muss mich bei richiger LAenge wieder in den Ursparung fuehren.
        Vector3 camposition = rs.getMainCamera().getCarrierPosition();
        // bin ich ueberhaupt richtig
        logger.debug("camposition=" + camposition);
        TestUtil.assertVector3("", new Vector3(50000, 30000, 20000), camposition);
        Vector3 camworldposition = rs.getMainCamera().getWorldModelMatrix().transform(camposition);
        //22.3.18: Keine Ahnung warum die worldmodel matrix anders ist
        logger.debug("camworldposition=" + camworldposition);
        TestUtil.assertVector3("", new Vector3(71236f, 65941, -25357), camworldposition, 0.5f);
        //TODO und wsarum komme ich nicht nach 0,0,0? Und Riesentoleranz fuer ThreeJS? Aber alle Platformen sind sich da einig.
        Vector3 target = camworldposition.add(pickingray.getDirection().multiply(camworldposition.length()));
        TestUtil.assertVector3("target", new Vector3(-10758, 18599, -58545), target, 2000f);
        // zurueck auf Anfang
        rs.controller.stepTo(rs.controller.viewpointList.size() - 1);
    }


    public static void testPyramideBackLeftFront(SceneNode pyramideblf) {
        //Referenzwerte aus ThreeJS
        Matrix4 pyramidworldmatrix = (pyramideblf.getTransform().getWorldModelMatrix());
        logger.debug("pyramid worldmatrix=\n" + pyramidworldmatrix.dump("\n"));
        Matrix4 expectedpyramidworldmatrix = new Matrix4(
                0, -1, 0, 1.5f,
                0, 0, 1, 0,
                -1, 0, 0, 3,
                0, 0, 0, 1);
        TestUtil.assertMatrix4(expectedpyramidworldmatrix, pyramidworldmatrix);

        // Die local Matrix ist gleich der world matrix.
        Matrix4 pyramidlocalmatrix = (pyramideblf.getTransform().getLocalModelMatrix());
        logger.debug("pyramid localmatrix=\n" + pyramidlocalmatrix.dump("\n"));
        Matrix4 expectedpyramidlocalmatrix = new Matrix4(
                0, -1, 0, 1.5f,
                0, 0, 1, 0,
                -1, 0, 0, 3,
                0, 0, 0, 1);
        TestUtil.assertMatrix4(expectedpyramidlocalmatrix, pyramidlocalmatrix);

        Quaternion pyramidrotation = pyramideblf.getTransform().getRotation();
        logger.debug("pyramid rotation=\n" + pyramidrotation.dump(" "));
        Quaternion expectedrotation = new Quaternion(-0.5f, 0.5f, 0.5f, 0.5f);
        TestUtil.assertQuaternion("pyramidrotation", expectedrotation, pyramidrotation);
    }

    public static boolean isUnity() {
        boolean isunity = Platform.getInstance().getName().equals("Unity");
        return isunity;
    }

    public static boolean isWebGl() {
        boolean isWebGl = Platform.getInstance().getName().equals("WebGL");
        return isWebGl;
    }

    public static void testFind(ReferenceScene rs, SceneNode movebox) {
        SceneNode mb = new SceneNode(Platform.getInstance().findSceneNodeByName(ReferenceScene.MOVEBOXNAME).get(0));
        TestUtil.assertNotNull("find movebox", mb);
    }

    public static void testGetParent(ReferenceScene referenceScene, SceneNode movingbox) {
        SceneNode parent = movingbox.getTransform().getParent().getSceneNode();
        parent = parent.getTransform().getParent().getSceneNode();
        TestUtil.assertEquals("parent name", "rechts 0", parent.getName());
        parent = parent.getTransform().getParent().getSceneNode();
        TestUtil.assertEquals("parent name", "World", parent.getName());
        Transform tparent = parent.getTransform().getParent();
        TestUtil.assertNull("world parent", tparent);
    }

    /**
     * Test, ob die Platform das mit den Childs richtig macht (z.B. jme meshholder)
     *
     * @param referenceScene
     */
    public static void testFindNodeByName(ReferenceScene referenceScene) {
        // Einfach erstmal etwas suchen, was es nicht gibt). Vorher einen Dump.
        String graph = ReferenceScene.dumpSceneGraph();
        logger.debug("\n" + graph);
        TestUtil.assertTrue("World", StringUtils.startsWith(graph, "World"));
        for (SceneNode n : referenceScene.towerrechts) {
            n.findNodeByName("xxxccvv", true);
        }
        for (SceneNode n : referenceScene.tower2) {
            n.findNodeByName("xxxccvv", true);
        }

        List<NativeSceneNode> nodes = SceneNode.findByName("models/loc.gltf");
        TestUtil.assertEquals("number loc", 1, nodes.size());
        // children are two level below
        NativeSceneNode childNode = nodes.get(0).getTransform().getChildren().get(0).getSceneNode();
        childNode = childNode.getTransform().getChildren().get(0).getSceneNode();
        TestUtil.assertEquals("number loc children", 8, childNode.getTransform().getChildren().size());

        SceneNode rechts1 = new SceneNode(referenceScene.towerrechts.get(0).findNodeByName("rechts 1", true).get(0));
        TestUtil.assertNotNull("", rechts1);
        TestUtil.assertNotNull("", rechts1.getTransform().getParent());
        // wenn das mesh und name in der Original Node enthalten ist, muss es auch in der find Instanz sein.
        TestUtil.assertNotNull("mesh", referenceScene.towerrechts.get(1).getMesh());
        TestUtil.assertEquals("name", "rechts 1", referenceScene.towerrechts.get(1).getName());
        TestUtil.assertEquals("children rechts 1", 1, referenceScene.towerrechts.get(1).getTransform().getChildren().size());
        TestUtil.assertNotNull("mesh", rechts1.getMesh());
        TestUtil.assertEquals("name", "rechts 1", rechts1.getName());
        TestUtil.assertEquals("children", 1, rechts1.getTransform().getChildren().size());

    }

    /**
     * GWT JsonParser doesn't like line breaks. TODO add test?
     */
    public static void testJson() {
        String jsonString = "{" +
                JsonHelper.buildProperty("a", "b") + "," +
                JsonHelper.buildProperty("c", "\"d") +
                "}";

        logger.debug("parsing " + jsonString);
        NativeJsonValue parsed = Platform.getInstance().parseJson(jsonString);
        NativeJsonObject o = parsed.isObject();
        TestUtil.assertNotNull("json.isObject", o);
        logger.debug("parsed a:" + parsed.isObject().get("a").isString().stringValue());
        logger.debug("parsed c:" + parsed.isObject().get("c").isString().stringValue());
        TestUtil.assertEquals("property a", "b", parsed.isObject().get("a").isString().stringValue());
        TestUtil.assertEquals("property c", "\"d", parsed.isObject().get("c").isString().stringValue());
    }
}

/**
 * Ein Cube oben links mit deferred camera.
 * 15.11.19: ist doch gar nicht mehr mit deferred cam(?). Doch, im Builder.
 */
class SecondMenu implements Menu {
    Log logger;
    SceneNode hudCube;

    SecondMenu(Log logger) {
        this.logger = logger;
        // Einen Cube links oben. Hier den Layer zu setzen bringt noch nichts, weil spaeter ein SetParent gemacht wird.

        //hudCube = FovElement.buildSceneNodeForDeferredCamera(camera);
        SceneNode cube = ModelSamples.buildCube(2, Color.LIGHTGREEN, new Vector3(-9, 8, -24));
        cube.setName("HudCube");
        //hudCube.attach(cube);
        //hudCube.setLayer(hudCube.getLayer());
        hudCube = cube;

    }

    /*@Override
    public Camera getMenuCamera() {
       return FovElement.getDeferredCamera(null);
    }*/

  /*      @Override
    public Request checkForClickedButton(Point mouselocation) {
        /*List<NativeCollision> hits = hudCube.getHits(mouselocation, FovElement.getDeferredCamera(null));
        if (hits.size() > 0) {
            logger.debug("hudCube was clicked");
        }* /
        return null;
    }*/

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

    /*@Override
    public void checkForSelectionByKey(int position) {

    }*/

    @Override
    public void remove() {
        //TODO check/test das er weg ist
        SceneNode.removeSceneNode(hudCube);
        hudCube = null;
    }


}

class MainMenuBuilder implements MenuProvider {
    ReferenceScene rs;

    MainMenuBuilder(ReferenceScene rs) {
        this.rs = rs;
    }

    @Override
    public Menu buildMenu() {
        // 6 Spalten und 3 Zeilen
        GuiGrid menu = GuiGrid.buildForCamera(rs.getDefaultCamera(), 1, 6, 3, GuiGrid.GREEN_SEMITRANSPARENT);
        // In der Mitte rechts ein Button mit Image
        menu.addButton(/*new Request(rs.REQUEST_CLOSE), */4, 1, 1, Icon.ICON_CLOSE, () -> {
            rs.menuCycler.close();
        });
        // und unten in der Mitte ein breiter Button mit Text
        menu.addButton(/*new Request(rs.REQUEST_CLOSE),*/ 2, 0, 2, new Text("Close", Color.BLUE, Color.LIGHTBLUE), () -> {
            rs.menuCycler.close();
        });
        menu.addButton(null, 3, 1, 1, Texture.buildBundleTexture("data", "images/river.jpg"));
        menu.addButton(null, 2, 1, 1, Texture.buildBundleTexture("data", "images/river.jpg"));

        //SceneNode testcube = ModelSamples.buildCube(0.3f, Color.LIGHTGREEN, new Vector3(0, 0, -2));
        //menu.attach(testcube);
        return menu;
    }

    @Override
    public Transform getAttachNode() {
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
}

class SecondMenuBuilder implements MenuProvider {
    ReferenceScene rs;

    SecondMenuBuilder(ReferenceScene rs) {
        this.rs = rs;
    }

    @Override
    public Menu buildMenu() {
        return new SecondMenu(ReferenceScene.logger);
    }

    /**
     * Was soll das eigentlich so genau? Ist as OK, das Menu an den Carrier zu haengen?
     *
     * @return
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
        //Hat keinen Avatar
        Ray ray = null;//avatar.controller1.getRay();
        return ray;
    }

}

