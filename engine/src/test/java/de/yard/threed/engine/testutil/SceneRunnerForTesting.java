package de.yard.threed.engine.testutil;

//import de.yard.threed.platform.HomeBrewRenderer;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;

import java.util.HashMap;

//23.2.21: brauch ich nicht mehr. Doch, ach das ist krude.
//Obwohl, fuer Tests ohne vollstaendigen SceneRunner, Hmm vielleicht doch.
//MA36: Den HomeBrewRenderer gibts hier jetzt nicht mehr.
/*@Deprecated*/
public class SceneRunnerForTesting extends AbstractSceneRunner {
    //private HomeBrewRenderer renderer;

    /**
     * 2.8.21: Jetzt mit den PlatformInternals
     */
    private SceneRunnerForTesting(PlatformInternals platformInternals) {
        super(platformInternals);
    }

    /**
     * Ein Init wie in anderen SceneRunnern auch.
     * 7.7.21
     * @param properties
     * @return
     */
    public static SceneRunnerForTesting init(HashMap<String, String> properties, PlatformFactory platformFactory) {
        if (instance != null) {
            throw new RuntimeException("already inited");
        }
        //5.12.18: Mal ohne OpenGL sondern spezieller TestPlatform. Geht aber nicht bei Tests die z.B. nodes brauchen (z.B. world)
        //MA36 jetzt muesste/soll aber Platform gehen.
        /*Engine*/
        PlatformInternals pl = /*(EngineHelper)*/ platformFactory.createPlatform(properties);
        instance = new SceneRunnerForTesting(pl);
        return (SceneRunnerForTesting) instance;
    }

    /**
     *
     *
     * @param frameCount
     */
    public void runLimitedFrames(int frameCount) {
        for (int i = 0; i < frameCount; i++) {
            // tpf 0 ist unguenstig, dann bewegt sich nichts.
            singleUpdate(0.1f);
        }
    }

    /**
     * Das ist ein einzelner "update".
     *
     * @param tpf
     */
    public void singleUpdate(float tpf) {
        //TODO scene.deltaTime = tpf;
        prepareFrame(tpf);
        renderScene();
    }

    private void renderScene() {
        //TODO tja, ist hier was zu tun?
       /*MA36 if (renderer != null) {
            // no camera->no matrix
            renderer.render(null, null, null);
        }
        */
    }

    /*MA36public void setRenderer(HomeBrewRenderer renderer) {
        this.renderer = renderer;
    }*/
}
