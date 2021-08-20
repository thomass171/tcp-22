package de.yard.threed.engine;


import de.yard.threed.core.Vector3;

import de.yard.threed.engine.geometry.GeometryHelper;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.common.SimpleGeometry;

import java.util.List;

/**
 * Die Verbindung zwischen einer CustomGeometry und SimpleGeometry zu einer Geometry. Damit kann der GC die CustomGeometry nach Anlage einer Native abräumen.
 * Created by thomass on 03.05.16.
 */
public class GenericGeometry extends Geometry {
    public GenericGeometry(SimpleGeometry geo){
       /* List<Face3List> faces3 = GeometryHelper.triangulate(faces);
        if (normals == null) {
            normals = GeometryHelper.calculateSmoothVertexNormals(vertices, faces3);
        }*/
       //validateGeometry(geo);
       geometry = EngineHelper./*getInstance().*/buildGeometry(geo.getVertices(),geo.getIndices(),geo.getUvs(), geo.getNormals());
    }

    /**
     * 10.4.18:Bringt keine Erkenntnisse
     * @param geo
     */
    private void validateGeometry(SimpleGeometry geo) {
        boolean valid=true;
        if (geo.getIndices().length%3 != 0){
            throw new RuntimeException("triangle mismatch");
        }
        for (int i=0;i<geo.getIndices().length;i+=3){
            Vector3 v0=geo.getVertices().getElement(geo.getIndices()[i]);
            Vector3 v1=geo.getVertices().getElement(geo.getIndices()[i+1]);
            Vector3 v2=geo.getVertices().getElement(geo.getIndices()[i+2]);
           float limit=0.00001f;
            double distance = MathUtil2.getDistance(v0,v1);
            if (distance < limit){
                //logger.error("low distance "+distance);
                valid=false;
            }
            distance = MathUtil2.getDistance(v2,v1);
            if (distance < limit){
                //logger.error("low distance "+distance);
                valid=false;
            }
            distance = MathUtil2.getDistance(v0,v2);
            if (distance < limit){
                //logger.error("low distance "+distance);
                valid=false;
            }
        }
        if (!valid) {
            logger.error("low distance ");
        }
    }

    /**
     * 28.12.18: Sowas gibt es jetzt auch in GeometryHelper. Darum hier, auch wegen der Exception, deprecated.
     * @param cg
     * @return
     */
    @Deprecated
    public static GenericGeometry buildGenericGeometry(CustomGeometry cg){
        // 13.7.16: Splitten duerfte nicht erforderlich sein, weil es nur ein Material gibt.
        // 01.12.16: Es kann aber Kanten geben, die Vertexduplizierung erfordern.
        List<SimpleGeometry> geos = GeometryHelper.prepareGeometry(cg.getVertices(), cg.getFaceLists(), cg.getNormals(), false,cg.hasedges);
        if (geos.size() > 1){
            throw new RuntimeException("unexpected multiple geos");
        }
        return new GenericGeometry(geos.get(0));
    }
}
