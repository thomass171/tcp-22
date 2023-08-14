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

import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
//its no REST @RestController
@Controller
@Slf4j
public class ServerController {

    private final ServerManagerService serverManagerService;

    @CrossOrigin
    @PostMapping("/server")
    //@ResponseBody
    public ResponseEntity<ServerInstance> start(@RequestParam String scenename, @RequestParam(required = false) Integer baseport,
                                                @RequestParam Map<String, String> allRequestParams) {
        log.info("Starting server for scene {} on port {} with args {}", scenename, baseport, allRequestParams);
        Map<String, String> argMap = allRequestParams.entrySet().stream().filter(x -> x.getKey().startsWith("arg.")).collect(Collectors.toMap(x -> x.getKey().substring(4), x -> x.getValue()));
        return new ResponseEntity<>(serverManagerService.startServer(scenename, baseport, argMap), HttpStatus.CREATED);
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