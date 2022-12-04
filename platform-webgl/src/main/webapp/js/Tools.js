/**
 * JS functions, for all the stuff that isnt done in GWT.
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

/**
 * TODO KeyboardEvent.keyCode is deprecated (https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/keyCode)
 */
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

/**
 * Tracking of non standard VR controller actions. Only the main trigger is standard,
 * permitting only one action per controller.
 *
 * derived from https://codepen.io/jason-buchheim/details/zYqYGXM
 * without speedfactor and hapticActuators
 *
 * Button map for Oculus Rift
 * 0 Trigger
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

var debugLog = true;
var axisThreshold = 0.7;
var stickFired = [];
stickFired['right2'] = false;
stickFired['right3'] = false;
stickFired['left2'] = false;
stickFired['left3'] = false;

const vrControllerEventMap = new Map();
vrControllerEventMap.set("right-button-4", function () {console.log("A pressed")});
vrControllerEventMap.set("right-button-5", function () {console.log("B pressed")});
vrControllerEventMap.set("right-stick-left", function () {lastkeydown.push(37)});
vrControllerEventMap.set("right-stick-right", function () {lastkeydown.push(39)});
vrControllerEventMap.set("right-stick-up", function () {lastkeydown.push(38)});
vrControllerEventMap.set("right-stick-down", function () {lastkeydown.push(40)});
vrControllerEventMap.set("left-stick-left", function () {lastkeydown.push(37)});
vrControllerEventMap.set("left-stick-right", function () {lastkeydown.push(39)});
vrControllerEventMap.set("left-stick-up", function () {lastkeydown.push(38)});
vrControllerEventMap.set("left-stick-down", function () {lastkeydown.push(40)});

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
              //check if it is 'all the way pushed'
              if (value === 1) {
                if (debugLog) console.log("Button" + button + "Down");
                checkVrControllerEvent(data.handedness + "-button-" + button);
              } else {
                 if (debugLog) console.log("Button" + button + "Up");
                 // No action currently for releasing a button
              }
            }
          });
          data.axes.forEach((value, axis) => {
            // handlers for thumbsticks
            // convert thumbstick action to button event
            if (Math.abs(value) > axisThreshold) {
              if (debugLog) console.log(data.handedness + " axis " + axis + " values exceeds threshold:", value);
              // avoid repeated events for one movement
              if (!stickFired[data.handedness+axis]) {
                  if (axis == 2) {
                    //left and right axis on thumbsticks
                    checkVrControllerEvent(data.handedness + "-stick-" + ((value<0)?"left":"right"));
                    stickFired[data.handedness+axis] = true;
                  }
                  if (axis == 3) {
                    //up and down axis on thumbsticks
                    checkVrControllerEvent(data.handedness + "-stick-" + ((value<0)?"up":"down"));
                    stickFired[data.handedness+axis] = true;
                  }
              }
            } else {
              stickFired[data.handedness+axis] = false;
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
    var eventFunction = vrControllerEventMap.get(eventKey);
    if (eventFunction != null) {
        eventFunction();
    } else {
        if (debugLog) console.log("No event defined for " + eventKey);
    }
}