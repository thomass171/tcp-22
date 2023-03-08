package de.yard.threed.servermanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServerManager {
    public static void main(String[] args) {
        SpringApplication.run(ServerManager.class, args);
    }
}
