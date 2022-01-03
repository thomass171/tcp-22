package de.yard.threed.traffic;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.XmlException;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleHelper;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;

public class Tile {
    static Log logger = Platform.getInstance().getLog(Tile.class);

    public String file;
    public Vector3 location = new Vector3(0, 0, 100);
    //public AirportConfig nearestairport;

    public Tile(String file) {
        this.file = file;

    }


    public Tile(String file, Vector3 location) {
        this(file);
        this.location = location;
    }

    /**
     * More advanced. Not only a single GLTF from bundle "osmscenery" but optional a config file
     * and optional several bundle.
     * But for now the XML file must already be loaded.
     * @return
     */
    public static NativeDocument loadConfigFile(BundleResource tile) {
        // XML only sync for now
        BundleData xml = BundleHelper.loadDataFromBundle(tile);

        if (xml == null) {
            logger.error("XML file not found:"+tile.getName());
            return null;
        }
       NativeDocument config = null;
        try {
            config = Platform.getInstance().parseXml(xml.getContentAsString());
        } catch (XmlException e) {
            e.printStackTrace();
        }
        if (config == null) {
            logger.error("parsing xml failed:" + xml);
            return config;
        }
        return config;
    }
}
