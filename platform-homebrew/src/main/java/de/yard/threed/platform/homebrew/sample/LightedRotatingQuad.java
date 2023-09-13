package de.yard.threed.platform.homebrew.sample;

import de.yard.threed.core.Degree;
import de.yard.threed.core.configuration.ConfigurationByProperties;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.DirectionalLight;
import de.yard.threed.engine.GenericGeometry;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.Light;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.Face;
import de.yard.threed.core.geometry.Face3;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Color;

import de.yard.threed.core.geometry.FaceList;
import de.yard.threed.core.geometry.GeometryHelper;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.platform.homebrew.HomeBrewMaterial;
import de.yard.threed.platform.homebrew.HomeBrewSceneRunner;
import de.yard.threed.platform.homebrew.OpenGlRenderer;
import de.yard.threed.platform.homebrew.PlatformHomeBrew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Very simple scene showing a rotating quad. Rotation can be stopped/started by key 'r'.
 * <p/>
 * There is also a sample LightedRotatingCubeScene.java showing a rotating cube.
 * 20.1.23: Still working
 * <p>
 * Created: 14.02.14
 */
public class LightedRotatingQuad extends Scene {
    /*OpenGl*/ Camera camera;
    Log logger = PlatformHomeBrew.getInstance().getLog(LightedRotatingQuad.class);
    float angle = 0.01f;
    SceneNode quad;
    boolean isrotating = true;
    int loop = 0;
    boolean backfaceculling = false;

    public static void main(String[] argv) {
        HashMap<String, String> properties = new HashMap<>();
        HomeBrewSceneRunner runner = HomeBrewSceneRunner.init(new ConfigurationByProperties(properties),new OpenGlRenderer(), SceneMode.forMonolith());

        LightedRotatingQuad quadExample = new LightedRotatingQuad();

        runner.runScene(quadExample);
        //Other scenes can also be used this way
        //runner.runScene(new ReferenceScene());
        //runner.runScene(new LightedRotatingCubeScene());
        System.out.println("started");
    }

    @Override
    public void init(SceneMode forServer) {

        camera = getDefaultCamera();
        camera.getCarrierTransform().setPosition(new Vector3(0, 1.5f, 3f));
        camera.lookAt(new Vector3(0, 0, 0));

        quad = buildColoredQuad();
        addToWorld(new SceneNode(quad));
        addLight();
    }

    /**
     * provide shader.
     */
    @Override
    public String[] getPreInitBundle() {
        return new String[]{"engine", "data"};
    }

    private void addLight() {

        Light pointLight = new DirectionalLight(Color.WHITE, new Vector3(0, 1, 1.5f));
        // pointLight.setPosition();
        addLightToWorld(pointLight);
    }

    @Override
    public void update() {
        //logger.debug("update");
        if (Input.GetKeyDown(KeyCode.R)) {
            logger.debug("r key was pressed. currentdelta=");

            isrotating = !isrotating;
        }
        if (isrotating) {
            //cube.object3d.translateOnAxis(new Vector3(1, 1, 1), angle);
            quad.getTransform().rotateOnAxis(new Vector3(1, 1, 1), new Degree(angle));
            //cube.rotateY(new Degree(angle));
            //cube.rotateZ(new Degree(angle));
            // System.out.println("loop="+loop+", quaternion="+cube.getRotation().dump(""));
            loop++;
            //angle += 0.00001;
        }
    }

    public SceneNode buildColoredQuad() {
        // Vertices, the order is not important. XYZW instead of XYZ
        List<Vector3> vertices = new ArrayList<Vector3>();
        vertices.add(new Vector3(-0.5f, 0.5f, 0f/*, 1f*/));
        vertices.add(new Vector3(-0.5f, -0.5f, 0f/*, 1f*/));
        vertices.add(new Vector3(0.5f, -0.5f, 0f/*, 1f*/));
        vertices.add(new Vector3(0.5f, 0.5f, 0f/*, 1f*/));

        Vector2 uv = new Vector2(0, 0);
        List<Face> faces = new ArrayList<Face>();

        //Variant with CCC and CW triangles for test of Culling.
        // OpenGL expects to draw vertices in counter clockwise order by default
        if (backfaceculling) {
            // CW triangle left bottom (not visible with backface culling)
            faces.add(new Face3(0, 2, 1, uv, uv, uv));
        } else {
            faces.add(new Face3(0, 1, 2, uv, uv, uv));
        }
        // CCW triangle top right
        faces.add(new Face3(2, 3, 0, uv, uv, uv));

        List<FaceList> facelist = new ArrayList<FaceList>();
        facelist.add(new FaceList(faces));
        HomeBrewMaterial material = (HomeBrewMaterial) Material.buildBasicMaterial(Color.RED).material;
        List<SimpleGeometry> geolist = GeometryHelper.prepareGeometry(vertices, facelist, null, true, false);

        SceneNode cube = new SceneNode(new Mesh(new GenericGeometry(geolist.get(0)).getNativeGeometry(), material, false, false));
        return cube;
    }
}
