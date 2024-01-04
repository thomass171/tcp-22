package de.yard.threed.traffic;


import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.DefaultEcsComponent;
import de.yard.threed.engine.ecs.EcsEntity;


/**
 * Daten ueber die TravelSphere der Entity. Für Entities, die zwischen TravelSphere wechseln.
 * <p>
 * Created by thomass on 18.10.19.
 */
public class SphereComponent extends DefaultEcsComponent {
    Log logger = Platform.getInstance().getLog(SphereComponent.class);
    public static String TAG = "SphereComponent";
    //the current sphere of the entity
    public String currentSphere = null;

    public SphereComponent() {

    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static SphereComponent getSphereComponent(EcsEntity e) {
        SphereComponent vc = (SphereComponent) e.getComponent(SphereComponent.TAG);
        return vc;
    }
}


