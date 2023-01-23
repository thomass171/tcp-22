package de.yard.threed.platform.homebrew;



import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.MathUtil2;



/**
 * Date: 14.02.14
 * Time: 08:41
 * 
 * MA16 nicht deprecated wegen statics
 */
/*21.1.23
public class OpenGlVector3 /*implements Vector3* / {
    float x, y, z;
//    private Vector3 rotation = new Vector3(0,0,0);

    public OpenGlVector3() {
        this.x = 0;
        this.y = 0;
        this.z = 0;

    }

    public OpenGlVector3(Vector3 v) {
        this.x = (float)v.getX();
        this.y = (float)v.getY();
        this.z = (float)v.getZ();

    }
    
    public OpenGlVector3(float x, float y, float z) {

        this.x = x;
        this.y = y;
        this.z = z;

    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getZ() {
        return z;
    }

    public float getY() {
        return y;
    }

    public float getX() {
        return x;
    }


    /*8.4.15: Rotation über Vector definieren wir mal nicht wegen konzeptioneller Unsauberkeit. Wohl aber über Quaternion public void setRotateStatus(Vector3 rotation) {
         //this.rotation = rotation;
    }* /

    /**
     * Die Rotation geht vielleicht auch ohne den Umweg über Matrix.
     *
     * @return
     * /
    public Vector3 rotate(Quaternion rotation) {
        Matrix4 m = MathUtil2.buildRotationMatrix(rotation);
        Util.nomore();
        return  null;//MA16 m.transform(this);
    }

    public OpenGlVector3 multiply(float scale) {
        return new OpenGlVector3(x * scale, y * scale, z * scale);
    }

    public OpenGlVector3 divideScalar(float scalar) {
        float invScalar = 1.0f / scalar;
        return new OpenGlVector3(x * invScalar, y * invScalar, z * invScalar);
    }

    /**
     * XYZW instead of XYZ
     *
     * @return
     * /
    public static float[] getElements(Vector3 v,boolean mitw) {
        float[] out = new float[mitw ? 4 : 3];
        int i = 0;

        // Insert XYZW elements
        out[i++] = (float) v.getX();
        out[i++] = (float) v.getY();
        out[i++] = (float) v.getZ();
        if (mitw)
            out[i++] = 1f;
        return out;
    }



    /**
     * Aus http://stackoverflow.com/questions/15777757/drawing-normals-in-lwjgl-messes-with-lighting
     * Feel free to use this for whatever you want, no licenses applied or anything.
     * /
    /*public static Vector3 getNormal(Vector3 p1, Vector3 p2, Vector3 p3) {

        //Create normal vector we are going to output.
        Vector3 output = new Vector3();

        //Calculate vectors used for creating normal (these are the edges of the triangle).
        Vector3f calU = new Vector3f(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
        Vector3f calV = new Vector3f(p3.x - p1.x, p3.y - p1.y, p3.z - p1.z);

        //The output vector isType equal to the cross products of the two edges of the triangle
        output.x = calU.y * calV.z - calU.z * calV.y;
        output.y = calU.z * calV.x - calU.x * calV.z;
        output.z = calU.x * calV.y - calU.y * calV.x;

        //Return the resulting vector.
        return output.normalize();
    }*/



    /**
     * Liefert die Rotation, die erforderlich ist um p1 in die selbe Orientierung wie p2 zu rotieren.
     */
    /*OGL public static Quaternion getRotation(OpenGlVector3 p1, OpenGlVector3 p2) {
        return Quaternion.buildQuaternion(p1,p2);
    }* /




    /*OGL @Override
    public String dump(String lineseparator) {
        String[] label = new String[]{"x", "y", "z"};
        String s = "";

        s += Util.formatFloats(label, new float[]{x, y, z});
        return s;
    }*/

    /**
     * TODO: Was ist hier der mathematische Unterbau, vor allem wegen des perspective divide?
     * 23.11.14: Und die ist SEHR aehnlich zu Matrix4.transform. Das muss bestimmt zusammengelegt werden.
     *
     * @return
     */
    /*OGL public OpenGlVector3 project(Matrix4 projectionmatrix) {

        float d = 1 / (projectionmatrix.e41 * x + projectionmatrix.e42 * y + projectionmatrix.e43 * z + projectionmatrix.e44); // perspective divide

        // this.x = ( e[0] * x + e[4] * y + e[8]  * z + e[12] ) * d;
        // this.y = ( e[1] * x + e[5] * y + e[9]  * z + e[13] ) * d;
        // this.z = ( e[2] * x + e[6] * y + e[10] * z + e[14] ) * d;

        float nx = (projectionmatrix.e11 * x + projectionmatrix.e12 * y + projectionmatrix.e13 * z + projectionmatrix.e14) * d;
        float ny = (projectionmatrix.e21 * x + projectionmatrix.e22 * y + projectionmatrix.e23 * z + projectionmatrix.e24) * d;
        float nz = (projectionmatrix.e31 * x + projectionmatrix.e32 * y + projectionmatrix.e33 * z + projectionmatrix.e34) * d;

        return new OpenGlVector3(nx, ny, nz);

    }* /

    @Override
    public String toString() {
        return "(" + x + "," + y + "," + z+")";
    }



}*/