package de.yard.threed.engine.platform.common;

import de.yard.threed.core.BuildResult;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.GeneralHandler;
import de.yard.threed.core.ModelBuildDelegate;
import de.yard.threed.core.Packet;
import de.yard.threed.core.Pair;
import de.yard.threed.core.Point;
import de.yard.threed.core.platform.AsyncHttpResponse;
import de.yard.threed.core.platform.Config;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeBundleLoader;
import de.yard.threed.core.platform.NativeCamera;
import de.yard.threed.core.platform.NativeScene;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneAnimationController;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.ClientBusConnector;
import de.yard.threed.engine.ecs.DefaultBusConnector;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.loader.InvalidDataException;
import de.yard.threed.engine.loader.PortableModelBuilder;
import de.yard.threed.engine.loader.PortableModelList;
import de.yard.threed.engine.loader.SceneLoader;
import de.yard.threed.engine.platform.*;

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
 * <p>
 * Created by thomass on 22.03.16.
 */
public class AbstractSceneRunner {
    Log logger = Platform.getInstance().getLog(AbstractSceneRunner.class);
    //23.7.21 NativeScene is in platform
    //19.10.18: Superklasse auch hier rein, weils einfach praktisch ist.
    public Scene ascene;
    public ArrayList<Integer> pressedkeys = new ArrayList<Integer>();
    public ArrayList<Integer> stillpressedkeys = new ArrayList<Integer>();
    public static AbstractSceneRunner instance = null;
    public Point mousemove, mousepress, mouseclick, mousedrag;
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
    int delegateid = 1;
    TreeMap<Integer, ModelBuildDelegate> delegates = new TreeMap<Integer, ModelBuildDelegate>();
    public TreeMap<Integer, BuildResult> delegateresult = new TreeMap<Integer, BuildResult>();
    TreeMap<Integer, BundleLoadDelegate> bundledelegates = new TreeMap<Integer, BundleLoadDelegate>();
    //20.5.20: Und noch eine Lösung fur invokeLater. <AsyncHttpResponse> wegen C#. Driss.
    public List<AsyncInvoked<AsyncHttpResponse>> invokedLater = new ArrayList<AsyncInvoked<AsyncHttpResponse>>();
    //2.8.21 public TreeMap<Integer, Bundle> bundledelegateresult = new TreeMap<Integer, Bundle>();
    // ResourceManager hier, damit er nicht mehr über die Platform zugreifbar ist (wegen Architektur)
    //5.8.21 private ResourceManager resourceManager = null;
    // cameras might also be stored in the scene or the platform (which not really fits).
    // And why should the platform know cameras? In any case there will be dependency conflicts.
    // These appear minimal here, because in Scene the list needs to be public.
    // Not for internal (VR) cameras. camera[0] is the main/default.
    private List<NativeCamera> cameras = new ArrayList<NativeCamera>();
    public NativeHttpClient httpClient = null;
    PlatformInternals platformInternals;
    // Only in client mode, null otherwise. Use protected for now even though it might cause C# problems.
    protected ClientBusConnector clientBusConnector = null;

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


