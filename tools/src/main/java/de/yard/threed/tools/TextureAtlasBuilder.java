package de.yard.threed.tools;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Util;
import de.yard.threed.javacommon.FileUtil;
import de.yard.threed.javacommon.ImageUtils;
import org.apache.commons.cli.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Baut einzelne Texturen gridartig in einen Atlas.
 * Das ist zunaechst mal alles codiert, koennte aber auch mal in eine Config.
 * <p>
 * Sowas aehnliches gabs schon mal für die 737.
 * <p>
 * Folgende Atlas sin definiert:
 * - Runway: Die Einzeltexturen von FG
 * - Road: Aus einer Basistextur einen Atals mit Decorations erstellen.
 *
 * <p>
 * Created by thomass on 21.05.19.
 */
public class TextureAtlasBuilder {
    // intended not the final destination directory. Should be validated before moving.
    public static final String outdir = "/tmp";
    public static Platform platform = ToolsPlatform.init();
    static Log logger = Platform.getInstance().getLog(TextureAtlasBuilder.class);


    private TextureAtlasBuilder() {

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

        String themename = cmd.getOptionValue("t");
        String name = cmd.getOptionValue("n");

        name = "Road";
        BufferedImage image = null;
        if (name.equals("Runway")) {
            image = buildRunwayAtlas();
        } else if (name.equals("Road")) {
            Util.nomore();
            //image = buildRoadAtlas();
        } else {
            usage();
        }
        ImagePreviewer.preview(image);

        String filename = outdir + "/" + name + "-Atlas.png";
        try {
            FileUtil.saveToPngFile(image, filename);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        //System.exit(0);
    }

    private static void usage() {
        System.err.println("usage:TODO");
        System.exit(1);
    }

    /**
     * 32 Spalten a 128
     * AWT zaehlt y von oben
     * Der Background ist nicht transparent.
     * <p>
     * Die Runway ist 1024 Pixel breit, ein Segment 128 Pixel hoch.
     * Nach unten kommen die 1024er, darüber die 512
     * In 2048 passt das nicht alles.
     * 23.5.19 nach rechts rotieren, weils intuitiver zu TriangleStrip passt.
     *
     * @return
     */
    private static BufferedImage buildRunwayAtlas() {
        String prefix = "pa_";
        String srcdir = "/Users/thomas/Projekte/FlightGearGit/FGData/Textures/Runway";
        TextureAtlas textureAtlas = new TextureAtlas(4096, 128);
        //Color color = theme.color;
        //int labelsize = 256;
        int bottomrow = 31;
        //Dimension basedimension = new Dimension(labelsize, 64);
        // von links oben nach rechts unten. awt hat y=0 oben
        int xpos = 0;
        BufferedImage texture;
        // new IconBuilder(dimension, color, font).drawFrame().drawCenteredString("Hello World").addToSet(image, xpos++, 0, bgcolor);
        int ypos = 4;
        for (int i = 1; i < 10; i++) {
            // Center 0 gibt es wohl nicht
            texture = ImageUtils.loadImageFromFile(logger, srcdir + "/" + prefix + i + "c.png");
            //bei 0 bleibt eine Luecke
            textureAtlas.add(0, bottomrow - i, 8, 1, texture);
        }
        texture = ImageUtils.loadImageFromFile(logger, srcdir + "/" + prefix + "L.png");
        textureAtlas.add(0, bottomrow - 10, 8, 1, texture);
        texture = ImageUtils.loadImageFromFile(logger, srcdir + "/" + prefix + "C.png");
        textureAtlas.add(0, bottomrow - 11, 8, 1, texture);
        texture = ImageUtils.loadImageFromFile(logger, srcdir + "/" + prefix + "R.png");
        textureAtlas.add(0, bottomrow - 12, 8, 1, texture);


        for (int i = 0; i < 3; i++) {
            //links geht nur bis 3
            texture = ImageUtils.loadImageFromFile(logger, srcdir + "/" + prefix + i + "l.png");
            textureAtlas.add(8, bottomrow - i, 4, 1, texture);
        }
        for (int i = 0; i < 10; i++) {
            texture = ImageUtils.loadImageFromFile(logger, srcdir + "/" + prefix + i + "r.png");
            textureAtlas.add(12, bottomrow - i, 4, 1, texture);
        }

        //aim ist 1024x512
        texture = ImageUtils.loadImageFromFile(logger, srcdir + "/" + prefix + "aim.png");
        textureAtlas.add(16, bottomrow - 3, 8, 4, texture);
        //centerline ist 1024x256. Sieht genauso aus wie rest
        texture = ImageUtils.loadImageFromFile(logger, srcdir + "/" + prefix + "centerline.png");
        textureAtlas.add(16, bottomrow - 5, 8, 2, texture);

        //rest ist 1024x256
        texture = ImageUtils.loadImageFromFile(logger, srcdir + "/" + prefix + "rest.png");
        textureAtlas.add(16, bottomrow - 7, 8, 2, texture);

        //no_threshold ist 1024x256
        texture = ImageUtils.loadImageFromFile(logger, srcdir + "/" + prefix + "no_threshold.png");
        textureAtlas.add(16, bottomrow - 9, 8, 2, texture);

        //threshold ist 1024x256
        texture = ImageUtils.loadImageFromFile(logger, srcdir + "/" + prefix + "threshold.png");
        textureAtlas.add(16, bottomrow - 11, 8, 2, texture);

        //tiedown.png ist 512x512. Hat aber ne andere Farbe(?).
        texture = ImageUtils.loadImageFromFile(logger, srcdir + "/" + prefix + "tiedown.png");
        textureAtlas.add(24, bottomrow - 3, 4, 4, texture);

        return rotate(textureAtlas.image);
    }


    /**
     * "verlustfrei" Rotation nach rechts (ohne AffineTransformation). Naja, ob das per Graphics2D verlustfrei?
     *
     * @return
     */
    private static BufferedImage rotate(BufferedImage src) {
        int width = src.getWidth();
        int height = src.getHeight();

        BufferedImage dest = new BufferedImage(height, width, src.getType());

        Graphics2D graphics2D = dest.createGraphics();
        graphics2D.translate((height - width) / 2, (height - width) / 2);
        graphics2D.rotate(Math.PI / 2, height / 2, width / 2);
        graphics2D.drawRenderedImage(src, null);

        return dest;
    }


    private static void buildLabel(Dimension basedimension, Color color, Font font, String text, boolean framed, BufferedImage image, int xpos, int y, int w, int h, Color bgcolor) {
        Dimension dimension = new Dimension(basedimension.width * w, basedimension.height * h);
        if (framed) {
            new IconBuilder(dimension, color, font).drawFrame().drawString(text, true).addToSet(image, xpos * basedimension.width, y * basedimension.height, -1, -1, bgcolor);
        } else {
            new IconBuilder(dimension, color, font).drawString(text, true).addToSet(image, xpos * basedimension.width, y * basedimension.height, -1, -1, bgcolor);
        }
    }


}

/**
 * Der Background ist nicht transparent.
 */
class TextureAtlas {
    static Log logger = Platform.getInstance().getLog(TextureAtlas.class);
    BufferedImage image;
    int cellsize;

