


varying	vec3	rawpos;
varying	vec3	VNormal;
varying	vec3	VTangent;
varying	vec3	VBinormal;
varying	vec3	vViewVec;
varying	vec3	reflVec;
varying vec3 	vertVec;
varying	float	alpha;

void main() {
		rawpos = vec3(0,0,0);
		VNormal= vec3(0,0,0);
		VTangent= vec3(0,0,0);
		VBinormal= vec3(0,0,0);
		vViewVec= vec3(0,0,0);
		reflVec= vec3(0,0,0);
  	vertVec= vec3(0,0,0);
  	//geht nicht in WebGL?? alpha = 0;


    //Transformation of the object space coordinate to projection space
    //coordinates.
    //- gl_Position is the standard GLSL variable holding projection space
    //position. It must be filled in the vertex shader
    //- To convert position we multiply the worldViewProjectionMatrix by
    //by the position vector.
    //The multiplication must be done in this order.

    gl_Position = PROJECTIONMATRIX * MODELVIEWMATRIX * vec4(VERTEX,1.0);
}
