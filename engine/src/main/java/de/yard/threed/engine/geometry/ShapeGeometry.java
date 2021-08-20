package de.yard.threed.engine.geometry;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.CircleExtruder;
import de.yard.threed.engine.CustomGeometry;
import de.yard.threed.core.Degree;
import de.yard.threed.engine.Extruder;
import de.yard.threed.engine.GridSurface;
import de.yard.threed.engine.Path;
import de.yard.threed.engine.PathExtruder;
import de.yard.threed.engine.SegmentedPath;
import de.yard.threed.engine.Shape;
import de.yard.threed.engine.ShapeFactory;
import de.yard.threed.engine.Surface;
import de.yard.threed.core.Util;
import de.yard.threed.engine.UvMap1;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.platform.common.Face;
import de.yard.threed.engine.platform.common.Face3;
import de.yard.threed.engine.platform.common.FaceN;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.FaceList;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.engine.platform.common.SmartArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * Zusammengesetzt aus einzelnen Tapes.
 * Wo Kanten liegen, ist ueber den Shape, die Linie der Extrusion und die Property open/close klar definiert.
 * <p/>
 * Durch den Aufbau aus Tapes mit Segmenten besteht die Geometrie immer aus n*m Vertices. Dies gilt auch, wenn Vertices an
 * der selben Stelle liegen, z.B. an den Polen von Kugeln. n*m ist wichtig, um relativ einfach ein UV Mapping machen
 * zu koennen.
 * <p/>
 * 25.05.2015: Meine Shape(Geometriy)s können mit denen von ThreeJs nicht mithalten. Was die alles können: Holes, Extrusion
 * und was die alles dabei machen: Viele schwer verständliche Dinge. Und das ist auch genau der Grund, warum ich meine weiter
 * verfolge. Meine sind zwar nicht so leistungsfähig, aber verständlich. Und bei einer Umstellung auf Unity vielleicht
 * auch einfacher weiterverwendbar.
 * 17.07.2015: Durch JME kommt noch dazu, dass die auch nicht so viel können (Holes, Triangulation, Extrusion),
 * und ich dann auch wieder darauf angwiesen bin,
 * selber Geometrien zu erzeugem.
 * Eigene Superklasse um zur NativeGeometry abzugrenten, was ja was was ganz anderes ist, zumindest im Moment).
 * 22.11.16: Es gibt auch noch die Klasse Panelgeometry, die auch mit Holes klarkommt.
 * <p/>
 * Date: 25.04.14
 */
public class ShapeGeometry extends CustomGeometry {
    Log logger = Platform.getInstance().getLog(ShapeGeometry.class);
    //public List<Tape> tapes = new ArrayList<Tape>();
    private int tapecnt;
    // Enthaelt bei geschlossen auch den schliessenden
    //Hat seit Surfaces doch keine Bewandnis mehr
    //public int segmentspertape;
    Shape shape;
    //22.8.15 int firstbackface = -1;
    //22.8.15 int firstfrontface = -1;
    // Eine Map aus der hervorgeht, welche Surfaces es gibt und ueber welche Segmente sie sich erstrecken.
    // Das schliessende Element sowie front/back zaehlen da auch mit. Ein W�rfel hat z.B. 6 surfaces.
    // Eine Extrusuion mit mehreren Pfadsegmenten hat noch mehr Surfaces. Die Reihenfoge in der List
    // ist: Surfaces pro Extrusonssegment und dann front back.
    // Mapping jetzt pro Pfadsegment. Front und Back haben jeweils ein eigenes Segment mit Index -1 und -2
    // 29.4.16: Das ist eine Ebene zu viel und passt so nicht zum Fac3List Konzept der Platform
    // 19.12.16: Ist da wirklich was zu viel? Die oberste Listenebene bezieht sich auf evtl. mehrere
    // Surfaces durch mehrere Segemente auf dem Extrusionspfad. Das ist eher selten der Fall. 
    // 21.3.17: Als HashMap ist doof wegen unzuverlässiger key order,z.B. Java8 (vor allem bei Tests). Darum eigene Klasse.
    SurfaceList/*HashMap<Integer, List<Surface>>*/ surfaces = new SurfaceList();//HashMap<Integer, List<Surface>>();
    public static final boolean debug = false;
    static public boolean closegeo = false;

    /**
     * Rotation eines Shapes.
     * <p/>
     * Die y-Koordinate des Shapes wird unveraendert uebernommen.
     * 10.4.15: Deprecated. Wird jetzt auch per Extrusion ueber einen Kreisbogen gemacht.
     * @param shape
     * @param tapes
     * @param degreesperstep
     */
    /*public ShapeGeometry(Shape shape, int tapes, Degree degreesperstep) {
        //super(tapes + 1, shape.getPoints().size());
        this.shape = shape;
        float x, z;
        double angle = 0, anglestep = 360 / tapes;
        //Tape tape = null;
        // this.tapes = tapes;
        /*this.segmentspertape = shape.getPoints().size() - 1;
        if (shape.isClosed())
            this.segmentspertape++; * /
        //TODO Das mit der Surface ist hier (bei der Kugel) bestimmt noch nicht richtig
        calculateSurfaces(tapes);
        Surface surface = surfaces.get(0);
        for (int s = 0; s <= tapes; s++) {
            if (s > 0) {
                //tape = new Tape(s - 1);
                //tapes.add(tape);
            }
            for (int r = 0; r < shape.getPoints().size(); r++) {
                Vector2 point = shape.getPoints().get(r);
                x = (float) (point.x * Math.cos(Math.toRadians(angle)));
                z = (float) (point.x * Math.sin(Math.toRadians(angle)));
                super.addVertex(new Vertex(new Vector3(x, point.y, z), Color.GREEN));
                logger.debug(Util.format("Vertex %d at %f,%f,%f", vertices.size() - 1, x, point.y, z));

                // Im ersten Durchlauf kann man noch keine Faces bauen. Aber ab dem 2. Vertex in der zweiten "Spalte"
                if (s > 0 && r > 0) {
                    int a = vertices.size() - shape.getPoints().size() - 1;
                    int b = vertices.size() - shape.getPoints().size() - 2;
                    int c = vertices.size() - 2;
                    int d = vertices.size() - 1;
                    addFaces(surface, s - 1, r - 1, a, b, c, d);
                }
                //TODO surface befuellen
            }
            angle += anglestep;
        }
        tapecnt = tapes;
    }*/

