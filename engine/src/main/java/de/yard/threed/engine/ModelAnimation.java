package de.yard.threed.engine;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Vector3;

/**
 * Animation rein im Modelspace
 *
 * Date: 18.07.14
 */
public class ModelAnimation extends Animation {
    double dx, dy, dz;
    Degree rotx, roty, rotz;
    float scale;
    int stepstogo;
    Transform obj;

    public ModelAnimation(Transform obj, Vector3 translation, Degree rotx, Degree roty, Degree rotz/*TODO,  scale*/, int steps) {
        this.obj = obj;
        dx = translation.getX() / steps;
        dy = translation.getY() / steps;
        dz = translation.getZ() / steps;
        this.rotx = new Degree(rotx.getDegree()/steps);
        this.roty = new Degree(roty.getDegree()/steps);
        this.rotz = new Degree(rotz.getDegree()/steps);
        this.scale = scale / steps;
        this.stepstogo = steps;
    }

    @Override
    public boolean process(boolean forward) {
        obj.translateX(dx);
        obj.translateY(dy);
        obj.translateZ(dz);
        obj.rotateX(rotx);
        obj.rotateY(roty);
        obj.rotateZ(rotz);
        //TODOobj.scale(scale);
        stepstogo--;
        return stepstogo == 0;
    }

    @Override
    public String getName() {
        return "??";
    }
}
