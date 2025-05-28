package de.yard.threed.trafficservices.util;

import de.yard.threed.core.LatLon;
import de.yard.threed.core.geometry.Polygon;
import de.yard.threed.trafficservices.model.Tile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TileResponse {
    String name;
    PolygonResponse polygon;

    public static TileResponse buildFromTile(Tile tile){
        TileResponse response=new TileResponse();
        response.setName(tile.getName());
        Polygon<LatLon> outline = tile.getOutline();
        if (outline != null){
            response.setPolygon(new PolygonResponse());
            for (int i=0;i<outline.getPointCount();i++){
                response.getPolygon().points.add(LatLonResponse.buildFromLatLon(outline.getPoint(i)));
            }
            response.getPolygon().setClosed(outline.closed);
        }
        return  response;
    }
}
