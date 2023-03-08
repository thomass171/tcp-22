package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.platform.ResourceLoadingListener;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.platform.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 * 05.10.2018: Altkram wie RemoteConsoleScene (die ist erschoben nach FG) entfernt und Platform mit Properties sauber init.
 */
public class Main implements EntryPoint {
    //static Platform platform = PlatformWebGl.getInstance();
    Log logger;// = Platform.getInstance().getLog(Main.class);
    //static boolean withgui;

    /**
     * This isType the entry point method.
     * Die Entscheidung, was gestartet wird, soll aus der URL kommen.
     * <p>
     * Eine Entscheidung über "Production" treffen, um Logging entsprechend zu akivieren. Die Erkennung ist so etwas "Naja". Aber am Port 8888
     * ist es z.Z. nicht erkennbar. 10.10.18: Das heisst jetzt devmode und muss explizit in der URL gesetzt werden.
     */
    public void onModuleLoad() {
        Map<String, List<String>> args = Window.Location.getParameterMap();
        HashMap<String, String> properties = new HashMap<String, String>();
        PlatformWebGl.isDevmode = false;
        for (String arg : args.keySet()) {
            String value = Window.Location.getParameter(arg);
            //6.3.23: Breaking change! No longer use prefix "argv"
            properties.put(arg, value);
            if (arg.equalsIgnoreCase("devmode")) {
                // too early to use Util.isTrue(value) due to logger
                PlatformWebGl.isDevmode = value.equals("1") || value.equalsIgnoreCase("true");
            }
        }
        String href = com.google.gwt.user.client.Window.Location.getHref();

        // Logger is not available before plaform init. So log it native to realize
        // potential init issues.
        WebGlLog.logNative("Properties:");
        for (String prop : properties.keySet()) {
            WebGlLog.logNative(prop + "=" + properties.get(prop));
        }
        if (PlatformWebGl.isDevmode) {
            setDevmode();
        }
        // devmode should be set before init.
        PlatformInternals platformInternals = PlatformWebGl.init(new ConfigurationByProperties(properties));
        Log logger = Platform.getInstance().getLog(Main.class);

        logger.info("Loading GWT Client from " + href + ", devmode=" + Platform.getInstance().isDevmode());

        String scene = properties.get("scene");//com.google.gwt.user.client.Window.Location.getParameter("scene");
        logger.debug("scene=" + scene);
        logger.debug("getHostPageBaseURL=" + GWT.getHostPageBaseURL());
        logger.debug("getModuleBaseURL=" + GWT.getModuleBaseURL());
        logger.debug("getModuleName=" + GWT.getModuleName());
        logger.debug("getModuleBaseForStaticFiles=" + GWT.getModuleBaseForStaticFiles());
        //5.2.23 TODO refactor configuration
        Config.initFromArguments();

        /*GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable throwable) {
                // Iterate over the trace and then print with $wnd.console.log
                logger.error("Throwable: "+throwable.getStackTrace()[0]);
            }
        });*/

        Window.addResizeHandler(resizeEvent -> {
            WebGlSceneRenderer.getInstance().windowResized(resizeEvent);
        });
        //17.7.21 das verschleiert doch nur try {
        if (scene == null) {
            // Dann eine Defaultapplikation (den SceneViewer) starten. 5.1.17: Der SceneViewer ist doch Asbach. Jetzt die RemoteConsole starten
            //die ist jetzt aber alternativ auch eine Scene. 23.1.18:
            //5.10.18: Quark. Einfach eine FM bzw. usage() anzeigen.
            //Scene updater = new LightedRotatingCubeScene();
        } else {
            //testAufruf();
            GwtUtil.showStatus("Initing...");
            Window.setTitle(scene);
            Scene updater = buildSceneUpdater(scene);
            //MA36 Platform.getInstance().getSceneRunner().runScene(updater);
            new WebGlSceneRunner(platformInternals).runScene(updater);
            //18.7.21Ähh, wie muss das denn jetzt?AbstractSceneRunner.instance.runScene(updater);
        }
        /*17.7.21 das verschleiert doch nur } catch (Throwable t) {
            logger.error("Throwable occured:" + t.getMessage() + t.getStackTrace()[0]);
            // Hier kann man gut einen Breakpint setzen, um einen Stacktrace zu bekommen
            t.printStackTrace();
            GWT.log("Throwable occured:", t);
        }*/
    }

    private void testAufruf() {
        MiscWrapper.alert("testAufruf");
        BundleResource resource = new BundleResource("TerraSync/Terrain/e000n50/e007n50/3072816.btg");
        ResourceLoadingListener listener = new ResourceLoadingListener() {
            @Override
            public void onLoad(BundleData bytebuf) {
                //logger.debug("loaded " + resource.getFullName());
                logger.error("onload got " + bytebuf.b.getSize() + " bytes");
                MiscWrapper.alert("onload got " + bytebuf.b.getSize() + " bytes");
                /*5.1.18 try {
                    //LoaderBTG btg = new LoaderBTG(bytebuf.b, options, boptions);
                } catch (InvalidDataException e) {
                    logger.error("Exception:" + e.getMessage(), e);
                    e.printStackTrace();
                }*/
            }

            @Override
            public void onError(int httperrorcode) {

            }
        };
        ((WebGlBundleLoader) Platform.getInstance().bundleLoader).loadRessource(resource, listener, true, false);
    }

    /**
     * Might be overridden by other Main with different ScenePool.
     *
     * @param sceneName
     * @return
     */
    public Scene buildSceneUpdater(String sceneName) {
        Scene updater = ScenePool.buildSceneUpdater(sceneName);
        return updater;
    }

    public static void testAufruf2() {
        if (BundleRegistry.getBundle("data") == null) {
            return;
        }
        //logger.debug("testAufruf2");
        //MiscWrapper.alert("testAufruf2");
        BundleResource resource = new BundleResource(BundleRegistry.getBundle("data"), "flusi/windturbine-gltf/windturbine.gltf");
        //String bundlebasedir = BundleRegistry.getBundleBasedir("data", true);
        String bundlebasedir = BundleResolver.resolveBundle("data", Platform.getInstance().bundleResolver).getPath();
        BundleResource res = new BundleResource(bundlebasedir + "/" + resource.getFullName());
        res.bundle = BundleRegistry.getBundle("data");
        WebGlLoader.loadGLTFbyThreeJS(res, 0, "");
    }

    private static native void setDevmode() /*-{
        $wnd.isDevmode = 1;
        //MA34 $wnd.logger = $wnd.log4javascript.getDefaultLogger();
    }-*/;
}
