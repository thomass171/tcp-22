package de.yard.threed.engine.apps.vr;

import de.yard.threed.engine.gui.AvatarMenuProvider;
import de.yard.threed.engine.gui.Menu;
import de.yard.threed.engine.vr.VrHelper;

public
class VrMainMenuBuilder extends AvatarMenuProvider {
    VrScene rs;

    VrMainMenuBuilder(VrScene rs) {
        super(rs.avatar, rs.getDefaultCamera());
        this.rs = rs;
    }

    @Override
    public Menu buildMenu() {
        VrMainMenu menu = new VrMainMenu(rs.getDefaultCamera(), VrScene.logger, rs.menuitems, VrHelper.getController(1));
        return menu;
    }

   /* @Override
    public SceneNode getAttachNode() {
        //return rs.getDefaultCamera().getCarrier();
        return rs.avatar.getFaceNode();
    }*/

    /* @Override
     public Camera getCamera() {
         return rs.getDefaultCamera();
     }*/
    /*@Override
    public Ray getRayForUserClick(Point mouselocation) {
        if (mouselocation != null) {
            return rs.avatar.buildPickingRay(rs.getDefaultCamera(),mouselocation);
        }
        Ray ray = rs.avatar.controller1.getRay();
        return ray;
    }*/
}
