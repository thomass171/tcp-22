package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;


/**
 * Created by thomass on 25.04.15.
 */
/*30.7.21 public class WebGlCurve  implements NativeCurve {
    JavaScriptObject curve;

    protected WebGlCurve(JavaScriptObject curve) {
        this.curve = curve;
    }

    public WebGlCurve() {
        this(buildNativeCurve());
    }

    private static native JavaScriptObject buildNativeCurve()  /*-{
     var curve = new $wnd.THREE.Curve.create(
	function(s) {
		this.scale = (s === undefined) ? 10 : s;

	},

	function(t) {

		var p = 3,
			q = 4;
		t *= Math.PI * 2;
		var tx = (2 + Math.cos(q * t)) * Math.cos(p * t),
			ty = (2 + Math.cos(q * t)) * Math.sin(p * t),
			tz = Math.sin(q * t);

		return new $wnd.THREE.Vector3(tx, ty, tz).multiplyScalar(this.scale);
	}

    );
    return new curve();
}-* /;
}*/
