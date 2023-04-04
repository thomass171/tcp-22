package de.yard.threed.services;

import de.yard.threed.services.maze.Maze;
import de.yard.threed.services.maze.MazeRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthorizationService {

    private final PasswordEncoder passwordEncoder;

    private final MazeRepository mazeRepository;

    public String encryptSecret(String secret) {

        return passwordEncoder.encode(secret);

    }

    public Boolean isSecretValid(long mazeId, String secret) {
        Optional<Maze> m = mazeRepository.findById(mazeId);
        if (!m.isPresent()) {
            return null;
        }

        return passwordEncoder.matches(secret, m.get().getSecret());
    }
}
