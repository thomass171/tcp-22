package de.yard.threed.engine.apps;


import de.yard.threed.core.*;
import de.yard.threed.core.geometry.Face;
import de.yard.threed.core.geometry.Face3;
import de.yard.threed.core.geometry.Face3List;
import de.yard.threed.core.geometry.FaceList;
import de.yard.threed.core.loader.PmlFactory;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.geometry.GeometryHelper;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.engine.*;
import de.yard.threed.core.platform.NativeMaterial;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;


import java.util.ArrayList;
import java.util.List;

/**
 * Beispielmodelle.
 * <p/>
 * Einheitliche Fehlerbehandlung: Ein Fehler wird geloggt und null geliefert. Einen Fehlerdialog gibt es nicht.
 * <p/>
 * Greift sowohl auf gebundelte wie externe Resourcen zu (FG). Das sollte vielleicht mal getrennt werden.
 * <p>
 * 22.12.18: Needs cleanup anyway and distribution to Modules.
 * <p>
 * <p/>
 * Created by thomass on 19.08.15.
 */
public class ModelSamples {
    static Log logger = Platform.getInstance().getLog(ModelSamples.class);

    public static Material buildTexturedCubeMaterial(AbstractMaterialFactory materialFactory) {

        //NativeMaterial mat = Material.buildLambertMaterial(Texture.buildBundleTexture("data", "textures/texturedcube-atlas.jpg")).material;
        //Just have a loader pointing to bundle data, from which texture loader will be derived.
        PortableMaterial pm = new PortableMaterial("no-name", "texturedcube-atlas.jpg");
        Material mat = materialFactory.buildMaterial(new ResourceLoaderFromBundle(new BundleResource(BundleRegistry.getBundle("data"), "xx")
        ), pm, new ResourcePath("textures"), true);
        return mat;
    }

    /**
     * The Showroom Cube (from https://github.com/rkwright/geofx_site/tree/master/graphics/nehe-three-js)
     * 22.2.25: Now with material factory instead of hardcoded LambertMaterial
     */
    public static SceneNode buildTexturedCube(double size, AbstractMaterialFactory materialFactory) {

        List</*7.2.18 Native*/Vector3> vertices = new ArrayList<Vector3>();
        // Die Reihenfolge der Vertices hier entspricht einem Cube bei ThreeJS
        vertices.add(new Vector3(0.5f * size, 0.5f * size, 0.5f * size));
        vertices.add(new Vector3(0.5f * size, 0.5f * size, -0.5f * size));
        vertices.add(new Vector3(0.5f * size, -0.5f * size, 0.5f * size));
        vertices.add(new Vector3(0.5f * size, -0.5f * size, -0.5f * size));
        vertices.add(new Vector3(-0.5f * size, 0.5f * size, -0.5f * size));
        vertices.add(new Vector3(-0.5f * size, 0.5f * size, 0.5f * size));
        vertices.add(new Vector3(-0.5f * size, -0.5f * size, -0.5f * size));
        vertices.add(new Vector3(-0.5f * size, -0.5f * size, 0.5f * size));

        Material mat = buildTexturedCubeMaterial(materialFactory);

        Vector2[] bricks = new Vector2[]{new Vector2(0, .666f), new Vector2(.5f, .666f), new Vector2(.5f, 1), new Vector2(0, 1)};
        Vector2[] clouds = new Vector2[]{new Vector2(.5f, .666f), new Vector2(1, .666f), new Vector2(1, 1), new Vector2(.5f, 1)};
        Vector2[] crate = new Vector2[]{new Vector2(0, .333f), new Vector2(.5f, .333f), new Vector2(.5f, .666f), new Vector2(0, .666f)};
        Vector2[] stone = new Vector2[]{new Vector2(.5f, .333f), new Vector2(1, .333f), new Vector2(1, .666f), new Vector2(.5f, .666f)};
        Vector2[] water = new Vector2[]{new Vector2(0, 0), new Vector2(.5f, 0), new Vector2(.5f, .333f), new Vector2(0, .333f)};
        Vector2[] wood = new Vector2[]{new Vector2(.5f, 0), new Vector2(1, 0), new Vector2(1, .333f), new Vector2(.5f, .333f)};

        List<Face> faces = new ArrayList<Face>();

        // Die Faces entsprechen auch einem Cube bei ThreeJS. Die Normalen werden nicht gesetzt. Die muesste die Plattform auch berechnen können.
        faces.add(new Face3(0, 2, 1, bricks[0], bricks[1], bricks[3]));
        faces.add(new Face3(2, 3, 1, bricks[1], bricks[2], bricks[3]));
        faces.add(new Face3(4, 6, 5, clouds[0], clouds[1], clouds[3]));
        faces.add(new Face3(6, 7, 5, clouds[1], clouds[2], clouds[3]));
        faces.add(new Face3(4, 5, 1, crate[0], crate[1], crate[3]));
        faces.add(new Face3(5, 0, 1, crate[1], crate[2], crate[3]));
        faces.add(new Face3(7, 6, 2, stone[0], stone[1], stone[3]));
        faces.add(new Face3(6, 3, 2, stone[1], stone[2], stone[3]));
        faces.add(new Face3(5, 7, 0, water[0], water[1], water[3]));
        faces.add(new Face3(7, 2, 0, water[1], water[2], water[3]));
        faces.add(new Face3(1, 3, 4, wood[0], wood[1], wood[3]));
        faces.add(new Face3(3, 6, 4, wood[1], wood[2], wood[3]));

        FaceList f = new FaceList(faces, true);
        f.onlyface3 = true;
        // Durch das splitten ist hasedges irrelevant.
        List<SimpleGeometry> geolist = GeometryHelper.prepareGeometry(vertices, new SmartArrayList<FaceList>(f), null, true, new Degree(60)/*, false, null*/);
        Mesh mesh = new Mesh(new GenericGeometry(geolist.get(0)).getNativeGeometry(), mat, false, false);
        SceneNode model = new SceneNode();
        model.setMesh(mesh);
        model.setName("MultiTextureCube");
        return model;
    }

