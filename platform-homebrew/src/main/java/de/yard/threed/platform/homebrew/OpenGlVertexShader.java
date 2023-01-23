package de.yard.threed.platform.homebrew;


/**
 * Date: 24.03.14
 */
public class OpenGlVertexShader extends OpenGlShader {
    public OpenGlVertexShader(String source, String name) {
        super(GlInterface.ShaderType.VERTEX, source, name);
    }

    /**
     * Ist schon zu neue GLSL. Nur in speziellen Faellen verwendbar.
     *
     * @return
     */
    public static OpenGlVertexShader buildDefault() {
        return new OpenGlVertexShader("#version 150 core\n" +
                "uniform mat4 projection;\n" +
                "uniform mat4 viewer;\n" +
                "uniform mat4 model;\n" +
                "\n" +
                "in vec4 in_Position;\n" +
                "in vec4 in_Color;\n" +
                "in vec2 in_TextureCoord;\n" +
                "\n" +
                "out vec4 pass_Color;\n" +
                "out vec2 pass_TextureCoord;\n" +
                "\n" +
                "void main(void) {\n" +
                "\tgl_Position = model * projection * viewer * in_Position;\n" +
                "\tpass_Color = in_Color;\n" +
                "pass_TextureCoord = in_TextureCoord;\n" +
                "}", "default");
        /*OGLRessourceManager rm = GlContext.getRessourceManager();
        String vertexshader = new String(rm.getRessource("shader/default.vs"));
        return new OpenGlVertexShader(vertexshader,"shader/default.vs");*/
    }
}
