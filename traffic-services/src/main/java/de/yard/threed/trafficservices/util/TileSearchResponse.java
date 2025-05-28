package de.yard.threed.trafficservices.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * just a list of tiles/names
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TileSearchResponse {
    List<TileResponse> tiles;
}
