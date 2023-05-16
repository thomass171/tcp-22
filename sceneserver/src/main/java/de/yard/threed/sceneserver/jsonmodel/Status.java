package de.yard.threed.sceneserver.jsonmodel;

import lombok.Data;

import java.util.List;

@Data
public class Status {

    List<Client> clients;
    double cpuload;
    List<Entity> entities;
}
