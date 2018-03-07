package de.vectordata.jvsl.net.packet;

import de.vectordata.jvsl.net.PacketHandler;
import de.vectordata.jvsl.net.packet.length.PacketLength;
import de.vectordata.jvsl.net.packet.length.VariableLength;
import de.vectordata.jvsl.util.PacketBuffer;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public class P09FileDataBlock implements Packet {

    private long startPosition;
    public byte[] dataBlock;

    public P09FileDataBlock(long startPosition, byte[] dataBlock) {
        this.startPosition = startPosition;
        this.dataBlock = dataBlock;
    }

    public P09FileDataBlock() {
    }

    @Override
    public byte getPacketId() {
        return 9;
    }

    @Override
    public PacketLength getLength() {
        return new VariableLength();
    }

    @Override
    public Packet createNew() {
        return new P09FileDataBlock();
    }

    @Override
    public void handlePacket(PacketHandler handler) {
        handler.handleP09FileDataBlock(this);
    }

    @Override
    public void readPacket(PacketBuffer buffer) {
        startPosition = buffer.readLong();
        dataBlock = buffer.readToEnd();
    }

    @Override
    public void writePacket(PacketBuffer buffer) {
        buffer.writeLong(startPosition);
        buffer.writeByteArray(dataBlock, false);
    }

    @Override
    public boolean needsBigBuffer() {
        return true;
    }
}
