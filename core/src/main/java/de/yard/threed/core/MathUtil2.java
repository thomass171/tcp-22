package de.yard.threed.core;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;


/**
 * Platformunabhaengige Matrix/Math/Quaternion Operationen.
 * Wobei? Auch fuer Unity (wegen lefthanded) ?
 */
public class MathUtil2 {
    public static double PI_2 = (Math.PI / 2);
    public static double PI = (Math.PI);
    public static double PI2 = (Math.PI * 2);
    public static float FLT_EPSILON = 1.19209290E-07f;
    public static double DBL_EPSILON = 2.2204460492503131e-016;

    // OpenGL default orientation pointing to 'us' (object's lookat).
    public static Vector3 DEFAULT_FORWARD = new Vector3(0, 0, 1);
    public static Vector3 DEFAULT_UP = new Vector3(0, 1, 0);
    public static Vector3 DEFAULT_LEFT = new Vector3(-1, 0, 0);
    public static Vector3 DEFAULT_RIGHT = new Vector3(1, 0, 0);

    /**
     * Kopiert und laut (http://fabiensanglard.net/doom3_documentation/37726-293748.pdf) angepasst.
     * Die Original scheint mir nicht ganz richtig zu sein. Diese angepasste Version liefert Ergebnisse
     * wie nachgerechnet und ThreeJS.
     *
     * @return
     */
    public static Matrix4 buildRotationMatrix(Quaternion q) {
        // das normalisieren dürfte wichtig sein, sonst kommt keine gültige Matrix dabei raus.
        // Vielleicht ist das auch das Problem mit der Originalversion.
        q = q.normalize();
        double x = q.getX();
        double y = q.getY();
        double z = q.getZ();
        double w = q.getW();
        double s = 1;

        double xs = x * s;
        double ys = y * s;
        double zs = z * s;
        double xx = x * xs;
        double xy = x * ys;
        double xz = x * zs;
        double xw = w * xs;
        double yy = y * ys;
        double yz = y * zs;
        double yw = w * ys;
        double zz = z * zs;
        double zw = w * zs;

        Matrix4 result = new Matrix4(
                1 - (2 * yy + 2 * zz),
                (2 * xy - 2 * zw),
                (2 * xz + 2 * yw),
                0,
                (2 * xy + 2 * zw),
                1 - (2 * xx + 2 * zz),
                (2 * yz - 2 * xw),
                0,
                (2 * xz - 2 * yw),
                (2 * yz + 2 * xw),
                1 - (2 * xx + 2 * yy),
                0,
                0, 0, 0, 1);

        //result.setScale(originalScale);

        return result;
    }

    /**
     * Aus JME.
     *
     * @return
     */
    public static Vector3 multiply(Quaternion q, Vector3 v) {
        if (v.getX() == 0 && v.getY() == 0 && v.getZ() == 0) {
            return new Vector3(0, 0, 0);
        } else {
            double vx = v.getX(), vy = v.getY(), vz = v.getZ();
            double x = q.getX(), y = q.getY(), z = q.getZ(), w = q.getW();
            Vector3 vec = new Vector3(w * w * vx + 2 * y * w * vz - 2 * z * w * vy + x * x
                    * vx + 2 * y * x * vy + 2 * z * x * vz - z * z * vx - y
                    * y * vx,
                    2 * x * y * vx + y * y * vy + 2 * z * y * vz + 2 * w
                            * z * vx - z * z * vy + w * w * vy - 2 * x * w * vz - x
                            * x * vy,
                    2 * x * z * vx + 2 * y * z * vy + z * z * vz - 2 * w
                            * y * vx - y * y * vz + 2 * w * x * vy - x * x * vz + w
                            * w * vz);
            return vec;
        }
    }

