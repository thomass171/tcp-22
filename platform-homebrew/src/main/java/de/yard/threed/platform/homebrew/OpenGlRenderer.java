package de.yard.threed.platform.homebrew;

import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.Settings;

import java.util.List;

public class OpenGlRenderer extends HomeBrewRenderer {
    GlInterface glImpl;

    static Log logger = PlatformHomeBrew.getInstance().getLog(OpenGlRenderer.class);

    public OpenGlRenderer(GlInterface glImpl){
        this.glImpl=glImpl;
    }

    @Override
    public void doRender(HomeBrewMesh mesh, Matrix4 projectionmatrix, Matrix4 viewmatrix, List<OpenGlLight> lights) {
        /*mesh.*/render(mesh, projectionmatrix, viewmatrix, lights);
    }

    @Override
    protected void doRender(HomeBrewSceneNode mesh, Matrix4 projectionmatrix, Matrix4 viewmatrix, List<OpenGlLight> lights) {
        //6.3.21 not needed here
    }

    /**
     * 26.4.20: Jetzt drei Methoden, die einst in OpenGlMesh waren.
     *
     * @param projectionmatrix
     * @param viewmatrix
     * @param lights
     */
    //@Override
    private void render(HomeBrewMesh mesh, Matrix4 projectionmatrix, Matrix4 viewmatrix/*, Matrix4 parenttransformation*/, List<OpenGlLight> lights) {
        if (mesh.needsSetup()) {
            mesh.setup(glImpl);
        }
        Matrix4 transformation = dorender(mesh, glImpl, projectionmatrix, viewmatrix/*, parenttransformation*/, lights);

        if (!Settings.linear) {
            Util.notyet();
           /*3.3.16 for (OpenGlObject3D c : children) {
                c.render(glImpl, projectionmatrix, viewmatrix/*, transformation* /, lights);
            }*/
        }
    }

    //@Override


    //@Override
    private Matrix4 dorender(HomeBrewMesh mesh, GlInterface gl, Matrix4 projectionmatrix, Matrix4 viewmatrix/*, Matrix4 parenttransformation*/, List<OpenGlLight> lights) {
        long startttime = System.currentTimeMillis();
        Matrix4 modelmatrix =  mesh.getSceneNodeWorldModelMatrix();

        if (Settings.usevertexarrays) {
            // Bind to the VAO that has all the information about the vertices
            gl.glBindVertexArray(mesh.vaoId);
        }

        for (int i = 0; i < mesh.geo.ea.l_indices.size(); i++) {
           /* OpenGlMaterial material;
            if (i < materials.size()) {
                material = (OpenGlMaterial) materials.get(i);
            } else {
                //Nicht loggen, weil es bei jedem rednern wieder passiert.
                //logger.warn("missing material for list " + i);
                material = (OpenGlMaterial) materials.get(0);
            }*/

            // logger.debug("dorender");
            //projectionmatrix=new OpenGlMatrix4();
            //if ("shuttle".equals(getName())){
                /*logger.debug("modelmatrix="+new Matrix4(modelmatrix).dump(" "));
                logger.debug("projectionmatrix="+new Matrix4(projectionmatrix).dump(" "));
                logger.debug("viewmatrix="+new Matrix4(viewmatrix).dump(" "));*/
            //}

            HomeBrewMaterial material = (HomeBrewMaterial) mesh.getMaterial();
            HomeBrewGeometry geo = mesh.getGeometry();

            if (!mesh.isBroken) {
                boolean success;
                if (material != null) {
                    material.prepareRender(gl, modelmatrix, projectionmatrix, viewmatrix, lights);

                    // Erst nach VAO binden kann man die VertexAttribArray enablen
                    material.sp.glEnableVertexAttribArray();
                    // ivbo.bindBuffer();
                    success = geo.ivbo.drawElements(geo.ivbo.glcontext, geo.ivbo, geo.ea, i, false, logger);
                } else {
                    //24.9.19:Ist das wireframe?
                    // ivbo.bindBuffer();
                    mesh.wireframematerial.prepareRender(gl, modelmatrix, projectionmatrix, viewmatrix, lights);
                    // Erst nach VAO binden kann man die VertexAttribArray enablen
                    mesh.wireframematerial.sp.glEnableVertexAttribArray();
                    success = geo.ivbo.drawElements(geo.ivbo.glcontext, geo.ivbo, geo.ea, i, true, logger);
                }
                // 25.9.19 der errorcheck ist doch unnötig, der letzte war doch in drawElements
                OpenGlContext.getGlContext().exitOnGLError(gl, "render");
                // Put everything back to default (deselect)
                //GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
                // ivbo.unbindBuffer();
                if (!success){
                    logger.error("drawElements failed for mesh. Setting isBroken. sceneNode="+mesh.getSceneNode().getName());
                    mesh.isBroken=true;
                }
            }

            if (material != null) {
                material.sp.DisableVertexAttribArray();
            }
        }
        gl.glBindVertexArray(0);

        gl.glUseProgram(0);
        OpenGlContext.getGlContext().exitOnGLError(gl, "loopCycle");

        //Util.logTimestamp(log,"dorender",starttime);
        return modelmatrix;
    }
}
