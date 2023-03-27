package de.yard.threed.platform.jme;

import com.jme3.app.SimpleApplication;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.NativeSocket;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.engine.ecs.ClientBusConnector;
import de.yard.threed.engine.ecs.DefaultBusConnector;
import de.yard.threed.engine.ecs.LoggingSystemTracker;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.outofbrowser.AsyncBundleLoader;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.platform.NativeSceneRunner;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.core.Point;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.World;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.NativeHttpClient;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.engine.platform.common.*;
import de.yard.threed.outofbrowser.SyncBundleLoader;

import java.util.HashMap;

/**
 * Rahmenklasse um eine Scene (Applikation) laufen zu lassen.
 * <p/>
 * Bei JME darf SimpleApplication nur genau einmal instanziiert werden (<a href="http://wiki.jmonkeyengine.org/doku.php/jme3:intermediate:simpleapplication"></a>).
 * Dies scheint die richtige Stelle dafuer.
 * <p/>
 * Created by thomass on 29.05.15.
 */
public class JmeSceneRunner extends AbstractSceneRunner implements NativeSceneRunner {
    Log logger = Platform.getInstance().getLog(JmeSceneRunner.class);
    // SceneRunner ist Singleton
    static private JmeSceneRunner scenerunner = null;
    public JmeCamera jmecamera;
    //3.12.18 super class Dimension dimension;
    public Node rootnode;
    public SimpleApplication simpleApplication;
    Settings scsettings;
    public NativeHttpClient httpClient;

    /**
     * Private, weil es im Grunde ein Singleton ist.
     */
    private JmeSceneRunner(PlatformInternals platformInternals) {
        super(platformInternals);
        logger.info("Building JmeSceneRunner");
    }

    public static JmeSceneRunner init(Configuration configuration) {
        if (scenerunner != null) {
            throw new RuntimeException("already inited");
        }
        // 25.2.21 TODO Es ist doch ein haessliches Coupling (z.B. fuer Testen), dass der Runner die Platform anlegt.
        PlatformInternals platformInternals = PlatformJme.init(configuration);
        scenerunner = new JmeSceneRunner(platformInternals);
        //MA36 ((EngineHelper) PlatformJme.getInstance()).runner = scenerunner;
        //MA36 scenerunner./*((PlatformJme) PlatformJme.getInstance()).*/httpClient = new AirportDataProviderMock();
        return scenerunner;
    }

    public static JmeSceneRunner getInstance() {
        if (scenerunner == null) {
            throw new RuntimeException("not inited");
        }
        return scenerunner;
    }

