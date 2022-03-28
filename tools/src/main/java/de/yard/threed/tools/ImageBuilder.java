package de.yard.threed.tools;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.javacommon.FileUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static de.yard.threed.tools.ToolsUtils.toAwtColor;

/**
 * Generieren von Images und Texturen.
 * <p>
 * 19.5.16: Da z.B. Unity keinen buildTextImage bzw. überhaupt keine draw Funktionen hat, ist der Nutzen dieser Klasse in der Engine fraglich. Ausser fuer ganz simples.
 * Daher hier in desktop tools um Imagedateien als Textur zu erzeugen. Ist dann natuerlich statisch. Da es in tools liegt, kann auch wieder awt verwendet werden.
 * <p>
 * 12.12.18: Das erstellen eines LabelSet aus Stringliste ist noch nicht fertig.
 * <p>
 * Created by thomass on 29.09.15.
 */
public class ImageBuilder {
    // intended not the final destination directory. Should be validated before moving.
    public static final String outdir = "/tmp";
    // platform is needed for some logging
    public static Platform platform = ToolsPlatform.init();

    private ImageBuilder() {

    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("t", "theme", true, "theme");
        options.addOption("n", "name", true, "name");
        //T.B:C: options.addOption("l", "labeldatafile", true, "labeldatafile");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            usage();
        }

        if (args.length <= 1) {
            // just a development preview
            buildForPreview();
            return;
        }
        String themename = cmd.getOptionValue("t");
        String name = cmd.getOptionValue("n");
        String labeldatafile = null;//cmd.getOptionValue("l");
        Theme theme = Theme.getFromName(themename);

