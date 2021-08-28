using UnityEngine;
using System;
using System.Collections;
using System.Threading;
using System.Net;
using System.IO;
using System.Net.Sockets;
using de.yard.threed.platform.unity;
using de.yard.threed.core.platform;
using de.yard.threed.platform;
using de.yard.threed.engine;
using java.util;

/**
 * Main class. Is attached to game object "MyScriptContainer" for being activated. Triggeres everything else.
 */
public class Main : MonoBehaviour
{
    protected UnitySceneRunner sr;
    Log logger;
    public static bool gcpermesh = false;

    // Use this for initialization
    void Start()
    {
        Debug.Log("Starting Main");
        init();
    }

    /**
     *
     */
    public void init()
    {
        try
        {
            // 4.5.16: HAt wie bei JME keinen sichtbaren Effekt beim IPAD Buch
            QualitySettings.anisotropicFiltering = AnisotropicFiltering.Enable;
            // Ein AA Effekt ist auch nicht erkennbar. 21.3.17: immer noch nicht. AA wirkt aber wohl nur auf Kanten, nicht in Texturen.
            QualitySettings.antiAliasing = 4;
            // Ob das mit der Framerate was bringt? Scheinbar ja. TODO woanders hin
            Application.targetFrameRate = 1;

            // 16.11.16: Auch hier den init mit Properties.
            HashMap<String, String> properties = new HashMap<String, String>();
            //FlightGearInit.initProperties (properties);

            //TODO adjust this to your local environment, possibly OS dependent
            //if (SystemInfo.operatingSystemFamily == OperatingSystemFamily.Windows)
            //{
            //  
            //}
            Environment.SetEnvironmentVariable("HOSTDIR", "/Users/thomas/Sites/tcp-22");
            string hostdir = Environment.GetEnvironmentVariable("HOSTDIR");
            if (hostdir == null)
            {
                Debug.Break();
                throw new Exception("HOSTDIR not set");
            }
            Debug.Log("using HOSTDIR=" + hostdir);

            if (isHandheld())
            {
                properties.put("CACHEDIR", Application.persistentDataPath/*"/storage/external_SD/ncache"*/);
                properties.put("BUNDLEDIR", hostdir + "/bundles");

            }
            else
            {
                //30.1.18:  Das BUNDLEDIR gilt so für MACOS und Win10. Ach, besser nicht
                properties.put("BUNDLEDIR", hostdir + "/bundles");

            }
            //25.8.21 TODO check that bundle dir exists

            // Der UnityScenerunner baut auch die Platform
            sr = (UnitySceneRunner)UnitySceneRunner.init(properties);
            logger = PlatformUnity.getInstance().getLog(typeof(Main));

            //30.1.18 BundleRegistry.registerBundle ("Terrasync", new BtgBundle ("Terrasync"));
            //30.1.18 BtgBundle textures = BtgBundle.buildTextureBundle ();
            //30.1.18 BundleRegistry.registerBundle (textures.name, textures);

            logger.info("Starting Main");
            if (isHandheld())
            {
                checkFileWrite();
                Debug.Log("Application.persistentDataPath is " + Application.persistentDataPath);
                (Platform.getInstance()).setSystemProperty("FG_HOME", "/storage/external_SD/fghome");
                (Platform.getInstance()).setSystemProperty("FG_ROOT", "/storage/external_SD/fgroot");
                (Platform.getInstance()).setSystemProperty("FG_SCENERY", "??/FlightGear/TerraSync");
                (Platform.getInstance()).setSystemProperty("MY777HOME", "/storage/external_SD/MyAircraft/My-777");
            }
            else
            {
                /* jetzt ueber flightgerinitPlatform.getInstance ().setSystemProperty ("FG_HOME", "/Users/thomas/Library/Application Support/FlightGear");
                Platform.getInstance ().setSystemProperty ("FG_ROOT", "/Applications/FlightGear-2016.4.3.app/Contents/Resources/data");
                Platform.getInstance ().setSystemProperty ("FG_SCENERY", "/Users/thomas/Library/Application Support/FlightGear/TerraSync");
                Platform.getInstance ().setSystemProperty ("MY777HOME", "/Users/thomas/Projekte/FlightGear/MyAircraft/My-777");*/
            }
            //((EnginePlatform)Platform.getInstance ()).setSystemProperty ("argv.basename", "B55-B477");
            //((EnginePlatform)Platform.getInstance ()).setSystemProperty ("argv.basename", "B55-B477-small");
            (Platform.getInstance()).setSystemProperty("argv.visualizeTrack", "true");
            (Platform.getInstance()).setSystemProperty("argv.enableUsermode", "false");
            (Platform.getInstance()).setSystemProperty("argv.enableNearView", "true");
            //haengt haeufig UnityLog.setupNetworkstream ("192.168.98.20");
            //UnityLog.setupNetworkstream ("192.168.98.38");
            startUdpListener();

            // Jetzt ist die Initialisierung durch und die Applikation wird gestartet.

            Scene updater = new de.yard.threed.engine.apps.reference.ReferenceScene();//TODO ScenePool.buildSceneUpdater(scene);
            //Scene updater = new de.yard.threed.maze.SokobanScene();//TODO ScenePool.buildSceneUpdater(scene);
            //Scene updater = new de.yard.threed.apps.ShowroomScene ();//TODO ScenePool.buildSceneUpdater(scene);
            //Scene updater = new de.yard.threed.client.ModelViewScene ();//TODO ScenePool.buildSceneUpdater(scene);
            //Scene updater = new de.yard.threed.apps.flusi.CockpitScene ();
            //Scene updater = new de.yard.threed.apps.flusi.SceneryViewerScene ();
            //Scene updater = new de.yard.threed.apps.flusi.TravelScene ();
            //Scene updater = new de.yard.threed.apps.osm.OsmScene ();
            //Scene updater = new de.yard.threed.apps.osm.FlatTravelScene ();
            //Scene updater = new de.yard.threed.apps.railing.RailingScene ();
            //Scene updater = new de.yard.threed.apps.flusi.ModelPreviewScene ();
            //Scene updater = new de.yard.threed.apps.flusi.OsmSceneryViewScene ();
            //braucht basename in env
            //Scene updater = new de.yard.threed.apps.osm.OsmSceneryScene ();
            //Scene updater = new de.yard.threed.apps.reference.VrScene ();
            sr.runScene (updater);
        } catch (System.Exception e) {
            string st = e.StackTrace;
            string msg = "Exception in Main.Start:" + e.ToString () + st;
            if (logger != null) {
                logger.error (msg);
            } else {
                Debug.Log (msg);
            }
            throw e;
        }
    }

