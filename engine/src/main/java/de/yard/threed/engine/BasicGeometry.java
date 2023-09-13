package de.yard.threed.engine;


import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.CustomGeometry;
import de.yard.threed.core.geometry.FaceList;
import de.yard.threed.core.SmartArrayList;

import java.util.List;

/**
 * Created by thomass on 22.02.16.
 */
public class BasicGeometry extends CustomGeometry {
    private FaceList facelist;

    public BasicGeometry(List</*7.2.18 Native*/Vector3> vertices, FaceList facelist) {
        this.facelist = facelist;
        this.vertices = vertices;
    }

    @Override
    public List<FaceList> getFaceLists() {
        return new SmartArrayList<FaceList>(facelist);
    }
}
