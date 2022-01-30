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
     * Build the backplane and attach it to the camera.
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
        if (worldBackplaneSize == null){
            // headless?
            return null;
        }
        //ControlPanel inventory = new ControlPanel(FovElementPlane.buildFovElementPlane(null, worldBackplaneSize, mat), worldBackplaneSize, basecolor);
        ControlPanel inventory = new ControlPanel( worldBackplaneSize, mat);
        // move it to the lower right screen corner
        inventory.getTransform().setPosition(new Vector3(worldPlaneSize.width / 2 - worldBackplaneSize.getWidth() / 2,
                -worldPlaneSize.height / 2 + worldBackplaneSize.getHeight() / 2, -zpos));

        camera.getCarrier().attach(inventory);
        inventory.getTransform().setLayer(camera.getLayer());
        return inventory;
    }

    /**
     * @return
     */
    private static DimensionF buildDimensionByPixel(DimensionF planeSize, Dimension dimension, Dimension expectedSizeInPixel) {
        if (dimension == null){
            // headless?
            return null;
        }
        double wfactor = (double) expectedSizeInPixel.getWidth() / dimension.getWidth();
        double hfactor = (double) expectedSizeInPixel.getHeight() / dimension.getHeight();
        return new DimensionF(planeSize.width * wfactor, planeSize.height * hfactor);
    }

    /**
     * Icon-value-icon
     *
     */
    public static ControlPanel buildPropertyControlPanel(DimensionF size, double margin, Material mat) {
        double m2 = 2 * margin;
        double h = size.height;
        double iconsize = size.height - 2 * margin;
        double iconareasize = size.height;
        double textareawidth = size.width - 2*iconareasize;
        ControlPanel cp = new ControlPanel(new DimensionF(size.width, size.height),mat);
        cp.addArea(new Vector2(- (textareawidth/2+iconareasize/2), 0), new DimensionF(iconsize, iconsize),() -> {
            logger.debug("left arrow clicked");
        }).setIcon(Icon.ICON_LEFTARROW);
        // text has no margin yet.
        TextTexture textTexture=new TextTexture(Color.LIGHTGRAY);
        cp.addArea(new Vector2(0, 0), new DimensionF(textareawidth,size.height),null).setTexture(textTexture.getTextureForText("961"));
        cp.addArea(new Vector2((textareawidth/2+iconareasize/2), 0), new DimensionF(iconsize, iconsize),() -> {
            logger.debug("right arrow clicked");
        }).setIcon(Icon.ICON_RIGHTARROW);
        return cp;
    }
}
