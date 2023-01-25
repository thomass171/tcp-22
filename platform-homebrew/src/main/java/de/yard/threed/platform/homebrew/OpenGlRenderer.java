package de.yard.threed.platform.homebrew;

import de.yard.threed.core.Dimension;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Point;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCamera;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Settings;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ARBMultisample;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import java.util.List;

/**
 * Renderer to OpenGL display with keyboard and mouse input.
 */
public class OpenGlRenderer extends HomeBrewRenderer {

    boolean inited;
    boolean use32 = true;//Jetzt mal Default
    Log logger;
    String window_title = "OpenGL";

    public OpenGlRenderer() {
        this.glcontext = new GlImplLwjgl();
        //not possible before init glcontext.exitOnGLError(glImpl, "setupOpenGL");

    }

    @Override
    protected void collectKeyboardAndMouseEvents(AbstractSceneRunner runner) {
        //distance in mouse movement from the last getDX() call.
        final int dx = Mouse.getDX();
        //distance in mouse movement from the last getDY() call.
        final int dy = Mouse.getDY();
        final int x = Mouse.getX();
        final int y = Mouse.getY();
        if (Mouse.isButtonDown(0)) {
            //TODO Unterscheidung click/press
            runner.mousepress = new Point(x, y);
            // logger.debug("mouse click action. keypressed=" + keypressed + ", location=" + mousepress);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
            System.exit(0);
        }

        while (Keyboard.next()) {
            int key = Keyboard.getEventKey();
            boolean pressed = Keyboard.getEventKeyState();
            //logger.debug("Key " + key + ", pressed=" + pressed);
            int k = KeyCode.lwjgl2Js(key);
            runner./*runnerhelper.*/addKey(k, pressed);
        }
        if (dx != 0 || dy != 0) {
            //24.9.19: mousemove soll die absolute Position ab links/unten enthalten
            runner./*runnerhelper.*/mousemove = new Point(/*dx, dy*/x, y);
        }

    }

    private void showStatistic() {
        logger.debug("totalvertexcnt=" + OpenGlIndexedVBO.totalvertexcnt + " : " + (float) OpenGlIndexedVBO.totalvertexcnt * (12 + 12 + 8) / (1024 * 1024) + " MB");
        logger.debug("texturecnt=" + OpenGlTexture.totaltexturecnt + " : " + (float) OpenGlTexture.totalsize / (1024 * 1024) + " MB");

    }

    @Override
    protected void updateDisplay() {
        Display.update();
    }

    @Override
    public void doRender(HomeBrewMesh mesh, Matrix4 projectionmatrix, Matrix4 viewmatrix, List<OpenGlLight> lights) {
        /*mesh.*/
        render(mesh, projectionmatrix, viewmatrix, lights);
    }

    @Override
    protected void doRender(HomeBrewSceneNode mesh, Matrix4 projectionmatrix, Matrix4 viewmatrix, List<OpenGlLight> lights) {
        //6.3.21 not needed here
    }

    @Override
    public void init(Dimension dimension) {
        initOpenGl(dimension);
    }