    @Deprecated
    public static AbstractSceneRunner init(NativeScene scene/*, ResourceManager resourceManager*/, Scene ascene) {
        instance = new AbstractSceneRunner(scene, /*resourceManager,*/ ascene);
        return instance;
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
     * Statt ueber platform
     * 10.7.21
     *
     * @return
     */
    public NativeHttpClient getHttpClient() {
        if (httpClient != null) {
            return httpClient;
        }
        throw new RuntimeException("abstract! Needs to be implemented");
    }

    /**
     * Alles das was vor dem draw gemacht wird.
     *
     * @param tpf
     */
    public void prepareFrame(double tpf) {
        // Die Reihenfolge ob erst Callback und dan Updater ist eigentlich rein willkürlich. Aber es ist
        // schon sinnvoll, dass der updater auf einen beendeten Job reagieren kann.

        processCompletedJobs();
        //2.8.21: Den extrahierten bundleloader vor asnyhelper
        List<Pair<BundleLoadDelegate, Bundle>> loadresult = null;
        if (!Platform.getInstance().hasOwnAsync()) {
            // die Abfrgae hasOwnAsync() ist eigentlich redundant. Warum die Results gesammelt und die Delegates erst staeter ausgefuehrt werden, ist
            //nicht mehr ganz klar.Wahrscheinlich um Aufrufe von irgendwo aus der Tiefe zu vermeiden.
            loadresult = Platform.getInstance().bundleLoader.processAsync();
        }
        // Der AsynHelper wird fuer jede Platform verwendet, auch WebGl. Obwohl die auch wirklich async laden kann, macht sie es nicht unbedingt.
        AsyncHelper.processAsync(/*getResourceManager(),*/Platform.getInstance().bundleLoader);
        for (GeneralHandler handler : platformInternals.beforeFrameHandler) {
            handler.handle();
        }

        processDelegates(loadresult);

        // Is this the best moment to get packets?
        int cnt = 0;
        if (clientBusConnector != null) {
            Packet packet;
            while ((packet = clientBusConnector.getPacket()) != null) {
                SystemManager.publishPacketFromServer(packet);
                cnt++;
            }
            if (cnt > 0) {
                logger.debug("Read " + cnt + " packets from client bus connector");
            }
        }

        // Update systems before individual update
        SystemManager.update(tpf);

        // Die Liste der updater duplizieren, damit im update() welche geadded werden koennen.
        ascene.update();

        pressedkeys.clear();
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
                    if (Config.isAsyncdebuglog()) {
                        logger.debug("job completed. " + newJobList.size() + " remaining");
                    }
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

    //@Override
    public boolean keyStillPressed(int keycode) {
        // if (renderer != null) {
        return stillpressedkeys.contains(keycode);
        // }
        //return false;
    }

    /**
     * Liefert die Koordinaten zu denen sich die Maus bewegt hat.
     * null bei keiner Bewegung.
     * <p/>
     * y= 0 ist oben(unten!), wie bei ThreeJS und auch OpenGL.
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

    public void addKey(int k, boolean pressed) {
        //logger.debug("Key " + key + ", pressed=" + pressed);
        if (pressed) {
            // Bei Android kommen bei gehaltener Taste staendig PRESS Events.
            if (!pressedkeys.contains(k)) {
                pressedkeys.add(k);
            }
            if (!stillpressedkeys.contains(k)) {
                stillpressedkeys.add(k);
            }
        } else {
            stillpressedkeys.remove(new Integer(k));
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
        List<CompletedJob> ascyncallbacklist = completedJobList.getCompletedJobs();
        while (ascyncallbacklist.size() > 0) {
            CompletedJob ac = ascyncallbacklist.get(0);
            if (Config.isAsyncdebuglog()) {
                logger.debug("found completed job " + ac.job.getName());
            }
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

    public void processDelegates(List<Pair<BundleLoadDelegate, Bundle>> loadresult) {
        //TODO threadsafe machen?
        //for (int i=0;i< modelbuilddelegates.size();i++){
        //C# kann keine iterator
        //for (Iterator<Integer> iter = delegates.keySet().iterator();iter.hasNext();){
        List<Integer> delegateids = new ArrayList<Integer>(delegates.keySet());
        for (int i = delegateids.size() - 1; i >= 0; i--) {
            //blocks webgl log. logger.debug("check processing delegate");
            Integer d = delegateids.get(i);
            if (delegateresult.get(d) != null) {
                if (Config.isAsyncdebuglog()) {
                    logger.debug(" processing build delegate id " + d);
                }

                delegates.get(d).modelBuilt(delegateresult.get(d));
                delegates.remove(d);
                delegateresult.remove(d);
            }
        }
        //modelbuilddelegates.clear();
        //modelbuildvalues.clear();
        //3.8.21 geht jetzt uber das loadresult
        if (loadresult != null) {
            // List<Integer> bundledelegateids = new ArrayList<Integer>(bundledelegates.keySet());
            //for (int i = bundledelegateids.size() - 1; i >= 0; i--) {
            for (Pair<BundleLoadDelegate, Bundle> result : loadresult) {
                //blocks webgl log. logger.debug("check processing delegate");
                //Integer d = bundledelegateids.get(i);
                //ist ja immer null if (bundledelegateresult.get(d) != null) {
                if (Config.isAsyncdebuglog()) {
                    logger.debug(" processing bundle delegate");
                }

                //bundledelegates.get(d).bundleLoad(bundledelegateresult.get(d));
                //bundledelegates.remove(d);
                //bundledelegateresult.remove(d);
                result.getFirst().bundleLoad(result.getSecond());
                //}
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
        if ((sceneExtension0 = Platform.getInstance().getConfiguration().getString("argv.sceneExtension0")) != null) {
            SceneLoader sceneLoader = null;
            try {
                sceneLoader = new SceneLoader(sceneExtension0, "");
                PortableModelList ppfile = sceneLoader.preProcess();
                PortableModelBuilder pmb = ppfile.createPortableModelBuilder();
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

    public int invokeLater(BundleLoadDelegate modeldelegate) {
        int id = ++delegateid;
        bundledelegates.put(delegateid, modeldelegate);
        return id;
    }

    public void invokeLater(AsyncInvoked<AsyncHttpResponse> asyncInvoked) {
        invokedLater.add(asyncInvoked);
    }

    /**
     * eigentlich nur fuer Tests
     */
    public void cleanup() {
        delegates.clear();
        delegateresult.clear();
        bundledelegates.clear();
        //bundledelegateresult.clear();
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
     * Das Bundle wird in jedem Fall asynchron, aber nicht multithreaded ueber die Platform geladen.
     * async analog zu Modeln
     * 20.2.18: Wenn ein Bundle schon geladen wurde, wird es nicht doppelt geladen (Eine Race Condition gibt es aber trotzdem).
     * Das Verhalten ist unabhaengig davon, ob das Model schon geladen wurde oder nicht.
     * <p>
     * 22.7.21: Aus Platform hier hin. Nicht static. Muss von webgl overrided werden!
     * GHenerell async aber ohne MT.
     */
    public void /*Bundle*/ loadBundle(String bundlename, /*AsyncJobCallback*/BundleLoadDelegate bundleLoadDelegate, boolean delayed) {
        //2.8.21 AsyncHelper.asyncBundleLoad(bundlename, AbstractSceneRunner.getInstance().invokeLater(bundleLoadDelegate), delayed);
        Platform.getInstance().bundleLoader.asyncBundleLoad(bundlename, bundleLoadDelegate, delayed);
    }

    public void loadBundle(String bundlename, BundleLoadDelegate loadlistener) {
        loadBundle(bundlename, loadlistener, false);
    }

    /**
     * 2.8.21 direkt deprecated wegen doofen coupling
     *
     * @return
     */
    @Deprecated
    public NativeBundleLoader getBundleLoader() {
        return Platform.getInstance().bundleLoader;
    }

    /**
     * Only for testing.
     */
    public ClientBusConnector getBusConnector() {
        return clientBusConnector;
    }

    /**
     * Additional approach for having commons in AbstractSceneRunner.
     * "scene" should hav been set already.
     */
    protected void initScene(){
        // decide scene mode monolith or client/server
        String server = Platform.getInstance().getConfiguration().getString("server");
        if (server == null) {
            ascene.init(SceneMode.forMonolith());
        } else {
            logger.info("Connecting to server " + server);
            NativeSocket socket = Platform.getInstance().connectToServer(server, 5890);
            clientBusConnector = new ClientBusConnector(socket);
            SystemManager.setBusConnector(clientBusConnector);

            ascene.init(SceneMode.forClient());
        }
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
