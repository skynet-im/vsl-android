package de.vectordata.jvsl.net.packet;

import de.vectordata.jvsl.net.PacketHandler;
import de.vectordata.jvsl.util.PacketBuffer;
import de.vectordata.jvsl.util.cscompat.Nullable;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public class P01KeyExchange implements Packet {

    private byte[] aesKey;
    private byte[] clientIV;
    private byte[] serverIV;
    private int latestVSL;
    private int oldestVSL;
    private int latestProduct;
    private int oldestProduct;

    private P01KeyExchange() {

    }

    public P01KeyExchange(byte[] aesKey, byte[] clientIV, byte[] serverIV, int latestVSL, int oldestVSL, int latestProduct, int oldestProduct) {
        this.aesKey = aesKey;
        this.clientIV = clientIV;
        this.serverIV = serverIV;
        this.latestVSL = latestVSL;
        this.oldestVSL = oldestVSL;
        this.latestProduct = latestProduct;
        this.oldestProduct = oldestProduct;
    }

    @Override
    public byte getPacketId() {
        return 1;
    }

    @Override
    public Nullable<Integer> getConstantLength() {
        return new Nullable<>(72);
    }

    @Override
    public Packet createNew() {
        return new P01KeyExchange();
    }

    @Override
    public void handlePacket(PacketHandler handler) {
        handler.handleP01KeyExchange(this);
    }

    @Override
    public void readPacket(PacketBuffer buffer) {
        aesKey = buffer.readByteArray(32);
        clientIV = buffer.readByteArray(16);
        serverIV = buffer.readByteArray(16);
        latestVSL = buffer.readUInt16();
        oldestVSL = buffer.readUInt16();
        latestProduct = buffer.readUInt16();
        oldestProduct = buffer.readUInt16();
    }

    @Override
    public void writePacket(PacketBuffer buffer) {
        buffer.writeByteArray(aesKey, false);
        buffer.writeByteArray(clientIV, false);
        buffer.writeByteArray(serverIV, false);
        buffer.writeUInt16(latestVSL);
        buffer.writeUInt16(oldestVSL);
        buffer.writeUInt16(latestProduct);
        buffer.writeUInt16(oldestProduct);
    }

}
