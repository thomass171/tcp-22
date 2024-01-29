package de.yard.threed.platform.webgl;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
//import de.yard.threed.core.platform.NativeAsyncRunner;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.World;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.Scene;
import de.yard.threed.core.platform.NativeSceneRunner;
import de.yard.threed.core.Dimension;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.InitExecutor;
import de.yard.threed.engine.platform.common.NativeInitChain;
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
        Platform.getInstance().sceneRunner = this;

        // Die ScreenViewer Camera gehoert nicht zur Scene und wird daher aich nicht an dei
        // Scene geadded.

        //((EngineHelper)PlatformWebGl.getInstance()).setWorld(new World());
        /*29.12.23 World world = new World();
        Scene.setWorld(world);*/
        double fov = (scsettings.fov == null) ? Settings.defaultfov : scsettings.fov;
        double near = (scsettings.near == null) ? Settings.defaultnear : scsettings.near;
        double far = (scsettings.far == null) ? Settings.defaultfar : scsettings.far;
        double aspect = (double) dimension.getWidth() / dimension.getHeight();

        WebGlCamera webglcamera = WebGlCamera.buildPerspectiveCamera(webglscene, fov, aspect, near, far);
        webglcamera.setName("Main Camera");
        Transform worldtranform = Scene.getCurrent().getWorld().getTransform();
        //7.5.21 Das mach ich mal nicht mehr. Warum sollte die camera in die world. Wenn, soll das doch die Scene machen. Und in VR stoert es massiv (MA35).
        //14.6.21: Aber als Konvention ist das so vorgesehen (siehe ReferenceSceneTests). Evtl. auch wegen mirror in Unity. Also hier
        //doch erstmal setzen. Für VR wird es spaeter at runtime geloescht.
        WebGlObject3D.setParent(webglcamera.carrier.object3d, ((WebGlObject3D) worldtranform.transform).object3d);
        scene.setSceneAndCamera(webglscene, /*webglcamera,*/ Scene.getCurrent().getWorld());
        sceneRenderer = WebGlSceneRenderer.buildInstance((Scene) scene, canvasPanel, scsettings);

        // Wait for init chain to be complete. Will start render loop than.
        enterInitChain(scene.getPreInitBundle());
    }

    /**
     * Override the default init chain with a asnyc runner.
     */
    @Override
    public NativeInitChain buildInitChain(InitExecutor initExecutor) {
        return new WebGlInitChain(initExecutor/*,new WebGlAsyncRunner()*/);
    }

    /**
     * Einen Looback einrichten, der immer wieder zum Rendern aufgerufen wird.
     * Die Camera gibt es erst nach dem Init.
     * <p>
     * Only called once as a trigger for the loopback.
     */
    @Override
    public void startRenderLoop() {
        //20.12.23: init was in preload once for scene init. Now here
        sceneRenderer.init();

        // don't continue using AnimationScheduler but switch to ThreeJs animationloop
        // updateRender(canvaspanel, cameras);
        logger.info("Switch to ThreeJs animationloop");
        sceneRenderer.renderer.activeAnimationLoop();

    }

    @Override
    public void sleepMs(int millis) {
        throw new RuntimeException("not implemented");
    }
}