    /**
     * Lineare Streckung.
     * <p/>
     * Die Streckung erfolgt entlang der z-Achse. Zentrierung auf 0.
     * <p/>
     * Wenn die UvMap null ist, wird ein Standard verwendet.
     *
     * @param width
     * @param tapes
     * @return
     */
    public ShapeGeometry(Shape shape, final double width, final int tapes, List<UvMap1> uvmap) {
        this(shape, new SimpleExtruder(width, tapes)/*width,*/, false, uvmap /*!= null ? Util.buildArrayList(uvmap) : null*/);
    }

    public ShapeGeometry(Shape shape, final double width, final int tapes, UvMap1 uvmap) {
        this(shape, width, tapes, uvmap != null ? new SmartArrayList<UvMap1>(uvmap) : null);
    }

    public ShapeGeometry(Shape shape, final double width, final int tapes) {
        this(shape, width, tapes, (UvMap1) null);
    }

    /**
     * Extrusion eines Shapes entlang eines Path.
     * Pro Pfadsegment entsteht eine Surface (bzw. mehrere, wenn es Shapeedges gibt).
     *
     * @param shape
     */
    public ShapeGeometry(Shape shape, final Path path/*, final float[] steps*/) {
        this(shape, new PathExtruder(path), true);
    }

    private ShapeGeometry(Shape shape, /*final float width,* / final int tapes,*/ Extruder pt, boolean closed) {
        this(shape, pt, closed, null);
    }

    public ShapeGeometry(Shape shape, final Path path, boolean closed, List<UvMap1> uvmap) {
        this(shape, new PathExtruder(path), closed, uvmap);
    }