    /**
     * Das wird nicht nativ gemacht, weil es zu Laufzeitfehler kommen kann, deren Handhabung
     * hier besser machbar ist.
     */
    public static Matrix4 getInverse(Matrix4 m) {
        //Die folgende Operation liesse sich auch noch durch extrahieren mehrfacher Multiplikationen optimieren
        Matrix4 inverse = new Matrix4(
                m.getElement(1, 2) * m.getElement(2, 3) * m.getElement(3, 1) - m.getElement(1, 3) * m.getElement(2, 2) * m.getElement(3, 1) + m.getElement(1, 3) * m.getElement(2, 1) * m.getElement(3, 2) - m.getElement(1, 1) * m.getElement(2, 3) * m.getElement(3, 2) - m.getElement(1, 2) * m.getElement(2, 1) * m.getElement(3, 3) + m.getElement(1, 1) * m.getElement(2, 2) * m.getElement(3, 3),
                m.getElement(0, 3) * m.getElement(2, 2) * m.getElement(3, 1) - m.getElement(0, 2) * m.getElement(2, 3) * m.getElement(3, 1) - m.getElement(0, 3) * m.getElement(2, 1) * m.getElement(3, 2) + m.getElement(0, 1) * m.getElement(2, 3) * m.getElement(3, 2) + m.getElement(0, 2) * m.getElement(2, 1) * m.getElement(3, 3) - m.getElement(0, 1) * m.getElement(2, 2) * m.getElement(3, 3),
                m.getElement(0, 2) * m.getElement(1, 3) * m.getElement(3, 1) - m.getElement(0, 3) * m.getElement(1, 2) * m.getElement(3, 1) + m.getElement(0, 3) * m.getElement(1, 1) * m.getElement(3, 2) - m.getElement(0, 1) * m.getElement(1, 3) * m.getElement(3, 2) - m.getElement(0, 2) * m.getElement(1, 1) * m.getElement(3, 3) + m.getElement(0, 1) * m.getElement(1, 2) * m.getElement(3, 3),
                m.getElement(0, 3) * m.getElement(1, 2) * m.getElement(2, 1) - m.getElement(0, 2) * m.getElement(1, 3) * m.getElement(2, 1) - m.getElement(0, 3) * m.getElement(1, 1) * m.getElement(2, 2) + m.getElement(0, 1) * m.getElement(1, 3) * m.getElement(2, 2) + m.getElement(0, 2) * m.getElement(1, 1) * m.getElement(2, 3) - m.getElement(0, 1) * m.getElement(1, 2) * m.getElement(2, 3),
                m.getElement(1, 3) * m.getElement(2, 2) * m.getElement(3, 0) - m.getElement(1, 2) * m.getElement(2, 3) * m.getElement(3, 0) - m.getElement(1, 3) * m.getElement(2, 0) * m.getElement(3, 2) + m.getElement(1, 0) * m.getElement(2, 3) * m.getElement(3, 2) + m.getElement(1, 2) * m.getElement(2, 0) * m.getElement(3, 3) - m.getElement(1, 0) * m.getElement(2, 2) * m.getElement(3, 3),
                m.getElement(0, 2) * m.getElement(2, 3) * m.getElement(3, 0) - m.getElement(0, 3) * m.getElement(2, 2) * m.getElement(3, 0) + m.getElement(0, 3) * m.getElement(2, 0) * m.getElement(3, 2) - m.getElement(0, 0) * m.getElement(2, 3) * m.getElement(3, 2) - m.getElement(0, 2) * m.getElement(2, 0) * m.getElement(3, 3) + m.getElement(0, 0) * m.getElement(2, 2) * m.getElement(3, 3),
                m.getElement(0, 3) * m.getElement(1, 2) * m.getElement(3, 0) - m.getElement(0, 2) * m.getElement(1, 3) * m.getElement(3, 0) - m.getElement(0, 3) * m.getElement(1, 0) * m.getElement(3, 2) + m.getElement(0, 0) * m.getElement(1, 3) * m.getElement(3, 2) + m.getElement(0, 2) * m.getElement(1, 0) * m.getElement(3, 3) - m.getElement(0, 0) * m.getElement(1, 2) * m.getElement(3, 3),
                m.getElement(0, 2) * m.getElement(1, 3) * m.getElement(2, 0) - m.getElement(0, 3) * m.getElement(1, 2) * m.getElement(2, 0) + m.getElement(0, 3) * m.getElement(1, 0) * m.getElement(2, 2) - m.getElement(0, 0) * m.getElement(1, 3) * m.getElement(2, 2) - m.getElement(0, 2) * m.getElement(1, 0) * m.getElement(2, 3) + m.getElement(0, 0) * m.getElement(1, 2) * m.getElement(2, 3),
                m.getElement(1, 1) * m.getElement(2, 3) * m.getElement(3, 0) - m.getElement(1, 3) * m.getElement(2, 1) * m.getElement(3, 0) + m.getElement(1, 3) * m.getElement(2, 0) * m.getElement(3, 1) - m.getElement(1, 0) * m.getElement(2, 3) * m.getElement(3, 1) - m.getElement(1, 1) * m.getElement(2, 0) * m.getElement(3, 3) + m.getElement(1, 0) * m.getElement(2, 1) * m.getElement(3, 3),
                m.getElement(0, 3) * m.getElement(2, 1) * m.getElement(3, 0) - m.getElement(0, 1) * m.getElement(2, 3) * m.getElement(3, 0) - m.getElement(0, 3) * m.getElement(2, 0) * m.getElement(3, 1) + m.getElement(0, 0) * m.getElement(2, 3) * m.getElement(3, 1) + m.getElement(0, 1) * m.getElement(2, 0) * m.getElement(3, 3) - m.getElement(0, 0) * m.getElement(2, 1) * m.getElement(3, 3),
                m.getElement(0, 1) * m.getElement(1, 3) * m.getElement(3, 0) - m.getElement(0, 3) * m.getElement(1, 1) * m.getElement(3, 0) + m.getElement(0, 3) * m.getElement(1, 0) * m.getElement(3, 1) - m.getElement(0, 0) * m.getElement(1, 3) * m.getElement(3, 1) - m.getElement(0, 1) * m.getElement(1, 0) * m.getElement(3, 3) + m.getElement(0, 0) * m.getElement(1, 1) * m.getElement(3, 3),
                m.getElement(0, 3) * m.getElement(1, 1) * m.getElement(2, 0) - m.getElement(0, 1) * m.getElement(1, 3) * m.getElement(2, 0) - m.getElement(0, 3) * m.getElement(1, 0) * m.getElement(2, 1) + m.getElement(0, 0) * m.getElement(1, 3) * m.getElement(2, 1) + m.getElement(0, 1) * m.getElement(1, 0) * m.getElement(2, 3) - m.getElement(0, 0) * m.getElement(1, 1) * m.getElement(2, 3),
                m.getElement(1, 2) * m.getElement(2, 1) * m.getElement(3, 0) - m.getElement(1, 1) * m.getElement(2, 2) * m.getElement(3, 0) - m.getElement(1, 2) * m.getElement(2, 0) * m.getElement(3, 1) + m.getElement(1, 0) * m.getElement(2, 2) * m.getElement(3, 1) + m.getElement(1, 1) * m.getElement(2, 0) * m.getElement(3, 2) - m.getElement(1, 0) * m.getElement(2, 1) * m.getElement(3, 2),
                m.getElement(0, 1) * m.getElement(2, 2) * m.getElement(3, 0) - m.getElement(0, 2) * m.getElement(2, 1) * m.getElement(3, 0) + m.getElement(0, 2) * m.getElement(2, 0) * m.getElement(3, 1) - m.getElement(0, 0) * m.getElement(2, 2) * m.getElement(3, 1) - m.getElement(0, 1) * m.getElement(2, 0) * m.getElement(3, 2) + m.getElement(0, 0) * m.getElement(2, 1) * m.getElement(3, 2),
                m.getElement(0, 2) * m.getElement(1, 1) * m.getElement(3, 0) - m.getElement(0, 1) * m.getElement(1, 2) * m.getElement(3, 0) - m.getElement(0, 2) * m.getElement(1, 0) * m.getElement(3, 1) + m.getElement(0, 0) * m.getElement(1, 2) * m.getElement(3, 1) + m.getElement(0, 1) * m.getElement(1, 0) * m.getElement(3, 2) - m.getElement(0, 0) * m.getElement(1, 1) * m.getElement(3, 2),
                m.getElement(0, 1) * m.getElement(1, 2) * m.getElement(2, 0) - m.getElement(0, 2) * m.getElement(1, 1) * m.getElement(2, 0) + m.getElement(0, 2) * m.getElement(1, 0) * m.getElement(2, 1) - m.getElement(0, 0) * m.getElement(1, 2) * m.getElement(2, 1) - m.getElement(0, 1) * m.getElement(1, 0) * m.getElement(2, 2) + m.getElement(0, 0) * m.getElement(1, 1) * m.getElement(2, 2)

        );
        double determinant = getDeterminant(m);

        if (determinant == 0.0f) {
            // 5.12.24: This might really happen under circumstances we don't know currently (maybe at the poles?)
            // So don't abort but return decision to caller
            getLogger().error("determinant is 0");
            return null;
        }
        return multiply(inverse, 1f / determinant);

    }

