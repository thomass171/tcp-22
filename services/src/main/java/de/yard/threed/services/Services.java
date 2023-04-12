package de.yard.threed.services;

import de.yard.threed.services.maze.MazeValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.validation.Validator;

@SpringBootApplication
public class Services implements RepositoryRestConfigurer {
    public static void main(String[] args) {
        SpringApplication.run(Services.class, args);
    }

    @Bean
    public Validator validator() {
        return new MazeValidator();
    }

    /**
     * auto registration not working?
     */
    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener v) {
        v.addValidator("beforeSave", validator());
    }
}
