package de.yard.threed.platform.jme;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetManager;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.javacommon.FileReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Alternativer Locator, um auch im Classpath zu suchen.
 * 2.5.19: Ist aber schon laenger über Bundle statt Classpath? Ich glaube das taeuscht. Wir sind
 * hier doch IN der Platform JME. Nee, nicht unbedingt. Custom shader werden aus Bundles geladen,
 * allerdings nicht hier.
 */
public class JmeFileLocator implements AssetLocator {
    Log logger = Platform.getInstance().getLog(JmeFileLocator.class);
    private String rootpath;

    public JmeFileLocator() {

    }

    @Override
    public void setRootPath(String s) {
        rootpath = s;
    }

    /**
     * @param assetManager
     * @param assetKey
     * @return
     */
    @Override
    public AssetInfo locate(AssetManager assetManager, AssetKey assetKey) {
        String assetkey = assetKey.getName();
        logger.debug("locate: " + assetkey + " with root path " + rootpath);

        final InputStream is;
        try {
            is = FileReader.getFileStream(new BundleResource(new ResourcePath(rootpath),assetKey.getName()));
        } catch (Exception e) {
            //Ein custom shader wird im FS tatsaechlich nicht gefunden. Nur ein Warning? Es kommen ja noch andere Locator.
            //e.printStackTrace();
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
