package de.yard.threed.engine;


import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.platform.NativeScene;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Settings;

import java.util.ArrayList;
import java.util.List;

/**
 * In Anlehung an JME und deswegen hat eine Scene jetzt schon als Default eine PerspectiveCamera.
 * <p>
 * 19.10.18: Diese Scene als Superklasse und dann NativeScene noch scheint irgendwie doppelt, obwohl es das eigentlich nicht
 * ist. Das ist doch analog zu anderen Native Objekten.
 * <p/>
 * 23.10.19: Kapselt auch die Cameras, obwohl die eigentlich in der Platform liegen (müssen).
 * Date: 14.02.14
 * Time: 15:56
 */
public abstract class Scene {
    // Vector statt List weil der Renderer als eigener Thread parallel darauf zugreift.
    //THREED Vector<Renderable> renderables = new Vector<Renderable>();
    List<Light> light = new ArrayList<Light>();
    // scene ist nur public, weil es noch unsaubere Referenzen gibt
    public NativeScene scene;
    Log logger = Platform.getInstance().getLog(Scene.class);
    //9.8.21 deltaTime is property of scene? TODO check and maybe remove
    public double deltaTime;
    // die root node der ganzen Scene. Zeichnet sich dadurch aus, als einizige keinen parent zu haben.
    // Auch für Spiegelung in Unity
    // obwohl es ja auch mehrere Trees geben koennte?
    // sollte eigentlich private sein, aber machmal zu Tests ganz praktisch. Und auch fuer "scene.add".
    // 7.5.21: Wird für VR in den VR space verschoben.
    private World world;
    //7.5.21 doppelt zu world in platform. jetzt nicht mehr. Doch besser hier statt in Platform
    // static World world;
    static private Scene current;


    public Scene() {
        // die scene muss in JME von aussen kommen scene = PlatformFactory.getInstance().buildScene();
        // die Defaaultcamera auch
        current = this;
        // 29.12.23: So far world was set in the scenerunner init. Might need
        // some static in scenerunner impls (eg. JmeScene.instance).
        world = new World();
    }

    public Camera getDefaultCamera() {
        return getMainCamera();
    }

    /**
     * Darf/Soll nur einmal aufgerufen werden.
     * Der Aufrufer muss sich darum kuemmern.
     * Ist nicht reset aehnlich, oder?
     * 20.11.15: Darf man denn schon vorher Platformfunktionen aufrufen, z.B. in der Variableninitialisierung der Scene?
     * Doch wohl eher nicht. Dann muss das unterbunden werden.
     * 18.11.21: Parameter for defining run mode (eg. for Server). Is called exactly once!
     */
    public abstract void init(SceneMode sceneMode);

    /**
     * Wird vor dem init() aufgerufen. Muss von der Scene überschrieben werden.
     */
    public void initSettings(Settings settings) {

    }

    public void vrDisplayPresentChange(boolean isPresenting) {

    }

    /**
     * Bundles loaded by platform berfore init() isType called.
     */
    public String[] getPreInitBundle() {
        return new String[]{};
    }

    /**
     * See {@link Light}
     * 30.4.19: Bei JME(DirectionalLightShadowRenderer?) noch nicht richtig
     * 25.2.22: This is just a convenience method. Maybe there can be a better location (in Light?).
     *
     * @param light
     * @return
     */
    public SceneNode addLightToWorld(Light light) {
        //due to transform the SceneNode should be for Light exclusively.

        SceneNode sn = new SceneNode();
        sn.setLight(light);
        // The name might be used in tests for checking light!
        sn.setName("Scene Light");
        addToWorld(sn);
        return sn;
    }

    public String dumpSceneGraph() {
        String s = world/*((Platform)Platform.getInstance()).getWorld()*/.dump("", 0);
        return s;
    }

    public void removeLight() {
        light.clear();
    }

    public void removeAll() {
        // Licht wird hier nicht entfernt
        //renderables.clear();
    }

    /**
     * Ob das so gut ist? Aber eine Scene braucht eine Camera.
     * 28.2.21: Nicht unbedingt. Z.B. MP. Also, kann auch null liefern.
     *
     * @return
     */
    public Camera getMainCamera() {
        if (getCameraCount() == 0) {
            return null;
        }
        return getCamera(0);
    }

    public int getCameraCount() {
        return AbstractSceneRunner.getInstance().getCameras().size();
    }

    public Camera getCamera(int index) {
        //unsauber: Camera might be other than Perspective?
        return new PerspectiveCamera(AbstractSceneRunner.getInstance().getCameras().get(index));
    }

    /**
     * Nur fuer die Platform gedacht.
     * MA36: Fuer world? Ach, soll die doch die Platform bereitstellen
     *
     * @param scene
     */
    public void setSceneAndCamera(NativeScene scene, /*NativeCamera camera,*/ World pworld) {
        this.scene = scene;
        //cameras = new ArrayList<Camera>();
        //cameras.add(new PerspectiveCamera(camera));
        //7.5.21
        world = pworld;
        scene.add((NativeSceneNode) pworld.nativescenenode);
    }

    /**
     * 29.12.16:Mal auf deprecated gesetzt. world als Parent koennte dann auch implizit beim ScneNode create erfolgen.
     * 23.3.17: Andererseits ist es aber ganz eingängig. Nennen wir es doch einfach um, statt einfach nur add().
     * Und dann auch nicht mehr deprecated. Also, die MEthode ist dafür da, das Objekt in die World zu adden. Vorhanden wäre es
     * sonst wohl auch, (oder je nach Platform). Naja, aber undefiniert.
     *
     * @param model
     */
    public void addToWorld(SceneNode model) {
        //logger.debug("addToWorld " + model);

        if (model == null || model.getTransform() == null) {
            // just to be safe. might happen during failed model loading.
            logger.error("model or model.object3d isType null. Ignoring");
            return;
        }
        //World world = Platform.getInstance().getWorld();
        model.getTransform().setParent(world.getTransform());
    }

    /**
     * 10.10.18: Nicht mehr so global festlegen. Das soll der Nutzer machen, wenn das nicht geht, dann die Platform.
     * Nur in Ausnahmen die App.
     *
     * @return
     */
    /*public Dimension getPreferredDimension() {
        return new Dimension(800, 600);
    }*/
    public Dimension getPreferredDimension() {
        return null;
    }

    /**
     * Liefert die tatsaechliche size.
     *
     * @return
     */
    public final Dimension getDimension() {
        return scene.getDimension();
    }

    /**
     * Liefert die Zeit in Sekunden.
     *
     * @return
     */
    public double getDeltaTime() {
        return deltaTime;
    }

    /**
     * Liefert die aktuelle Scene.
     * Das ist nicht so ganz sauber, aber praktisch.
     *
     * @return
     */
    public static Scene getCurrent() {
        return current;
    }

    /**
     * Manchmal kann der attachToWorld() nicht aufgerufen werden. Dann kann world als destinationnode uebergeben werden.
     *
     * @return
     */
    public World getWorld() {
        if (world == null) {
            throw new RuntimeException("world not set");
        }
        return world;//((Platform)Platform.getInstance()).getWorld();
    }

    /**
     * Instead of SceneUpdater interface
     */
    public abstract void update();
}
