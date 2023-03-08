package de.yard.threed.platform.homebrew;

import de.yard.threed.core.Dimension;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.NativeCamera;
import de.yard.threed.core.platform.NativeScene;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Settings;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Abstraction of "redrawing" of objects. Could by OpenGL or remote on a server without display.
 * Also used for testing. Not for C# oder GWT.
 * 22.2.21 There is also a SceneRenderer, but that works on a higher level.
 * 26.4.20
 */
public abstract class HomeBrewRenderer {

    //25.4.20: Set by implementor
    protected GlInterface glcontext;

    /**
     * Einen einzelnen Frame rendern
     * <p/>
     * <p/>
     * Hier werden
     * 1) Controllerevents gesammelt
     * 2) Updater aufgerufen
     * 3) Szene neu gerendered
     * @param camera
     */
    public void renderFrame(AbstractSceneRunner runner, /*HomeBrewNativeScene scene*/List<OpenGlLight> lights, NativeCamera camera) {

        collectKeyboardAndMouseEvents(runner);

        Scene sc = Scene.getCurrent();
        sc.deltaTime = runner./*runnerhelper.*/calcTpf();
        runner./*runnerhelper.*/prepareFrame(sc.deltaTime);

        //TODO multiple cameras inlc. enabled
        renderScene(lights, camera);
        updateDisplay();

    }

    protected abstract void renderScene(List<OpenGlLight> lights,/*HomeBrewScene scene, /*HomeBrew*/NativeCamera camera);


    /*public HomeBrewRenderer getRenderer() {
        return renderer;
    }*/

    /**
     * Rendern aller Objekte innerhalb von Homebrew.
     *
     * @param projectionmatrix
     * @param viewmatrix
     * @param lights
     */
    public void render(Matrix4 projectionmatrix, Matrix4 viewmatrix, List<OpenGlLight> lights) {
        Renderables renderables = new Renderables();
        //TODO optimieren mit needsupdate Flag?
        collectRenderables(renderables);
        /*for (Mesh g : meshes)
            g.draw(glImpl);   */
        // for (Face f : faces)
        //     f.draw(glImpl, false);
        if (Settings.linear) {
            for (HomeBrewMesh renderable : renderables.renderables) {
                //renderable.render(glImpl, projectionmatrix, viewmatrix, lights);
                doRender(renderable, projectionmatrix, viewmatrix, lights);
            }
            for (HomeBrewMesh renderable : renderables.transparent) {
                //renderable.render(glImpl, projectionmatrix, viewmatrix, lights);
                doRender(renderable, projectionmatrix, viewmatrix, lights);
            }
            // special feature in MP. TODO 23.1.23 MP has its own renderer. So: still needed?
            for (HomeBrewSceneNode renderable : renderables.nodes) {
                //renderable.render(glImpl, projectionmatrix, viewmatrix, lights);
                doRender(renderable, projectionmatrix, viewmatrix, lights);
            }

        } else {
            Util.notyet();
            //root.render(glImpl, projectionmatrix, viewmatrix, lights);
        }
    }

    protected void collectRenderables(Renderables renderables) {
        HomeBrewSceneNode.collectRenderables(renderables);
    }

    protected abstract void collectKeyboardAndMouseEvents(AbstractSceneRunner runner);
    protected abstract void updateDisplay();

    /**
     * Das ist aber noch nicht zu Ende gedacht. in OpenGlMesh ist immer noch sehr viel.
     *
     * @param mesh
     * @param projectionmatrix
     * @param viewmatrix
     * @param lights
     */
    protected abstract void doRender(HomeBrewMesh mesh, Matrix4 projectionmatrix, Matrix4 viewmatrix, List<OpenGlLight> lights);

    /**
     * MP needs "rendering" og nodes.
     * 06.03.21
     *
     * @param mesh
     * @param projectionmatrix
     * @param viewmatrix
     * @param lights
     */
    protected abstract void doRender(HomeBrewSceneNode mesh, Matrix4 projectionmatrix, Matrix4 viewmatrix, List<OpenGlLight> lights);

    public abstract void init(Dimension dimension);

    public  GlInterface getGlContext(){
        return glcontext;
    }

    public abstract boolean userRequestsTerminate();

    public abstract void close();
}
