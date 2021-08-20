/**
 * JS Funktionen, fuer das was nicht mit GWT gemacht wird.
 */

'use strict';

var lastkeydown = new Array();
var lastkeyup = new Array();
var stats = null;
// Overridden in Main. This is NOT the GWT devmode
var isDevmode = 0;
// logger only exists in devmode
//MA34 var logger = null;//log4javascript.getDefaultLogger();

function setDebuginfo(s) {
    document.getElementById("debug").innerHTML = s;
}

function ThreedonDocumentKeyDown(event) {
    //logger.debug("KeyDownEvent keycode=" + event.keyCode);
    lastkeydown.push(event.keyCode);
}
function ThreedonDocumentKeyUp(event) {
    //logger.debug("KeyUpEvent keycode=" + event.keyCode);
    lastkeyup.push(event.keyCode);
}

//die async geladenenen GLTF model
var loadedmodel = new Array();

// VRButton is not available from "GWT native $wnd" (because its a module?)
function createVrButton(renderer) {
    return VRButton.createButton( renderer );
}

// callback to be used via ThreeJSs setAnimationLoop()
var webGlSceneRendererInstance;
function renderCallback() {
    webGlSceneRendererInstance();
}

document.addEventListener("keydown", ThreedonDocumentKeyDown, false);
document.addEventListener("keyup", ThreedonDocumentKeyUp, false);