    private bool isHandheld ()
    {
        return SystemInfo.deviceType == DeviceType.Handheld;
    }

    // Update is called once per frame
    public void Update ()
    {
        Resources.UnloadUnusedAssets ();
        System.GC.Collect ();
        //detectPressedKeyOrButton ();
        Scene scene = Scene.current;
        if (/*sr.*/scene == null) {
            logger.error ("sr.scene is null");
        } else {
            /*sr.*/scene.deltaTime = Time.deltaTime;
        }
        sr./*runnerhelper.*/prepareFrame (/*sr.*/scene.deltaTime);
    }

    public void detectPressedKeyOrButton ()
    {
        foreach (UnityEngine.KeyCode kcode in System.Enum.GetValues(typeof(UnityEngine.KeyCode))) {
            if (UnityEngine.Input.GetKeyDown (kcode))
                Debug.Log ("keydetect:KeyCode down: " + kcode);
        }
        float f = UnityEngine.Input.GetAxis ("Vertical");
        if (f != 0) {
            Debug.Log ("keydetect:Vertical axis: " + f);
        }
        f = UnityEngine.Input.GetAxis ("Horizontal");
        if (f != 0) {
            Debug.Log ("keydetect:Horizontal axis: " + f);
        }
    }

    UdpClient client;
    IPEndPoint receivePoint;

    private void startUdpListener ()
    {
        try {
            Debug.Log ("startUdpListener");
            client = new UdpClient (9877);
            receivePoint = new IPEndPoint (IPAddress.Parse ("127.0.0.1"), 9877);
            Thread startClient = new Thread (new ThreadStart (StartClient));
            startClient.Start ();
            Debug.Log ("UdpListener started");

        } catch (System.Exception e) {
            Debug.Log ("Exception: " + e.Message);
        }
    }

    public void StartClient ()
    {
        try {
            while (true) {
                byte[] recData = client.Receive (ref receivePoint);

                System.Text.ASCIIEncoding encode = new System.Text.ASCIIEncoding ();
                string data = encode.GetString (recData);
                Debug.Log ("received" + data);
                if (data.StartsWith ("P")) {
                    UnitySceneRunner.getInstance ()./*runnerhelper.*/addKey (Int32.Parse (data.Substring (1)), true);
                }
                if (data.StartsWith ("R")) {
                    UnitySceneRunner.getInstance ()./*runnerhelper.*/addKey (Int32.Parse (data.Substring (1)), false);
                }
            }
        } catch (System.Exception e) {
            Debug.Log ("Exception: " + e.Message);
        }
    }

    private void checkFileWrite ()
    {
        string fileName = "";
        fileName = Application.persistentDataPath + "/test.txt";
        Debug.Log ("filename=" + fileName);
        StreamWriter fileWriter = File.CreateText (fileName);
        fileWriter.WriteLine ("Hello world");
        fileWriter.Close ();
    }
}
