package de.yard.threed.services;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@AllArgsConstructor
//its no REST @RestController
@Controller
@Slf4j
public class ServicesController {

    private final AuthorizationService authorizationService;

    @CrossOrigin
    @GetMapping("/mazes/confirmsecret")
    public ResponseEntity<Void> confirmSecret(@RequestParam("mazeid") long mazeId, @RequestParam("secret") String secret) {

        Boolean b = authorizationService.isSecretValid(mazeId,secret);
        if (b==null){
            return new ResponseEntity<>( HttpStatus.NOT_FOUND);
        }
        if (!b.booleanValue()){
            return new ResponseEntity<>( HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>( HttpStatus.OK);
    }
}