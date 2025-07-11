package de.yard.threed.trafficservices;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.javacommon.MinimalisticPlatform;
import de.yard.threed.javacommon.MinimalisticPlatformFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

import java.util.HashMap;

@SpringBootApplication
public class TrafficServices implements RepositoryRestConfigurer {

    public static void main(String[] args) {
        // platform needed for logging and stringHelper in core components
        new MinimalisticPlatformFactory().createPlatform(Configuration.buildDefaultConfigurationWithArgs(args, new HashMap<>()));
        SpringApplication.run(TrafficServices.class, args);
    }
}
