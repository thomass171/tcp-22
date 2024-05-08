package de.yard.threed.core;


/**
 * Date: 14.02.14
 * Time: 08:41
 */
public class Vector2 {
    public double x, y;
//    private Vector3 rotation = new Vector3(0,0,0);

    public Vector2() {
        this.x = 0;
        this.y = 0;

    }

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;

    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getY() {
        return y;
    }

    public double getX() {
        return x;
    }

    public Vector2 clone() {
        return new Vector2(x, y);
    }

    public Vector2 add(Vector2 v) {
        return new Vector2(x + v.x, y + v.y);
    }

    public Vector2 subtract(Vector2 v) {
        return new Vector2(x - v.x, y - v.y);
    }

    public Vector2 multiply(double scale) {
        return new Vector2(x * scale, y * scale);
    }

    /**
     * 30.8.18: Change to not self modififying
     *
     * @param scale
     */
    public Vector2 scale(double scale) {
        return new Vector2(x * scale, y * scale);
    }

    public double distance(Vector2 v) {
        double dx = v.x - this.x;
        double dy = v.y - this.y;
        return (double) Math.sqrt(dx * dx + dy * dy);
    }

    public double getDistanceSquared(Vector2 v) {
        double dx = v.x - this.x;
        double dy = v.y - this.y;
        return dx * dx + dy * dy;
    }

    public double length() {
        return (double) Math.sqrt(lengthSqr());

    }

    public double lengthSqr() {
        return getX() * getX() + getY() * getY();

    }

    /**
     * Liefert den Winkel zu p. Liefert 0 Grad wenn p genau oben liegt. Und dann nach rechts im Uhrzeigersinn.
     * 02.04.2015: Das ist mathematisch nicht schluessig. Da ist ein 0 Grad Winkel (im Einheitskreis) nach rechts
     * und geht von da gegen den Uhrzeigersinn.
     * <p>
     * 8.2.17: Es gibt jetzt auch die mathematisch "richtige" Variante in angle(v1,v2);
     * 10.9.19: Obwohl das ja eigentlich was anderes ist. Jetzt noch getRotationAngleBetween.
     *
     * @param p
     * @return
     */
    public Degree angle(Vector2 p) {
        double dis = distance(p);
        if (dis < 0.0000001f) {
            //TODO loggen und epsilon?
            return new Degree(0);
        }
        Degree basis = new Degree((double) MathUtil2.toDegrees(Math.acos((p.x - this.x) / dis)));
        if (p.x < x) {
            // ist links
            if (p.y < y)
                //base = base.add(new Degree(90));
                basis = new Degree(-basis.getDegree());//new Degree(180).subtract(base);
            else
                basis = basis;//base.add(new Degree(360));
        } else {
            if (p.y < y)
                //base = new Degree(180).subtract(base);
                basis = new Degree(-basis.getDegree());
            else
                basis = basis;
        }
        //System.out.println("base=" + basis.degree);
        return basis;
    }

    /**
     * Den Winkel zwischen den Strecken center-p1 und center-p2 berechnen (im Uhrzeigersinn)
     * Damit kommt immer ein positiver Winkel raus.
     * 02.04.2015: Jetzt im Gegenuhrzeiger. NeeNee, es duerfte besser sein, die gwuenschte Richtung vorzugeben.
     * (sonst geht z.B. der U-Shape nicht). Und der Winkel wird von der <p1> Strecke aus gesehen.
     * 25.7.17: Das ist eigentlich total laienhaft (oder?). Und anfällig wenn einer Vektoren (0,0) ist oder wird. Darum
     * mal deprecated. Wobei die Problematik der 0 Vektoren natürlich nicht wegzukriegen ist. Ach, lassen wirs mal.
     *
     * @param center
     * @param p1
     * @param p2
     * @return
     */
    public static Degree angle(Vector2 center, Vector2 p1, Vector2 p2, boolean ccw) {
        Degree w1 = center.angle(p1);
        Degree w2 = center.angle(p2);
        Degree r;
        if (ccw) {
            if (w2.getDegree() < w1.getDegree())
                w2 = w2.add(new Degree(360));
            r = w2.subtract(w1);
        } else {
            if (w1.getDegree() < w2.getDegree())
                w1 = w1.add(new Degree(360));
            r = w1.subtract(w2);
        }
        if (MathUtil2.areEqual((double) r.getDegree(), 180f) && !ccw)
            r = new Degree(-180);
        return r;
    }

    public static double getAngleBetween(Vector2 p1, Vector2 p2) {
        double dot = getDotProduct(p1.normalize(), p2.normalize());
        // 9.2.17: Offenbar kann es zu Rundungsfehlern kommen. Dann scheitert speter der acos mit NaN. 
        if (dot < -1) {
            dot = -1;
        }
        if (dot > 1) {
            dot = 1;
        }
        return (double) Math.acos(dot);
    }

