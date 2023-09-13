package de.yard.threed.engine;

import de.yard.threed.core.geometry.CustomGeometry;
import de.yard.threed.core.geometry.FaceList;

import java.util.ArrayList;
import java.util.List;

/**
 * Weil CustomGeometry abstract ist.
 * 
 * Created by thomass on 23.11.16.
 */
public class PrimitiveGeometry extends CustomGeometry {
    public List<FaceList> facelist = new ArrayList<FaceList>();
    
    @Override
    public List<FaceList> getFaceLists() {
        return facelist;
    }
}
