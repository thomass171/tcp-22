package de.yard.threed.engine.apps;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.Color;
import de.yard.threed.core.Degree;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.engine.DirectionalLight;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.Light;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.ModelFactory;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.avatar.AvatarPmlFactory;
import de.yard.threed.engine.avatar.VehiclePmlFactory;
import de.yard.threed.core.geometry.Primitives;
import de.yard.threed.engine.gui.Hud;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.engine.loader.PortableModelBuilder;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.core.geometry.SimpleGeometry;

/**
 * A simple model preview scene.
 * <p>
 * Verwendet das FG Koordinatensystem?
 * Die z-Achse zeigt nach oben, y von links nach rechts und x von vorne nach hinten;wie bei FG Aircraft- und Scenery Modellen üblich.
 * Die Camera steht auf positivem x und blickt Richtung negativem X. Damit werden Aircraftmodelle z.B. zunächst von hinten oben gesehen.
 * Dimensionierung in etwa in Metern, um nicht zu viel scalen zu muessen.
 * ECS wird aber nicht verwendet, um es schlicht zu halten.
 */
public class ModelPreviewScene extends Scene {
    public Log logger = Platform.getInstance().getLog(ModelPreviewScene.class);
    Light light;
    public double scale = 1;
    public SceneNode model = null;
    public int major = 5;
    String[] modellist;
    Hud hud;
    double rotationspeed = 10;
    SceneNode ground = null;
    public double elapsedsec = 0;

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
                "pcm:avatarA"
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
        getDefaultCamera().getCarrier().getTransform().setPosition(new Vector3(120, 0, 30));
        getDefaultCamera().lookAt(new Vector3(0, 0, 0), new Vector3(0, 0, 1));

        hud = Hud.buildForCameraAndAttach(getDefaultCamera(), 0);

        customInit();

