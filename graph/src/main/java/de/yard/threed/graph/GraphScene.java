package de.yard.threed.graph;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.DirectionalLight;
import de.yard.threed.engine.Scene;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Color;
import de.yard.threed.core.Dimension;
import de.yard.threed.engine.platform.common.Settings;

/**
 * Abstrakte Superklasse fuer Scene, die Graphen darsetellen.
 * 7.2.18: Ob die Klasse noch berechtigt ist, ist fraglich.
 * Created by thomass on 14.09.16.
 * 
 * 9.1.18 Zumindest gibt es jetzt TrafficScene.
 */
public abstract class GraphScene extends Scene {
    private Log logger = Platform.getInstance().getLog(GraphScene.class);
   // public GraphVisualizer visualizer;

  

    @Override
    public void initSettings(Settings settings) {
        settings.targetframerate = 20;//60;
        settings.aasamples = 4;
        
    }

    @Override
    public Dimension getPreferredDimension() {
        return new Dimension(800, 600);
    }

    protected void addLight() {
        DirectionalLight light = new DirectionalLight(Color.WHITE, new Vector3(0, 30000000, 20000000));
        addLightToWorld(light);
        light = new DirectionalLight(Color.WHITE, new Vector3(0, -30000000, -20000000));
        addLightToWorld(light);
    }

    @Override
    public void update() {
      
    }
}