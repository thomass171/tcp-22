package de.yard.threed.engine;

import de.yard.threed.core.platform.*;

import de.yard.threed.engine.platform.common.SimpleGeometry;

/**
 * Ein Mesh entsteht aus einer Geometry und darauf gelegten Oberflaechen/Materialien.
 * Gerendered wird ein Mesh, keine Geometry.
 * <p/>
 * Da es zu einem Mesh genau einen VBO etc und damit auch genau einen Shader gibt,
 * kann das Mesh nur aus genau einem Material bestehen, denn daraus ergibt sich der verwendete Shader.
 * Das passt eigentlich auch gut zur realen Welt. Da ist ein Objekt in der Regel aus demselben Material
 * (evtl. mit verschiedenen Farben durch unterschiedliche Lackierung). Wenn ein anderes Material ins Spiel kommt,
 * dann sind es doch eher zusammengesetzte Objekte.
 * <p/>
 * Das ist anders als in ThreeJS, wo ein Mesh aus verschiedenen Materialien bestehen kann.
 * <p/>
 * 24.4.14: Ob der Name so ganz passt, das sei nochmal dahingestellt.
 * <p/>
 * Zusammengeh�rende VBOs, Indexbuffer, Shader und ...
 * <p/>
 * <p/>
 * 15.6.16: Mesh ist nicht mehr Teil des Scenegraph, sondern Komponente eines SceneNodes (analog Unity)
 * 28.4.21: Den ganzen alten auskommentierten Kram mit uvmap removed. Eine uvmap gibts im mesh genauso wenig wie im Material.
 * Date: 11.04.14
 */
public class Mesh /*extends Object3D /*implements Renderable*/ {
    Log logger = Platform.getInstance().getLog(Mesh.class);
    //28.4.21 List<UvMap1> uvmap;
    //WebGlMesh mesh;
    // 09.11.15 Die Geometry und Material ist hier nur bekannt, wenn sie dynamic ist.
    // Sonst koennte der GC sie nicht freigeben, nachdem sie in der GPU ist.
    // 02.05.16: Das Konzept erstmal nicht weiter verfolgen. Wenn eine Geometrie in der Platform nach einem Update nicht mehr benötigt wird, muss
    // die freigegebn werden. Wobei das mit der Verwendung wegen shared geometry ja gar nicht so einfach ist.
    //CustomGeometry customDynamicGeometry = null;
    public NativeMesh nativemesh;

    /**
     * Die Oberfl�che ergibt sich aus dem Material.
     * Ein Shader mit Lichteffekten.
     */
    public Mesh(SimpleGeometry geometry, Material material) {
        this(new GenericGeometry(geometry), material, false, false/*,false*/);
    }
    public Mesh(Geometry geometry, Material material) {
        this(geometry, material, false, false/*,false*/);
    }

    public Mesh(Geometry geometry, Material material, boolean castShadow, boolean receiveShadow) {
        this(geometry.getNativeGeometry(), material, castShadow, receiveShadow/*,false*/);
    }
    
    /*public Mesh(Geometry geometry, Material material, boolean castShadow, boolean receiveShadow, boolean isLine) {
        this(geometry.getNativeGeometry(), material, castShadow, receiveShadow,isLine);
    }*/
    
    public Mesh(NativeGeometry geometry, Material material, boolean castShadow, boolean receiveShadow/*, boolean isLine*/) {
        //logger.debug("Building mesh with geo "+geometry.getId());
        nativemesh = Platform.getInstance().buildMesh(geometry, (material!=null)?material.material:null, castShadow, receiveShadow/*,isLine*/);
    }

    public Mesh(NativeGeometry geometry, NativeMaterial material, boolean castShadow, boolean receiveShadow) {
        this(geometry,material,castShadow,receiveShadow,false);
    }
    
    public Mesh(NativeGeometry geometry, NativeMaterial material, boolean castShadow, boolean receiveShadow, boolean isLine) {
        nativemesh = Platform.getInstance().buildMesh(geometry, material, castShadow, receiveShadow/*,isLine*/);
    }