    /**
     * Die uvmap ist eine Liste mit einer map für jede entstehende Surface pro Extrusionssegment. Das ist jetzt einfach mal so
     * festgelegt, dass die uvmaps bei der Extrusion wiederverwendet werden. Wird ja eh erstmal noch nicht verwendet.
     * 24.11.15: Das duerfte nicht so simpel sein, denn die Map enthält evtl. auch UVMaps fuer Front und Back.
     * <p>
     * 02.12.16: closed bezieht sich nicht auf den Shape, sondern das Verbinden des ersten Tapes mit dem letzten extrudiertem.
     * Das ist eigentlich nur bei Kreisextrusionen sinnvoll. Aber auch da ist der Sinn fraglich, weil dort eine Texturnaht liegt.
     * Die Vertices koennten zwar später dupliziert werden, das ist aber nicht intuitiv, weil da ja eigentlich keine Kante ist.
     * Mal ohne closed versuchen (closedgeo flag). Das scheint eingängiger, auch im Sinne von wickeln.
     *
     * @param shape
     * @param pt
     * @param closed
     * @param uvmap
     */
    private ShapeGeometry(Shape shape, /*final float width,* / final int tapes,*/ Extruder pt, boolean closed, List<UvMap1> uvmap) {
        //super/* Geometry geometry = new Geometry*/(1 + tapes, shape.getPoints().size());
        if (debug) {
            logger.debug(Util.format("Building geometry from shape with %d points", shape.getPoints().size()));
        }
        this.shape = shape;
        double x, z;//, segmentwidth = width / tapes;
        //Tape tape = null;
        //this.tapes = tapes;
        /*this.segmentspertape = shape.getPoints().size() - 1;
        if (shape.isClosed())
            this.segmentspertape++; */
        //float[] tapesteps = pt.getTapeSteps();
        calculateSurfaces(pt/*.getSegments(), tapesteps.length*/, closed && ShapeGeometry.closegeo);

        // Der erste Durchlauf für den ersten Shape, dabei entstehen noch keine Faces
        // Es erfolgt aber schon eine quasi Extrusion, um den Start entsprechend des Pfads zu haben.
        // 25.8.15: Der erste Punkt jetzt doch nicht aus den Tapesteps.
        for (int r = 0; r < shape.getPoints().size(); r++) {
            //Vector2 point = shape.getPoints().get(r);
            Vector3 point = pt.transformPoint(shape.getPoints().get(r), pt.getStart()/*tapesteps[0]*/);

            x = point.getX();
            // Zentrierung auf 0 und beginnend im positven z Bereich zum negativen hin
            //  muesste manchmal ja in den negativen (z.B. bei Würfeln)
            // Das wird auch ueber den Extruder gemacht.
            z = point.getZ();//width / 2 - s * segmentwidth;
            super.addVertex(new Vector3(x, point.getY(), z));
            int vindex = vertices.size() - 1;
            // Bei einem geschlossenen Shape kommt der erste Vertex auch zur letzten Surface
            if (shape.isClosed() && r == 0) {
                //29.9.14 addVertexToSurface(surfaces.size()-1, vindex, calcVertexLocation(s, segindex, surfaces.get(surfaces.size()-1).segments));
            }
            //30.9.14 addVertexToSurface(surfaceindex, vindex, calcVertexLocation(s, segindex, surfaces.get(surfaceindex).segments));
            if (ShapeGeometry.debug) {
                logger.debug(Util.format("Initial Vertex %d at %f,%f,%f ", new Object[]{vindex, x, point.getY(), z}));
            }
        }

        int surfacesegindex = 0;
        //Und jetzt die Segmente bzw. Tapes nach und nach an die erste bzw. vorherige Shapereihe hängen
        for (int segment = 0; segment < pt.getSegments(); segment++) {
            double[] tapesteps = pt.getTapeSteps(segment);
            if (ShapeGeometry.debug) {
                logger.debug(Util.format("Building segment %d with %d tapesteps", segment, tapesteps.length));
            }
            // der erste Step wurde schon in der oberen Schleife gemacht. 25.8.15: Seit geaendertem Start nicht mehr
            for (int tapestep = 0/*1*/; tapestep < tapesteps.length; tapestep++) {
                if (tapestep > 0) {
                    //tape = new Tape(s - 1);
                    //tapes.add(tape);
                }
                int segindex = 0;
                int segoffset = 0;
                // Hier mit den Surfaces wieder von vorne innerhalb des aktuellen Segments beginnen
                // 15.9.15: Nee, immer bei 0. Segmente sind doch in surfaces uebergeordnet
                int surfaceindex = 0;//surfacesegindex;
                if (ShapeGeometry.debug) {
                    logger.debug("surfaceindex = " + surfaceindex);
                }
                for (int r = 0; r < shape.getPoints().size(); r++, segindex++) {
                    //Vector2 point = shape.getPoints().get(r);
                    Vector3 point = pt.transformPoint(shape.getPoints().get(r), tapesteps[tapestep]);

                    super.addVertex(new Vector3(point.getX(), point.getY(), point.getZ()));
                    int vindex = vertices.size() - 1;
                    // Bei einem geschlossenen Shape kommt der erste Vertex auch zur letzten Surface
                    if (shape.isClosed() && r == 0) {
                        //29.9.14 addVertexToSurface(surfaces.size()-1, vindex, calcVertexLocation(s, segindex, surfaces.get(surfaces.size()-1).segments));
                    }
                    //30.9.14 addVertexToSurface(surfaceindex, vindex, calcVertexLocation(s, segindex, surfaces.get(surfaceindex).segments));
                    if (ShapeGeometry.debug) {
                        logger.debug(Util.format("Vertex %d at %f,%f,%f in surface %d", new Object[]{vindex, point.getX(), point.getY(), point.getZ(), surfaceindex}));
                    }
                    GridSurface gf = (GridSurface) surfaces.get(segment).get(surfaceindex);
                    // Und jetzt die Faces, normal nur eins, beim Schliessen auch zwei.
                    // Im ersten Durchlauf kann man noch keine Faces bauen. Aber ab dem 2. Vertex in der zweiten "Spalte"
                    if (r > 0) {
                        int a = vertices.size() - shape.getPoints().size() - 2;
                        int b = vertices.size() - shape.getPoints().size() - 1;
                        int c = vertices.size() - 1;
                        int d = vertices.size() - 2;
                        addGridFaces((GridSurface) surfaces.get(segment).get(surfaceindex), tapestep /*- 1*/, r - 1, a, b, c, d, r - 1, segoffset, (uvmap != null) ? uvmap.get(surfaceindex) : null/*gf.calcVertexLocation(tapestep,r-1-segoffset),
                                gf.calcVertexLocation(tapestep,r-segoffset),gf.calcVertexLocation(tapestep+1,r-segoffset),gf.calcVertexLocation(tapestep+1,r-1-segoffset)*/);
                    }
                    // Wenn eine Kante vorliegt (aber nicht am letzten Punkt) ...
                    boolean nextsurface = false;
                    if (shape.isEdge(r) && r < shape.getPoints().size() - 1) {
                        if (ShapeGeometry.debug) {
                            logger.debug("edge detected. switching surface");
                        }
                        nextsurface = true;
                    }
                    // ... oder der Shape geschlossen wird (und eine Ecke ist), auf die naechste Surface gehen
                    if (shape.isClosed() && r == shape.getPoints().size() - 1 && shape.isEdge(r)) {
                        if (ShapeGeometry.debug) {
                            logger.debug("shape closed. switching surface");
                        }
                        nextsurface = true;
                    }
                    if (nextsurface) {
                        // Aus Effizienzgruenden und weils unlogisch ist, wir der Vertex nicht doppelt angelegt. Fuer den VBO
                        // wird das spaeter gemacht. Aber zur naechsten Surface muss er dazu.
                        segoffset += gf.segmentlength.size();
                        surfaceindex++;
                        segindex = 0;
                        //29.9.14 addVertexToSurface(surfaceindex, vindex, calcVertexLocation(s, segindex, surfaces.get(surfaceindex).segments));
                    }
                    gf = (GridSurface) surfaces.get(segment).get(surfaceindex);
                    // Bei einem closed shape zum Schluss zur anderen Kante verschliessen. Aber auch erst ab dem zweiten Durchlauf
                    if (shape.isClosed() && r == shape.getPoints().size() - 1) {
                        int b = vertices.size() - 2 * shape.getPoints().size();
                        int a = vertices.size() - shape.getPoints().size() - 1;
                        int d = vertices.size() - 1;
                        int c = vertices.size() - shape.getPoints().size();
                        // Die schliessende Surface hat in y Richtung ja in jedem Fall nur ein Segment, darum ist keine segoffset erforderlich
                        addGridFaces((GridSurface) surfaces.get(segment).get(surfaceindex), tapestep /*- 1*/, r, a, b, c, d, 0, 0, (uvmap != null) ? uvmap.get(surfaceindex) : null);
                        /*addFaces(surfaces.get(segment).get(surfaceindex), tapestep, r, a, b, c, d, gf.calcVertexLocation(tapestep, 0),
                                gf.calcVertexLocation(tapestep, 1), gf.calcVertexLocation(tapestep + 1, 1), gf.calcVertexLocation(tapestep + 1, 0));*/
                    }
                }
                tapecnt++;
                //dumpSurfaces();
            }
            String tmp = "(";
            for (int i = 0; i < surfaces.size(); i++) {
                tmp += "(";
                for (int j = 0; j < surfaces.get(i).size(); j++) {
                    tmp += ((j > 0) ? "," : "") + surfaces.get(i).get(j).getFaces().size();
                }
                tmp += ")";
            }
            tmp += ")";

            if (ShapeGeometry.debug) {
                logger.debug(Util.format("Created %s vertices and %s faces ", new Object[]{vertices.size(), tmp}));
            }
            surfacesegindex += surfaces.get(segment).size();
        }
        // siehe closegeo
        if (closed && closegeo) {
            // Die Geometrie schliessen. Die Vertices sind ja schon alle da, es fehlt nur
            // noch ein Tape. Sonderfaelle wie Kanten zum Schluss sind hier noch nicht beruecksichtigt.
            // Es gehen erstmal nur so Dinge wie Kugeln (TODO)
            if (ShapeGeometry.debug) {
                logger.debug("Closing geometry = ");
            }
            // Erstmal nur ein Extrusionssegment.
            int segment = 0;
            // und auch keine Kanten
            int surfaceindex = 0;
            //int segoffset = 0;
            double[] tapesteps = pt.getTapeSteps(segment);
            int tapestep = tapesteps.length;
            for (int r = 0; r < shape.getPoints().size(); r++) {
                GridSurface gf = (GridSurface) surfaces.get(segment).get(surfaceindex);
                // Und jetzt die Faces, normal nur eins, beim Schliessen auch zwei.
                // Im ersten Durchlauf kann man noch keine Faces bauen. Aber ab dem 2. Vertex in der zweiten "Spalte"
                if (r > 0) {
                    int a = vertices.size() - shape.getPoints().size() + r - 1;
                    int b = vertices.size() - shape.getPoints().size() + r;
                    int c = r;
                    int d = r - 1;
                    addGridFaces(gf, tapestep, r, a, b, c, d, r - 1, 0, (uvmap != null) ? uvmap.get(surfaceindex) : null);
                }

            }
        }
    }

