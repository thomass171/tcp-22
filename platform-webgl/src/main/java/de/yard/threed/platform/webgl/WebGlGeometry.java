package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint16Array;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import de.yard.threed.core.platform.NativeGeometry;
import de.yard.threed.core.Vector2Array;
import de.yard.threed.core.Vector3Array;

/**
 * Created by thomass on 25.04.15.
 */
public class WebGlGeometry implements NativeGeometry {
    static Log logger = Platform.getInstance().getLog(WebGlGeometry.class);
    JavaScriptObject geometry;

    private WebGlGeometry(JavaScriptObject geometry) {
        this.geometry = geometry;
    }

    @Override
    public String getId() {
        return getId(geometry);
    }

    public static WebGlGeometry buildCubeGeometry(double width, double height, double depth) {
        return new WebGlGeometry(buildNativeCubeGeometry(width, height, depth));
    }

    /*30.7.21 asbach? public static WebGlGeometry buildPlaneGeometry(double width, double height, int widthSegments, int heightSegments) {
        return new WebGlGeometry(buildNativePlaneGeometry(width, height, widthSegments, heightSegments));
    }

    public static WebGlGeometry buildTubeGeometry(NativeCurve path, int segments, double radius, int radialSegments, boolean closed, boolean debug) {
        return new WebGlGeometry(buildNativeTubeGeometry(path, segments, radius, radialSegments, closed, debug));
    }*/

    public static NativeGeometry buildSphereGeometry(double radius, int wsegs, int hsegs) {
        return new WebGlGeometry(buildNativeSphereGeometry(radius, wsegs, hsegs));
    }

    public static NativeGeometry buildCylinderGeometry(double radiusBottom, double radiusTop, double height, int radialSegments) {
        return new WebGlGeometry(buildNativeCylinderGeometry(radiusBottom, radiusTop, height, radialSegments));
    }

    /**
     * 12.10.18: Uses more efficient THREE.BufferGeometry over Geometry (see docu)
     *
     * @param vertices
     * @param indices
     * @param uvs
     * @param normals
     * @return
     */
    public static NativeGeometry buildGeometry(Vector3Array vertices, int[] indices, Vector2Array uvs, Vector3Array normals) {

        //logger.debug("buildGeometry");
        Uint16Array inds = TypedArrays.createUint16Array(indices.length);
        inds.set(indices);
        //logger.debug("vertices:"+vertices.arr32.length());
        //logger.debug("indices:" + inds.length());
        //for (int i = 0; i < inds.length(); i++) {
        //  logger.debug("ind:" + inds.get(i));
        //}

        JavaScriptObject geo = buildNativeBufferGeometry(createFloat32Array(vertices), indices, inds, createFloat32Array(uvs), (normals == null) ? null : createFloat32Array(normals));
        //WebGlVBO vbo = moveToNative(vertices, indices, uvs, normals/*, nvertices, nnormals, uvs*/);
        //geo = buildNativeGeometry(vbo.vertices, vbo.getTriangles(), vbo.faceuvs, vbo.normals);
        return new WebGlGeometry(geo);
    }

    /**
     * @param a
     * @return
     */
    private static Float32Array createFloat32Array(Vector3Array a) {
        ArrayBuffer buf = ((WebGlByteBuffer) a.basedata).buffer;
        //logger.debug("createFloat32Array:size="+buf.byteLength()+",a.byteOffset="+a.byteOffset+",a.sizeinvec3="+a.sizeinvec3);
        Float32Array arr32 = TypedArrays.createFloat32Array(buf, a.byteOffset, a.sizeinvec3 * 3);
        return arr32;
    }

    private static Float32Array createFloat32Array(Vector2Array a) {
        Float32Array arr32 = TypedArrays.createFloat32Array(((WebGlByteBuffer) a.basedata).buffer, a.byteOffset, a.sizeinvec2 * 2);
        return arr32;
    }

    /*public static /*JsArray* /WebGlVBO moveToNative(Vector3Array vertices, int[] indices, Vector2Array uvs, Vector3Array normals) {
        WebGlVBO vbo = new WebGlVBO(vertices, indices, uvs, normals);
        return vbo;
    }*/

    private static native JavaScriptObject buildNativeCubeGeometry(double width, double height, double depth)  /*-{
        if ($wnd.THREE.REVISION >= 71) {
            var boxgeometry = new $wnd.THREE.BoxGeometry(width,  height,  depth);
            return boxgeometry;
        }
        var cubegeometry = new $wnd.THREE.CubeGeometry(width,  height,  depth);
        return cubegeometry;
    }-*/;