        BufferedImage image = null;
        if (name.equals("Iconset")) {
            image = buildIconSet(theme);

        } else if (name.equals("Labelset")) {
            if (labeldatafile != null) {
                image = buildLabelSet(theme, labeldatafile);
            } else {
                image = buildLabelSet(theme);
            }

        } else if (name.equals("Face")) {
            image = TextureBuilder.buildFace(toAwtColor(de.yard.threed.core.Color.parseString(themename)));
        } else {

            usage();
        }
        String filename = outdir + "/" + name + "-" + ((theme == null) ? themename : theme.name) + ".png";
        try {
            FileUtil.saveToPngFile(image, filename);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private static void usage() {
        System.err.println("usage:TODO");
        System.exit(1);
    }

    /**
     * Preview for development.
     */
    public static void buildForPreview() {

        int previewOption = 8;
        BufferedImage image = null;

        switch (previewOption) {
            case 1:
                image = buildIconSet(Theme.ORANGE);
                break;
            case 2:
                image = buildIconSet(Theme.LIGHTBLUE);
                break;
            case 3:
                image = buildLabelSet(Theme.LIGHTBLUE);
                break;
            case 4:
                String[] helptext = new String[]{"Keys:", "[shift] x/y/z adjusts view position"};
                image = buildTextPage(Theme.LIGHTBLUE, "Helptext-LightBlue.png", helptext);
                break;
            case 5:
                image = TextureBuilder.buildFace(Color.green);
                break;
            case 6:
                image = buildSokobanTarget();
                break;
            case 7:
                // "red","blue","green","darkgreen" according to faces available
                image = buildMazeHome(de.yard.threed.core.Color.DARKGREEN);
                //image = buildMazeHome(de.yard.threed.core.Color.DARKGREEN);
                break;
            case 8:
                image = TextureBuilder.buildMonsterFace(new Color(0x993300));
                break;
        }

        // preview might modify image (blue frame?). So save it first.
        /*try {
            FileUtil.saveToPngFile(image, "Face-Monster.png");
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        ImagePreviewer.preview(image);

    }


    /**
     * AWT has y from top.
     */
    private static BufferedImage buildSokobanTarget() {
        MazeIconTheme mit = new MazeIconTheme(java.awt.Color.orange, null);
        int size = mit.size;
        int offset = mit.offset;

        float linewidth = 60;
        mit.g2d.drawLine(offset, offset, size - offset, offset);
        mit.g2d.drawLine(size - offset, offset, size - offset, size - offset);
        mit.g2d.drawLine(offset, offset, offset, size - offset);
        mit.g2d.drawLine(offset, size - offset, size - offset, size - offset);
        offset = 3 * offset;
        mit.g2d.drawLine(offset, offset, size - offset, size - offset);
        mit.g2d.drawLine(size - offset, offset, offset, size - offset);
        mit.g2d.dispose();

        //   FileUtil.saveToPngFile(image, "SokobanTarget.png");
        return mit.image;
    }

    /**
     * AWT has y from top.
     */
    private static BufferedImage buildMazeHome(de.yard.threed.core.Color color) {
        MazeIconTheme mit = new MazeIconTheme(toAwtColor(color), toAwtColor(de.yard.threed.core.Color.BLACK_FULLTRANSPARENT));
        int size = mit.size;
        int offset = mit.offset;

        float linewidth = 60;
        //top
        mit.g2d.drawLine(4 * offset, size / 2, size - 4 * offset, size / 2);
        //right
        mit.g2d.drawLine(size - 4 * offset, size / 2, size - 4 * offset, size - 2 * offset);
        //left
        mit.g2d.drawLine(4 * offset, size / 2, 4 * offset, size - 2 * offset);
        //bottom
        mit.g2d.drawLine(4 * offset, size - 2 * offset, size - 4 * offset, size - 2 * offset);

        //left roof
        mit.g2d.drawLine(4 * offset, size / 2, size / 2, 3 * offset);
        //right root
        mit.g2d.drawLine(size - 4 * offset, size / 2, size / 2, 3 * offset);
        mit.g2d.dispose();


        return mit.image;
    }

    /**
     * Ein Image (Textur) mit 16*16 Icons a 64x64 Pixel erzeugen.
     * Die Anzahl kann wegen der Ziffern nicht beliebig geändert werden. Die Positionen sind in definiert.
     * Die 48 Zahlen werden in den Zeilen 1-3 erwartet.
     * <p>
     * Der Background ist nicht transparent.
     */
    private static BufferedImage buildIconSet(Theme theme) {
        java.awt.Color color = theme.color;
        int iconsize = Theme.ICONSIZE;
        int cnt = 16;
        java.awt.Color bgcolor = theme.bgcolor;
        BufferedImage image = new BufferedImage(cnt * iconsize, cnt * iconsize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // von links oben nach rechts unten. awt hat y=0 oben
        int xpos = 0;
        new IconBuilder(iconsize, theme.color, theme.boldfont).drawTarget().addToSet(image, xpos++, 0, bgcolor);
        new IconBuilder(iconsize, theme.color, theme.boldfont).drawLeftArrow().addToSet(image, xpos++, 0, bgcolor);
        new IconBuilder(iconsize, theme.color, theme.boldfont).drawRightArrow().addToSet(image, xpos++, 0, bgcolor);
        new IconBuilder(iconsize, theme.color, theme.boldfont).drawUpArrow().addToSet(image, xpos++, 0, bgcolor);
        new IconBuilder(iconsize, theme.color, theme.boldfont).drawDownArrow().addToSet(image, xpos++, 0, bgcolor);
        new IconBuilder(iconsize, theme.color, theme.boldfont).drawPlus().addToSet(image, xpos++, 0, bgcolor);
        new IconBuilder(iconsize, theme.color, theme.boldfont).drawHorizontalLine().addToSet(image, xpos++, 0, bgcolor);
        // Leerstelle, war mal undo
        new IconBuilder(iconsize, theme.color, theme.boldfont).addToSet(image, xpos++, 0, bgcolor);
        new IconBuilder(iconsize, theme.color, theme.boldfont).drawVerticalLine().addToSet(image, xpos++, 0, bgcolor);
        new IconBuilder(iconsize, theme.color, theme.boldfont).drawCross().addToSet(image, xpos++, 0, bgcolor);
        //? fuer help
        new IconBuilder(iconsize, theme.color, theme.boldfont).drawString("?", true).addToSet(image, xpos++, 0, bgcolor);
        new IconBuilder(iconsize, theme.color, theme.boldfont).drawMenu().addToSet(image, xpos++, 0, bgcolor);
        new IconBuilder(iconsize, theme.color, theme.boldfont).drawReset().addToSet(image, xpos++, 0, bgcolor);
        new IconBuilder(iconsize, theme.color, theme.boldfont).drawPosition().addToSet(image, xpos++, 0, bgcolor);
        new IconBuilder(iconsize, theme.color, theme.boldfont).drawTurnRight().addToSet(image, xpos++, 0, bgcolor);
        new IconBuilder(iconsize, theme.color, theme.boldfont).drawTurnLeft().addToSet(image, xpos++, 0, bgcolor);

        for (int i = 0; i < cnt; i++) {
            new IconBuilder(iconsize, theme.color, theme.boldfont).drawString("" + (i + 1), true).addToSet(image, i, 1, bgcolor);
            new IconBuilder(iconsize, theme.color, theme.boldfont).drawString("" + (i + cnt + 1), true).addToSet(image, i, 2, bgcolor);
            new IconBuilder(iconsize, theme.color, theme.boldfont).drawString("" + (i + 2 * cnt + 1), true).addToSet(image, i, 3, bgcolor);
        }
        String abc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < 26; i++) {
            new IconBuilder(iconsize, theme.color, theme.boldfont).drawString("" + abc.charAt(i), true).addToSet(image, i % 16, 4 + i / 16, bgcolor);

        }
        return image;

    }

    /**
     * 4 Spalten a 256x
     * Erstmal nicht transparent, weil das den (Frame)Overlay bestimmt erschwert.
     *
     * @return
     */
    private static BufferedImage buildLabelSet(Theme theme) {
        java.awt.Color color = theme.color;
        int labelsize = 256;
        int cnt = 4;
        java.awt.Color bgcolor = theme.bgcolor;
        BufferedImage image = new BufferedImage(cnt * labelsize, cnt * labelsize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        Dimension basedimension = new Dimension(labelsize, 64);
        Font font = theme.font;
        // von links oben nach rechts unten. awt hat y=0 oben
        int xpos = 0;
        // new IconBuilder(dimension, color, font).drawFrame().drawCenteredString("Hello World").addToSet(image, xpos++, 0, bgcolor);
        int ypos = 0;
        buildLabel(basedimension, color, font, "Reset", true, image, 0, ypos, 1, 1, bgcolor);
        buildLabel(basedimension, color, font, "Start", true, image, 1, ypos, 1, 1, bgcolor);
        buildLabel(basedimension, color, font, "Close", true, image, 2, ypos++, 1, 1, bgcolor);

        buildLabel(basedimension, color, font, "Loading...", false, image, 0, ypos++, 1, 1, bgcolor);
        buildLabel(basedimension, color, font, "Loading Vehicle...", false, image, 0, ypos++, 2, 1, bgcolor);
        buildLabel(basedimension, color, font, "Loading Terrain...", false, image, 0, ypos++, 2, 1, bgcolor);
        //Maze level
        for (int i = 0; i < 4; i++) {
            buildLabel(basedimension, color, font, ((i + 1) * 5) + "x" + ((i + 1) * 5), false, image, i, ypos, 1, 1, bgcolor);
        }
        ypos++;
        //leere Frames
        for (int i = 0; i < 4; i++) {
            buildLabel(basedimension, color, font, "", true, image, 0, ypos++, 1 + i, 1, bgcolor);
        }
        for (int i = 0; i < 2; i++) {
            buildLabel(basedimension, color, font, "", true, image, 0, ypos, 1 + i, 2, bgcolor);
            ypos += 2;
        }
        return image;
        //FileUtil.saveToPngFile(image, filename);
    }

    /**
     * 4 (oder besser 8?) Spalten a 256x
     * Erstmal nicht transparent, weil das den (Frame)Overlay bestimmt erschwert.
     * LabelSet aus Stringliste. T.B.C.
     *
     * @return
     */
    private static BufferedImage buildLabelSet(Theme theme, String labeldatafile) {
        java.awt.Color color = theme.color;
        int labelsize = 256;
        int cnt = 4;
        java.awt.Color bgcolor = theme.bgcolor;
        BufferedImage image = new BufferedImage(cnt * labelsize, cnt * labelsize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        Dimension basedimension = new Dimension(labelsize, 64);
        Font font = theme.font;
        // von links oben nach rechts unten. awt hat y=0 oben
        int xpos = 0;
        // new IconBuilder(dimension, color, font).drawFrame().drawCenteredString("Hello World").addToSet(image, xpos++, 0, bgcolor);
        int ypos = 0;
        return image;
    }

    private static void buildLabel(Dimension basedimension, java.awt.Color color, Font font, String text, boolean framed, BufferedImage image, int xpos, int y, int w, int h, java.awt.Color bgcolor) {
        Dimension dimension = new Dimension(basedimension.width * w, basedimension.height * h);
        if (framed) {
            new IconBuilder(dimension, color, font).drawFrame().drawString(text, true).addToSet(image, xpos * basedimension.width, y * basedimension.height, -1, -1, bgcolor);
        } else {
            new IconBuilder(dimension, color, font).drawString(text, true).addToSet(image, xpos * basedimension.width, y * basedimension.height, -1, -1, bgcolor);
        }
    }


    private static BufferedImage buildTextPage(Theme theme, String filename, String[] text) {
        java.awt.Color color = theme.color;
        int labelsize = 1024;
        int cnt = 1;
        int lineheight = 64;
        java.awt.Color bgcolor = Color.LIGHT_GRAY;
        BufferedImage image = new BufferedImage(cnt * labelsize, 16 * lineheight, BufferedImage.TYPE_INT_ARGB);
        Dimension dimension = new Dimension(cnt * labelsize, 16 * lineheight);
        Dimension linedimension = new Dimension(labelsize, lineheight);
        Font font = theme.font;
        //geht nicht new IconBuilder(dimension, color, font).drawFrame();
        int ypos = 0;
        for (String line : text) {
            new IconBuilder(linedimension, color, font).drawString(line, false).addToSet(image, 0, ypos++ * lineheight, -1, -1, bgcolor);
        }
        return image;
    }



    /*1.3.17 public static ImageData buildLabelImage(String label, Dimension size, Color background) {
        ImageData image = buildSingleColor(size.getWidth(), size.getHeight(), background);

        // irgendwie zentrieren
        int fontsize = 36;
        // int width = StringUtils.length(label) * 15;
        int height = fontsize * 2;

        /* Zentrieren in awt. Geht hier aber nicht.
        FontRenderContext frc = ((Graphics2D) g2).getFontRenderContext();
        Rectangle2D boundsTemp = font.getStringBounds(text, frc);
        Rectangle2D boundsCond = font.getStringBounds(condStr, frc);
        int wText = Math.max((int) boundsTemp.getWidth(), (int) boundsCond.getWidth());
        int hText = (int) boundsTemp.getHeight() + (int) boundsCond.getHeight();
        int rX = (width - wText) / 2;
        int rY = (height - hText) / 2;

        g2.setColor(java.awt.Color.LIGHT_GRAY);
        g2.fillRect(rX, rY, wText, hText);

        g2.setColor(textcolor);
        int xTextTemp = rX - (int) boundsTemp.getX();
        int yTextTemp = rY - (int) boundsTemp.getY();
        g2.buildTextImage(text, xTextTemp, yTextTemp);

        int xTextCond = rX - (int) boundsCond.getX();
        int yTextCond = rY - (int) boundsCond.getY() + (int) boundsTemp.getHeight();
        g2.buildTextImage(condStr, xTextCond, yTextCond);
        * /
        image = pf.buildTextImage(label/*, size.getWidth() / 2 - width / 2, size.getHeight() / 2 - height / 2* /, Color.BLACK, "Arial", fontsize);

        return image;

    }*/



}


class Theme {
    public static int ICONSIZE = 64;
    static Theme LIGHTBLUE = new Theme("LightBlue", new java.awt.Color(51, 153, 255), Color.LIGHT_GRAY,
            new Font("SansSerif", Font.PLAIN, 36), new Font("TimesRoman", Font.BOLD, ICONSIZE * 7 / 8));
    static Theme ORANGE = new Theme("Orange", java.awt.Color.orange, java.awt.Color.BLACK,
            new Font("SansSerif", Font.PLAIN, 36), new Font("TimesRoman", Font.BOLD, ICONSIZE * 7 / 8));

    Font font;
    String name;
    java.awt.Color color, bgcolor;
    Font boldfont;

    Theme(String name, java.awt.Color color, java.awt.Color bgcolor, Font font, Font boldfont) {
        this.name = name;
        this.color = color;
        this.bgcolor = bgcolor;
        this.font = font;
        this.boldfont = boldfont;
    }

    public static Theme getFromName(String themename) {
        if (themename.equals(LIGHTBLUE.name)) {
            return LIGHTBLUE;
        }
        if (themename.equals(ORANGE.name)) {
            return ORANGE;
        }
        return null;
    }
}

class MazeIconTheme {
    public int size = 512;
    public int offset = 40;
    public Graphics2D g2d;
    public BufferedImage image;

    public MazeIconTheme(java.awt.Color color, java.awt.Color backgroundColor) {
        image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        g2d = image.createGraphics();
        if (backgroundColor != null) {
            g2d.setBackground(backgroundColor);
            g2d.clearRect(0, 0, size, size);
        }
        float linewidth = 60;
        BasicStroke linestyle = new BasicStroke(linewidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(linestyle);
        g2d.setColor(color);

    }
}