package de.yard.threed.platform.homebrew;

import de.yard.threed.engine.SceneMode;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.engine.World;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Dimension;
import de.yard.threed.engine.Scene;
import de.yard.threed.core.platform.NativeSceneRunner;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.SceneRenderer;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.outofbrowser.SyncBundleLoader;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.ARBMultisample;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import java.util.HashMap;

/**
 * In eigenem Thread, um nicht den awtEvnet Thread zu blockieren (wirklich)?
 * 30.1.15: Wenn es nicht als eigener Thread läuft, kann man z.B. nicht parallel ein JFrame (ModelViewer aufhaben).
 * Darum doch als Thread.
 * 10.4.15: Der Thread ist jetzt im Renderloop Start
 * <p/>
 * Date: 14.02.14
 * Time: 16:09
 */
public class HomeBrewSceneRunner extends AbstractSceneRunner implements NativeSceneRunner {
    Scene scene;
    boolean inited = false;
    // int width, height;
    boolean use32 = true;//Jetzt mal Default
    String window_title = "OpenGL";
    //public ShaderProgram shaderProgram;
    Log logger = Platform.getInstance().getLog(HomeBrewSceneRunner.class);
    // Die Liste enthält die gerade laufenden Animationen. Beendete werden entfernt.
    private static HomeBrewSceneRunner instance = null;
    /*22.2.21 OpenGL*/ SceneRenderer renderer;

    private HomeBrewSceneRunner(PlatformInternals platformInternals){
        super(platformInternals);
    }

    public static HomeBrewSceneRunner init(HashMap<String, String> properties) {
        if (instance != null) {
            throw new RuntimeException("already inited");
        }
        // Die Reihenfolge ist wichtig um NPE zu vermeiden
        PlatformInternals platformInternals =  PlatformHomeBrew.init(properties);
        PlatformHomeBrew pl = (PlatformHomeBrew) Platform.getInstance();
        // context in den Platform init.
        //OpenGlContext.init(new GlImplLwjgl());
        pl.setRenderer(new GlImplLwjgl());
        ((PlatformHomeBrew) PlatformHomeBrew.getInstance()).logfactory = new Log4jLogFactory();
        ((PlatformHomeBrew) PlatformHomeBrew.getInstance()).logger = ((PlatformHomeBrew) PlatformHomeBrew.getInstance()).logfactory.getLog(PlatformHomeBrew.class);
        instance = new HomeBrewSceneRunner(platformInternals/*width, height, scene, /*FirstPerson* / camera, canvas*/);
        //MA36((Platform)PlatformHomeBrew.getInstance()).runner = instance;
        return instance;
    }

    public static HomeBrewSceneRunner getInstance() {
        if (instance == null) {
            throw new RuntimeException("not inited");
        }
        return instance;
    }

    @Override
    public void runScene(Scene scene) {
        //24.9.19: Die Scene braucht er später - anders als JME - für tpf.
        this.scene = scene;
        Settings scsettings = new Settings();
        scene.initSettings(scsettings);
        dimension = scene.getPreferredDimension();
        //10.10.18: Wenn es keine Vorgbe gibt, lege ich das fest.
        //
        if (dimension == null) {
            dimension = new Dimension(800, 600);
        }
        float aspect = (float)dimension.width / dimension.height;
        float fov = (scsettings.fov == null) ? Settings.defaultfov : scsettings.fov;
        float near = (scsettings.near == null) ? Settings.defaultnear : scsettings.near;
        float far = (scsettings.far == null) ? Settings.defaultfar : scsettings.far;

        initOpenGl(dimension);
        OpenGlContext.getGlContext().exitOnGLError(OpenGlContext.getGlContext(), "setupOpenGL");

        Scene.world = new World();
        //HomeBrewScene openglscene = new HomeBrewScene();
        initAbstract(null,/*21.1.23 openglscene,/*((PlatformHomeBrew)PlatformHomeBrew.getInstance()).resourcemanager,*/scene);
        //((Platform)PlatformHomeBrew.getInstance()).setWorld(new World());

        HomeBrewCamera camera = new HomeBrewPerspectiveCamera(fov,aspect,near,far);
        camera.setName("Main Camera");
        camera.getCarrier().setName("Main Camera Carrier");

        //JavaSceneRunnerHelper.prepareScene(scene,openglscene,Scene.world);
        scene.setSceneAndCamera(((Platform)PlatformHomeBrew.getInstance()).nativeScene, Scene.world/* ((EngineHelper) Platform.getInstance()).getWorld()*/);
        /*BundleLoaderExceptGwt*/
        SyncBundleLoader.preLoad(scene.getPreInitBundle(),new DefaultResourceReader(),Platform.getInstance().bundleResolver);

        //20.1.23 init was missing??
        scene.init(SceneMode.forMonolith());

        postInit();

        startRenderloop((HomeBrewScene) scene.scene, camera);
    }

