package de.yard.threed.engine.imaging;

import de.yard.threed.core.Point;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;

import de.yard.threed.core.Color;
import de.yard.threed.core.ImageData;

import java.util.List;

/**
 * Zur besseren Kapselung.
 * <p>
 * Created by thomass on 20.03.17.
 */
public class NormalMap {
    public ImageData image;
    // Senkrechte Normale
    public static Vector3 defaultnormal = new Vector3(0, 0, 1);


    public NormalMap(int width, int height) {
        //image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image = new ImageData(width, height);
    }

    public Vector3 getNormalFromMap(Point p) {
        return getNormalFromMap(p.getX(), p.getY());
    }

    public Vector3 getNormalFromMap(int x, int y) {
        int argb = image.getColor(x, y).getARGB();//image.getRGB(x, y);
        float nx = ((float) ((argb >> 16) & 0xFF)) / 127f - 1;
        float ny = ((float) ((argb >> 8) & 0xFF)) / 127f - 1;
        float nz = ((float) ((argb >> 0) & 0xFF)) / 127f - 1;
        Vector3 v = new Vector3(nx, ny, nz);
        return v;
    }

    /**
     * Hier ist das beschrieben:
     * https://en.wikipedia.org/wiki/Normal_mapping
     *
     * @param v
     * @return
     */
    public static int vector2color(Vector3 v) {
        //erstmal nicht normalisieren, um Fehler erkenn Quatsch.
        //27.7.16: Im Wikipediabeispiel ist er aber nicht normalisiert, da ist der oben rechts (1,1,0) isType mapped to (255,255,128).
        //v = MathUtil2.normalize(v);
        int r = (int)Math.round((v.getX() + 1f) * 127);
        int g = (int)Math.round((v.getY() + 1f) * 127);
        int b = (int)Math.round((v.getZ() + 1f) * 127);
        // Der Alphawert dürfte keine Rolle spielen. Doch, tut er für die PNG Erzeugung.
        int argb = (0xFF << 24) + (r << 16) + (g << 8) + b;
        return argb;
    }

    /**
     * Um den Punkt (cx,cy) drückt sich eine Halbkugel nach oben.
     * TODO so ganz stimmt das wohl noch nicht.
     *
     * @return
     */
    public static NormalMap buildSampleNormalmap(int width, int height, final int cx, final int cy, final int radius) {
        final Vector2 center = new Vector2(cx, cy);
        //BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        NormalMap img = new NormalMap(width, height);

        ImageHelper.loop(img.image, width, height, (/*BufferedImage*/ImageData image, int x, int y) -> {
            Vector2 p = new Vector2(x, y);
            int argb = vector2color(defaultnormal);
            double distance = p.distance(center);
            if (distance < radius) {
                int dx = Math.abs(x - cx);
                int dy = Math.abs(y - cy);
                dx = x - cx;
                dy = y - cy;
                float xangle;
                float yangle;
                //  Vector3 nv = platform.buildVector3(Math.sin(xangle),Math.sin(yangle));
                Vector3 nv = new Vector3(dx, dy, (float) Math.sqrt(radius * radius - distance * distance));
                //Vector3 nv = buildVector(dx, dy, radius, false);
                argb = vector2color(nv);

            }
            image.setColor(x, y, new Color(argb));

        });
        return img;

    }

    /**
     * Normalmap mit "runden" Aussenkanten für z.B. einen Cube.
     * <p/>
     * Ein Radius von 0 führt zu einer Map ohne besondere Normale. Radius 1 zu einem Pixel mit 45 Grad Normale. Die 45 Grad
     * aussen sind wichtig bei Cubes, bei denen ja die anstossende Fläche auch 45 Grad hat.
     * <p/>
     * Radius 1 zu 45/0 bzw dx=1/0
     * Radius 2 zu 45/22.5/0 bzw dx=1/0.5/0
     * Radius 3 zu 45/30/15/0 bzw dx=1/0.66/0.33/0
     * Radius 4 zu 45/?/22.5/?/0 bzw dx=1/0.75/0.5/0.25/0
     * <p>
     * Skizze 23
     * <p>
     * Ob das wirklich "richtig" ist, ist aber fraglich. 17.3.17: Unten war falsch. Jetzt nach Überprüfung in Gimp,
     * Vergleich mit Wikipedia Beispiel und nachrechnen scheint
     * es aber richtig zu sein.
     * <p/>
     * 28.7.2016
     */
    public static NormalMap buildEdgeNormalmap(final int width, final int height, final int radius) {
        //BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        NormalMap img = new NormalMap(width, height);

        ImageHelper.loop(img.image, width, height, (/*BufferedImage*/ImageData image, int x, int y) -> {
                    Vector2 p = new Vector2(x, y);
                    int argb = vector2color(defaultnormal);

                    float dx = 0;
                    float dy = 0;

                    if (radius != 0) {
                        dx = getDelta(width, x, radius);
                        // 27.7.16: Ob das Vorzeichen getauscht werden? Sieht beides nicht richtig aus.
                            /*if (y < radius) {
                                dy = (float) (radius - y) / radius;
                            }
                            if (y >= height - radius) {
                                dy = +(float) (radius - (height - y)+1) / radius;
                            }*/
                        dy = getDelta(height, y, radius);
                    }

                    //  Vector3 nv = platform.buildVector3(Math.sin(xangle),Math.sin(yangle));
                    Vector3 nv = new Vector3(dx, dy, 1);
                    //17.3.17: Unklar ob normalisiert sein sollte. Schaden dürfte es aber nicht. Wikipedia Sample ist auch normalisiert.
                    nv = nv.normalize();
                    //System.out.println(x + ":" + nv.dump(" "));
                    argb = vector2color(nv);
                    //BufferedImage hat y0 oben.
                    image.setColor(x, height - y - 1, new Color(argb));

                }
        );
        return img;
    }

