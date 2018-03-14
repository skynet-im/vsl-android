package de.vectordata.jvsl.net.packet;


import java.io.IOException;

import de.vectordata.jvsl.net.PacketHandler;
import de.vectordata.jvsl.net.packet.util.ConnectionState;
import de.vectordata.jvsl.util.PacketBuffer;
import de.vectordata.jvsl.util.cscompat.Nullable;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public class P03FinishHandshake implements Packet {

    public ConnectionState connectionState;
    private String address;
    private int port;
    public int vslVersion;
    private int productVersion;

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
    public void handlePacket(PacketHandler handler) throws IOException {
        handler.handleP03FinishHandshake(this);
    }

    @Override
    public void readPacket(PacketBuffer buffer) {
        connectionState = ConnectionState.values()[buffer.readByte()];
        if (connectionState == ConnectionState.Redirect) {
            address = buffer.readString();
            port = buffer.readUInt16();
        }
        else if (connectionState == ConnectionState.Compatible){
            vslVersion = buffer.readUInt16();
            productVersion = buffer.readUInt16();
        }
    }

    @Override
    public void writePacket(PacketBuffer buffer) {
        buffer.writeByte((byte) connectionState.ordinal());
        if (connectionState == ConnectionState.Redirect) {
            buffer.writeString(address);
            buffer.writeUInt16(port);
        }
        else if (connectionState == ConnectionState.Compatible){
            buffer.writeUInt16(vslVersion);
            buffer.writeUInt16(productVersion);
        }
    }

}
