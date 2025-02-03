// 16.10.24: Shader refactored to provide the following effects:
//
// Limited to only one texture.
// Be aware of JME mat3 workaround!

IN vec2 texcoord;

uniform sampler2D u_texture;
//uniform bool transparent = false;
uniform float u_transparency;
uniform mat3 u_texture_matrix;

void main() {

    vec2 uv_tmp = (u_texture_matrix * vec3(texcoord, 1.0)).xy;

    vec4 texColor  = TEXTURE2D(u_texture, uv_tmp );

    if (u_transparency > 0.0) {
        FRAGCOLOR  = vec4(texColor.r,texColor.g,texColor.b,1.0-u_transparency);
        //FRAGCOLOR  = vec4(1.0,0.0,0.0,1.0-u_transparency);
    } else {
        FRAGCOLOR  = texColor;
    }
}
