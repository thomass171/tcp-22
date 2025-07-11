package de.yard.threed.traffic;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.DataProvider;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.ecs.TeleporterSystem;
import de.yard.threed.engine.platform.common.Request;
import de.yard.threed.engine.platform.common.RequestType;
import de.yard.threed.trafficcore.EllipsoidCalculations;

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
    public boolean scenerysystemdebuglog = true;
    AbstractSceneryBuilder terrainBuilder;
    public static String TAG = "ScenerySystem";

    public ScenerySystem(SceneNode world) {
        super(new RequestType[]{RequestRegistry.TRAFFIC_REQUEST_LOAD_SCENERY}, new EventType[]{TeleporterSystem.EVENT_POSITIONCHANGED});
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

    @Override
    public boolean processRequest(Request request) {
        if (scenerysystemdebuglog) {
            logger.debug("got request " + request.getType());
        }
        if (request.getType().equals(RequestRegistry.TRAFFIC_REQUEST_LOAD_SCENERY)) {
            // 4.5.25 Was part of TeleporterSystem.EVENT_POSITIONCHANGED before, but now separate request
            LatLon latlon = request.getPayload().get(Payload.KEY_LATLON, s -> Util.parseLatLon(s));

            if (terrainBuilder != null) {
                terrainBuilder.updateForPosition(latlon);
            } else {
                logger.warn("No terrain builder");
            }
            return true;
        }
        return false;
    }

    /**
     *
     */
    @Override
    public void process(Event evt) {
        if (scenerysystemdebuglog) {
            logger.debug("got event " + evt.getType() + ":" + evt);
        }

        if (evt.getType().equals(TeleporterSystem.EVENT_POSITIONCHANGED)) {
            // 4.5.25 Request TRAFFIC_REQUEST_LOAD_SCENERY extracted, so this only happens via teleport.
            Vector3 position = evt.getPayload().getPosition();
            EllipsoidCalculations ec = TrafficHelper.getEllipsoidConversionsProviderByDataprovider();
            // for now assume we only need scenery load in combination with EllipsoidConversionsProvider. This is no clear concept.
            if (ec != null) {
                SystemManager.putRequest(RequestRegistry.buildLoadScenery(ec.fromCart(position)));
            } else {
                logger.warn("no EllipsoidConversionsProvider, so no LOAD_SCENERY request for EVENT_POSITIONCHANGED");
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
        // 7.5.25 Now we have TerrainElevationProvider from tcp-flightgear.
        DataProvider tep = new TerrainElevationProvider(terrainBuilder);
        // An existing dataprovider cannot be overwritten. So we remove a possibly existing. No reason for a warning,
        // a new terrainbuilder implies different elevations.
        SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, null);
        SystemManager.putDataProvider(SystemManager.DATAPROVIDERELEVATION, tep);
    }

    public boolean hasTerrainBuilder() {
        return terrainBuilder != null;
    }

    /**
     * Intended for testing
     */
    public AbstractSceneryBuilder getTerrainBuilder() {
        return terrainBuilder;
    }
}