    TextureAtlas(int size, int cellsize) {
        //Color bgcolor = theme.bgcolor;
        image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        this.cellsize = cellsize;
    }

    /**
     * (x,y) ist links oben der Zelle
     * w und h wird erstmal ignoriert.
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @param img
     */
    void add(int x, int y, int w, int h, BufferedImage img) {
        if (w * cellsize != img.getWidth()) {
            throw new RuntimeException(("ivalid width") + img.getWidth());
        }
        if (h * cellsize != img.getHeight()) {
            throw new RuntimeException(("ivalid height") + img.getHeight());
        }
        image.createGraphics().drawImage(img, x * cellsize, y * cellsize, null);
    }

    public void fill(String basetexture) {
        BufferedImage base = ImageUtils.loadImageFromFile(logger, basetexture);
        if (image.getWidth() % base.getWidth() != 0) {
            throw new RuntimeException("invalid size");
        }
        if (image.getHeight() % base.getHeight() != 0) {
            throw new RuntimeException("invalid size");
        }
        int cnt = image.getWidth() / base.getWidth();
        int factor = base.getWidth() / cellsize;
        for (int x = 0; x < cnt; x++) {
            for (int y = 0; y < cnt; y++) {
                add(x, y, factor, factor, base);
            }
        }
    }

    /**
     * draws den path mit (0,0) im Center der mit x,y,w,h definierten Zelle. Anhand shapescale wird skaliert. NeeNee, direkt bei shapeerstellung skalieren.
     * (x,y) ist links oben der Zelle
     * <p>
     * GradientPaint, and TexturePaint? Der VALUE_INTERPOLATION_BILINEAR wirkt beim fill teilweise(!) auch auf orthogonale Linien. Das ist eigentlich ganz gut.
     * VALUE_INTERPOLATION_BICUBIC ist nicht erkennbar besser, VALUE_INTERPOLATION_NEAREST_NEIGHBOR aber etwas schlechter.
     */
    void draw(Decoration decoration, int x, int y, int w, int h) {
        double xcenter = x * cellsize + w * cellsize / 2;
        double ycenter = y * cellsize + h * cellsize / 2;
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        for (GeneralPath path : decoration.paths) {
            AffineTransform at = new AffineTransform();
            at.translate(xcenter,ycenter );
            path.transform(at);
            graphics2D.setColor(decoration.color);
            graphics2D.fill(path);
            //graphics2D.draw(path);
        }
        for (Decoration.Text text : decoration.texts) {
            graphics2D.drawString(text.text, Math.round(xcenter + text.x), Math.round(ycenter + text.y));
        }
    }
}