package de.yard.threed.traffic;

import de.yard.threed.core.JsonHelper;
import de.yard.threed.core.platform.NativeJsonArray;
import de.yard.threed.core.platform.NativeJsonObject;
import de.yard.threed.core.platform.NativeJsonValue;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.trafficcore.model.Airport;
import de.yard.threed.trafficcore.model.Runway;

/**
 * There are also other JsonUtil/JsonHelper classes.
 */
public class JsonUtil {
    public static String TAG_GROUNDNETXML = "groundNetXml";
    public static String TAG_ICAO = "icao";
    public static String TAG_CENTERLAT = "centerLat";
    public static String TAG_CENTERLON = "centerLon";
    public static String TAG_RUNWAYS = "runways";

    public static String TAG_FROMLAT = "fromLat";
    public static String TAG_FROMLON = "fromLon";
    public static String TAG_FROMNUMBER = "fromNumber";
    public static String TAG_TOLAT = "toLat";
    public static String TAG_TOLON = "toLon";
    public static String TAG_TONUMBER = "toNumber";
    public static String TAG_WIDTH = "width";
    public static String TAG_ENTERNODEFROMGROUNDNET = "enternodefromgroundnet";

    public static String toJson(Airport airport) {
        String response = "{" +
                JsonHelper.buildProperty(TAG_ICAO, airport.getIcao()) + "," +
                JsonHelper.buildProperty(TAG_CENTERLAT, airport.getCenter().getLatDeg().getDegree()) + "," +
                JsonHelper.buildProperty(TAG_CENTERLON, airport.getCenter().getLonDeg().getDegree()) + "," +
                // GWT JsonParser doesn't like line breaks.
                JsonHelper.buildProperty(TAG_GROUNDNETXML, airport.getGroundNetXml().replace("\n","")) + ",\"runways\":[";
        int index = 0;
        if (airport.getRunways() != null) {
            for (Runway runway : airport.getRunways()) {
                if (index > 0) {
                    response += ",";
                }
                response += "" + toJson(runway) + "";
                index++;
            }
        }
        response += "]}";
        return response;
    }

    public static String toJson(Runway runway) {
        String response = "{" +
                JsonHelper.buildProperty(TAG_FROMLAT, runway.getFrom().getLatDeg().getDegree()) + "," +
                JsonHelper.buildProperty(TAG_FROMLON, runway.getFrom().getLonDeg().getDegree()) + "," +
                JsonHelper.buildProperty(TAG_FROMNUMBER, runway.getFromNumber()) + "," +
                JsonHelper.buildProperty(TAG_TOLAT, runway.getTo().getLatDeg().getDegree()) + "," +
                JsonHelper.buildProperty(TAG_TOLON, runway.getTo().getLonDeg().getDegree()) + "," +
                JsonHelper.buildProperty(TAG_TONUMBER, runway.getToNumber()) + "," +
                JsonHelper.buildProperty(TAG_WIDTH, runway.getWidth()) + "," +
                JsonHelper.buildProperty(TAG_ENTERNODEFROMGROUNDNET, runway.enternodefromgroundnet) +
                "}";
        return response;
    }

    public static Airport toAirport(String s) {
        NativeJsonValue json = Platform.getInstance().parseJson(s);
        NativeJsonObject ap = json.isObject();
        String groundNetXml = JsonHelper.getString(ap, TAG_GROUNDNETXML);
        Airport airport = new Airport(JsonHelper.getString(ap, "icao"),
                (double)JsonHelper.getDouble(ap, "centerLat"),
                (double)JsonHelper.getDouble(ap, "centerLon"));
        airport.setGroundNetXml(groundNetXml);

        NativeJsonArray rps = ap.get(TAG_RUNWAYS).isArray();
        Runway[] runways = new Runway[rps.size()];
        for (int i = 0; i < runways.length; i++) {
            NativeJsonObject rp = rps.get(i).isObject();
            runways[i] = new Runway((double)JsonHelper.getDouble(rp, TAG_FROMLAT), (double)JsonHelper.getDouble(rp, TAG_FROMLON),
                    JsonHelper.getString(rp, TAG_FROMNUMBER),
                    (double)JsonHelper.getDouble(rp, TAG_TOLAT), (double)JsonHelper.getDouble(rp, TAG_TOLON),
                    JsonHelper.getString(rp, TAG_TONUMBER), (double)JsonHelper.getDouble(rp, TAG_WIDTH));
            runways[i].enternodefromgroundnet = JsonHelper.getString(rp, TAG_ENTERNODEFROMGROUNDNET);
        }
        airport.setRunways(runways);
        return airport;
    }
}
