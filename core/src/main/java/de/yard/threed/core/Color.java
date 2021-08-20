package de.yard.threed.core;


import de.yard.threed.core.buffer.ByteArrayInputStream;
import de.yard.threed.core.buffer.NativeOutputStream;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;


/**
 * OpenGL verwendet floatwerte 0-1, AWT int Werte von 0-255 (wirklich?).
 * Wahrscheinlich wäre es besser, auf float umzustellen, weil awt
 * nur von ein paar Utilities verwendet wird.
 * 10.4.2015: umgestellt auf float. Andere arbeiten auch mit float.
 * Ist auch besser für Lichtberechnungen.
 *
 * color picker https://www.w3schools.com/colors/colors_picker.asp
 *
 * 30.6.21 TODO move to core
 * <p/>
 * Date: 14.02.14
 * Time: 16:41
 */
public class Color {
    static Log logger = Platform.getInstance().getLog(Color.class);

    // geht nicht als final wegen C#
    // 6.3.21: Das sind z.T. awt Standard Colors and partly self defined
    public static Color RED = new Color(1.0f, 0, 0);
    public static Color DARKRED = new Color(0.6f, 0, 0);
    public static Color GREEN = new Color(0, 1.0f, 0);
    public static Color DARKGREEN = new Color(0, 0.5f, 0);
    public static Color LIGHTGREEN = new Color(0.5f, 1.0f, 0.5f);
    public static Color BLUE = new Color(0, 0, 1.0f);
    //LIGHTBLUE ist background. Darum nur bedingt geeignet
    public static Color LIGHTBLUE = new Color(0.5f, 0.5f, 1.0f);
    public static Color WHITE = new Color(1.0f, 1.0f, 1.0f);
    public static Color ORANGE = new Color(255, 0x7F, 0);
    public static Color YELLOW = new Color(1.0f, 1.0f, 0.0f);//??der Code FFFF00 ist aber richtig
    public static Color DARKYELLOW = new Color(0.5f, 0.5f, 0.0f);//??der Code FFFF00 ist aber richtig
    //12.7.17: 0x7F->0x80
    public static Color GRAY = new Color(0x80, 0x80, 0x80);
    public static Color DARKGRAY = new Color(0x40, 0x40, 0x40);
    public static Color LIGHTGRAY = new Color(0xD3, 0xD3, 0xD3);
    // Die Default(?) Light Farbe in Unity
    public static Color UNITY = new Color(255, 244, 214);
    public static Color TRANSPARENT = new Color(0, 0, 0, 0);
    public static Color BLACK = new Color(0, 0, 0);

    public static  Color ral7043 = new Color(79, 82, 80);

    // 29.1.16: Nicht mehr ueber Platform
    //alpha=0 -> transparent
    private float alpha = 1, r, g, b;
    //public NativeColor color;

    public Color(int r, int g, int b) {
        this((float) r / 255, (float) g / 255, (float) b / 255);
    }

    public Color(float r, float g, float b) {
        //color = PlatformFactory.getInstance().buildColor(r, g, b, 1);
        this(r, g, b, 1);
    }

    public Color(int r, int g, int b, int a) {
        this((float) r / 255, (float) g / 255, (float) b / 255, (float) a / 255);
    }

    public Color(float r, float g, float b, float a) {
        // color = PlatformFactory.getInstance().buildColor(r, g, b, a);
        this.r = r;
        this.g = g;
        this.b = b;
        this.alpha = a;
    }

    public Color(int argb) {
        this((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, (argb >> 0) & 0xFF, (argb >> 24) & 0xFF);
    }

    public Color(ByteArrayInputStream ins) {
        this(ins.readFloat(), ins.readFloat(), ins.readFloat(), ins.readFloat());
    }

    public float getR() {
        return r;//color.getR();
    }

    public float getG() {
        return g;//color.getG();
    }

    public float getB() {
        return b;//color.getB();
    }

    public float getAlpha() {
        return alpha;//color.getA();
    }

    public int getRasInt() {
        return (int) (getR() * 255f);
    }

    public int getGasInt() {
        return (int) (getG() * 255f);
    }

    public int getBasInt() {
        return (int) (getB() * 255f);
    }

    public int getAlphaasInt() {
        return (int) (getAlpha() * 255f);
    }

    /**
     * RGBW instead of RGB
     *
     * @return
     */
   /* public float[] getElements(boolean mitw) {
        float[] out = new float[mitw ? 4 : 3];
        int i = 0;

        // Insert XYZW elements
        out[i++] = getR();
        out[i++] = getG();
        out[i++] = getB();
        if (mitw)
            out[i++] = 1f;


        return out;
    } */



    /*THREED public void putComponents(float[] out, int i) {
        out[i++] = r;
        out[i++] = g;
        out[i++] = b;
        out[i++] = alpha;
    }

    public void putComponents(FloatBuffer buf) {
        buf.put(r);
        buf.put(g);
        buf.put(b);
        buf.put(alpha);
    }*/
    public boolean isEqual(Color color) {
        return MathUtil.floatEquals(color.getR(), getR()) && MathUtil.floatEquals(color.getG(), getG()) &&
                MathUtil.floatEquals(color.getB(), getB()) && MathUtil.floatEquals(color.getAlpha(), getAlpha());
    }


    public int getARGB() {
        int alphaasint = getAlphaasInt();
        /*if (alphaasint < 0) {
            alphaasint += 256;
        }*/
        int argb = alphaasint << 24;
        argb = argb + (getRasInt() << 16) + (getGasInt() << 8) + getBasInt();
        //logger.debug("argb="+argb+",basint="+getBasInt());
        //if (argb < 0){
        //    argb += 0x1000000;
        //}
        return argb;
    }

    public int getRGB() {
        int rgb = (getRasInt() << 16) + (getGasInt() << 8) + getBasInt();
        //logger.debug("argb="+argb+",basint="+getBasInt());
        return rgb;
    }

    @Override
    public String toString() {
        return "r=" + r + ",g=" + g + ",b=" + b;
    }

    public void serialize(NativeOutputStream outs) {
        outs.writeFloat(getR());
        outs.writeFloat(getG());
        outs.writeFloat(getB());
        outs.writeFloat(getAlpha());
    }

    public static Color parseString(String data) {
        if (StringUtils.toLowerCase(data).equals("green")){
            return Color.GREEN;
        }
        if (StringUtils.toLowerCase(data).equals("darkgreen")){
            return Color.DARKGREEN;
        }
        if (StringUtils.toLowerCase(data).equals("orange")){
            return Color.ORANGE;
        }
        if (StringUtils.toLowerCase(data).equals("red")){
            return Color.RED;
        }
        if (StringUtils.toLowerCase(data).equals("blue")){
            return Color.BLUE;
        }
        String[] s = StringUtils.split(data, " ");
        if (s.length != 4) {
            logger.error("parseString: invalid color data");
        }
        return new Color(Util.parseFloat(s[0]), Util.parseFloat(s[1]), Util.parseFloat(s[2]), Util.parseFloat(s[3]));
    }
}
