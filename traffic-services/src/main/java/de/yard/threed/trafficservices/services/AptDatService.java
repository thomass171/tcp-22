package de.yard.threed.trafficservices.services;

import de.yard.threed.core.Util;
import de.yard.threed.trafficservices.util.AirportFilter;
import de.yard.threed.trafficservices.util.TrafficServicesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AptDatService {

    @Value("${trafficservices.aptdat:}")
    private String aptdat;

    @Value("${trafficservices.basedir:}")
    private String basedir;

    public List<AirportLine> getAirports(AirportFilter filter) {
        List<AirportLine> airports = null;
        try {
            airports = findAirports(filter);
        } catch (Exception e) {
            log.error("failed", e);
            return null;
        }
        return airports;
    }

    public List<AptLine> getAirport(String icao) {
        List<AptLine> airportLines = null;
        try {
            airportLines = extractAirportFromApt(icao);
        } catch (Exception e) {
            log.error("failed", e);
            return null;
        }

        //airport.setGroundNetXml(FileReader.readAsString(new File(DIR+"/"+icao.toUpperCase()+".groundnet.xml")));
        return airportLines;
    }

    /**
     * exact match search
     */
    public List<AptLine> extractAirportFromApt(String icao) throws IOException {
        BufferedReader reader = TrafficServicesUtil.getReaderFromPath(getAptDatPath(), StandardCharsets.US_ASCII);
        String line = null;

        boolean found = false;
        List<AptLine> airportlines = null;
        while ((line = reader.readLine()) != null) {
            if (isAirportLine(line)) {
                if (found) {
                    // next airport reached
                    return airportlines;
                }
                if (line.contains(icao)) {
                    found = true;
                    airportlines = new ArrayList<>();
                }
            }
            if (airportlines != null) {
                AptLine aptLine = buildAptLine(line);
                if (aptLine != null) {
                    airportlines.add(aptLine);
                }
            }
        }
        return new ArrayList<>();

    }

    public List<AirportLine> findAirports(AirportFilter filter) throws IOException {
        BufferedReader reader = TrafficServicesUtil.getReaderFromPath(getAptDatPath(), StandardCharsets.US_ASCII);
        String line;

        List<AirportLine> airports = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            if (isAirportLine(line)) {
                AirportLine airportLine = new AirportLine(line);
                if (airportLine.matchesIcao(filter.getIcao())) {
                    airports.add(airportLine);
                }
            }
        }
        return airports;
    }

    boolean isAirportLine(String line) {
        // "1  " is airport marker
        return line.startsWith("1  ");
    }

    boolean isRunwayLine(String line) {
        // "100 " is land runway
        return line.startsWith("100 ");
    }

    public AptLine buildAptLine(String line) {
        if (isAirportLine(line)) {
            return new AirportLine(line);
        }
        if (isRunwayLine(line)) {
            return new RunwayLine(line);
        }
        return null;
    }

    private Path getAptDatPath() {

        Path workingDir;

        if (basedir.startsWith("/")) {
            workingDir = Paths.get(basedir);
        } else {
            Path userDir = Paths.get(System.getProperty("user.dir"));
            workingDir = userDir.resolve(basedir);
        }
        Path path = workingDir.resolve(aptdat);
        log.debug("Using apt.dat from '{}'", path);
        if (!Files.exists(path)){
            log.error("apt.dat does not exist: '{}'", path);
        }
        return path;
    }
}

abstract class AptLine {
    final String line;
    final String[] parts;

    AptLine(String line) {
        this.line = line;
        parts = line.split("\\s+");
    }
}

class AirportLine extends AptLine {

    AirportLine(String line) {
        super(line);
    }

    public String getIcao() {
        return parts[4];
    }

    public boolean matchesIcao(String icao) {
        return getIcao().startsWith(icao.toUpperCase());
    }

    public String getName() {
        String s = StringUtils.substringAfter(line, getIcao());
        return s.trim();
    }
}

/**
 * example: 100   29.87   1   0 0.25 0 0 0 06   50.40338900  006.52166700  269.75    0.00 2  0 0 1 24   50.40838900  006.53450000   39.93    0.00 2  0 0 1 2
 *
 */
class RunwayLine extends AptLine {

    RunwayLine(String line) {
        super(line);
    }

    public String getIcao() {
        return parts[4];
    }

    public double getFromLat(){
        return Util.parseDouble(parts[9]);
    }

    public double getFromLon(){
        return Util.parseDouble(parts[10]);
    }

    public String getFromNumber(){
        return parts[8];
    }

    public double getToLat(){
        return Util.parseDouble(parts[18]);
    }

    public  double getToLon(){
        return Util.parseDouble(parts[19]);
    }

    public String getToNumber(){
        return parts[17];
    }

    public double getWidth(){
        return Util.parseDouble(parts[1]);
    }
}
