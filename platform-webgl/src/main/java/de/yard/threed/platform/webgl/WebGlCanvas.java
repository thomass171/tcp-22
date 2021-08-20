package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import de.yard.threed.core.ImageData;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCanvas;

/**
 * Created by thomass on 29.09.15.
 *
 * TODO: 6.8.21: Solve display issue with hard to read text (drawString)
 */
public class WebGlCanvas implements NativeCanvas {
    JavaScriptObject canvas;
    JavaScriptObject context;
    // Direkt auf die Plattform, um Abhaengigkeit auf engine zu vermeiden
    static Log logger = new WebGlLog(/*LogFactory.getLog(*/WebGlCanvas.class.getName());
    int width, height;
    static int cnt = 100;

    private WebGlCanvas(JavaScriptObject canvas, int width, int height) {
        this.width = width;
        this.height = height;
        this.canvas = canvas;
        this.context = createContext(canvas);
        drawString("hello from webgl",50,50,"Font", 14);
    }

    static WebGlCanvas create(int width, int height) {
        logger.info("creating WebGlCanvas. width=" + width + ", height=" + height);
        return new WebGlCanvas(createCanvas(width, height), width, height);
    }

    //@Override
    public void drawImage(ImageData img, int x, int y) {
        logger.debug(" WebGlCanvas. drawImage x=" + x + ", y=" + y);
        if (img == null) {
            logger.error(" WebGlCanvas. drawImage img isType null");
            return;
        }
        JsArrayInteger data = (JsArrayInteger) JsArrayInteger.createArray();
        for (int i = 0; i < img.pixel.length; i++) {
            data.push(img.pixel[i]);
        }
        drawImage(context, x, y, img.width, img.height, data);
    }

    //@Override
    public ImageData getImageData() {
        JsArrayInteger data = (JsArrayInteger) JsArrayInteger.createArray();
        logger.debug("context=" + context);
        readImage(context, width, height, data);
        int[] pixel = new int[data.length()];
        for (int i = 0; i < data.length(); i++) {
            pixel[i] = data.get(i);
            /*if (pixel[i] != 0) {
                logger.info("image from canvas. pixel i=" + pixel[i]);
            }*/
        }
        logger.info("image from canvas. pixel0=" + pixel[0]);
        logger.info("image from canvas. pixel10=" + pixel[10]);

        return new ImageData(width, height, pixel);
    }

    public void drawString(String text, int x, int y, String font, int fontsize) {
        drawString(context, text, x, y,  font,  fontsize);
    }

    private static native JavaScriptObject createCanvas(int width, int height)  /*-{
        var canvas = $wnd.document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;
        return canvas;
    }-*/;

    private static native JavaScriptObject createContext(JavaScriptObject canvas)  /*-{
        var context = canvas.getContext("2d");
        return context;
    }-*/;

    /**
     */
    private static native void drawImage(JavaScriptObject context, int x, int y, int width, int height, JsArrayInteger data)  /*-{
        // Ein leeres ImageData Objekt anlegen und dann befuellen.
        var imgData = context.createImageData(width, height);
        var index = 0;
        for (var i=0;i<data.length;i++) {
            var j = data[i];
            imgData.data[index++] = ((j>>16)&0xFF);
            imgData.data[index++] = ((j>>8)&0xFF);
            imgData.data[index++] = ((j>>0)&0xFF);
            imgData.data[index++] = ((j>>24)&0xFF);
        }
        context.putImageData(imgData,x,y);
        //alert("nch putImageData");
    }-*/;

    /**
     */
    private static native void readImage(JavaScriptObject context, int width, int height, JsArrayInteger data)  /*-{
    //context.beginPath();
//context.moveTo(0, 0);
//context.lineTo(300, 150);
//context.stroke();
//context.fillRect(0,0,290,290);
//context.fillStyle    = '#ffffff';
//  context.fillRect( 0, 0, 32, 64 );

//var canvas = $wnd.document.createElement('canvas');
 //       canvas.width = width;
   //     canvas.height = height;
//cv = canvas;
//alert(context);

// context = cv.getContext("2d");
//context = ctx;

// context = canvas.getContext("2d");
//context.fillStyle="red";
//context.fillRect(0,0,50,50);

         var imgData = context.getImageData(0,0,width,height);
        // Ablegen im ARGB Format
        //alert(imgData.data.length+" "+imgData.data[0]+" "+imgData.data[1]+" "+imgData.data[2]+" "+imgData.data[3]+" "+width+" "+height);
        for (var i=0;i<imgData.data.length;i+=4) {
            data.push((imgData.data[i]<<16)+
            (imgData.data[i+1]<<8)+
            (imgData.data[i+2]<<0)+
            (imgData.data[i+3]<<24));
        }
    }-*/;

    /**
     * (x,y) ist links unten von der Schrift.
     */
    private static native void drawString(JavaScriptObject context, String text, int x, int y, String font, int fontsize) /*-{
        context.fillStyle = "blue";
        context.font = "bold 16px Arial";
        context.fillText(text, x, y);

    }-*/;

}
