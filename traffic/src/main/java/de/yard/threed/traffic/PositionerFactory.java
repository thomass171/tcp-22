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
            Double elevation = geo.getElevationM();
            if (elevation == null) {
                ElevationProvider elevationProvider = (ElevationProvider) SystemManager.getDataProvider(SystemManager.DATAPROVIDERELEVATION);
                if (elevationProvider == null) {
                    logger.debug("No elevationProvider");
                    // cannot be solved by waiting
                    return new PositionerFactoryResult(null, NOTYET, " missing elevationProvider");
                }
                elevation = elevationProvider.getElevation(geo.getLatDeg().getDegree(), geo.getLonDeg().getDegree());
            }
            if (elevation == null) {
                logger.warn("No elevation for " + geo + " of initialLocation");
                SystemManager.putRequest(RequestRegistry.buildLoadScenery(geo));
                return new PositionerFactoryResult(null, NOTYET, " missing elevation");
            }
            geo = GeoCoordinate.fromLatLon(geo, elevation);
            if (optionalHeading == null) {
                optionalHeading = "0";
            }
            EllipsoidCalculations rbcp = TrafficHelper.getEllipsoidConversionsProviderByDataprovider();
            Vector3 position = rbcp.toCart(geo, null, null);
            Quaternion rotation = rbcp.buildZUpRotation(geo, new Degree(Util.parseDouble(optionalHeading)), new Degree(0));

            return new PositionerFactoryResult(new SphereVehiclePositioner(position, rotation), SUCCESS, null);
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
