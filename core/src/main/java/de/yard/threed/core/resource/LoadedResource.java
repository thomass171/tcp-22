package de.yard.threed.core.resource;


import de.yard.threed.core.platform.NativeTexture;

/**
 * Nur einer der beiden Elemente ist gefuellt, je nach Platform und abhaengig davon, was fuer eine Resource geladen wurde.
 * 28.2.17: Eine Textur als Inputstream zu liefern ist ineffizent und Unsinn. Aber als ImageData ist es evtl. interessant.
 * 
 * TODO: Aufteilen in zwei verschiedene Loader.
 * 
 * Created by thomass on 19.04.16.
 */
public class LoadedResource {
    //public InputStream inputStream;
    public NativeTexture texture;

    /*16.10.18 public LoadedResource(InputStream inputStream) {
        this.inputStream = inputStream;
    }*/

    public LoadedResource( NativeTexture texture) {
        this.texture = texture;
    }
}
