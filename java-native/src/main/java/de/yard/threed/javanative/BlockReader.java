package de.yard.threed.javanative;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Accept text lines line by line and build a block after empty lines.
 */
public class BlockReader {
    static Logger logger = LoggerFactory.getLogger(BlockReader.class.getName());

    List<List<String>> blocks = new Vector<>();
    List<String> lines = new Vector<>();
    private boolean debuglog = false;

    public BlockReader() {
    }

    public void add(String inputLine) {
        if (inputLine.length() == 0) {
            if (debuglog) {
                logger.debug("found block end");
            }
            blocks.add(lines);
            lines = new ArrayList<>();
        } else {
            lines.add(inputLine);
        }
    }

    public boolean hasBlock() {
        return blocks.size() > 0;
    }

    public List<String> pull() {
        if (blocks.size() == 0) {
            return null;
        }
        List<String> block = new ArrayList<>();
        //TODO more than Vector needed to make MT safe?
        block = blocks.remove(0);
        return block;
    }

    public static void writePacket(List<String> packet, LinePrinter linePrinter) {
        for (String s : packet) {
            linePrinter.println(s);
        }
        // empty line as delimiter
        linePrinter.println("");
    }
}
