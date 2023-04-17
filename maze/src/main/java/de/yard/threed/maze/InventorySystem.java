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

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Created by thomass on 09.04.21.
 */

public class InventorySystem extends DefaultEcsSystem {
    private static Log logger = Platform.getInstance().getLog(InventorySystem.class);

    public static EventType EVENT_ITEM_COLLECTED = EventType.register(2000, "EVENT_ITEM_COLLECTED");

    boolean inventorysystemdebuglog = true;
    // Inventory display for main user
    List<MazeInventory> userInventoryList = new ArrayList<MazeInventory>();

    /**
     * Only once for main player.
     */
    public InventorySystem() {
        super(new RequestType[]{}, new EventType[]{
                EVENT_ITEM_COLLECTED, MazeEventRegistry.EVENT_BULLET_FIRED});
    }

    @Override
    public void init() {
        //3.5.21 userInventory.setSectionText(0, "-");
        for (MazeInventory userInventory : userInventoryList) {
            userInventory.setBullets(0);
            userInventory.setDiamonds(0);
        }
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
            for (MazeInventory userInventory : userInventoryList) {
                userInventory.setBullets(bullets.size());
                userInventory.setDiamonds(diamonds.size());
            }
        }
    }

    @Override
    public void process(Event evt) {
        if (inventorysystemdebuglog) {
            logger.debug("got event " + evt.getType());
        }
        if (evt.isType(EVENT_ITEM_COLLECTED)) {
            int userEntityId = (int) evt.getPayloadByIndex(0);
            List<EcsEntity> diamonds = MazeUtils.getDiamonds(userEntityId);
            List<EcsEntity> bullets = MazeUtils.getBullets(MazeUtils.getMainPlayer());
            for (MazeInventory userInventory : userInventoryList) {
                userInventory.setBullets(bullets.size());
                userInventory.setDiamonds(diamonds.size());
            }
        }
    }

    public static Event buildItemCollectedEvent(int userEntityId, int itemId) {
        return new Event(EVENT_ITEM_COLLECTED, new Payload(new Integer(userEntityId), new Integer(itemId)));
    }

    public void addInventory(MazeInventory userInventory) {
        userInventoryList.add(userInventory);
    }
}
