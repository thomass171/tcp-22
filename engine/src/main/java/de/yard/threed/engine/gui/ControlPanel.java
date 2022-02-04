package de.yard.threed.engine.gui;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mit deferred camera, klassisch an near plane, in VR am Controller oder einfach eine "Schalttafel".
 * <p>
 * Mal als ausgereiftereren Alternativentwurf zu FovElementPlane (und Hud/GuiGrid, denn das ist doch sehr ähnlich). Und auch zu allen Menus.
 * War mal Inventory, aber ControlPanel trifft es viel besser. Allgemein erstmal nur eine Plane, auf der
 * <p>
 * - clickable areas (buttons, menu items)
 * - Stratusanzeigen (Inventory)
 * - Text
 * <p>
 * angezeigt werden. Und mannche Teile sind per click/ray wählbar/triggerbar. Und das ganze stackable(??).
 *
 * <p>
 * Bietet als HUD eine 300x20 Pixel area unten rechts (ControlPanelHelper.buildInventoryForDeferredCamera).
 * <p>
 * Weil anders als bei Hud kein ständig geändertes Overlayimage verwendet wird, ist Transparenz kaskadierend und damit optisch auffällig.
 * <p>
 * 28.4.21
 */
public class ControlPanel extends SceneNode implements GenericControlPanel {

    static Log logger = Platform.getInstance().getLog(ControlPanel.class);

    // blauer Hintergrund, der durch Transparenz blasser erscheint. Vielleicht eher im Maze L&F. Naja, mal sehn. die passt gut
    //Color basecolor = new Color(204, 230, 255, 128);
    //Color basecolor = new Color(128, 193, 255, 128);
    //static Color basecolor = new Color(255, 217, 102, 128);

    // Color basecolor;
    Map<Integer, SceneNode> nodeByIndex = new HashMap<Integer, SceneNode>();
    //SceneNode backplane;
    DimensionF planeSize;
    private double zoffset = 0.01f;
    List<ControlPanelArea> areas = new ArrayList<ControlPanelArea>();
    List<ControlPanel> subPanel = new ArrayList<ControlPanel>();


    /*private ControlPanel(SceneNode backplane, DimensionF worldBackplaneSize, Color basecolor) {
        this.backplane = backplane;
        this.worldBackplaneSize = worldBackplaneSize;
        this.basecolor = basecolor;
    }*/

    /**
     * Just a backplane.
     */
    public ControlPanel(DimensionF planeSize, Material mat) {
        SimpleGeometry geo = Primitives.buildSimpleXYPlaneGeometry(planeSize.width, planeSize.height,new ProportionalUvMap());

        Mesh mesh = new Mesh(geo, mat);
        setMesh(mesh);
        setName("Backplane");

        this.planeSize = planeSize;
    }

    public ControlPanelArea addArea(Vector2 position, DimensionF size, ButtonDelegate buttonDelegate) {
        ControlPanelArea cpa = new ControlPanelArea(size,buttonDelegate);
        attach(cpa);
        cpa.getTransform().setPosition(new Vector3(position.getX(), position.getY(), zoffset));
        areas.add(cpa);
        return cpa;
    }

    /**
     * "position" is center relative
     */
    public void add(Vector2 position, ControlPanel controlPanel) {
        attach(controlPanel);
        controlPanel.getTransform().setPosition(new Vector3(position.getX(), position.getY(), zoffset));
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
        for (ControlPanel cp:subPanel){
            if (cp.checkForClickedArea(pickingray)){
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
            List<NativeCollision> intersects = pickingray.getIntersections(bn);
            if (intersects.size() > 0) {
                return bn;
            }
        }
        return null;
    }

    public DimensionF getSize() {
        return planeSize;
    }
}
