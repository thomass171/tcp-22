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
 * Time: 07:05
 * <p/>
 * Hergeleitet aus einem Internet OpenGL Beispiel (mit unbekannten Autor)
 * Der Cube ist hier beleuchtet.
 * Rotation kann ueber die Taste r gestoppt/gestartet werden.
 * <p/>
 * 8.4.15: Das Light scheint sich mitzudrehen, zumindest sind immer die gleichen Flaechen
 * beleuchtet.
 * Pendant zur ThreeJS Referenzanwendung
 * 10.4.15: Der Würfel dreht anders als in ThreeJS, evtl. aufgrund unterschiedlicher Eulerreihenfolge?
 * 2.3.16: Geht jetzt auch mit OpenGL.
 * 23.3.16: Das ist die einfachste aller Scenes.
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

    public static SceneNode buildColoredCube() {
        // ohne eigene shapes weil es auch ein einfacher Test sein soll.
        Geometry cubegeometry =  Geometry.buildCube(1, 1, 1);
        Material mat = Material.buildLambertMaterial(Color.RED);
        SceneNode cube = new SceneNode(new Mesh(cubegeometry, mat));
        cube.getTransform().translateX(0.5f);
        return cube;
    }

    /*23.3.16: Aufruf jetzt auch ueber SceneRunner public static void main(String[] argv){
        //PlatformOpenGL.getInstance();
        NativeSceneRunner renderer = Platform.getInstance().getSceneRunner();
        LightedRotatingCube quadExample = new LightedRotatingCube();
        renderer.runScene(quadExample);
        System.out.println("started");
    }*/
    
    /**
     * So positioniert dass die Obeseite stark, die Vorderseite leicht und die rechte gar nicht
     * beschienen ist.
     */
    private void addLight() {
        // create a point light
        //2.2.16: Pointlight benötigt eine spezielle Behandlung im Shader, darum erstmal directional
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
