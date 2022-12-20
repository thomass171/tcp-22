package de.yard.threed.engine.gui;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.engine.geometry.Primitives;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.DimensionF;
import de.yard.threed.engine.platform.common.SimpleGeometry;

import java.util.ArrayList;
import java.util.List;

/**
 * Just a flat panel with components (like buttons, textareas) added. Can be used for user menus, inventory, control panel aso.
 * To be attached to either a (deferred) camera, traditional on a near plane, or in VR at a controller.
 * <p>
 * A more sophisticated concept than FovElementPlane (and Hud/GuiGrid, which is similar). And also compared to menus.
 * In general just a plane for placing thing like
 * <p>
 * - clickable areas (buttons, menu items)
 * - status display (Inventory)
 * - Text
 * <p>
 * And some parts can be selected by click/ray wählbar/triggerbar. Maybe even stackable(??).
 *
 * <p>
 * For inventory its a 300x20 Pixel area right at the bottom (ControlPanelHelper.buildInventoryForDeferredCamera).
 * <p>
 * Different from Guigrid based on FovElement, transparency might be cascading and thus be eye-catching?
 * <p>
 * 28.4.21
 */
public class ControlPanel extends SceneNode implements GenericControlPanel {

    static Log logger = Platform.getInstance().getLog(ControlPanel.class);

    DimensionF planeSize;
    // offset for components to be raised above the back plane. A positive value. Needs to be related to where the panel is used. A low value (eg. 0.000001) for
    // near plane usage, but larger (eg 0.01) for world usage.
    // In the OpenGL camera space the z axis of the frustum runs into the negative part, so the zoffset here needs to be positive to be before the plane itself.
    private double zoffsetForComponents;
    List<ControlPanelArea> areas = new ArrayList<ControlPanelArea>();
    List<ControlPanel> subPanel = new ArrayList<ControlPanel>();

    /**
     * Just a backplane.
     */
    public ControlPanel(DimensionF planeSize, Material backplaneMaterial, double zoffsetForComponents) {
        this.zoffsetForComponents = zoffsetForComponents;
        SimpleGeometry geo = Primitives.buildSimpleXYPlaneGeometry(planeSize.width, planeSize.height, new ProportionalUvMap());

        Mesh mesh = new Mesh(geo, backplaneMaterial);
        setMesh(mesh);
        setName("ControlPanel-Backplane");

        this.planeSize = planeSize;
    }

    public ControlPanelArea addArea(Vector2 position, DimensionF size, ButtonDelegate buttonDelegate) {
        return addArea(null, position, size, buttonDelegate);
    }

    public ControlPanelArea addArea(String name, Vector2 position, DimensionF size, ButtonDelegate buttonDelegate) {
        ControlPanelArea cpa = new ControlPanelArea(size, buttonDelegate);
        attach(cpa);
        if (StringUtils.empty(name)) {
            cpa.setName("ControlPanel-Area");
        } else {
            cpa.setName(name);
        }
        cpa.getTransform().setPosition(new Vector3(position.getX(), position.getY(), zoffsetForComponents));
        areas.add(cpa);
        return cpa;
    }

    /**
     * "position" is center relative
     */
    public void add(Vector2 position, ControlPanel controlPanel) {
        attach(controlPanel);
        controlPanel.getTransform().setPosition(new Vector3(position.getX(), position.getY(), zoffsetForComponents));
        subPanel.add(controlPanel);
    }

    /**
     * Actions are triggered by delegates
     */
    @Override
    public boolean checkForClickedArea(Ray pickingray) {
        //logger.debug("guigrid picking ray isType " + pickingray);

        ControlPanelArea area = getClickedArea(pickingray);
        if (area != null) {
            //command might be null
            logger.debug("area clicked ");
            if (area.buttonDelegate != null) {
                area.buttonDelegate.buttonpressed();
            }
            return true;
        }
        // Also check sub control panel
        for (ControlPanel cp : subPanel) {
            if (cp.checkForClickedArea(pickingray)) {
                return true;
            }
        }
        return false;
    }

    private ControlPanelArea getClickedArea(Ray pickingray) {
        if (pickingray == null) {
            logger.warn("pickingray is null");
            return null;
        }
        for (ControlPanelArea bn : areas) {
            List<NativeCollision> intersects = pickingray.getIntersections(bn, true);
            if (intersects.size() > 0) {
                return bn;
            }
        }
        return null;
    }

    public DimensionF getSize() {
        //logger.debug("planeSize="+planeSize);
        return planeSize;
    }
}