    private void dumpSurfaces() {
        for (int j = 0; j < surfaces.size(); j++) {
            for (int i = 0; i < surfaces.get(j).size(); i++) {
                logger.debug("" + j + ":Surface " + i + ": " + surfaces.get(j).get(i).getFaces().size() + " faces");
            }
        }
    }

    /*30.9.14 private void addVertexToSurface(int surfaceindex, int vindex, Vector2 loc) {
        logger.debug("Adding vertex " + vindex + " to surface " + surfaceindex + " with loc " + loc);
        surfaces.get(surfaceindex).addVertex(vindex, loc);

    }*/

    /*private void addVertex(int surfaceindex, Vertex vertex, Vector2 loc) {

    }*/


    /**
     * Ein Tape führt nicht zwangsläufig zu einer eigenständigen Surface. Z.B. bei Kugeln und Tubes.
     * Eine Kugel hat, unabhängig von den Tapes, genau eine Surface, weil es auch keine Shape Kanten gibt.
     * An den Grenzen der Extrusionssegmente entstehen auf jeden Fall neue Surfaces. 19.12.16: Was heisst das? Wahrscheinlich bezieht
     * sich das auf die Extrusion über einen Pfad, der aus mehreren Segmenten besteht. So bleibt es bei einer Kreisextrusion bei einer
     * Surface.
     */
    private void calculateSurfaces(Extruder pt/*int extrudesegments, int tapes*/, boolean geoclosed) {

        for (int j = 0; j < pt.getSegments(); j++) {
            surfaces.put(j, new ArrayList<Surface>());
            ArrayList<Double> segmentlength = new ArrayList<Double>();
            boolean endOnEdge = false;
            int tapes = pt.getTapeSteps(j).length;//25.8.15 -1;
            if (geoclosed) {
                tapes++;
            }
            if (ShapeGeometry.debug) {
                logger.debug("Calculating surfaces for " + tapes + " tapes in segment " + j + ". geoclosed=" + geoclosed);
            }
            // Wenn der Shape geschlossen ist, startet auch die erste Surface auf einer Kante. Da ist zwar auf
            // den ersten Blick verwirrend, aber dann sind die Surfcaes einheitlicher, zB. beim Cuboid
            // hat dann jede der 4 Surfaces zwei duplizierte Vertices
            // Aber das ist etwas doof. Darum werden jetzt die hinteren kopiert, d.h. eine Surface startet
            // immer auf Originalen
            // 02.12.16: Hier gibts doch ueberhaupt keine VertexDuplizierung mehr, oder?
            if (shape.isClosed()) {
                endOnEdge = true;
            }
            // Nur bis -1, weil es ja einen Shapepoint mehr als Segments gibt . Und weil noch nicht klar ist, was ist wenn
            // der letzte edged ist. 11.11.14: ?
            for (int i = 0; i < shape.getPoints().size() - 1; i++) {
                if (shape.isEdge(i)) {
                    hasedges = true;
                    //segmentsize[surfaceindex++] = segments;
                    surfaces.get(j).add(new GridSurface(tapes, segmentlength, true));
                    if (ShapeGeometry.debug) {
                        logger.debug(Util.format("New surface with %d segments at point %d", segmentlength.size(), i));
                    }
                    segmentlength = new ArrayList<Double>();
                    //startOnEdge = true;
                }
                segmentlength.add(shape.getPoints().get(i).distance(shape.getPoints().get(i + 1)));

            }
            if (shape.isClosed()) {
                if (shape.isEdge(shape.getPoints().size() - 1)) {
                    // letzter Punkt ist edge. Aktuelle Surface abscghliessen
                    surfaces.get(j).add(new GridSurface(tapes, segmentlength, true));
                    if (ShapeGeometry.debug) {
                        logger.debug(Util.format("New surface with %d segments for last shape", segmentlength.size()));
                    }
                    segmentlength = new ArrayList<Double>();
                    //endOnEdge = true;
                }
                segmentlength.add(shape.getPoints().get(0).distance(shape.getPoints().get(shape.getPoints().size() - 1)));

            }
            //segmentsize[surfaceindex] = segments;
            surfaces.get(j).add(new GridSurface(tapes, segmentlength, shape.isClosed()));
            if (ShapeGeometry.debug) {
                logger.debug(Util.format("New surface with %d segments", segmentlength.size()));
            }
            //if (shape.isClosed())
            //    surfaces.add(new Surface(tapes, 1));
        }
        String tmp = "(";
        for (int i = 0; i < surfaces.size(); i++) {
            tmp += ((i > 0) ? "," : "") + surfaces.get(i).size();
        }
        tmp += ")";
        if (ShapeGeometry.debug) {
            logger.debug("Initially created surfaces per path segment:" + tmp);
        }
    }

