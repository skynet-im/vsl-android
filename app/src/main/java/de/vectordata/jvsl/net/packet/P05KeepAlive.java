package de.vectordata.jvsl.net.packet;


import de.vectordata.skynet.net.jvsl.packet.handler.PacketHandler;
import de.vectordata.skynet.net.jvsl.packet.length.ConstantLength;
import de.vectordata.skynet.net.jvsl.packet.length.PacketLength;
import de.vectordata.skynet.net.jvsl.util.PacketBuffer;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public class P05KeepAlive implements Packet {
    @Override
    public byte getPacketId() {
        return 5;
    }

    @Override
    public PacketLength getLength() {
        return new ConstantLength(0);
    }

    @Override
    public Packet createNew() {
        return new P05KeepAlive();
    }

    @Override
    public void handlePacket(PacketHandler handler) {
        handler.handleP05KeepAlive(this);
    }

    @Override
    public void readPacket(PacketBuffer buffer) {

    }

    @Override
    public void writePacket(PacketBuffer buffer) {

    }

    @Override
    public boolean needsBigBuffer() {
        return false;
    }
}
