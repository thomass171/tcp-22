/**
 * JS related to travelworld.html
 */

// The host where the scene is launched. This is not the traffic-services host.
var host = "https://ubuntu-server.udehlavj1efjeuqv.myfritz.net/publicweb/tcp-flightgear";
// The host of traffic-services. Can be customized with query param, eg. '?serviceshost=http://localhost:8080'
var serviceshost = "https://ubuntu-server.udehlavj1efjeuqv.myfritz.net";

var wellKnownAirports = [
  "EDDK", // Cologne
  "EDKB", // Hangelar
  "EGPF", // Glasgow
  "EGPH", // Edinburgh
  "EHAM", // Schiphol
  "EHTX", // Texel
  "EHLE", // Lelystad
  ];

var allAirports = new Map();

var map;
var tileGroup = null;
var foundGeoRoute = null;
var map_routeMarker = null;

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
    if (element == null) {
        console.warn("input not found:" + inputid);
    }

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
         /*while (datalist.children.length > 0) {
                    datalist.children[0].remove();
                }*/
        datalist.replaceChildren();
        return;
    }
}

/**
 * Returns the runway data structure
 */
function getRunwayOfAirport(airport, fromNumber) {
    for (var i = 0; i < airport.runways.length; i++) {
        if (airport.runways[i].fromNumber == fromNumber) {
            return airport.runways[i];
        }
    }
    console.warn("runway " + fromNumber + " not found at airport ", airport);
    return null;
}

/**
 * Returns the runway data structure
 */
function getRunwayFromUserSelection(idsuffix) {
    var airport = allAirports.get(getInputIcao(idsuffix));
    if (airport == null) {
        return null;
    }
    var inputRunway = getInputRunway(idsuffix);
    if (inputRunway == null) {
        return null;
    }
    var runway = getRunwayOfAirport(airport, inputRunway);
    return runway;
}

/**
 * After each action
 */
function updateStatus() {
    // disable all buttons as a default setting
    $("#btn_launch").prop("disabled",true);
    $("#btn_launch_vr").prop("disabled",true);
    $("#btn_launch_route").prop("disabled",true);
    $("#btn_launch_route_vr").prop("disabled",true);

    // check for possible route
    var fromRunway = getRunwayFromUserSelection("from");
    var toRunway = getRunwayFromUserSelection('to');
    //console.log("updateStatus:",fromRunway,toRunway);

    if (fromRunway != null) {
        $("#btn_launch").prop("disabled",false);
        $("#btn_launch_vr").prop("disabled",false);
    }
    if (fromRunway != null && toRunway != null) {
        // retrieve georoute
        var params ="";
        params += "runwayFromFrom=" + fromRunway.from.lat + "," + fromRunway.from.lon;
        params += "&";
        params += "runwayFromTo=" + fromRunway.to.lat + "," + fromRunway.to.lon;
        params += "&";
        params += "runwayToFrom=" + toRunway.from.lat + "," + toRunway.from.lon;
        params += "&";
        params += "runwayToTo=" + toRunway.to.lat + "," + toRunway.to.lon;

        doGet(serviceshost+"/traffic/route/buildAirportToAirport?"+params, json => {
            //console.log("got ", json);

            if (json.geoRoute) {
                showGeoRoute(json.geoRoute);
                foundGeoRoute = json.geoRoute;
            }
            updateStatusForGeoRoute();
        });
        updateStatusForGeoRoute();
    }
}

/**
 * cannot be included above because of asny load of geoRoute
 */
function updateStatusForGeoRoute() {
    if (foundGeoRoute != null) {
        $("#btn_launch_route").prop("disabled",false);
        $("#btn_launch_route_vr").prop("disabled",false);
    }

}

/**
 * 'oninput' callback for ICAO input field. idsuffix is 'from' or 'to'.
 */
function icaoChanged(idsuffix) {
    var icao = getInputIcao(idsuffix);
    console.log("icao changed to " + icao);

    var runwayoptions = document.getElementById("sel_runway_"+idsuffix).options;
    clearOptions("sel_runway_"+idsuffix);

    if (icao.length == 4) {
        loadAirport(icao, idsuffix);
    } else {
        // Only start searching with 3 characters. Starting with 2 returns too much data that takes too long and doesn't
        // fit into the select box
        if (icao.length >= 3) {
            searchAirport(icao, idsuffix);
        }
    }
}

/**
 * No search. 'icao' must be the full pure icao.
 */
