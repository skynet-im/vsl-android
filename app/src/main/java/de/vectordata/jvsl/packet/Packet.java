package de.vectordata.jvsl.packet;

import de.vectordata.jvsl.net.PacketHandler;
import de.vectordata.jvsl.util.PacketBuffer;
import de.vectordata.jvsl.util.cscompat.Nullable;

/**
 * Created by Daniel Lerch on 07.03.2018.
 * © 2018 Daniel Lerch
 */

public interface Packet {
    byte getPacketId();
    Nullable<Integer> getConstantLength();
    Packet createNew();
    void handlePacket(PacketHandler handler);
    void readPacket(PacketBuffer buf);
    void writePacket(PacketBuffer buf);
}
