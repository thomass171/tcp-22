
/**
 * Requires naming convention in setting fields!
 */
function addCommonArgs(args, prefix) {
    args.set("vr-controlpanel-posrot",$("#inp_ctrlPanel").val());
    // there are two different fields for offsetvr
    args.set("offsetVR",$("#inp_" + prefix + "offsetVR").val());
    args.set("devmode",$("#chk_devMode").prop("checked"));
}

function launchMazeScene(vr,boxname) {

    var args = new Map();
    addCommonArgs(args, "");
    args.set("initialMaze",$("#" + boxname).val());
    args.set("enableVR",vr);
    launchScene("MazeScene",args);
}

function launchScene(scenename,args) {

    const params = new URLSearchParams()

    var url = host + "/webgl.html?scene="+scenename;

    args.forEach(function (value, key) {
        //console.log(`${key}: ${value}`);
        //html += '<option value="' + key + '">' + value + '</option>\n';
        url += "&" + key + "=" + value;
    });

    var win = window.open(url, '_blank');
}