package de.yard.threed.engine.platform.common;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.GeneralHandler;
import de.yard.threed.core.ModelBuildDelegate;
import de.yard.threed.core.Packet;
import de.yard.threed.core.Pair;
import de.yard.threed.core.Point;
import de.yard.threed.core.Server;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.platform.AsyncDelegator;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.AsyncJobDelegate;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCamera;
import de.yard.threed.core.platform.NativeFuture;
import de.yard.threed.core.platform.NativeBundleResourceLoader;
import de.yard.threed.core.platform.NativeHttpClient;
import de.yard.threed.core.platform.NativeScene;
import de.yard.threed.core.platform.NativeSceneRunner;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.ecs.DefaultSystemTracker;
import de.yard.threed.engine.ecs.SystemTracker;
import de.yard.threed.engine.platform.PlatformBundleLoader;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneAnimationController;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.ClientBusConnector;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.engine.loader.PortableModelBuilder;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.engine.loader.SceneLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


/**
 * Common base functionality of all scene runner. Capsules those parts that are needed in each platform.
 * <p>
 * Delegates are processed here (async for the app logic) to have it all in the same thread (JME doesn't like MT).
 * <p/>
 * 10.10.18: Should not be used from an application!
 * 28.11.18: Not easy to decide what to define here and what in the platform (like scene). There is a nasty dependency cycle of the abstract runner using the platform, using the runner implementation.
 * Maybe a "PlatformBase" instead of RunnerHelper might help. (Skizze 82).
 * Delegates passen in eine "Base" aber nicht so gut. Die wären dann besser in der Platform aufgehoben.
 * <p>
 * 16.2.21: Shouldn't the class be abstract?
 * 08.04.21: No main-loop here, because some platforms like JME have no.
 * 26.03.23: Now implements interface from core to have Scenerunner available in all platforms (like SimpleHeadlessPlatform)
 * 10.12.23: Made abstract to enable platform specific implementations that cannot be in Platform (MA51).
 * <p>
 * Created by thomass on 22.03.16.
 */
public abstract class AbstractSceneRunner implements NativeSceneRunner {
    Log logger = Platform.getInstance().getLog(AbstractSceneRunner.class);
    //23.7.21 NativeScene is in platform
    //19.10.18: Superklasse auch hier rein, weils einfach praktisch ist.
    public Scene ascene;
    public ArrayList<Integer> pressedkeys = new ArrayList<Integer>();
    public ArrayList<Integer> releasedkeys = new ArrayList<Integer>();
    public ArrayList<Integer> stillpressedkeys = new ArrayList<Integer>();
    public static AbstractSceneRunner instance = null;
    // used for forwarding native mouse events to platform interface
    public Point mousemove, mousepress, mouseclick;
    public ArrayList<Integer> buttondown = new ArrayList<Integer>();
    public ArrayList<Integer> buttonup = new ArrayList<Integer>();
    private long lastTime = -1; // when the last frame was
    int frames;
    // fps ist immer der letzte ermittelte Wert
    int fps;
    int lastsecond;
    long framecount;
    // dimension must be adjusted on resize by the platform TODO, oder hier gar nicht ablegen, was noch besser ist
    public Dimension dimension;
    CompletedJobList completedJobList = new CompletedJobList();
    // Die newjoblist braucht keien sync, weil nur der Haupthtread damit arbeitet.
    // wird noch nicht verwendet.
    private List<AsyncJobInfo> newJobList = new ArrayList<AsyncJobInfo>();
    SceneAnimationController sceneAnimationController;
    // three delegates for async model building
    int delegateid = 1;
    private TreeMap<Integer, ModelBuildDelegate> delegates = new TreeMap<Integer, ModelBuildDelegate>();
    public TreeMap<Integer, BuildResult> delegateresult = new TreeMap<Integer, BuildResult>();
    private List<AsyncDelegator> invokeLaters = new ArrayList<AsyncDelegator>();
    //20.5.20: Und noch eine Lösung fur invokeLater. <AsyncHttpResponse> wegen C#. Driss.
    //21.12.23 not needed/used? External usage.
    //4.1.24 public List<AsyncInvoked<AsyncHttpResponse>> invokedLater = new ArrayList<AsyncInvoked<AsyncHttpResponse>>();
    //23.3.23: And one more solution for real MTs Futures
    // ("<Object>" for C#. In C# a list of generic delegates cannot contain abstract delegates
    // (See https://stackoverflow.com/questions/3319447/add-generic-actiont-delegates-to-a-list).
    public List<Pair<NativeFuture, AsyncJobDelegate>> futures = new ArrayList();
    // C# public List<Object> futures = new ArrayList<Object>();

