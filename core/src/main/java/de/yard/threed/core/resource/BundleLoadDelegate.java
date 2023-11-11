package de.yard.threed.core.resource;

/**
 * MA18: Listener fuer das Laden eines Bundle über die Platform.
 * 7.11.23: TODO: Should have option for reporting success or error.
 */
@FunctionalInterface
public interface BundleLoadDelegate {
   
     void bundleLoad(Bundle bundle);

    
//    public void onError(int errorcode);

}
