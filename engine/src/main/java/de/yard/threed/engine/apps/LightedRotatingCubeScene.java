package de.yard.threed.engine.apps;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.engine.Input;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Color;

/**
 * Date: 14.02.14
 * <p/>
 * From an internet OpenGL example (unknown author). Cube is lighted here. Rotation can be started/sopped by key 'r'.
 * <p/>
 * 8.4.15: Light seems to be rotating too, at least always the same faces are lighted.
 * 10.4.15: The cube rotates different from ThreeJS, maybe due to euler order?
 * 2.3.16: Also runs in platform-homebrew.
 * 23.3.16: The most simple scene.
 */
public class LightedRotatingCubeScene extends Scene {
    Camera camera;
    Log logger = Platform.getInstance().getLog(LightedRotatingCubeScene.class);
    double angle = 1;
    SceneNode cube;
    boolean isrotating = false;
    int loop = 0;

    @Override
    public void init(SceneMode sceneMode) {
        logger.info("init LightedRotatingCube");

        camera = getDefaultCamera();
        camera.getCarrier().getTransform().setPosition(new Vector3(2, 1.5f, 3));
        camera.lookAt(new Vector3(0,0,0));
        cube = buildColoredCube();
        addToWorld(cube);
        addLight();
    }

    /**
     * 21.1.23 provide shader.
     */
    @Override
    public String[] getPreInitBundle() {
        return new String[]{"engine", "data"};
    }

    public static SceneNode buildColoredCube() {

        Geometry cubegeometry =  Geometry.buildCube(1, 1, 1);
        Material mat = Material.buildLambertMaterial(Color.RED);
        SceneNode cube = new SceneNode(new Mesh(cubegeometry, mat));
        cube.getTransform().translateX(0.5f);
        return cube;
    }
    
    /**
     * So positioniert dass die Obeseite stark, die Vorderseite leicht und die rechte gar nicht
     * beschienen ist.
     */
    private void addLight() {
        // create a point light
        //2.2.16: Pointlight needs special handling in shader, darum erstmal directional
        //Light pointLight = new PointLight(Color.WHITE);
        Light light = new DirectionalLight(Color.WHITE,new Vector3(0,2,3));
        //light.setPosition(new Vector3(0, 2, 1.5f));
        addLightToWorld(light);
    }

    @Override
    public void update() {
        double tpf = getDeltaTime();
        if (Input.GetKeyDown(KeyCode.R)) {
            logger.debug("r key was pressed. currentdelta=" + tpf);
            isrotating = !isrotating;
        }
        //System.out.println((getDefaultCamera().getViewMatrix()).dump("\n"));

        if (isrotating) {
            cube.getTransform().rotateX(new Degree(angle));
            cube.getTransform().rotateY(new Degree(angle));
            cube.getTransform().rotateZ(new Degree(angle));
            // System.out.println("loop="+loop+", quaternion="+cube.getRotation().dump(""));
            loop++;
        }
    
     }
}