    /**
     * Der Einfachheithalber werden vom ersten Vertex an Face3 faecherartig CCW angelegt.
     * Duplizieren von Vertices ist hier noch nicht erforderlich.
     * Die UVs werden defaultmaessig (ohne dass eine UvMap uebergeben wird) so erzeugt, dass bei Betrachtung von vorne die Textur
     * die "richtige" Orientierung hat.(Vertex 0 hat UV(0,0)).
     * 24.11.15: Na, das ist aber doch Unsinn (gilt nur für sowas wie Würfel), siehe z.B. Radiofront. Die natürlichen UVs lassen sich
     * nur über min/max
     * Betrachtung ermitteln. Genau das macht ShapeSurface.
     * 11.6.16: Umgestellt auf FaceN
     */
    public void closeFront(UvMap1 uvmap) {
        if (ShapeGeometry.debug) {
            logger.debug("Closing front");
        }
        hasedges = true;
        ShapeSurface front = new ShapeSurface(shape, false);
        int facecnt = 0;
        boolean usefacen = true;
        if (usefacen) {
            front.addFaceN(shape.getPoints().size(), vertices, uvmap);
        } else {
            //int a = 0;
            // geschlossen oder nicht ist hier egal
            int lastindex = shape.getPoints().size() - 1;
            /*7.2.18 Native*/Vector3 v = vertices.get(0);
            Vector2 uv0 = new Vector2(0, 0);
            // front.addVertex(0, new Vector2(v.getX(), v.getY()));
            v = vertices.get(lastindex);
            //front.addVertex(lastindex, new Vector2(v.getX(), v.getY()));
            for (int i = lastindex; i > 1; i--) {
                v = vertices.get(i - 1);
                //  front.addVertex(i - 1, new Vector2(v.getX(), v.getY()));
                front.addFace3(0, i, i - 1, vertices, uvmap);
                facecnt++;
                // if (firstfrontface == -1)
                //   firstfrontface = index;
            }
        }
        //int surfacecnt = surfaces.size();
        surfaces.front= new ArrayList<Surface>();
        surfaces.front.add(front);
        if (ShapeGeometry.debug) {
            logger.debug(Util.format("front closed with 1 faceN in surface %d", facecnt, -1));

        }
    }

    /**
     * Der Einfachheithalber werden vom letztem Vertex des letzten Shape an Face3 faecherartig CCW angelegt.
     * Wie bei Front, ob das immer so tauglich ist, muss sich noch zeigen
     * Duplizieren von Vertices ist hier noch nicht erforderlich.
     * Die UVs werden defaultmaessig (ohne dass eine UvMap uebergeben wird) so erzeugt, dass bei
     * Betrachtung von hinten die Textur die "richtige" Orientierung hat.(Vertex links
     * unten - beim Wurfel die 7 - hat UV(0,0)). Das duerfte wie bei Front nicht ganz stimmen. Beim Wuerfel ja,
     * aber bei Radio aber nicht mehr (siehe Front).
     */
    public void closeBack(UvMap1 uvmap) {
        if (ShapeGeometry.debug) {
            logger.debug("Closing back");
        }
        hasedges = true;
        ShapeSurface back = new ShapeSurface(shape, true);
        /* int b = lastvertex + 0;
        int c = lastvertex + 1;
        int d = lastvertex + 2;
        int a = lastvertex + 3;*/
        int facecnt = 0;
        // geschlossen oder nicht ist hier egal
        int lastindex = getVertices().size() - 1;
        int startindex = lastindex - shape.getPoints().size() + 1;
        // 5.12.16: Wenn der shape nicht geschlossen ist, liegt der letzte Vertex genau wie der erste. Dann ein Triangle weniger, denn das wäre entartet ohne Normale.
        // Vorsicht, das ist nicht mit dem Cube zu testen.
        if (!shape.isClosed()) {
            lastindex--;
        }
        //Vector3 v = vertices.get(lastindex);
        //back.addVertex(lastindex, new Vector2(-v.getX(), v.getY()));
        //v = vertices.get(startindex);
        //back.addVertex(startindex, new Vector2(-v.getX(), v.getY()));
        for (int i = startindex; i < lastindex - 1; i++) {
            //if (firstbackface == -1)
            //  firstbackface = index;
            //v = vertices.get(i + 1);
            //  back.addVertex(i + 1, new Vector2(-v.getX(), v.getY()));
            int index = back.addFace3(lastindex, i, i + 1, vertices, uvmap);
            facecnt++;
        }
        //int surfacecnt = surfaces.size();
        surfaces.back = new ArrayList<Surface>();
        surfaces.back.add(back);
        if (ShapeGeometry.debug) {
            logger.debug(Util.format("back closed with %d faces in surface %d", facecnt, -2));
        }
    }

    private int addGridFaces(GridSurface surface, int tape, int segment, int a, int b, int c, int d, int seg, int segoffset, UvMap1 uvmap) {
        //Vector3 normal = buildNormal(a,b,c);
        Vector2 uv0 = surface.calcVertexLocation(tape, seg - segoffset);
        Vector2 uv1 = surface.calcVertexLocation(tape, seg + 1 - segoffset);
        Vector2 uv2 = surface.calcVertexLocation(tape + 1, seg + 1 - segoffset);
        Vector2 uv3 = surface.calcVertexLocation(tape + 1, seg - segoffset);

        if (uvmap != null) {
            return addFaces(surface, tape, segment, a, b, c, d,
                    uvmap.getUvFromNativeUv(uv0),
                    uvmap.getUvFromNativeUv(uv1),
                    uvmap.getUvFromNativeUv(uv2),
                    uvmap.getUvFromNativeUv(uv3));
        }
        return addFaces(surface, tape, segment, a, b, c, d, uv0, uv1, uv2, uv3);
    }