    private static native JavaScriptObject buildNativePlaneGeometry(double width, double height, int widthSegments, int heightSegments)  /*-{
        var geometry = new $wnd.THREE.PlaneGeometry(width,  height,  widthSegments, heightSegments);
        return geometry;
    }-*/;

    /*30.7.21 private static JavaScriptObject buildNativeTubeGeometry(NativeCurve path, int segments, double radius, int radialSegments, boolean closed, boolean debug) {
        return buildNativeTubeGeometry(((WebGlCurve) path).curve, segments, radius, radialSegments, closed, debug);
    }*/

    private static native JavaScriptObject buildNativeTubeGeometry(JavaScriptObject path, int segments, double radius, int radialSegments, boolean closed, boolean debug)  /*-{
        var tubegeometry = new $wnd.THREE.TubeGeometry(path, segments, radius, radialSegments, closed);
        return tubegeometry;
    }-*/;

    private static native String getId(JavaScriptObject geometry)  /*-{
        return ""+geometry.id;
    }-*/;

    private static native JavaScriptObject buildNativeSphereGeometry(double radius, int wsegs, int hsegs)  /*-{
        var spheregeometry = new $wnd.THREE.SphereGeometry(radius,  wsegs,  hsegs);
        return spheregeometry;
    }-*/;

    private static native JavaScriptObject buildNativeCylinderGeometry(double radiusBottom, double radiusTop, double height, int radialSegments)  /*-{
        // Threejs uebergibt zuerst toip und dann bottom radius
        var cylindergeometry = new $wnd.THREE.CylinderGeometry( radiusTop, radiusBottom  ,  height,  radialSegments);
        return cylindergeometry;
    }-*/;

    /*private static native JavaScriptObject buildNativeGeometry(JavaScriptObject vertices, JavaScriptObject faces, JavaScriptObject uv, JavaScriptObject normals)  /*-{
        var geom = new $wnd.THREE.Geometry();

        for (var i=0;i<vertices.length;i++) {
            //$wnd.logger.info("adding vertex "+i+ ": v="+$wnd.dumpVector3(vertices[i]));
            geom.vertices.push(vertices[i]);
        }
        for (var i=0;i<faces.length;i++) {
            //$wnd.logger.info("adding face "+i+ ":"+$wnd.dumpFace3(faces[i]));
            var face = faces[i];
            geom.faces.push(face);
            if (normals != null) {            
                face.vertexNormals[ 0 ] = normals[face.a];
                face.vertexNormals[ 1 ] = normals[face.b];
                face.vertexNormals[ 2 ] = normals[face.c];
            }
        }
        geom.faceVertexUvs[0] = [];
        for (var i=0;i<faces.length;i++) {
            //$wnd.logger.info("adding uv for face "+i+ ":"+uv[i][0]+","+uv[i][1]+","+ uv[i][2]);
            geom.faceVertexUvs[0][i] = [ uv[i][0],uv[i][1], uv[i][2]  ];
        }
        if (normals == null) {
            //oder computeVertexNormals?
            geom.computeFaceNormals();
        } 
        return geom;
    }-* /;*/

    /**
     * Prefer more efficient BufferGeometry over Geometry.
     * https://threejs.org/docs/#api/en/core/BufferGeometry
     * <p>
     * Mit dem Uint16Array ist was faul.->invalid type
     * 5.5.21: addAttribute() renamed to setAttribute()
     *
     * @return THREE.BufferGeometry
     */
    private static native JavaScriptObject buildNativeBufferGeometry(Float32Array vertices, int[] inds, Uint16Array indices, Float32Array uv, Float32Array normals)  /*-{                     
        var geometry = new $wnd.THREE.BufferGeometry();

        // itemSize = 3 because there are 3 values (components) per vertex
        geometry.setAttribute( 'position', new $wnd.THREE.BufferAttribute( vertices, 3 ) );
        if (normals != null) {
            geometry.setAttribute( 'normal', new $wnd.THREE.BufferAttribute( normals, 3 ) );
        }
        geometry.setAttribute( 'uv', new $wnd.THREE.BufferAttribute( uv, 2 ) );
        //geometry.setIndex(new $wnd.THREE.BufferAttribute( indices, 1 ));
        geometry.setIndex(inds);
        //vielleicht wird flat shading gemacht?
        //geometry.computeFaceNormals();
        return geometry;
    }-*/;
}
