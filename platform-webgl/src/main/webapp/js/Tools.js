/**
 * JS functions, for all the stuff that isnt done in GWT.
 */

'use strict';

var lastkeydown = new Array();
var lastkeyup = new Array();
// if a key is hold down, keydown events are fired endlessly by JS/browser. So keep track of received down events.
var gotkeydown = new Map();
var stats = null;
// Overridden in Main. This is NOT the GWT devmode
var isDevmode = 0;
// logger only exists in devmode
//MA34 var logger = null;//log4javascript.getDefaultLogger();

function setDebuginfo(s) {
    document.getElementById("debug").innerHTML = s;
}

/**
 * TODO KeyboardEvent.keyCode is deprecated (https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/keyCode)
 */
function ThreedonDocumentKeyDown(event) {
    //console.log("KeyDownEvent keycode=" + event.keyCode);
    if (!gotkeydown.has(event.keyCode)) {
        lastkeydown.push(event.keyCode);
        gotkeydown.set(event.keyCode,null);
    }
}
function ThreedonDocumentKeyUp(event) {
    //logger.debug("KeyUpEvent keycode=" + event.keyCode);
    lastkeyup.push(event.keyCode);
    gotkeydown.delete(event.keyCode);
}

//die async geladenenen GLTF model
var loadedmodel = new Array();
var loadedaudiobuffer = new Map();

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

/**
 * Tracking of non standard VR controller actions. Only the main trigger is standard,
 * permitting only one action per controller.
 *
 * defines commonly used keys (space/fire is covered by standard trigger)
 * temporary solution until configurable from app.

 * derived from https://codepen.io/jason-buchheim/details/zYqYGXM
 * without speedfactor and hapticActuators
 *
 * Button map for Oculus Rift
 * 0 Trigger (seems to be a button with value range 0.0-1.0)
 * 1 Grabber (seems to be a button with value range 0.0-1.0)
 * 4 -> 'X' on left and 'A' on right
 * 5 -> 'Y' on left and 'B' on right
 *
 * Thumbstick Axis
 * 3 -> values from -1.0 (pushed) up to 1.0 (pulled)
 * 2 -> values from -1.0 (left) up to 1.0 (right)
 *
 *
 */

const prevGamePads = new Map();

var debugLog = false;
var axisThresholdUpper = 0.7;
var axisThresholdLower = 0.2;
var stickFired = [];

const vrControllerEventMap = new Map();
// 84=t(eleport),77=m(enu), further not known??
vrControllerEventMap.set("left-button-4-down", function () {lastkeydown.push(77)});
vrControllerEventMap.set("left-button-4-up", function () {lastkeyup.push(77)});
vrControllerEventMap.set("left-button-5-down", function () {lastkeydown.push(84)});
vrControllerEventMap.set("left-button-5-up", function () {lastkeyup.push(84)});
vrControllerEventMap.set("right-button-4-down", function () {lastkeydown.push(77)});
vrControllerEventMap.set("right-button-4-up", function () {lastkeyup.push(77)});
vrControllerEventMap.set("right-button-5-down", function () {lastkeydown.push(84)});
vrControllerEventMap.set("right-button-5-up", function () {lastkeyup.push(84)});
// 37=curleft, 38=curup ,39=curright,40=curdown
vrControllerEventMap.set("right-stick-left", function () {lastkeydown.push(37)});
vrControllerEventMap.set("right-stick-left-center", function () {lastkeyup.push(37)});
vrControllerEventMap.set("right-stick-right", function () {lastkeydown.push(39)});
vrControllerEventMap.set("right-stick-right-center", function () {lastkeyup.push(39)});
vrControllerEventMap.set("right-stick-up", function () {lastkeydown.push(38)});
vrControllerEventMap.set("right-stick-up-center", function () {lastkeyup.push(38)});
vrControllerEventMap.set("right-stick-down", function () {lastkeydown.push(40)});
vrControllerEventMap.set("right-stick-down-center", function () {lastkeyup.push(40)});
// 87=w,83=s,65=a,68=d
vrControllerEventMap.set("left-stick-left", function () {lastkeydown.push(65)});
vrControllerEventMap.set("left-stick-left-center", function () {lastkeyup.push(65)});
vrControllerEventMap.set("left-stick-right", function () {lastkeydown.push(68)});
vrControllerEventMap.set("left-stick-right-center", function () {lastkeyup.push(68)});
vrControllerEventMap.set("left-stick-up", function () {lastkeydown.push(87)});
vrControllerEventMap.set("left-stick-up-center", function () {lastkeyup.push(87)});
vrControllerEventMap.set("left-stick-down", function () {lastkeydown.push(83)});
vrControllerEventMap.set("left-stick-down-center", function () {lastkeyup.push(83)});

