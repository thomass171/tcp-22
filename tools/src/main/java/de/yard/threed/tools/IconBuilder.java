package de.yard.threed.tools;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import static java.awt.geom.Path2D.WIND_EVEN_ODD;

public class IconBuilder {
    private Font font;
    private Dimension dimension = null;
    BufferedImage image;
    Graphics2D g2d;
    int size, t31, t32, t33, t51, t52, t53, t54, t55;
    // Der typische Abstand vom Rand beim Zeichnen von Linien
    int margin;

    /**
     * Constructor fuer quadratische Icons.
     *
     * @param size
     * @param color
     * @param font
     */
    IconBuilder(int size, Color color, Font font) {
        this.size = size;
        dimension = new Dimension(size, size);
        image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        g2d = image.createGraphics();
        float linewidth = size / 8;
        BasicStroke linestyle = new BasicStroke(linewidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(linestyle);
        g2d.setColor(color);
        // g2d.setFont(new Font("TimesRoman", Font.BOLD, size * 7 / 8));
        g2d.setFont(font);
        this.font = font;
        //25.10.18 6->10, ist gefälliger
        margin = 10;
        setTabs();
    }

    /**
     * Constructor fuer beliebige (groessere) Icons.
     *
     * @param color
     * @param font
     */
    public IconBuilder(Dimension dimension, Color color, Font font) {
        this.dimension = dimension;
        this.font = font;
        image = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_ARGB);
        g2d = image.createGraphics();
        //schmal fuer frame
        float linewidth = 2;
        BasicStroke linestyle = new BasicStroke(linewidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(linestyle);
        g2d.setColor(color);
        g2d.setFont(font);
        margin = 20;
        setTabs();
    }

    private void setTabs() {
        t31 = 16;
        t32 = 32;
        t33 = 48;
        t51 = 10;
        t52 = 21;
        t53 = 32;
        t54 = 43;
        t55 = 53;
    }

    public IconBuilder drawTarget() {
        int offset = margin;
        g2d.drawLine(offset, offset, size - offset, offset);
        g2d.drawLine(size - offset, offset, size - offset, size - offset);
        g2d.drawLine(offset, offset, offset, size - offset);
        g2d.drawLine(offset, size - offset, size - offset, size - offset);
        //offset = 3 * offset;
        //g2d.drawLine(offset, offset, size - offset, size - offset);
        //g2d.drawLine(size - offset, offset, offset, size - offset);
        return this.drawCross();
    }

    public IconBuilder drawCross() {
        int offset = margin;
        offset = 2 * offset;
        g2d.drawLine(offset, offset, size - offset, size - offset);
        g2d.drawLine(size - offset, offset, offset, size - offset);
        return this;
    }

    public IconBuilder drawMenu() {
        int offset = margin;

        /*for (int i = 1; i <= 3; i++) {
            g2d.drawLine(offset, i * (size / 4), size - offset, i * (size / 4));
        }*/
        g2d.drawLine(offset, t31, size - offset, t31);
        g2d.drawLine(offset, t32, size - offset, t32);
        g2d.drawLine(offset, t33, size - offset, t33);

        return this;
    }

    public IconBuilder drawReset() {
        int offset = margin + 6;
        g2d.drawArc(offset, offset, size - 2 * offset, size - 2 * offset, 180 - 45, 270);
        // ein Pfeil nach links rechts oben am Bogen
        int offset1 = 4;
        drawSlash(t53 + offset1, t51 + offset1, 0, 1);
        drawSlash(t53 + offset1, t51 + offset1, 1, 0);
        drawArc(t32, t32, 16, 180 - 45, 270);
        return this;
    }

    public IconBuilder drawArc(int x, int y, int radius, int startAngle, int arcAngle) {
        g2d.drawArc(x - radius, y - radius, 2 * radius, 2 * radius, startAngle, arcAngle);
        return this;
    }

    /**
     * So eine Art senkrechte Stecknadel.
     *
     * @return
     */
    public IconBuilder drawPosition() {
        //oben Knopf
        int x = size / 2;
        int oben = size / 4;
        int radius = 8;
        g2d.drawArc(x - radius, oben - radius, 2 * radius, 2 * radius, 0, 360);
        int unten = 3 * (size / 4);
        //senkrecht
        g2d.drawLine(size / 2, oben, size / 2, unten);
        //unten waagerecht
        g2d.drawLine(2 * margin, unten, size - 2 * margin, unten);
        return this;
    }

    public IconBuilder drawTurnRight() {
        //g2d.drawArc(12 + offset, offset + 2, size - 2 * offset, size - 2 * offset, 180, -90);
        drawSlash(t33, t31, -1, -1);
        drawSlash(t33, t31, -1, 1);
        g2d.drawLine(t32, t31, t33, t31);
        drawArc(t32, t32, t32 - t31, 180, -90);
        return this;
    }

    public IconBuilder drawTurnLeft() {
        // g2d.drawArc(offset - 11, offset + 2, size - 2 * offset, size - 2 * offset, 0, 90);
        drawSlash(t31, t31, 1, 1);
        drawSlash(t31, t31, 1, -1);
        g2d.drawLine(t31, t31, t32, t31);
        drawArc(t32, t32, t32 - t31, 0, 90);
        return this;
    }

    public IconBuilder drawLeftArrow() {
        drawHorizontalLine();
        drawWinkelLinksOben();
        drawWinkelLinksUnten();
        return this;
    }

    public IconBuilder drawRightArrow() {
        drawHorizontalLine();
        drawWinkelRechtsOben();
        drawWinkelRechtsUnten();
        return this;
    }

    public IconBuilder drawUpArrow() {
        drawVerticalLine();
        drawWinkelLinksOben();
        drawWinkelRechtsOben();
        return this;
    }

    public IconBuilder drawDownArrow() {
        drawVerticalLine();
        drawWinkelLinksUnten();
        drawWinkelRechtsUnten();
        return this;
    }

    public IconBuilder drawPlus() {
        drawHorizontalLine();
        drawVerticalLine();
        return this;
    }

    int arrowoffset = 7;

    /**
     * Eine 45 Grad Strecke mit x/y offset arrowoffset.
     *
     * @return
     */
    public IconBuilder drawSlash(int x, int y, int xdir, int ydir) {
        g2d.drawLine(x, y, x + xdir * arrowoffset, y + ydir * arrowoffset);
        return this;
    }

    public IconBuilder drawWinkelLinksOben() {
        g2d.drawLine(margin, size / 2, size / 2, margin);
        return this;
    }

    public IconBuilder drawWinkelRechtsOben() {
        g2d.drawLine(size / 2, margin, size - margin, size / 2);
        return this;
    }

    public IconBuilder drawWinkelRechtsUnten() {
        g2d.drawLine(size - margin, size / 2, size / 2, size - margin);
        return this;
    }

    public IconBuilder drawWinkelLinksUnten() {
        g2d.drawLine(size / 2, size - margin, margin, size / 2);
        return this;
    }

    public IconBuilder drawHorizontalLine() {
        g2d.drawLine(margin, size / 2, size - margin, size / 2);
        return this;
    }

    public IconBuilder drawVerticalLine() {
        g2d.drawLine(size / 2, margin, size / 2, size - margin);
        return this;
    }

    public IconBuilder drawString(String s) {
        if (s.length() > 2) {
            throw new RuntimeException("das geht vorerst nicht:" + s);
        }
        int offset = size / 10;
        int x = size / 3;
        if (s.length() > 1) {
            x /= 3;
        }
        int y = size - 2 * margin;
        g2d.drawString(s, x, y);
        return this;
    }

    public IconBuilder drawString(String text, boolean centered) {
        if (text == null) {
            return this;
        }
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics metrics = g2d.getFontMetrics(font);
        int width = dimension.width;
        int height = dimension.height;
        int x = /*margin +*/ (width - metrics.stringWidth(text)) / 2;
        if (!centered) {
            x = margin;
        }
        // in java 2d 0 isType top of the screen
        int y = /*margin +*/ ((height - metrics.getHeight()) / 2) + metrics.getAscent();
        g2d.drawString(text, x, y);
        return this;
    }

    public IconBuilder drawFrame() {
        float radius = 5;
        Path2D path = buildFrame(dimension.width, dimension.height, 5, radius);
        g2d.draw(path);
        return this;
    }

    public Path2D buildFrame(float width, float height, float thickness, float radius) {
        Path2D p = new Path2D.Float();

        float innerWidth = width - thickness;
        float innerHeight = height - thickness;

        p.moveTo(thickness + radius, thickness);
        p.lineTo(innerWidth - radius, thickness);
        p.curveTo(innerWidth, thickness, innerWidth, thickness, innerWidth, thickness + radius);
        p.lineTo(innerWidth, innerHeight - radius);
        p.curveTo(innerWidth, innerHeight, innerWidth, innerHeight, innerWidth - radius, innerHeight);
        p.lineTo(thickness + radius, innerHeight);
        p.curveTo(thickness, innerHeight, thickness, innerHeight, thickness, innerHeight - radius);
        p.lineTo(thickness, thickness + radius);
        p.curveTo(thickness, thickness, thickness, thickness, thickness + radius, thickness);
        p.closePath();
        p.setWindingRule(WIND_EVEN_ODD);
        return p;
    }

    public void addToSet(BufferedImage iconsetimage, int x, int y, java.awt.Color bgcolor) {

        iconsetimage.createGraphics().drawImage(image, x * size, y * size, bgcolor, null);
    }

    public void addToSet(BufferedImage iconsetimage, int x, int y, int w, int h, java.awt.Color bgcolor) {
        iconsetimage.createGraphics().drawImage(image, x, y, bgcolor, null);

    }
}

