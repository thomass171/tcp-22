package de.yard.threed.graph;

import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;

/**
 * Definition of an arc 3D segment from direction ex to direction ey with center and radius.
 * Dies hier sind die daten für einen Innen/Aussenkreis(?,ja, auch aussen, dann aber anderes beta) im Schenkel. Es gibt auch GraphArcParameter.
 */
public class GraphArc {
    private static Log logger = Platform.getInstance().getLog(GraphArc.class);

    public Vector3 arccenter/*11.4.18, arcbeginloc, v2*/, crossproduct;
    //ex,ey sind wegen Weiterverwendung in Berechnungen normalisiert.
    //muss besser e1 e2 heissen, weil es nicht wirklich ex ey ist.
    public Vector3 ex/*, 15.3.18 brauchts nicht ey*/;
    //Normale auf den Kreis. Kann bei Halbkreisen nicht aus e1 und e2 berechnet werden.
    public Vector3 n;
    private double radius;
    // eigentlich wird beta hier doch nicht wirklich gebraucht. 11.4.18: Zumindest ist der Name in der edge verwirrend.
    public double beta;
    //public boolean inner;
    //optional, eg. the smoothed node in a smootharc
    public GraphNode origin;

    public GraphArc(Vector3 arccenter, double radius, Vector3 ex, Vector3 n,double beta) {
        this.arccenter = arccenter;
        this.radius = radius;
        //sicher ist sicher
        this.ex = ex.normalize();
        this.n = n.normalize();
        this.beta=beta;
    }
    
    /**
     * Effective rotated e Vector between ex (t=0) and ey (t=1).
     * e1 und e2 sind normiert. Das Resultat hat richtige Länge (radius).
     * 
     * 12.11.18: Das ist doch eigentlich ein rotateOnAxis? 5.4.20:Nee, zumindest keine der Standard xyz-Achsen, sondern eine beliebige im Raum.
     * @return
     */
    public Vector3 getRotatedEx(double t, /*float radius*/ float ellipsefactor) {
        Vector3 e1 = ex;
       // Vector3 e2 = ey;
        //Vector3 n = Vector3.getCrossProduct(e2, e1);
        //System.out.println("n=" + n);
        Quaternion z2n = Quaternion.buildQuaternion(new Vector3(0, 0, 1), n);
        //System.out.println("z2n=" + z2n);
        Quaternion n2z = Quaternion.buildQuaternion(n, new Vector3(0, 0, 1));
        //System.out.println("z2n=" + n2z);
        //15.3.18: angle nicht berechnen, den kenne ich doch als beta. Und er koennte auch negativ sein. Warum eigentlich negieren?
        double angle = -beta;//MathUtil2.getAngleBetween(e1.vector3, e2.vector3);
        Vector3 rotated = e1.rotate(n2z);
        rotated = rotated.rotate( Quaternion.buildFromAngles(0, 0, -angle * t));
        rotated = rotated.rotate(z2n);
        rotated = rotated.multiply(radius);
        //logger.debug("getRotatedEx:"+rotated);
        return rotated;
    }

    public Vector3 getRotatedEx(float t){
        return getRotatedEx(t,0);
    }

    public double getBeta() {
        return beta;
    }

    public double getRadius() {
        return radius;
    }

    public Vector3 getCenter() {
        return arccenter;
    }
}
