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
using de.yard.threed.core.configuration;

/**
 * Main class. Is attached to game object "MyScriptContainer" for being activated. Triggers everything else.
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
            // 4.5.16: Like in JME no visual effect with IPAD book
            QualitySettings.anisotropicFiltering = AnisotropicFiltering.Enable;
            // Ein AA Effekt ist auch nicht erkennbar. 21.3.17: immer noch nicht. AA wirkt aber wohl nur auf Kanten, nicht in Texturen.
            QualitySettings.antiAliasing = 4;
            // Apparently works. TODO mode to configuration?
            Application.targetFrameRate = 1;

            Environment.SetEnvironmentVariable("HOSTDIR", "/Users/thomas/Sites/tcp-22");

            // UnityScenerunner also builds the platform
            sr = (UnitySceneRunner)UnitySceneRunner.init(ConfigurationByEnv.buildDefaultConfigurationWithEnv(setUp()));
            logger = PlatformUnity.getInstance().getLog(typeof(Main));

            logger.info("Starting Main");

            string scene = Platform.getInstance().getConfiguration().getString("scene");
            // Get the type contained in the name string
            Type type = Type.GetType(scene, true);

            Scene updater = (Scene)Activator.CreateInstance(type);

            //30.1.18 BundleRegistry.registerBundle ("Terrasync", new BtgBundle ("Terrasync"));
            //30.1.18 BtgBundle textures = BtgBundle.buildTextureBundle ();
            //30.1.18 BundleRegistry.registerBundle (textures.name, textures);

            //Log from Android? often blocks UnityLog.setupNetworkstream ("192.168.98.20");
            //UnityLog.setupNetworkstream ("192.168.98.38");
            startUdpListener();

            sr.runScene(updater);
        }
        catch (System.Exception e)
        {
            string st = e.StackTrace;
            string msg = "Exception in Main.Start:" + e.ToString() + st;
            if (logger != null)
            {
                logger.error(msg);
            }
            else
            {
                Debug.Log(msg);
            }
            throw e;
        }
    }

    /**
     * logger not yet available
     */
    private HashMap<String, String> setUp()
    {
        HashMap<String, String> properties = new HashMap<String, String>();

        if (isHandheld())
        {
            checkFileWrite();
            Debug.Log("Application.persistentDataPath is " + Application.persistentDataPath);
            properties.put("FG_HOME", "/storage/external_SD/fghome");
            properties.put("FG_ROOT", "/storage/external_SD/fgroot");
            properties.put("FG_SCENERY", "??/FlightGear/TerraSync");
            properties.put("MY777HOME", "/storage/external_SD/MyAircraft/My-777");
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
        properties.put("argv.visualizeTrack", "true");
        properties.put("argv.enableUsermode", "false");
        properties.put("argv.enableNearView", "true");
        properties.put("argv.initialMaze", "skbn/SokobanWikipedia.txt");

        bool emulateVR = true;
        if (emulateVR)
        {
            properties.put("argv.emulateVR", "true");
            properties.put("argv.offsetVR", "0,0,0");
        }


        properties.put("scene", "de.yard.threed.engine.apps.reference.ReferenceScene");

        bool wayland = false;
        if (wayland)
        {
            properties.put("scene", "de.yard.threed.traffic.apps.BasicTravelScene");

            properties.put("argv.basename", "Wayland");
        }
        bool demo = false;
        if (demo)
        {
            properties.put("argv.basename", "traffic:tiles/Demo.xml");
            properties.put("argv.enableAutomove", "true");
            properties.put("scene", "de.yard.threed.traffic.apps.BasicTravelScene");
        }
        //updater = new de.yard.threed.traffic.DemoScene();


        return properties;
    }

    private bool isHandheld()
    {
        return SystemInfo.deviceType == DeviceType.Handheld;
    }

    // Update is called once per frame
    public void Update()
    {
        Resources.UnloadUnusedAssets();
        System.GC.Collect();
        //detectPressedKeyOrButton ();
        Scene scene = Scene.current;
        if (/*sr.*/scene == null)
        {
            logger.error("sr.scene is null");
        }
        else
        {
            /*sr.*/
            scene.deltaTime = Time.deltaTime;
        }
        sr./*runnerhelper.*/prepareFrame(/*sr.*/scene.deltaTime);
    }

    public void detectPressedKeyOrButton()
    {
        foreach (UnityEngine.KeyCode kcode in System.Enum.GetValues(typeof(UnityEngine.KeyCode)))
        {
            if (UnityEngine.Input.GetKeyDown(kcode))
                Debug.Log("keydetect:KeyCode down: " + kcode);
        }
        float f = UnityEngine.Input.GetAxis("Vertical");
        if (f != 0)
        {
            Debug.Log("keydetect:Vertical axis: " + f);
        }
        f = UnityEngine.Input.GetAxis("Horizontal");
        if (f != 0)
        {
            Debug.Log("keydetect:Horizontal axis: " + f);
        }
    }

    UdpClient client;
    IPEndPoint receivePoint;

    private void startUdpListener()
    {
        try
        {
            Debug.Log("startUdpListener");
            client = new UdpClient(9877);
            receivePoint = new IPEndPoint(IPAddress.Parse("127.0.0.1"), 9877);
            Thread startClient = new Thread(new ThreadStart(StartClient));
            startClient.Start();
            Debug.Log("UdpListener started");

        }
        catch (System.Exception e)
        {
            Debug.Log("Exception: " + e.Message);
        }
    }

    public void StartClient()
    {
        try
        {
            while (true)
            {
                byte[] recData = client.Receive(ref receivePoint);

                System.Text.ASCIIEncoding encode = new System.Text.ASCIIEncoding();
                string data = encode.GetString(recData);
                Debug.Log("received" + data);
                if (data.StartsWith("P"))
                {
                    UnitySceneRunner.getInstance()./*runnerhelper.*/addKey(Int32.Parse(data.Substring(1)), true);
                }
                if (data.StartsWith("R"))
                {
                    UnitySceneRunner.getInstance()./*runnerhelper.*/addKey(Int32.Parse(data.Substring(1)), false);
                }
            }
        }
        catch (System.Exception e)
        {
            Debug.Log("Exception: " + e.Message);
        }
    }

    private void checkFileWrite()
    {
        string fileName = "";
        fileName = Application.persistentDataPath + "/test.txt";
        Debug.Log("filename=" + fileName);
        StreamWriter fileWriter = File.CreateText(fileName);
        fileWriter.WriteLine("Hello world");
        fileWriter.Close();
    }
}
