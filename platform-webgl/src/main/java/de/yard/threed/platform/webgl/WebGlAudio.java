package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeAudio;
import de.yard.threed.core.platform.Platform;

/**
 *
 */
public class WebGlAudio implements NativeAudio {

    static Log logger = Platform.getInstance().getLog(WebGlAudio.class);
    JavaScriptObject audio, listener;
    WebGlAudioClip audioClip;
    boolean hasBuffer = false;

    private WebGlAudio(JavaScriptObject audio, JavaScriptObject listener, WebGlAudioClip audioClip) {
        this.audio = audio;
        this.listener = listener;
        this.audioClip = audioClip;
    }

    public static WebGlAudio createAudio(WebGlAudioClip audioClip) {
        JavaScriptObject listener = buildAudioListener();
        return new WebGlAudio(buildAudio(listener), listener, audioClip);
    }

    @Override
    public void setVolume(double v) {
        setVolume(audio, v);
    }

    @Override
    public void play() {
        if (setBuffer(audio, audioClip.id)) {
            hasBuffer = true;
        }
        if (hasBuffer) {
            play(audio);
        }
    }

    @Override
    public void setLooping(boolean b) {
        setLoop(audio, b);
    }

    @Override
    public void stop() {
        if (audio == null) {
            logger.warn("No audio");
            return;
        }
        if (!hasBuffer) {
            logger.warn("No audio buffer");
            return;
        }
        stop(audio);
    }

    /*@Override*/
    /*not possible?? public boolean isLooping() {
        return audioNode.isLooping();
    }*/

    @Override
    public boolean isPlaying() {
        return isPlaying(audio);
    }

    private static native JavaScriptObject buildAudioListener()  /*-{
        var listener = new $wnd.THREE.AudioListener();
        return listener;
    }-*/;

    private static native JavaScriptObject buildAudio(JavaScriptObject listener)  /*-{
        var audio = new $wnd.THREE.Audio(listener);
        return audio;
    }-*/;

    private static native void setVolume(JavaScriptObject audio, double v)  /*-{
        audio.setVolume(v);
    }-*/;

    private static native void play(JavaScriptObject audio)  /*-{
        audio.play();
    }-*/;

    private static native void stop(JavaScriptObject audio)  /*-{
        audio.stop();
    }-*/;

    private static native void setLoop(JavaScriptObject audio, boolean b)  /*-{
        audio.setLoop(b);
    }-*/;

    private static native boolean setBuffer(JavaScriptObject audio, int clipId)  /*-{
        var buffer = $wnd.loadedaudiobuffer.get(clipId);
        if (buffer != null) {
            audio.setBuffer(buffer);
            // too often console.log("buffer set " + clipId);
            return true;
        } else {
            console.log("audio buffer set failed for clipid " + clipId);
        }
        return false;
    }-*/;

    private static native boolean isPlaying(JavaScriptObject audio)  /*-{
        return audio.isPlaying;
    }-*/;
}
