package de.yard.threed.platform.jme;

import com.jme3.math.Vector3f;
import de.yard.threed.core.Vector3;



/**
 * Created by thomass on 05.06.15.
 */
public class JmeVector3 /*implements Vector3*/ {
    Vector3f vector3;

    public JmeVector3(double x, double y, double z) {
        vector3 = new Vector3f(x,y,z);
    }

    JmeVector3(Vector3f vector3) {
        this.vector3 = vector3;
    }

    public static Vector3f toJme(Vector3 v) {
        return  new Vector3f(v.getX(),v.getY(),v.getZ());
    }

    public double getX() {
        return vector3.getX();
    }

    public double getY() {
        return vector3.getY();
    }

    public double getZ() {
        return vector3.getZ();
    }

    public static Vector3 fromJme(Vector3f v) {
        return  new Vector3(v.getX(),v.getY(),v.getZ());
    }

    /*private void writeObject(ObjectOutputStream oos) throws IOException {
        // default serialization 
        oos.defaultWriteObject();
        // write the object
        oos.writeFloat(vector3.getX());
        oos.writeFloat(vector3.getY());
        oos.writeFloat(vector3.getZ());
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        // default deserialization
        ois.defaultReadObject();
        vector3 = new Vector3f(ois.readFloat(), ois.readFloat(), ois.readFloat());
        
    }*/
}
