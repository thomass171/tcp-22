/**
 * JS part of index.html
 */

var mazeScenes = ["skbn/SokobanWikipedia.txt","skbn/Sokoban10x10.txt","skbn/SokobanTrivial.txt","maze/Maze15x10.txt"];

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

function buildReferenceSceneNonVrPanel() {
    var panel = '<div class="">';

    var devMode = createCheckbox("w3-check");
    panel += "Dev Mode:" + devMode.html;
    var hud = createCheckbox();
    panel += "Hud:" + hud.html;
    var nearView = createCheckbox();
    panel += "nearView:" + nearView.html;
    var btn = createButton("Launch","w3-button  w3-round w3-khaki ");
    panel += btn.html;

    panel += "</div>"
    addPanel("Reference Scene", function () {
        return panel;
    });

    $("#" + btn.id).click(function () {
        var args = new Map();
        args.set("enableVR","false");
        args.set("devmode",$("#" + devMode.id).prop("checked"));
        args.set("enableHud",$("#" + hud.id).prop("checked"));
        args.set("enableNearView",$("#" + nearView.id).prop("checked"));
        launchScene("ReferenceScene",args);
    });
}

function buildReferenceSceneVrPanel() {
    var panel = '<div class="">';

    var devMode = createCheckbox("w3-check");
    panel += "Dev Mode:" + devMode.html;
    var ctrlPanel = createInput();
    panel += "VR Control Panel:" + ctrlPanel.html;
    var yoffsetVR = createInput();
    panel += "yoffsetVR:" + yoffsetVR.html;
    var btn = createButton("Launch","w3-button  w3-round w3-khaki ");
    panel += btn.html;

    panel += "</div>"
    addPanel("Reference Scene (VR)", function () {
        return panel;
    });

    $("#" + ctrlPanel.id).val("0,0,0,200,90,0");
    $("#" + yoffsetVR.id).val("-0.9");

    $("#" + btn.id).click(function () {
        var args = new Map();
        args.set("vr-controlpanel-posrot",$("#" + ctrlPanel.id).val());
        args.set("yoffsetVR",$("#" + yoffsetVR.id).val());
        args.set("enableVR","true");
        args.set("devmode",$("#" + devMode.id).prop("checked"));
        launchScene("ReferenceScene",args);
    });
}

function buildMazePanel() {
    var panel = '<div class="">';

    var devMode = createCheckbox("w3-check");
    panel += "Dev Mode:" + devMode.html;
    var sceneBox = createSelectBoxForMapOrArray(mazeScenes, false, "w3-select");
    panel += sceneBox.html;
    var ctrlPanel = createInput();
    panel += "VR Control Panel:" + ctrlPanel.html;
    var yoffsetVR = createInput();
    panel += "yoffsetVR:" + yoffsetVR.html;
    var enableVR = createCheckbox();
    panel += "enableVR:" + enableVR.html;
    var btn = createButton("Launch","w3-button  w3-round w3-khaki ");
    panel += btn.html;

    panel += "</div>"
    addPanel("Mazes", function () {
        return panel;
    });

    $("#" + ctrlPanel.id).val("0,0,0,200,90,0");
    $("#" + yoffsetVR.id).val("-0.9");
    $("#" + enableVR.id).prop("checked","true");

    $("#" + btn.id).click(function () {
        var args = new Map();
        args.set("initialMaze",$("#" + sceneBox.id).val());
        args.set("vr-controlpanel-posrot",$("#" + ctrlPanel.id).val());
        args.set("yoffsetVR",$("#" + yoffsetVR.id).val());
        args.set("enableVR",$("#" + enableVR.id).prop("checked"));
        args.set("devmode",$("#" + devMode.id).prop("checked"));
        launchScene("MazeScene",args);
    });
}

function buildVrScenePanel() {
    var panel = '<div class="">';

    var devMode = createCheckbox("w3-check");
    panel += "Dev Mode:" + devMode.html;
    var ctrlPanel = createInput();
    panel += "VR Control Panel:" + ctrlPanel.html;
    var yoffsetVR = createInput();
    panel += "yoffsetVR:" + yoffsetVR.html;
    var btn = createButton("Launch","w3-button  w3-round w3-khaki ");
    panel += btn.html;

    panel += "</div>"
    addPanel("VR Scene", function () {
        return panel;
    });

    $("#" + ctrlPanel.id).val("0,0,0,200,90,0");
    $("#" + yoffsetVR.id).val("-0.9");

    $("#" + btn.id).click(function () {
        var args = new Map();
        args.set("vr-controlpanel-posrot",$("#" + ctrlPanel.id).val());
        args.set("yoffsetVR",$("#" + yoffsetVR.id).val());
        args.set("enableVR","true");
        args.set("devmode",$("#" + devMode.id).prop("checked"));
        launchScene("VrScene",args);
    });
}

function init() {
    buildReferenceSceneNonVrPanel();
    buildReferenceSceneVrPanel();
    buildMazePanel();
    buildVrScenePanel();

}