package de.yard.threed.engine.apps.vr;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.*;
import de.yard.threed.engine.avatar.Avatar;
import de.yard.threed.engine.gui.*;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.engine.platform.common.*;
import de.yard.threed.core.platform.Log;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 17.10.2017: A simple VR Scene as reference analog to ThreeJS VrScene.html (not using ECS).
 * The bar is at height 1m. Depending on the VR configuration (sitting/standing), the sitting view position is
 * initially at the bars height.
 * The avatar (green cube) is at same height as the bar, also in VR (independent from vr y-offset).
 * <p>
 * Provides finetune with (shift) x/y/z.
 * Left ccontroller teleports (only), right controller controls:
 * - scale down red cube (or mouse click). (scale up with menu->start)
 * - Via CP at left controller:
 * -- menu toggle
 * -- finetune up/down
 * -- info
 * -- indicator on/off
 * <p>
 * Outside VR Observer can move with FPC.
 * No traditional HUD in VR. Instead control panel at left controller. Additional menu toggled by 'm' or control panel. No deferred camera, only the VR camera.
 * <p>
 * 14.5.21: Avatar not really needed here, but its good for orientation.
 * 23.11.21: initialY set to 0 instead of BARYPOSITION. With that the "standing position" (Oculus 190cm)
 * is quite correct (with offset -0.9).
 * With "ReferenceSpaceType" 'local' instead of 'local-floor' -0.1 is better than -0.9
 */
public class VrScene extends Scene {
    static Log logger = Platform.getInstance().getLog(VrScene.class);
    Bundle databundle;
    SceneNode bar, box1, ground, platform, secondBar;
    Avatar avatar;
    MenuCycler menuCycler = null;
    MenuItem[] menuitems;

    ControlPanel leftControllerPanel = null;
    Map<String, ButtonDelegate> buttonDelegates = new HashMap<String, ButtonDelegate>();
    VrInstance vrInstance = null;
    Observer observer = null;
    double initialY = 0;
    GridTeleporter gridTeleporter;
    // FirstPersonController not used in VR
    FirstPersonController fps = null;
    StepController controller;

