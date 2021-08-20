package de.yard.threed.engine.platform;

import de.yard.threed.core.resource.BundleData;

/**
 * Date: 13.06.14
 * <p/>
 * Den verwenden wir erstmal nicht, weil zu Ajax spezifisch
 * 17.07.2015: Jetzt doch.
 * 21.04.2017: Jetzt String statt byte[]
 */

public interface ResourceLoadingListener {
    //Platform kapselt mit Base64 bei Binärdaten, daher geht auch byte[] und nicht nur String

    public void onLoad(BundleData data/*byte[] bytebuf*/);


    public void onError(int errorcode);

}
