using UnityEngine;
using System.Collections;
using System;
using java.lang;
using de.yard.threed.core.platform;
using de.yard.threed.engine.platform;

using de.yard.threed.engine;
using java.util;
using de.yard.threed.engine.platform.common;
using de.yard.threed.outofbrowser;

namespace de.yard.threed.platform.unity
{
    /**
     * Das Unity Pendant zu den anderen SceneRunnern. Wird so bei Unity zwar nicht gebraucht, dient aber der
     * Vereinheitlichung der Abläufe.
     */
    public class UnitySceneRunner  : AbstractSceneRunner,  NativeSceneRunner
    {
        Log logger = Platform.getInstance ().getLog (typeof(UnitySceneRunner));
        // muss synced sein
        //Vector<CompletedAsync> ascyncallbacklist = new Vector();
        // SceneRunner ist Singleton
        static private UnitySceneRunner scenerunner = null;
        //private static ViewPort viewport;
        //public  RunnerHelper runnerhelper;
        //9.8.21 public Scene scene;
        UnityCamera unityCamera;
        Settings scsettings;
        public NativeHttpClient httpClient;

        /**
         * Private, weil es im Grunde ein Singleton ist.
         */
        private UnitySceneRunner (PlatformInternals platformInternals):base(platformInternals)
        {
            logger.info ("Building UnitySceneRunner");
        }

        public static UnitySceneRunner init (HashMap<String, String> properties)
        {
            if (scenerunner != null) {
                throw new RuntimeException ("already inited");
            }
            PlatformInternals platformInternals=PlatformUnity.init (properties);
            scenerunner = new UnitySceneRunner (platformInternals);
            //MA36 (PlatformUnity.getInstance ()).runner = scenerunner;
            scenerunner./*((PlatformJme) PlatformJme.getInstance()).*/httpClient = null;//31.3.21 new AirportDataProviderMock ();

            return scenerunner;
        }

        public static UnitySceneRunner getInstance ()
        {
            if (scenerunner == null) {
                throw new RuntimeException ("not inited");
            }
            return scenerunner;
        }

        public void runScene (Scene scene)
        {           
            //9.8.21 this.scene = scene;
            UnityScene.init ();
                    
            Settings scsettings = new Settings();
            scene.initSettings(scsettings);

            //runnerhelper = RunnerHelper.init (UnityScene.getInstance (),UnityResourceManager.getInstance(),scene);
            Platform.getInstance ().nativeScene = UnityScene.getInstance ();
            initAbstract (null/*UnityScene.getInstance (),UnityResourceManager.getInstance()*/,scene);
             
            //((EnginePlatform)PlatformUnity.getInstance ()).world = new World (true);
            World world = new World (true);
            Scene.world = world;

            float fov = (float)((scsettings.fov==null)?Settings.defaultfov:scsettings.fov);
            float near = (float)((scsettings.near==null)?Settings.defaultnear:scsettings.near);
            float far = (float)((scsettings.far==null)?Settings.defaultfar:scsettings.far);

            unityCamera = new UnityCamera (UnityEngine.Camera.main, null);
            unityCamera.camera.nearClipPlane = near;
            unityCamera.camera.farClipPlane = far;
            unityCamera.camera.fieldOfView = fov;

            scene.setSceneAndCamera (UnityScene.getInstance (), /*unityCamera,*/ world);

            //0.8.21 UnityScene sc = (UnityScene)scene.scene;

            SyncBundleLoader.preLoad (scene.getPreInitBundle (), new UnityResourceManager(), Platform.getInstance().bundleResolver);
            /*foreach (string bname in scene.getPreInitBundle()){
                string bundlename = bname;
                bool delayed = false;
                if (bundlename.EndsWith("-delayed")) {
                    //TODO das mit delay ist ne kruecke
                    bundlename = bundlename.Replace("-delayed", "");
                    delayed = true;
                }
                BundleLoaderExceptGwt.loadBundleSyncInternal(bundlename,null,delayed,null,AbstractSceneRunner.getInstance().getResourceManager());
            }*/
            scene.init ();
            /*runnerhelper.*/postInit();

        }


    }
}