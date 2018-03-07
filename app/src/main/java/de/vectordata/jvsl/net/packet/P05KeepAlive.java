package de.vectordata.jvsl.net.packet;

import de.vectordata.jvsl.net.PacketHandler;
import de.vectordata.jvsl.util.PacketBuffer;
import de.vectordata.jvsl.util.cscompat.Nullable;

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
    public Nullable<Integer> getConstantLength() {
        return new Nullable<>(0);
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

}
