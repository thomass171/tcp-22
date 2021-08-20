package de.yard.threed.engine;






import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsGroup;

import java.util.List;

/**
 * Steuerung des Viewpoint bzw. View direction. Aber keine Bewegung.
 * <p>
 * Created by thomass on 16.09.16.
 */

public class ObserverSystem extends DefaultEcsSystem {
    
    public ObserverSystem() {
        super(new String[]{"ObserverComponent"});
    }
    
    /**
     * Die initiale Position auch darstellen.
     *
     * @param group
     */
    @Override
    public void init(EcsGroup group) {
        
    }

    @Override
    public void update(EcsEntity entity, EcsGroup group, double tpf) {
        ObserverComponent oc = (ObserverComponent) group.cl.get(0);

        if (!Input.GetKey(KeyCode.Shift)) {

            if (Input.GetKey(KeyCode.RightArrow)) {
                oc.incHeading(-tpf);
            }
            if (Input.GetKey(KeyCode.LeftArrow)) {
                oc.incHeading(tpf);
            }
            if (Input.GetKey(KeyCode.UpArrow)) {
                oc.incPitch(tpf);
            }
            if (Input.GetKey(KeyCode.DownArrow)) {
                oc.incPitch(-tpf);
            }
        }
    }

    /*MA31 dependency zu graph und gehört hier dann doch nicht hin public static EcsGroup matches(List<EcsComponent> components) {
        EcsGroup grp = new EcsGroup();
        for (EcsComponent c : components) {
            if (c instanceof VelocityComponent) {
                grp.add(c);
            }
            if (c instanceof GraphMovingComponent) {
                grp.add(c);
            }
        }
        if (grp.cl.size() == 2) {
            return grp;
        }
        return null;
    }*/
}
