package de.yard.threed.core.resource;

/**
 * MA18: Listener fuer das Laden eines Bundle über die Platform.
 */
@FunctionalInterface
public interface BundleLoadDelegate {
   
     void bundleLoad(Bundle bundle);

    
//    public void onError(int errorcode);

}
