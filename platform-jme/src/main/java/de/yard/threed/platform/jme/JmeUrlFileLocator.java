package de.yard.threed.platform.jme;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetManager;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.URL;
import de.yard.threed.javacommon.FileReader;
import de.yard.threed.javacommon.JavaBundleHelper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 12.12.25: More upToDate generic locator for locating files via URL using bundleresolver.
 * Very spcific for audio for now
 */
public class JmeUrlFileLocator implements AssetLocator {
    Log logger = Platform.getInstance().getLog(JmeUrlFileLocator.class);

    public JmeUrlFileLocator() {
    }

    /**
     * Not used.
     */
    @Override
    public void setRootPath(String s) {
    }

    /**
     * Returns null when the assetkey couldn't been found. Will forward to next locator.
     */
    @Override
    public AssetInfo locate(AssetManager assetManager, AssetKey assetKey) {
        String assetkey = assetKey.getName();
        logger.debug("locate: " + assetkey);

        if (!(assetKey instanceof AudioKeyForURL)){
            return null;
        }
        final InputStream is;
        try {
            URL url = ((AudioKeyForURL)assetKey).url;
            byte[] buffer = JavaBundleHelper.loadResource(url);
             is = new ByteArrayInputStream(buffer);
        } catch (Exception e) {
            logger.debug("not found");
            return null;
        }

        AssetInfo ai = new AssetInfo(assetManager, assetKey) {
            @Override
            public InputStream openStream() {
                return new ByteArrayInputStream(FileReader.readFully(is));
            }
        };
        return ai;
    }
}
