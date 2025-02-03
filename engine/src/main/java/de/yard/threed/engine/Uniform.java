package de.yard.threed.engine;

/**
 * Only for setup, no value. runtime has nativeuniform.
 *
 * Created by thomass on 30.10.15.
 */
public class Uniform {
    public String name;
    public UniformType type;

    public static String TEXTUREMATRIX = "u_texture_matrix";
    //public static String TEXTUREMATRIX_COL1 = "u_texture_matrix_col1";
    //public static String TEXTUREMATRIX_COL2 = "u_texture_matrix_col2";
    public static String TEXTURE = "u_texture";
    public static String TRANSPARENCY = "u_transparency";

    public Uniform(String name, UniformType type) {
        this.name = name;
        this.type = type;
    }
}
