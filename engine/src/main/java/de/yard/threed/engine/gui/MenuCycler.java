package de.yard.threed.engine.gui;


import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.core.platform.Log;

/**
 * Helper for open,close,cycle menus on 'M'.
 * Not single items but complete menus.
 */
public class MenuCycler {
    Log logger = Platform.getInstance().getLog(MenuCycler.class);
    MenuProvider[] menuBuilders;
    Menu menu = null;
    int index = 0;
    //Camera cameraOfMenu;


    public MenuCycler(MenuProvider[] menuBuilders) {
        this.menuBuilders = menuBuilders;
    }

    /**
     * Returns true if the mouseclick was consumed by a click.
     *
     */
    public boolean update(Point mouseClickLocation) {
        if (Input.GetKeyDown(KeyCode.M)) {
            //logger.debug("m key was pressed. currentdelta=" + tpf);
            cycle();
        }

        //linke Controller Button cycled
        //12.5.21 nicht mehr
        /*if (Input.getControllerButtonDown(0)) {
            logger.debug(" found controller button down 0 (left)");
            cycle();
        }*/
        //rechts macht irgendwas (z.B.markiert rote Box) und auch menu.

        //eine menuausführung koennte 'menu' auf null setzen. Darum ist das nicht unten im Block
        if (Input.GetKeyDown(KeyCode.Alpha3) && menu!=null) {
            logger.debug("3 key was pressed. ");
            menu.checkForSelectionByKey(3);
        }
        if (Input.GetKeyDown(KeyCode.Alpha2)&& menu!=null) {
            logger.debug("2 key was pressed. ");
            menu.checkForSelectionByKey(2);
        }

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
                //4.10.19 Generischer ueber Ray
                //Request command = menu.checkForClickedButton(mouselocation);
                //if (cameraOfMenu != null) {
                Ray pickingray;//= cameraOfMenu.buildPickingRay(mouselocation);
                pickingray = menuBuilders[index].getRayForUserClick(mouseClickLocation);
                logger.debug("menu for mouse click (menuWorldPos=" + menuWorldPos + ") picking ray isType " + pickingray);
                consumed = checkForClick(pickingray);
            }
        }
        return consumed;
    }

    /**
     * Es ist doch inkonsistent, hier (nur!) auf Close zu pruefen und andere Aktionen per Delegate zu machen.
     *
     * @param
     */
    private boolean checkForClick(Ray pickingray) {
        /*Request command =*/
        return menu.checkForClickedArea(pickingray);
        // 4.10.19 Pruefung auf close sollte/muss anders gehen
        /*31.12.19if (command != null && command.getType().getLabel() != null && command.getType().getLabel().equals("close")) {
            cycle();
        }*/
    }

    public void cycle() {
        if (menu == null) {
            menu = menuBuilders[index].buildMenu();
            //27.1.22 menuBuilders[index].getAttachNode().attach(menu.getNode());
            menu.getNode().getTransform().setParent(menuBuilders[index].getAttachNode());
            //cameraOfMenu= menuBuilders[index].getCamera();
        } else {
            close();

        }
    }

    /**
     * Zum programmatischen close
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

