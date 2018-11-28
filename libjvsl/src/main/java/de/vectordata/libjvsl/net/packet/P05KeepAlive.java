package de.vectordata.libjvsl.net.packet;

import de.vectordata.libjvsl.net.PacketHandler;
import de.vectordata.libjvsl.net.packet.util.KeepAliveRole;
import de.vectordata.libjvsl.util.PacketBuffer;
import de.vectordata.libjvsl.util.cscompat.Nullable;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public class P05KeepAlive implements Packet {

    private KeepAliveRole role;

    public P05KeepAlive() {
    }

    public P05KeepAlive(KeepAliveRole role) {
        this.role = role;
    }

    public KeepAliveRole getRole() {
        return role;
    }

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
        return new P05KeepAlive(role);
    }

    @Override
    public void handlePacket(PacketHandler handler) {
        handler.handleP05KeepAlive(this);
    }

    @Override
    public void readPacket(PacketBuffer buffer) {
        role = KeepAliveRole.values()[buffer.readByte()];
    }

    @Override
    public void writePacket(PacketBuffer buffer) {
        buffer.writeByte((byte) role.ordinal());
    }

}
