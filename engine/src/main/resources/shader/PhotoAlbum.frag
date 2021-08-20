#ifdef VERSION150
in vec3 v_lighting;
in vec2 texcoord;
#else
varying vec3 v_lighting;
// als Ersatz fuer gl_TexCoord
varying vec2 texcoord;
#endif


uniform sampler2D texture0;
uniform sampler2D texture1;



float vmargin = 0.1;
float hmargin = 0.1;
// Breite eines Image
float width = 0.6;
float height = 0.35;

bool isTopImage( vec2 texcoord)
{
    float x = texcoord.x;
    float y = texcoord.y;
    bool istop = false;

    if (x > hmargin && x <= hmargin + width && y < 1 - vmargin && y >= 1 - vmargin - height) {
        istop = true;
    }
    return istop;
}

bool isBottomImage( vec2 texcoord)
{
    float x = texcoord.x;
    float y = texcoord.y;
    bool isbottom = false;

    if (x < 1 - hmargin && x > 1 - hmargin - width && y > vmargin && y < vmargin + height) {
        isbottom = true;
    }
    return isbottom;
}


/**
 * Transformiert eine x Koordinate in einem Imagebereich in eine absolutes Texture x im Bereich 0-1.
 */
float transformX(float x, float offset) {
    return (x - offset) / width;
}

float transformY(float y, float offset) {
    return (y - offset) / height;
}

void main(){
    //returning the color of the pixel (here solid blue)
    //- gl_FragColor is the standard GLSL variable that holds the pixel
    //color. It must be filled in the Fragment Shader.

    FRAGCOLOR = vec4(0.0, 0.0, 1.0, 1.0);

    vec2 TexCoord = texcoord;//vec2( gl_TexCoord[0] );
   //vec4 RGB      = texture2D( Texture0, TexCoord );

  //gl_FragColor  = texture2D(Texture1, TexCoord) * RGB.r +
    //              texture2D(Texture2, TexCoord) * RGB.g +
      //            texture2D(Texture3, TexCoord) * RGB.b;

    vec2 TexCoordeff;
    //if (TexCoord.y < 0.5) {
    if (isTopImage(TexCoord)) {
        TexCoordeff = vec2(transformX(TexCoord.x,hmargin),transformY(TexCoord.y,1-vmargin-height ));
        FRAGCOLOR  = TEXTURE2D(texture0, TexCoordeff);
    } else {
        if (isBottomImage(TexCoord)) {
            TexCoordeff = vec2(transformX(TexCoord.x,1-width-hmargin),transformY(TexCoord.y,vmargin));
            FRAGCOLOR  = TEXTURE2D(texture1, TexCoordeff);
        } else {
            FRAGCOLOR = vec4(0.2, 0.2, 0.2, 1.0);
        }
    }
}


