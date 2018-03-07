package de.vectordata.jvsl.net.packet;


import de.vectordata.skynet.net.jvsl.packet.handler.PacketHandler;
import de.vectordata.skynet.net.jvsl.packet.length.ConstantLength;
import de.vectordata.skynet.net.jvsl.packet.length.PacketLength;
import de.vectordata.skynet.net.jvsl.util.PacketBuffer;

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
    public PacketLength getLength() {
        return new ConstantLength(32);
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

    @Override
    public boolean needsBigBuffer() {
        return false;
    }
}
