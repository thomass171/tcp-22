// 19.02.25: Shader refactored.

IN vec3 inout_normal;
IN vec3 inout_light_direction;

uniform float u_transparency;

uniform vec4 u_color;
uniform bool u_shaded;
uniform vec3 u_ambient_light_color;
uniform vec3 u_directional_light_color;
uniform vec3 u_directional_light_direction;

void main() {

    vec3 normal = inout_normal;
    // use light direction from view space
    vec3 directional_light_direction = inout_light_direction;//u_directional_light_direction;

    vec4 finalColor = u_color;

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