        newModel();
        addToWorld(ModelSamples.buildAxisHelper(50));
    }

    @Override
    public void initSettings(Settings settings) {
        settings.targetframerate = 10;
        settings.aasamples = 4;
    }

    private void redraw() {
        if (model != null) {
            // maybe not loaded yet
            model.getTransform().setScale(new Vector3(scale, scale, scale));
        }
        hud.setText(2, "scale: " + scale);
    }

    private void addLight() {
        Light light = new DirectionalLight(Color.WHITE, new Vector3(0, 3, 2));
        addLightToWorld(light);
        //light = new DirectionalLight(Color.WHITE, new Vector3(-0, 3, -2));
        //addLightToWorld(light);
    }

    private void cycleMajor(int inc, int cnt) {
        major += inc;
        if (major < 0) {
            major = cnt - 1;
        }
        if (major >= cnt) {
            major = 0;
        }
        logger.info("cycled to " + "." + major);
        newModel();
    }

    private void newModel() {
        if (model != null) {
            SceneNode.removeSceneNode(model);
        }
        addModel();
        if (model != null) {
            model.getTransform().setScale(new Vector3(scale, scale, scale));
        }
        if (hud != null) {
            hud.clear();
            hud.setText(1, "major: " + major);
            hud.setText(2, "scale: " + scale);
        }
    }

    private void addModel() {
        model = null;
        String modelname = modellist[major];

        BuildResult result = loadModel(modelname);
        if (result != null) {
            model = new SceneNode(result.getNode());
            //scale = 0.5f;
            if (model != null) {
                logger.info("Building imported model. scale=" + scale);

            } else {
                //cube as a 'not loaded indicator'
                logger.warn("Showing cube ");
                model = ModelSamples.buildCube(10, new Color(0xCC, 00, 00));

            }
            model.getTransform().setScale(new Vector3(scale, scale, scale));
            addToWorld(model);
        }
    }

    public BuildResult loadModel(String modelname) {
        String dir = null;
        String bundlename = null;
        if (StringUtils.startsWith(modelname, "pcm")) {
            // Pseudo Bundle
            bundlename = "pcm";
            modelname = StringUtils.substring(modelname, 4);
        } else {
            int index = StringUtils.indexOf(modelname, ":");
            if (index != -1) {
                bundlename = StringUtils.substring(modelname, 0, index);
                modelname = StringUtils.substring(modelname, index + 1);
            }
        }

        BuildResult result = null;
        final String mname = modelname;
        final String bname = bundlename;
        if (bundlename != null) {
            if (bundlename.equals("pcm")) {
                // Pseudo Bundle
                logger.debug("Building pcm for model " + modelname);
                PortableModelList pml;
                if (modelname.equals("loc")) {
                    pml = VehiclePmlFactory.buildLocomotive();
                } else if (modelname.equals("bike")) {
                    pml = VehiclePmlFactory.buildBike();
                } else if (modelname.equals("mobi")) {
                    pml = VehiclePmlFactory.buildMobi();
                } else if (modelname.equals("avatarA")) {
                    pml = AvatarPmlFactory.buildAvatarA("red");
                } else {
                    throw new RuntimeException("unknown pcm model " + modelname);
                }
                SceneNode node = new PortableModelBuilder(pml).buildModel(null, null);
                result = new BuildResult(node.nativescenenode);
            } else {
                Bundle bundle = BundleRegistry.getBundle(bundlename);
                if (bundle == null) {
                    final SceneNode destination = new SceneNode();
                    result = new BuildResult(destination.nativescenenode);
                    AbstractSceneRunner.instance.loadBundle(bundlename, (Bundle b_isnull) -> {
                        Bundle b = BundleRegistry.getBundle(bname);
                        BuildResult res = addModelFromBundle(b, mname);
                        destination.attach(new SceneNode(res.getNode()));
                    });
                } else {
                    result = addModelFromBundle(bundle, modelname);
                }
            }
        } else {
            //7.7.21 FileSystemResource resource = FileSystemResource.buildFromFullString(dir + "/" + modelname);
            result = null;//25.4.17 ModelFactory.buildModel(resource, false, 0, FGGlobals.getInstance().get_props(), null);
        }
        return result;
    }

    public BuildResult addModelFromBundle(Bundle bundle, String modelname) {
        BundleResource br = BundleResource.buildFromFullString(modelname);
        br.bundle = bundle;
        String extension = br.getExtension();
        BuildResult result = null;

            result = new BuildResult(ModelFactory.asyncModelLoad(br, EngineHelper.LOADER_USEGLTF).nativescenenode);

        /*29.12.18 model = null;
        // Das model hat evtl. die offsets in seinem transform
        model = new SceneNode(result.getNode());
        //scale = 0.5f;
        // Der dump bringt hier nichts, weil der Load async ist und spaeter eingehangen wird.
        //logger.info("Building imported model. scale=" + scale+", tree:"+model.dump("",0));
        logger.info("Building imported model.");
        model.getTransform().setScale(new Vector3(scale, scale, scale));
        */

        //29.12.18 addToWorld(model);
        return result;
    }

    @Override
    public Dimension getPreferredDimension() {
        return new Dimension(1200, 900);
        //return new Dimension(800, 600);
    }


    @Override
    public void update() {
        double tpf = getDeltaTime();

        if (Input.getKeyDown(KeyCode.Alpha1)) {
            logger.debug("3 key was pressed. currentdelta=" + tpf);
            cycleMajor(1, modellist.length);
            //doppelt newModel();
        }
        if (Input.getKeyDown(KeyCode.Alpha2)) {
            logger.debug("4 key was pressed. currentdelta=" + tpf);
            cycleMajor(-1, modellist.length);
            //doppelt newModel();
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
            logger.info("model tree:" + model.dump("", 1));
        }
        if (Input.getKey(KeyCode.KEY_LEFT)) {
            model.getTransform().rotateZ(new Degree(rotationspeed * 1f));
        }
        if (Input.getKey(KeyCode.KEY_RIGHT)) {
            model.getTransform().rotateZ(new Degree(rotationspeed * -1f));
        }
        if (Input.getKey(KeyCode.KEY_UP)) {
            model.getTransform().rotateX(new Degree(rotationspeed * 1f));
        }
        if (Input.getKey(KeyCode.KEY_DOWN)) {
            model.getTransform().rotateX(new Degree(rotationspeed * -1f));
        }
        if (Input.getKeyDown(KeyCode.KEY_R)) {
            // mal als Test wegen memory
            BundleRegistry.clear();
        }
        elapsedsec += tpf;
        customUpdate();

    }

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
}
