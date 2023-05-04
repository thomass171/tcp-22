package de.yard.threed.servermanager;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

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

    // no camel case for scenename, gridname and baseport to comply to query parameter
    String scenename;

    String gridname;

    int baseport;

    String state;

    int upTime;
}
