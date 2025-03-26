package de.yard.threed.traffic;


import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.traffic.VehicleLoaderResult;

import java.util.List;

/**
 * 4.4.18: Zusaetzlich zum ... brauchts das nicht, denn es kann eh nicht gesagt werden, wann ein Model komplett geladen ist.
 * Der hier kann aber definiert werden als Delegate, wenn die oberste Node da ist (auch wenn die noch leer ist).
 * Die Animationlist wird fortlaufend aufgefuellt.
 *
 * "raised" und "basemodel" unterscheidet nur der zoffset? Ja, mit capsule nodes dazwischen.
 *
 * 9.11.21: Decoupled and moved from fg to traffic. FG extends for animationList.
 * This is only for vehicle loading, not entity building. So no EcsEntity parameter.
 * Created on 02.03.18.
 */
@FunctionalInterface
public interface VehicleLoadedDelegate {

    void vehicleLoaded(SceneNode raised,/* SceneNode basemodel,*/ VehicleLoaderResult loaderResult, SceneNode lowresNode);

}
