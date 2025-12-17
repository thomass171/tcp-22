package de.yard.threed.core.platform;

public interface NativeAudio {
    void setVolume(double v);

    void play();

    void stop();

    void setLooping(boolean b);

    //not in threejs? boolean isLooping();

    boolean isPlaying();
}