    /**
     * Aus ThreeJS bzw. on http://code.google.com/p/webgl-mjs/
     * Aus mathematischer Sicht kommt mir das sehr fragwürdig vor.
     *
     * @return
     */
    public static Matrix3 getInverseAsMatrix3(Matrix4 m) {

        double te0 = m.getElement(2, 2) * m.getElement(1, 1) - m.getElement(2, 1) * m.getElement(1, 2);
        double te1 = -m.getElement(2, 2) * m.getElement(1, 0) + m.getElement(2, 0) * m.getElement(1, 2);
        double te2 = m.getElement(2, 1) * m.getElement(1, 0) - m.getElement(2, 0) * m.getElement(1, 1);
        double te3 = -m.getElement(2, 2) * m.getElement(0, 1) + m.getElement(2, 1) * m.getElement(0, 2);
        double te4 = m.getElement(2, 2) * m.getElement(0, 0) - m.getElement(2, 0) * m.getElement(0, 2);
        double te5 = -m.getElement(2, 1) * m.getElement(0, 0) + m.getElement(2, 0) * m.getElement(0, 1);
        double te6 = m.getElement(1, 2) * m.getElement(0, 1) - m.getElement(1, 1) * m.getElement(0, 2);
        double te7 = -m.getElement(1, 2) * m.getElement(0, 0) + m.getElement(1, 0) * m.getElement(0, 2);
        double te8 = m.getElement(1, 1) * m.getElement(0, 0) - m.getElement(1, 0) * m.getElement(0, 1);

        /*logger.debug("te0="+te0);
        logger.debug("te1="+te1);
        logger.debug("te2="+te2);
        logger.debug("te3="+te3);
        logger.debug("te4="+te4);
        logger.debug("te5="+te5);
        logger.debug("te6="+te6);
        logger.debug("te7="+te7);
        logger.debug("te8="+te8);*/
        Matrix3 m3 = new Matrix3(te0, te3, te6,
                te1, te4, te7,
                te2, te5, te8);

        // Das ist nicht mit getDerminant() identisch (??).
        double determinant = m.getElement(0, 0) * te0 + m.getElement(1, 0) * te3 + m.getElement(2, 0) * te6;
        //logger.debug("determinant="+determinant);

        if (determinant == 0.0f) {
//TODO anders
            throw new RuntimeException("determinant is 0");
        }
        return m3.multiply(1.0 / determinant);
    }

    public static double getDeterminant(Matrix4 m) {
        return m.getElement(0, 0) * (m.getElement(1, 2) * m.getElement(2, 3) * m.getElement(3, 1) - m.getElement(1, 3) * m.getElement(2, 2) * m.getElement(3, 1) + m.getElement(1, 3) * m.getElement(2, 1) * m.getElement(3, 2) - m.getElement(1, 1) * m.getElement(2, 3) * m.getElement(3, 2) - m.getElement(1, 2) * m.getElement(2, 1) * m.getElement(3, 3) + m.getElement(1, 1) * m.getElement(2, 2) * m.getElement(3, 3)) +
                m.getElement(1, 0) * (m.getElement(0, 3) * m.getElement(2, 2) * m.getElement(3, 1) - m.getElement(0, 2) * m.getElement(2, 3) * m.getElement(3, 1) - m.getElement(0, 3) * m.getElement(2, 1) * m.getElement(3, 2) + m.getElement(0, 1) * m.getElement(2, 3) * m.getElement(3, 2) + m.getElement(0, 2) * m.getElement(2, 1) * m.getElement(3, 3) - m.getElement(0, 1) * m.getElement(2, 2) * m.getElement(3, 3)) +
                m.getElement(2, 0) * (m.getElement(0, 2) * m.getElement(1, 3) * m.getElement(3, 1) - m.getElement(0, 3) * m.getElement(1, 2) * m.getElement(3, 1) + m.getElement(0, 3) * m.getElement(1, 1) * m.getElement(3, 2) - m.getElement(0, 1) * m.getElement(1, 3) * m.getElement(3, 2) - m.getElement(0, 2) * m.getElement(1, 1) * m.getElement(3, 3) + m.getElement(0, 1) * m.getElement(1, 2) * m.getElement(3, 3)) +
                m.getElement(3, 0) * (m.getElement(0, 3) * m.getElement(1, 2) * m.getElement(2, 1) - m.getElement(0, 2) * m.getElement(1, 3) * m.getElement(2, 1) - m.getElement(0, 3) * m.getElement(1, 1) * m.getElement(2, 2) + m.getElement(0, 1) * m.getElement(1, 3) * m.getElement(2, 2) + m.getElement(0, 2) * m.getElement(1, 1) * m.getElement(2, 3) - m.getElement(0, 1) * m.getElement(1, 2) * m.getElement(2, 3));

    }

