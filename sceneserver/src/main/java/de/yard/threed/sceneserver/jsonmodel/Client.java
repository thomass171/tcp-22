package de.yard.threed.sceneserver.jsonmodel;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class Client {

        private OffsetDateTime connectedAt;
}
