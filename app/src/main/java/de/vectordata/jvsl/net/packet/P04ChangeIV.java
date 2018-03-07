package de.vectordata.jvsl.net.packet;


import de.vectordata.jvsl.net.PacketHandler;
import de.vectordata.jvsl.util.PacketBuffer;
import de.vectordata.jvsl.util.cscompat.Nullable;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public class P04ChangeIV implements Packet {

    private byte[] ClientIV;
    private byte[] ServerIV;

    public P04ChangeIV() {

    }

    public P04ChangeIV(byte[] clientIV, byte[] serverIV) {
        ClientIV = clientIV;
        ServerIV = serverIV;
    }

    @Override
    public byte getPacketId() {
        return 4;
    }

    @Override
    public Nullable<Integer> getConstantLength() {
        return new Nullable<>(32);
    }

    @Override
    public Packet createNew() {
        return null;
    }

    @Override
    public void handlePacket(PacketHandler handler) {
        handler.handleP04ChangeIV(this);
    }

    @Override
    public void readPacket(PacketBuffer buffer) {

    }

    @Override
    public void writePacket(PacketBuffer buffer) {

    }

}
