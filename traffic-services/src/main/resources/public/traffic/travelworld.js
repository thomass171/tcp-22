/**
 * JS related to travelworld.html
 */

// The host where the scene is launched. This is not the traffic-services host.
var host = "https://ubuntu-server.udehlavj1efjeuqv.myfritz.net/publicweb/tcp-flightgear";
// The host of traffic-services. Can be customized with query param, eg. '?serviceshost=http://localhost:8080'
var serviceshost = "https://ubuntu-server.udehlavj1efjeuqv.myfritz.net";

var wellKnownAirports = [ "EDDK", "EDKB"];

var allAirports = new Map();

var map;
var tileGroup = null;

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

    // Just an arbirary start center
    var center = new L.latLng(52.0,7.2);
    console.log("center",center);

    setCss("map", "height", "280px");
    var zoom = 13;
    map = L.map('map').setView(center, zoom);
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(map);
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

/**
 * Also for datalists of inputs
 */
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

/**
 * Also for datalists of inputs
 */
function clearOptions(inputid) {
    var element = document.getElementById(inputid);
    //console.log("element:",element,inputid);

    if (element.options != null) {
        //not working element.options.innerHTML = "";
        //$("#droplist").empty();
        while (element.options.length > 0) {
                element.remove(0);
            }
        return;
    }
    if (element.list != null) {
        var datalist = element.list;
        datalist.replaceChildren();
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
    } else {
        if (icao.length >= 2) {
            searchAirport(icao, idsuffix);
        }
    }
}

/**
 * No search. 'icao' must be the full pure icao.
 */
function loadAirport(icao, idsuffix) {
    doGet(serviceshost+"/traffic/airport/"+icao, json => {
        //console.log("got " + json);

        allAirports.set(icao, json);
        clearOptions("sel_runway_"+idsuffix);
        addOption("sel_runway_"+idsuffix, " ");
        json.runways.forEach(runway => {
            //console.log("adding ", runway);
            addOption("sel_runway_"+idsuffix, runway.fromNumber);
            console.log("runway:", runway);
            var latlng = buildLatLng(runway.from);
            var point = L.Projection.Mercator.project(latlng);
            //console.log(latlng, point);
             var zoom = 11;
             map.setView(latlng, zoom);
             var marker = L.marker([latlng.lat, latlng.lng]).addTo(map);
        });
        updateStatus();
    });
}

function searchAirport(icao, idsuffix) {
    doGet(serviceshost+"/traffic/airport/search/findByFilter?icao="+icao, json => {
        //console.log("got " + json);

        clearOptions("inp_takeoff_" + idsuffix);
        json.airports.forEach(airport => {
            //console.log("adding ", airport);
            addOption("inp_takeoff_" + idsuffix, airport.icao + "(" + airport.name + ")");
        });
        updateStatus();
    });
}

function buildLatLng(e) {
    return new L.LatLng(e.lat, e.lon);
}

function buildPolygon(p) {
    var latlngs = [];
    p.points.forEach(point => {
        latlngs.push(buildLatLng(point));
    });
    return L.polygon(latlngs, {color: 'red', weight: 1, fillOpacity: 0.0 });
}

/**
 * Returns pure icao without appended name
 */
function getICAO(idsuffix) {
    var v= $("#inp_takeoff_"+idsuffix).val();
    // ignore optional name
    return v.substring(0,4);
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
    args.set("initialLocation","geo:" + runway.from.lat + "," + runway.from.lon);
    args.set("initialHeading",runway.heading);

    launchScene('TravelScene',args);
}

function showSceneryTilesChanged() {

    if ($("#cb_scenerytiles").is(":checked")) {
        clearTileGroup();
        tileGroup = L.layerGroup();
        doGet(serviceshost+"/traffic/tile/search/findByFilter", json => {
            json.tiles.forEach(tile => {
                buildPolygon(tile.polygon).addTo(tileGroup);
            });
        });
        tileGroup.addTo(map);
    } else {
        clearTileGroup();
    }
    updateStatus();
}

function clearTileGroup() {
    if (tileGroup != null) {
        tileGroup.removeFrom(map);
    }
    tileGroup = null;
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
        $("#debuginfo").html("(host="+hostparam+")");
    }
    var serviceshostparam = url.searchParams.get("serviceshost");
    if (serviceshostparam != null) {
        serviceshost = serviceshostparam;
        $("#debuginfo").html("(serviceshost="+serviceshostparam+")");
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

