package de.yard.threed.platform.homebrew;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.SceneRenderer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * In eigenem Thread, um nicht den awtEvnet Thread zu blockieren (wirklich)?
 * 30.1.15: Wenn es nicht als eigener Thread läuft, kann man z.B. nicht parallel ein JFrame (ModelViewer aufhaben).
 * Darum doch als Thread.
 * 10.4.15: Der Thread ist jetzt im Renderloop Start
 * Extrahiert aus OpenGLRenderer. Gedacht, um standalone ein Spiel laufen zu lassen, rein mit lwjgl und
 * vor allem ohne Swing/awt (auch ohne Monitor).
 * <p/>
 * Ob das ein eigenr Thread sein sollte ist jetzt wieder nicht ganz klar.
 * Erstmal nicht.
 * <p/>
 * <p/>
 * Date: 14.02.14
 * Time: 16:09
 */
public class OpenGLSceneRenderer implements SceneRenderer {
    boolean inited = false;
    boolean use32 = true;//Jetzt mal Default
    String window_title = "";
    //public ShaderProgram shaderProgram;
    Log logger = Platform.getInstance().getLog(OpenGLSceneRenderer.class);
    private static OpenGLSceneRenderer instance = null;
    private Canvas canvas;
    private Color background = null;
    private HomeBrewScene scene;
    HomeBrewCamera camera;
    HomeBrewSceneRunner runner;
    private OpenGlRenderer renderer = new OpenGlRenderer(OpenGlContext.getGlContext());


    private OpenGLSceneRenderer(HomeBrewSceneRunner runner, HomeBrewScene scene, HomeBrewCamera camera) {
        this.scene = scene;
        this.camera = camera;
        this.runner = runner;
    }

    public static OpenGLSceneRenderer buildInstance(HomeBrewSceneRunner runner, HomeBrewScene scene, HomeBrewCamera camera) {

        if (instance != null)
            throw new RuntimeException("instance already exists");

        instance = new OpenGLSceneRenderer(runner, scene, camera);
        return instance;
    }

    public static OpenGLSceneRenderer getInstance() {
        return instance;
    }


    /**
     * Falls es nicht als Thread laeuft.
     */
    /*siehe Kommentar oben zu Thread.public void startRenderloop() {
        run();
    }*/

    /**
     * Einen einzelnen Frame rendern
     * <p/>
     * <p/>
     * Hier werden
     * 1) Controllerevents gesammelt
     * 2) Updater aufgerufen
     * 3) Szene neu gerendered
     */
    public void renderFrame() {

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
            runner./*runnerhelper.*/mousemove = new Point(/*dx, dy*/x,y);
        }

        runner.scene.deltaTime = runner./*runnerhelper.*/calcTpf();
        runner./*runnerhelper.*/prepareFrame(runner.scene.deltaTime);

        //TODO multiple cameras inlc. enabled
        renderScene(scene, camera);
        Display.update();


    }

    private void renderScene(HomeBrewScene scene, HomeBrewCamera camera) {
        // Clear the screen and depth buffer      
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        OpenGlMatrix4 projectionmatrix, viewmatrix;
        if (camera == null) {
            projectionmatrix = new OpenGlMatrix4();
            viewmatrix = new OpenGlMatrix4();
        } else {
            projectionmatrix = OpenGlMatrix4.toOpenGl(camera.getProjectionMatrix());
            viewmatrix = OpenGlMatrix4.toOpenGl(camera.getViewMatrix());
        }

        //26.4.20 neue Stelle fuer Rendering
        //scene.render(OpenGlContext.getGlContext(), OpenGlMatrix4.fromOpenGl(projectionmatrix), OpenGlMatrix4.fromOpenGl(viewmatrix));
        renderer.render( OpenGlMatrix4.fromOpenGl(projectionmatrix), OpenGlMatrix4.fromOpenGl(viewmatrix),scene.getLights());
    }


}
