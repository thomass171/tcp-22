package de.yard.threed.engine;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeAudio;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.gui.ControlPanelHelper;

/**
 * Wrapper for a global(background) audio. Becomes local(positional) by attaching it to a scene node.
 * <p>
 * Is like light and mesh a component of a scene node, though its standalone in some platforms (JME, ThreeJS)
 */
public class Audio {
    Log logger = Platform.getInstance().getLog(Audio.class);

    private NativeAudio audio;

    private Audio(AudioClip audioClip) {
        audio = Platform.getInstance().buildNativeAudio(audioClip.audioClip);
    }

    public Audio(NativeAudio audio) {
        this.audio = audio;
    }

    public static Audio buildAudio(AudioClip audioClip) {
        return new Audio(audioClip);
    }

    public void setVolume(double v) {
        if (audio == null) {
            logger.warn("audio is null");
        } else {
            audio.setVolume(v);
        }
    }

    public void play() {
        if (audio == null) {
            logger.warn("audio is null");
        } else {
            audio.play();
        }
    }

    public void setLooping(boolean b) {
        if (audio == null) {
            logger.warn("audio is null");
        } else {
            audio.setLooping(b);
        }
    }
}