    private void showStatistic() {
        logger.debug("totalvertexcnt=" + OpenGlIndexedVBO.totalvertexcnt + " : " + (float) OpenGlIndexedVBO.totalvertexcnt * (12 + 12 + 8) / (1024 * 1024) + " MB");
        logger.debug("texturecnt=" + OpenGlTexture.totaltexturecnt + " : " + (float) OpenGlTexture.totalsize / (1024 * 1024) + " MB");

    }


    /**
     * Falls es nicht als Thread laeuft.
     */
    /*siehe Kommentar oben zu Thread.public void startRenderloop() {
        run();
    }*/

    /**
     * Klasse Renderloop.
     * Ist hier nur noch der Einstieg (Convenience)
     */
    //@Override
    private void startRenderloop(HomeBrewScene scene, HomeBrewCamera camera) {
        renderer = OpenGLSceneRenderer.buildInstance(this, scene, camera);

        //Mouse.setClipMouseCoordinatesToWindow(true);
        //hide the mouse
        //Mouse.setGrabbed(true);

        // keep looping till the display window isType closed the ESC key isType down
        while (!Display.isCloseRequested() &&
                !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            long starttime = System.currentTimeMillis();

            renderer.renderFrame();
            //logger.debug("renderFrame took "+(System.currentTimeMillis()-starttime)+" ms.");

            //einbremsen TODO schaltbar. 29.8.16: bischen weniger bremsen
            try {
                //logger.info("sleeping 500");
                Thread.sleep(0/*200/*2000*/);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //showStatistic();

        }

        Display.destroy();
        System.exit(0);

    }

    private void initOpenGl(Dimension size) {
        try {
            //ContextCapabilities ctxCaps = GLContext.getCapabilities();
            /*aus jmonkey if (ctxCaps.OpenGL20) {
                caps.add(Caps.OpenGL20);
                if (ctxCaps.OpenGL21) {
                    caps.add(Caps.OpenGL21);
                    if (ctxCaps.OpenGL30) {
                        caps.add(Caps.OpenGL30);
                        if (ctxCaps.OpenGL31) {
                            caps.add(Caps.OpenGL31);
                            if (ctxCaps.OpenGL32) {
                                caps.add(Caps.OpenGL32);
                            }
                        }
                    }
                }
            } */
            logger.debug("java.library.path=" + System.getProperty("java.library.path"));
            logger.debug("java.class.path=" + System.getProperty("java.class.path"));

            // In Fusion laeuft das eh wegen nur OpenGL 2.1 nicht
            if (System.getProperty("os.name").contains("indows")) {
                System.setProperty("org.lwjgl.librarypath", "Y:/tmp/LwjglRuntime");
            }
            // MAcos 10.9 (meins) muesste Opengl 4.1 koennen, laut Apple Seite.
            // Setup an OpenGL context with API version 3.2
            // Nur mit false/false lassen sich auch die alten !! Funktinonen nutzen (bracuht z.B. glulookat, aber nur wenn das
            // implementiert ist. Bei Macos ist es nicht.
            Settings settings = new Settings();
            if (use32) {
                int alphaSize = 8;
                // 9.3.16 16->24. Mit 16 geht FlightScene nicht. Klären. Mit DepthFunc gehts aber auch mit 16.
                int depthSize = 16; // 16 soll default sein
                int samples = 0;
                if (settings.aasamples != null) {
                    samples = settings.aasamples;
                }
                PixelFormat pixelFormat = new PixelFormat().withDepthBits(depthSize).withSamples(samples);
                ContextAttribs contextAtrributes = new ContextAttribs(3, 2)
                        .withForwardCompatible(true)
                        .withProfileCore(true)
                        .withProfileCompatibility(false);
                //2.2.16: OpenGl 3.0 statt 3.2 (GLSL 150), um GLSL 1.2 verwenden zu koennen. vertex arrays gehen dann nicht.
                //Profiles gibt es erst ab 3.2
                //contextAtrributes = new ContextAttribs(3, 0)
                //      .withForwardCompatible(true);
                //usevertexarrays = false;
                //contextAtrributes = null;

                Display.setDisplayMode(new DisplayMode(size.width, size.height));
                Display.setTitle(window_title);
               /*OGL if (canvas == null) {
                } else {

                    Display.setParent(canvas);
                }*/
                Display.create(pixelFormat, contextAtrributes);
                GL11.glViewport(0, 0, size.width, size.height);
                // 9.3.16: Laut Doku muesste es mit backgroundColor gehen, tuts aber nicht
                if (Settings.backgroundColor != null) {
                    Display.setInitialBackground(Settings.backgroundColor.getR(), Settings.backgroundColor.getG(), Settings.backgroundColor.getB());
                    GL11.glClearColor(Settings.backgroundColor.getR(), Settings.backgroundColor.getG(), Settings.backgroundColor.getB(), 1);
                }
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                //wofuer ist das denn genau? Anscheinend sehr wichtig bei grossen Dimensionen! ThreeJS setzt das auch so.
                GL11.glDepthFunc(GL11.GL_LEQUAL);

                // Backface Culling aktivieren. Eigentlich sollte das doch
                // Default sein. Naja, ist es aber wohl nicht
                // 10.4.15: Scheinbar nicht so generell einschaltbar. Bei Tubes z.B. ist ja die Rückseite von innen sichtbar. Dann
                // ist backface culling unguenstig. Bei gegenueberliegenden Papierseiten (Leaf) ist es wieder erforderlich
                // um durchscheinen zu verhindern.
                // 10.3.16: Es muss aber sinnvollerweise per Default aktiv sein. Das machen andere auch so.
                boolean backfaceculling = true;
                if (backfaceculling) {
                    GL11.glEnable(GL11.GL_CULL_FACE);
                    GL11.glCullFace(GL11.GL_BACK);
                }
                //strange effekte GL11.glDepthRange(0, 0.99f);
                // MSAA MultiSampleAntiAliasing enablen (ist Teil von OpenGL 1.3 und angeblich sogar Default)
                // Wird aber wohl trotzdem als Extension behandelt.
                //GL11.glEnable(GL13.GL_MULTISAMPLE);
                //GL11.glEnable(GL12.GL_MULTISAMPLE_ARB);
                //GL11.glHint(GL13.GL_MULTISAMPLE_FILTER_HINT_NV, GL11.GL_NICEST);
                if (samples > 0) {
                    GL11.glEnable(ARBMultisample.GL_MULTISAMPLE_ARB);
                }
                // GL11.glEnable(GL13.GL_MULTISAMPLE_);
                //GL11.glDisable(GL13.GL_MULTISAMPLE);

            } else {
                // init OpenGL
                /**GL11.glMatrixMode(GL11.GL_PROJECTION);
                 GL11.glLoadIdentity();
                 GL11.glOrtho(0, 800, 0, 600, 1, -1);
                 GL11.glMatrixMode(GL11.GL_MODELVIEW);
                 **/

                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glMatrixMode(GL11.GL_PROJECTION);

                GL11.glLoadIdentity();
                //gulPerspective bringt, dass 3D aktiv.
                //Parameter: fov, aspect, zNear, zFar
                //FOV sollte glaube ich jeder kennen! (http://de.wikipedia.org/wiki/FOV)
                //Der aspect ist einfach nur width/height.
                //zNear ist wie Nahe ein Objekt minimal sein muss um es zu rendern.
                //zNear ist wie Nahe ein Objekt maximal sein muss um es zu rendern.
              /*OGL  GLU.gluPerspective(45, (float) Display.getWidth() / (float) Display.getHeight(), 0.3f, 1000);

                GL11.glMatrixMode(GL11.GL_MODELVIEW);*/
            }
            logger.debug("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));


            inited = true;
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    /*11.11.14 private void setup(GlInterface gl, Scene scene) {
        scene.setup(gl);
    }*/


    /**
     * Ein Key kann nur in stillpressed auftauchen, wenn er vorher in pressed war.
     */
   /*OGL  private void refresh(ActiveKeys activekeys) {
        // Pruefen, ob die zuletzt  gepressten immer noch gepressed sind
        List<Key> stillpressed = new ArrayList<Key>();
        for (Key key : activekeys.getStillpressedKeys()) {
            if (Keyboard.isKeyDown(key.keycode))
                stillpressed.add(key);
        }
        for (Key key : activekeys.getPressedKeys()) {
            if (Keyboard.isKeyDown(key.keycode))
                stillpressed.add(key);
        }
        activekeys.setStillpressedKeys(stillpressed);

        List<Key> pressed = new ArrayList<Key>();
        while (Keyboard.next()) {
            //logger.debug("next:");
            if (Keyboard.getEventKeyState()) {
                // pressed
                pressed.add(new Key(Keyboard.getEventKey(), Keyboard.getEventCharacter()));
                   /* if (Keyboard.getEventKey() == Keyboard.KEY_A) {
                        logger.debug("A Key Pressed");
                    } * /
            } else {
                // released
                // verwenden wir erstmal nicht
                    /*if (Keyboard.getEventKey() == Keyboard.KEY_A) {
                        logger.debug("A Key Released");
                    } * /
            }
        }
        activekeys.setPressedKeys(pressed);

        String s_pressed = "";
        for (Key key : pressed) {
            s_pressed += "" + key.character + ",";
        }
        if (s_pressed.length() > 0)
            logger.debug("pressed:" + s_pressed);

        String s_stillpressed = "";
        for (Key key : stillpressed) {
            s_stillpressed += "" + key.character + ",";
        }
        //logger.debug("stillpressed:"+s_stillpressed);

      
    }*/


}
