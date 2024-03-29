package de.yard.threed.core.resource;

/**
 * MA18: Listener fuer das Laden eines Bundle über die Platform.
 * 7.11.23: TODO: Should have option for reporting success or error.
 * 11.12.23:Maybe a class BundleLoadResult to be usabel in Future? but a kind of delegate is needed? For now calls with null on error.
 * Intercepted in AbstractSceneRunner for bundle registry.
 */
@FunctionalInterface
public interface BundleLoadDelegate {

     void bundleLoad(Bundle bundle);

    
//    public void onError(int errorcode);

}
