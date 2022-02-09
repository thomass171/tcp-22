package de.yard.threed.engine.gui;

import de.yard.threed.engine.platform.common.RequestType;

/**
 * Created on 01.10.18.
 */
public class MenuItem {
     RequestType request;
    String command;
    GuiTexture guiTexture;
    ButtonDelegate buttonDelegate;
    String text;
    
    public MenuItem(String command, GuiTexture guiTexture, ButtonDelegate buttonDelegate) {
        this.command = command;
        this.guiTexture = guiTexture;
        this.buttonDelegate = buttonDelegate;
    }

    public MenuItem(String text, ButtonDelegate buttonDelegate) {
        this.text = text;
        this.buttonDelegate = buttonDelegate;
    }

    /*30.12.19 doch nicht public MenuItem(RequestType request, GuiTexture guiTexture) {
        this.request = request;
        this.guiTexture = guiTexture;
        this.buttonDelegate = null;
    }*/
}
