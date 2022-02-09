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

public class ControlPanelHelper {
    static Log logger = Platform.getInstance().getLog(ControlPanelHelper.class);

    static Dimension inventorySizeInPixel = new Dimension(300, 20);

    /**
     * Build the backplane for an inventory panel located in the lower right area and attach it to the camera.
     * <p>
     * Die Dimensionierung ergibt sich aus dem Ziel, das Inventory unten im unteren rechten drittel darzustellen.
     * Aber dann ist xy-Skalierung doof. Oder ausgehend von Requirement 300x20 Pixel. Hmm
     *
     * @param camera
     * @param screenDimensionInPixel
     * @return
     */
    public static ControlPanel buildInventoryForDeferredCamera(Camera camera, Dimension screenDimensionInPixel, Color basecolor) {
        Material mat = Material.buildBasicMaterial(basecolor, /*Effect.buildUniversalEffect()*/ true);
        double zpos = 4;

        DimensionF worldPlaneSize = camera.getPlaneSize(zpos);
        logger.debug("worldPlaneSize=" + worldPlaneSize);

        DimensionF worldBackplaneSize = buildDimensionByPixel(worldPlaneSize, screenDimensionInPixel, inventorySizeInPixel);
        logger.debug("worldBackplaneSize=" + worldBackplaneSize);
        if (worldBackplaneSize == null) {
            // headless?
            return null;
        }
        //ControlPanel inventory = new ControlPanel(FovElementPlane.buildFovElementPlane(null, worldBackplaneSize, mat), worldBackplaneSize, basecolor);
        ControlPanel inventory = new ControlPanel(worldBackplaneSize, mat);
        // move it to the lower right screen corner. TODO check: Why is zpos negated?
        inventory.getTransform().setPosition(new Vector3(worldPlaneSize.width / 2 - worldBackplaneSize.getWidth() / 2,
                -worldPlaneSize.height / 2 + worldBackplaneSize.getHeight() / 2, -zpos));

        camera.getCarrier().attach(inventory);
        inventory.getTransform().setLayer(camera.getLayer());
        return inventory;
    }

    /**
     * A simple column grid as menu.
     *
     * @param dimension
     * @param zpos
     * @param buttonzpos
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

        ControlPanel controlPanel = new ControlPanel(worldBackplaneSize, mat);
        // locate it to the screen center
        controlPanel.getTransform().setPosition(new Vector3(0, 0, zpos));

        // start at top
        double h2 = rowHeight / 2;
        double startY = 0;
        if (menuitems.length > 2) {
            startY = ((menuitems.length - 1) / 2) * rowHeight;
        }
        if (menuitems.length % 2 == 0) {
            startY += h2;
        }

        TextTexture textTexture = new TextTexture(Color.LIGHTGRAY);
        for (int i = 0; i < menuitems.length; i++) {
            controlPanel.addArea(new Vector2(0, startY - i * rowHeight), new DimensionF(elementWidth, rowHeight),
                    menuitems[i].buttonDelegate).setTexture(textTexture.getTextureForText(menuitems[i].text));
        }
        return controlPanel;
    }

    /**
     * @return
     */
    private static DimensionF buildDimensionByPixel(DimensionF planeSize, Dimension dimension, Dimension expectedSizeInPixel) {
        if (dimension == null) {
            // headless?
            return null;
        }
        double wfactor = (double) expectedSizeInPixel.getWidth() / dimension.getWidth();
        double hfactor = (double) expectedSizeInPixel.getHeight() / dimension.getHeight();
        return new DimensionF(planeSize.width * wfactor, planeSize.height * hfactor);
    }
}
