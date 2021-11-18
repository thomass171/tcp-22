package de.yard.threed.engine.apps;

/**
 * Simple Terraindarstellung ohne sowas wie Geokoordinaten.
 * FPC Camera.
 * PGUP/DOWN für Höhenänderung.
 */

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.DirectionalLight;
import de.yard.threed.engine.FirstPersonController;
import de.yard.threed.engine.gui.Hud;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.engine.Light;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.PointLight;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Color;
import de.yard.threed.core.Dimension;
import de.yard.threed.engine.platform.common.Settings;

public class TerrainScene extends Scene  {
    public Log logger = Platform.getInstance().getLog(TerrainScene.class);
    Light light;
    public double scale = 1;
    Hud hud;
    FirstPersonController fps;
    private double lastheight=0;


    @Override
    public void init(boolean forServer) {

        addLight();

        // ungefaehr auf EDDK
        getDefaultCamera().getCarrier().getTransform().setPosition(new Vector3(500, 100, -1000));
        getDefaultCamera().lookAt(new Vector3(0, 0, 0));


        boolean mithud = true;
        if (mithud) {
            hud = Hud.buildForCamera(getDefaultCamera(),0);
        }
        fps = new FirstPersonController(getMainCamera().getCarrierTransform());
        fps.setMovementSpeed(100);
        addToWorld(buildTile(1000));
    }

    @Override
    public void initSettings(Settings settings) {
        settings.targetframerate = 30;
        settings.aasamples = 4;
        settings.pngcacheenabled=false;
    }


    private void addLight() {
        boolean usepointlight = false;
        if (usepointlight) {
            // create a point light
            PointLight pointLight1 = new PointLight(Color.WHITE);
            //pointLight = new AmbientLight(Color.BLUE);
            //pointLight1.setPosition(new Vector3(0, 2, 1.5f));
            addLightToWorld(pointLight1);
        } else {
            Light light = new DirectionalLight(Color.WHITE, new Vector3(0, 3, 2));
            addLightToWorld(light);
        }

        // und noch ein Licht, sonst ist einiges - vor allem bei JME - zu dunkel. Liegt evtl. an fehlenden Normlen
        PointLight pointLight = new PointLight(Color.WHITE);
       // pointLight.setPosition(new Vector3(2, 5, 1.5f));
        //23.3.17 addLightToWorld(pointLight);


    }

    private SceneNode buildTile(double size) {
        int widthSegments = 1;
        int heightSegments = 1;

        ShapeGeometry planegeo = ShapeGeometry.buildPlane(1410, 3113, widthSegments, heightSegments);
        Material mat = null;//Material.buildLambertMaterial(null/*26.4.17 new Texture(new FileSystemResource("/Users/thomass/Projekte/OSM2WORLD/tmp/t.png")*/);
        SceneNode model = new SceneNode(new Mesh(planegeo, mat));
        return model;

    }

    @Override
    public Dimension getPreferredDimension() {
        // 26.4.16: fuer Book/Office Tests groesser
        return new Dimension(1200, 900);
        //return new Dimension(800, 600);
    }

    @Override
    public void update() {
        double tpf = getDeltaTime();
        if (fps != null) {
            fps.update(tpf);
        }
        if (Input.GetKeyDown(KeyCode.PageUp)) {
            incCamera(100);
        }
        if (Input.GetKeyDown(KeyCode.PageDown)) {
            incCamera(-100);
        }
        showHeight();
     
    }

    private void showHeight(){
        double h = getMainCamera().getCarrierPosition().getY();
        if (h != lastheight) {
            hud.setText(0, "Hoehe: " + h);
            lastheight=h;
        }
    }
    
    private void incCamera(double inc) {
        Vector3 position = getMainCamera().getCarrierPosition();
        position = new Vector3(position.getX(), position.getY() + inc, position.getZ());
        getMainCamera().getCarrier().getTransform().setPosition(position);

    }

}
