package de.yard.threed.traffic;

import de.yard.threed.core.BooleanHolder;
import de.yard.threed.core.Degree;
import de.yard.threed.core.Event;
import de.yard.threed.core.LatLon;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Payload;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.traffic.flight.FlightLocation;
import de.yard.threed.traffic.geodesy.ElevationProvider;
import de.yard.threed.core.GeoCoordinate;
import de.yard.threed.trafficcore.model.SmartLocation;

import static de.yard.threed.engine.ecs.TeleporterSystem.EVENT_POSITIONCHANGED;

public class PositionerFactory {
    private static Log logger = Platform.getInstance().getLog(PositionerFactory.class);

    public static int SUCCESS = 1;

    // eg. with wrong syntax
    public static int IMPOSSIBLE = 2;
    // eg. missing graph/groundnet, missing terrain
    public static int NOTYET = 3;

    public static PositionerFactoryResult buildFromLocation(SmartLocation smartLocation, String optionalHeading) {
        Vector3 v = smartLocation.getCoordinate();
        GeoCoordinate geo = smartLocation.getGeoCoordinate();
        if (v != null) {
            // no special rotation needed. Vehicle is expected to be in 'FG space'
            Quaternion baseRotation = new Quaternion();
            return new PositionerFactoryResult(new SphereVehiclePositioner(v, baseRotation), SUCCESS, null);
        }
        if (geo != null) {
            // only 3D sphere
            BooleanHolder shouldAbort = new BooleanHolder(false);
            EllipsoidCalculations rbcp = TrafficHelper.getEllipsoidConversionsProviderByDataprovider();
            ElevationProvider elevationProvider = (ElevationProvider) SystemManager.getDataProvider(SystemManager.DATAPROVIDERELEVATION);
            EllipsoidCalculations ec = TrafficHelper.getEllipsoidConversionsProviderByDataprovider();
            // GeoCoordinate gc = new GeoCoordinate(latLon.getLatDeg(), latLon.getLonDeg(), 0);
            Vector3 cart = rbcp.toCart(geo, elevationProvider, geoCoordinate -> {
                logger.debug("No elevation for " + geoCoordinate + " of initialLocation");
                if (!shouldAbort.getValue()) {
                    // trigger terrain loading (but only once)
                    LocalTransform loc = new LocalTransform(ec.toCart(geo), new Quaternion());
                    SystemManager.sendEvent(new Event(EVENT_POSITIONCHANGED, new Payload(new Object[]{loc})));
                    shouldAbort.setValue(true);
                }
            });
            if (shouldAbort.getValue()) {
                return new PositionerFactoryResult(null, NOTYET, " missing elevation");
            }
            if (optionalHeading == null) {
                optionalHeading = "0";
            }
            FlightLocation flightLocation = new FlightLocation(geo, new Degree(Util.parseDouble(optionalHeading)));
            LocalTransform posrot = flightLocation.toPosRot(ec);
            return new PositionerFactoryResult(new SphereVehiclePositioner(posrot.position, posrot.rotation), SUCCESS, null);
        }
        return new PositionerFactoryResult(null, IMPOSSIBLE, "not supported " + smartLocation);
    }

    static class PositionerFactoryResult {
        public VehiclePositioner positioner;
        int status;
        String msg;

        public PositionerFactoryResult(VehiclePositioner positioner, int status, String msg) {
            this.positioner = positioner;
            this.status = status;
            this.msg = msg;
        }
    }
}
