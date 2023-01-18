package de.yard.threed.sceneserver;

import com.jme3.app.SimpleApplication;
import com.jme3.scene.Node;
import de.yard.threed.core.Packet;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeScene;
import de.yard.threed.core.platform.NativeSceneRunner;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.World;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.outofbrowser.SyncBundleLoader;
import de.yard.threed.platform.opengl.HomeBrewRenderer;
import de.yard.threed.platform.opengl.OpenGlScene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * In MP gibt es eigentlich zwar keine Scene, aber dann ist es analog zu anderen Abläufen.
 * <p>
 * Und hier ist dann auch die main loop
 * <p>
 * <p/>
 * Created by thomass on 29.04.20.
 */
public class ServerSceneRunner extends AbstractSceneRunner implements NativeSceneRunner/*, SyncedObjectsRegistry*/ {
    Log logger = Platform.getInstance().getLog(ServerSceneRunner.class);
    // SceneRunner ist Singleton
    static private ServerSceneRunner scenerunner = null;
    //public JmeCamera jmecamera;
    //3.12.18 super class Dimension dimension;
    public Node rootnode;
    public SimpleApplication simpleApplication;
    Settings scsettings;
    Scene scene;
    HomeBrewRenderer renderer;
    // list of scene nodes whose transform is synced to a client
    //18.11.21 private List<SyncedSceneElement> syncedSceneNodes = new ArrayList<>();
    // 8.4.21: Optional frame limit for testing.
    public int frameLimit = 0;

    /**
     * Private, weil es im Grunde ein Singleton ist.
     */
    private ServerSceneRunner(PlatformInternals pl) {
        super(pl);
        logger.info("Building ServerSceneRunner");
    }

    public static ServerSceneRunner init(HashMap<String, String> properties) {
        if (scenerunner != null) {
            throw new RuntimeException("already inited");
        }
        // 25.2.21 TODO Es ist doch ein haessliches Coupling (z.B. fuer Testen), dass der Runner die Platform anlegt.
        PlatformInternals pl = new PlatformSceneServerFactory().createPlatform(properties);
        scenerunner = new ServerSceneRunner(pl);
        //MA36pl.runner = scenerunner;
        scenerunner.renderer = new SceneServerRenderer(/*scenerunner*/);
        return scenerunner;
    }

    /**
     * 25.2.21: Isn't this nonsense? Why isn't it part of TestFactory.resetInit()?
     */
    public static void dropInstance() {
        scenerunner = null;
    }

    @Override
    public void runScene(/*Native*/final Scene scene) {
        logger.info("runScene");
        logger.debug("java.library.path=" + System.getProperty("java.library.path"));

        this.scene = scene;

        scsettings = new Settings();
        scene.initSettings(scsettings);

        //JAResourceManager rm = JAResourceManager.getInstance();

        // in der native Scene liegen ja alle Nodes
        NativeScene nativeScene = new OpenGlScene();
        //TODO brauchts sowas? JmeScene.init(this, this.flyCam);

        initAbstract(nativeScene, /*rm,*/ scene);

        //((EngineHelper) PlatformJme.getInstance()).setWorld(new World());
        Scene.world=new World();

        //3.8.21 JavaSceneRunnerHelper.prepareScene(scene, nativeScene,Scene.world);
        scene.setSceneAndCamera(nativeScene, Scene.world/* ((EngineHelper) Platform.getInstance()).getWorld()*/);
        /*BundleLoaderExceptGwt*/
        SyncBundleLoader.preLoad(scene.getPreInitBundle(),new DefaultResourceReader(),Platform.getInstance().bundleResolver);
        scene.init(SceneMode.forServer());

        // 18.11.21: Now centralized here
        //2.1.23 SystemManager.addSystem(new NetworkSystem());

        postInit();

        // Now ECS isType running. Allow clients to connect (asyn/MT).

        ClientListener.dropInstance();
        ClientListener clientListener = ClientListener.getInstance("", -1);
        clientListener.start();

        SystemManager.setBusConnector(new SceneServerBusConnector(ClientListener.getInstance().getMpSocket()));

        startRenderloop();

        // Only reached with framelimit
        logger.info("runScene completed");

    }


