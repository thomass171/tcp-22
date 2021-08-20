package de.yard.threed.engine;

import de.yard.threed.core.Util;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.engine.platform.common.Face;
import de.yard.threed.core.platform.Log;

import java.util.List;

/**
 * Eine Oberfläche aus n Tapes mit jeweils m Segmenten. Jedes Band hat die gleiche Anzahl Segmente.
 * Die Topologie ist damit immer einheitlich.
 * Die Abstaende sind aber nicht unbedingt gleich (z.B. Kugel).
 * Zumindest nicht die der Segmente. Wir gehen aber erstmal davon aus, dass die jeweiligen Segmente in jedem
 * Tape gleich lang sind.
 * <p/>
 * Date: 02.09.14
 */
public class GridSurface extends Surface {
    Log logger = Platform.getInstance().getLog(GridSurface.class);
    public int tapes;
    public List<Double> segmentlength;
    boolean endOnEdge;
    // Zeilenweise aufgebaut, nicht mehr nach Tapes
    //Face[][] faces;
    //HashMap<Integer, Vector2> vsegmentsize = new HashMap<Integer, Vector2>();
    double totallength;

    public GridSurface(int tapes, List<Double> segmentlength) {
        this(tapes, segmentlength, false);
    }

    public GridSurface(int tapes, List<Double> segmentlength, boolean endOnEdge) {
        this.tapes = tapes;
        this.segmentlength = segmentlength;
        this.endOnEdge = endOnEdge;
        totallength = getDistance(segmentlength.size());
        if (ShapeGeometry.debug) {
            logger.debug(Util.format("Building GridSurface: tapes=%d, totallength=%f, length=%s", new Object[]{tapes, totallength, segmentlength}));
        }
    }




    /**
     * Liefert die relative bzw. natürliche UV Position eines Vertex auf seiner "Surface".
     * Der Wert (jeweils x und y) liegt zwischen 0 (ganz links/unten) bis 1 (ganz rechts/oben).
     * <p/>
     * Wenn die Surface nicht rechteckig ist (z.B. eine Kugeloberfl�che), bekommt das mittlere Vertex damit
     * immer die x-Position 0.5 egal wie schmal es wird, solange die Anzahl Vertices "pro Zeile" gleich bleibt.
     * <p/>
     * public zum Testen
     * 02.12.16: segindex zaehlt ab 1? Nee, schon ab 0. Es wird aber auch mit quaisi +1 aufgerufen beim Ende des Segments.
     * @return
     */
    public Vector2 calcVertexLocation(int tape, int segindex) {
        if (ShapeGeometry.debug) {
            logger.debug(Util.format("calcVertexLocation at tape %d segindex %d", tape, segindex));
        }
        if (segindex > segmentlength.size()) {
            throw new RuntimeException("invalid segindex " + segindex);
        }
        float x = (float) tape / (float) tapes;
        /*int i = surfaceindex;
        while (i > 0) {
            i--;
            segindex -= sg.segmentsize[i];
        }  */
        //11.11.14 float y = (float) segindex / (float) /*segmentspertape*/segmentlength.size();
        // y=0 ist unten, y=1 ist oben
        double y = 1 - getDistance(segindex) / totallength;
        return new Vector2(x, y);
    }

    private double getDistance(int index) {
        double d = 0;

        for (int i = 0; i < index; i++)
            d += (double)segmentlength.get(i);
        return d;
    }

    public Face getFace(int tape, int segment) {
        //logger.debug(Util.format("getUvForFace for faceid %d (tape %d, segment %d, sts.length=%d)",faceid,tape,segment,sts.length));
        //Vector2[] ret = new Vector2[4];
        return getFaces().get(tape * /*segmentspertape*/segmentlength.size() + segment);

    }
}
