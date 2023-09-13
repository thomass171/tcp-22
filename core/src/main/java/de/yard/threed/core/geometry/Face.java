package de.yard.threed.core.geometry;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.buffer.NativeOutputStream;

/**
 * Date: 14.02.14
 * Time: 17:40
 */
public abstract class Face   {
    // 20.10.15: Die Normale für die ganze Face. Für gebogene Flächen wird die Normale in den
    // Subklassen pro Vertex abgebildet.
    // 16.2.16: Das ist nicht mehr aktuell. Dafuer gibt es Smooting Group Berechnung in der Platform.
    // Wenn normal null ist, muss die Platform die Normale berechnen
    // 13.2.16: Die Normalen koennen aber auch ausserhalb der Faces in den Vertices bzw. parallel dazu
    // abgelegt werden. Aber ob das wirklich genutzt wird?
    // 15.2.2016: Auf jeden Fall kann es aber mehrere Normals geben. Darum Liste.
    // 29.4.16: Das birgt aber die Gefahr von Inkonsistenzen. Vertices einer Fac3List werden im VBO nicht mehr dupliziert.
    // 13.7.16 und frueher:    Mehrere normale in der Face sind obselet.

    //public List<Vector3> normals;
    public Vector3 normal;

    //Dies Eigenschaft muss an die ganze Surface (oder das Material), nicht an ein einzelnes Face
    //16.2.16: Aber im Moment ist es so einfacher
    public boolean hasUV = false;

    public abstract void serialize(NativeOutputStream outs);

    /**
     * Waehrend der Vertexduplizierung bei der Normalenbildung einen Index ersetzen.
     */
    public abstract void replaceIndex(int vindex, int newindex);

    public abstract int[] getIndices();
    
}
