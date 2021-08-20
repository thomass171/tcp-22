package de.yard.threed.maze;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.DefaultEcsSystem;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsGroup;
import de.yard.threed.engine.platform.common.*;

import java.util.List;

/**
 * <p>
 * Created by thomass on 09.04.21.
 */

public class InventorySystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(InventorySystem.class);

    boolean inventorysystemdebuglog = true;
    // Inventory display for main user
    /*ControlPanel*/ MazeInventory userInventory;

    /**
     * Only once for main player.
     */
    public InventorySystem(/*ControlPanel*/MazeInventory userInventory) {
        super(/*new String[]{InventoryComponent.TAG},*/ new RequestType[]{}, new EventType[]{
                EventRegistry.EVENT_ITEM_COLLECTED, EventRegistry.EVENT_BULLET_FIRED});
        this.userInventory = userInventory;
    }

    @Override
    public void init() {
        //3.5.21 userInventory.setSectionText(0, "-");
        userInventory.setBullets(0);
    }

    /**
     * Only once for main player.
     */
    @Override
    public void update(EcsEntity entity, EcsGroup group, double tpf) {
      /*  InventoryComponent ic = InventoryComponent.getInventoryComponent(entity);
        if (ic.needsRefresh) {
            ic.needsRefresh = false;
            //TODO only for THIS user
            userInventory.setSectionText(0, "" + ic.bulletCount);
        }*/
        if (entity == null && MazeUtils.getMainPlayer() != null) {
            List<EcsEntity> items = MazeUtils.getInventory(MazeUtils.getMainPlayer());
            List<EcsEntity> bullets = MazeUtils.getBullets(MazeUtils.getMainPlayer());
            userInventory.setBullets(bullets.size());

        }
    }

    @Override
    public void process(Event evt) {
        if (inventorysystemdebuglog) {
            logger.debug("got event " + evt.getType());
        }
        if (evt.isType(EventRegistry.EVENT_ITEM_COLLECTED)) {
            //String playername = (String) request.getPayloadByIndex(0);
            //EcsEntity player = MazeUtils.getMainPlayer();
            //MoverComponent mv = MoverComponent.getMoverComponent(player);


        }
    }
}
