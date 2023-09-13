package de.yard.threed.engine.apps;

import de.yard.threed.core.geometry.Shape;
import de.yard.threed.engine.*;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.core.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Eine Plane, bei sich einzelne Teile per Animation heben lassen.
 * <p/>
 * Created by thomass on 30.01.15.
 */
public class AnimatedPlane extends SceneNode implements AnimatedModel {
    public Animation animation;
    public Mesh plane;
    public Material material;
    public double height = 0f;

    public AnimatedPlane() {
        // Erstmal ganz platt anlegen
        ShapeGeometry sg = buildGeometry(height);
        material = Material.buildBasicMaterial(Color.GREEN);
        plane = new Mesh(sg, material);
        setMesh(plane);
        animation = new PlaneAnimation(this);

    }

    @Override
    public List<Animation> getAnimations() {
        ArrayList<Animation> list = new ArrayList<Animation>();
        list.add(animation);
        return list;
    }

    @Override
    public void processAnimationStep(int value) {
  
    }

    public ShapeGeometry buildGeometry(double height) {
        Shape shape = new Shape();
        shape.addPoint(-1f, 0f);
        shape.addPoint(0, height, true);
        shape.addPoint(1f, 0f);
        ShapeGeometry sg = new ShapeGeometry(shape, 1, 1);
        sg.setDynamic();
        return sg;
    }

    /*@Override
    public boolean processAnimation() {

    }*/
}


class PlaneAnimation extends Animation {
    AnimatedPlane apl;

    public PlaneAnimation(AnimatedPlane apl) {
        this.apl = apl;
    }

    @Override
    public boolean process(boolean forward) {
        if (forward) {
            apl.height += 0.01f;
        } else {
            apl.height -= 0.01f;
        }
        apl.plane.updateGeometry(apl.buildGeometry(apl.height));
        return false;
    }

    @Override
    public String getName() {
        return "??";
    }
}