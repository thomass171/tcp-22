package de.yard.threed.platform.jme;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioKey;
import de.yard.threed.core.platform.NativeAudioClip;
import de.yard.threed.core.resource.BundleResource;

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
    static JmeAudioClip loadFromFile(BundleResource bundleResource, AssetManager assetManager) {
        //logger.debug(String.format("loadFromFile took %d ms", System.currentTimeMillis() - starttime));
        AudioKey audioKey = new AudioKey(bundleResource.getFullQualifiedName(), false, true);
        AudioData audioData = (AudioData) assetManager.loadAsset(audioKey);
        return new JmeAudioClip(audioData, audioKey);
    }
}
