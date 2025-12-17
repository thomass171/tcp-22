package de.yard.threed.platform.jme;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioKey;
import de.yard.threed.core.platform.NativeAudioClip;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.URL;

/**
 * https://wiki.jmonkeyengine.org/docs/3.4/tutorials/beginner/hello_audio.html
 */
public class JmeAudioClip implements NativeAudioClip {

    AudioData audioData;
    AudioKey audioKey;

    private JmeAudioClip(AudioData audioData, AudioKey audioKey) {
        this.audioData = audioData;
        this.audioKey = audioKey;
    }

    /**
     * Uses FileLocator of the assetmanager with ":" representation to pass bundle name to JmeBundleFileLocator
     * <p>
     * 11.4.17: Returns null on (already logged) error.
     */
    static JmeAudioClip loadFromFile(URL bundleResource, AssetManager assetManager) {
        //logger.debug(String.format("loadFromFile took %d ms", System.currentTimeMillis() - starttime));

        // Even though the name will not be used for loading (URL will be used) we need to
        // set it because it is also a key in the JME cache
        AudioKey audioKey = new AudioKeyForURL(bundleResource.getFullQualifiedName(), false, true, bundleResource);
        AudioData audioData = (AudioData) assetManager.loadAsset(audioKey);
        return new JmeAudioClip(audioData, audioKey);
    }
}