    /**
     * Endless loop. For testing a framelimit can be used and called multiple times.
     */
    public void startRenderloop() {

        long firstFrame = getFrameCount();

        while (true) {
            renderFrame();

            //einbremsen TODO schaltbar. 29.8.16: bischen weniger bremsen
            try {
                //logger.info("sleeping 500");
                Thread.sleep(200/*200/*2000*/);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //showStatistic();
            if (frameLimit > 0 && getFrameCount() - firstFrame >= frameLimit) {
                break;
            }
        }
        logger.debug("render loop completed " + frameLimit + " frames");
    }

    /**
     * Einen einzelnen Frame rendern
     * <p/>
     * <p/>
     * Hier werden
     * 1) Controllerevents gesammelt
     * 2) Updater aufgerufen
     * 3) Szene neu gerendered
     */
    private void renderFrame() {

        // instead of keyboard/mouse events get remote events.

        // client events einspielen vor prepareFrame weil darin die Events verteilt werden.
        List<ClientConnection> clientConnections = ClientListener.getInstance().getClientConnections();
        for (ClientConnection clientConnection : clientConnections) {
            Packet packet;
            while ((packet = clientConnection.getPacket()) != null) {
                SystemManager.publishPacket(packet);
            }
        }

        scene.deltaTime = calcTpf();

        prepareFrame(scene.deltaTime);

        renderScene(/*scene/*,camera*/);
        //no display here Display.update();


    }

    /**
     * Camera, Ligfht, View und Porjection duerfte hier nicht erforderlich sein.
     */
    private void renderScene(/*OpenGlScene scene, OpenGlCamera camera*/) {
        // Clear the screen and depth buffer
        /*GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        OpenGlMatrix4 projectionmatrix, viewmatrix;
        if (camera == null) {
            projectionmatrix = new OpenGlMatrix4();
            viewmatrix = new OpenGlMatrix4();
        } else {
            projectionmatrix = OpenGlMatrix4.toOpenGl(camera.getProjectionMatrix());
            viewmatrix = OpenGlMatrix4.toOpenGl(camera.getViewMatrix());
        }*/

        //26.4.20 neue Stelle fuer Rendering
        //scene.render(OpenGlContext.getGlContext(), OpenGlMatrix4.fromOpenGl(projectionmatrix), OpenGlMatrix4.fromOpenGl(viewmatrix));
        renderer.render(null, null, new ArrayList<>()/*OpenGlMatrix4.fromOpenGl(projectionmatrix), OpenGlMatrix4.fromOpenGl(viewmatrix),scene.getLights()*/);

        // 18.11.21: Neuer sync Ansatz statt ueber renderer
        List<EcsEntity> entities = SystemManager.findEntities((EntityFilter) null);
        for (EcsEntity entity : entities) {
            if (isSynced(entity)) {
                //TODO send local or global pos?
                //Matrix4 worldModelMatrix = mesh.getSceneNodeWorldModelMatrix();
                //Vector3 position = worldModelMatrix.extractPosition();
                //worldModelMatrix.extractQuaternion()

                // 17.1.23:Shouldn't we publish every entity, independent from having a node(most will have one)?
                SceneNode node = entity.getSceneNode();
                if (node!=null) {
                    Vector3 position = node.getTransform().getPosition();
                    Quaternion rotation = node.getTransform().getRotation();
                    SceneServerRenderer.sendEntityState(entity.getId(), "a", "b", position, rotation);
                }
            }
        }

    }

    /**
     * For now all entities are synced between server and client.
     * @param entity
     * @return
     */
    private boolean isSynced(EcsEntity entity) {
        return true;
    }

    /*18.11.21 public void addSyncedSceneNode(SyncedSceneElement nativeSceneNode) {
        //logger.debug("addSyncedSceneNode:"+(new SceneNode(nativeSceneNode)).getPath());
        syncedSceneNodes.add(nativeSceneNode);
    }

    @Override
    public boolean isSyncedSceneNode(NativeSceneNode nativeSceneNode) {
        return syncedSceneNodes.contains(nativeSceneNode);
    }

    @Override
    public SyncedSceneElement getSyncedSceneNode(NativeSceneNode nativeSceneNode) {
        for (SyncedSceneElement sse : syncedSceneNodes) {
            if (sse.nativeSceneNode == nativeSceneNode) {

                return sse;
            }
        }
        return null;
    }

    @Override
    public List<SyncedSceneElement> getSyncedSceneNodes() {
        return syncedSceneNodes;
    }

    public int getSyncedSceneNodeCount() {
        return syncedSceneNodes.size();
    }

    public SyncedSceneElement getSyncedSceneNode(int index) {
        return syncedSceneNodes.get(index);
    }*/
}


