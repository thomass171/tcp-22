package de.yard.threed.engine.ecs;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Quaternion;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * Viewpoint Positionen (Model oder auch Camera) inklusive Orientierung. Relativ zu seiner Entity. parent ist zusätzlich
 * erforderlich, weil der Viewpoint nicht unbedingt an der root node der entity liegt.
 * Rotieren gibt es hier nicht, das mach die ObserverComponent. Evtl. zusammenführen
 * Stammt aus StepController.
 * 29.10.17: Das koennte mittlerweilse redundant zu Teleport sein, aber nicht zu Observer.
 * 08.05.21: Was anderes als Observer(?)
 * <p>
 * Created by thomass on 09.01.17.
 */
public class ViewpointComponent extends EcsComponent {
    //public List<PosRot> point = new ArrayList<PosRot>();
    //public List<SceneNode> parent = new ArrayList<SceneNode>();
    //  public SceneNode observer;
    public List<Vector3> stepposition = new ArrayList<Vector3>();
    //List<Vector3> steplookat = new ArrayList<Vector3>();
    List<Quaternion> steprotation = new ArrayList<Quaternion>();
    Vector3 upVector = new Vector3(0, 1, 0);
    SceneNode parent;

    public ViewpointComponent(SceneNode parent) {
        this.parent = parent;
    }

    //@Override
    public void doinit() {

    }

    @Override
    public String getTag() {
        return "ViewpointComponent";
    }

    public void addPosition(LocalTransform posrot) {
        stepposition.add(posrot.position);
        // steplookat.add(lookat);
        steprotation.add(posrot.rotation);

    }

    public void addStep(Vector3 position, Vector3 lookat) {
        Vector3 forward = lookat.subtract(position);
        // 14.12.16: Warum negate?
        Quaternion rotation = Quaternion.buildLookRotation(forward/*dir*/.negate(), upVector);
        stepposition.add(position);
        // steplookat.add(lookat);
        steprotation.add(rotation);
        //stepparent.add(null);
    }

    public void addStep(Vector3 position, Quaternion rot) {
        stepposition.add(position);
        //steplookat.add(null);
        steprotation.add(rot);
        //stepparent.add(null);
    }
}
