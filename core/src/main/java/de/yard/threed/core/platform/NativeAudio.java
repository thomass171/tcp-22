package de.yard.threed.core.platform;

public interface NativeAudio {
    void setVolume(double v);

    void play();

    void setLooping(boolean b);
}
