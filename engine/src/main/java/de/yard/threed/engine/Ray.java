package de.yard.threed.engine;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ein Strahl bestimmter Laenge von einem Ausgangspunkt in eine Richtung.
 * Der Origin ist in world coordinates. Alles andere ja auch Unsinn.
 * 
 * <p/>
 * Created by thomass on 25.11.14.
 */
public class Ray {
    Log logger = Platform.getInstance().getLog(Ray.class);
    NativeRay ray;

    public Ray(Vector3 origin, Vector3 direction) {
        this(origin, direction, java.lang.Double.MAX_VALUE);
    }

    public Ray(Vector3 origin, Vector3 direction, double length) {
        // this.origin = origin;
        // Das mit normalize ist aus http://gamedev.stackexchange.com/questions/72440/the-correct-way-to-transform-a-ray-with-a-matrix
        // Sicherstellen, dass die direction immer normalisiert ist. ThreeJs ist da erwiesenermassen empfindlich.
        ray = Platform.getInstance().buildRay(origin, direction.normalize());
        //29.11.15 this.length = length;
    }
    
    public Ray(NativeRay ray){
        this.ray = ray;
    }

    @Override
    public String toString() {
        return "ray from " + getOrigin() + " with direction " + getDirection();
    }

    public Vector3 getDirection() {
        return (ray.getDirection());
    }

    public Vector3 getOrigin() {
        return ((ray.getOrigin()));
    }


    /**
     * 29.3.18:Kombination auf Native intersects und dann Teilgraphsuche. TODO
     * 21.5.21: Das mit dem Teilgraph scheint Unsinn zu sein.
     */
    public List<NativeCollision> getIntersections/*21.5.21 intersects*/(SceneNode model) {
        //nicht so einfach, zumindest im Maze guigrid
        /*21.5.21 boolean newmode=false;
        if (newmode){
            List<NativeCollision> intersections =new ArrayList<NativeCollision>();
            List<NativeCollision> allintersections = getIntersections();
            for (NativeCollision intersection : allintersections){
                //die Suche uebr den Namen ist natuerlich etwas unsicher
                if (model.findNodeByName(intersection.getSceneNode().getName(),true)!=null) {
                    intersections.add(intersection);
                }
            }
            return intersections;
        }*/
        return ray.intersects((NativeSceneNode) model.nativescenenode);
    }

    /**
     * 21.5.21
     * @param model
     * @return
     */
    public boolean intersects(SceneNode model) {
        return getIntersections(model).size()>0;
    }

    /**
     * Die Reihenfolge der gelieferten Models ist nicht definiert.
     *
     * @param modellist
     * @return
     */
    public List<NativeCollision> intersects(List<SceneNode> modellist) {
        List<NativeCollision> imodellist = new ArrayList<NativeCollision>();
        for (SceneNode m : modellist) {
            List<NativeCollision> nclist = ray.intersects((NativeSceneNode) m.nativescenenode);
            for (NativeCollision nc : nclist) {
                imodellist.add(nc);
            }
        }
        return imodellist;
    }

    /**
     * Einfach alle Collisions ermitteln, ohne einen (Teil)Graph zu uebergeben, in dem gesucht wird. Ist mehr Unity Like.
     * Und manchmal auch praktischer.
     * @return
     */
    public List<NativeCollision> getIntersections(){
        return ray.getIntersections();
    }
    



}
