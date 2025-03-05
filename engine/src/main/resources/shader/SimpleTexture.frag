// 16.10.24: Shader refactored to provide the following effects:
//
// Limited to only one texture.
// Be aware of JME mat3 workaround!

IN vec2 texcoord;
IN vec3 inout_normal;
IN vec3 inout_light_direction;

uniform sampler2D u_texture;
//uniform bool transparent = false;
uniform float u_transparency;
uniform mat3 u_texture_matrix;

uniform bool u_shaded;
uniform vec3 u_ambient_light_color;
uniform vec3 u_directional_light_color;
uniform vec3 u_directional_light_direction;

void main() {

    vec3 normal = inout_normal;
    // use light direction from view space
    vec3 directional_light_direction = inout_light_direction;//u_directional_light_direction;

    if (false) {
        // illustrate normal by color. Might be useful for debugging
        FRAGCOLOR  = vec4((normal),1.0);
        return;
    }

    if (false && length(normal) < 0.99) {
        FRAGCOLOR  = vec4(1.0,0.0,0.0,1.0);
        return;
    }
    if (false && length(u_directional_light_direction) < 0.99) {
        FRAGCOLOR  = vec4(1.0,1.0,0.0,1.0);
        return;
    }

    vec2 uv_tmp = (u_texture_matrix * vec3(texcoord, 1.0)).xy;
    vec4 texColor = TEXTURE2D(u_texture, uv_tmp );

    if (u_shaded) {
        float diff = max(dot(normal,directional_light_direction),0.0);
        if (false && diff < 0.00001) {
            //  illustrate areas without any diffuse by color. Useful for debugging
            FRAGCOLOR  = vec4(0.0,1.0,0.0,1.0);
            return;
        }
        if (false) {
            // illustrate diff by color for debugging
            FRAGCOLOR  = vec4(diff,0.0,0.0,1.0);
            return;
        }
        vec3 diffColor = diff * u_directional_light_color;
        //diffColor = vec3(1.0,1.0,1.0);

        //vec3 specColor = pow(spec,3) * vec3(1,1,1);
        vec3 ambientColor = u_ambient_light_color;

        texColor =  vec4(texColor.rgb * (diffColor + /*0.5 * specColor +*/ ambientColor), texColor.a);
    }

    if (u_transparency > 0.0) {
        FRAGCOLOR  = vec4(texColor.r,texColor.g,texColor.b,1.0-u_transparency);
        //FRAGCOLOR  = vec4(1.0,0.0,0.0,1.0-u_transparency);
    } else {
        FRAGCOLOR  = texColor;
    }
}