    // cameras might also be stored in the scene or the platform (which not really fits).
    // And why should the platform know cameras? In any case there will be dependency conflicts.
    // These appear minimal here, because in Scene the list needs to be public.
    // Not for internal (VR) cameras. camera[0] is the main/default.
    private List<NativeCamera> cameras = new ArrayList<NativeCamera>();
    public NativeHttpClient httpClient = null;
    PlatformInternals platformInternals;
    // Only in client mode, null otherwise. Use protected for now even though it might cause C# problems.
    protected ClientBusConnector clientBusConnector = null;
    // 13.12.23: New loader and here instead of platform.
    public PlatformBundleLoader bundleLoader = new PlatformBundleLoader();
    public SystemTracker systemTracker = new DefaultSystemTracker();

    /**
     * 28.4.20: Warum ist der deperecated. Der scheint jetzt ein vielleicht zeitgemaesser constructor.
     * Vielleicht weil es ja ein Singletoin ist? initAbstract() soll der Einstieg sein, weil es per
     * constructor unhandlich ist.
     * 18.7.21:Dies hier vor dem initAbstract führt auf jeden Fall zur Exception im SceneAnimationController
     *
     * @param scene
     */
    @Deprecated
    private AbstractSceneRunner(NativeScene scene, /*5.8.21ResourceManager resourceManager,*/ Scene ascene) {
        /*das lass ich mal 28.4.20 if (instance != null){
            throw new  RuntimeException("duplicate instance");
        }*/
        //23.7,21 this.scene = scene;
        this.ascene = ascene;
        //5.8.21 this.resourceManager = resourceManager;
        SceneAnimationController.initForRunnerHelper();
        sceneAnimationController = SceneAnimationController.getInstance();
        instance = this;
    }

    /**
     * 2.8.21: Jetzt mit den PlatformInternals
     */
    public AbstractSceneRunner(PlatformInternals platformInternals) {
        this.platformInternals = platformInternals;
    }

    /**
     * das soll der Einstieg sein, weil es per
     * constructor unhandlich ist.
     */
    public void initAbstract(@Deprecated NativeScene scene, /*5.8.21 ResourceManager resourceManager,*/ Scene ascene) {
        //this.scene = scene;
        this.ascene = ascene;
        //5.8.21 this.resourceManager = resourceManager;
        SceneAnimationController.initForRunnerHelper();
        sceneAnimationController = SceneAnimationController.getInstance();
        instance = this;

        // 21.12.23: help to avoid need for getInstance.
        Platform.getInstance().sceneRunner = this;
    }

    /**
     * 20.11.21 Anyway. Somehow access to cameras is needed and these are here.
     * So provide access to anybody, even this doesn't comply to the concept of hiding the runner.
     */
    public static AbstractSceneRunner getInstance() {
        return instance;
    }

    /**
     * 7.7.21: Statt in NativeSceneRunner
     * Quais abstract
     *
     * @param scene
     */
    public void runScene(Scene scene) {
        throw new RuntimeException("abstract! Needs to be implemented");
    }

