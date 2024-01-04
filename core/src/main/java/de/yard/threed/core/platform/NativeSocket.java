package de.yard.threed.core.platform;


import de.yard.threed.core.Packet;
import de.yard.threed.core.WriteException;

/**
 * A (web)socket connection between tow peers.
 *
 * Not on byte level but UTF-8 text block level with empty line as separator (like mail)
 *
 */
public interface NativeSocket {
    void sendPacket(Packet packet) throws WriteException;

    /**
     * Returns next packet, null if no packet exists.
     * Should/Must not block.
     *
     * @return
     */
    Packet getPacket();

    void close();

    boolean isPending();
}