    @Override
    public void runScene(/*Native*/final Scene scene) {
        logger.debug("java.library.path=" + System.getProperty("java.library.path"));

        scsettings = new Settings();
        scene.initSettings(scsettings);

        SimpleApplication app = new SimpleApplication() {

            @Override
            public void simpleInitApp() {
                logger.info("simpleInitApp");
                simpleApplication = this;
                // Es ist wichtig, dass der AssetManager vor dem init() Aufruf der Scene gesetzt ist.
                JmeResourceManager rm = new JmeResourceManager(assetManager);
                ((PlatformJme) Platform.getInstance()).postInit(rm);

                // Starten mit Standardcamera. Die Camera bleibt aber wohl auch bei Cahe und CameraNode immer dieselbe.
                // Das Setzen des aspect hier duerfte redundant sein.
                // 14.10.15: Defaultwerte aus Scene übernehmen. Aspect wird aus width/height berechnet.
                Camera defaultcamera = getCamera();
                //viewport = viewPort;
                float width = defaultcamera.getWidth();
                float height = defaultcamera.getHeight();
                float aspect = width / height;
                //logger.debug("aspect isType " + aspect);
                float fov = (scsettings.fov == null) ? Settings.defaultfov : scsettings.fov;
                float near = (scsettings.near == null) ? Settings.defaultnear : scsettings.near;
                float far = (scsettings.far == null) ? Settings.defaultfar : scsettings.far;
                //26.11.18 in jmecamera defaultcamera.setFrustumPerspective(fov, aspect, near, far);
                JmeScene.init(this, this.flyCam);
                // 23.2.16: Die FPS Camera jetzt defaultmaessig ausschalten (siehe Wiki)
                this.flyCam.setEnabled(false);

                Platform.getInstance().nativeScene = JmeScene.getInstance();
                Platform.getInstance().sceneRunner = instance;
                initAbstract(null/*JmeScene.getInstance(), rm*/, scene);

                World world = new World();
                //((EngineHelper) PlatformJme.getInstance()).setWorld(new World());

                //10.7.21: Camera geht erst, wenn world in der Scene ist
                Scene.world = world;
                rootnode = getRootNode();
                jmecamera = new JmeCamera(defaultcamera, getRootNode(), fov, aspect, near, far, Settings.backgroundColor, viewPort);
                jmecamera.camera.setName("Main Camera");

                //JavaSceneRunnerHelper.prepareScene(scene, JmeScene.getInstance(),world);
                scene.setSceneAndCamera(JmeScene.getInstance(), world/* ((EngineHelper) Platform.getInstance()).getWorld()*/);

                SyncBundleLoader.preLoad(scene.getPreInitBundle(), rm, Platform.getInstance().bundleResolver);

                initScene();


                postInit();
                // Wenn die Scene sich keine Camera eingerichtet hat, wird jetzt Default FPS einregichtet
                /*24.2.16 if (sc.enableModelCameracalled) {
                    // Da ist bei JME aber z.Z. nicht zu machen, weil eh FPS der Default ist
                }*/


                // ob die keys besser vor oder nach dem scene init erfolgen, ist noch unklar.
                // Na, doch wohl nachher, denn im init() werden sie ja eingerichtet.
                // Einen Handler fuer alle in Unity definierten Keys einrichten.
                int actionindex = 1;
                for (int keycode : KeyCode.unitykeys) {
                    //NativeActionListener lis = sc.actionlistener.get(keycodes);
                    //for (int keycode : keycodes) {
                    //der actionname muss wohl eindeutig sein 
                    int[] keys = getKeyInput(keycode);
                    for (int k : keys) {
                        String actionname = "" + actionindex + "-" + keycode;
                        inputManager.addMapping(actionname, new KeyTrigger(k));
                        inputManager.addListener(new CustomActionListener(keycode), actionname);
                        actionindex++;
                    }
                }
                /*2.3.16 for (int keycode : sc.analoglistener.keySet()) {
                    //der actionname muss wohl eindeutig sein TODO anders machen
                    String actionname = "" + keycode;
                    inputManager.addMapping(actionname, new KeyTrigger(getKeyInput(keycode)));
                    inputManager.addListener(new CustomAnalogListener(sc.analoglistener.get(keycode)), actionname);
                }*/
                //for (NativeMouseMoveListener nmml : sc.mousemovelistener) {

                CustomMouseListener cml = new CustomMouseListener(inputManager, scene);
                inputManager.addMapping("MouseRight", new MouseAxisTrigger(MouseInput.AXIS_X, false));
                inputManager.addListener(cml, "MouseRight");
                inputManager.addMapping("MouseLeft", new MouseAxisTrigger(MouseInput.AXIS_X, true));
                inputManager.addListener(cml, "MouseLeft");
                inputManager.addMapping("MouseUp", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
                inputManager.addListener(cml, "MouseUp");
                inputManager.addMapping("MouseDown", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
                inputManager.addListener(cml, "MouseDown");

                // Mausclick aehnlich wie einen key behandeln.
                inputManager.addMapping("MouseClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
                inputManager.addListener(new MouseActionListener(inputManager), "MouseClick");

                // }

                boolean secondcam = false;
                if (secondcam) {
                    // Setup getSecond, smaller PiP viewer
                    Camera cam2 = getCamera().clone();
                    cam2.setViewPort(.4f, .6f, 0.8f, 1f);
                    cam2.setLocation(new Vector3f(7.50f, 2.01f, 3.81f));
                    cam2.setRotation(new Quaternion(0.00f, 0.99f, -0.04f, 0.02f));
                    ViewPort viewPort2 = renderManager.createMainView("PiP", cam2);
                    viewPort2.setClearFlags(true, true, true);
                    viewPort2.attachScene(rootNode);
                }
                //28.11.18 geht nicht in JmeCamera weil man nicht an den Viewport kommt.
                viewPort.setBackgroundColor(PlatformJme.buildColor(Settings.backgroundColor));
            }

            @Override
            public void simpleUpdate(float tpf) {
                //logger.info("simpleUpdate. tpf=" + tpf);

                // Das ist so aber noch nicht ganz rund.
                final JmeScene sc = (JmeScene) scene.scene;

                scene.deltaTime = tpf;
                /*if (recorder != null){
                    int key = recorder.next();
                }*/
                /*runnerhelper.*/
                prepareFrame(tpf);
                jmecamera.updateCameraModel();
            }
        };

        //7.4.17: Warum disablen? War das nur mal ein Test?
        //ImageUtil.pngcache.disable();
        // Avoid Splash screen
        AppSettings settings = new AppSettings(true);
        //settings.setResolution(1280, 720);
        //settings.setResolution(640, 480);
        //settings.setRenderer(AppSettings.LWJGL_OPENGL3);
        dimension = scene.getPreferredDimension();
        //10.10.18: Wenn es keine Vorgbe gibt, lege ich das fest.
        if (dimension == null) {
            dimension = new Dimension(800, 600);
        }
        settings.setResolution(dimension.getWidth(), dimension.getHeight());
        settings.setBitsPerPixel(32);
        settings.setBitsPerPixel(24);
        // Anti Aliasing multisampling (default ist 0)
        if (scsettings.aasamples != null) {
            settings.setSamples(scsettings.aasamples);
        }
        //target framerate
        if (scsettings.targetframerate != null) {
            settings.setFrameRate(scsettings.targetframerate);
        }
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start(); // start the game

    }

    /*public ViewPort getDefaultViewPort() {
        return viewport;
    }*/

    /**
     * Mapping des JS keycodesa auf den JME keycode.
     *
     * @param keycode
     * @return
     */
    private int[] getKeyInput(int keycode) {
        switch (keycode) {
            case KeyCode.KEY_A:
                return new int[]{KeyInput.KEY_A};
            case KeyCode.KEY_B:
                return new int[]{KeyInput.KEY_B};
            case KeyCode.KEY_C:
                return new int[]{KeyInput.KEY_C};
            case KeyCode.KEY_D:
                return new int[]{KeyInput.KEY_D};
            case KeyCode.KEY_E:
                return new int[]{KeyInput.KEY_E};
            case KeyCode.KEY_G:
                return new int[]{KeyInput.KEY_G};
            case KeyCode.KEY_F:
                return new int[]{KeyInput.KEY_F};
            case KeyCode.KEY_H:
                return new int[]{KeyInput.KEY_H};
            case KeyCode.KEY_I:
                return new int[]{KeyInput.KEY_I};
            case KeyCode.KEY_J:
                return new int[]{KeyInput.KEY_J};
            case KeyCode.KEY_K:
                return new int[]{KeyInput.KEY_K};
            case KeyCode.KEY_L:
                return new int[]{KeyInput.KEY_L};
            case KeyCode.KEY_M:
                return new int[]{KeyInput.KEY_M};
            case KeyCode.KEY_N:
                return new int[]{KeyInput.KEY_N};
            case KeyCode.KEY_P:
                return new int[]{KeyInput.KEY_P};
            case KeyCode.KEY_R:
                return new int[]{KeyInput.KEY_R};
            case KeyCode.KEY_S:
                return new int[]{KeyInput.KEY_S};
            case KeyCode.KEY_T:
                return new int[]{KeyInput.KEY_T};
            case KeyCode.KEY_U:
                return new int[]{KeyInput.KEY_U};
            case KeyCode.KEY_V:
                return new int[]{KeyInput.KEY_V};
            case KeyCode.KEY_W:
                return new int[]{KeyInput.KEY_W};
            case KeyCode.KEY_X:
                return new int[]{KeyInput.KEY_X};
            case KeyCode.KEY_Y:
                //Y/Z PRoblem
                return new int[]{KeyInput.KEY_Z};
            case KeyCode.KEY_Z:
                return new int[]{KeyInput.KEY_Y};
            case KeyCode.KEY_UP:
                return new int[]{KeyInput.KEY_UP};
            case KeyCode.KEY_DOWN:
                return new int[]{KeyInput.KEY_DOWN};
            case KeyCode.KEY_PAGEUP:
                return new int[]{KeyInput.KEY_PGUP};
            case KeyCode.KEY_PAGEDOWN:
                return new int[]{KeyInput.KEY_PGDN};
            case KeyCode.KEY_LEFT:
                return new int[]{KeyInput.KEY_LEFT};
            case KeyCode.KEY_RIGHT:
                return new int[]{KeyInput.KEY_RIGHT};
            case KeyCode.KEY_SPACE:
                return new int[]{KeyInput.KEY_SPACE};
            //17.5.15: Das mit deutscher Tastatur ist irgendwie doof
            case KeyCode.KEY_PLUS:
                return new int[]{KeyInput.KEY_RBRACKET};
            case KeyCode.KEY_DASH:
                return new int[]{KeyInput.KEY_SLASH};
            case KeyCode.KEY_TAB:
                return new int[]{KeyInput.KEY_TAB};
            case KeyCode.KEY_SHIFT:
                return new int[]{KeyInput.KEY_LSHIFT, KeyInput.KEY_RSHIFT};
            case KeyCode.KEY_ONE:
                return new int[]{KeyInput.KEY_1};
            case KeyCode.KEY_TWO:
                return new int[]{KeyInput.KEY_2};
            case KeyCode.KEY_THREE:
                return new int[]{KeyInput.KEY_3};
            case KeyCode.KEY_FOUR:
                return new int[]{KeyInput.KEY_4};
            case KeyCode.KEY_FIVE:
                return new int[]{KeyInput.KEY_5};
            case KeyCode.KEY_SIX:
                return new int[]{KeyInput.KEY_6};
            case KeyCode.KEY_SEVEN:
                return new int[]{KeyInput.KEY_7};
            case KeyCode.KEY_EIGHT:
                return new int[]{KeyInput.KEY_8};
            case KeyCode.KEY_NINE:
                return new int[]{KeyInput.KEY_9};
            case KeyCode.KEY_ZERO:
                return new int[]{KeyInput.KEY_0};
            case KeyCode.Escape:
                return new int[]{KeyInput.KEY_ESCAPE};
            case KeyCode.Ctrl:
                return new int[]{KeyInput.KEY_LCONTROL, KeyInput.KEY_RCONTROL};
        }
        logger.error("unknown keycode " + keycode);
        return new int[]{0};
    }


    class CustomActionListener implements ActionListener {
        //NativeActionListener al;
        int keycode;

        CustomActionListener(/*NativeActionListener al,*/ int keycode) {
            //this.al = al;
            this.keycode = keycode;
        }

        @Override
        public void onAction(String s, boolean keypressed, float tpf) {
            /*runnerhelper.*/
            addKey(keycode, keypressed);
        }
    }

    /**
     * Erstmal nur fuer click release.
     * 14.5.19: Jetzt auch press.
     */
    class MouseActionListener implements ActionListener {
        InputManager inputManager;

        MouseActionListener(InputManager inputManager) {
            this.inputManager = inputManager;
        }

        @Override
        public void onAction(String s, boolean keypressed, float tpf) {
            if (!keypressed) {
                mouseclick = getMousePositionFromCursorPosition(inputManager);
            } else {
                mousepress = getMousePositionFromCursorPosition(inputManager);
                // logger.debug("mouse click action. keypressed=" + keypressed + ", location=" + mousepress);
            }
        }
    }

    /*class CustomAnalogListener implements AnalogListener {
        NativeAnalogListener al;

        CustomAnalogListener(NativeAnalogListener al) {
            this.al = al;
        }

        @Override
        public void onAnalog(String s, float keypressed, float tpf) {
            al.onAnalog(keypressed, tpf);
        }
    }*/

    class CustomMouseListener implements AnalogListener {
        // NativeMouseMoveListener lis;
        InputManager inputManager;
        Scene scene;

        CustomMouseListener(InputManager inputManager, Scene scene) {
            //this.lis = lis;
            this.inputManager = inputManager;
            this.scene = scene;
        }

        @Override
        public void onAnalog(String s, float keypressed, float tpf) {
            /*runnerhelper.*/
            mousemove = getMousePositionFromCursorPosition(inputManager);
        }
    }

    Point getMousePositionFromCursorPosition(InputManager inputManager) {
        Vector2f pos = inputManager.getCursorPosition();
        // 5.1.16: y wird von lwjgl von unten nach oben zaehlend betrachtet! Anders als z.B. ThreeJS. Und auch anders als OpenGL. Dort ist
        // y = 0 oben. Darum hier die Richting umkehren.
        // 8.4.16: Wegen Unity und offenbarer OpenGL Konvention (0,0) links unten jetzt nicht mehr spiegeln.
        //int y = scene.getPreferredDimension().height - Math.round(pos.getY()) - 1;
        int y = (int) Math.round(pos.getY());
        return new Point((int) Math.round(pos.getX()), y);
    }


}