    public static Matrix4 multiply(Matrix4 m, double f) {
        Matrix4 res = new Matrix4(
                m.getElement(0, 0) * f, m.getElement(0, 1) * f, m.getElement(0, 2) * f, m.getElement(0, 3) * f,
                m.getElement(1, 0) * f, m.getElement(1, 1) * f, m.getElement(1, 2) * f, m.getElement(1, 3) * f,
                m.getElement(2, 0) * f, m.getElement(2, 1) * f, m.getElement(2, 2) * f, m.getElement(2, 3) * f,
                m.getElement(3, 0) * f, m.getElement(3, 1) * f, m.getElement(3, 2) * f, m.getElement(3, 3) * f);
        return res;
    }

    /**
     * Zeilen und Spalten tauschen
     *
     * @return
     */
    public static Matrix4 transpose(Matrix4 m) {
        return new Matrix4(m.getElement(0, 0), m.getElement(1, 0), m.getElement(2, 0), m.getElement(3, 0),
                m.getElement(0, 1), m.getElement(1, 1), m.getElement(2, 1), m.getElement(3, 1),
                m.getElement(0, 2), m.getElement(1, 2), m.getElement(2, 2), m.getElement(3, 2),
                m.getElement(0, 3), m.getElement(1, 3), m.getElement(2, 3), m.getElement(3, 3));
    }

    /**
     * Aus JME
     *
     * @param q1
     * @param q2
     * @return
     */
    public static Quaternion multiply(Quaternion q1, Quaternion q2) {
        double qw = q2.getW(), qx = q2.getX(), qy = q2.getY(), qz = q2.getZ();
        Quaternion res = new Quaternion(
                q1.getX() * qw + q1.getY() * qz - q1.getZ() * qy + q1.getW() * qx,
                -q1.getX() * qz + q1.getY() * qw + q1.getZ() * qx + q1.getW() * qy,
                q1.getX() * qy - q1.getY() * qx + q1.getZ() * qw + q1.getW() * qz,
                -q1.getX() * qx - q1.getY() * qy - q1.getZ() * qz + q1.getW() * qw);
        return res;
    }

    /*MA16 public static Quaternion buildQuaternionFromAngles(*/


    public static Vector3 divideScalar(Vector3 v, double scalar) {
        double invScalar = 1.0f / scalar;
        return new Vector3((v.getX() * invScalar), (v.getY() * invScalar), (v.getZ() * invScalar));
        //return new Vector3((v.getX() / scalar), (v.getY() / scalar), (v.getZ() / scalar));
    }


    /**
     * Kreuzprodukt. Wikipedia: Das Kreuzprodukt der Vektoren a und b
     * ist ein Vektor, der senkrecht auf der von den beiden Vektoren aufgespannten Ebene steht und mit ihnen ein Rechtssystem bildet.
     * Die Länge dieses Vektors entspricht dem Flächeninhalt des Parallelogramms, das von den Vektoren a und b aufgespannt wird.
     * <p>
     * Definitionsgemaess nicht normalisiert.
     * <p>
     * Mit der Definition dürfte klar sein, dass weder a und b noch das Resultat normalisiert sein müssen.
     * 5.12.16: Umgestellt auf Rechnung mit double wegen Genauigkeitsproblemen bei getNormal(). Bringt aber nicht viel.
     *
     * @param a
     * @param b
     * @return
     */
    public static Vector3 getCrossProduct(Vector3 a, Vector3 b) {
        // NAch Wikipedia
        //Vector3 output = new Vector3(p1.getY() * p2.getZ() - p1.getZ() * p2.getY(),
        //        p1.getZ() * p2.getX() - p1.getX() * p2.getZ(),
        //        p1.getX() * p2.getY() - p1.getY() * p2.getX());

        double x1 = (double) a.getY() * (double) b.getZ();
        double x2 = (double) a.getZ() * (double) b.getY();
        double y = (double) a.getZ() * (double) b.getX() - (double) a.getX() * (double) b.getZ();
        double z = (double) a.getX() * (double) b.getY() - (double) a.getY() * (double) b.getX();
        Vector3 output = new Vector3((x1 - x2),
                y, z);

        return output;
    }

    /**
     * Einige Folgebrechnungen brauchen einen normalisierten Vektor.
     *
     * @param a
     * @param b
     * @return
     */
    public static Vector3 getNormalizedCrossProduct(Vector3 a, Vector3 b) {
        return getCrossProduct(a, b).normalize();
    }

    public static Vector3 add(Vector3 v1, Vector3 v2) {
        return new Vector3(v1.getX() + v2.getX(), v1.getY() + v2.getY(), v1.getZ() + v2.getZ());
    }

    public static Vector3 subtract(Vector3 v1, Vector3 v2) {
        return new Vector3(v1.getX() - v2.getX(), v1.getY() - v2.getY(), v1.getZ() - v2.getZ());
    }

    /**
     * Das Skalarprodukt (dot product, inner product) von zwei Vektoren
     * Liefert den Cosinus des Winkel zwischen zwei Vektoren, aber das nur bei normalisierten Vektoren!
     * Wenn sie nicht normalisiert sind, ist es trotzdem ein gültiges Skalarprodukt, das manche Berechnungen auch erfordern.
     * Zur Winkelbestimmung sollte daher lieber getAngleBetween() verwendet werden.
     * <p>
     * Liefert zwischen positiv (bei parallelen Vektoren) und negativ (bei entgegengesetzten Vektoren)
     *
     * @return
     */
    public static double getDotProduct(Vector3 v1, Vector3 v2) {
        double p = v1.getX() * v2.getX() + v1.getY() * v2.getY() + v1.getZ() * v2.getZ();
        return p;
    }

    public static double getDotProduct(Quaternion v1, Quaternion v2) {
        double p = v1.getX() * v2.getX() + v1.getY() * v2.getY() + v1.getZ() * v2.getZ() + v1.getW() * v2.getW();
        return p;
    }

    public static double getDistance(Vector3 p1, Vector3 p2) {
        return subtract(p1, p2).length();
    }

    /**
     * M = T x R x S
     */
    public static Matrix4 buildMatrix(Matrix4 translationMatrix, Matrix4 rotationMatrix, Matrix4 scaleMatrix) {
        // 16.7.14: Was ist denn nun die richtige Reihenfolge?
        Matrix4 modelMatrix = translationMatrix.multiply(rotationMatrix).multiply(scaleMatrix);
        //modelMatrix = scaleMatrix.multiply(rotationMatrix).multiply(translationMatrix);
        return modelMatrix;
    }

