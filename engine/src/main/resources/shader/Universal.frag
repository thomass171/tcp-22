// 16.10.24: Shader refactored to provide the following effects:
// 03.03.25: Merged SimpleTexture and SoldiColor shader to here
// Limited to only one texture.
// Be aware of JME mat3 workaround!

IN vec2 inout_texcoord;
IN vec3 inout_normal;
IN vec3 inout_light_direction;

uniform sampler2D u_texture;
//uniform bool transparent = false;
uniform float u_transparency;
uniform mat3 u_texture_matrix;

uniform vec4 u_color;
uniform bool u_shaded;
uniform bool u_textured;
uniform vec3 u_ambient_light_color;
uniform vec3 u_directional_light_color;
uniform vec3 u_directional_light_direction;
// 0=no debug, 1=debug normal,2=full color orange
uniform int u_debug_mode;

void main() {

    vec3 normal = inout_normal;
    // use light direction from view space
    vec3 directional_light_direction = inout_light_direction;//u_directional_light_direction;

    if (u_debug_mode == 1) {
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
    if (u_debug_mode == 2) {
        // just orange
        FRAGCOLOR  = vec4(1.0,0.5,0.0,1.0);
        return;
    }

    vec4 finalColor;
    if (u_textured) {
        vec2 uv_tmp = (u_texture_matrix * vec3(inout_texcoord, 1.0)).xy;
        finalColor = TEXTURE2D(u_texture, uv_tmp );
    } else {
        finalColor = u_color;
    }

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

        finalColor =  vec4(finalColor.rgb * (diffColor + /*0.5 * specColor +*/ ambientColor), finalColor.a);
    }

    if (u_transparency > 0.0) {
        FRAGCOLOR  = vec4(finalColor.r,finalColor.g,finalColor.b,1.0-u_transparency);
        //FRAGCOLOR  = vec4(1.0,0.0,0.0,1.0-u_transparency);
    } else {
        FRAGCOLOR  = finalColor;
    }


}
