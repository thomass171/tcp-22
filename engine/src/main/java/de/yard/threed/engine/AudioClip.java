package de.yard.threed.engine;

import de.yard.threed.core.platform.NativeAudioClip;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;


/**
 * Wrapper for an audio clip.
 */
public class AudioClip {

    public NativeAudioClip audioClip;

    private AudioClip(BundleResource bundleResource) {
        audioClip = Platform.getInstance().buildNativeAudioClip(bundleResource);
    }

    public AudioClip(NativeAudioClip audioClip) {
        this.audioClip = audioClip;
    }

    public static AudioClip buildAudioClipFromBundle(String bundlename, String filename) {
        BundleResource br = new BundleResource(BundleRegistry.getBundle(bundlename), filename);
        return new AudioClip(br);
    }

    /**
     * bundle in br must be set.
     */
    public static AudioClip buildAudioClipFromBundle(BundleResource br) {
        return new AudioClip(br);
    }
}