    public static Matrix4 buildMatrix(Vector3 pos, Quaternion rotation, Vector3 scale) {
        Matrix4 translationMatrix = buildTranslationMatrix(pos);
        Matrix4 rotationMatrix = buildRotationMatrix(rotation);
        Matrix4 scaleMatrix = buildScaleMatrix(scale);

        Matrix4 modelMatrix = translationMatrix.multiply(rotationMatrix).multiply(scaleMatrix);
        return modelMatrix;
    }

    public static Matrix4 buildTranslationMatrix(Vector3 v) {
        Matrix4 mat = new Matrix4();
        mat.setTranslation(v);
        return mat;
    }

    public static double toRadians(double angdeg) {
        return angdeg * Math.PI / 180.0;
    }

    public static double toDegrees(double radians) {
        return 180.0f * radians / Math.PI;
    }

    public static Quaternion multiply(Quaternion q, Matrix4 m) {
        Quaternion res = new Quaternion(
                m.getElement(0, 0) * q.getX() + m.getElement(0, 1) * q.getY() + m.getElement(0, 2) * q.getZ() + m.getElement(0, 3) * q.getW(),
                m.getElement(1, 0) * q.getX() + m.getElement(1, 1) * q.getY() + m.getElement(1, 2) * q.getZ() + m.getElement(1, 3) * q.getW(),
                m.getElement(2, 0) * q.getX() + m.getElement(2, 1) * q.getY() + m.getElement(2, 2) * q.getZ() + m.getElement(2, 3) * q.getW(),
                m.getElement(3, 0) * q.getX() + m.getElement(3, 1) * q.getY() + m.getElement(3, 2) * q.getZ() + m.getElement(3, 3) * q.getW());

        return res;
    }

    public static Matrix4 buildScaleMatrix(Vector3 v) {
        Matrix4 mat = new Matrix4(
                v.getX(), 0, 0, 0,
                0, v.getY(), 0, 0,
                0, 0, v.getZ(), 0,
                0, 0, 0, 1);
        return mat;
    }

    /**
     * Den scale Anteil der Matrix extrahieren.
     *
     * @return
     */
    public static Vector3 extractScale(Matrix4 m) {
        double e11 = m.getElement(0, 0);
        double e12 = m.getElement(0, 1);
        double e13 = m.getElement(0, 2);
        double e21 = m.getElement(1, 0);
        double e22 = m.getElement(1, 1);
        double e23 = m.getElement(1, 2);
        double e31 = m.getElement(2, 0);
        double e32 = m.getElement(2, 1);
        double e33 = m.getElement(2, 2);

        return new Vector3(Math.sqrt(e11 * e11 + e21 * e21 + e31 * e31),
                Math.sqrt(e12 * e12 + e22 * e22 + e32 * e32),
                Math.sqrt(e13 * e13 + e23 * e23 + e33 * e33));
    }

    /**
     * Den Rotationsanteil der Matrix als Quaternion extrahieren. Das geht rein aus dem linken oberen 3x3 Anteil.
     * <p>
     * Sicher ist aber, dass der Algorithmus nur mit Matrizen ohne Scale geht, vor allem nicht mit non uniform scale.
     *
     * @return
     */
    public static Quaternion extractQuaternion(Matrix4 m) {
        // als erstes scale rausrechnen
        m = removeScale(m);

        double m00 = m.getElement(0, 0);
        double m01 = m.getElement(0, 1);
        double m02 = m.getElement(0, 2);
        double m10 = m.getElement(1, 0);
        double m11 = m.getElement(1, 1);
        double m12 = m.getElement(1, 2);
        double m20 = m.getElement(2, 0);
        double m21 = m.getElement(2, 1);
        double m22 = m.getElement(2, 2);
        return extractQuaternion(m00, m01, m02,
                m10, m11, m12,
                m20, m21, m22);
    }

    /**
     * Den Rotationsanteil aus einer 3x3 Matrix als Quaternion extrahieren.
     * <p/>
     * Der Algorithmus ist Woodo. Infos dazu finden sich bei
     * http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/index.htm
     * <p/>
     * Es gibt auch andere Quellen. Die Algorithmen sind teilweise leicht unterschiedlich.
     * Sicher ist aber, dass der Algorithmus nur mit Matrizen ohne Scale geht, vor allem nicht mit non uniform scale.
     * Das muss der Aufrufer sicherstellen.
     *
     * @return
     */
    private static Quaternion extractQuaternion(double m00, double m01, double m02,
                                                double m10, double m11, double m12,
                                                double m20, double m21, double m22) {
   
        /*double m00 = ohnescale.matrix[0], m01 = ohnescale.matrix[3], m02 = ohnescale.matrix[6],
                m10 = ohnescale.matrix[1], m11 = ohnescale.matrix[4], m12 = ohnescale.matrix[7],
                m20 = ohnescale.matrix[2], m21 = ohnescale.matrix[5], m22 = ohnescale.matrix[8];*/
        double trace = m00 + m11 + m22;
        double x, y, z, w;

        // Der folgende Algorithmus ist aus jme3

        if (trace >= 0) {
            double s = Math.sqrt(trace + 1);
            w = 0.5f * s;
            s = 0.5f / s;
            x = (m21 - m12) * s;
            y = (m02 - m20) * s;
            z = (m10 - m01) * s;
        } else if ((m00 > m11) && (m00 > m22)) {
            double s = Math.sqrt(1.0f + m00 - m11 - m22);
            x = s * 0.5f;
            s = 0.5f / s;
            y = (m10 + m01) * s;
            z = (m02 + m20) * s;
            w = (m21 - m12) * s;
        } else if (m11 > m22) {
            double s = Math.sqrt(1.0f + m11 - m00 - m22);
            y = s * 0.5f;
            s = 0.5f / s;
            x = (m10 + m01) * s;
            z = (m21 + m12) * s;
            w = (m02 - m20) * s;
        } else {
            double s = Math.sqrt(1.0f + m22 - m00 - m11);
            z = s * 0.5f;
            s = 0.5f / s;
            x = (m02 + m20) * s;
            y = (m21 + m12) * s;
            w = (m10 - m01) * s;
        }
        Quaternion q = new Quaternion(x, y, z, w).normalize();
        return q;
    }

