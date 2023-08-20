package de.yard.threed.engine.platform;

import de.yard.threed.core.resource.BundleData;

/**
 * Date: 13.06.14
 * <p/>
 */
public interface ResourceLoadingListener {

    void onLoad(BundleData data);

    void onError(int errorcode);

}
