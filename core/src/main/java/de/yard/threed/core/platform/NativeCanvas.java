package de.yard.threed.core.platform;

/**
 * Bei HMTL Canvas ist y=0 oben. Bei FG Canvas und Java Graphics2D auch.
 * OpenVG und OpenGL haben y=0 unten.
 *
 * Also: Vor allem wegen FG: y=0 ist oben.
 * Unity kann das wegen fehlendem System.Drawing nicht.
 * Aus Effizienzgruenden sollte das wohl per render-to-texture und/oder FBO gehen.
 * 8.8.25: Deprecated because LWJGL3 interferes with AWT (see README.md).
 * Created by thomass on 02.10.15.
 */
@Deprecated
public interface NativeCanvas {
    /*public void drawImage(ImageData img, int x, int y);

    public ImageData getImageData();*/

}
