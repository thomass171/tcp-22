package de.yard.threed.core.geometry;


import de.yard.threed.core.Vector2;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.geometry.UvMap1;

/**
 * UV-Mapping von einen Texturausschnitt auf Vertices. Ist abhaenig vom proportionalen Verhaeltnis der gemappten
 * Vertices zu zwei Referenzpunkten.
 * <p/>
 * Ist so, als w�rde man den Ausschnitt mit zwei Stecknadeln (die zwei Referenzpunkte) auf das Mesh fixieren.
 * Damit ist es nat�rlich nur f�r bestimmte Meshes geeignet, z.B. f�r die bei ShapeGeometry entstehenden.
 * Ob aber wirklich f�r alle, ist unklar.
 * <p/>
 * Beziet sich immer auf eine einzelne Mappingarea des Meshes, nicht auf das ganze Mesh.
 * <p/>
 * Muesste vielleicht eher LinearMap heissen.
 * 24.11.2015: y (in den beiden refs) läuft hier von oben nach unten.
 * <p>
 * <p/>
 * Date: 02.09.14
 */
public class ProportionalUvMap extends UvMap1 {
    Vector2 ref1, ref2;
    //private boolean leftrotated = false;
    public static final int ROTATION_LEFT = 1;
    public static final int ROTATION_RIGHT = 2;
    public static final int ROTATION_RIGHT_AND_ROTATE_Y = 3;

    private int rotation = 0;

    /**
     * Der Default. ref1 links unten, ref2 rechts oben
     */
    public ProportionalUvMap() {
        this(new Vector2(0, 0), new Vector2(1, 1));
    }

    /**
     * Verwendet, um einen Texturanteil zu mappen z.B. eine Texturhaelfte
     *
     * @param ref1
     * @param ref2
     */
    public ProportionalUvMap(Vector2 ref1, Vector2 ref2) {
        this(ref1, ref2, 0);
    }

    /**
     * Constraints:
     * ref1.x < ref2.x
     * ref1.y < ref2.y
     *
     * @param ref1
     * @param ref2
     * @param rotation
     */
    public ProportionalUvMap(Vector2 ref1, Vector2 ref2, int rotation) {
        if (ref1.x >= ref2.x) {
            throw new RuntimeException("invalid ref.x:" + ref1.x + " " + ref2.x);
        }
        if (ref1.y >= ref2.y) {
            throw new RuntimeException("invalid ref.y");
        }
        this.ref1 = ref1;
        this.ref2 = ref2;
        this.rotation = rotation;
    }

    public ProportionalUvMap(Dimension texturesize, Rectangle clip) {
        this(new Vector2((float) clip.x1 / (float) texturesize.width, (float) clip.y1 / (float) texturesize.height),
                new Vector2((float) clip.x2 / (float) texturesize.width, (float) clip.y2 / (float) texturesize.height));
    }

    /**
     * Liefert das UV Mapping fuer den Punkt p abh�ngig von seiner Lage zu den Referenzpunkten
     *
     * @param p
     * @return
     */
    @Override
    public Vector2 getUvFromNativeUv(Vector2 p) {
        double xdiff = ref2.x - ref1.x;
        double ydiff = ref2.y - ref1.y;
        double x = ref1.x + xdiff * p.getX();
        double y = ref1.y + ydiff * p.getY();
        Vector2 st;
        switch (rotation) {
            case ROTATION_LEFT:
                //12.10.15 st = new Vector2(1 - y, x);
                st = new Vector2(y, 1 - x);
                break;
            case ROTATION_RIGHT:
                //st = new Vector2(y, 1-x);
                st = new Vector2(y, 1 - x);
                st = new Vector2(1 - y, x);
                //27.2.17: Skizze 14
                st = new Vector2(ref1.x + xdiff - (xdiff * p.getY()), ref1.y + ydiff * p.getX());
                break;
            case ROTATION_RIGHT_AND_ROTATE_Y:
                //13.11.15: Unsicher, ob das so stimmt. Ist jetzt aber mit Photoseite getestet.
                st = new Vector2(1 - y, 1 - x);
                st = new Vector2(1 - y, x);
                break;
            default:
                //TODO 12.10.15: Das duerfte nicht richtig sein. Testen.
                //24.11.2015: Mit der Radiofront stimmt es zumindest.
                st = new Vector2(x, y);
                break;
        }
        return st;
    }

    /**
     * For fontmaps, iconsets, etc. eg "Iconset-LightBlue.png".
     * Logical 'y' counts from top, while 'v' in 'uv' counts from bottom.
     * 22.1.25: 'y' from top is the historical default, but from bottom is more intuitive, so have it optional now.
     */
    public static ProportionalUvMap buildForGridElement(int cnt, int x, int y, boolean yFromTop) {
        float fcnt = cnt;
        float fy = y;
        ProportionalUvMap uvmap;
        if (yFromTop) {
            // the way it always was
            uvmap = new ProportionalUvMap(
                    new Vector2(((float) x / cnt), ((fcnt - fy - 1) / fcnt)),
                    new Vector2((((float) x + 1) / cnt), ((fcnt - fy) / fcnt)));
        } else {
            // the more intuitive way
            uvmap = new ProportionalUvMap(
                    new Vector2(((float) x / cnt), fy / fcnt),
                    new Vector2((((float) x + 1) / cnt), (fy + 1) / fcnt));
        }
        return uvmap;
    }
}
