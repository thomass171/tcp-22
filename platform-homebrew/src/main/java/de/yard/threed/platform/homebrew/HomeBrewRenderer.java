package de.yard.threed.platform.homebrew;

import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Util;
import de.yard.threed.engine.platform.common.Settings;

import java.util.List;

/**
 * Abstraction of "redrawing" of objects. Could by OpenGL or remote.
 * Also used for testing. Nichts fuer C# oder GWT.
 * 22.2.21 jetzt hab ich auch noch einen SceneRenderer(??). Das sind aber verschiedene Ebenen(??)
 * 26.4.20
 */
public abstract class HomeBrewRenderer {

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
            // special feature in MP.
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



}
