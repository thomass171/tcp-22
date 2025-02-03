
IN vec2 texcoord;

uniform sampler2D u_texture0;
uniform sampler2D u_texture1;



float vmargin = 0.1;
float hmargin = 0.1;
// Width of image
float width = 0.6;
float height = 0.35;

bool isTopImage( vec2 texcoord)
{
    float x = texcoord.x;
    float y = texcoord.y;
    bool istop = false;

    if (x > hmargin && x <= hmargin + width && y < 1.0 - vmargin && y >= 1.0 - vmargin - height) {
        istop = true;
    }
    return istop;
}

bool isBottomImage( vec2 texcoord)
{
    float x = texcoord.x;
    float y = texcoord.y;
    bool isbottom = false;

    if (x < 1.0 - hmargin && x > 1.0 - hmargin - width && y > vmargin && y < vmargin + height) {
        isbottom = true;
    }
    return isbottom;
}


/**
 * Transform a x coordinate of an image area to an absolute texture x in range 0-1.
 */
float transformX(float x, float offset) {
    return (x - offset) / width;
}

float transformY(float y, float offset) {
    return (y - offset) / height;
}

void main() {
    //returning the color of the pixel (here solid blue)
    //- gl_FragColor is the standard GLSL variable that holds the pixel
    //color. It must be filled in the Fragment Shader.

    FRAGCOLOR = vec4(0.0, 0.0, 1.0, 1.0);

    vec2 TexCoord = texcoord;

    vec2 TexCoordeff;

    if (isTopImage(TexCoord)) {
        TexCoordeff = vec2(transformX(TexCoord.x,hmargin),transformY(TexCoord.y,1.0-vmargin-height ));
        FRAGCOLOR  = TEXTURE2D(u_texture0, TexCoordeff);
    } else {
        if (isBottomImage(TexCoord)) {
            TexCoordeff = vec2(transformX(TexCoord.x,1.0-width-hmargin),transformY(TexCoord.y,vmargin));
            FRAGCOLOR  = TEXTURE2D(u_texture1, TexCoordeff);
        } else {
            FRAGCOLOR = vec4(0.2, 0.2, 0.2, 1.0);
        }
    }
}


