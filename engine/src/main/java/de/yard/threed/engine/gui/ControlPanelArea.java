package de.yard.threed.engine.gui;

import de.yard.threed.engine.*;
import de.yard.threed.engine.geometry.Primitives;
import de.yard.threed.core.DimensionF;
import de.yard.threed.engine.platform.common.SimpleGeometry;

public class ControlPanelArea extends SceneNode {
    DimensionF size;
    ButtonDelegate buttonDelegate;

    public ControlPanelArea(DimensionF size, ButtonDelegate buttonDelegate) {
        this.size = size;
        this.buttonDelegate = buttonDelegate;

        // move to camera a little to avoid z-fighting. For testing y can be -0.8. But why -0.2?
        /*section.*///getTransform().setPosition(new Vector3(0, -0.2, 0.1f));

    }

    /**
     * Primary use is displaying text.
     * Always creates new mesh/material. For updating use updateText().
     */
    public void setTexture(Texture texture) {
        Material mat = Material.buildBasicMaterial(texture, null, true);

        // use height of plane and width by text len??
        SimpleGeometry geo = Primitives.buildSimpleXYPlaneGeometry(size.width, size.height, new ProportionalUvMap());
        setMesh(new Mesh(geo, mat));
    }

    public void updateTexture(Texture text) {
        //TODO really update material
        setTexture(text);
    }

    /**
     * Weil sich auch UVs aendern koennen, das ganze Element neu machen.
     * TODO: same here: how to update
     *
     * @param icon
     */
    public void setIcon(GuiTexture icon) {
        //this.icon = icon;

        //this.texture = icon.getTexture();
        //this.uvmap = icon.getUvMap();
        //SceneNode.removeSceneNode(element);
        //element = null;

        SimpleGeometry geo = Primitives.buildSimpleXYPlaneGeometry(size.width, size.height, icon.getUvMap());

        setMesh(new Mesh(geo, Material.buildBasicMaterial(icon.getTexture())));


    }
}
