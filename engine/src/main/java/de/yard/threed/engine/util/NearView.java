package de.yard.threed.engine.util;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.*;
import de.yard.threed.engine.geometry.Primitives;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Color;
import de.yard.threed.engine.platform.common.SimpleGeometry;

/**
 * Erstmal als Prototyp.
 * Der Name trifft es nicht ganz, denn entscheidend ist, dass das Model ganz woanders im Scene Graph ist.
 * "lowDimension"? "InteriorHelper"? "NearView" ist doch huebsch. "NearView" gefaellt mir aber besser.
 * <p>
 * Muesste doch, zumindest wegen des Layer, ein Singleton sein. Es koennte aber auch mehrere geben.
 * Liegt per Default an (0,0,0).
 * <p>
 * 21.10.19
 */
public class NearView {
    private Log logger = Platform.getInstance().getLog(NearView.class);

    //liegt "irgendwo", da kommen die Model dran
    SceneNode home;
    //eine deferred camera
    Camera deferredcamera;
    SceneNode marker, hiddenNode;
    boolean debugMode = false;
    static int LAYER = 11;
    int layer = LAYER;

    public NearView(Camera mainCamera, double near, double far, Scene scene) {
        //Die deferredcamera kommt nachher an eine hide node.
        deferredcamera = Camera.createFreeDeferredCamera(mainCamera, LAYER, near, far);
        deferredcamera.setName("nearview-camera");

        home = new SceneNode();
        home.setName("Near view home");
        scene.addToWorld(home);
        marker = buildPyramidMarker(Color.BLUE);
        //Marker ist bei Railing links/vorne mit Spitze (y-Achse) nach oben.
        //Bei Travel mit Spitze nach rechts. Ob das OK ist?
        marker.getTransform().setPosition(new Vector3(-(far - 5), 0, 2));
        //12.11.19:Marker kommt besser an die hiddennode, denn die koennte rotieren. Oder doch besser mal so lassen? Dann muesste sie mal ins Blickfeld kommen.
        home.attach(marker);

        //Mit einer "Garage" (ein Mesh drumrum wie eine Erdoberflaeche um 0,0,0) pruefen, ob es beim Rendering Probleme mit Bounding Volumes oder Culling gibt (JME?).
        boolean withGarage = false;
        if (withGarage) {
            SceneNode garage = ModelSamples.buildCube(30, Color.BLUE);
            home.attach(garage);
        }
    }

    public int getLayer() {
        return layer;
    }

    public void setPosition(Vector3 v) {
        home.getTransform().setPosition(v);
    }

    public void hide(SceneNode nodeToHide) {
        //addToWorld() haengt den parent um. 12.11.19: Aber das muss doch an Home.
        //Scene.getCurrent().addToWorld(nodeToHide);
        home.attach(nodeToHide);
        nodeToHide.getTransform().setPosition(new Vector3());

        logger.debug("Hiding node "+nodeToHide.getName());
        if (!debugMode) {

            nodeToHide.getTransform().setLayer(layer);
        }
        hiddenNode = nodeToHide;
        hiddenNode.attach(deferredcamera.getCarrier());
    }

    /**
     * Die geo läuft schon an y-Achse.
     *
     * @param color
     * @return
     */
    private SceneNode buildPyramidMarker(Color color) {

        double size = 2;
        double radiusTop = 0, radiusBottom = size / 2;
        int radialSegments = 16;
        SimpleGeometry pyramidgeo = Primitives.buildCylinderGeometry(radiusTop, radiusBottom, 2, radialSegments, 0, MathUtil2.PI2);
        SceneNode model = new SceneNode(new Mesh(pyramidgeo, Material.buildPhongMaterial(color)));
        model.setName("MarkerPyramid");
        model.getTransform().setLayer(layer);
        return model;
    }

    public void enable(LocalTransform posrot) {
        deferredcamera.getCarrier().getTransform().setPosition(posrot.position);
        deferredcamera.getCarrier().getTransform().setRotation(posrot.rotation);

        deferredcamera.setEnabled(true);
    }

    public void disable() {
        deferredcamera.setEnabled(false);
    }

    public Camera getCamera() {
        return deferredcamera;
    }
}
