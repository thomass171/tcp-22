package de.yard.threed.trafficservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

@SpringBootApplication
public class TrafficServices implements RepositoryRestConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(TrafficServices.class, args);
    }
}
