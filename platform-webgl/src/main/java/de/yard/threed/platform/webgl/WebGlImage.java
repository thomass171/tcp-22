package de.yard.threed.platform.webgl;


/**
 *  Das ist schon ziemlich hinten rum, an die Imagedaten zu kommen.
 *  Und weil ja immer nur Imagedaten gewollt sind, ist das hier eigentlich transient (TODO)
 *  TODO pruefen, ob das auch mit mehreeren parallel geht
 *  Es wird wirklich nur das an Imagedaten ausgelesen, was auch angezeigt wird.
 *  Der Rest ist 0(schwarz). Das ist totaler Driss und vielleicht völlig unbrauchbar.
 *  Das Canvas muss offenbar wirklich komplett sichtbar sein.
 *  02.10.15: Wegen der Komplikationen erstmal nicht mehr verwenden.
 *
 * Created by thomass on 29.09.15.
 */
/*30.7.21
public class WebGlImage /*30.7.21 implements NativeImage* / {
    JavaScriptObject image;
    // Direkt auf die Plattform, um Abhaengigkeit auf engine zu vermeiden
    Log logger = new WebGlLog(/*LogFactory.getLog(* /WebGlImage.class.getName());
    ImageLoadingListener loadlistener;
    JsArrayInteger data;
    int width, height;
    static int cnt = 100;

    @Deprecated
    public WebGlImage(String imageresource,ImageLoadingListener loadlistener) {
        //WebGlCommon.alert("vor loaded");
        this.loadlistener = loadlistener;
        data = (JsArrayInteger) JsArrayInteger.createArray();
        Util.notyet();
        //30.8.16 image = loadImage(cnt++,imageresource,this,data);
    }

    /*19.5.16 siehe Kommentar ResourceManagervoid loaded(){
        logger.info("image loaded. length="+data.length()+", width="+width+", height="+height);
        int[] pixel = new int[data.length()];
        for (int i =0;i<data.length();i++){
            pixel[i] = data.get(i);
            if (pixel[i] != 0){
                logger.info("image loaded. pixel i="+pixel[i]);
            }
        }
        logger.info("image loaded. pixel0="+pixel[0]);
        logger.info("image loaded. pixel10="+pixel[10]);

        loadlistener.onLoad(new ImageData(width, height, pixel));
        //WebGlCommon.alert("loaded");
    }* /

    /**
     * /
    private static native JavaScriptObject loadImage(int cnt, String imageresource, WebGlImage instance, JsArrayInteger data)  /*-{
        var canvas = $wnd.document.getElementById('dummyimage');
        //var canvas  = $wnd.document.createElement( 'canvas' );
        //canvas.id = 'dummyimage'+cnt;
        //$wnd.document.body.appendChild(canvas);
        var context = canvas.getContext('2d');
        var base_image = new Image();
        base_image.src = imageresource;
        base_image.onload = function() {
            //naturalHeight und Width sind evtl. Browserspezifisch
            var naturalWidth = base_image.naturalWidth;
            var naturalHeight = base_image.naturalHeight;
            context.drawImage(base_image, naturalWidth, naturalHeight);
            var imgData=context.getImageData(0,0,naturalWidth,naturalHeight);
            // Ablegen im ARGB Format
            for (var i=0;i<imgData.data.length;i+=4) {
                data.push((imgData.data[i]<<16)+
                (imgData.data[i+1]<<8)+
                (imgData.data[i+2]<<0)+
                (imgData.data[i+3]<<24));
            }
            //alert('image loaded '+naturalWidth+"x"+naturalHeight);
            instance.@WebGlImage::width = naturalWidth;
            instance.@WebGlImage::height = naturalHeight;
            // Der Aufruf braucht zweimal runde Klammern!
            instance.@WebGlImage::loaded()();
        }
        return base_image;
    }-* /;* /
}*/
