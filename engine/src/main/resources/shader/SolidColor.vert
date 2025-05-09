// 19.02.25: Shader refactored.

uniform vec3 u_directional_light_direction;

// outgoing normal to fragment shader
OUT vec3 inout_normal;
OUT vec3 inout_light_direction;

void main() {

    gl_Position = PROJECTIONMATRIX * MODELVIEWMATRIX * vec4(VERTEX,1.0);

    // get normal vectors in world space (f.k.a. transpose inverse of modelview matrix)
    inout_normal = normalize(NORMALMATRIX *  NORMAL);
    // do (inefficient) light direction to view space transform here in v-shader to stay more independent from platform light injection to shader
    inout_light_direction = normalize(mat3(VIEWMATRIX) *  u_directional_light_direction);
}
