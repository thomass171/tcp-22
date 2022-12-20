package de.yard.threed.engine.gui;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.Material;
import de.yard.threed.core.Color;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.DimensionF;
import de.yard.threed.engine.Texture;

public class ControlPanelHelper {
    static Log logger = Platform.getInstance().getLog(ControlPanelHelper.class);

    /**
     * Build the backplane for an inventory panel located in the lower right area and attach it to the camera.
     * The camera should be a 'deferred' camera to have the inventory always visible.
     * <p>
     * The size is defined in pixel.
     * <p>
     *
     * @param camera
     * @param screenDimensionInPixel
     * @return
     */
    public static ControlPanel buildInventoryForDeferredCamera(Camera camera, Dimension screenDimensionInPixel, Color basecolor, Dimension inventorySizeInPixel) {
        Material mat = Material.buildBasicMaterial(basecolor, true);
        // Shouldn't be a fix value but derived from the cameras near plane. And keep a distance for backplane
        // elements.
        double zpos = camera.getNear() + 0.1;

        DimensionF worldPlaneSize = camera.getPlaneSize(zpos);
        logger.debug("worldPlaneSize=" + worldPlaneSize+ " for zpos "+ zpos);

        DimensionF worldBackplaneSize = buildDimensionByPixel(worldPlaneSize, screenDimensionInPixel, inventorySizeInPixel);
        logger.debug("worldBackplaneSize=" + worldBackplaneSize);
        if (worldBackplaneSize == null) {
            // headless?
            return null;
        }
        // zoffset needs to be lower than 0.01 to have no visual gap between the backplane and the component
        ControlPanel inventory = new ControlPanel(worldBackplaneSize, mat, 0.001);
        // move it to the lower right screen corner.
        // zpos is negative because in the OpenGL camera space the z axis of the frustum runs into the negative part.
        inventory.getTransform().setPosition(new Vector3(worldPlaneSize.width / 2 - worldBackplaneSize.getWidth() / 2,
                -worldPlaneSize.height / 2 + worldBackplaneSize.getHeight() / 2, -zpos));

        camera.getCarrier().attach(inventory);
        inventory.getTransform().setLayer(camera.getLayer());
        return inventory;
    }

    /**
     * a panel located centered at top screen/display/window border (used for example for a banner)
     */
    public static ControlPanel buildForNearplaneBanner(Camera camera, Dimension screenDimensionInPixel, Color basecolor) {
        Material mat = Material.buildBasicMaterial(basecolor, true);

        DimensionF nearPlaneSize = camera.getNearplaneSize();
        logger.debug("nearPlaneSize=" + nearPlaneSize);

        Dimension bannerSizeInPixel = new Dimension(300, 20);
        DimensionF worldBackplaneSize = buildDimensionByPixel(nearPlaneSize, screenDimensionInPixel, bannerSizeInPixel);
        logger.debug("worldBackplaneSize=" + worldBackplaneSize);
        if (worldBackplaneSize == null) {
            // headless?
            return null;
        }
        ControlPanel panel = new ControlPanel(worldBackplaneSize, mat, 0.000001);
        // move it to the top mid
        // avoid z-fighting on near. TODO: again: why negating? Because near is just a (positive) distance, no coordinate?
        double near = camera.getNear();
        panel.getTransform().setPosition(new Vector3(0, nearPlaneSize.height / 2 - worldBackplaneSize.getHeight() / 2, -(near + 0.0001)));

        camera.getCarrier().attach(panel);
        panel.getTransform().setLayer(camera.getLayer());
        return panel;
    }

    /**
     * A simple column grid as menu.
     *
     * @param dimension
     * @param zpos       should be a negative value
     * @param buttonzpos unclear
     * @param menuitems
     * @param basecolor
     * @return
     */
    public static ControlPanel buildSingleColumnFromMenuitems(DimensionF dimension, double zpos, double buttonzpos, MenuItem[] menuitems, Color basecolor) {
        Material mat = Material.buildBasicMaterial(basecolor, true);

        double elementWidth = dimension.getWidth();
        double rowHeight = dimension.getHeight() / menuitems.length;

        DimensionF worldBackplaneSize = dimension;
        logger.debug("worldBackplaneSize=" + worldBackplaneSize);

        ControlPanel controlPanel = new ControlPanel(worldBackplaneSize, mat, buttonzpos);
        // locate it to the screen center
        controlPanel.getTransform().setPosition(new Vector3(0, 0, zpos));

        PanelGrid panelGrid = new PanelGrid(elementWidth, rowHeight, menuitems.length, new double[]{elementWidth});

        TextTexture textTexture = new TextTexture(Color.LIGHTGRAY);
        // y starts at bottom
        for (int i = 0; i < menuitems.length; i++) {
            Texture texture;
            if (menuitems[i].text != null) {
                texture = textTexture.getTextureForText(menuitems[i].text, Color.RED);
            } else {
                texture = menuitems[i].guiTexture.getTexture();
            }
            controlPanel.addArea(panelGrid.getPosition(0, menuitems.length - i - 1), new DimensionF(elementWidth, rowHeight),
                    menuitems[i].buttonDelegate).setTexture(texture);
        }
        return controlPanel;
    }

    public static void addText(ControlPanel cp, String text, Vector2 pos, DimensionF size) {
        TextTexture textTexture = new TextTexture(Color.LIGHTGRAY);
        ControlPanelArea textArea = cp.addArea(pos, size, null);
        textArea.setTexture(textTexture.getTextureForText(text, Color.RED));
    }

    /**
     * @return
     */
    public static DimensionF buildDimensionByPixel(DimensionF planeSize, Dimension dimension, Dimension expectedSizeInPixel) {
        if (dimension == null) {
            // headless?
            return null;
        }
        double wfactor = (double) expectedSizeInPixel.getWidth() / dimension.getWidth();
        double hfactor = (double) expectedSizeInPixel.getHeight() / dimension.getHeight();
        return new DimensionF(planeSize.width * wfactor, planeSize.height * hfactor);
    }
}
