package de.yard.threed.core.platform;

/**
 * Only for setup, no value. runtime has nativeuniform.
 *
 * Created by thomass on 30.10.15.
 */
public class Uniform {
    public String name;
    public UniformType type;

    public static String TEXTUREMATRIX = "u_texture_matrix";
    public static String COLOR = "u_color";
    public static String TEXTURE = "u_texture";
    public static String TRANSPARENCY = "u_transparency";
    public static String SHADED = "u_shaded";
    public static String TEXTURED = "u_textured";
    public static String AMBIENT_LIGHT_COLOR = "u_ambient_light_color";
    public static String DIRECTIONAL_LIGHT_COLOR = "u_directional_light_color";
    // vector TO the lights origin
    public static String DIRECTIONAL_LIGHT_DIRECTION = "u_directional_light_direction";
    public static String DEBUG_MODE = "u_debug_mode";

    public Uniform(String name, UniformType type) {
        this.name = name;
        this.type = type;
    }
}
