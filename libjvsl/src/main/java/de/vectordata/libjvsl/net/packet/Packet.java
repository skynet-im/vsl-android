package de.vectordata.libjvsl.net.packet;

import java.io.IOException;

import de.vectordata.libjvsl.net.PacketHandler;
import de.vectordata.libjvsl.util.PacketBuffer;
import de.vectordata.libjvsl.util.cscompat.Nullable;

/**
 * Created by Daniel Lerch on 07.03.2018.
 * © 2018 Daniel Lerch
 */

public interface Packet {
    byte getPacketId();
    Nullable<Integer> getConstantLength();
    Packet createNew();
    void handlePacket(PacketHandler handler) throws IOException;
    void readPacket(PacketBuffer buf);
    void writePacket(PacketBuffer buf);
}