    /**
     * 20.4.16: Ob das aus Matrix3 so auch für Matrix4 gilt?
     * <p/>
     * Aus http://gamedev.stackexchange.com/questions/119702/fastest-way-to-neutralize-scale-in-the-transform-matrix:
     * <p/>
     * Assuming your matrix multiplication follows the convention...
     * M * v = (T * R * S) * v
     * (where M is your composed matrix, T is a Translation matrix, R rotation, S scale, and v is a vector you want to transform using the matrix)
     * ...then you can normalize the getFirst three columns of the matrix to get just the T * R part.
     * If you use the opposite matrix multiplication convention (v * M) then you'd normalize the getFirst three rows instead. Either way, you only want to modify the 3x3 block of entries in the top-left of the matrix, ignoring the last row & column (which contain translation information and the homogeneous unit)
     * If you want to eke out every last CPU cycle, you can play with SIMD instructions to do the three vector normalizations with one multiply & square root, but this is likely to only be noticeable if you're processing big batches of these matrices in a very friendly data layout.
     *
     * @param m
     * @return
     */
    public static Matrix4 removeScale(Matrix4 m) {
        double m00 = m.getElement(0, 0);
        double m01 = m.getElement(0, 1);
        double m02 = m.getElement(0, 2);
        double m10 = m.getElement(1, 0);
        double m11 = m.getElement(1, 1);
        double m12 = m.getElement(1, 2);
        double m20 = m.getElement(2, 0);
        double m21 = m.getElement(2, 1);
        double m22 = m.getElement(2, 2);

        //double sx = MathUtil2.length(new OpenGlVector3(matrix[0], matrix[1], matrix[2]));
        //double sy = MathUtil2.length(new OpenGlVector3(matrix[3], matrix[4], matrix[5]));
        //double sz = MathUtil2.length(new OpenGlVector3(matrix[6], matrix[7], matrix[8]));


        double sx = getColumn(m, 0).length();
        double sy = getColumn(m, 1).length();
        double sz = getColumn(m, 2).length();
        // Das mit der Determinanten macht ThreeJS so. JME nicht.
        double det = getDeterminant(m);
        if (det < 0) {
            sx = -sx;
        }
        sx = 1 / sx;
        sy = 1 / sy;
        sz = 1 / sz;

        return new Matrix4(
                m00 * sx, m01 * sy, m02 * sz, m.getElement(0, 3),
                m10 * sx, m11 * sy, m12 * sz, m.getElement(1, 3),
                m20 * sx, m21 * sy, m22 * sz, m.getElement(2, 3),
                m.getElement(3, 0), m.getElement(3, 1), m.getElement(3, 2), m.getElement(3, 3));
    }

    public static Vector3 extractPosition(Matrix4 m) {
        return new Vector3(m.getElement(0, 3), m.getElement(1, 3), m.getElement(2, 3));
    }

    /**
     * Das ist jetzt auch so eine Ungereimtheit. Muesste eigentlich Matrix3 sein.
     *
     * @param column
     * @return
     */
    public static Vector3 getColumn(Matrix4 m, int column) {
        return new Vector3(m.getElement(0, column), m.getElement(1, column), m.getElement(2, column));
    }

    public static double distSqr(Vector3 p1, Vector3 p2) {
        return (subtract(p1, p2)).lengthSqr();
    }

    /**
     * Aus http://www.geometrictools.com/GTEngine/Include/GteIntrRay3Triangle3.inl
     * Auch in ThreeJS (// from http://www.geometrictools.com/LibMathematics/Intersection/Wm5IntrRay3Triangle3.cpp).
     *
     * @param v0
     * @param v1
     * @param v2
     * @return Den Schnittpunkt mit dem Dreieck oder null, wenn es keinen gibt.
     */
    public static Vector3 getTriangleIntersection(Vector3 origin, Vector3 direction, Vector3 v0, Vector3 v1, Vector3 v2) {
        Vector3 edge1, edge2, normal;              // triangle vectors
        Vector3 w0, w;           // maze vectors
        double a, b;              // params to calc maze-plane intersect

        //28.8.16: direction sicherheitshalber normalize
        direction = direction.normalize();

        Vector3 diff = MathUtil2.subtract(origin, v0);
        edge1 = MathUtil2.subtract(v1, v0);
        edge2 = MathUtil2.subtract(v2, v0);
        //28.2.17: Das Kreusprodukt selber ist nicht normalisiert.
        normal = MathUtil2.getCrossProduct(edge1, edge2);

        // Solve Q + t*D = b1*E1 + b2*E2 (Q = kDiff, D = maze direction,
        // E1 = edge1, E2 = edge2, N = Cross(E1,E2)) by
        //   |Dot(D,N)|*b1 = sign(Dot(D,N))*Dot(D,Cross(Q,E2))
        //   |Dot(D,N)|*b2 = sign(Dot(D,N))*Dot(D,Cross(E1,Q))
        //   |Dot(D,N)|*t = -sign(Dot(D,N))*Dot(Q,N)
        double DdN = MathUtil2.getDotProduct(direction, normal);
        //logger.debug("DdN="+DdN);
        double sign;
        if (DdN > 0) {
            sign = 1;
        } else if (DdN < 0) {
            sign = -1;
            DdN = -DdN;
        } else {
            // Ray and triangle are parallel, call it a "no intersection"
            // even if the maze does intersect.
            //result.intersect = false;
            //return result;
            return null;
        }

        double DdQxE2 = sign * MathUtil2.getDotProduct(direction, MathUtil2.getCrossProduct(diff, edge2));
        //logger.debug("normal="+normal+",edge1="+edge1+",direction="+direction);
        //logger.debug("diff="+diff+",edge2="+edge2+" sign="+sign+", DdN="+DdN);
        //logger.debug("v0="+v0+",v1="+v1+",v2="+v2+"DdQxE2="+DdQxE2+"origin="+origin);
        if (DdQxE2 >= 0) {
            double DdE1xQ = sign * MathUtil2.getDotProduct(direction, MathUtil2.getCrossProduct(edge1, diff));
            if (DdE1xQ >= 0) {
                if (DdQxE2 + DdE1xQ <= DdN) {
                    // Line intersects triangle, check whether maze does.
                    double QdN = -sign * MathUtil2.getDotProduct(diff, normal);
                    if (QdN >= 0) {
                        // Ray intersects triangle.
                        // result.intersect = true;
                        double inv = (1) / DdN;
                        //result.parameter = QdN*inv;
                        /*result.triangleBary[1] = DdQxE2*inv;
                        result.triangleBary[2] = DdE1xQ*inv;
                        result.triangleBary[0] = 1 - result.triangleBary[1]
                                - result.triangleBary[2];
                        result.point = maze.origin +
                                result.parameter * maze.direction;
                        return result;*/
                        return MathUtil2.add(origin, direction.multiply(QdN / DdN));
                    }
                    // else: t < 0, no intersection
                }
                // else: b1+b2 > 1, no intersection
            }
            // else: b2 < 0, no intersection
        }
        // else: b1 < 0, no intersection

        //result.intersect = false;
        //return result;
        return null;
    }

