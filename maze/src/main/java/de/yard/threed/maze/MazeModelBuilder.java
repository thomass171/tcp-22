package de.yard.threed.maze;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.*;
import de.yard.threed.engine.avatar.AvatarPmlFactory;
import de.yard.threed.engine.geometry.Primitives;
import de.yard.threed.core.Color;
import de.yard.threed.engine.loader.PortableMaterial;
import de.yard.threed.engine.loader.PortableModelDefinition;
import de.yard.threed.engine.loader.PortableModelList;
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
        // Not too much transparency (0xCC)
        Mesh m = new Mesh(cuboid, Material.buildBasicMaterial(MazeSettings.diamondColor.transparency(0xCC), true));
        SceneNode diamond = new SceneNode(m);
        diamond.getTransform().setRotation(Quaternion.buildFromAngles(new Degree(45), new Degree(45), new Degree(45)));
        return new SceneNode(diamond);
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

    /**
     * Just a sphere. Has no elevation above anything, so needs to be raised.
     *
     * @return
     */
    public static SceneNode buildMonster() {

        double headRadius = 0.20;

        PortableMaterial faceMaterial = new PortableMaterial("faceMaterial", "maze:textures/Face-Monster.png");

        PortableModelDefinition head = AvatarPmlFactory.buildHead(headRadius, "faceMaterial");

        PortableModelList pml = new PortableModelList(null);
        pml.addModel(head);
        pml.addMaterial(faceMaterial);

        SceneNode model = pml.buildModel(null, null);
        model.setName("Monster");
        return new SceneNode(model);
    }

}
