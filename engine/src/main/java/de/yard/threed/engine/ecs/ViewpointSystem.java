package de.yard.threed.engine.ecs;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.Input;
import de.yard.threed.engine.KeyCode;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.StepController;
import de.yard.threed.core.platform.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wechseln eines Viewpoint. Anders als ein MovingSystem, das auf einer Geschwindigkeitskomponente basiert. Irgendwie ist das alles zwar ähnlich,
 * aber so sehr ist es doch nicht dasselbe.
 * <p>
 * Stammt aus dem StepController. Der Wechsel der Viewpoints erfolgt je nach Einstellung als Sprung oder als Animation.
 * Jede Entity kann eine Liste von Viepoints (ViewComponent) definieren, die hier dann durchgesprungen wird.
 * 
 * Das ist ähnlich zum TeleportSystem, arbeitet aber mit der Camera. 
 * 7.2.2018: Eigentlich koennte man das auch weglassen und nur mit Teleporter (und Avatar) arbeiten. Ich setz das mal auf deprecated.
 * <p>
 * Created by thomass on 09.04.17.
 */
@Deprecated
public class ViewpointSystem extends DefaultEcsSystem {
    Log logger = Platform.getInstance().getLog(ViewpointSystem.class);
    Map<Integer, Integer> viewpointspercomponent = new HashMap<Integer, Integer>();
    // das vorhalten der Components ist bestimmt unsauber.
    List<ViewpointComponent> groups = new ArrayList<ViewpointComponent>();
    int indexingroup = -1;
    
    int groupindex = -1;
    int startpos, startgroup;
    Camera camera;
    boolean warned = false;

    public ViewpointSystem(int startgroup, int startpos, Camera camera) {
        super(new String[]{"ViewpointComponent"});
        this.startpos = startpos;
        this.startgroup = startgroup;
        this.camera = camera;
        updatepergroup = false;
    }
    
    @Override
    public void init(EcsGroup group) {
        logger.debug("init with group "+group);
        if (group != null) {
            ViewpointComponent vc = (ViewpointComponent) group.cl.get(0);
            int poscnt = vc.stepposition.size();
            viewpointspercomponent.put(group.id, poscnt);
            groups.add(vc);
            if (groups.size() == startgroup + 1) {
                groupindex = startgroup;
                indexingroup = startpos - 1;
                step(0, true);
            }
        } else {
            //index = startpos;
        }
    }
    
    @Override
    public void update(EcsEntity entity,EcsGroup group, double tpf) {
        if (groups.size() == 0 && !warned) {
            logger.warn("no groups set");
            warned = true;
        }
        // Darf nur einmal pro Frame gemacht werden, sonst steppe ich zu weit.         
        if (Input.GetKeyDown(KeyCode.Tab) || Input.GetKeyDown(KeyCode.V)/*fuer Android || Input.GetKeyDown(KeyCode.RightArrow)*/) {
            //logger.debug("tab key was pressed. currentdelta=" + tpf);
            //ViewpointComponent vc = (ViewpointComponent) group.cl.get(0);
            //int poscnt=vc.stepposition.size();
            // if (index < )
            step(tpf, true);
        }

       
    }
   
    public LocalTransform step(/*ViewpointComponent vc,*/ double tpf, boolean forward) {
        if (groupindex == -1){
            // das ist irgendwie etwas krude mit dem index
            groupindex=0;
        }
        ViewpointComponent currentgroup = groups.get(groupindex);
        // if (vc.stepposition.size() > 0) {
        if (forward) {
            if (++indexingroup >= currentgroup.stepposition.size()) {
                indexingroup = 0;
                if (++groupindex >= groups.size()) {
                    groupindex = 0;
                }
            }
        } else {
            //TODO if (--indexingroup < 0)
            //    vc.index = vc.point.size() - 1;
        }
        return stepTo(indexingroup, groups.get(groupindex));
        //}
        //return null;
    }

    public LocalTransform stepTo(int i, ViewpointComponent vc) {
        SceneNode parent = null;
        if (vc.parent != null) {
            parent = vc.parent;
        }
        // SceneNode node = vc.observer;
        StepController.setNewStep(null, vc.stepposition.get(i), vc.steprotation.get(i), false, camera, parent, null, null);
        return null;
    }

}
