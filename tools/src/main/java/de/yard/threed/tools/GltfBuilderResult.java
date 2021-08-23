package de.yard.threed.tools;

/**
 * Created on 03.01.19.
 */
public class GltfBuilderResult {
    public String gltfstring;
    public byte[] bin;

    GltfBuilderResult(String gltfstring,byte[] bin){
        this.gltfstring=gltfstring;
        this.bin=bin;
        
    }
}
