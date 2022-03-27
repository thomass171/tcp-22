package de.yard.threed.tools;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TextureBuilder {

    public static BufferedImage buildFace(Color backGround) {
        int width = 256;
        int height = 256;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // setBackground() is only for use with clearRect
        g2d.setColor(backGround);
        g2d.fillRect(0, 0, width, height);

        float linewidth = 8;
        BasicStroke linestyle = new BasicStroke(linewidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(linestyle);
        g2d.setColor(Color.BLACK);

        int width2 = width / 2;
        int mouthWidth = 64;
        int mouthWidth2 = mouthWidth / 2;

        int mouthHeight = 16;
        int mouthYpos = 128 + 16;

        int arc2 = 70;
        g2d.drawArc(width2 - mouthWidth2, mouthYpos, mouthWidth, mouthHeight, 270 - arc2, 2 * arc2);

        int eyeRadius = 14;
        int eyeOffsetX = 20;
        int eyeOffsetY = 100;
        g2d.drawArc(width2 - eyeOffsetX - eyeRadius, eyeOffsetY - eyeRadius, 2 * eyeRadius, 2 * eyeRadius, 0, 360);

        g2d.drawArc(width2 + eyeOffsetX - eyeRadius, eyeOffsetY - eyeRadius, 2 * eyeRadius, 2 * eyeRadius, 0, 360);

        return image;
    }

    public static BufferedImage buildMonsterFace(Color backGround) {
        int width = 256;
        int height = 256;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // setBackground() is only for use with clearRect
        g2d.setColor(backGround);
        g2d.fillRect(0, 0, width, height);

        float linewidth = 8;
        BasicStroke linestyle = new BasicStroke(linewidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(linestyle);
        g2d.setColor(Color.BLACK);

        int width2 = width / 2;
        int mouthWidth = 64;
        int mouthWidth2 = mouthWidth / 2;

        int mouthHeight = 22;
        int mouthHeight2 = mouthHeight / 2;
        int mouthYpos = 128 + 16;

        int xsteps = 8;
        int mouthxstep = mouthWidth / xsteps;

        int i = 0;
        g2d.drawLine(width2 - mouthWidth2 + i * mouthxstep, mouthYpos, width2 - mouthWidth2 + (i + 1) * mouthxstep, mouthYpos + mouthHeight2);
        g2d.drawLine(width2 + mouthWidth2 - i * mouthxstep, mouthYpos, width2 + mouthWidth2 - (i + 1) * mouthxstep, mouthYpos + mouthHeight2);

        i = 1;
        g2d.drawLine(width2 - mouthWidth2 + i * mouthxstep, mouthYpos + mouthHeight2, width2 - mouthWidth2 + (i + 1) * mouthxstep, mouthYpos - mouthHeight2);
        g2d.drawLine(width2 + mouthWidth2 - i * mouthxstep, mouthYpos + mouthHeight2, width2 + mouthWidth2 - (i + 1) * mouthxstep, mouthYpos - mouthHeight2);

        i = 2;
        g2d.drawLine(width2 - mouthWidth2 + i * mouthxstep, mouthYpos - mouthHeight2, width2 - mouthWidth2 + (i + 1) * mouthxstep, mouthYpos + mouthHeight2);
        g2d.drawLine(width2 + mouthWidth2 - i * mouthxstep, mouthYpos - mouthHeight2, width2 + mouthWidth2 - (i + 1) * mouthxstep, mouthYpos + mouthHeight2);

        i = 3;
        g2d.drawLine(width2 - mouthWidth2 + i * mouthxstep, mouthYpos + mouthHeight2, width2 - mouthWidth2 + (i + 1) * mouthxstep, mouthYpos - mouthHeight2);
        g2d.drawLine(width2 + mouthWidth2 - i * mouthxstep, mouthYpos + mouthHeight2, width2 + mouthWidth2 - (i + 1) * mouthxstep, mouthYpos - mouthHeight2);

        int eyeWidth2 = 14;
        int eyeHeight2 = 7;
        int eyeOffsetX = 20;
        int eyeOffsetY = 100;
        g2d.drawArc(width2 - eyeOffsetX - eyeWidth2, eyeOffsetY - eyeHeight2, 2 * eyeWidth2, 2 * eyeHeight2, 0, 360);

        g2d.drawArc(width2 + eyeOffsetX - eyeWidth2, eyeOffsetY - eyeHeight2, 2 * eyeWidth2, 2 * eyeHeight2, 0, 360);

        return image;
    }
}

