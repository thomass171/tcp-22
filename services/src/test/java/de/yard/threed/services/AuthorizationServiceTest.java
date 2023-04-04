package de.yard.threed.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest()
@Slf4j
public class AuthorizationServiceTest {

    @Autowired
    private AuthorizationService authorizationService;

    @AfterEach
    void tearDown() {
    }

    @Test
    public void testEncryption() {

        String s = authorizationService.encryptSecret("Baskerville");
        log.debug("encryption={}", s);
        assertNotNull(s);

    }
}