    /**
     * Den Normalenvektor für eine Face bauen. Zunächst mal ganz simpel senkrecht auf die angenommene Ebene.
     * Ausgangspunkt ist b (rechts unten/vorne)
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    public static Vector3 buildNormal(List<Vector3> vertices, int a, int b, int c) {
        Vector3 v1 = MathUtil2.subtract(vertices.get(a), vertices.get(b));
        Vector3 v2 = MathUtil2.subtract(vertices.get(c), vertices.get(b));
        return MathUtil2.getCrossProduct(v2, v1);
    }

    /**
     * Face(s) an den Vertices a,b,c,d anlegen.
     * 26.5.14: Mal mit Face4 versuchen.
     * Liefert den Index des ersten neuen Face.
     * 29.05.2015: Jetzt nur noch Face3, weil ThreeJS auch so arbeitet.
     * 19.08.2015: Das ist aber nicht relevant. Model Formate (AC,OBJ) kennen durchaus Polygonfaces. Also doch Face4.
     *
     * @param a
     * @param b
     * @param c
     * @param d
     */
    private int addFaces(Surface surface, int tape, int segment, int a, int b, int c, int d, Vector2 uv0, Vector2 uv1, Vector2 uv2, Vector2 uv3) {
        boolean useface4 = true;
        int index;
        if (useface4) {
            FaceN face = new FaceN(a, b, c, d, uv0, uv1, uv2, uv3);
            if (ShapeGeometry.debug) {
                logger.debug(Util.format("Face %d at a=%d,b=%d,c=%d,d=%d in tape %d, uv0=%f, uv1=%f, uv2=%f, uv3=%f", new Object[]{segment, a, b, c, d, tape, uv0, uv1, uv2, uv3}));
            }
            return addFace(surface,/*tape,*/ face);

        } else {
            index = addFace3(surface,/*tape,segment*/a, b, d, uv0, uv1, uv3);
            index = addFace3(surface,/*tape,segment*/c, d, b, uv2, uv3, uv1);
        }
        return index;
    }

    private int addFace3(Surface surface, /*int tape, int segment,*/ int a, int b, int c, Vector2 uv0, Vector2 uv1, Vector2 uv2) {
        Face3 face = new Face3(a, b, c, uv0, uv1, uv2);
        int index = addFace(surface,/*tape,*/ face);
        if (ShapeGeometry.debug) {
            logger.debug(Util.format("Face3  a=%d,b=%d,c=%d  at %d", new Object[]{a, b, c, index}));
        }
        return index;
    }

    /**
     * Liefert den Index des neuen Face in der Surface.
     *
     * @return
     */
    private int addFace(Surface surface, /*int tape,*/ Face face) {
        /*tape.*/
        surface.getFaces().add(face);
        return surface.getFaces().size() - 1;
    }

    /**
     * Center in 0,0,0
     */
    public static ShapeGeometry buildStandardSphere() {
        return buildSphere(6, 60, new Degree(360));
    }

    /**
     * Kugel mit "Nordpol" im positivem y und "Suedpol" im negativen y. UV (0,0) ist am Suedpol.
     * Die horizonzalsegments gelten für die roundness, d.h. je kleiner die roundness, umso kleiner
     * sind die einzelnen Winkelstuecke.
     * Radius ist 1, d.h. Durchmesser 2.
     * <p/>
     * Center in 0,0,0
     */
    public static ShapeGeometry buildSphere(int verticalsegments, int horizonzalsegments, Degree roundness) {
        // Bogen von oben nach unten CW, damit erster Vertex "oben" ist und damit auch der "natürliche" UP Mapping Ursprung. 9.3.17: ist der nicht unten?
        double radius = 1;
        Shape shape = ShapeFactory.buildArc(new Degree(90), new Degree(-180), radius, verticalsegments);
        // Ueber einen Kreis extrudieren.
        SegmentedPath path = new SegmentedPath(new Vector3(radius, 0, 0));
        /*float[] steps = new float[horizonzalsegments];
        for (int i = 0; i < horizonzalsegments; i++) {
            steps[i] = roundness.degree / horizonzalsegments;
        }*/
        // Weil die Geometrie geschlossen wird, das letzte Segment nicht im  Path anlegen
        // 02.11.16: closed geo gibts nicht mehr
        Degree r = new Degree(roundness.getDegree() * (horizonzalsegments /*- 1*/) / horizonzalsegments);
        path.addArc(new Vector3(0, 0, 0), r, horizonzalsegments /*- 1*/);
        ShapeGeometry g = new ShapeGeometry(shape, path/*, steps*/);// 60, new Degree(12));
        return g;
    }

    /**
     * Eigentlich eine Figur nur zum Testen. Einen senkrechten Strich (von unten nach oben Laenge 2) kreisrund extrudieren.
     * <p/>
     * Center in 0,0,0
     */
    public static ShapeGeometry buildVerticalTube(int horizonzalsegments) {
        Shape shape = new Shape(false);
        shape.addPoint(new Vector2(1, -1));
        shape.addPoint(new Vector2(1, 1));
        // Ueber einen Kreis extrudieren.
        double radius = 1;
        SegmentedPath path = new SegmentedPath(new Vector3(radius, 0, 0));
        // Weil die Geometrie geschlossen wird, das letzte Segment nicht im  Path anlegen
        // 02.11.16: closed geo gibts nicht mehr.
        Degree r = new Degree(360 * (horizonzalsegments /*- 1*/) / horizonzalsegments);
        path.addArc(new Vector3(0, 0, 0), r, horizonzalsegments /*- 1*/);
        ShapeGeometry g = new ShapeGeometry(shape, path/*, steps*/);// 60, new Degree(12));
        return g;
    }

    /**
     * Ein Zylinder (geschlossene Tube). Ein Kreis läuft Richtung z-Achse (depth).
     * Es gibt noch die Alternative, eine Art U um die y-Achse zu rotieren. Da hat man dann nichts mit front und back zu tun.
     * <p/>
     * Center in 0,0,0
     */
    public static ShapeGeometry buildCylinder(double radius, double depth) {
        return buildTube(radius, depth, new Degree(0), new Degree(-360), 64, true, null);
    }

