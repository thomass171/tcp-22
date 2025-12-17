package de.yard.threed.platform.jme;

import com.jme3.asset.AssetKey;
import com.jme3.audio.AudioKey;
import de.yard.threed.core.resource.URL;

public class AudioKeyForURL extends AudioKey {

    public URL url;

    public AudioKeyForURL(String name, boolean stream, boolean streamCache, URL url) {
        super(name, stream, streamCache);
        this.url = url;
    }
}
