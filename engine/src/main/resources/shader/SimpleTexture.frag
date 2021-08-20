
   uniform vec4 m_Color;
//   #ifdef COLORMAP
     uniform sampler2D m_ColorMap;
//   #endif

uniform sampler2D Texture0;
uniform sampler2D Texture1;
uniform sampler2D Texture2;
uniform sampler2D Texture3;


void main(){
    //returning the color of the pixel (here solid blue)
    //- gl_FragColor is the standard GLSL variable that holds the pixel
    //color. It must be filled in the Fragment Shader.

    gl_FragColor = vec4(0.0, 0.0, 1.0, 1.0);

  vec2 TexCoord = vec2( gl_TexCoord[0] );
  //vec4 RGB      = texture2D( Texture0, TexCoord );

  //gl_FragColor  = texture2D(Texture1, TexCoord) * RGB.r +
    //              texture2D(Texture2, TexCoord) * RGB.g +
      //            texture2D(Texture3, TexCoord) * RGB.b;

      gl_FragColor  = texture2D(m_ColorMap, TexCoord);
}
