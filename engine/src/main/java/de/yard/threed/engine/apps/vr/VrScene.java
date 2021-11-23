package de.yard.threed.engine.apps.vr;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.*;
import de.yard.threed.engine.avatar.Avatar;
import de.yard.threed.engine.gui.*;
import de.yard.threed.engine.vr.VrHelper;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.engine.platform.common.*;
import de.yard.threed.core.platform.Log;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 17.10.2017: A simple VR Scene as reference analog to ThreeJS VrScene.html.
 * The bar is at height 1m. Depending on the VR configuration (sitting/standing), the sitting view position is
 * initially at the bars height.
 * The avatar (green cube) is at same height as the bar, also in VR (independent from vr y-offset).
 *
 * Provides finetune with (shift) x/y/z.
 * Left ccontroller teleports (only), right controller controls:
 * - scale down red cube (or mouse click). (scale up with menu->start)
 * - Via CP at left controller:
 * -- menu toggle
 * -- finetune up/down
 * -- info
 * -- indicator on/off
 * <p>
 * No traditional HUD in VR. Instead control panel at left controller. No deferred camera, only the VR camera.
 *
 * 14.5.21: Avatar not really needed here, but its good for orientation.
 * 23.11.21: initialY set to 0 instead of BALKENYPOSITION. With that the "standing position" (Oculus 190cm)
 * is quite correct (with offset -0.9).
 */
public class VrScene extends Scene {
    static Log logger = Platform.getInstance().getLog(VrScene.class);
    Bundle databundle;
    SceneNode balken, box1, ground;
    Avatar avatar;
    MenuCycler menuCycler = null;
    MenuItem[][] menuitems;
    static double BALKENYPOSITION = 1;
    ControlPanel leftControllerPanel = null;
    ControlPanel controlPanel;
    Map<String, ButtonDelegate> buttonDelegates = new HashMap<String, ButtonDelegate>();
    VrInstance vrInstance = null;
    Observer observer = null;
    double initialY = 0;
    GridTeleporter gridTeleporter;

    @Override
    public void init(boolean forServer) {
        processArguments();

        vrInstance = VrInstance.buildFromArguments();

        //erst jetzt ist menu texture geladen
        menuitems = new MenuItem[][]{new MenuItem[]{
                new MenuItem(null, Label.LABEL_RESET, () -> {
                    logger.debug("reset");

                }),
                new MenuItem(null, Label.LABEL_START, () -> {
                    logger.debug("start");
                    Vector3 scale = box1.getTransform().getScale();
                    scale = scale.multiply(1.1f);
                    box1.getTransform().setScale(scale);
                }),
                new MenuItem(null, Label.LABEL_LOADINGVEHICLE, () -> {
                    logger.debug("loadvehicle");
                    Vector3 scale = box1.getTransform().getScale();
                    scale = scale.multiply(0.9f);
                    box1.getTransform().setScale(scale);
                })
        }
        };
        databundle = BundleRegistry.getBundle("data");

        observer = Observer.buildForDefaultCamera();
        if (vrInstance != null) {
            observer.initFineTune(vrInstance.getYoffsetVR());

            //if (VrHelper.getController(0) != null) {
            observer.attach(VrHelper.getController(0));
            //getDefaultCamera().getCarrier().attach(VrHelper.getController(0));
            //}
            //if (VrHelper.getController(1) != null) {
            observer.attach(VrHelper.getController(1));
            //getDefaultCamera().getCarrier().attach(VrHelper.getController(1));
            //}
        }
        observer.setPosition(new Vector3(0, initialY, 0));


        // Ein Mesh am Avatar um zu sehen, ob er sich mitbewegt (sollte er nicht). Die Camera ist dann per default in dem Cube.
        // 5.10.19: Bei VR nicht unbedingt, denn da kommt der VR offset ja noch dazu.
        avatar = new Avatar();//Avatar.buildSimple(camera);
        //4.10.19: Wieder autoadjust. Darum auf BALKENYPOSITION (ohne VR, Höhe Dangast Balken) beginnen. Mit VR abzgl. Rift Ground Offset, also 1.3
        //5.5.21: Die Avatar position auf Höhe des Balekn ist schon gut so. Bei aktivieren von VR muss dann der Offset greifen.
        avatar.setPosition(new Vector3(0, BALKENYPOSITION, 0));
        //7.5.21 avatar.ac.setVrOffsetPosition(new Vector3(0, -BALKENYPOSITION, 0));
        avatar.enableBody();
        //camera.attach(avatar.getTransform());
        addToWorld(avatar.getSceneNode());

        addLight();

        box1 = VrSceneHelper.buildRedBox();
        addToWorld(box1);

        balken = VrSceneHelper.buildBalken();
        addToWorld(balken);

        ground = VrSceneHelper.buildGround();
        addToWorld(ground);


        SceneNode locationMarker = VrSceneHelper.buildGroundMarker(Icon.ICON_CLOSE);
        addToWorld(locationMarker);
        SceneNode directionMarker = VrSceneHelper.buildGroundMarker(Icon.ICON_UPARROW);
        addToWorld(directionMarker);
        gridTeleporter = new GridTeleporter(locationMarker, directionMarker);

        buttonDelegates.put("reset", () -> {
            logger.info("reset");
        });
        buttonDelegates.put("info", () -> {
            logger.info("cam vr pos=" + getDefaultCamera().getVrPosition());
            logger.info("world pos=" + Scene.getWorld().getTransform().getPosition());

            avatar.dumpDebugInfo();
        });
        buttonDelegates.put("mainmenu", () -> {
            menuCycler.cycle();
        });
        buttonDelegates.put("up", () -> {
            logger.info("up");
            /*avatar*/
            observer.fineTune(true);
            //??vrInstance.increaseOffset(0.1);
        });
        buttonDelegates.put("down", () -> {
            logger.info("down");
            /*avatar*/
            observer.fineTune(false);
            //??vrInstance.increaseOffset(-0.1);
        });

        controlPanel = VrSceneHelper.buildControllerControlPanel(buttonDelegates);
        controlPanel.getTransform().setPosition(new Vector3(-0.5, 1.5, -2.5));
        addToWorld(controlPanel);

        menuCycler = new MenuCycler(new MenuProvider[]{new VrMainMenuBuilder(this)});
    }

