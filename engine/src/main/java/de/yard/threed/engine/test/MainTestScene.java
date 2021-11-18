package de.yard.threed.engine.test;

import de.yard.threed.engine.Scene;

/**
 * Nur eine Huelle, um die Platform Tests in der Engine aufzurufen.
 * 30.6.21 Ist doch obselet, weil Tests in Referencescene laufen.
 *
 * Created by thomass on 22.12.16.
 */
public class MainTestScene extends Scene {
    @Override
    public void init(boolean forServer) {
        MainTest.runTest(null);
    }

    @Override
    public void update() {
       
    }
}
