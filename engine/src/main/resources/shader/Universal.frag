/** 
 * Ein Universalshader für die Effekte (siehe auch den Vertexshader)
 *
 *   Transparenz
 *
 * Fuer Transparenz ist hier nichts weiter zu machen. Es reicht, in der Textur einen Alpha Wert zu haben
 * das Objekt am Schluss zu rendern und OpenGL Blending einzuschalten. Dann macht die Grafikengine den Rest.
 */

//2.2 uniform vec4 m_Color;
//   #ifdef COLORMAP
//uniform sampler2D m_ColorMap;
//   #endif

uniform sampler2D u_basetex;
uniform bool u_isunicolor;
uniform vec4 u_unicolor;
uniform vec4 u_lightcolor;

//3.3.16 v_lighting jetzt vec3 statt vec4
#ifdef VERSION150
in vec3 v_lighting;
in vec2 texcoord;
#else
varying vec3 v_lighting;
// als Ersatz fuer gl_TexCoord
varying vec2 texcoord;
#endif


void main() {
    
    vec4 surfacecolor;
    float alpha;
    if (u_isunicolor) {
        //FRAGCOLOR = vec4(0.0, 0.0, 1.0, 1.0);
        //FRAGCOLOR = vec4(v_lighting * u_unicolor.rgb, u_unicolor.a);
        surfacecolor = vec4(u_unicolor.rgb,0);
        alpha = u_unicolor.a;
    } else {
        //FRAGCOLOR = vec4(0.0, 0.0, 1.0, 1.0);
        //FRAGCOLOR  = TEXTURE2D(basetex, texcoord);
        surfacecolor  = TEXTURE2D(u_basetex, texcoord);
        // 19.3.16: Da ist die Frage, ob Alpha aus der Textur genommen wird, oder von aussen, oder fix, oder nur bei transparent? Erstmal fix.
        alpha = 0.5;//surfacecolor.a;
    }

    FRAGCOLOR = vec4(u_v_lighting * surfacecolor.rgb, alpha);

}
