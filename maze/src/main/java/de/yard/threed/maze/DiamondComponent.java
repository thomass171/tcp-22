package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;
import de.yard.threed.engine.ecs.EcsHelper;

public class DiamondComponent extends ItemComponent {

    static String TAG = "DiamondComponent";

    public DiamondComponent() {
        super();
    }

    public DiamondComponent(int owner) {
        super(owner);
    }

    public DiamondComponent(Point b) {
        super();
        setLocation(b);
        setOwner(-1);
        setNeededForSolving();
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static DiamondComponent getDiamondComponent(EcsEntity e) {
        DiamondComponent m = (DiamondComponent) e.getComponent(DiamondComponent.TAG);
        return m;
    }

    @Override
    public void collectedBy(int collector) {
        super.collectedBy(collector);
    }
}
