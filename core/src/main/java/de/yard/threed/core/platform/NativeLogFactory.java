package de.yard.threed.core.platform;

/**
 * Konkretes Log geht ueber Platform. Das hier ist nur die Factory dazu.
 * Created by thomass on 20.04.15.
 */
public interface NativeLogFactory {
     Log getLog(Class clazz) ;
}