    /**
     * Eine (Teil)Roehre. Geschlossen ist es ein Zylinder. Ein Kreis läuft Richtung z-Achse (depth).
     * Die Punkte des Shape muessen CW sein, sonst passt das Culling nicht.
     * begonnen wird bei startangle. 0 Grad zeigt nach rechts. Ein positiver spanangle fuehrt zu CCW, wie im Einheitskreis.
     * Der Shape und damit die Geo ist nicht geschlossen. Das ist zumindest bei Zylinderbildung auch gut so, denn an der Naht gibt es verschiedene UVs.
     * <p/>
     * Center in 0,0,0
     */
    public static ShapeGeometry buildTube(double radius, double depth, Degree startangle, Degree spanangle, int verticalsegments, boolean closed, List<UvMap1> uvmap) {
        //
        // Bogen von startangle spanangle weit, je nach Vorzeichen CW oder CCW.
        // 
        // 
        Shape shape = ShapeFactory.buildArc(startangle, spanangle, radius, verticalsegments);

        ShapeGeometry g = new ShapeGeometry(shape, depth, 1, uvmap);
        if (closed) {
            g.closeFront((uvmap != null && uvmap.size() > 4) ? uvmap.get(4) : null);
            g.closeBack((uvmap != null && uvmap.size() > 5) ? uvmap.get(5) : null);
        }
        return g;
    }

    /**
     * Eigentlich eine Figur nur zum Testen. Einen waagerechten Strich (von links nach rechts Laenge 2) kreisrund extrudieren.
     * So dass in der Mitte aber ein Loch mit Durchmesser 1 bleibt.
     * <p/>
     * Center in 0,0,0
     */
    public static ShapeGeometry buildHorizontalDisc(int segments) {
        Shape shape = new Shape(false);
        shape.addPoint(new Vector2(0.5f, 0));
        shape.addPoint(new Vector2(2.5f, 0));
        double radius = 2;
        return buildByCircleRotation(shape,/* radius,*/ segments);
    }

    /**
     * Ein Shape ueber einen Kreis extrudieren.
     * Ich glaube, der radius spielt keine Rolle. Doch, spielt eine, aber welche?
     * Beim falschen Radius gibt es innen Verzerrungen. Wahrscheinlich muss der radius im Zentrum des zu
     * rotierenden Shapes liegen.
     * 5.12.16: Die Problematik ist brisanter: Der Shape wird bei der Extrusion rotiert. Je mehr Segmente es gibt, um so eher
     * kann es passieren, dass im Zentrum ein nachfolge Vertex "vor" dem Vorgaenger liegt. Dann wird die Normale
     * die falsche Orientierung bekommen, was hässlich aussieht. Man kann entweder den Shape ein wenig vom center verlegen oder
     * den Rotationsradius sehr nah an den Shapebeginn rücken.
     * Kreisförmige Extrusion mal nicht ueber PathExtruder, sondern speziellen CircleExtruder. Optional auch nicht den kompletten Kreis (spanangle != null).
     * <p>
     * Die Geo wird geschlossen.
     *
     * @param segments
     * @return
     */
    public static ShapeGeometry buildByCircleRotation(Shape shape, int segments, double spanangle, List<UvMap1> uvmap) {
        //Path path = SegmentedPath.buildHorizontalArc(radius, segments);
        // Weil die Geometrie geschlossen wird, ist das letzte Segment im  Path nicht angelegt
        // 02.11.16: closed geo gibts nicht mehr
        CircleExtruder ce = new CircleExtruder(segments, spanangle);
        ShapeGeometry g = new ShapeGeometry(shape, ce, true, uvmap/*, steps*/);// 60, new Degree(12));
        // 19.12.16: Die Geo ist an der Rotationsgrenze wegen evtl. unterschiedlicher uv-Werte nicht geschlossen, auch es optisch so aussieht oder aussehen soll.
        // Darum eine SmootingMap anlegen, die das berücksichtigt.
        /*if (spanangle == null){
            // TODO 19.12.16. Löst ja nicht das Railingproblem. :-(
        }*/
        return g;
    }

    public static ShapeGeometry buildByCircleRotation(Shape shape, int segments) {
        return buildByCircleRotation(shape, segments,  (2 * Math.PI), null);
    }

    /**
     * Center in 0,0,0
     * <p/>
     * width ist entlang x, height der y Achse und depth ueber die z-Achse.
     * 18.5. umbenannt von cuboid nach box
     */
    public static ShapeGeometry buildBox(double width, double height, double depth, List<UvMap1> uvmap) {
        Shape shape = ShapeFactory.buildRectangle(width, height);
        ShapeGeometry g = new ShapeGeometry(shape, depth, 1, uvmap);
        g.closeFront((uvmap != null && uvmap.size() > 4) ? uvmap.get(4) : null);
        g.closeBack((uvmap != null && uvmap.size() > 5) ? uvmap.get(5) : null);
        return g;
    }

    /**
     * Hier sind die Kanten entlang des Shapes abgerundet.
     * Je nach St�rke der Abrundung kann dies zu einem Rohr f�hren.
     * <p/>
     * Center in 0,0,0
     * <p/>
     * width ist entlang x, height der y Achse.
     * 18.5. umbenannt von cuboid nach box
     */
    public static ShapeGeometry buildRoundedBox(double width, double height, double depth, double radius) {
        return buildRoundedBox(width, height, depth, radius, null);
    }

    public static ShapeGeometry buildRoundedBox(double width, double height, double depth, double radius, List<UvMap1> uvmaps) {
        Shape shape = ShapeFactory.buildRoundedRectangle(width, height, radius);
        ShapeGeometry g = new ShapeGeometry(shape, depth, 1, uvmaps);
        // Die uvmap Liste mal so interpretiren, dass Front/Back auch da drin sind. Ob das generell Bestand haben kann, ist
        // fraglich.
        UvMap1 uvmap = null;
        if (uvmaps != null && uvmaps.size() > 1) {
            uvmap = uvmaps.get(1);
        }
        g.closeFront(uvmap);
        uvmap = null;
        if (uvmaps != null && uvmaps.size() > 2) {
            uvmap = uvmaps.get(2);
        }
        g.closeBack(uvmap);
        return g;
    }

