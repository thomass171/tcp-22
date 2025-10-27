package de.yard.threed.engine.gui;


import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.core.platform.Log;

/**
 * Helper for open,close,cycle menus (MenuProvider) on 'M'.
 * Not single items but complete menus.
 */
public class MenuCycler {
    Log logger = Platform.getInstance().getLog(MenuCycler.class);
    MenuProvider[] menuBuilders;
    Menu menu = null;
    int index = 0;

    public MenuCycler(MenuProvider[] menuBuilders) {
        this.menuBuilders = menuBuilders;
    }

    /**
     * Returns true if the mouseclick was consumed by a click.
     * 21.2.25 only consider pure 'm', not shifted.
     */
    public boolean update(Point mouseClickLocation) {
        if (Input.getKeyDown(KeyCode.M) && !Input.getKey(KeyCode.Shift)) {
            //logger.debug("m key was pressed. currentdelta=" + tpf);
            cycle();
        }

        //rechts macht irgendwas (z.B.markiert rote Box) und auch menu.

        //eine menuausführung koennte 'menu' auf null setzen. Darum ist das nicht unten im Block
        /*27.10.25 not sure whether selectionbykey was an option ever? if (Input.getKeyDown(KeyCode.Alpha3) && menu!=null) {
            logger.debug("3 key was pressed. ");
            menu.checkForSelectionByKey(3);
        }
        if (Input.getKeyDown(KeyCode.Alpha2)&& menu!=null) {
            logger.debug("2 key was pressed. ");
            menu.checkForSelectionByKey(2);
        }*/

        boolean consumed = false;
        if (menu != null) {

            Vector3 menuWorldPos = menu.getNode().getTransform().getWorldModelMatrix().extractPosition();
            if (Input.getControllerButtonDown(10)) {
                logger.debug(" found controller button down 10 (right)");
                //VRController controller = menu.getController1();

                //if (controller != null && menu != null) {
                //logger.debug("che")
                Ray ray;// = controller.getRay();
                ray = menuBuilders[index].getRayForUserClick(null);
                logger.debug("menu for VR (menuWorldPos=" + menuWorldPos + ") picking ray isType " + ray);
                checkForClick(ray);
            }
            if (mouseClickLocation != null) {
                Ray pickingray = menuBuilders[index].getRayForUserClick(mouseClickLocation);
                logger.debug("menu for mouse click (menuWorldPos=" + menuWorldPos + ") picking ray isType " + pickingray);
                consumed = checkForClick(pickingray);
            }
        }
        return consumed;
    }

    /**
     *
     */
    private boolean checkForClick(Ray pickingray) {
        return menu.checkForClickedArea(pickingray);
    }

    public void cycle() {
        if (menu == null) {
            menu = menuBuilders[index].buildMenu(Scene.getCurrent().getDefaultCamera());
            //27.1.22 menuBuilders[index].getAttachNode().attach(menu.getNode());
            menu.getNode().getTransform().setParent(menuBuilders[index].getAttachNode());
            menuBuilders[index].menuBuilt();
        } else {
            close();

        }
    }

    /**
     * programmatic close
     */
    public void close() {
        if (menu != null) {
            menu.remove();
            menu = null;
            index++;
            if (index >= menuBuilders.length) {
                index = 0;
            }
        }
    }
}

