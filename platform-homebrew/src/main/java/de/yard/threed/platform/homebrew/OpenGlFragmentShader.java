package de.yard.threed.platform.homebrew;


/**
 * Date: 24.03.14
 */
public class OpenGlFragmentShader extends OpenGlShader {
    public OpenGlFragmentShader(String source, String name) {
        super(GlInterface.ShaderType.FRAGMENT/*GL20.GL_FRAGMENT_SHADER*/, source, name);
    }

    public static OpenGlFragmentShader buildDefault() {
        return new OpenGlFragmentShader("#version 150 core\n" +
                "\n" +
                "in vec4 pass_Color;\n" +
                "\n" +
                "out vec4 out_Color;\n" +
                "\n" +
                "void main(void) {\n" +
                "\tout_Color = vec4(0.0, 0.0, 1.0, 1.0);//pass_Color;\n" +
                "}","builtin") ;
    }

    /*OGL public static OpenGlFragmentShader buildDefaultTexture() {
        String fragmentshader = new String();
        RessourceManager rm = GlContext.getRessourceManager();
        fragmentshader = new String(rm.getRessource("shader/textured.fs"));
        return new OpenGlFragmentShader(fragmentshader,"shader/textured.fs");
        /*
        return new FragmentShader("#version 150 core\n" +
                "uniform sampler2D texture_diffuse;\n" +
                "in vec4 pass_Color;\n" +
                "in vec2 pass_TextureCoord;\n"+
                "\n" +
                "out vec4 out_Color;\n" +
                "\n" +
                "void main(void) {\n" +
                "\tout_Color = pass_Color;\n" +
                // Override out_Color with our texture pixel
                "out_Color = texture(texture_diffuse, pass_TextureCoord);\n"+
                "}") ; * /
    }

    public static OpenGlFragmentShader buildLightedTextured() {
        String fragmentshader = new String();
        RessourceManager rm = GlContext.getRessourceManager();
        fragmentshader = new String(rm.getRessource("shader/lightedtextured.fs"));
        return new OpenGlFragmentShader(fragmentshader,"shader/lightedtextured.fs");
    }*/

}
