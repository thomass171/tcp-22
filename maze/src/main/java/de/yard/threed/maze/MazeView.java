package de.yard.threed.maze;


import de.yard.threed.core.Degree;
import de.yard.threed.core.Point;
import de.yard.threed.core.Quaternion;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.avatar.AvatarSystem;
import de.yard.threed.engine.ecs.EcsEntity;

/**
 * Die Darstellung eines Maze.
 * 30.10.20: Es gibt jetzt auch MazeVisualizationSystem.
 * 9.4.21: Ist das nicht ein Mischmasch aus State (Boxes) und Visualization?
 * <p>
 * Created by thomass on 06.03.17.
 */
public class MazeView {

    public MazeTerrain terrain;

    public void remove() {
        SceneNode.removeSceneNode(terrain);

        terrain = null;
    }

    /**
     * Liefert die Box an einer bestimmten Position.
     * 3.3.17: Das ist vielleicht ein schon ganz ordentliches Mapping Model->View.
     *
     * @param p
     * @return
     */
    /*24.4.21 public  EcsEntity getBox(Point p) {
        Vector3 pin3d = MazeDimensions.getWorldElementCoordinates(p.getX(), p.getY());
        for (EcsEntity e : MazeUtils.getBoxes()) {
            if (e.scenenode instanceof SokobanBox) {
                //TODO Darf nicht moven, sonst stimmen die Koords nicht

                //Point boxpos = ((SokobanBox) e.scenenode).getGridPosition();
                //if (p.equals(boxpos)) {
                if (e.scenenode.getTransform().getPosition().subtract(pin3d).length() < 0.001f) {
                    //logger.debug("found Entity at " + p);
                    return e;
                }
            }
        }
        return null;
    }*/

    /*13.4.21 public void visualizeState(GridState state) {
        setRayPosition(state.playerposition);
        setRayRotation(state.playerorientation.getYaw());
        // Es gibt keine fests Zuordnung der Boxes zu ihrer Visualisierung
        for (int i = 0; i < state.boxes.size(); i++) {
            Point b = state.boxes.get(i);
            MazeUtils.getBoxes().get(i).scenenode.getTransform().setPosition(MazeDimensions.getWorldElementCoordinates(b.getX(), b.getY()));
        }
    }*/

    void setRayPosition(Point position) {
        Vector3 effectivestartposition = MazeDimensions.getWorldElementCoordinates(position.getX(), position.getY());
        //Ray Oberkante zum Test genau auf Pillaroberkante mit rayy = Pillar.HEIGHT - 0.15f
        float rayy = 0.6f;
        effectivestartposition = new Vector3(effectivestartposition.getX(), rayy, effectivestartposition.getZ());
        EcsEntity ray = AvatarSystem.getAvatar().avatarE;
        ray.scenenode.getTransform().setPosition(effectivestartposition);

    }

    void setRayRotation(Degree yaw) {
        EcsEntity ray = AvatarSystem.getAvatar().avatarE;
        Quaternion q =  Quaternion.buildFromAngles(new Degree(0), yaw, new Degree(0));
        ray.scenenode.getTransform().setRotation(q);
        MoverComponent mover = ((MoverComponent) ray.getComponent(MoverComponent.TAG));
        mover.setYaw(yaw);
    }

    public boolean isMoving() {
        if (AvatarSystem.getAvatar()==null){
            return false;
        }
        EcsEntity ray = AvatarSystem.getAvatar().avatarE;

        MoverComponent mover = ((MoverComponent) ray.getComponent(MoverComponent.TAG));
        if (mover==null){
            //1.4.21 wann passiert das denn?
            return false;
        }
        if (mover.isMoving()) {
            return true;
        }
        for (EcsEntity e : MazeUtils.getBoxes()) {
            mover = ((MoverComponent) e.components.get(0));
            if (mover.isMoving()) {
                return true;
            }
        }
        return false;
    }
}
