package de.yard.threed.platform.homebrew;

import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.ImageData;
import de.yard.threed.core.platform.NativeTexture;
import de.yard.threed.javacommon.BufferHelper;

import java.nio.ByteBuffer;

/**
 * Hier ist die zentrale Stelle fuer das Zwischenspeichern von Images. D.h. die werden von einem Reader
 * hier in den Zwischenspeicher geschrieben, evtl. mehrere zusammen, und dann zur GPU geschickt.
 * Der ByteBuffer wird aufgrund seiner "direct" Natur (will OpenGL wohl als direct) wirklich nur zur Uebertragung verwendet und
 * danach sofort wieder freigegeben.
 * Der Texturepool verwaltet alle geladenenen Texturen, um Dubletten zu verhindern.
 * 3.3.16: Das ist nur die Referenz auf eine Texture in der GPU. Hier liegen keine Imagedaten.
 * <p/>
 * Date: 26.03.14
 */
public class OpenGlTexture implements NativeTexture {
    private int gltextureid;
    int mode;
    private static final int BYTES_PER_PIXEL = 4;//3 for RGB, 4 for RGBA
    private int height;
    private int width;
    static Log logger = Platform.getInstance().getLog(OpenGlTexture.class);
    GlInterface glcontext = OpenGlContext.getGlContext();
    public static int totalsize = 0;
    public static int totaltexturecnt;
    String name="unknown";
    
    public OpenGlTexture(ImageData image, int mode) {
        //this.target = GL15.GL_ARRAY_BUFFER;
        // images.add(image);
        this.mode = mode;
        this.width = image.width;
        this.height = image.height;
        ByteBuffer buffer = BufferHelper.buildTextureBuffer(image.width, image.height, image.pixel,BYTES_PER_PIXEL);
        setup(buffer);
        totalsize += image.width*image.height*BYTES_PER_PIXEL;
        totaltexturecnt++;
        buffer.clear();
    }

    private OpenGlTexture(int gltextureid, int mode,String name) {
        this.gltextureid = gltextureid;
        this.mode = mode;
        this.name=name;
        //TODO totalsize += image.width*image.height*BYTES_PER_PIXEL;
        totaltexturecnt++;

    }
    
        //bufferData(floats);
    private void buildFromBufferedImage(ImageData image) {
        logger.debug("bi.width=" + image.width);
        logger.debug("bi.height=" + image.height);

        width = image.width;
        height = image.height;
        //buildBuffer(image.getWidth(), image.getHeight(), image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth()));

        // pixel = image.pixel;//new int[height][width];
        //OGL loadPerRow(pixel, 0, 0,image, width, height);

        /*for (int i = 0; i < height; i++) {
            image.getRGB(0, i, width, 1, pixel[i], 0, width);
        } */
    }



   /*OGL private static int[][] loadPerRow(int[][] arr, int rowoffset, int coloffset, ImageData img, int width, int height) {
        int startx = 0;
        int widthofregion = width;
        int heightofregion = 1;
        int offsetinarray = coloffset;
        int strideforarray = width;
        for (int starty = 0; starty < height; starty++)
            img.bufferedimage.getRGB(startx, starty, widthofregion, heightofregion, arr[rowoffset + starty], offsetinarray, strideforarray);
        return arr;
    }*/



    /**
     * Texture zur GPU senden.
     */
    private void setup(ByteBuffer buffer) {
        gltextureid = createId();
        // 3.3.16 active und bind ist dafür erforderlich
        active(0);
        bind();

        assert buffer != null;

        switch (mode) {
            case 0:
                glcontext.glUploadTexture(width,height,buffer,true);
                break;
            case 1:
                glcontext.glUploadTexture(width,height,buffer,false);
                break;               
        }
        buffer.clear();
        OpenGlContext.getGlContext().exitOnGLError(glcontext, "loadPNGTexture");
    }

    /**
     * Der offset ab 0.
     * @param unitoffset
     */
    public void active(int unitoffset) {
        glcontext.glActiveTexture(unitoffset);
    }

    public void bind() {
        glcontext.glBindTexture( gltextureid);
    }

    public void unbind() {
        glcontext.glBindTexture( 0);
    }

