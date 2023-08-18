package de.yard.threed.platform.jme;

import com.jme3.audio.AudioNode;
import de.yard.threed.core.platform.NativeAudio;

/**
 * https://wiki.jmonkeyengine.org/docs/3.4/tutorials/beginner/hello_audio.html
 */
public class JmeAudio implements NativeAudio {

    AudioNode audioNode;

    private JmeAudio(JmeAudioClip audioClip) {
        this.audioNode = new AudioNode(audioClip.audioData,audioClip.audioKey);
        audioNode.setPositional(true);
        JmeScene.getInstance().getRootNode().attachChild(audioNode);
    }

    public static JmeAudio createAudio(JmeAudioClip audioClip) {
        return new JmeAudio(audioClip);
    }

    @Override
    public void setVolume(double v) {
        audioNode.setVolume((float)v);
    }

    @Override
    public void play() {
        audioNode.play();
    }

    @Override
    public void setLooping(boolean b) {
        audioNode.setLooping(b);
    }
}
