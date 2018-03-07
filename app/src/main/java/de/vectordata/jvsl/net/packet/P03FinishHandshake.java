package de.vectordata.jvsl.net.packet;


import de.vectordata.jvsl.net.PacketHandler;
import de.vectordata.jvsl.net.packet.util.ConnectionType;
import de.vectordata.jvsl.util.PacketBuffer;
import de.vectordata.jvsl.util.cscompat.Nullable;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public class P03FinishHandshake implements Packet {

    public ConnectionType connectionType;
    private String address;
    private int port;

    @Override
    public byte getPacketId() {
        return 3;
    }

    @Override
    public Nullable<Integer> getConstantLength() {
        return new Nullable<>(null);
    }

    @Override
    public Packet createNew() {
        return new P03FinishHandshake();
    }

    @Override
    public void handlePacket(PacketHandler handler) {
        handler.handleP03FinishHandshake(this);
    }

    @Override
    public void readPacket(PacketBuffer buffer) {
        connectionType = ConnectionType.values()[buffer.readByte()];
        if (connectionType == ConnectionType.Redirect) {
            address = buffer.readString();
            port = buffer.readUInt16();
        }
    }

    @Override
    public void writePacket(PacketBuffer buffer) {
        buffer.writeByte((byte) connectionType.ordinal());
        if (connectionType == ConnectionType.Redirect) {
            buffer.writeString(address);
            buffer.writeUInt16(port);
        }
    }

}
