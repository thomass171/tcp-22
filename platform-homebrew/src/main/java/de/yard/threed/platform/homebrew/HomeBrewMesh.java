package de.yard.threed.platform.homebrew;


import de.yard.threed.core.*;
import de.yard.threed.core.platform.*;
import de.yard.threed.engine.Material;

import de.yard.threed.engine.platform.common.*;

/**
 * Eine Geometry mit einem Material. Ohne material wirds wireframe.
 * 25.9.19: Es gab mal die Idee von mehreren Materialien, die ist aber vom Tisch. Das ist aufwaendig, andere können das auch nur bedingt und gar nicht
 * unbedingt nötig. Soll das Mesh halt aufgeteilt werden.
 *
 * Created by thomass on 25.05.15.
 */
public class HomeBrewMesh /*19.7.16 extends OpenGlObject3D*/ implements NativeMesh/*26.4.20 , OpenGlRenderable */{
    //Geometry geometry;
    static Log logger = PlatformHomeBrew.getInstance().getLog(HomeBrewMesh.class);
    public int vaoId = -1;
    //14.7.14: Statt ganzem Material nur die Texture merken. null wenns keine gibt
    //private Material material;
    private boolean setup = false;
    public boolean needssetup = true;
    private HomeBrewMaterial material;
    HomeBrewGeometry geo;
    // 15.6.16 Objekt, an dem dieses Mesh hängt.
    HomeBrewSceneNode parentscenenode;
    public HomeBrewMaterial wireframematerial;
    public boolean isBroken=false;

    /**
     * siehe Platform Interface.
     * Für jede Face3List wird eine eigene Indexliste erstellt, die separat gedrawed wird. Damit können dann auch multiple materials verwendet
     * werden.
     */
    HomeBrewMesh(HomeBrewGeometry geo, NativeMaterial nmaterial, boolean castShadow, boolean receiveShadow) {
        material = (HomeBrewMaterial) nmaterial;
        this.geo = geo;
        //istransparent = false;
        //for (NativeMaterial nm : materials){
        /*20.7.16 if (nmaterial.isTransparent()) {
            istransparent = true;
        }*/
        // }

        //logger.debug(ivbo.dump("\n"));
    }

    public static HomeBrewMesh buildMesh(HomeBrewGeometry geo, NativeMaterial material, boolean castShadow, boolean receiveShadow) {
        return new HomeBrewMesh(geo, material, castShadow, receiveShadow);
    }

    public void updateMesh(NativeGeometry geometry, NativeMaterial nmaterial) {
        HomeBrewGeometry geo = (HomeBrewGeometry) geometry;
        if (geo != null/*vertices != null && faces != null*/) {
            //TODO entweder alten weiterverwenden oder freigeben
            //ivbo = new OpenGlIndexedVBO(OpenGlIndexedVBO.MODE_NORMAL, /*TODO | OpenGlIndexedVBO.MODE_COLORED | ((uvprovider != null) ? IndexedVBO.MODE_TEXTURED : 0*/ false, false);
            //GeometryHelper.buildVBOandTriangles(geo.vertices, geo.facelist, null, ivbo,geo.normals);
            this.geo = geo;
        }
        if (nmaterial != null) {
            //istransparent = nmaterial.isTransparent();
            material = /*new SmartArrayList<NativeMaterial>*/(HomeBrewMaterial) nmaterial;
        }
    }

    public boolean needsSetup() {
        return needssetup;
    }

    //26.4.20 @Override
    public void setup(GlInterface gl) {
        //11.11.14 super.setup(gl);
        // Der ShaderProgram setup muss ausserhalb gemacht werden, weil er sonst doppelt sein kann
        /*3.3.16if (texture != null)
            texture.setup();*/
        if (material != null) {
             material.setup();

        }else{
            // auch wireframe (GL_LINE_LOOP braucht wohl einen Shader
            MaterialDefinition materialDefinition=new MaterialDefinition("wireframe",Material.buildColorMap(Color.WHITE), null, Material.buildParam(NumericType.SHADING, new NumericValue(NumericValue.UNSHADED)));
            wireframematerial = (HomeBrewMaterial) HomeBrewMaterial.buildMaterial(gl, materialDefinition);
        }
       gl.exitOnGLError(gl, "setup.material.setup");


        if (Settings.usevertexarrays) {
            vaoId = gl.GenVertexArrays();
            gl.glBindVertexArray(vaoId);
        }
       gl.exitOnGLError(gl, "setup.glBindVertexArray");

        /*if (material == null){
            OpenGlContext.getGlContext().glEnableVertexAttribArray(0);
        }*/
        //cvbo.build(buildModelMatrix());
        geo.ea.setup(gl, parentscenenode.object3d.getLocalModelMatrix());
        geo.ivbo.setup(gl, parentscenenode.object3d.getLocalModelMatrix());
        if (Settings.usevertexarrays) {
            gl.glBindVertexArray(0);
        }
       gl.exitOnGLError(gl, "after setup");
        setup = true;
        needssetup = false;
    }

    public Matrix4 getSceneNodeWorldModelMatrix(){

            Matrix4 modelmatrix =  parentscenenode.object3d.getWorldModelMatrix();//.multiply(parenttransformation);
            // Die Mulitplikation ist M = Parent * local;
            // Das ist so, wie ThreeJS es auch macht.
            //modelmatrix = parenttransformation.multiply(buildModelMatrix());
        return modelmatrix;
    }


    @Override
    public NativeMaterial getMaterial() {
        return material;
    }

    @Override
    public void setBoxColliderSizeHint(Vector3 size) {
        //ignore, kann platform eh nicht
    }

    @Override
    public NativeSceneNode getSceneNode() {
        return parentscenenode;
    }

    public HomeBrewGeometry getGeometry() {
        return geo;
    }
}