    /**
     * Aus einer Blickrichtung und der up-Orientierung die entsprechende Rotation bilden. Muesste eigentlich auch mit buildQuaternion()
     * ermittelbar sein, evtl. mit Speieglung der y-Achse (oder eine andere?). Die Algorithmen sind aber komplett unterschiedlich (oder das kommt einem
     * wegen der Matrix so vor).
     * <p>
     * Der Referenzvektor ist (0,0,-1; oder -1, wegen Achensiegelung?), d.h. Die Rotation zu diesem Vektor ist der Einheitsquaternion (bei up-y=1)(?).
     * <p>
     * Dazu die beiden normalisieren (evtl. unnoetig?), das CrossProduct, das in eine Matrix und dann Quaternion
     * extrahieren. Das scheint der uebliche Weg zu sein.
     * Der up Vektor muss nicht unbedingt orthogonal zu forward sein, er wird aus dem Cross
     * erneut berechnet.
     * <p>
     * 14.12.16: Auch wenn look im Namen ist, ist das eine allgemeingültige Methode um die Rotation in Verbindung mit einer Ausrichtung zu ermitteln.
     * Komisch, dass es teilweise mit negate() auf forward aufgerufen werden muss. Evtl. ist es doch auf Look ausgelegt.
     *
     * @return
     */
    public static Quaternion buildLookRotation(Vector3 forward, Vector3 up) {
        forward = (forward).normalize();
        up = (up).normalize();
        Vector3 right = (getCrossProduct(up, forward)).normalize();
        up = (getCrossProduct(forward, right)).normalize();

        //Matrix3 m = new Matrix3(
        return extractQuaternion(right, up, forward);
                /*right.getX(), up.getX(), forward.getX(),
                right.getY(), up.getY(), forward.getY(),
                right.getZ(), up.getZ(), forward.getZ());*/
    }

    public static Quaternion extractQuaternion(Vector3 right, Vector3 up, Vector3 forward) {
        return extractQuaternion(
                right.getX(), up.getX(), forward.getX(),
                right.getY(), up.getY(), forward.getY(),
                right.getZ(), up.getZ(), forward.getZ());
    }

    /**
     * Liefert die Rotation, die erforderlich ist, um v1 in die selbe Orientierung wie v2 zu rotieren.
     * Aus der ThreeJs 71 Funktion setFromUnitVectors(), die wiederum auf
     * http://lolengine.net/blog/2014/02/24/quaternion-from-two-vectors-final
     * basiert. Beide Vectors muessen mormalisiert sein. Warum eigentlich?
     * 28.08.2015: Ist eintlich egal warum, wichtig ist, dass sie es sind. Sonst gibt
     * es falsche Resultate.
     * <p/>
     * Ist das eigentlich abhaengig von Euler Order?
     * 17.7.17: Ich glaube, die MEthode ist mit Vorsicht zu geniessen, weil es Fälle gibt, wo es
     * viele Rotationsmöglichkeiten gibt;oder immer(??). Bei OsmScene scheint sich das bestaetigt zu haben. Da fehlte mit dieser Methode
     * immer ein Stueck.
     * 18.7.17: Nach neuem Check scheint die Methode aber zu stimmen. Haben nur die opposite Vektoren ein Problem? Es sieht so aus, als wenn dann viele gleichwertige
     * zufällige Lösungen exisieren.
     * 15.3.18: Gilt das nicht immer? Gibt es nicht immer unendlich viele mögliche Rotationen. In 2D gibt es ja auch immer mehrere (links und rechts rum).
     */
    public static Quaternion buildQuaternion(Vector3 from, Vector3 to) {
        from = (from).normalize();
        to = (to).normalize();
        double r;
        Vector3 v1;
        double EPS = 0.000001f;

        r = getDotProduct(from, to) + 1;
        if (r < EPS) {
            //opposite
            r = 0;
            if (Math.abs(from.getX()) > Math.abs(from.getZ())) {
                v1 = new Vector3(-from.getY(), from.getX(), 0);
            } else {
                v1 = new Vector3(0, -from.getZ(), from.getY());
            }
        } else {
            v1 = getCrossProduct(from, to);
        }
        Quaternion q = new Quaternion(v1.getX(), v1.getY(), v1.getZ(), r).normalize();
        return q;


        /*r = 1+getDotProduct(from, to) ;
        if (r < EPS) {
            r = 0;
            if (Math.abs(from.getX()) > Math.abs(from.getZ())) {
                v1 = new Vector3(-from.getY(), from.getX(), 0);
            } else {
                v1 = new Vector3(0, -from.getZ(), from.getY());
            }
        } else {
            v1 = getCrossProduct(from, to);
        }
        Quaternion q = new Quaternion(r,v1.getX(), v1.getY(), v1.getZ());
        q.normalize();
        return q;*/

    }

