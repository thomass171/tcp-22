package de.yard.threed.engine.test;

import de.yard.threed.engine.Scene;
import de.yard.threed.core.SceneUpdater;

/**
 * Nur eine Huelle, um die Platform Tests in der Engine aufzurufen.
 * 30.6.21 Ist doch obselet, weil Tests in Referencescene laufen.
 *
 * Created by thomass on 22.12.16.
 */
public class MainTestScene extends Scene implements SceneUpdater {
    @Override
    public void init() {
        MainTest.runTest(null);
    }

    @Override
    public void update() {
       
    }
}
