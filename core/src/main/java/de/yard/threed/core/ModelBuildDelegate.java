package de.yard.threed.core;



/**
 * 15.09.2017: Listener fuer das Laden eines Models über die Platform.
 */
@FunctionalInterface
public interface ModelBuildDelegate {

    /**
     * 21.12.17: Warum ist der Parameter ein BuildResult? Animationen gibt es hier doch nicht. Naja, wenn die mal aus GLTF gelesen werden, vielleicht doch.
     * Aber das lasse ich erstmal. Animationen gibts nur aus FG. Darum ModelBuildResult statt BuildResult.
     *
     * 10.4.21: Additionally provide original load parameter "loadedResource"? Tricky.
     *
     * @param result
     */
    void modelBuilt(BuildResult result/*, BundleResource loadedResource*/);
}