// grab button, or is it a stick?
// 71=g,74=j, no idea where 73 is
vrControllerEventMap.set("left-button-1-down", function () {lastkeydown.push(71)});
vrControllerEventMap.set("left-button-1-up", function () {lastkeyup.push(71)});
vrControllerEventMap.set("right-button-1-down", function () {lastkeydown.push(74)});
vrControllerEventMap.set("right-button-1-up", function () {lastkeyup.push(74)});

function pollVrControllerEvents(renderer) {
  var handedness = "unknown";

  //determine if we are in an xr session
  const session = renderer.xr.getSession();
  let i = 0;

  if (session) {

    //a check to prevent console errors if only one input source
    if (isIterable(session.inputSources)) {
      for (const source of session.inputSources) {
        if (source && source.handedness) {
          handedness = source.handedness; //left or right controllers
        }
        if (!source.gamepad) continue;
        const controller = renderer.xr.getController(i++);
        const old = prevGamePads.get(source);
        const data = {
          handedness: handedness,
          buttons: source.gamepad.buttons.map((b) => b.value),
          axes: source.gamepad.axes.slice(0)
        };
        if (old) {
          data.buttons.forEach((value, button) => {
            // handlers for buttons
            // When a button is pressed, its value changes from 0 to 1.
            // The purpose of the math.abs seems to be for checking whether it is still pressed. Not used for now.
            // Buttons seems to have only values 0 and 1
            if (value !== old.buttons[button] /*|| Math.abs(value) > 0.8*/) {
              if (debugLog) console.log(data.handedness + " button " + button + " value changed from " + old.buttons[button] + " to " + value);
              //check if it is 'all the way pushed'. 24.1.24: Grabber seems to be stick like and might not reach 1.00 at all.
              //if (value === 1) {
              if (value > 0.9) {
                if (debugLog) console.log("Button " + button + " down");
                checkVrControllerEvent(data.handedness + "-button-" + button + "-down");
              } else {
                if (debugLog) console.log("Button " + button + " up");
                checkVrControllerEvent(data.handedness + "-button-" + button + "-up");
              }
            }
          });
          data.axes.forEach((value, axis) => {
            // handlers for thumbsticks
            // convert thumbstick action to button event
            if (Math.abs(value) > axisThresholdUpper) {
              if (debugLog) console.log(data.handedness + " axis " + axis + " value exceeds threshold " + value);
              // avoid repeated events for one movement
              if (!stickFired[data.handedness+axis]) {
                  if (axis == 2) {
                    //left and right axis on thumbsticks
                    var dir = (value<0)?"left":"right";
                    checkVrControllerEvent(data.handedness + "-stick-" + dir);
                    stickFired[data.handedness+axis] = dir;
                  }
                  if (axis == 3) {
                    //up and down axis on thumbsticks
                    var dir = (value<0)?"up":"down";
                    checkVrControllerEvent(data.handedness + "-stick-" + dir);
                    stickFired[data.handedness+axis] = dir;
                  }
              }
            }
            if (Math.abs(value) < axisThresholdLower) {
              if (stickFired[data.handedness+axis] != null) {
                var firedDir = stickFired[data.handedness+axis];
                if (debugLog) console.log(data.handedness + " axis " + axis + " value back below threshold " + value, firedDir);
                if (axis == 2) {
                  //left and right axis on thumbsticks
                  checkVrControllerEvent(data.handedness + "-stick-" + firedDir + "-center");
                }
                if (axis == 3) {
                  //up and down axis on thumbsticks
                  checkVrControllerEvent(data.handedness + "-stick-" + firedDir + "-center");
                }
                stickFired[data.handedness+axis] = null;
              }
            }
          });
        }
        prevGamePads.set(source, data);
      }
    }
  }
}

function isIterable(obj) {
  // checks for null and undefined
  if (obj == null) {
    return false;
  }
  return typeof obj[Symbol.iterator] === "function";
}

function checkVrControllerEvent(eventKey) {
    if (debugLog) console.log("checkVrControllerEvent for " + eventKey);

    var eventFunction = vrControllerEventMap.get(eventKey);
    if (eventFunction != null) {
        eventFunction();
    } else {
        if (debugLog) console.log("No event defined for " + eventKey);
    }
}