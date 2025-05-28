package de.yard.threed.trafficservices.util;

import de.yard.threed.core.LatLon;
import de.yard.threed.trafficservices.model.Tile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LatLonResponse {
    double lat;
    double lon;

    public static LatLonResponse buildFromLatLon(LatLon latLon){
        return new LatLonResponse(latLon.getLatDeg().getDegree(), latLon.getLonDeg().getDegree());
    }
}
