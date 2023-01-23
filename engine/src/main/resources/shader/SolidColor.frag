// Fixed color shader for testing/debugging

void main(){
    //returning the color of the pixel (here solid blue)
    //- gl_FragColor is the standard GLSL variable that holds the pixel
    //color. It must be filled in the Fragment Shader.
    //23.1.23 In version 15 the name can be different?

    my_fragcolor = vec4(0.0, 0.0, 1.0, 1.0);


}
