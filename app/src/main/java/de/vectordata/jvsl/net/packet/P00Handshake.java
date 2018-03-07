package de.vectordata.jvsl.net.packet;

import de.vectordata.jvsl.net.PacketHandler;
import de.vectordata.jvsl.net.packet.util.RequestType;
import de.vectordata.jvsl.util.PacketBuffer;
import de.vectordata.jvsl.util.cscompat.Nullable;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public class P00Handshake implements Packet {

    private RequestType requestType;

    private P00Handshake() {
    }

    public P00Handshake(RequestType requestType) {
        this.requestType = requestType;
    }

    @Override
    public byte getPacketId() {
        return 0;
    }

    @Override
    public Nullable<Integer> getConstantLength() {
        return new Nullable<>(1);
    }

    @Override
    public Packet createNew() {
        return new P00Handshake();
    }

    @Override
    public void handlePacket(PacketHandler handler) {
        handler.handleP00Handshake(this);
    }

    @Override
    public void readPacket(PacketBuffer buffer) {
        requestType = RequestType.values()[buffer.readByte()];
    }

    @Override
    public void writePacket(PacketBuffer buffer) {
        buffer.writeByte((byte) requestType.ordinal());
    }
}
