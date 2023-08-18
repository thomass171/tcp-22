package de.yard.threed.platform.jme;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetManager;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.javacommon.FileReader;
import de.yard.threed.outofbrowser.FileSystemResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 16.8.23: More upToDate generic locator for locating files in bundles and using bundleresolver.
 * Uses ":" representation to pass bundle name to JmeBundleFileLocator
 */
public class JmeBundleFileLocator implements AssetLocator {
    Log logger = Platform.getInstance().getLog(JmeBundleFileLocator.class);

    public JmeBundleFileLocator() {
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

        final InputStream is;
        try {
            BundleResource bundleResource = BundleResource.buildFromFullQualifiedString(assetkey);
            String bundlebasedir = BundleResolver.resolveBundle(bundleResource.bundlename, Platform.getInstance().bundleResolver).getPath();
            FileSystemResource resource = FileSystemResource.buildFromFullString(bundlebasedir + "/" + bundleResource.getFullName());
            is = FileReader.getFileStream(resource);
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
