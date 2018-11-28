package de.vectordata.libjvsl.net.packet;

import de.vectordata.libjvsl.fileTransfer.Identifier;
import de.vectordata.libjvsl.fileTransfer.StreamMode;
import de.vectordata.libjvsl.net.PacketHandler;
import de.vectordata.libjvsl.util.PacketBuffer;
import de.vectordata.libjvsl.util.cscompat.Nullable;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public class P07OpenFileTransfer implements Packet {

    private Identifier identifier;
    private StreamMode streamMode;

    public P07OpenFileTransfer() {
    }

    public P07OpenFileTransfer(Identifier identifier, StreamMode streamMode) {
        this.identifier = identifier;
        this.streamMode = streamMode;
    }

    @Override
    public byte getPacketId() {
        return 7;
    }

    @Override
    public Nullable<Integer> getConstantLength() {
        return new Nullable<>(null);
    }

    @Override
    public Packet createNew() {
        return new P07OpenFileTransfer();
    }

    @Override
    public void handlePacket(PacketHandler handler) {
        handler.handleP07OpenFileTransfer(this);
    }

    @Override
    public void readPacket(PacketBuffer buffer) {
        identifier = Identifier.fromBinary(buffer);
        streamMode = StreamMode.values()[buffer.readByte()];
    }

    @Override
    public void writePacket(PacketBuffer buffer) {
        identifier.toBinary(buffer);
        buffer.writeByte((byte) streamMode.ordinal());
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public StreamMode getStreamMode() {
        return streamMode;
    }
}
