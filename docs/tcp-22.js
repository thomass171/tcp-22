/**
 * JS part of tcp-22.html
 */

var mazeScenes = ["skbn/SokobanWikipedia.txt","skbn/Sokoban10x10.txt","skbn/SokobanTrivial.txt","maze/Maze15x10.txt"];
var host = "https://ts171.de";

var chk_devMode;
var inp_ctrlPanel;
var inp_yoffsetVR;

function addPanel(label, contentProvider, optionalElement) {
    //console.log("addPanel " + label);
    var headerid = "header" + getUniqueId();
    var newElement = '<div id="' + headerid + '" class="w3-bar w3-black">';
    newElement += '<div class="w3-bar-item">' + label + '</div>';
    if (optionalElement != null) {
        newElement += optionalElement;
    }
    newElement += '</div>';
    var id = "row" + getUniqueId();
    newElement += '<div id="' + id + '" class="w3-panel w3-hide w3-show"><div>' + contentProvider() + '</div></div>'
    $("#panellist").append(newElement);

}

function launchReferenceScene(vr,nearview,hud) {

    var args = new Map();
    addCommonArgs(args);
    args.set("enableVR",vr);
    args.set("enableHud",hud);
    args.set("enableNearView",nearview);
    launchScene("ReferenceScene",args);
}

function launchMazeScene(vr) {

    var args = new Map();
    addCommonArgs(args);
    args.set("initialMaze",$("#sel_sceneBox").val());
    args.set("enableVR",vr);
    launchScene("MazeScene",args);
}

function launchVrScene() {
    var args = new Map();
    addCommonArgs(args);
    args.set("enableVR","true");
    launchScene("VrScene",args);
}

function addCommonArgs(args) {
    args.set("vr-controlpanel-posrot",$("#inp_ctrlPanel").val());
    args.set("yoffsetVR",$("#inp_yoffsetVR").val());
    args.set("devmode",$("#chk_devMode").prop("checked"));
}

function init() {
    var url = new URL(window.location.href);
    console.log("url=" + url);
    var hostparam = url.searchParams.get("host");
    if (hostparam != null) {
        host = hostparam;
    }

    $("#inp_ctrlPanel").val("0,0,0,200,90,0");
    $("#inp_yoffsetVR").val("-0.9");
}