    /**
     * VR is processed in VrInstance
     */
    protected void processArguments() {
    }

    @Override
    public String[] getPreInitBundle() {
        return new String[]{"engine", "data"};
    }

    private void addLight() {
        DirectionalLight light = new DirectionalLight(Color.WHITE, new Vector3(0, 3, 2));
        addLightToWorld(light);
    }

    @Override
    public void update() {

        /*avatar*/
        observer.update();
        VrHelper.update();

        if (VrHelper.getController(0) != null && leftControllerPanel == null) {

            leftControllerPanel = VrSceneHelper.buildControllerControlPanel(buttonDelegates);
            LocalTransform lt = vrInstance.getCpTransform();
            if (lt != null) {
                //leftControllerPanel.getTransform().setPosition(new Vector3(-0.5, 1.5, -2.5));
                //200,90,0 are good rotations
                leftControllerPanel.getTransform().setPosition(lt.position);
                leftControllerPanel.getTransform().setRotation(lt.rotation);
                leftControllerPanel.getTransform().setScale(new Vector3(0.4, 0.4, 0.4));
            }
            VrHelper.getController(0).attach(leftControllerPanel);
        }


        // right controller
        //rechts verkleinert rote Box (und auch menu 'start' ueber menucycler)
        //24.11.19: Besser nicht hier, sonst weiss man ja nicht, ob das Menu geht.
        //Aber das ist doch für Verkleinern bei hit der Box
        //4.5.21 jetzt eher generell pruefen auf alle hit area.
        if (Input.getControllerButtonDown(10)) {
            logger.debug(" found button down 10 ");
            if (VrHelper.getController(1) != null) {
                toggleRedBox(VrHelper.getController(1).getRay(), false);
            }
            Ray ray = VrHelper.getController(1).getRay();
            controlPanel.checkForClickedArea(ray);
            leftControllerPanel.checkForClickedArea(ray);
        }

        // show destination marker on ground by left controller
        if (VrHelper.getController(0) != null) {
            gridTeleporter.updateDestinationMarker(VrHelper.getController(0).getRay(),ground,1);
        }
        Point mouseMovelocation = Input.getMouseMove();
        if (mouseMovelocation != null) {

            Ray ray = observer.buildPickingRay(getDefaultCamera(), mouseMovelocation);
            gridTeleporter.updateDestinationMarker(ray,ground,1);
        }

        // check for teleport (by mouse or left controller)
        GridTeleportDestination markerTransform = null;

        Point mouseClickLocation = Input.getMouseClick();
        if (mouseClickLocation != null) {
            // menu geht mit delegate. Aber auch mit direktem mouse click red box verkleinern
            Ray ray = /*avatar*/observer.buildPickingRay(getDefaultCamera(), mouseClickLocation);
            toggleRedBox(ray, false);
            controlPanel.checkForClickedArea(ray);

            markerTransform = gridTeleporter.updateDestinationMarker(ray,ground,1);
            if (markerTransform != null) {
                teleport(markerTransform);
            }

        }
        //links ist nur noch fuer teleport.
        if (Input.getControllerButtonDown(0)) {
            markerTransform = gridTeleporter.updateDestinationMarker(VrHelper.getController(0).getRay(),ground,1);
            if (markerTransform != null) {
                teleport(markerTransform);
            }
        }

        //menucycler.update() is also for keys!
        menuCycler.update(mouseClickLocation);
    }

    private void teleport(GridTeleportDestination markerTransform) {

        //move to marker position, not intersection
        Vector3 destination = markerTransform.transform.position;
        // Move avatar and observer. TODO y1?
        avatar.setPosition(new Vector3(destination.getX(), 1, destination.getZ()));
        //TODO rotate avatar.;
        //camera.getCarrier().getTransform().setPosition(new Vector3(destination.getX(), 0, destination.getZ()));
        observer.setPosition(new Vector3(destination.getX(), initialY, destination.getZ()));
        if (markerTransform.transform.rotation != null) {
            observer.setRotation(markerTransform.transform.rotation);
        }


    }

    /**
     * die rote Box verkleinern/vergroessern.
     *
     * @param inc
     */
    private void toggleRedBox(Ray ray, boolean inc) {
        if (ray == null) {
            return;
        }
        List<NativeCollision> intersections = ray.getIntersections();
        //if (intersections.size() > 0) {
        logger.debug("intersections: " + intersections.size());
        for (int i = 0; i < intersections.size(); i++) {
            logger.debug("intersection: " + intersections.get(i).getSceneNode().getName());
            if (intersections.get(i).getSceneNode().getName().equals("red box")) {
                SceneNode pickerobject = new SceneNode(intersections.get(i).getSceneNode());
                Vector3 scale = pickerobject.getTransform().getScale();
                scale = scale.multiply((inc) ? 1.1f : 0.9f);
                pickerobject.getTransform().setScale(scale);
            }
        }
    }

    @Override
    public Dimension getPreferredDimension() {
        if (Platform.getInstance().isDevmode()) {
            return new Dimension(800, 600);
        }
        return null;
    }

    @Override
    public void initSettings(Settings settings) {
        settings.vrready = true;
    }
}