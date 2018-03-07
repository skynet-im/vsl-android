package de.vectordata.jvsl.net.packet;

import de.vectordata.jvsl.net.PacketHandler;
import de.vectordata.jvsl.net.packet.length.PacketLength;
import de.vectordata.jvsl.net.packet.length.VariableLength;
import de.vectordata.jvsl.util.PacketBuffer;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public class P07OpenFileTransfer implements Packet {

    private Identifier identifier;
    private StreamMode streamMode;

    private P07OpenFileTransfer() {
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
    public PacketLength getLength() {
        return new VariableLength();
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

    @Override
    public boolean needsBigBuffer() {
        return false;
    }
}