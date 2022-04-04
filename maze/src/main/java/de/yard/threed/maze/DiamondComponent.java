package de.yard.threed.maze;

import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.ecs.EcsEntity;

public class DiamondComponent extends ItemComponent {

    static String TAG = "DiamondComponent";

    public DiamondComponent(Point b) {
        super(b);
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
