package de.vectordata.jvsl.net.packet;


import de.vectordata.skynet.net.jvsl.packet.handler.PacketHandler;
import de.vectordata.skynet.net.jvsl.packet.length.PacketLength;
import de.vectordata.skynet.net.jvsl.util.PacketBuffer;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public interface Packet {
    byte getPacketId();

    PacketLength getLength();

    Packet createNew();

    void handlePacket(PacketHandler handler);

    void readPacket(PacketBuffer buffer);

    void writePacket(PacketBuffer buffer);

    boolean needsBigBuffer();
}
