package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayNumber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Das ist keine ThreeJs Angelegenheit, sondern rein JS.
 * Arbeitet mit Tools.js
 * <p/>
 * Created by thomass on 25.04.15.
 */
public class WebGlInput {

    /*public static boolean isKeyDown(int code){
        int i = getKeyDown();
        return(i == code);

    }*/
    static List<Integer> stillpressed = new ArrayList();

    public static JsArrayInteger getPressedKeys() {
        return getKeyDown();
    }

    public static JsArrayInteger getUpKeys() {
        return getKeyUp();
    }

    public static List<Integer> getStillPressedKeys() {
        return stillpressed;
    }

    public static List<WebGlLoaderData> getLoadedmodelList() {
        List<WebGlLoaderData> l = new ArrayList<WebGlLoaderData>();
        JsArray loaded = getLoadedmodel();
        for (int i = 0; i < loaded.length(); i++) {
            JavaScriptObject jsobject = loaded.get(i);
            l.add( WebGlLoaderData.create(jsobject));
        }
        clearLoadedmodel();
        return l;
    }

    /**
     * Alle gespeicherten Inputevents wieder loeschen und die gedrückten
     * speichern, um die immer noch gedrueckten ermitteln zu koennen.
     *
     * @param pressedkeys
     * @param upkeys
     */
    public static void close(JsArrayInteger pressedkeys, JsArrayInteger upkeys) {
        for (int i = 0; i < pressedkeys.length(); i++) {
            stillpressed.add(pressedkeys.get(i));
        }
        for (int i = 0; i < upkeys.length(); i++) {
            stillpressed.removeAll(Arrays.asList(new Integer[]{upkeys.get(i)}));
        }
        clear();
    }

    private static native void clear()  /*-{
        $wnd.lastkeydown = new Array();
        $wnd.lastkeyup = new Array();
    }-*/;

    private static native JsArrayInteger getKeyDown()  /*-{
        //if ($wnd.lastkeyevent == null) {
        //    return -1;
        //}
        //return $wnd.lastkeyevent.keyCode;
        return $wnd.lastkeydown;
    }-*/;

    private static native JsArrayInteger getKeyUp()  /*-{
        return $wnd.lastkeyup;
    }-*/;

    private static native JsArray getLoadedmodel()  /*-{
        return $wnd.loadedmodel;
    }-*/;

    private static native void clearLoadedmodel()  /*-{
        $wnd.loadedmodel = new Array();        
    }-*/;
}