    private  int createId() {
        return glcontext.glGenTextures();
    }


    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    /**
     * Liefert null bei error.
     * 
     * @param filename
     * @return
     */
    static OpenGlTexture loadFromFile(NativeResource filename,int mode) {
        long starttime = System.currentTimeMillis();
        /*if (!filename.startsWith("/")) {
            //TODO auch hier. 20.3.16: Geklaert?
            filename = "src/main/resources/" + filename;
        }*/
        //13.11.15: zu langsam: return buildFromImage(JmeImageUtil.buildImageData(JmeImageUtil.loadImageFromFile(new File(filename))));
        OpenGlTexture tex;
        // 25.8.16: Der PNG Decoder kann keine grey... laden. Darum per Cache
        // Nicht nur fuer PNG, denn er weiss, ob bei JPg o.ae. nicht aehnliches gilt.
        // 23.7.21:TODO merge mit load in JmeTexture
        if (true && filename.getName().toUpperCase().endsWith(".PNG")){
            int textureid = OpenGlContext.getGlContext().loadPngTextureFromFile(filename,mode==0);
            if (textureid==-1){
                return null;
            }
            tex = new OpenGlTexture(textureid,mode,filename.getName());
            
        }else {
            //2.10.19: Im Test laedt der ja gar nichts. Das ist doof. TODO.
            tex = buildFromImage(OpenGlContext.getGlContext().loadImageFromFile(filename));
            logger.debug(String.format("loadFromFile took %d ms", System.currentTimeMillis() - starttime));
        }
        return tex;
    }

    

    public Dimension getDimension() {
        return new Dimension(getWidth(), getHeight());
    }

    public static OpenGlTexture buildFromImage(ImageData imagedata) {
        if (imagedata == null){
            return null;
        }
        return new OpenGlTexture(imagedata, 0);
    }



    @Override
    public String getName() {
        return name;
    }
}

/**
 * Eine Kapselung des BufferedImage, weil das ja aus awt ist.
 * <p/>
 * Date: 27.03.14
 */
/*21.3.16 class OpenGlImage {
    //private static final int BYTES_PER_PIXEL = 4;//3 for RGB, 4 for RGBA
    //ByteBuffer buffer;
    int width, height;
    //BufferedImage image;
    int[] pixels;
    BufferedImage bufferedimage;

    public OpenGlImage(BufferedImage image) {
        this(image.getWidth(), image.getHeight(), image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth()));
        bufferedimage = image;

    }

    private OpenGlImage(int width, int height, int[] pixels) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }


    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

   /*21.3.16 public void addText(String s) {
        Graphics2D g2 = bufferedimage.createGraphics();
        java.awt.Color textcolor = java.awt.Color.RED;
        Font font = new Font("Serif", Font.PLAIN, 36);
        g2.setFont(font);
        g2.setColor(textcolor);
        //g2.buildTextImage(label, 5, 50);
        g2.drawLine(0, 0, 30, 30);

        Composite origComposite;

        // siehe http://docs.oracle.com/javase/tutorial/2d/text/renderinghints.html

        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        /*g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON); * /

        String condStr = "cond";

        FontRenderContext frc = ((Graphics2D) g2).getFontRenderContext();
        Rectangle2D boundsTemp = font.getStringBounds(s, frc);
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
        g2.buildTextImage(s, xTextTemp, yTextTemp);

        int xTextCond = rX - (int) boundsCond.getX();
        int yTextCond = rY - (int) boundsCond.getY() + (int) boundsTemp.getHeight();
        g2.buildTextImage(condStr, xTextCond, yTextCond);

    }

    /**
     * Das Skalieren ist relativ langsam. Aber immerhin, es geht so.
     *
     * @param scale
     * @return
     * /
     public OpenGlImage scale(float scale) {
        BufferedImage before = bufferedimage;
        boolean usescaledinstance = true;
        if (usescaledinstance) {
            java.awt.Image toolkitiamge = before.getScaledInstance((int) (before.getWidth() * scale), (int) (before.getHeight() * scale), java.awt.Image.SCALE_SMOOTH);
            BufferedImage newimg = new BufferedImage(toolkitiamge.getWidth(null), toolkitiamge.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics g = newimg.getGraphics();
            g.drawImage(toolkitiamge, 0, 0, null);
            g.dispose();
            return new OpenGlImage(newimg);
        } else {
            // Verkleinert zwar, hat aber noch die Original width/height
            int w = before.getWidth();
            int h = before.getHeight();
            BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            AffineTransform at = new AffineTransform();
            at.scale(scale, scale);
            AffineTransformOp scaleOp =
                    new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            after = scaleOp.filter(before, after);
            return new OpenGlImage(after);
        }
    }

}*/
