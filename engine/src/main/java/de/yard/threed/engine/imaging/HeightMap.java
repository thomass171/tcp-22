package de.yard.threed.engine.imaging;

import de.yard.threed.core.Point;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;

import de.yard.threed.core.Color;
import de.yard.threed.core.ImageData;

import java.util.ArrayList;
import java.util.List;


/**
 * Um HeightMaps klar erkennen zu koennen.
 * Und auch um die entsprechende Logik unterzubringen.
 * Skizze 26.
 * Eine Hebung/Senkung darf "innen" nicht aus nur einem Pixel bestehen, denn dafür kann man ja keinen vernünftigen Normalvektor ermitteln.
 * 2 ist Minimumgröße.
 */
public class HeightMap {
    public ImageData image;
    public static int DEFAULTHEIGHT = 127;
    //bloede Kruecke wegen nicht final
    private List<Integer> currentxpos;
    public final static int FUGENTIEFE = 80;

    HeightMap(int width, int height) {
        //super(width, height, BufferedImage.TYPE_INT_ARGB);
        image = new ImageData(width, height);
    }

    /**
     * Eine Standrad Heightmap mit H
     *
     * @return
     */
    public static HeightMap buildDefaultHeightmap(int width, int height) {
        HeightMap image = new HeightMap(width, height);
        final int h = 127;


        ImageHelper.loop(image.image, width, height, (ImageData img, int x, int y) -> {
            int argb = height2color(h);
            img.setColor(x, y, new Color(argb));
        });

        /*ImageHelper.loop(image.image, width, height, new PixelHandler() {
            public void handlePixel(/*BufferedImage* /ImageData image, int x, int y) {
                int argb = height2color(h);
                image.setColor(x, y, new Color(argb));
            }
        });*/
        return image;
    }

    /**
     * Um den Punkt (cx,cy) drückt sich eine Halbkugel nach oben.
     *
     * @return
     */
    public void addSphere(final int cx, final int cy, final int radius, final boolean lower) {
        final Vector2 center = new Vector2(cx, cy);

        ImageHelper.loop(image, image.width, image.height, (/*BufferedImage*/ImageData img, int x, int y) -> {
                    Vector2 p = new Vector2(x, y);
                    int argb = NormalMap.vector2color(NormalMap.defaultnormal);
            double distance = p.distance(center);
                    if (distance < radius) {
                        int height = DEFAULTHEIGHT + (int) Math.round(Math.sqrt(radius * radius - distance * distance) * (float) DEFAULTHEIGHT / (float) radius);
                        if (lower) {
                            height = -height;
                        }
                        //logger.debug("distance=" + distance + ",height=" + height);
                        argb = height2color(height);
                        img.setColor(x, y, new Color(argb));
                    }
                }
        );
    }

    /**
     * Ein Radius von 0 führt zu einer Map ohne besondere Height.
     * Die Reihen oben und unten sind duenner als die innenliegenden, weil sie ja nur zu einem Stein gehören.
     * Die erste xpos Liste gilt für unter der ersten ypos. Die letzte xpos Liste ist fuer oberhalb des letzten ypos.
     * Links und rechts wird nicht gerundet, damit die Map tilebar bleibt/wird.
     * Die size ist die Ausdehnung der Fuge, eine Art Radius. size=1 führt zu 3 Pixel breiten Fugen, size 2 zu 5 Pixeln.
     */
    public void addWallGrid(final List<Integer> ypos, final List<List<Integer>> xpos, final int size) {

        ImageHelper.loop(image, image.width, image.height, (/*BufferedImage*/ImageData img, int x, int y) -> {
                    Vector2 p = new Vector2(x, y);
                    int closestypos = Util.findClosestIntInSet(ypos, y);

                    int dx = Integer.MAX_VALUE;
                    int dy = Integer.MAX_VALUE;

                    if (Math.abs(y - closestypos) < size) {
                        dy = y - closestypos;
                    }
                    if (y == closestypos) {
                        // genau auf einem ypos. Dann die darunterliegenden xpos setzen;ausser ganz unten.
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
                        if (Math.abs(x - closestxpos) < size) {
                            dx = x - closestxpos;
                        }
                    }
                    if (dx < Integer.MAX_VALUE || dy < Integer.MAX_VALUE) {
                        // Punkt liegt auf Fuge
                        //final Vector2 centeroffuge = new Vector2(closestxpos, closestypos);

                        //float distance = p.distance(centeroffuge);

                        int h = height2color(FUGENTIEFE);
                        img.setColor(x, y, new Color(h));
                    }


                }

        );
    }

    /**
     * Da der Wert in allen drei Elementen gleich sein sollte, wird er irgendwo raus genommen.
     *
     * @param x
     * @param y
     * @return
     */
    public int getHeightFromMap(int x, int y) {
        return image.getColor(x, y).getARGB() & 0xFF;
    }

    public int getHeightFromMap(Point p) {
        return image.getColor(p.getX(), p.getY()).getARGB() & 0xFF;
    }

