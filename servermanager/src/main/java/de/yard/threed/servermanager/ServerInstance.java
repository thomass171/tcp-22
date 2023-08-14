package de.yard.threed.servermanager;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerInstance {

    // just a unique subsequent id
    int id;

    // not available until java 9
    long pid;

    OffsetDateTime started;

    long startedMillis;

    // no camel case for scenename and baseport to comply to query parameter
    String scenename;

    Map<String, String> argMap;

    int baseport;

    String state;

    int upTime;
}
