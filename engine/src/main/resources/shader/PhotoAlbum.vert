/**
 * Ein Photoalbumshader fuer alle Platformen
 */
 
// als Ersatz fuer gl_TexCoord
#ifdef VERSION150
out vec2 texcoord;
out vec3 v_lighting;
#else
varying vec2 texcoord;
varying vec3 v_lighting;
#endif

void main(){
    
    gl_Position = PROJECTIONMATRIX * MODELVIEWMATRIX * vec4(VERTEX, 1.0);

    texcoord = MULTITEXCOORD0;
}
