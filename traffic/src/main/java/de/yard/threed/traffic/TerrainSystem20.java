package de.yard.threed.traffic;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.NumericValue;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.apps.ModelSamples;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.TeleporterSystem;

import de.yard.threed.traffic.flight.FlightLocation;

/**
 * Merge result of TerrainSystem and FlatTerrainSystem (MA49).
 * <p>
 * Displays/Updates terrain based on position changes.
 * Decoupled from effective terrain by AbstractTerrainBuilder.
 * AbstractTerrainBuilder should be provided by SphereConfiguration.
 * <p>
 * Created by thomass on 20.09.23.
 */
@Deprecated
public class TerrainSystem20 extends DefaultEcsSystem {
    Log logger = Platform.getInstance().getLog(TerrainSystem20.class);
    SceneNode world;
    boolean terrainsystem20debuglog = true;
    AbstractTerrainBuilder terrainBuilder;
    public static String TAG = "TerrainSystem20";

    public TerrainSystem20(SceneNode world) {
        super(new EventType[]{TeleporterSystem.EVENT_POSITIONCHANGED});
        this.world = world;
    }

    @Override
    public void init(EcsGroup gr) {

        /*TODO add TerrainElevationProvider tep = new TerrainElevationProvider();
        tep.setWorld(world);
        //TODO 25.11.21: Wo kommt denn der her, der hier erst gelöscht werden muss?
        SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, null);
        SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, tep);*/
    }

    /**
     *
     */
    @Override
    public void process(Event evt) {
        if (terrainsystem20debuglog) {
            logger.debug("got event " + evt.getType());
        }

        if (evt.getType().equals(TeleporterSystem.EVENT_POSITIONCHANGED)) {
            LocalTransform newpos = (LocalTransform) evt.getPayloadByIndex(0);
            FlightLocation fl = FlightLocation.fromPosRot(newpos);

            // quick solution for now without direction and optimization
            terrainBuilder.updateForPosition(newpos.position, null);
        }
    }
}

