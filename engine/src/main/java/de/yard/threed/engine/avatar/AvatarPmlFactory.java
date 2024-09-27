package de.yard.threed.engine.avatar;

import de.yard.threed.core.Color;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.geometry.Shape;
import de.yard.threed.core.loader.PmlFactory;
import de.yard.threed.engine.apps.WoodenToyPmlFactory;
import de.yard.threed.core.geometry.Primitives;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.geometry.SimpleGeometry;

/**
 * Factory for building avatar models as PML.
 * <p>
 * PML can easily be saved as GLTF and used to build the 3D model.
 * <p>
 * Created by thomass on 10.01.22.
 */
public class AvatarPmlFactory {

    /**
     * Simple A-like avatar. Body runs along z-axis(?) facing up (+y)(?)
     * The total height is appx. 0.8 + 0.2.
     * Only specific colors are allowed (due to faces needed): "red","blue","green","darkgreen"
     */
    public static PortableModel/*List*/ buildAvatarA(String color) {
        double headRadius = 0.20;

        double bottomRadius = 0.4f;
        double topRadius = 0.1;
        double baseHeight = 0.8;

        PortableMaterial mainMaterial = new PortableMaterial("mainMaterial", Color.parseString(color));
        PortableMaterial faceMaterial = new PortableMaterial("faceMaterial", "data:textures/Face-" + color + ".png");
        WoodenToyPmlFactory tbf = new WoodenToyPmlFactory();

        PortableModelDefinition body = buildBody(baseHeight, topRadius, bottomRadius, "mainMaterial");
        PortableModelDefinition head = buildHead(headRadius, "faceMaterial");

        head.setPosition(new Vector3(0, baseHeight / 2 + headRadius - 0.1, 0));
        body.attach(head);

        PortableModel pml = new PortableModel(body, null);
        //pml.addModel(body);
        pml.addMaterial(mainMaterial);
        pml.addMaterial(faceMaterial);
        return pml;
    }

    public static PortableModelDefinition buildBody(double height, double topRadius, double bottomRadius, String matname) {
        Shape shape = new Shape(false);
        double height2 = height / 2;
        shape.addPoint(new Vector2(0, height2));
        shape.addPoint(new Vector2(topRadius, height2), true);
        shape.addPoint(new Vector2(bottomRadius, -height2), true);
        shape.addPoint(new Vector2(0, -height2), false);
        ShapeGeometry geo = ShapeGeometry.buildByCircleRotation(shape, 64);
        PortableModelDefinition e = de.yard.threed.engine.loader.PmlFactory.buildElement(geo, matname);
        e.setName("Body");
        return e;
    }

    public static PortableModelDefinition buildHead(double radius, String matname) {
        SimpleGeometry sphere = Primitives.buildSphereGeometry(radius, 64, 64);
        PortableModelDefinition e = PmlFactory.buildElement(sphere, matname);
        e.setName("Head");
        return e;
    }
}
