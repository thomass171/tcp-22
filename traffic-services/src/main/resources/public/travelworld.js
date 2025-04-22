/**
 * JS related to travelworld.html
 */

var host = "https://ubuntu-server.udehlavj1efjeuqv.myfritz.net/publicweb/tcp-flightgear";
var TRAFFIC_SERVICES_BASEURL = "https://ubuntu-server.udehlavj1efjeuqv.myfritz.net/traffic";
var wellKnownAirports = [ "EDDK", "EDKB"];

var allAirports = new Map();

var map;

class AlbumElement {
    /**
     * fileName is always the 'real' name, even if a thumbnail exists.
     */
    constructor(fileName, thumbnailName) {
        console.log("Building AlbumElement with fileName '" + fileName + "' and thumbnailName '" + thumbnailName + "'");
        this.detailImageId = -1;
    }

    getHtmlFullImage() {
    }
}

/**
 *
 */
function initMap() {
    $("#album_title").html("Karte");

    // If favorites are used, show only favorites per default. Otherwise show all.
    //$("#optFavorites").prop("checked", usesFavorites(albumDefinition));

var center = new L.latLng(52.0,7.2);
        console.log("center",center);
        //albumDefinition.map.area = area;
        //albumDefinition.map.height = "280px";

    //if (!isUndefined(albumDefinition.map)) {
        setCss("map", "height", "280px");
        var zoom = 13;
        map = L.map('map').setView(center, zoom);
        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        }).addTo(map);
    //}


}

/**
 *useful??
 */
function populateOptions(options, valueArray, getter, withEmptyLine) {
    console.log("populateOptions");
    var idx = 0;

}

function clearOptions(inputid) {
    var element = document.getElementById(inputid);
    //console.log("element:",element);

    if (element.options != null) {
        while (element.options.length > 0) {
            element.options.remove(0);
        }
        return;
    }
    if (element.list != null) {
        var datalist = element.list;//document.getElementById(element.list);
        //console.log("clear of datalist ", datalist, datalist.children.length);
        while (datalist.children.length > 0) {
            datalist.children[0].remove();
        }
        return;
    }
}

function addOption(inputid, value) {
    var element = document.getElementById(inputid);
    //console.log("element:",element);

    if (element.options != null) {
        element.options.add(new Option(value));
        return;
    }
    if (element.list != null) {
        var datalist = element.list;//document.getElementById(element.list);
        //console.log("datalist of" + element.list, datalist);
        // not possible datalist.add(value);
        var option = document.createElement('option');
        option.value = value;
        datalist.appendChild(option);
        return;
    }
}

function getRunwayOfAirport(airport, runway) {
    for (var i = 0; i < airport.runways.length; i++) {
        if (airport.runways[i].fromNumber == runway) {
            return airport.runways[i];
        }
    }
    console.warn("runway not found", runway);
    return null;
}

/**
 * After each action
 */
function updateStatus() {
    //$("#scanstatus").html("(" + loadResult.loaded +  "/" + loadResult.totalToScan + ", " + loadResult.failed + " failed)");
}

/**
 * 'oninput' callback for ICAO input field
 */
function icaoChanged(idsuffix) {
    var icao = getICAO(idsuffix);
    console.log("icao changed to " + icao);

    var runwayoptions = document.getElementById("sel_runway_"+idsuffix).options;
    clearOptions("sel_runway_"+idsuffix);

    if (icao.length == 4) {
        loadAirport(icao, idsuffix);
    }
}

function loadAirport(icao, idsuffix) {
    doGet(TRAFFIC_SERVICES_BASEURL+"/airport/"+icao, json => {
        //console.log("got " + json);

        allAirports.set(icao, json);
        addOption("sel_runway_"+idsuffix, " ");
        json.runways.forEach(runway => {
            //console.log("adding ", runway);
            addOption("sel_runway_"+idsuffix, runway.fromNumber);
            console.log("runway:", runway);
            var latlng = new L.LatLng(runway.fromLat, runway.fromLon);
            var point = L.Projection.Mercator.project(latlng);
            console.log(latlng, point);
             var zoom = 11;
             map.setView(latlng, zoom);
             var marker = L.marker([latlng.lat, latlng.lng]).addTo(map);
        });
        updateStatus();
    });
}

function getICAO(idsuffix) {
    return $("#inp_takeoff_"+idsuffix).val();
}

function getRunway(idsuffix) {
    return $("#sel_runway_"+idsuffix).val();
}

function getAircraft() {
    return $("#sel_aircraft").val();
}

function launchSingleScene(vrMode) {

    var args = new Map();
    //addCommonArgs(args, "");
    if (vrMode != null) {
        args.set("vrMode",vrMode);
    }
    //unused args.set("enableHud",hud);
    //unused args.set("enableNearView",nearview);
    var initialVehicle = getAircraft();
    if (initialVehicle == null) {
        // should not happen
        alert("no aircraft selected");
        return;
    }
    args.set("initialVehicle",getAircraft());

    /*not yet if (initialRoute != null) {
        args.set("initialRoute",initialRoute);
    }*/
    if (getICAO("from") == null) {
        // should not happen
        alert("no airport selected");
        return;
    }
    if (getRunway("from") == null) {
        // should not happen
        alert("no runway selected");
        return;
    }
    var airport = allAirports.get(getICAO("from"));
    if (airport == null) {
        // should not happen
        alert("unknown airport");
        return;
    }
    var runway = getRunwayOfAirport(airport, getRunway("from"));
    if (runway == null) {
        // should not happen
        alert("unknown runway");
        return;
    }

    // example: 'geo:50.85850600, 007.13874200 ,78.05
    args.set("initialLocation","geo:" + runway.fromLat + "," + runway.fromLon);
    args.set("initialHeading",runway.heading);

    launchScene('TravelScene',args);
}

/**
 * init for travelworld.html
 */
function init() {
    var url = new URL(window.location.href);
    console.log("url=" + url);
    var hostparam = url.searchParams.get("host");
    if (hostparam != null) {
        host = hostparam;
        console.log("host="+host);
    }

    var initialICAO = "EDDK";
    var icaoparam = url.searchParams.get("icao");
    if (icaoparam != null) {
        initialICAO = icaoparam;
        console.log("initialICAO="+initialICAO);
    }

    initMap();

    document.getElementById("inp_takeoff_from").value = initialICAO;
    loadAirport(initialICAO, "from");

    clearOptions("inp_takeoff_from");
    wellKnownAirports.forEach(a => addOption("inp_takeoff_from", a));

    updateStatus();
}