    /**
     * Return the angle needed to rotate p1 CCW to p2.
     * From https://math.stackexchange.com/questions/74307/two-2d-vector-angle-clockwise-predicate
     */
    public static double getRotationAngleBetween(Vector2 p1, Vector2 p2) {
        double angle = getAngleBetween(p1, p2);
        double c = p1.x * p2.y - p1.y * p2.x;
        if (c > 0) {
            return angle;
        }
        return MathUtil2.PI2 - angle;
    }

    /**
     * Den Punkt um ein Center rotieren.
     * 14.7.17: Mal deprecated wegen center. Das ist irgendwie nicht konsistent. Sonst sollte Methode etwas anders heissen.
     *
     * @return
     */
    @Deprecated
    public Vector2 rotate(Vector2 center, Degree angle) {
        double s = (double) Math.sin(angle.toRad());
        double c = (double) Math.cos(angle.toRad());

        double rx = x - center.x;
        double ry = y - center.y;

        double xnew = rx * c - ry * s;
        double ynew = rx * s + ry * c;

        return new Vector2(xnew + center.x, ynew + center.y);
    }

    public Vector2 rotate(Degree angle) {
        double s = (double) Math.sin(angle.toRad());
        double c = (double) Math.cos(angle.toRad());

        double rx = x;
        double ry = y;

        double xnew = rx * c - ry * s;
        double ynew = rx * s + ry * c;

        return new Vector2(xnew, ynew);
    }

    @Override
    public String toString() {
        return "x=" + x + ",y=" + y;
    }

    /**
     * bewusst ohne Toleranz. Wenn zwei halt nur ein bischen unterschiedlich sind, sind sie halt nicht equals. (fuer DuplicateVertex)
     * Darum auch nicht als Override equals. Wer weiss, was das fuer Nebenwirkungen hat.
     * 21.3.17: Doch besser mit epsilon. Ist sauberer.
     *
     * @param v
     * @return
     */
    public boolean equalsVector2(Vector2 v) {
        if (!MathUtil2.areEqual(v.getX(), getX())) {
            return false;
        }
        if (!MathUtil2.areEqual(v.getY(), getY())) {
            return false;
        }
        return true;
    }


    /**
     * Das Skalarprodukt (dot product, inner product) von zwei Vektoren
     * Liefert den Cosinus des Winkels, aber nur bei normalisierten Vektoren.
     * Wenn sie nicht normalisiert sind, ist wohl nur das Vorzeichen des dot product aussagekräftig.
     * <p>
     * Liefert zwischen 1 (bei parallelen Vektoren) und -1 (bei entgegengesetzten Vektoren)
     *
     * @return
     */
    public static double getDotProduct(Vector2 v1, Vector2 v2) {
        return v1.getX() * v2.getX() + v1.getY() * v2.getY();
    }

    /*gibt es nicht?public static Vector2 getCrossProduct(Vector2 a, Vector2 b) {
        double x1 = (double) a.getY() * (double) b.getZ();
        double x2 = (double) a.getZ() * (double) b.getY();
        double y = (double) a.getZ() * (double) b.getX() - (double) a.getX() * (double) b.getZ();

        return  new Vector2( (x1 - x2),                y);
    }*/

    public Vector2 normalize() {
        double len = length();
        if (Math.abs(len) < 0.0000001) {
            return clone();
        }
        return divideScalar(len);
    }

    public Vector2 divideScalar(double scalar) {
        double invScalar = 1.0f / scalar;
        return new Vector2((double) (getX() * invScalar), (double) (getY() * invScalar));
    }

    public Vector2 negate() {
        return new Vector2(-getX(), -getY());
    }

    public static Vector2 buildFromVector3(Vector3 v3) {
        return new Vector2(v3.getX(), v3.getY());
    }

    public Vector2 rightNormal() {
        return new Vector2(y, -x).normalize();
    }

    /**
     * 31.5.21: Jetzt mal so als Standard: translate ist selbstaendernd.
     */
    public void translateX(int t) {
        x += t;
    }

    public void translateY(int t) {
        y += t;
    }

    public Vector2 addX(double t) {
        return new Vector2(x + t, y);
    }

    public Vector2 addY(double t) {
        return new Vector2(x, y + t);
    }

    /**
     * like in math, 0 degree point right (+x).
     * From https://stackoverflow.com/questions/21483999/using-atan2-to-find-angle-between-two-vectors
     */
    public Degree angle() {
        // just to be sure
        Vector2 v = new Vector2(x, y).normalize();
        double angle = Math.atan2(y, x) - Math.atan2(0, 1);
        if (angle < 0) {
            angle += 2 * Math.PI;
        }
        return Degree.buildFromRadians(angle);
    }
}