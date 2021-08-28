package de.yard.threed.platform.jme;

import de.yard.threed.core.platform.NativeCanvas;
import de.yard.threed.javacommon.ImageUtil;

import java.awt.image.BufferedImage;

public class JmeCanvas implements NativeCanvas {
    BufferedImage image;

    JmeCanvas(int width, int height){
         image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        ImageUtil.addText(image,"hello from jme",50,50, java.awt.Color.RED,null,14);
    }


}
