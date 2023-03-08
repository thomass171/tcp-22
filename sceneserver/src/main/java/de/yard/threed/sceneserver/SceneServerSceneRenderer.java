package de.yard.threed.sceneserver;

/*Hmm
public class SceneServerSceneRenderer implements SceneRenderer {

    /**
     * Einen einzelnen Frame rendern
     * <p/>
     * <p/>
     * Hier werden
     * 1) Controllerevents gesammelt
     * 2) Updater aufgerufen
     * 3) Szene neu gerendered
     * /
    @Override
    public void renderFrame() {

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

        renderScene(/*scene/*,camera* /);
        //no display here Display.update();


    }


    /**
     * Camera, Ligfht, View und Porjection duerfte hier nicht erforderlich sein.
     * /
    private void renderScene(/*OpenGlScene scene, OpenGlCamera camera* /) {
        // Clear the screen and depth buffer
        /*GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        OpenGlMatrix4 projectionmatrix, viewmatrix;
        if (camera == null) {
            projectionmatrix = new OpenGlMatrix4();
            viewmatrix = new OpenGlMatrix4();
        } else {
            projectionmatrix = OpenGlMatrix4.toOpenGl(camera.getProjectionMatrix());
            viewmatrix = OpenGlMatrix4.toOpenGl(camera.getViewMatrix());
        }* /

        //26.4.20 neue Stelle fuer Rendering
        //scene.render(OpenGlContext.getGlContext(), OpenGlMatrix4.fromOpenGl(projectionmatrix), OpenGlMatrix4.fromOpenGl(viewmatrix));
        renderer.render(null, null, new ArrayList<>()/*OpenGlMatrix4.fromOpenGl(projectionmatrix), OpenGlMatrix4.fromOpenGl(viewmatrix),scene.getLights()* /);

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
}
*/