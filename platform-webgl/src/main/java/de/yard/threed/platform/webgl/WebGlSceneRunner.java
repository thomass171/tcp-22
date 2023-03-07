package de.yard.threed.platform.webgl;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.World;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.Scene;
import de.yard.threed.core.platform.NativeSceneRunner;
import de.yard.threed.core.Dimension;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Settings;

/**
 * Rahmenklasse um eine Scene (Applikation) laufen zu lassen.
 * <p/>
 * Created by thomass on 29.04.15.
 */
public class WebGlSceneRunner extends AbstractSceneRunner implements NativeSceneRunner {
    Log logger = Platform.getInstance().getLog(WebGlSceneRunner.class);
    //AbstractSceneRunner runnerhelper;
    public WebGlSceneRenderer sceneRenderer;
    public Settings scsettings;

    public WebGlSceneRunner(PlatformInternals platformInternals) {
        super(platformInternals);
    }

    /**
     * 4.5.17: Optional mit RahmenLayout um GUI eibinden zu koennen.
     * Das wirkt irgendwie nicht rund. Scheint aber erstmal zu gehen.
     *
     * @param scene
     */
    @Override
    public void runScene(/*Native*/Scene scene) {

        logger.debug("runScene");
        //HTMLPanel p = new HTMLPanel("");
        //p.getElement().setId(WebGlRenderer.canvasid);
        //FocusPanel focuspanel = new FocusPanel(p);
        CanvasPanel canvasPanel = new CanvasPanel(WebGlRenderer.canvasid);
        canvasPanel.getElement().getStyle().setBackgroundColor("#2062B8");
        RootLayoutPanel.get().add(canvasPanel);

        // 5.8.15: Das mit dem FocusPanel gefaellt mir nicht
        /*focuspanel.addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_NUM_MINUS) {
                    Window.alert("down hit");
                    event.preventDefault();
                }
            }
        });*/

        scsettings = new Settings();
        scene.initSettings(scsettings);

        WebGlScene webglscene = (WebGlScene) Platform.getInstance().getScene();//23.7.21new WebGlScene();
        GwtUtil.showStatus("Initing...");
        //runnerhelper = AbstractSceneRunner.init(webglscene,WebGlResourceManager.getInstance(),scene);
        initAbstract(webglscene,/*WebGlResourceManager.getInstance(),*/scene);
        Dimension dimension = scene.getPreferredDimension();
        if (dimension == null) {
            dimension = new Dimension(Window.getClientWidth(), Window.getClientHeight());
        }
        AbstractSceneRunner.getInstance().dimension = dimension;


        // Die ScreenViewer Camera gehoert nicht zur Scene und wird daher aich nicht an dei
        // Scene geadded.

        //((EngineHelper)PlatformWebGl.getInstance()).setWorld(new World());
        World world = new World();
        Scene.world=world;
        double fov = (scsettings.fov == null) ? Settings.defaultfov : scsettings.fov;
        double near = (scsettings.near == null) ? Settings.defaultnear : scsettings.near;
        double far = (scsettings.far == null) ? Settings.defaultfar : scsettings.far;
        double aspect = (double) dimension.getWidth() / dimension.getHeight();

        WebGlCamera webglcamera = WebGlCamera.buildPerspectiveCamera(webglscene,fov,aspect,near,far);
        webglcamera.setName("Main Camera");
        Transform worldtranform = Scene.getWorld().getTransform();
        //7.5.21 Das mach ich mal nicht mehr. Warum sollte die camera in die world. Wenn, soll das doch die Scene machen. Und in VR stoert es massiv (MA35).
        //14.6.21: Aber als Konvention ist das so vorgesehen (siehe ReferenceSceneTests). Evtl. auch wegen mirror in Unity. Also hier
        //doch erstmal setzen. Für VR wird es spaeter at runtime geloescht.
        WebGlObject3D.setParent(webglcamera.carrier.object3d,((WebGlObject3D)worldtranform.transform).object3d);
        scene.setSceneAndCamera(webglscene, /*webglcamera,*/ Scene.getWorld());
        sceneRenderer = WebGlSceneRenderer.buildInstance((Scene) scene, canvasPanel, scsettings);

        ((WebGlBundleLoader)Platform.getInstance().bundleLoader).preLoad(scene.getPreInitBundle());

        // Preload still running
        sceneRenderer.startRenderLoop();

    }

    /**
     * 23.7.21: Aus platform verschoben
     */
    /*2.8.21 @Override
    public void loadBundle(String bundlename, BundleLoadDelegate delegate, boolean delayed) {
        WebGlResourceManager.getInstance().loadBundlePlatformInternal(bundlename, delegate, delayed);
    }*/

}