    /**
     * 2.10.19: Nicht mehr Lambert, weils ja simpel sein soll.
     *
     * @param size
     * @param color
     * @return
     */
    public static SceneNode buildCube(double size, Color color) {
        ShapeGeometry cubegeometry = ShapeGeometry.buildBox(size, size, size, null);
        //Material mat = Material.buildLambertMaterial(color);
        Material mat = Material.buildBasicMaterial(color);
        SceneNode model = new SceneNode(new Mesh(cubegeometry, mat));
        return model;
    }

    public static SceneNode buildCube(double size, Color color, Vector3 position) {
        SceneNode cube = buildCube(size, color);
        cube.getTransform().setPosition(position);
        return cube;
    }

    /**
     * Der Globus. Der Shape rotiert von der positven Seite. Da die Karte dort mit dem Pazifik
     * ansetzt, liegt der Pazifik auf dem Globus rechts (positives x) und man blickt von (0,0,+z) etwa auf den indischen Ozean.
     * Die y-Achse geht durch die Pole (Nordpol im positivem y).
     */
    public static SceneNode buildEarth() {
        return buildEarth(32, NumericValue.SMOOTH);
    }

    public static SceneNode buildEarth(int segments, int shading) {
        ShapeGeometry geoSphere = ShapeGeometry.buildSphere(segments, segments/*16*/, new Degree(360));
        Material mat = Material.buildLambertMaterial((Texture.buildBundleTexture("data", "textures/earth/2_no_clouds_4k.jpg")), null, (shading == NumericValue.FLAT));
        //mat.setWireframe(true);
        Mesh mesh = new Mesh(geoSphere, mat);
        SceneNode model = new SceneNode();
        model.setMesh(mesh);
        model.setName("Earth");
        return model;
    }

    /**
     * Eine CompassNeedle um Orientierungen zu visualisieren. Darum bewusst schlicht als Triangles, dann faellt eine
     * falsche Orientierung besser auf.
     * <p>
     * Laeuft nativ in der xy-Ebene (z=0) entlang der y-Achse (Norden rot positiv).
     */
    public static PortableModel buildCompassNeedle(double height, double width) {
        PortableModelDefinition needle = new PortableModelDefinition();
        needle.setName("Needle");
        double w2 = width / 2;
        double h2 = height / 2;

        List<Vector3> vertices = new ArrayList<Vector3>();
        FaceList faces = new FaceList(true);
        vertices.add(new Vector3(-w2, 0, 0));
        vertices.add(new Vector3(w2, 0, 0));
        vertices.add(new Vector3(0, h2, 0));
        faces.faces.add(new Face3(0, 1, 2));
        SimpleGeometry geo = GeometryHelper.prepareGeometry(vertices, new SmartArrayList<FaceList>(faces), null, false, new Degree(30)/*, false, null*/).get(0);
        PortableMaterial northmat = new PortableMaterial("northmat", new Color(0xCC, 0, 00));
        PortableModelDefinition north = PmlFactory.buildElement(geo, northmat.getName());
        north.setName("North");
        needle.attach(north);

        vertices = new ArrayList<Vector3>();
        faces = new FaceList(true);
        vertices.add(new Vector3(-w2, 0, 0));
        vertices.add(new Vector3(0, -h2, 0));
        vertices.add(new Vector3(w2, 0, 0));
        faces.faces.add(new Face3(0, 1, 2));
        geo = GeometryHelper.prepareGeometry(vertices, new SmartArrayList<FaceList>(faces), null, false, new Degree(30)/*, false, null*/).get(0);
        PortableMaterial southmat = new PortableMaterial("southmat", new Color(0, 0, 0xCC));
        PortableModelDefinition south = PmlFactory.buildElement(geo, southmat.getName());
        south.setName("South");

        needle.attach(south);

        PortableModel pml = PmlFactory.buildPortableModel(needle, new PortableMaterial[]{northmat, southmat});
        pml.setName("CompassNeedle");
        return pml;
    }
    /*17.1.19: ohne pmd public static SceneNode buildCompassNeedle(float height, float width) {
        SceneNode needle = new SceneNode();
        needle.setName("Needle");
        float w2 = width / 2;
        float h2 = height / 2;

        List<Vector3> vertices = new ArrayList<Vector3>();
        FaceList faces = new FaceList();
        vertices.add(new Vector3(-w2, 0, 0));
        vertices.add(new Vector3(w2, 0, 0));
        vertices.add(new Vector3(0, h2, 0));
        faces.faces.add(new Face3(0, 1, 2));
        SimpleGeometry geo = GeometryHelper.prepareGeometry(vertices, new SmartArrayList<FaceList>(faces), null, false, new Degree(30), false, null).get(0);
        Material northmat = Material.buildLambertMaterial(new Color(0xCC, 0, 00));
        SceneNode north = new SceneNode(new Mesh(new GenericGeometry(geo), northmat));
        north.setName("North");
        needle.attach(north);

        vertices = new ArrayList<Vector3>();
        faces = new FaceList();
        vertices.add(new Vector3(-w2, 0, 0));
        vertices.add(new Vector3(0, -h2, 0));
        vertices.add(new Vector3(w2, 0, 0));
        faces.faces.add(new Face3(0, 1, 2));
        geo = GeometryHelper.prepareGeometry(vertices, new SmartArrayList<FaceList>(faces), null, false, new Degree(30), false, null).get(0);
        Material southmat = Material.buildLambertMaterial(new Color(0, 0, 0xCC));
        SceneNode south = new SceneNode(new Mesh(new GenericGeometry(geo), southmat));
        south.setName("South");

        needle.attach(south);
        return needle;
    }*/


