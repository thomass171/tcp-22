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

    // not available unitl java 9
    long pid;

    OffsetDateTime started;

    long startedMillis;

    String sceneName;

    String gridname;

    int basePort;

    String state;

    int upTime;
}