    @Override
    public void init(boolean forServer) {
        processArguments();

        vrInstance = VrInstance.buildFromArguments();

        //erst jetzt ist menu texture geladen
        menuitems = new MenuItem[]{
                new MenuItem("reset", () -> {
                    logger.debug("reset");
                    reset();
                }),
                new MenuItem("scale up", () -> {
                    logger.debug("scale up");
                    Vector3 scale = box1.getTransform().getScale();
                    scale = scale.multiply(1.1f);
                    box1.getTransform().setScale(scale);
                }),
                new MenuItem("scale down", () -> {
                    logger.debug("scale down");
                    Vector3 scale = box1.getTransform().getScale();
                    scale = scale.multiply(0.9f);
                    box1.getTransform().setScale(scale);
                }),
                // No 'teleport', because it only moves the observer, not the avatar.
                new MenuItem("step", () -> {
                    controller.step(true);
                })
        };
        databundle = BundleRegistry.getBundle("data");

        observer = Observer.buildForDefaultCamera();

        // Ein Mesh am Avatar um zu sehen, ob er sich mitbewegt (sollte er nicht). Die Camera ist dann per default in dem Cube.
        // 5.10.19: Bei VR nicht unbedingt, denn da kommt der VR offset ja noch dazu.
        avatar = new Avatar();//Avatar.buildSimple(camera);
        //4.10.19: Wieder autoadjust. Darum auf BALKENYPOSITION (ohne VR, Höhe Dangast Balken) beginnen. Mit VR abzgl. Rift Ground Offset, also 1.3
        //5.5.21: Die Avatar position auf Höhe des Balekn ist schon gut so. Bei aktivieren von VR muss dann der Offset greifen.
        avatar.setPosition(new Vector3(0, VrSceneHelper.BARYPOSITION, 0));
        //camera.attach(avatar.getTransform());
        addToWorld(avatar.getSceneNode());

        if (vrInstance != null) {

            // Without attaching the controller to the obeserver they appear too low. Somehow strange, like many VR aspects.
            observer.attach(vrInstance.getController(0));
            observer.attach(vrInstance.getController(1));
        } else {
            // No VR. just attach observer to avatar? Hmm, keep it separate for now for separate testing of teleport of avatar and moving view point.
            //observer.getTransform().setParent(avatar.getSceneNode().getTransform());
            // use FPC to move around? Conflicts with teleport. But only if attached to avatar.
            fps = new FirstPersonController(getMainCamera().getCarrier().getTransform());
            fps.setMovementSpeed(3.2);
            fps.setRotationSpeed(42.2);
            // avoid conflicts with mouse clicks on control panel
            fps.moveByMouseEnabled = false;
        }
        reset();

        addLight();

        box1 = VrSceneHelper.buildRedBox();
        addToWorld(box1);

        bar = VrSceneHelper.buildBar();
        addToWorld(bar);

        ground = VrSceneHelper.buildGround();
        addToWorld(ground);

        platform = VrSceneHelper.buildPlatform();
        addToWorld(platform);

        secondBar = VrSceneHelper.buildSecondBar();
        addToWorld(secondBar);


        SceneNode locationMarker = VrSceneHelper.buildGroundMarker(Icon.ICON_CLOSE);
        addToWorld(locationMarker);
        SceneNode directionMarker = VrSceneHelper.buildGroundMarker(Icon.ICON_UPARROW);
        addToWorld(directionMarker);
        gridTeleporter = new GridTeleporter(locationMarker, directionMarker);

        buttonDelegates.put("reset", () -> {
            logger.info("reset");
            reset();
        });
        buttonDelegates.put("info", () -> {
            VrInstance.getInstance().dumpDebugInfo();
            Observer.getInstance().dumpDebugInfo();
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
        buttonDelegates.put("cycleLeft", () -> {
            logger.info("cycleLeft");
            controller.step(false);
        });
        buttonDelegates.put("cycleRight", () -> {
            logger.info("cycleRight");
            controller.step(true);
        });

        //menuCycler = new MenuCycler(new MenuProvider[]{new VrMainMenuBuilder(this)});
        menuCycler = new MenuCycler(new MenuProvider[]{new DefaultMenuProvider(getDefaultCamera(), () -> {
            //7.10.19: Mal nicht auf near plane sondern 3 in der Tiefe (wegen VR Verzerrung?)
            //Dann ist es hinterm Balken. Erstmal versuchen.
            //VrMainMenu menu = new VrMainMenu(/*rs.getDefaultCamera(), VrScene.logger,*/ rs.menuitems/*, VrHelper.getController(1)*/);
            //BrowseMenu menu = new BrowseMenu(new DimensionF(3, 2), -3, -2.9, rs.menuitems);
            //GuiGrid m = GuiGrid.buildSingleColumnFromMenuitems(new DimensionF(3, 2), -3, -2.9, rs.menuitems);

            // not too large to avoid overlapping with regular control panel in non VR for avoiding click conflicts.
            ControlPanel m = ControlPanelHelper.buildSingleColumnFromMenuitems(new DimensionF(1.3, 0.7), -3, 0.01, menuitems, Color.LIGHTBLUE);
            ControlPanelMenu menu = new ControlPanelMenu(m);
            return menu;
        })});

        ViewpointList tl = new ViewpointList();
        // its no 'teleport' here, so observer is stepping, not avatar. Teleport is done via GridTeleporter.
        controller = new StepController(/*avatar.getSceneNode()*/Observer.getInstance().getTransform(), tl);

        // on top of platform, not attached to platform
        tl.addEntry(new Vector3(VrSceneHelper.PLATFORM_X_POSITION, VrSceneHelper.SECONDBARYPOSITION, 0), new Quaternion());
        // attached to top of platform
        // y position is the same like unattached? Apparently it is, because platform center is at y=0.
        tl.addEntry(new Vector3(0, VrSceneHelper.SECONDBARYPOSITION, 0), new Quaternion(), platform.getTransform());

        // back to origin, attached to avatar
        tl.addEntry(new Vector3(0, 0, 0), new Quaternion(), avatar.getSceneNode().getTransform());

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

        double tpf = getDeltaTime();

        observer.update();
        VrInstance.update();

        // VR controller might not be available until the user enters VR. So attach the controller the first time
        // they are found.
        if (vrInstance != null && vrInstance.getController(0) != null && leftControllerPanel == null) {

            leftControllerPanel = VrSceneHelper.buildControllerControlPanel(buttonDelegates);
            vrInstance.attachControlPanelToController(vrInstance.getController(0), leftControllerPanel);//.attachControlPanel(leftControllerPanel);
        }


        // right controller
        //rechts verkleinert rote Box (und auch menu 'start' ueber menucycler)
        //24.11.19: Besser nicht hier, sonst weiss man ja nicht, ob das Menu geht.
        //Aber das ist doch für Verkleinern bei hit der Box
        //4.5.21 jetzt eher generell pruefen auf alle hit area.
        if (Input.getControllerButtonDown(10)) {
            logger.debug(" found button down 10 (VR controller trigger)");
            if (vrInstance.getController(1) != null) {
                toggleRedBox(vrInstance.getController(1).getRay(), false);
            }
            Ray ray = vrInstance.getController(1).getRay();
            //controlPanel.checkForClickedArea(ray);
            leftControllerPanel.checkForClickedArea(ray);
        }

        // show destination marker on ground by left controller
        if (vrInstance != null && vrInstance.getController(0) != null) {
            Ray ray = vrInstance.getController(0).getRay();
            if (ray != null) {
                gridTeleporter.updateDestinationMarker(ray, ground, 1);
            }
        }
        Point mouseMovelocation = Input.getMouseMove();
        if (mouseMovelocation != null) {

            Ray ray = observer.buildPickingRay(getDefaultCamera(), mouseMovelocation);
            gridTeleporter.updateDestinationMarker(ray, ground, 1);
        }

        // check for teleport (by mouse or left controller)
        GridTeleportDestination markerTransform = null;

        Point mouseClickLocation = Input.getMouseClick();
        //menucycler.update() is also for keys!
        boolean clickConsumed = menuCycler.update(mouseClickLocation);

        if (mouseClickLocation != null && !clickConsumed) {
            // mouse click wasn't consumed by menu
            // menu geht mit delegate. Aber auch mit direktem mouse click red box verkleinern
            Ray ray = /*avatar*/observer.buildPickingRay(getDefaultCamera(), mouseClickLocation);
            toggleRedBox(ray, false);
            //controlPanel.checkForClickedArea(ray);

            markerTransform = gridTeleporter.updateDestinationMarker(ray, ground, 1);
            if (markerTransform != null) {
                teleport(markerTransform);
            }

            if (vrInstance != null && vrInstance.isEmulated()) {
                // emulate VR trigger
                ray = getDefaultCamera().buildPickingRay(getDefaultCamera().getCarrierTransform(), mouseClickLocation);
                leftControllerPanel.checkForClickedArea(ray);
            }
        }
        //links ist nur noch fuer teleport.
        if (Input.getControllerButtonDown(0)) {
            markerTransform = gridTeleporter.updateDestinationMarker(vrInstance.getController(0).getRay(), ground, 1);
            if (markerTransform != null) {
                teleport(markerTransform);
            }
        }

        if (fps != null) {
            fps.update(tpf);
        }
    }

    /**
     * Teleport applies to avatar and attached observer.
     * No longer a need to keep these in sync, because they are not coupled.
     */
    private void teleport(GridTeleportDestination markerTransform) {

        //move to marker position, not intersection
        Vector3 destination = markerTransform.transform.position;
        // Move avatar and observer. TODO y1?
        avatar.setPosition(new Vector3(destination.getX(), 1, destination.getZ()));
        if (markerTransform.transform.rotation != null) {
            avatar.setRotation(markerTransform.transform.rotation);
        }
        //camera.getCarrier().getTransform().setPosition(new Vector3(destination.getX(), 0, destination.getZ()));
        /*observer.setPosition(new Vector3(destination.getX(), initialY, destination.getZ()));
        if (markerTransform.transform.rotation != null) {
            observer.setRotation(markerTransform.transform.rotation);
        }*/
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
            //logger.debug("intersection: " + intersections.get(i).getSceneNode().getName());
            if (intersections.get(i).getSceneNode().getName().equals("red box")) {
                SceneNode pickerobject = new SceneNode(intersections.get(i).getSceneNode());
                Vector3 scale = pickerobject.getTransform().getScale();
                scale = scale.multiply((inc) ? 1.1f : 0.9f);
                pickerobject.getTransform().setScale(scale);
            }
        }
    }

    /**
     * Return to initial avatar/observer location
     */
    private void reset() {
        logger.debug("Resetting");
        if (vrInstance != null) {

            observer.initFineTune(vrInstance.getOffsetVR());
            observer.setPosition(new Vector3(0, initialY, 0));
        } else {
            // No VR. Keep observer exactly on height of bar/avatar
            observer.setPosition(new Vector3(0, 0, 0));

        }
        Observer.getInstance().getTransform().setParent(avatar.getSceneNode().getTransform());
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