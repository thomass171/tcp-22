package de.yard.threed.javacommon;

import java.nio.ByteBuffer;

/**
 * Created by thomass on 30.08.16.
 */
public class LoadedImage {
    public int width,height;
    public ByteBuffer buffer;
    
    public LoadedImage(int width, int height, ByteBuffer buffer) {
       this.width = width;
        this.height = height;
        this.buffer = buffer;
    }
}