    /**
     * Aus der Heightmap eine Normalmap erzeugen.
     * <p>
     * passt aber evtl. noch nicht ganz richtig (wegen + oder - und so). 23.3.17: mde 2 ist aber ok.
     * Skizze 26
     * Mit <factor> [0,1] kann man die Normale abschaechen.
     *
     * @return
     */
    public NormalMap buildNormalMap(float factor) {
        //final Platform platform = ((Platform)Platform.getInstance());
        NormalMap map = new NormalMap(getWidth(), getHeight());
        final int mode = 2;

        ImageHelper.loop(map.image, getWidth(), getHeight(), (/*BufferedImage*/ImageData img, int x, int y) -> {
                    Vector3 n;
                    float nx = 0, ny = 0, nz = 1.0f;
                    switch (mode) {
                        case 1:
                            //Algorithmus ist aus http://www.gamedev.net/topic/475213-generate-normal-map-from-heightmap-algorithm/
                            if (x == 0 || y == 0 || x == getWidth() - 1 || y == getHeight() - 1) {
                                nx = 0;
                                ny = 0;
                            } else {
                                float spacing = 4;
                                // n[0]=(basemap->get(x-1,z) - basemap->get(x+1,z)) / (spacing/4.0f);
                                // n[2]=(basemap->get(x,z-1) - basemap->get(x,z+1)) / (spacing/4.0f);
                                nx = (getHeightFromMap(x - 1, y) - getHeightFromMap(x + 1, y)) / (spacing / 4.0f);
                                ny = (getHeightFromMap(x, y - 1) - getHeightFromMap(x, y + 1)) / (spacing / 4.0f);
                            }
                            break;
                        case 2:
                            // zugeschnitten auf meine HeightMaps mit nur zwei Höhen (SKizze 26).
                            if (x == 0) {
                                nx = getGradient(getHeightFromMap(x, y), getHeightFromMap(x, y), getHeightFromMap(x + 1, y));
                            } else if (x == getWidth() - 1) {
                                nx = getGradient(getHeightFromMap(x - 1, y), getHeightFromMap(x, y), getHeightFromMap(x, y));
                            } else {
                                nx = getGradient(getHeightFromMap(x - 1, y), getHeightFromMap(x, y), getHeightFromMap(x + 1, y));
                            }
                            if (y == 0) {
                                ny = getGradient(getHeightFromMap(x, y + 1), getHeightFromMap(x, y), getHeightFromMap(x, y));
                            } else if (y == getHeight() - 1) {
                                ny = getGradient(getHeightFromMap(x, y), getHeightFromMap(x, y), getHeightFromMap(x, y - 1));
                            } else {
                                ny = getGradient(getHeightFromMap(x, y + 1), getHeightFromMap(x, y), getHeightFromMap(x, y - 1));
                            }
                            nx *= factor;
                            ny *= factor;
                            break;
                    }

                    n = new Vector3(nx, ny, nz);
                    // 20.3.17: normalisieren duerfte nicht falsch sein.
                    // Naja, Unity hat damit ein Problem, wahrscheinlich aufgrund seines internen Formats. Darum lass ich es.
                    //In JME sieht Wall richtig gut aus, auch ohne normalize.
                    //n = n.normalize();
                    int argb = NormalMap.vector2color(n);
                    img.setColor(x, y, new Color(argb));

                }
        );
        return map;
    }

    /**
     * @param heightleft
     * @param heightmiddle
     * @param heightright
     * @return
     */
    private float getGradient(int heightleft, int heightmiddle, int heightright) {
        if (heightleft == heightmiddle) {
            if (heightmiddle == heightright) {
                return 0;
            }
            if (heightmiddle < heightright) {
                return -1;
            }
            return 1;
        }
        if (heightleft > heightmiddle) {
            return 1;
        }
        return -1;

    }
    /*public void engraveText(int x, int y, String text){
        this.dr
    }*/

    /**
     * quasi zwei Fugenreihen erstellen (die Sterne sind um den Formatierer zu blockieren):
     * ---------
     * *|**|
     * ---------
     * ****|**|
     * ---------
     */
    public static HeightMap buildSampleWallGrid() {
        int radius = 2;
        int width = 512;
        int height = 512;
        List<Integer> ypos = new ArrayList<Integer>();
        ypos.add(radius - 2);
        ypos.add(90);
        ypos.add(height - radius + 1);
        List<List<Integer>> xpos = new ArrayList<List<Integer>>();
        List<Integer> cxpos = new ArrayList<Integer>();
        cxpos.add(30);
        cxpos.add(200);
        xpos.add(cxpos);
        cxpos = new ArrayList<Integer>();
        cxpos.add(200);
        cxpos.add(300);
        xpos.add(cxpos);

        HeightMap hm = buildDefaultHeightmap(width, height);
        hm.addWallGrid(ypos, xpos, radius);
        return hm;
    }

    /**
     * height im Bereich von 0-255
     *
     * @param height
     * @return
     */
    public static int height2color(int height) {
        int r = height;
        int g = height;
        int b = height;
        // Der Alphawert dürfte keine Rolle spielen. Doch, tut er für die PNG Erzeugung.
        int argb = (0xFF << 24) + (r << 16) + (g << 8) + b;
        return argb;
    }

    public int getWidth() {
        return image.width;
    }

    public int getHeight() {
        return image.height;
    }
}
