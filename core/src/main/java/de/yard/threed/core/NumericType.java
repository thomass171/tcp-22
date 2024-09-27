package de.yard.threed.core;

/**
 * Created by thomass on 16.02.16.
 */
public enum NumericType {
    SHININESS,
    //0=no transparency,1=full. This is reverse of opaque and alpha channel
    TRANSPARENCY,
    TEXTURE_WRAP_S,
    TEXTURE_WRAP_T,
    // 0:Einfaches Material ohne Lichteinfluss z.B. HUD
    // 1:Smooth, the default
    // 2:flat
    SHADING,

}
