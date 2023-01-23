package de.yard.threed.sceneserver;

import de.yard.threed.core.Event;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.platform.homebrew.HomeBrewRenderer;
import de.yard.threed.platform.homebrew.OpenGlLight;
import de.yard.threed.platform.homebrew.HomeBrewMesh;
import de.yard.threed.platform.homebrew.HomeBrewSceneNode;
import de.yard.threed.platform.homebrew.Renderables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.yard.threed.engine.BaseEventRegistry.BASE_EVENT_ENTITY_CHANGE;

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
public class SceneServerRenderer extends HomeBrewRenderer {
    Log logger = Platform.getInstance().getLog(SceneServerRenderer.class);
    //SyncedObjectsRegistry syncedObjectsRegistry;

    /*public MpServerRenderer(SyncedObjectsRegistry syncedObjectsRegistry) {
        this.syncedObjectsRegistry = syncedObjectsRegistry;
    }*/

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

    /**
     * Publish entity state.
     *
     */
    public static void sendEntityState(int id, String bundlename, String modelfile, Vector3 position, Quaternion rotation){
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", Integer.toString(id));
        map.put("bundle",bundlename);
        map.put("model",modelfile);
        map.put("position", position.toString());
        map.put("rotation", rotation.toString());
        //Gson gson = new Gson();
        //String json = gson.toJson(transformChange);
        Event event = new Event(BASE_EVENT_ENTITY_CHANGE, new Payload(map));
        SystemManager.sendEvent(event);
        //logger.debug("tranformChange:" + event);
    }

    @Override
    protected void doRender(HomeBrewMesh mesh, Matrix4 projectionmatrix, Matrix4 viewmatrix, List<OpenGlLight> lights) {
        // not needed
    }

    /**
     * 18.11.21: Statt separate Liste lieber ueber die Entities. Eigentlich braucht man hier doch gar nichts zu machen.
     * Eigentlic! Aber sonst wird der dorender nicht aufgerufen?
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


}
