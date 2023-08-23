package de.yard.threed.core;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Accept text lines line by line and build a block after empty lines.
 */
public class BlockReader {
    static Log logger = Platform.getInstance().getLog(BlockReader.class);

    // Vector is synced
    List<List<String>> blocks = new Vector<List<String>>();
    List<String> lines = new Vector<String>();
    private boolean debuglog = false;

    public BlockReader() {
    }

    public void add(String inputLine) {
        if (StringUtils.length(inputLine) == 0) {
            if (debuglog) {
                logger.debug("found block end");
            }
            blocks.add(lines);
            lines = new ArrayList<String>();
        } else {
            lines.add(inputLine);
        }
    }

    public boolean hasBlock() {
        return blocks.size() > 0;
    }

    /**
     * Returns null if no block is available.
     */
    public List<String> pull() {
        if (blocks.size() == 0) {
            return null;
        }
        List<String> block = new ArrayList<String>();
        //TODO more than Vector needed to make MT safe?
        block = blocks.remove(0);
        return block;
    }

    /**
     * TODO move to Packet?
     * @param packet
     * @param linePrinter
     */
    public static void writePacket(List<String> packet, LinePrinter linePrinter) throws WriteException {
        for (String s : packet) {
            linePrinter.println(s);
        }
        // empty line as delimiter
        linePrinter.println("");
    }
}
