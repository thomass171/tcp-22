package de.yard.threed.maze;

import de.yard.threed.engine.ecs.EcsEntity;

public class DiamondComponent extends ItemComponent {

    static String TAG = "DiamondComponent";

    public DiamondComponent() {
        super();
    }

    public DiamondComponent(int owner) {
        super(owner);
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public static DiamondComponent getDiamondComponent(EcsEntity e) {
        DiamondComponent m = (DiamondComponent) e.getComponent(DiamondComponent.TAG);
        return m;
    }
}
