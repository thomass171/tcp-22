/**
 * Ein Universalshader für verschiedene Effekte in allen Platformen und gleichzeitig der Default Shader für OpenGL.
 *
 * Effekte:
 *
 *   Beleuchtung mit directional Light (Pointlight erstmal nicht)
 *   Transparenz
 *
 */

uniform bool isunicolor;
uniform bool isunshaded;
uniform vec4 unicolor;

uniform vec3 u_lightdirection;
uniform vec4 u_lightcolor;
uniform vec4 u_ambientlightcolor;

// als Ersatz fuer gl_TexCoord
#ifdef VERSION150
//3.3.16 v_lighting jetzt vec3 statt vec4
out vec2 texcoord;
out vec3 v_lighting;
#else
varying vec2 texcoord;
varying vec3 v_lighting;
#endif

void main() {

    // Nach letzter Recherche ist die Reihenfolge richtig. Umgekehrt MVP.
    // 10.3.16: In zwei statt einem Schritt werden Rundungsfehler vermieden, zumindest in bestimmten Situationen
    // Ist aber nicht entscheidend.
    //gl_Position = PROJECTIONMATRIX * MODELVIEWMATRIX * vec4(VERTEX, 1.0);
    vec4 posi;
    posi = MODELVIEWMATRIX * vec4(VERTEX, 1.0);
    gl_Position = PROJECTIONMATRIX * posi;

    //  gl_TexCoord[0] = vec4(MULTITEXCOORD0,0,0);
    texcoord = MULTITEXCOORD0;


    vec3 normal = normalize(NORMALMATRIX*NORMAL);
    //mat4 normalMatrix = transpose(inverse(MODELVIEWMATRIX));
    //normal = normalize(vec3(normalMatrix*vec4(NORMAL,1.0)));
       
    // The dot product of the light direction and the normal (the orientation of a surface)
    float nDotL = max(dot(u_lightdirection, normal), 0.0);
        
    vec3 diffuse = u_lightcolor.rgb * nDotL;
    vec3 ambient = u_ambientlightcolor.rgb ;

    v_lighting = diffuse + ambient;
    //v_lighting =vec3(nDotL, 0.0, 0.0);
    //v_lighting = NORMAL;
    //v_lighting = u_lightdirection;
    //v_lighting = vec4(diffuse,1);
    
    if (isunshaded) {
        v_lighting = vec3(1,1,1);
    }
        
}
