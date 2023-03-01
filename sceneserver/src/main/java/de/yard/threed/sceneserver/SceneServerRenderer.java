package de.yard.threed.sceneserver;

import de.yard.threed.core.Dimension;
import de.yard.threed.core.Event;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Packet;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCamera;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.DefaultBusConnector;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EntityFilter;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.platform.homebrew.GlDummyImpl;
import de.yard.threed.platform.homebrew.HomeBrewRenderer;
import de.yard.threed.platform.homebrew.HomeBrewSceneRunner;
import de.yard.threed.platform.homebrew.OpenGlLight;
import de.yard.threed.platform.homebrew.HomeBrewMesh;
import de.yard.threed.platform.homebrew.HomeBrewSceneNode;
import de.yard.threed.platform.homebrew.PlatformHomeBrew;
import de.yard.threed.platform.homebrew.Renderables;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Convert an objects transform change to a bus event.
 * <p>
 * 23.2.21: Ob das wirklich ein guter Ansatz ist? Das ist doch total ineffizient.
 * Zunaechst mal sollten nur changed Nodes gesendet werden. Und nur Entities?
 * Mal eine Liste verwenden.
 * <p>
 * 5.3.21: Die Idee greift nicht, weil ja wohl nur Objekte mit Mesh gerendered (also hierhin) aufgerufen werden.
 * Die ParentNode eines Model z.B. hat aber gar kein Mesh. Aber man kann ihm passende renderables geben. Und das ist dann
 * wieder ganz gut (und effizient).
 */
@Slf4j
public class SceneServerRenderer extends HomeBrewRenderer {
    //not yet available  Log logger = Platform.getInstance().getLog(SceneServerRenderer.class);
    //SyncedObjectsRegistry syncedObjectsRegistry;

    /*public MpServerRenderer(SyncedObjectsRegistry syncedObjectsRegistry) {
        this.syncedObjectsRegistry = syncedObjectsRegistry;
    }*/

    // in milliseconds
    public int noClientCpuSaveDelay = 1000;

    public SceneServerRenderer() {
        glcontext = new GlDummyImpl();
    }

    @Override
    public void doRender(HomeBrewSceneNode node, Matrix4 projectionmatrix, Matrix4 viewmatrix, List<OpenGlLight> lights) {
        //mesh.getSceneNode();

        //logger.debug("doRender:"+node.getName());
        /*no longer needed
        if (syncedObjectsRegistry == null || !syncedObjectsRegistry.isSyncedSceneNode(node)) {
            return;
        }*/

        /*SyncedSceneElement sse = syncedObjectsRegistry.getSyncedSceneNode(node);
        //TODO send local or global pos?
        //Matrix4 worldModelMatrix = mesh.getSceneNodeWorldModelMatrix();
        //Vector3 position = worldModelMatrix.extractPosition();
        //worldModelMatrix.extractQuaternion()
        Vector3 position = node.getTransform().getPosition();
        Quaternion rotation = node.getTransform().getRotation();
        //TransformChange transformChange = new TransformChange(position, rotation);

     syncElement(position,rotation);
        */
    }

    @Override
    public void init(Dimension dimension) {
        // nothing to do?
    }

    @Override
    public boolean userRequestsTerminate() {
        // no user->no user request
        return false;
    }

    @Override
    public void close() {
        // nothing to do?
    }

    /**
     * Publish entity state.
     */
    /*public static void sendEntityState(int id, String bundlename, String modelfile, Vector3 position, Quaternion rotation) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", Integer.toString(id));
        map.put("bundle", bundlename);
        map.put("model", modelfile);
        map.put("position", position.toString());
        map.put("rotation", rotation.toString());
        //Gson gson = new Gson();
        //String json = gson.toJson(transformChange);
        Event event = new Event(BASE_EVENT_ENTITY_CHANGE, new Payload(map));
        SystemManager.sendEvent(event);
        //logger.debug("tranformChange:" + event);
    }*/
    @Override
    protected void doRender(HomeBrewMesh mesh, Matrix4 projectionmatrix, Matrix4 viewmatrix, List<OpenGlLight> lights) {
        // not needed
    }

    /**
     * Camera, Ligfht, View und Porjection duerfte hier nicht erforderlich sein.
     */
    @Override
    protected void renderScene(List<OpenGlLight> lights, NativeCamera camera) {

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
        ((PlatformHomeBrew) PlatformHomeBrew.getInstance()).renderer.render(null, null, new ArrayList<>()/*OpenGlMatrix4.fromOpenGl(projectionmatrix), OpenGlMatrix4.fromOpenGl(viewmatrix),scene.getLights()*/);

        // 18.11.21: Neuer sync Ansatz statt ueber renderer
        List<EcsEntity> entities = SystemManager.findEntities((EntityFilter) null);
        for (EcsEntity entity : entities) {
            if (isSynced(entity)) {
                //TODO send local or global pos?
                //Matrix4 worldModelMatrix = mesh.getSceneNodeWorldModelMatrix();
                //Vector3 position = worldModelMatrix.extractPosition();
                //worldModelMatrix.extractQuaternion()

                // 17.1.23:Shouldn't we publish every entity, independent from having a node(most will have one)?
                /*Now in busconnector SceneNode node = entity.getSceneNode();
                if (node != null) {
                    Vector3 position = node.getTransform().getPosition();
                    Quaternion rotation = node.getTransform().getRotation();
                    SceneServerRenderer.sendEntityState(entity.getId(), "a", "b", position, rotation);
                }*/
            }
        }
    }

    /**
     * 18.11.21: Statt separate Liste lieber ueber die Entities. Eigentlich braucht man hier doch gar nichts zu machen.
     * Eigentlic! Aber sonst wird der dorender nicht aufgerufen?
     *
     * @param renderables
     */
    @Override
    protected void collectRenderables(Renderables renderables) {

       /* List<EcsEntity> entities = SystemManager.findEntities((EntityFilter) null);

        for (EcsEntity entity:entities) {
            if (isSynced(entity)) {

                //OpenGlSceneNode.collectRenderables(renderables);
                //for (SyncedSceneElement n : syncedObjectsRegistry.getSyncedSceneNodes()) {
                renderables.nodes.add((OpenGlSceneNode) entity.getSceneNode().nativescenenode);
            }
        }*/
    }

    @Override
    protected void collectKeyboardAndMouseEvents(AbstractSceneRunner runner) {

        ClientListener.getInstance().checkLiveness();

        // for closed connections send a close event, independent from whether the client logged off.
        ClientConnection cc;
        while ((cc = ClientListener.getInstance().discardClosedConnection()) != null) {
            SystemManager.sendEvent(new Event(DefaultBusConnector.EVENT_CONNECTION_CLOSED, new Payload()));
        }

        // instead of keyboard/mouse events get remote events.

        // get clients packets and publish before prepareFrame, which distributes the events.
        List<ClientConnection> clientConnections = ClientListener.getInstance().getClientConnections();
        int cnt = 0;
        for (ClientConnection clientConnection : clientConnections) {
            Packet packet;
            while ((packet = clientConnection.getPacket()) != null) {
                SystemManager.publishPacketFromClient(packet);
                cnt++;
            }
        }
        if (cnt > 0) {
            log.debug("Read {} packets from {} clients", cnt, clientConnections.size());
        }
        if (clientConnections.size() == 0) {
            // extra wait for saving CPU
            try {
                Thread.sleep(noClientCpuSaveDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void updateDisplay() {
        // nothing to do on server
    }

    /**
     * For now all entities are synced between server and client.
     *
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
