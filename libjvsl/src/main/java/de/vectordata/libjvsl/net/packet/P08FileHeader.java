package de.vectordata.libjvsl.net.packet;

import de.vectordata.libjvsl.net.PacketHandler;
import de.vectordata.libjvsl.util.PacketBuffer;
import de.vectordata.libjvsl.util.cscompat.Nullable;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public class P08FileHeader implements Packet {

    public byte[] binaryData;

    public P08FileHeader(byte[] binaryData) {
        this.binaryData = binaryData;
    }

    public P08FileHeader() {
    }

    @Override
    public byte getPacketId() {
        return 8;
    }

    @Override
    public Nullable<Integer> getConstantLength() {
        return new Nullable<>(null);
    }

    @Override
    public Packet createNew() {
        return new P08FileHeader();
    }

    @Override
    public void handlePacket(PacketHandler handler) {
        handler.handleP08FileHeader(this);
    }

    @Override
    public void readPacket(PacketBuffer buffer) {
        binaryData = buffer.readToEnd();
    }

    @Override
    public void writePacket(PacketBuffer buffer) {
        buffer.writeByteArray(binaryData, false);
    }

}
