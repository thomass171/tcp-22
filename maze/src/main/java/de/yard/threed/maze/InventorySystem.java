package de.yard.threed.maze;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Payload;
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

    public static EventType EVENT_ITEM_COLLECTED = new EventType("EVENT_ITEM_COLLECTED");

    boolean inventorysystemdebuglog = true;
    // Inventory display for main user
    /*ControlPanel*/ MazeInventory userInventory;

    /**
     * Only once for main player.
     */
    public InventorySystem(/*ControlPanel*/MazeInventory userInventory) {
        super(/*new String[]{InventoryComponent.TAG},*/ new RequestType[]{}, new EventType[]{
                EVENT_ITEM_COLLECTED, EventRegistry.EVENT_BULLET_FIRED});
        this.userInventory = userInventory;
    }

    @Override
    public void init() {
        //3.5.21 userInventory.setSectionText(0, "-");
        userInventory.setBullets(0);
        userInventory.setDiamonds(0);
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
            // TODO: duplicate to event handling
            List<EcsEntity> diamonds = MazeUtils.getDiamonds(MazeUtils.getMainPlayer());
            List<EcsEntity> bullets = MazeUtils.getBullets(MazeUtils.getMainPlayer());
            userInventory.setBullets(bullets.size());
            userInventory.setDiamonds(diamonds.size());
        }
    }

    @Override
    public void process(Event evt) {
        if (inventorysystemdebuglog) {
            logger.debug("got event " + evt.getType());
        }
        if (evt.isType(EVENT_ITEM_COLLECTED)) {
            int userEntityId = (Integer) evt.getPayloadByIndex(0);
            List<EcsEntity> diamonds = MazeUtils.getDiamonds(userEntityId);
            List<EcsEntity> bullets = MazeUtils.getBullets(MazeUtils.getMainPlayer());
            userInventory.setBullets(bullets.size());
            userInventory.setDiamonds(diamonds.size());
        }
    }

    public static Event buildItemCollectedEvent(int userEntityId, int itemId) {
        return new Event(EVENT_ITEM_COLLECTED, new Payload(new Integer(userEntityId), new Integer(itemId)));
    }
}