    //bloede Kruecke wegen nicht final
    static List<Integer> currentxpos;


    /**
     * Ein Radius von 0 führt zu einer Map ohne besondere Normale. Radius 1 zu einem Pixel mit 45 Grad Normale.
     * Die erste xpos Liste gilt für unter der ersten ypos. Die letzte xpos Liste ist fuer oberhalb des letzten ypos.
     * Links und rechts wird nicht gerundet, damit die Map tilebar bleibt/wird.
     * Erstmal obselet: verlagert in die Heightmap 
     * 20.3.17: deprecated weil ueber heightmap.
     */
    @Deprecated
    public static NormalMap buildWallNormalmap(int width, int height, final List<Integer> ypos, final List<List<Integer>> xpos, final int radius) {
        ///BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        NormalMap img = new NormalMap(width, height);

        ImageHelper.loop(img.image, width, height, (/*BufferedImage*/ImageData image, int x, int y) -> {
                    Vector2 p = new Vector2(x, y);
                    int argb = vector2color(defaultnormal);
                    int closestypos = Util.findClosestIntInSet(ypos, y);

                    int dx = 0;
                    int dy = 0;

                    if (Math.abs(y - closestypos) <= radius) {
                        dy = y - closestypos;
                    }
                    if (y == closestypos) {
                        int indexofy = ypos.indexOf(y);
                        if (indexofy < xpos.size()) {
                            currentxpos = xpos.get(ypos.indexOf(y));
                        } else {
                            //dann bin ich so weit unten dass es keine xpos mehr gibt
                            currentxpos = null;
                        }
                    }
                    if (currentxpos != null) {
                        int closestxpos = Util.findClosestIntInSet(currentxpos, x);
                        if (Math.abs(x - closestxpos) <= radius) {
                            dx = x - closestxpos;
                        }
                        if (dx != 0 || dy != 0) {
                            float xangle;
                            float yangle;
                            final Vector2 centeroffuge = new Vector2(closestxpos, closestypos);

                            double distance = p.distance(centeroffuge);

                            //  Vector3 nv = platform.buildVector3(Math.sin(xangle),Math.sin(yangle));
                            Vector3 nv = new Vector3(dx, dy, (float) Math.sqrt(radius * radius - distance * distance));
                            argb = vector2color(nv);
                        }
                    }

                    image.setColor(x, y, new Color(argb));
                }


        );
        return img;

    }


    public static float getDelta(int len, int x, int radius) {
        float delta = 0;
        if (x < radius) {
            delta = -(float) (radius - x) / radius;
        }
        if (x >= len - radius) {
            delta = (float) (radius - (len - x) + 1) / radius;
        }
        return delta;
    }


    /**
     * Die aeussersten Normalen stehen im 45 Grad Winkel, weil sie da ja an die anderen stossen, die auch 45 Grad haben.
     * Das unterscheidet diese Map von der WallMap.
     *
     * @return
     */
    public static NormalMap buildToyBlockNormalmap(int width, int height, final List<Integer> ypos, final List<List<Integer>> xpos, final int radius) {
        return null;
    }


    public static Vector3 buildVector(int dx, int dy, int radius, boolean lowered) {
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float dz = (float) Math.sqrt(radius * radius - distance * distance);
        Vector3 v = new Vector3(dx, dy, dz);
        return v;
    }
}
