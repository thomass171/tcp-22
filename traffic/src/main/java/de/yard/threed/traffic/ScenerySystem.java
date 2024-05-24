package de.yard.threed.traffic;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.DataProvider;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.TeleporterSystem;
import de.yard.threed.traffic.geodesy.ElevationProvider;

/**
 * Merge of TerrainSystem and FlatTerrainSystem discarded (MA49).
 * <p>
 * Displays/Updates terrain based on position changes.
 * Decoupled from effective terrain by AbstractTerrainBuilder.
 * AbstractTerrainBuilder and EllipsoidCalculations should be provided by SphereConfiguration.
 * TerrainSystem20 renamed to ScenerySystem, because its not only terrain. Scenery might include for example
 * buildings and trees, which is not part of terrain.
 * <p>
 * Created by thomass on 20.09.23.
 */
public class ScenerySystem extends DefaultEcsSystem {
    Log logger = Platform.getInstance().getLog(ScenerySystem.class);
    SceneNode world;
    public boolean terrainsystem20debuglog = true;
    AbstractSceneryBuilder terrainBuilder;
    public static String TAG = "ScenerySystem";

    public ScenerySystem(SceneNode world) {
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
            logger.debug("got event " + evt.getType() + ":" + evt);
        }

        if (evt.getType().equals(TeleporterSystem.EVENT_POSITIONCHANGED)) {
            LocalTransform newpos = (LocalTransform) evt.getPayloadByIndex(0);

            if (terrainBuilder != null) {
                // quick solution for now without direction and optimization
                terrainBuilder.updateForPosition(newpos.position, null);
            } else {
                logger.warn("No terrain builder");
            }
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public void setTerrainBuilder(AbstractSceneryBuilder terrainBuilder) {
        this.terrainBuilder = terrainBuilder;
        // 10.5.24 TerrainElevationProvider not available here. But it seems a good location for adding
        // the provider. Temp solution. Maybe get it from terrainbuilder?
        /*13.5.24 Not until we have a solution
        TempElevationProvider tep = new TempElevationProvider();
        SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, tep);*/
    }

    public boolean hasTerrainBuilder() {
        return terrainBuilder != null;
    }
}

class TempElevationProvider implements ElevationProvider, DataProvider {

    @Override
    public Double getElevation(double latitudedeg, double longitudedeg) {
        return 99.0;
    }

    @Override
    public Double getData(Object[] parameter) {
        return Double.valueOf(99.0);
    }

}