    public static SceneNode buildAxisHelper(double len) {
        return buildAxisHelper(len, 0.05);
    }

    /**
     * Achenfarben wie Unity x=rot,y=green,z=blue
     * TODO umstellen auf lines
     *
     * @param len
     * @return
     */
    public static SceneNode buildAxisHelper(double len, double thickness) {
        SceneNode axishelper = new SceneNode();
        axishelper.setName("AxisHelper");
        ShapeGeometry xg = ShapeGeometry.buildBox(len, thickness, thickness, null);
        Material mat = Material.buildBasicMaterial(Color.RED);
        SceneNode xn = new SceneNode(new Mesh(xg, mat));
        axishelper.attach(xn);
        xg = ShapeGeometry.buildBox(thickness, len, thickness, null);
        mat = Material.buildBasicMaterial(Color.GREEN);
        xn = new SceneNode(new Mesh(xg, mat));
        axishelper.attach(xn);
        xg = ShapeGeometry.buildBox(thickness, thickness, len, null);
        mat = Material.buildBasicMaterial(Color.BLUE);
        xn = new SceneNode(new Mesh(xg, mat));
        axishelper.attach(xn);
        return axishelper;
    }

    /**
     * 17.3.17: Einen Vector optisch darstellen. Irgenwie blöd zu machen mit Faces. Im Moment ist das eher eine Krücke.
     * 12.2.18: Es gibt jetzt lines.
     *
     * @return
     */
    public static SceneNode buildVector(Vector3 v) {
        List</*7.2.18 Native*/Vector3> vertices = new ArrayList<Vector3>();
        List<Vector3> normals = new ArrayList<Vector3>();
        vertices.add(new Vector3());
        vertices.add(v);
        vertices.add(new Vector3(0.01f, 0, 0));
        // Normale sollten egal sein.
        normals.add(new Vector3(0, 0, 1));
        normals.add(new Vector3(0, 0, 1));
        normals.add(new Vector3(0, 0, 1));
        Face3List faces = new Face3List();
        faces.add(new Face3(0, 1, 2));
        // wegen Culling two sided machen
        faces.add(new Face3(2, 1, 0));
        SimpleGeometry geo = new SimpleGeometry(vertices, faces, normals);
        Material mat = Material.buildBasicMaterial(Color.RED);
        SceneNode n = new SceneNode(new Mesh(new GenericGeometry(geo), mat));
        n.setName("Vector");
        return n;
    }


    public static SceneNode buildLine(Vector3 from, Vector3 to, Color color) {
        List<Vector3> vertices = new ArrayList<Vector3>();
        vertices.add(from);
        vertices.add(to);
        List<Vector2> uvs = new ArrayList<Vector2>();
        uvs.add(new Vector2());
        uvs.add(new Vector2());
        //fuer normale einfach vertices duplizieren
        SimpleGeometry xg = new SimpleGeometry(vertices, uvs, vertices, new int[]{0, 1});
        Material mat = Material.buildBasicMaterial(color);
        SceneNode line = SceneNode.buildLineMesh(from, to, color);

        return line;
    }

}