    /**
     * Everything before drawing.
     *
     * @param tpf
     */
    public void prepareFrame(double tpf) {
        // The order here is quite arbitrary. Aber es ist
        // schon sinnvoll, dass der updater auf einen beendeten Job reagieren kann.

        processCompletedJobs();
        //2.8.21: Den extrahierten bundleloader vor asnyhelper
        // Der AsynHelper wird fuer jede Platform verwendet, auch WebGl. Obwohl die auch wirklich async laden kann, macht sie es nicht unbedingt.
        // 15.12.23: futures moved from AsyncHelper to here. Also new invokelaters are here. seems more consistent.
        AsyncHelper.processAsync(/*13.12.23Platform.getInstance().bundleLoader*/);
        processFutures();
        processInvokeLaters();

        for (GeneralHandler handler : platformInternals.beforeFrameHandler) {
            handler.handle();
        }

        processDelegates(/*21.12.23loadresult*/);

        // Is this the best moment to get packets?
        int cnt = 0;
        if (clientBusConnector != null) {
            Packet packet;
            while ((packet = clientBusConnector.getPacket()) != null) {
                SystemManager.publishPacketFromServer(packet);
                cnt++;
            }
            if (cnt > 0) {
                // too much. TODO add to statistic
                //  logger.debug("Read " + cnt + " packets from client bus connector");
            }
        }

        // Update systems before individual update
        SystemManager.update(tpf);

        // Die Liste der updater duplizieren, damit im update() welche geadded werden koennen.
        ascene.update();

        pressedkeys.clear();
        releasedkeys.clear();
        mousemove = null;
        mouseclick = null;
        mousepress = null;
        buttondown.clear();
        buttonup.clear();


        // Vor starten neuer Jobs die Animationen durchgehen, damit neue Jobs nur starten, wenn keine Animation
        // mehr laeuft. TODO: Jobs queuen, damit nicht zu viele parallel laufen.

        sceneAnimationController.update();
        if (sceneAnimationController.getRunningAnimationCnt() == 0 && newJobList.size() > 0) {
            long currentmillis = Platform.getInstance().currentTimeMillis();

            //TODO alle jobs durchgehen
            AsyncJobInfo job = newJobList.get(0);
            if (currentmillis > job.createtime + job.delaymillis) {
                newJobList.remove(0);
                //4.10.18: Das war mal als echtes multithreaded gedacht. Das geht aber weder in Unity noch in GWT gut, darum einfach mal so
                //direkt.
                //Platform.getInstance().executeAsyncJobNurFuerRunnerhelper(job.job);
                try {
                    String msg = job.job.execute();
                    logger.debug("job completed. " + newJobList.size() + " remaining");
                    addCompletedJob(new CompletedJob(job.job, msg));
                } catch (java.lang.Exception e) {
                    addCompletedJob(new CompletedJob(job.job, e.getMessage()));
                }
            }
        }

        // zum Schluss FPS tracken
        trackFps();
        framecount++;
        SystemManager.reportStatistics();
    }

    public boolean keyPressed(int keycode) {
        return pressedkeys.contains(keycode);
    }

    public boolean keyReleased(int keycode) {
        return releasedkeys.contains(keycode);
    }

    //@Override
    public boolean keyStillPressed(int keycode) {
        // if (renderer != null) {
        return stillpressedkeys.contains(keycode);
        // }
        //return false;
    }

    /**
     * Coordinates where the mouse moved to. null if it didn't move.
     * <p/>
     * y= 0 ist oben(bottom!), wie bei ThreeJS und auch OpenGL.
     */
    //@Override
    public Point getMouseMove() {
        return mousemove;
    }

    public Point getMouseClick() {
        return mouseclick;
    }

    public Point getMousePress() {
        return mousepress;
    }

    /**
     * A key was pressed or released (down or up).
     */
    public void addKey(int k, boolean pressed) {
        //logger.debug("addKey: key " + k + ", pressed=" + pressed);
        if (pressed) {
            // Bei Android kommen bei gehaltener Taste staendig PRESS Events.
            if (!pressedkeys.contains(k)) {
                pressedkeys.add(k);
            }
            if (!stillpressedkeys.contains(k)) {
                stillpressedkeys.add(k);
            }
        } else {
            stillpressedkeys.remove(Integer.valueOf(k));
            releasedkeys.add(k);
        }
    }

    public double calcTpf() {
        long time;

        time = Platform.getInstance().currentTimeMillis();
        if (lastTime == -1) {
            //beim ersten mal
            lastTime = time;
            lastsecond = (int) (time / 1000);
        }

        double deltaTime = (time - lastTime) / 1000.0f;
        lastTime = time;
        return deltaTime;
    }

    void trackFps() {
        long time = Platform.getInstance().currentTimeMillis();
        frames++;

        // FPS Tracking
        int currentsecond = (int) (time / 1000);
        if (currentsecond > lastsecond) {
            // Eine Sekunde ist vergangen. fps setzen.
            fps = frames;
            frames = 0;
            lastsecond = currentsecond;
            //logger.info("fps=" + fps);
        }
    }

