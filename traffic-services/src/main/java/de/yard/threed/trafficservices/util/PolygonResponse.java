package de.yard.threed.trafficservices.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PolygonResponse {
    final List<LatLonResponse> points = new ArrayList();
private boolean closed=false;
}
