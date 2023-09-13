package de.yard.threed.core.loader;

import de.yard.threed.core.Degree;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoadedObject;

/**
 * Created by thomass on 04.04.16.
 */

public class AcObject extends LoadedObject {
    public String type; //one of:  world, poly, group.
    //AcVector3[] vertices = null;
    //int vcount = 0;
    public AcSurface[] surface;
    //public SurfaceBin surfacebin;
    public int pendingkits = 0;
    private float cosCreaseAngle;
    boolean isworld = false;
   
    public AcObject(AcToken token, int materialcnt) throws InvalidDataException {
        type = token.stringvalue;
        // Soviel facelisten anlegen, wie es Materialien gibt
        //16.2.16: Neeneenee, die muessen schon passen
        //for (int i = 0; i < materialcnt; i++) {
        //   addFacelist();
        //}
    }

    public void addObject(AcObject obj) {
        kids.add(obj);
    }

    
    public void setCrease(Degree crease) {
        this.crease = crease;
        if (crease.getDegree() <= 0)
            cosCreaseAngle = 1;
        else if (180 <= crease.getDegree())
            cosCreaseAngle = -1;
        else
            cosCreaseAngle = (float) Math.cos(crease.toRad());
    }

    
}