    private void processCompletedJobs() {
        // Die gelieferte Liste ist threadsafe. Neu endende noch laufende Jobs kommen da nicht mehr rein.
        // D.h., die Liste kann hier "in Ruhe" durchgegangen und verarbeitet und danch verworfen werden.
        // 23.3.23 Is this still true with real MT async http client?
        List<CompletedJob> ascyncallbacklist = completedJobList.getCompletedJobs();
        while (ascyncallbacklist.size() > 0) {
            CompletedJob ac = ascyncallbacklist.get(0);
            logger.debug("found completed job " + ac.job.getName());
            AsyncJobCallback callback = ac.job.getCallback();
            if (callback != null) {
                if (ac.e == null) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(ac.e);
                }
            }
            ascyncallbacklist.remove(0);
        }

    }

    /**
     * Don't remember what this is. Probably a delegate processing for detached model building (after a bundle was loaded?).
     * <p>
     * Goodness, get rid of this or make it more clear.
     */
    public void processDelegates(/*21.10.23 no longer with new bundle loader List<Pair<BundleLoadDelegate, Bundle>> loadresult*/) {
        //TODO threadsafe machen?
        //for (int i=0;i< modelbuilddelegates.size();i++){
        //C# kann keine iterator
        //for (Iterator<Integer> iter = delegates.keySet().iterator();iter.hasNext();){
        List<Integer> delegateids = new ArrayList<Integer>(delegates.keySet());
        for (int i = delegateids.size() - 1; i >= 0; i--) {
            //blocks webgl log. logger.debug("check processing delegate");
            Integer d = delegateids.get(i);
            if (delegateresult.get(d) != null) {
                logger.debug(" processing build delegate id " + d);

                if (delegates.get(d) != null) {
                    delegates.get(d).modelBuilt(delegateresult.get(d));
                }
                delegates.remove(d);
                delegateresult.remove(d);
            }
        }
    }

    /**
     * Default implementation doing nothing.
     * Might be abstract one day.
     *
     * @return
     */
    /*public List<List<String>> getExternalEvents() {
        return new ArrayList();
    }*/
    public void addCompletedJob(CompletedJob completedJob) {
        completedJobList.add(completedJob);
    }

    /**
     * Nur zur Verwendung durch RunnerHelper, der die Jobs verwaltet und letztlich ja aufrufen muss.
     * 4.10.18: Das mag, wenn die Platform es kann, multithreaded gemacht werden. Da aber weder GWT noch Unity das wirklich können/wollen,
     * besser nie MT machen, im Sinne der Konsistenz, auch was Testen angeht. Und dirket im RunnerHelper.
     */
    public void addNewJob(AsyncJob newJob, int delaymillis) {
        newJobList.add(new AsyncJobInfo(newJob, delaymillis));
    }

    /**
     * Called by scene runner after scene.init()
     */
    public void postInit() {

        String sceneExtension0;
        if ((sceneExtension0 = Platform.getInstance().getConfiguration().getString("sceneExtension0")) != null) {
            SceneLoader sceneLoader = null;
            try {
                sceneLoader = new SceneLoader(sceneExtension0, "");
                PortableModelList ppfile = sceneLoader.preProcess();
                PortableModelBuilder pmb = new PortableModelBuilder(ppfile);
                SceneNode node = pmb.buildModel(null);
                ascene.addToWorld(node);
            } catch (InvalidDataException e) {
                logger.error("sceneExtension0 failed");
                e.printStackTrace();
            }
        }

        SystemManager.initSystems();
        logger.info("Scene postInit done");
    }


    public int invokeLater(ModelBuildDelegate modeldelegate) {
        int id = ++delegateid;
        delegates.put(delegateid, modeldelegate);
        return id;
    }

    //21.12.23 not needed/used? External usage.
    /*4.1.24 public void invokeLater(AsyncInvoked<AsyncHttpResponse> asyncInvoked) {
        invokedLater.add(asyncInvoked);
    }*/

    public void invokeLater(AsyncDelegator asyncInvoked) {
        invokeLaters.add(asyncInvoked);
    }

    /**
     * Executes delegate when future is complete.
     */
    @Override
    public <T, D> void addFuture(NativeFuture<T> future, AsyncJobDelegate<D> asyncJobDelegate) {
        futures.add(new Pair(future, asyncJobDelegate));
        //logger.debug("added future. Now " + futures.size());
        //C# futures.add(new Pair<NativeFuture<T>,AsyncJobDelegate<D>>(future,asyncJobDelegate));
    }

    /**
     * eigentlich nur fuer Tests
     */
    public void cleanup() {
        delegates.clear();
        delegateresult.clear();
        newJobList.clear();
        completedJobList.getCompletedJobs().clear();
        if (clientBusConnector != null) {
            clientBusConnector.close();
            clientBusConnector = null;
        }
    }

    public long getFrameCount() {
        return framecount;
    }

    /*5.8.21 public ResourceManager getResourceManager() {
        return resourceManager;
    }*/

    public boolean isButtonDown(int button) {
        return buttondown.contains(button);
    }

    public boolean isButtonUp(int button) {
        return buttonup.contains(button);
    }

    /**
     * 20.11.21: Finally cameras are here, neither in scene nor platform. See header.
     */
    public List<NativeCamera> getCameras() {
        return cameras;
    }

    /**
     * Used by platform implementations for adding a new camera.
     */
    public void addCamera(NativeCamera nativeCamera) {
        cameras.add(nativeCamera);
    }

    public NativeCamera findCameraByName(String name) {
        for (NativeCamera camera : cameras) {
            if (name.equals(camera.getName())) {
                return camera;
            }
        }
        return null;
    }

    /**
     * The main mathod for an app to load a bundle. Loading is always async (like model) via the platform, but not multithreaded.
     * After loaded the bundle is added to the BundleRegistry and the delegate delegated to Asynchelper invokelater for consistent program flow
     * 20.2.18: Wenn ein Bundle schon geladen wurde, wird es nicht doppelt geladen (Eine Race Condition gibt es aber trotzdem).
     * Das Verhalten ist unabhaengig davon, ob das Model schon geladen wurde oder nicht.
     * <p>
     * 22.7.21: Moved from Platform to here. Not static. Muss von webgl overrided werden!
     * In general async but without MT.
     * 10.11.23: 'delayed' removed.
     * 15.12.23: also used in preload.
     */
    public void loadBundle(String bundlename, BundleLoadDelegate bundleLoadDelegate) {
        //2.8.21 AsyncHelper.asyncBundleLoad(bundlename, AbstractSceneRunner.getInstance().invokeLater(bundleLoadDelegate), delayed);

        NativeBundleResourceLoader resourceLoader;
        if (StringUtils.startsWith(bundlename, "http")) {
            // full qualified bundle
            //new HttpBundleLoader().asyncBundleLoad(bundlename, bundleLoadDelegate, delayed);
            //String bundlebasedir = BundleResolver.resolveBundle(bundlename, Platform.getInstance().bundleResolver).getPath();
            String bundlebasedir = StringUtils.substringBeforeLast(bundlename, "/");
            resourceLoader = Platform.getInstance().buildResourceLoader(StringUtils.substringAfterLast(bundlename, "/"), bundlebasedir);
            bundlename = StringUtils.substringAfterLast(bundlename, "/");

            //bundleLoader/*new PlatformBundleLoader()*/.loadBundle(bundlename, bundleLoadDelegate, resourceLoader);
        } else {
            //13.12.23 Platform.getInstance().bundleLoader.asyncBundleLoad(bundlename, bundleLoadDelegate, delayed);
            resourceLoader = Platform.getInstance().buildResourceLoader(bundlename, null);

        }

        String loadBundlename = bundlename;
        bundleLoader.loadBundle(loadBundlename, bundle -> {
            // the loader doesn't register the bundle
            if (bundle == null) {
                logger.error("Bundle load failed");
            } else {
                BundleRegistry.registerBundle(loadBundlename, bundle);
            }
            // delegate via Asynchelper for consistent program flow.
            invokeLater(() -> bundleLoadDelegate.bundleLoad(bundle));
        }, resourceLoader);
    }

    /**
     * Only for testing.
     */
    public ClientBusConnector getBusConnector() {
        return clientBusConnector;
    }

    /**
     * Additional approach for having commons in AbstractSceneRunner.
     * "scene" should have been set already.
     * platform-webgl needs it public.
     */
    public void initScene() {
        // decide scene mode monolith or client/server. 20.12.23 or server
        String server = Platform.getInstance().getConfiguration().getString("server");
        if (server == null) {
            ascene.init(getSceneMode());
        } else {
            logger.info("Connecting to server " + server);
            NativeSocket socket = Platform.getInstance().connectToServer(new Server(server));
            clientBusConnector = new ClientBusConnector(socket);
            SystemManager.setBusConnector(clientBusConnector);

            ascene.init(SceneMode.forClient());
        }
    }

    // to be overridden in server mode
    public SceneMode getSceneMode() {
        return SceneMode.forMonolith();
    }

    /**
     * 15.12.23 Moved here from AsyncHelper.
     * public for testing?
     */
    public void processFutures() {

        //logger.debug("Processing futures from " + futures.size());
        // "<Object>" for C# (See "futures" definition
        // the complete callback might add additional futures. Avoid ConcurrentModificationException and don't loose the new.
        List<Pair<NativeFuture, AsyncJobDelegate>> futures = new ArrayList<>(AbstractSceneRunner.getInstance().futures);
        //C# List<Object> futures = new ArrayList<Object>();
        int pending = 0;
        //C# foreach (Object p1 in AbstractSceneRunner.getInstance().futures) {
        for (Pair<NativeFuture, AsyncJobDelegate> p : futures) {
            //C# Pair<NativeFuture<Object>, AsyncJobDelegate<Object>> p = (Pair<NativeFuture<Object>, AsyncJobDelegate<Object>>) p1;
            if (p.getFirst().isDone()) {
                p.getSecond().completed(p.getFirst().get());
                // mark completed for below remove
                p.setSecond(null);
            } else {
                pending++;
            }
        }
        AbstractSceneRunner.getInstance().futures.removeIf(e -> e.getSecond() == null);
        if (pending > 0) {
            logger.debug("Still " + pending + " futures pending");
        }
    }

    public boolean hasCompletedFutures() {

        // "<Object>" for C# (See "futures" definition
        // the complete callback might add additional futures. Avoid ConcurrentModificationException and don't loose the new.
        List<Pair<NativeFuture, AsyncJobDelegate>> futures = new ArrayList<>(AbstractSceneRunner.getInstance().futures);
        //C# List<Object> futures = new ArrayList<Object>();
        //C# foreach (Object p1 in AbstractSceneRunner.getInstance().futures) {
        for (Pair<NativeFuture, AsyncJobDelegate> p : futures) {
            //C# Pair<NativeFuture<Object>, AsyncJobDelegate<Object>> p = (Pair<NativeFuture<Object>, AsyncJobDelegate<Object>>) p1;
            if (p.getFirst().isDone()) {
                return true;
            }
        }
        return false;
    }

    public void processInvokeLaters() {

        for (AsyncDelegator delegator : invokeLaters) {
            delegator.run();
        }
        invokeLaters.clear();
    }

    public void enterInitChain(String[] bundleNames) {

        List<Bundle> loadedBundle = new ArrayList();

        for (String bundlename : bundleNames) {
            /*15.12.23 this is old stuff boolean delayed = false;
            //wegen C#
            String bname = bundlename;
            if (StringUtils.endsWith(bname, "-delayed")) {
                //TODO das mit delay ist ne kruecke
                bname = StringUtils.replaceAll(bname, "-delayed", "");
                delayed = true;
            }
            ResourcePath bundlebasedir = BundleResolver.resolveBundle(bundlename, bundleResolver);
            //3.8.21 hier kam fruehe bei einem Fehler ueber AsyncJobCallbackImpl eine Exception
            /*BundleLoaderExceptGwt.* /
            String e = loadBundleSyncInternal(bname,  /*13.12.23 null,* / delayed, /*2.8.21 new AsyncJobCallbackImpl(),* / rm, bundlebasedir);
            if (e != null) {
                throw new RuntimeException(e);
            }end old stuff */

            // loading might be async using futures. InitChain will process futures.
            AbstractSceneRunner.getInstance().loadBundle(bundlename, new BundleLoadDelegate() {
                @Override
                public void bundleLoad(Bundle bundle) {
                    logger.debug("BundleLoadDelegate for bundle " + bundlename);
                    loadedBundle.add(bundle);
                }
            });
        }
        NativeInitChain initChain = buildInitChain(new PreloadCallback(bundleNames, loadedBundle));
        initChain.execute();
    }

    /**
     * Default implementation.
     */
    public NativeInitChain buildInitChain(InitExecutor initExecutor) {
        return new DefaultInitChain(initExecutor);
    }

    public abstract void startRenderLoop();

    /**
     * Workaround until we have it in initchain.
     */
    @Deprecated
    public abstract void sleepMs(int millis);

    public SystemTracker getSystemTracker() {
        return systemTracker;
    }
}

class AsyncJobInfo {
    public AsyncJob job;
    public int delaymillis;
    public long createtime = Platform.getInstance().currentTimeMillis();

    AsyncJobInfo(AsyncJob job, int delaymillis) {
        this.job = job;
        this.delaymillis = delaymillis;
    }
}
