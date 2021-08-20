package de.yard.threed.maze;

import de.yard.threed.core.Vector3;
import de.yard.threed.engine.*;
import de.yard.threed.engine.geometry.Primitives;
import de.yard.threed.core.Color;
import de.yard.threed.engine.platform.common.SimpleGeometry;

//TODO merge with MazeModelFactory?
public class MazeModelBuilder {
    /**
     * Created by thomass on 30.11.15.
     */

    public static SceneNode buildSokobanBox(/*int x, int y*/) {
        //mover = new Mover(this.object3d);

        SceneNode container = new SceneNode();
        SceneNode mesh = new SceneNode(new Mesh(MazeSettings.getSettings().sokobanboxgeo, MazeSettings.getSettings().sokobanboxmaterial));
        // Das Mesh halb nach oben
        mesh.getTransform().setPosition(new Vector3(0, MazeSettings.getSettings().sokobanboxsize / 2, 0));
        // Und das ganze Model auf dir Gridposition
        //Vector3 pos = MazeDimensions.getWorldElementCoordinates(x, y);
        //24.4.21  getTransform().setPosition(pos);
        container.attach(mesh);
        return container;

    }

    public static SceneNode buildSimpleBody(float height, float diameter, Color color) {
        Geometry cuboid = Geometry.buildCube(diameter, height, diameter);
        Mesh m = new Mesh(cuboid, Material.buildBasicMaterial(color));
        return new SceneNode(m);
    }

    public static SceneNode buildDiamond() {
        double size = MazeSettings.getSettings().sokobanboxsize / 2;
        Geometry cuboid = Geometry.buildCube(size, size, size);
        Mesh m = new Mesh(cuboid, Material.buildBasicMaterial(Color.LIGHTBLUE));
        return new SceneNode(m);
    }

    public static SceneNode buildSimpleBall(double radius, Color color/*, Point position*/) {
        SimpleGeometry sphere = Primitives.buildSphereGeometry(radius, 32, 32);
        Mesh m = new Mesh(sphere, Material.buildBasicMaterial(color));
        SceneNode ball = new SceneNode(m);
        ball.setName("bullet");
        //ball.getTransform().setPosition(MazeUtils.point2Vector3(position).add(new Vector3(0, 1.25, 0)));
        Scene.getCurrent().addToWorld(ball);
        return ball;
    }
}