    public static boolean equalsVector3(Vector3 v1, Vector3 v2, double tolerance) {
        if (Math.abs(v1.getX() - v2.getX()) > tolerance) {
            return false;
        }
        if (Math.abs(v1.getY() - v2.getY()) > tolerance) {
            return false;
        }
        if (Math.abs(v1.getZ() - v2.getZ()) > tolerance) {
            return false;
        }
        return true;
    }

    /**
     * Liefert den Winkel in Radian zwischen zwei Vektoren. Eine Rotation bzw. Richtung wird nicht geliefert, nur
     * der absolute Winkel (keine negativen).
     * <p>
     * Liefert maximal PI (bei entgegengesetzten Vektoren). 0 bei parallelen
     *
     * @param p1
     * @param p2
     * @return
     */
    public static double getAngleBetween(Vector3 p1, Vector3 p2) {
        double dot = getDotProduct(p1.normalize(), p2.normalize());
        // 9.2.17: Offenbar kann es zu Rundungsfehlern kommen. Dann scheitert speter der acos mit NaN. 
        if (dot < -1) {
            dot = -1;
        }
        if (dot > 1) {
            dot = 1;
        }
        return Math.acos(dot);
    }

    public static double sin(double f) {
        return Math.sin(f);
    }

    public static double asin(double f) {
        return Math.asin(f);
    }

    public static double atan2(double y, double x) {
        return Math.atan2(y, x);
    }

    public static double cos(double f) {
        return Math.cos(f);
    }

    /**
     * Das ist nicht geod geeignet.
     * 14.6.19: Ist doch bestimmt auch nicht korrekt. TODO klaeren
     * Aber direkt an der Stelle ist die Abweichung gering??
     *
     * @param v
     * @return
     */
    public static Degree getHeadingFromDirection(Vector2 v) {
        double a = Math.atan2(v.getX(), v.getY());
        if (a < 0) {
            a += PI2;
        }
        return Degree.buildFromRadians(a);
    }

    /**
     * Result is normalized.
     * 14.5.18: Deprecated weil nicht korrekt. TODO klaeren. Die Abweichung fürht doch schon innerhalb eines Airport zu sichtbaren Fehlern, oder?.
     */
    @Deprecated
    public static Vector2 getDirectionFromHeading(Degree d) {
        double rad = -d.toRad();
        double x = -Math.sin(rad);
        double y = Math.cos(rad);
        return new Vector2(x, y);
    }

    /**
     * Unter der Annahme eines -x Headings als Default.
     *
     * @param heading
     * @return
     */
    public static Degree getDegreeFromHeading(Degree heading) {
        return new Degree(-(90 + heading.getDegree()));
    }

    public static boolean areEqual(double f1, double f2) {
        return areEqual(f1, f2, FLT_EPSILON);
    }

    /**
     * 14.5.18 double->double
     */
    public static boolean areEqual(double f1, double f2, double epsilon) {
        return Math.abs(f1 - f2) <= epsilon;
    }

    /**
     * Liefert den zu "p" nächsten Punkt auf dem Vector "v" von "start" aus.
     * Skizze 24
     *
     * @param p
     * @param start
     * @param v
     * @return
     */
    public static Vector3 getNearestPointOnVector(Vector3 p, Vector3 start, Vector3 v) {
        Vector3 v1 = subtract(start, p);
        double angle = getAngleBetween(v1, v);
        if (angle < PI_2) {
            return start;
        }
        double b = -(cos(angle) * (v1).length());
        if (b > (v).length()) {
            return add(start, v);
        }
        return add(start, (v).normalize().multiply(b));
    }

    /**
     * Schnittpunkt zweier Geraden (p1,p2) und (p3,p4). Nur 2D! Die Parameter sind Koordinaten, nicht Vektoren.
     * Cramersche Regel.
     */
    public static Vector2 getLineIntersection(Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4) {
        double nenner = ((((double) p4.getY() - p3.getY()) * (p2.getX() - p1.getX())) - ((p2.getY() - p1.getY()) * (p4.getX() - p3.getX())));
        if (Math.abs(nenner) < FLT_EPSILON) {
            return null;
        }
        double xs = (((p4.getX() - p3.getX()) * ((p2.getX() * p1.getY()) - (p1.getX() * p2.getY()))) -
                ((p2.getX() - p1.getX()) * ((p4.getX() * p3.getY()) - (p3.getX() * p4.getY())))) / nenner;

        //bleibt nenner = ((p4.getY()-p3.getY())*(p2.getX()-p1.getX())-(p2.getY()-p1.getY())*(p4.getX()-p3.getX()));
        //if (nenner < FLT_EPSILON){
        //  return null;
        //}
        double ys = ((((double) p1.getY() - p2.getY()) * (p4.getX() * p3.getY() - p3.getX() * p4.getY())) -
                ((p3.getY() - p4.getY()) * (p2.getX() * p1.getY() - p1.getX() * p2.getY()))) / nenner;
        return new Vector2(xs, ys);
    }

    /**
     * Pruefung ob ein Punkt auf einer Strecke liegt (p1,p2). Nur 2D! Die Parameter sind Koordinaten, nicht Vektoren.
     */
    public static boolean isPointOnLine(Vector2 start, Vector2 end, Vector2 p) {
        if (p.getX() < Math.min(start.getX(), end.getX())) {
            return false;
        }
        if (p.getX() > Math.max(start.getX(), end.getX())) {
            return false;
        }
        if (p.getY() < Math.min(start.getY(), end.getY())) {
            return false;
        }
        if (p.getY() > Math.max(start.getY(), end.getY())) {
            return false;
        }
        return true;
    }

    /**
     * From C++
     * Computes the floating-point remainder of the division operation x / y
     */
    public static double fmod(double d1, double d2) {
        // The int mod operation should also work for double
        return d1 % d2;
    }

    static private Log getLogger() {
        return Platform.getInstance().getLog(MathUtil2.class);
    }
}

