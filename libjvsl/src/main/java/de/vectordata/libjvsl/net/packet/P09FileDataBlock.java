package de.vectordata.libjvsl.net.packet;

import java.io.IOException;

import de.vectordata.libjvsl.net.PacketHandler;
import de.vectordata.libjvsl.util.PacketBuffer;
import de.vectordata.libjvsl.util.cscompat.Nullable;

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
    public Nullable<Integer> getConstantLength() {
        return new Nullable<>(null);
    }

    @Override
    public Packet createNew() {
        return new P09FileDataBlock();
    }

    @Override
    public void handlePacket(PacketHandler handler) throws IOException {
        handler.handleP09FileDataBlock(this);
    }

    @Override
    public void readPacket(PacketBuffer buffer) {
        startPosition = buffer.readInt64();
        dataBlock = buffer.readToEnd();
    }

    @Override
    public void writePacket(PacketBuffer buffer) {
        buffer.writeInt64(startPosition);
        buffer.writeByteArray(dataBlock, false);
    }
}