function loadAirport(icao, idsuffix) {
    console.log("Loading icao " + icao);
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

/**
 * Found airports are added to the select/option list of input 'idsuffix'.
 */
function searchAirport(icao, idsuffix) {
    console.log("Searching for icao " + icao);
    doGet(serviceshost+"/traffic/airport/search/findByFilter?icao="+icao, json => {
        //console.log("got " + json);

        clearOptions("inp_icao_" + idsuffix);
        json.airports.forEach(airport => {
            //console.log("adding ", airport);
            addOption("inp_icao_" + idsuffix, airport.icao + "(" + airport.name + ")");
        });
        // always keep well known airports at the end
        addWellKnownAirportsToSelectBox(idsuffix);
        updateStatus();
    });
}

function buildLatLng(e) {
    return new L.LatLng(e.lat, e.lon);
}

function buildLatLngFromString(s) {
    var parts = s.split(",");
    return new L.LatLng(parts[0], parts[1]);
}

function buildPolygon(p) {
    var latlngs = [];
    p.points.forEach(point => {
        latlngs.push(buildLatLng(point));
    });
    return L.polygon(latlngs, {color: 'red', weight: 1, fillOpacity: 0.0 });
}

function showGeoRoute(geoRoute) {
    //console.log("geoRoute="+geoRoute);
    if (map_routeMarker != null) {
        map.removeLayer(map_routeMarker);
        map_routeMarker = null;
    }
    var parts = geoRoute.split("->");
    console.log(parts);
    var lastlatLng = null;
    var latLngs = new Array();
    parts.forEach(part => {
        var subparts = part.split(":");
        var latLng = buildLatLngFromString(subparts[1]);
        if (lastlatLng != null) {
            //L.polyline([lastlatLng,latLng], {color: 'red'}).addTo(map);
            latLngs.push([lastlatLng,latLng]);
        }
        lastlatLng = latLng;
    });
    // this is no polygon, but just a line
    map_routeMarker = L.polyline(latLngs, {color: 'blue'});
    map_routeMarker.addTo(map);
}

/**
 * Returns pure icao without appended name. idsuffix is 'from' or 'to'.
 */
function getInputIcao(idsuffix) {
    var v= $("#inp_icao_"+idsuffix).val();
    // ignore optional name
    return v.substring(0,4);
}

function getInputRunway(idsuffix) {
    var val = $("#sel_runway_"+idsuffix).val();
    // check for blank
    if (!val) {
        return null;
    }
    return val;
}

function getAircraft() {
    return $("#sel_aircraft").val();
}

function launchSingleScene(vrMode, useRoute) {

    var args = new Map();
    //addCommonArgs(args, "");
    if (vrMode != null) {
        args.set("vrMode",vrMode);
        args.set("vr-controlpanel-posrot", "0,0,0,200,90,0");
        // offsetVR not needed, other examples just use (0,0,0)
    } else {
        // not sure, but currently there seems to be nothing displayed
        args.set("enableHud", false);
    }

    //unused args.set("enableHud",hud);
    //unused args.set("enableNearView",nearview);
    var initialVehicle = getAircraft();
    if (initialVehicle == null) {
        // should not happen
        alert("no aircraft selected");
        return;
    }
    args.set("initialVehicle", initialVehicle);
    if (initialVehicle == "bluebird") {
        // double speed of sound
        args.set("vehicle.bluebird.maximumspeed", 2 * 343);
        args.set("vehicle.bluebird.acceleration", 343 * 0.02);
    }

    if (useRoute) {
        args.set("initialRoute", foundGeoRoute);
    } else {
        if (getInputIcao("from") == null) {
            // should not happen
            alert("no airport selected");
            return;
        }
        if (getInputRunway("from") == null) {
            // should not happen
            alert("no runway selected");
            return;
        }
        var runway = getRunwayFromUserSelection("from");
        if (runway == null) {
            // should not happen
            alert("unknown airport/runway");
            return;
        }

        // example: 'geo:50.85850600, 007.13874200 ,78.05
        args.set("initialLocation","geo:" + runway.from.lat + "," + runway.from.lon);
        args.set("initialHeading",runway.heading);
    }

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

function addWellKnownAirportsToSelectBox(idsuffix) {
    wellKnownAirports.forEach(a => addOption("inp_icao_" + idsuffix, a));
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

    // debug helper for geoRoutes
    var geoRouteparam = url.searchParams.get("geoRoute");
    if (geoRouteparam != null) {
        showGeoRoute(geoRouteparam);
    }

    // Setting a default value will reduce the datalist options displayed to only fitting values! This is 'intended by 'browser/spec'?
    document.getElementById("inp_icao_from").value = initialICAO;
    loadAirport(initialICAO, "from");

    clearOptions("inp_icao_from");
    addWellKnownAirportsToSelectBox("from");

    clearOptions("inp_icao_to");
    addWellKnownAirportsToSelectBox("to");

    updateStatus();
}

