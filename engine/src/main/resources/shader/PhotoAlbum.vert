/**
 * A photo album shader for all platforms
 */

OUT vec2 texcoord;

void main() {
    
    gl_Position = PROJECTIONMATRIX * MODELVIEWMATRIX * vec4(VERTEX, 1.0);

    texcoord = MULTITEXCOORD0;
}
