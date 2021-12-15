package de.yard.threed.core;

/**
 * Just a simple transform data container. Ansonsten gibt es noch Transform.java
 * <p>
 * 9.1.19: PosRot->LocalTransform. Der Name "transform" fuer "position" ist doch irgendwie nicht universell genug.
 * <p>
 * Created by thomass on 05.01.17.
 */
public class LocalTransform {
    public Vector3 position, scale;
    public Quaternion rotation;

    public LocalTransform(Vector3 position, Quaternion rotation) {
        this.position = position;
        this.rotation = rotation;
        this.scale = new Vector3(1, 1, 1);
    }

    public LocalTransform(Vector3 position, Quaternion rotation, Vector3 scale) {
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    /**
     * Builds from pattern x,y,z,xdeg,ydeg,zdeg
     *
     */
    public static LocalTransform buildFromConfig(String posrot) {
        String[] parts = StringUtils.split(posrot, ",");
        if (parts.length != 6) {
            return null;
        }
        Vector3 v = new Vector3(Util.parseDouble(parts[0]),Util.parseDouble(parts[1]),Util.parseDouble(parts[2]));
        Quaternion q = Quaternion.buildFromAngles(new Degree(Util.parseDouble(parts[3])),new Degree(Util.parseDouble(parts[4])),new Degree(Util.parseDouble(parts[5])));
        return new LocalTransform(v,q);
    }

    @Override
    public String toString() {
        return position + "," + rotation + "," + scale;
    }
}