    /**
     * Ein Triangle Mesh in der y0 Ebene.  Ersetzt Klasse PlaneGeometry.
     * Center in 0,0,0
     */
    public static ShapeGeometry buildPlane(double width, double height, int widthSegments, int heightSegments, UvMap1 uvmap) {
        Shape shape = ShapeFactory.buildLine(width, widthSegments);
        ShapeGeometry g = new ShapeGeometry(shape, height, heightSegments, uvmap != null ? new SmartArrayList<UvMap1>(uvmap) : null);
        return g;
    }

    public static ShapeGeometry buildPlane(double width, double height, int widthSegments, int heightSegments) {
        return buildPlane(width, height, widthSegments, heightSegments, null);
    }

    /**
     * Eine U-Form, ie. eine Strecke, ein Bogen und wieder eine Strecke.
     * Der Bogen steht nach oben (y). Der Bogen ist abhaengig von arccenteroffset eingedellt.
     * Center ist in der Mitte des Rechtecks (ohne Bogen).
     *
     * @return
     */
    public static CustomGeometry buildU(double width, double height, double arccenteroffset) {
        Shape shape = ShapeFactory.buildU(width, height, arccenteroffset);
        CustomGeometry g = new ShapeGeometry(shape, 1, 1);
        return g;
    }

    /**
     * Ein kreisrotiertes Rechteck. Test fuer korrektes smooth shading an der Rotationsschnittstelle. Da muss eine
     * passende Smootinmg Group angelegt werden.
     *
     * @return
     */
    public static ShapeGeometry buildRotatedRectangle(int segments) {
        Shape rect = ShapeFactory.buildRectangle(3, 2);
        rect = rect.translateX(7);
        ShapeGeometry geo = ShapeGeometry.buildByCircleRotation(rect, segments);
        return geo;
    }

    /**
     * surfaces ist eine HashMap, da gehe auch -1 und -2 fuer Front/Back
     *
     * @param segment
     * @return
     */
    public List<Surface> getSurfaces(int segment) {
        return surfaces.get(segment);
    }

    public int getSurfacesCount() {
        return surfaces.size();
    }

    /**
     * 26.8.15: Das ist doch Asbach.
     * 05.12.16: Mit Parameter surface gehts aber doch, oder?
     */
    public int getTapes(int surface) {
        return ((GridSurface) surfaces.get(surface).get(0)).tapes;
        //return tapecnt;
    }

    /**
     * 28.8.15: Kann jetzt Face3 und Face 4 enthalten
     *
     * @return
     */
    @Override
    public List<FaceList> getFaceLists() {
        List<FaceList> facelist = new ArrayList<FaceList>();
        // 21.3.17: keyset hat keine definierte Reihenfolge. In Java 8 ist sie sortiert, vorher in Reihenfolge des Reinpacken.
        // Duerfte eigentlich egal sein, aber nicht unbedingt für Tests. Darum kein keyset mehr.
        for (List<Surface> surfacelist : surfaces.slist) {
            //List<Surface> surfacelist = surfaces.get(new Integer(index));
            for (Surface surface : surfacelist) {
                /*29.4.16:Nicht mehr umkopieren sondern direkt aus Surface nehmen List<Face> faces = new ArrayList<Face>();
                for (Face face : surface.getFaceLists()) {
                    if (face instanceof Face3 || face instanceof Face4) {
                        faces.add(face);
                    } else {
                        throw new RuntimeException("not impl");
                    }
                }*/
                facelist.add(new FaceList(surface.getFaces()));
            }
        }
        List<Surface> surfacelist1 = surfaces.back;
        if (surfacelist1 != null) {
            for (Surface surface : surfacelist1) {
                facelist.add(new FaceList(surface.getFaces()));
            }
        }
        surfacelist1 = surfaces.front;
        if (surfacelist1 != null) {
            for (Surface surface : surfacelist1) {
                facelist.add(new FaceList(surface.getFaces()));
            }
        }
        return facelist;
    }

    public void makeDoubleSided() {
        //3.11.15 evtl. TODO g
    }


}

class SimpleExtruder implements Extruder {
    private int tapes;
    private double width;

    public SimpleExtruder(double width, int tapes) {
        this.width = width;
        this.tapes = tapes;
    }

    @Override
    public Vector3 transformPoint(Vector2 p, double t) {
        double segmentwidth = width / tapes;

        // Zentrierung auf 0 und beginnend im positven z Bereich zum negativen hin
        double z = width / 2 - t;
        return new Vector3(p.x, p.y, z);
    }

    @Override
    public double[] getTapeSteps(int segment) {
        // Es gibt nur das eine Segment, aber evtl. mehrere Tapes TODO mehr als ein Tape
        //return new float[]{width/2,-width/2};
        //Die Zentrierung auf 0,0 erfolgt nicht ueber die Steps, sondenr den PAth
        //25.8.15: Der Start ist nicht mehr drin.
        double[] tapesteps = new double[tapes];
        for (int i = 0; i < tapes; i++) {
            tapesteps[i] = (i + 1) * width / tapes;
        }
        //return new float[]{/*0,*/ width};
        return tapesteps;
    }

    @Override
    public int getSegments() {
        return 1;
    }

    @Override
    public double getStart() {
        return 0;
    }
}

class SurfaceList {
    List<List<Surface>> slist = new ArrayList<List<Surface>>();
    List<Surface> front = null;//new ArrayList<Surface>();
    List<Surface> back = null;//new ArrayList<Surface>();

    SurfaceList() {

    }

    public int size() {
        int s = slist.size();
        if (front != null) {
            s++;
        }
        if (back != null) {
            s++;
        }
        return s;
    }

    public List<Surface> get(int surface) {
        if (surface == -1) {
            return front;
        }
        if (surface == -2) {
            return back;
        }
        return slist.get(surface);
    }


    public void put(int j, ArrayList<Surface> surfaces) {
        while (slist.size() < j+1) {
            slist.add(null);
        }
        slist.set(j, surfaces);
    }
}