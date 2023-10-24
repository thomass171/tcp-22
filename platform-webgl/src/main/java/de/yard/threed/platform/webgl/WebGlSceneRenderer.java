package de.yard.threed.platform.webgl;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.logical.shared.ResizeEvent;
import de.yard.threed.core.Point;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCamera;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.*;


import de.yard.threed.core.BuildResult;
import de.yard.threed.core.Dimension;
import de.yard.threed.engine.ecs.ClientBusConnector;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Weil hier auch die AnimationFrames erzeugt werden, kann es immer nur einen einzigen SceneRenderer geben.
 * Wenn sich die Scene aendern soll, gibt es hier einen setter dazu.
 * 16.5.15: Die Camera nicht mehr ueber den Konstruktor (da ist die Camera der Scene noch nicht da,
 * weil noch kein init gelaufen ist), sondern beim Rendern.
 * <p/>
 * 12.06.2015: Der AnimationController duerfte hier in WebGL nicht mehr richtig aufgehoben sein.
 * 12.06.2015: Das hier die ganze Scene reinkommt, dürfte auch nicht gut sein, wird aber noch wegen init() gebraucht.
 * <p>
 * 05.05.2021: AnimationScheduler is GWTs corresponding of JSs Window.requestAnimationFrame(). But this is not allowed for ThreeJS VR.
 * Instead renderer.setAnimationLoop() needs to be used. (https://threejs.org/docs/index.html#manual/en/introduction/How-to-create-VR-content).
 * So AnimationScheduler is only used for preload and then we switch to setAnimationLoop().
 * <p>
 * <p/>
 * Date: 14.02.14
 * Time: 16:09
 */
public class WebGlSceneRenderer implements AnimationController {
    boolean inited = false;
    int width, height;
    boolean preloaded = false;
    //Wenn ThreeJs sowas nicht hat, dann auch nicht versuchen nachzubilden
    //5.5.21boolean paused = false;
    private static long lasttime;
    boolean use32 = true;//Jetzt mal Default
    String window_title = "";
    static Log logger = Platform.getInstance().getLog(WebGlSceneRenderer.class);
    // Die Liste enthält die gerade laufenden Animationen. Beendete werden entfernt.
    HashMap<Animation, Boolean> animations = new HashMap<Animation, Boolean>();
    List<Animation> pausedanimations = new ArrayList<Animation>();
    private static WebGlSceneRenderer instance = null;
    //private Canvas canvas;
    //private Color background = null;
    Scene scene;
    public WebGlRenderer renderer;
    PreloadLoopCallback preloadLoopback;
    CanvasPanel canvasPanel;
    //Wenn ThreeJs sowas nicht hat, dann auch nicht versuchen nachzubilden
    //5.5.21 Integer targetframerate = null;

    private WebGlSceneRenderer(Scene scene, CanvasPanel canvasPanel, Settings scsettings) {
        boolean vrready = scsettings.vrready != null && scsettings.vrready;
        boolean antialiasing = scsettings.aasamples != null && scsettings.aasamples != 0;
        boolean vrEnabled = EngineHelper.isEnabled("enableVR");
        boolean statEnabled = EngineHelper.isEnabled("enableStat");

        logger.debug("Building WebGlSceneRenderer: vrEnabled=" + vrEnabled + ",statEnabled=" + statEnabled);
        renderer = WebGlRenderer.buildRenderer(AbstractSceneRunner.getInstance().dimension, vrready, antialiasing, vrEnabled, statEnabled);
        setScene(scene);
        this.canvasPanel = canvasPanel;
        //target framerate
        if (scsettings.targetframerate != null) {
            //Wenn ThreeJs sowas nicht hat, dann auch nicht versuchen nachzubilden
            //5.5.21 targetframerate = scsettings.targetframerate;
        }
        exportUpdateRender();
    }

    public static WebGlSceneRenderer buildInstance(Scene scene, CanvasPanel canvasPanel, Settings scsettings) {
        logger.debug("Building SceneRenderer");

        if (instance != null)
            throw new RuntimeException("instance already exists");

        instance = new WebGlSceneRenderer(scene, canvasPanel, scsettings);

        return instance;
    }

    public static WebGlSceneRenderer getInstance() {
        return instance;
    }

    /**
     * Neue Scene einrichten. Eine evtl. laufende Loop wird angehalten. TODO
     *
     * @param scene
     */
    public void setScene(Scene scene) {
        this.scene = scene;
        inited = false;
        //5.5.21 renderer.setOrbitControlsScene(scene.scene);
    }

    /**
     * Einen Looback einrichten, der immer wieder zum Rendern aufgerufen wird.
     * Die Camera gibt es erst nach dem Init.
     * <p>
     * Only called once as a trigger for the loopback.
     */
    public void startRenderLoop() {
        logger.debug("starting renderloop");
        if (!inited) {
            // init();
        }

        // Anlegen geht erst jetzt, weil jetzt erst die Camra da ist
        // Wait for preload to be complete. Will start render loop than.
        preloadLoopback = new PreloadLoopCallback(renderer, scene, null, AbstractSceneRunner.getInstance().getCameras(), ((WebGlBundleLoader) Platform.getInstance().bundleLoader));

        AnimationScheduler animationScheduler = AnimationScheduler.get();
        animationScheduler.requestAnimationFrame(preloadLoopback);
    }

    /**
     * Preload now has finished (but no websocket yet).
     */
    void init() {
        logger.debug("init started");

        // initScene() might open a websocket
        AbstractSceneRunner.getInstance().initScene();
        AbstractSceneRunner.getInstance().postInit();

        canvasPanel.addMouseMoveHandler(mouseMoveEvent -> {
            int x = mouseMoveEvent.getClientX();
            int y = mouseMoveEvent.getClientY();
            //if (mouseMoveEvent.)
            AbstractSceneRunner.getInstance().mousemove = getScreenPosition(x, y);
        });
        canvasPanel.addMouseUpHandler(mouseUpEvent -> {
            int x = mouseUpEvent.getClientX();
            int y = mouseUpEvent.getClientY();
            logger.debug("mouseUpEvent x=" + x + ",y=" + y);
            AbstractSceneRunner.getInstance().mouseclick = getScreenPosition(x, y);
        });
        canvasPanel.addMouseDownHandler(mouseDownEvent -> {
            int x = mouseDownEvent.getClientX();
            int y = mouseDownEvent.getClientY();
            logger.debug("mouseDownEvent x=" + x + ",y=" + y);
            AbstractSceneRunner.getInstance().mousepress = getScreenPosition(x, y);
        });
        //14.5.19: Touch Gesten analog Mouseevents
        canvasPanel.addTouchStartHandler(touchStartHandler -> {
            Element element = touchStartHandler.getRelativeElement();
            int touches = touchStartHandler.getTouches().length();
            if (touches < 1 || touches > 2) {
                //solche weglassen
                return;
            }
            Touch t = touchStartHandler.getTouches().get(0);
            //bei two finger nur Coordinates vom ersten verwenden.
            int x = t.getRelativeX(element);
            int y = t.getRelativeY(element);
            //logger.debug("touchStartHandler x=" + x + ",y=" + y + ",touches=" + touches);
            AbstractSceneRunner.getInstance().mousepress = getScreenPosition(x, y);
        });
        canvasPanel.addTouchMoveHandler(touchMoveEvent -> {
            Element element = touchMoveEvent.getRelativeElement();
            int touches = touchMoveEvent.getTouches().length();
            if (touches < 1 || touches > 2) {
                //solche weglassen
                return;
            }
            Touch t = touchMoveEvent.getTouches().get(0);
            //bei two finger nur Coordinates vom ersten verwenden.
            int x = t.getRelativeX(element);
            int y = t.getRelativeY(element);
            //logger.debug("touchMoveEvent x=" + x + ",y=" + y + ",touches=" + touches);
            AbstractSceneRunner.getInstance().mousemove = getScreenPosition(x, y);
            if (touches == 1) {
                //AbstractSceneRunner.getInstance().touchmoveOneFinger = getScreenPosition(x, y);
            }
            if (touches == 2) {
                //AbstractSceneRunner.getInstance().touchmoveTwoFinger = getScreenPosition(x, y);
            }
            // Not quite clear. other software also calls both.
            touchMoveEvent.stopPropagation();
            touchMoveEvent.preventDefault();
        });
        inited = true;
        logger.debug("init completed");
    }

    /**
     * 8.4.16: Wegen Unity und offenbarer OpenGL Konvention (0,0) links unten jetzt y hier spiegeln.
     *
     * @param clientx
     * @param clienty
     * @return
     */
    private Point getScreenPosition(int clientx, int clienty) {
        clienty = AbstractSceneRunner.getInstance().dimension.height - clienty - 1;
        return new Point(clientx, clienty);
    }

    //Wenn ThreeJs sowas nicht hat, dann auch nicht versuchen nachzubilden
    /*5.5.21
    public void togglePaused() {
        if (paused) {
            paused = false;
            startRenderLoop();
        } else {
            paused = true;
        }
    }*/

    /**
     * Die Scene wir gerendered in ihrem aktuellen Zustand. Hier wird kein Update()
     * aufgerufen. Ein Init der Scene wird gemacht, wenn noch nicht geschehen.
     * 25.12.15: Das ist doch nur noch fuer den Scene Viewer, oder?
     * 23.3.17: Darum jetzt mal rausgenommen. verwirrt nur.
     */
    /*@Deprecated
    public void renderScene(CanvasPanel cp, Camera camera) {
        //logger.debug("this message should get logged");
        if (!inited) {
            logger.debug("initing scene");

            scene.init();
            inited = true;
        }
        //logger.debug("vor jsrender" + renderer.renderer.toSource() + "-" + scene.scene.scene.toSource());
        renderer.render(((WebGlScene) scene.scene).scene, (WebGlCamera) camera.getNativeCamera());
        // logger.debug("after jsrender");

    }*/
    private void updateAnimations() {
        List<Animation> terminated = new ArrayList<Animation>();

        for (Animation a : animations.keySet()) {
            if (!pausedanimations.contains(a)) {
                if (a.process(animations.get(a))) {
                    terminated.add(a);
                }
            }
        }
        for (Animation a : terminated) {
            logger.debug("Animation terminated");
            animations.remove(a);
        }
    }

    /**
     * Rendern mit vorherigem update() Aufruf.
     * Hier werden
     * 1) Controllerevents gesammelt
     * 2) Updater aufgerufen
     * 3) Szene neu gerendered
     * <p>
     * 5.5.21: Called via native JS now. No longer parameter to make it easy.
     */
    private void updateRender(/*5.5.21 CanvasPanel cp, List<NativeCamera> cameras*/) {
        //logger.debug("updateRender");

        //5.5.21 Was in loopback only before changing loopback
        //WebGlResourceManager.getInstance().checkBundleCompleted();
        ((WebGlBundleLoader) Platform.getInstance().bundleLoader).checkBundleCompleted();

        List<NativeCamera> cameras = AbstractSceneRunner.getInstance().getCameras();
        // erst Scene oder erst camera?

        long time = System.currentTimeMillis();
        // tpf ist in Sekunden
        double tpf = (double) (time - lasttime) / 1000;
        lasttime = time;
        //TODO ThreeJS Clock.getDelta() verwenden
        // wenn tpf 0 ist, bleiben mover im maze einfach haengen. TODO 10.10.18: Ist das nicht driss?
        if (tpf < 0.001) {
            logger.warn("Adjusting low tpf " + tpf);
            tpf = 0.001;
        }

        // Used keycodes are those of GWT/JS
        // VR controller events are added to the array for keyboard input.
        WebGlInput.collectVrControllerEvents(renderer);
        JsArrayInteger jspressedkeys = WebGlInput.getPressedKeys();
        //logger.debug(""+jspressedkeys.length()+" keys pressed");
        for (int i = 0; i < jspressedkeys.length(); i++) {
            int pressedkey = jspressedkeys.get(i);
            AbstractSceneRunner.getInstance().pressedkeys.add(pressedkey);
        }
        JsArrayInteger jsreleasedkeys = WebGlInput.getUpKeys();
        //logger.debug(""+jsreleasedkeys+" keys pressed");
        for (int i = 0; i < jsreleasedkeys.length(); i++) {
            int releasedkey = jsreleasedkeys.get(i);
            AbstractSceneRunner.getInstance().releasedkeys.add(releasedkey);
        }
        // attach model load by platform internal (GLTF)loader.
        List<WebGlLoaderData> loadedmodel = WebGlInput.getLoadedmodelList();
        for (WebGlLoaderData ld : loadedmodel) {
            int delegateid = ld.getDelegateid();
            logger.debug("loaded model detected: delegateid=" + delegateid);
            //TODO BuildResult und SceneNode sollte nicht IN der platform verwendet werden.28.11.18: wirklich nicht
            SceneNode node = new SceneNode(new WebGlSceneNode(ld.getNode()));
            //TODO dynamisch nur fuer FG und generisch in platform
            //20.3.21: no more AcPolicy
            Util.nomore();
            //node = new ACProcessPolicy(null).process(node, null);
            node.dump("", 1);
            BuildResult result = new BuildResult(node.nativescenenode);
            AbstractSceneRunner.getInstance().delegateresult.put(delegateid, result);
        }

        WebGlScene wsc = (WebGlScene) scene.scene;
        scene.deltaTime = tpf;

        AbstractSceneRunner.getInstance().prepareFrame(tpf);

        final WebGlScene sc = (WebGlScene) scene.scene;

        JsArrayInteger upkeys = WebGlInput.getUpKeys();

        // Alle gespeicherten Inputevents wieder loeschen und die gedrückten
        // speichern, um die immer noch gedrueckten ermitteln zu koennen.
        // Also fills 'stillpressed'.
        WebGlInput.close(jspressedkeys, upkeys);

        AbstractSceneRunner.getInstance().stillpressedkeys = new ArrayList(WebGlInput.getStillPressedKeys());

        //renderScene(cp, camera);
        renderer.render(((WebGlScene) scene.scene).scene, cameras);
    }

    /**
     * @param animation
     */
    @Override
    public void startAnimation(Animation animation, boolean forward) {
        // Bei einer Richtungsaenderung wird die Animation einfach ueberschrieben
        this.animations.put(animation, forward);
        // evtl. Pause aufheben
        if (pausedanimations.contains(animation)) {
            pausedanimations.remove(animation);
        }
    }

    @Override
    public void pauseAnimation(Animation animation) {
        if (pausedanimations.contains(animation)) {
            pausedanimations.remove(animation);
        } else {
            pausedanimations.add(animation);
        }
    }

    @Override
    public void resetAnimation(Animation animation) {
        animation.reset();
    }

    public void windowResized(ResizeEvent resizeEvent) {
        int width = resizeEvent.getWidth();
        int height = resizeEvent.getHeight();
        logger.debug("window resized to " + width + "x" + height);
        // 10.10.18: ignore resize with preferred size? Currently: no.
        renderer.setSize(width, height);
        if (AbstractSceneRunner.getInstance() == null) {
            // might happen during startup.
            logger.warn("ResizeEvent ignored due to missing runner");
            return;
        }
        AbstractSceneRunner.getInstance().dimension = new Dimension(width, height);
        WebGlCamera camera = (WebGlCamera) scene.getMainCamera().getNativeCamera();
        camera.setAspect(((double) width) / (double) height);
    }

    /**
     * Callback loop while preloading. Triggers itself while preload is running.
     * There is no longer a timer here because this undermines the concept of ThreeJs (and others?).
     * There is and never really was a "pause" here.
     * After preload ConnectLoopCallback and then "renderer.setAnimationLoop()" is triggered.
     */
    class PreloadLoopCallback implements AnimationScheduler.AnimationCallback {
        Scene scene;
        WebGlRenderer renderer;
        List<NativeCamera> cameras;
        CanvasPanel canvaspanel;
        Log logger = Platform.getInstance().getLog(PreloadLoopCallback.class);
        int cnt = 0;
        WebGlBundleLoader bundleLoader;

        PreloadLoopCallback(WebGlRenderer renderer, Scene scene, CanvasPanel canvaspanel, List<NativeCamera> cameras, WebGlBundleLoader bundleLoader) {
            this.renderer = renderer;
            this.scene = scene;
            this.cameras = cameras;
            this.canvaspanel = canvaspanel;
            this.bundleLoader = bundleLoader;
        }

        @Override
        public void execute(double xxv) {

            logger.debug("PreloadLoopCallback execute");

            //5.5.21 boolean usetimer = true;
            bundleLoader.checkBundleCompleted();
            if (preloaded) {
                if (!inited) {
                    // init() will (optionally open a websocket)
                    init();
                }
                // don't continue using AnimationScheduler but switch to ThreeJs animationloop
                // 7.3.23 now switch to websocket loopback (ConnectLoopCallback).
                logger.info("Switch to ConnectLoopCallback");
                AnimationScheduler.get().requestAnimationFrame(new ConnectLoopCallback(renderer, scene, canvaspanel, cameras, bundleLoader));
                // don't retrigger PreloadLoopCallback. Just return without requesting next frame.
                return;
            } else {
                logger.info("still preloading " + bundleLoader.currentlyloading.size());
                GwtUtil.showStatus("still preloading " + bundleLoader.currentlyloading.size() + " " + cnt);
                cnt++;

                if (bundleLoader.preLoadCompleted()) {
                    logger.info("preloading completed");
                    String[] bnames = BundleRegistry.getBundleNames();
                    for (String bname : bnames) {
                        logger.debug("preloaded bundle: " + bname);
                    }
                    preloaded = true;

                }
                if (bundleLoader.preLoadFailure()) {
                    logger.error("preloading failed");
                    MiscWrapper.alert(bundleLoader.getPreloadError());
                    // don't continue. Just return without requesting next frame.
                    return;
                    //paused = true;
                }
            }
            //The recommended way, but no for VR. 7.3.23: Hmm: what does that mean? What is special about VR here.
            AnimationScheduler animationScheduler = AnimationScheduler.get();
            animationScheduler.requestAnimationFrame(this);
        }
    }

    /**
     * Callback loop while waiting for websocket connect (optionally). Triggers itself while websocket is not connected.
     * After connect or when there is no connection "renderer.setAnimationLoop()" is triggered.
     */
    class ConnectLoopCallback implements AnimationScheduler.AnimationCallback {
        Scene scene;
        WebGlRenderer renderer;
        List<NativeCamera> cameras;
        CanvasPanel canvaspanel;
        Log logger = Platform.getInstance().getLog(ConnectLoopCallback.class);
        int cnt = 0;
        WebGlBundleLoader bundleLoader;

        ConnectLoopCallback(WebGlRenderer renderer, Scene scene, CanvasPanel canvaspanel, List<NativeCamera> cameras, WebGlBundleLoader bundleLoader) {
            this.renderer = renderer;
            this.scene = scene;
            this.cameras = cameras;
            this.canvaspanel = canvaspanel;
            this.bundleLoader = bundleLoader;
        }

        @Override
        public void execute(double xxv) {

            logger.debug("ConnectLoopCallback execute");

            // wait for socket. preload and init() are done here.
            if (AbstractSceneRunner.getInstance().getBusConnector() != null) {
                ClientBusConnector cbc = AbstractSceneRunner.getInstance().getBusConnector();
                WebGlSocket socket = (WebGlSocket) cbc.getSocket();
                logger.debug("check socket");
                if (socket.isPending()) {
                    logger.debug("Waiting for socket to connect");
                    AnimationScheduler.get().requestAnimationFrame(this);
                    return;
                }
            }

            // don't continue using AnimationScheduler but switch to ThreeJs animationloop
            // updateRender(canvaspanel, cameras);
            logger.info("Switch to ThreeJs animationloop");
            renderer.activeAnimationLoop();
            // don't trigger myself any more. Just return without requesting next frame.
        }
    }

    public native void exportUpdateRender() /*-{
        var that = this;
        $wnd.webGlSceneRendererInstance = $entry(function() {
          //that.@mypackage.Account::add(I)(amt);
          that.@de.yard.threed.platform.webgl.WebGlSceneRenderer::updateRender()();
        });
    }-*/;
}