    public Mesh(CustomGeometry geometry, Material material) {
        this(geometry, material, false, false,false);
    }

    public Mesh(CustomGeometry geometry, Material material, boolean castShadow, boolean receiveShadow){
        this(geometry,material,castShadow,receiveShadow,false);
    }
    /**
     * Ein Mesh aus einer CustomGeometrie bauen.
     */
    public Mesh(CustomGeometry geometry, Material material, boolean castShadow, boolean receiveShadow, boolean isLine) {
        /*2.5.16if (geometry.isDynamic()) {
            this.customDynamicGeometry = geometry;
        }*/
        //MiscWrapper.alert("Building Mesh");
        // Die Normalen in den Faces bauen, wo sie noch fehlen
        //4.2.16: Das macht doch die Plattform buildMissingNormals(geometry);
        //2.5.16 object3d = Platform.getInstance().buildMeshG(geometry.getVerticesNative(), geometry.getFaceLists(), material.material, castShadow, receiveShadow);
        this(GenericGeometry.buildGenericGeometry(geometry)/*geometry.getVerticesNative(), geometry.getFaceLists(),*/, material, /*null,*/ castShadow, receiveShadow/*,isLine*/);
    }

    public Mesh(NativeMesh nativeMesh) {
        this.nativemesh = nativeMesh;
    }


    public void updateMeshG(NativeMesh mesh, Geometry geo/*List<Vector3> vertices, List<FaceList> faces*/, NativeMaterial material/*,List<Vector3> normals*/) {
       /* // das G steht fuer generic faces.
        List<Face3List> faces3 = null;
        // mesh update geht auch ohne faces nur mit Material.
        if (faces != null) {
            faces3 = GeometryHelper.triangulate(faces);
        }
        if (normals == null && vertices != null && faces != null) {
            normals = GeometryHelper.calculateSmoothVertexNormals(vertices, faces3);
        }*/
        Platform.getInstance().updateMesh(mesh,(geo==null)?null:geo.getNativeGeometry()/* vertices, faces3*/, material/*,normals*/);
    }
    
    /**
     * 30.1.15: Eine neue Geometry setzen. Dann muss auch der VBO neu erstellt werden.
     * Der update ist sehr aufwändig, weil der ganze VBO neu erstellt werden muss.
     * Aber weil die Basis 3D-Daten erhalten bleiben sollen, gibt es diesen Update trotzdem,
     * anstatt das Mesh einfach komplett neu zu erstellen.
     * 09.11.15: Das definier ich mal nur mit CustomGeometry und auch nur dann, wenn sie als
     * dynamisch definiert ist.
     */
    public void updateGeometry(CustomGeometry geometry) {
       updateGeometry(GenericGeometry.buildGenericGeometry(geometry));
    }

    public void updateGeometry(Geometry geometry) {
       /*2.5.16 if (customDynamicGeometry == null || !geometry.isDynamic()) {
            throw new RuntimeException("geometry not marked as dynamic");
        }*/
        //15.2.16 buildMissingNormals(geometry);

        updateMeshG((NativeMesh) nativemesh, geometry, null);
        //2.5.16 customDynamicGeometry = geometry;
        //buildMesh(geometry, material, material.getDefaultShader(), uvmap);
        //needssetup=true;
    }
    
    @Deprecated
    public void updateMaterial(Material material) {
        updateMeshG((NativeMesh) nativemesh, null,  material.material);
        //needssetup=true;
    }

    /**
     * 13.12.18: Besserer Name statt update
     * @param material
     */
    public void setMaterial(Material material) {
        updateMeshG((NativeMesh) nativemesh, null,  material.material);
        //needssetup=true;
    }
    
    /**
     * 31.12.17: null liefern, wenn native null ist
     * @return
     */
    public Material getMaterial() {
        NativeMaterial n = nativemesh.getMaterial();
        if (n==null){
            return null;
        }
        return new Material(n);
    }

    /*9.11.15public Geometry getGeometry() {
        return geometry;
    }*/


}
