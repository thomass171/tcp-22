package de.yard.threed.core.geometry;

/**
 * Ein Mapping von einem Vertex Index, auf die Face3, die an diesem Vertex effektiv zusammenstossen, auch wenn der Vertex vielleicht mehrfach existiert.
 * <p>
 * Das ist vielleicht nicht so besonders effizient, aber intuitiver und leichter in die Verarbeitung einzubauen.
 * <p>
 * Siehe Wiki.
 * <p>
 * Created by thomass on 15.12.16.
 */

import de.yard.threed.core.Degree;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;

import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SmoothingMap {
    static Log logger = Platform.getInstance().getLog(SmoothingMap.class);
    Map<Integer, List<Face3>> map = new HashMap<Integer, List<Face3>>();

    public void add(int index, Face3 face) {
        List<Face3> list = map.get(index);
        if (list == null) {
            list = new ArrayList<Face3>();
            map.put(index, list);
        }
        list.add(face);
    }

    /**
     * Die Vertices durchgehen und pruefen, inwieweit die daran liegenden Faces anhand des crease Wertes zusammengehoeren und zu smoothen sind. Wenn Faces
     * nicht dazugehoeren, durch Vertex duplizieren eine neue Group bilden und dann die Pruefung fuer die restlichen Faces durchführen.
     * Die Faces muessen Normale enthalten, sonst kann man ja kein crease bewerten.
     */
    public void applyCrease(List</*7.2.18 Native*/Vector3> vertices, List<Face3List> faces3, Degree crease) {
        //List<List<Face>> facesatvertex;
        float cosCreaseAngle;

        if (crease.getDegree() <= 0)
            cosCreaseAngle = 1;
        else if (180 <= crease.getDegree())
            cosCreaseAngle = -1;
        else
            cosCreaseAngle = (float) Math.cos(crease.toRad());

/*        facesatvertex = new ArrayList<List<Face>>();
        for (int i = 0; i < vertices.size(); i++) {
            facesatvertex.add(new ArrayList<Face>());
        }*/

        for (Face3List fl : faces3) {
            for (Face gface : fl.faces) {
                Face3 face = (Face3) gface;
                //registerFace(facesatvertex, face);
            }
        }


        // Initialbefuellung
        /*List<Vector3> normals = new ArrayList<Vector3>();
        for (int i = 0; i < vertices.size(); i++) {
            normals.add(Platform.getInstance().buildVector3(0, 0, 0));
        }*/
        int vcnt = vertices.size();
        // number of vertices might grow
        for (int i = 0; i < vcnt; i++) {
            List<Face3> facesofvertex = map.get(i);
            // Es gibt vielleicht vertices ohne Faces. Evtl. Line oder sowas.
            if (facesofvertex != null && facesofvertex.size() > 0) {
                // Vertexindizes, die von diesen Faces referenziert werden. Erstmal gibt es nur die eine Gruppe, naemlich die urspruenglich vorhandene
                List<Integer> groupsofvertex = new ArrayList<Integer>();
                groupsofvertex.add(i);
                // Pruefung erst ab dem zweiten Face und immer vergleichen zum ersten Face.
                for (int j = 1; j < facesofvertex.size(); j++) {
                    Face3 f = facesofvertex.get(j);
                    int duplicatedvertex = SmoothingHelper.getgroupForFace(f, i, groupsofvertex, vertices, cosCreaseAngle, this);
                    if (duplicatedvertex != -1) {
                        // Face passt nicht zu den anderen. Zusaetzliches Mapping wenn erforderlich wurde schon angelegt. Der neue Vertexindex ist schon
                        // in groupsofvertex aufgenommen.
                        //add(duplicatedvertex,f);
                        // Und Face aus seiner alten Map rausnehmen.
                        facesofvertex.set(j, null);
                    }
                }
                //fav.removeAll(new SmartArrayList<Object>(null));
                boolean removednull = true;
                while (removednull) {
                    removednull = false;
                    for (int k = 0; k < facesofvertex.size(); k++) {
                        if (facesofvertex.get(k) == null) {
                            facesofvertex.remove(k);
                            removednull = true;
                            break;
                        }
                    }
                }
            } else {
                // 15.12.16: Ist doch egal, oder?
                logger.warn("vertex without face");
            }
        }

    }
}
