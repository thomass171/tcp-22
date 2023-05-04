package de.yard.threed.servermanager;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
//its no REST @RestController
@Controller
@Slf4j
public class ServerController {

    private final ServerManagerService serverManagerService;

    @CrossOrigin
    @PostMapping("/server")
    //@ResponseBody
    public ResponseEntity<ServerInstance> start(@RequestParam String scenename, @RequestParam String gridname, @RequestParam(required = false) Integer baseport) {
        log.info("Starting server for scene {} with grid name {} on port {}", scenename, gridname, baseport);
        return new ResponseEntity<>(serverManagerService.startServer(scenename, gridname, baseport), HttpStatus.CREATED);
    }

    @CrossOrigin
    @GetMapping("/server/list")
    public ResponseEntity<ServerInstanceList> list() {
        return new ResponseEntity<>(serverManagerService.getServer(), HttpStatus.OK);
    }

    @CrossOrigin
    @DeleteMapping("/server")
    public ResponseEntity<Void> stop(@RequestParam int id) {
        serverManagerService.stopServer(id);
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }
}