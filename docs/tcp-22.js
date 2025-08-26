/**
 * JS part of tcp-22.html
 */

var mazeScenes = ["skbn/SokobanWikipedia.txt","skbn/Sokoban10x10.txt","skbn/SokobanTrivial.txt","maze/Maze15x10.txt"];
// was ts171.de once, then yard.de, but due to DOS protection changed again
var host = "https://ubuntu-server.udehlavj1efjeuqv.myfritz.net/publicweb/tcp-22";

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

function launchSceneByName(sceneName,vr,nearview,hud) {

    var args = new Map();
    addCommonArgs(args, "");
    if (vr != null) {
        args.set("vrMode",vr);
    }
    args.set("enableHud",hud);
    args.set("enableNearView",nearview);
    launchScene(sceneName,args);
}



function launchVrScene() {
    var args = new Map();
    addCommonArgs(args, "");
    args.set("vrMode","VR");
    launchScene("VrScene",args);
}

function launchTrafficScene(tilename, vr, dragControl) {

    var args = new Map();
    addCommonArgs(args, "tf_");
    args.set("basename","traffic:tiles/" + tilename + ".xml");
    if (vr != null) {
        args.set("vrMode",vr);
    }
    if (dragControl != null) {
        args.set("dragControl",dragControl);
    }
    if (tilename == "Wayland") {
        args.set("initialVehicle", "mobi");
        args.set("initialLocation", "coordinate:90.0,110.5,76.0");
    }
    launchScene("BasicTravelScene",args);
}



function toggleAccordion(id) {
    var x = document.getElementById(id);
    if (x.className.indexOf("w3-show") == -1) {
        x.className += " w3-show";
    } else {
        x.className = x.className.replace(" w3-show", "");
    }
}

/**
 * init for tcp-22.html
 */
function init() {
    var url = new URL(window.location.href);
    console.log("url=" + url);
    var hostparam = url.searchParams.get("host");
    if (hostparam != null) {
        host = hostparam;
        $("#debuginfo").html("(host="+hostparam+")");
    }

    $("#inp_ctrlPanel").val("0,0,0,200,90,0");
    // With "ReferenceSpaceType" 'local' instead of 'local-floor' -0.1 is better than -0.9. 0.6 good for 1.80m player in maze
    // But for BasicTravelScene and VrScene 0 seem to be better. So better use a neutral value 0 here initially and let
    // application adjust it. And have a vector3 tuple now.
    $("#inp_offsetVR").val("0.0, 0.0, 0.0");
    // for some unknown reason traffic needs to be lowered
    $("#inp_tf_offsetVR").val("0.0, -1.0, 0.0");
    $("#inp_teamSize").val("1");

    $.get(host + "/version.html", function(responseText) {
        var s = responseText;
        var index = s.indexOf("</div>");
        if (index != -1) {
            s = s.substring(0,index);
        }
        index = s.indexOf("<div>");
        if (index != -1) {
            s = s.substring(index+5);
        }
        $("#versionInfo").html("Latest Build: " + s);
    });
}