    @Override
    public boolean userRequestsTerminate() {
        return Display.isCloseRequested() || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE);
    }

    @Override
    public void close() {
        Display.destroy();
    }

    @Override
    protected void renderScene(List<OpenGlLight> lights,/*HomeBrewScene scene, /*HomeBrew*/NativeCamera camera) {
        // Clear the screen and depth buffer
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        Matrix4 projectionmatrix, viewmatrix;
        if (camera == null) {
            projectionmatrix = new Matrix4();
            viewmatrix = new Matrix4();
        } else {
            projectionmatrix = /*OpenGlMatrix4.toOpenGl*/(camera.getProjectionMatrix());
            viewmatrix = /*OpenGlMatrix4.toOpenGl*/(camera.getViewMatrix());
        }

        //26.4.20 neue Stelle fuer Rendering
        //scene.render(OpenGlContext.getGlContext(), OpenGlMatrix4.fromOpenGl(projectionmatrix), OpenGlMatrix4.fromOpenGl(viewmatrix));
        /*renderer.*/
        render( /*OpenGlMatrix4.fromOpenGl*/(projectionmatrix), /*OpenGlMatrix4.fromOpenGl*/(viewmatrix), lights/*scene.getLights()*/);
    }

    /**
     * 26.4.20: Jetzt drei Methoden, die einst in OpenGlMesh waren.
     *
     * @param projectionmatrix
     * @param viewmatrix
     * @param lights
     */
    private void render(HomeBrewMesh mesh, Matrix4 projectionmatrix, Matrix4 viewmatrix/*, Matrix4 parenttransformation*/, List<OpenGlLight> lights) {
        if (mesh.needsSetup()) {
            mesh.setup(getGlContext());
        }
        Matrix4 transformation = dorender(mesh, getGlContext(), projectionmatrix, viewmatrix/*, parenttransformation*/, lights);

        if (!Settings.linear) {
            Util.notyet();
           /*3.3.16 for (OpenGlObject3D c : children) {
                c.render(glImpl, projectionmatrix, viewmatrix/*, transformation* /, lights);
            }*/
        }
    }

    private Matrix4 dorender(HomeBrewMesh mesh, GlInterface gl, Matrix4 projectionmatrix, Matrix4 viewmatrix/*, Matrix4 parenttransformation*/, List<OpenGlLight> lights) {
        long startttime = System.currentTimeMillis();
        Matrix4 modelmatrix = mesh.getSceneNodeWorldModelMatrix();

        // late init of logger
        if (logger == null) {
            logger = PlatformHomeBrew.getInstance().getLog(OpenGlRenderer.class);
        }
        if (Settings.usevertexarrays) {
            // Bind to the VAO that has all the information about the vertices
            gl.glBindVertexArray(mesh.vaoId);
        }

        for (int i = 0; i < mesh.geo.ea.l_indices.size(); i++) {
           /* OpenGlMaterial material;
            if (i < materials.size()) {
                material = (OpenGlMaterial) materials.get(i);
            } else {
                //Nicht loggen, weil es bei jedem rednern wieder passiert.
                //logger.warn("missing material for list " + i);
                material = (OpenGlMaterial) materials.get(0);
            }*/

            // logger.debug("dorender");
            //projectionmatrix=new OpenGlMatrix4();
            //if ("shuttle".equals(getName())){
                /*logger.debug("modelmatrix="+new Matrix4(modelmatrix).dump(" "));
                logger.debug("projectionmatrix="+new Matrix4(projectionmatrix).dump(" "));
                logger.debug("viewmatrix="+new Matrix4(viewmatrix).dump(" "));*/
            //}

            HomeBrewMaterial material = (HomeBrewMaterial) mesh.getMaterial();
            HomeBrewGeometry geo = mesh.getGeometry();

            if (!mesh.isBroken) {
                boolean success;
                if (material != null) {
                    material.prepareRender(gl, modelmatrix, projectionmatrix, viewmatrix, lights);

                    // Erst nach VAO binden kann man die VertexAttribArray enablen
                    material.sp.glEnableVertexAttribArray(gl);
                    // ivbo.bindBuffer();
                    success = geo.ivbo.drawElements(gl, geo.ivbo, geo.ea, i, false, logger);
                } else {
                    //24.9.19:Ist das wireframe?
                    // ivbo.bindBuffer();
                    mesh.wireframematerial.prepareRender(gl, modelmatrix, projectionmatrix, viewmatrix, lights);
                    // Erst nach VAO binden kann man die VertexAttribArray enablen
                    mesh.wireframematerial.sp.glEnableVertexAttribArray(gl);
                    success = geo.ivbo.drawElements(gl, geo.ivbo, geo.ea, i, true, logger);
                }
                // 25.9.19 der errorcheck ist doch unnötig, der letzte war doch in drawElements
                gl.exitOnGLError(gl, "render");
                // Put everything back to default (deselect)
                //GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
                // ivbo.unbindBuffer();
                if (!success) {
                    logger.error("drawElements failed for mesh. Setting isBroken. sceneNode=" + mesh.getSceneNode().getName());
                    mesh.isBroken = true;
                }
            }

            if (material != null) {
                material.sp.DisableVertexAttribArray(gl);
            }
        }
        gl.glBindVertexArray(0);

        gl.glUseProgram(0);
        gl.exitOnGLError(gl, "loopCycle");

        //Util.logTimestamp(log,"dorender",starttime);
        return modelmatrix;
    }


    private void initOpenGl(Dimension size) {
        // late init of logger
        if (logger == null) {
            logger = PlatformHomeBrew.getInstance().getLog(OpenGlRenderer.class);
        }
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
}
