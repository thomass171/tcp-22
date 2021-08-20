package de.yard.threed.engine.platform;

/**
 * Date: 09.04.2017
 *
 */
public interface BundleLoadingListener {
    
     void onLoad(byte[] bytebuf);

     void onError(java.lang.Exception e);

}
