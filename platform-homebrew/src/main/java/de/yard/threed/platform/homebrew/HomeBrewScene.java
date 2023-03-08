package de.yard.threed.platform.homebrew;

import de.yard.threed.core.platform.NativeLight;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.NativeScene;
import de.yard.threed.core.Dimension;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomass on 22.01.16.
 */
public class HomeBrewScene implements NativeScene {

    private List<OpenGlLight> lights = new ArrayList<OpenGlLight>();
    boolean enableModelCameracalled;
    private String uniqueName;
    private static int uniqueid = 1;
    //24.3.18: Wegen intersection SceneNode statt transform
    //24.9.19: keine zusaetzliche root node. "world" ist root. Nee, besser doch einen internen root, sonst droht Rekursion.
    public static HomeBrewSceneNode root;


    public HomeBrewScene() {
        root = new HomeBrewSceneNode("root");//SceneNode("root");
        ((HomeBrewTransform) root.getTransform()).isRoot = true;
    }

    public void add(NativeSceneNode objtoadd) {
        addToTree((HomeBrewTransform) ((HomeBrewSceneNode) objtoadd).getTransform());
    }

    /*public void add(NativeMesh objtoadd) {
        addToTree((OpenGlObject3D) objtoadd);
    }*/

    /*@Override
    public void remove(NativeMesh objtoremove) {
       removeFromTree((OpenGlObject3D)objtoremove);
    }*/

    //@Override
    public void remove(NativeSceneNode objtoremove) {
        removeFromTree((HomeBrewTransform) objtoremove);
    }

    public void add(NativeLight light) {
        lights.add((OpenGlLight) light);
    }

    /**
     *
     */
    /*29.9.18 @Override
    public void enableModelCamera(NativeSceneNode model, NativeCamera camera, Vector3 position, Vector3 lookat) {
        Util.notyet();
    }*/
    private void addToTree(HomeBrewTransform renderable) {
        if (renderable == null)
            throw new RuntimeException("renderable isType null");
        //renderables.add(renderable);
        //root.add(renderable);
        renderable.setParent(getRootTransform());
        /*if (renderable instanceof SceneUpdater) {
            addSceneUpdater((SceneUpdater) renderable);
        }*/
    }

    public HomeBrewTransform getRootTransform() {
        return (HomeBrewTransform) root.getTransform();
    }

    private void removeFromTree(HomeBrewTransform renderable) {
        ((HomeBrewTransform) getRootTransform()).remove(renderable);
    }

    /*26.4.20 jetzt in OpenGlRenderer public void render(GlInterface glImpl, Matrix4 projectionmatrix, Matrix4 viewmatrix) {
        Renderables renderables = new Renderables();
        //TODO optimieren mit needsupdate Flag?
        collectRenderables(renderables);
        /*for (Mesh g : meshes)
            g.draw(glImpl);   * /
        // for (Face f : faces)
        //     f.draw(glImpl, false);
        if (Settings.linear) {
            for (OpenGlMesh renderable : renderables.renderables)
                renderable.render(glImpl, projectionmatrix, viewmatrix, lights);
            for (OpenGlMesh renderable : renderables.transparent)
                renderable.render(glImpl, projectionmatrix, viewmatrix, lights);

        } else {
            Util.notyet();
            //root.render(glImpl, projectionmatrix, viewmatrix, lights);
        }
    }*/

public List<OpenGlLight> getLights(){
    return lights;
}

    @Override
    public Dimension getDimension() {
        return AbstractSceneRunner.getInstance().dimension;
    }

}

