package de.vectordata.libjvsl.net.packet;

import de.vectordata.libjvsl.net.PacketHandler;
import de.vectordata.libjvsl.util.PacketBuffer;
import de.vectordata.libjvsl.util.cscompat.Nullable;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public class P01KeyExchange implements Packet {

    private byte[] aesKey;
    private byte[] hmacKey;
    private int latestVSL;
    private int oldestVSL;
    private int latestProduct;
    private int oldestProduct;

    private P01KeyExchange() {

    }

    public P01KeyExchange(byte[] aesKey, byte[] hmacKey, int latestVSL, int oldestVSL, int latestProduct, int oldestProduct) {
        this.aesKey = aesKey;
        this.hmacKey = hmacKey;
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
        hmacKey = buffer.readByteArray(32);
        latestVSL = buffer.readUInt16();
        oldestVSL = buffer.readUInt16();
        latestProduct = buffer.readUInt16();
        oldestProduct = buffer.readUInt16();
    }

    @Override
    public void writePacket(PacketBuffer buffer) {
        buffer.writeByteArray(aesKey, false);
        buffer.writeByteArray(hmacKey, false);
        buffer.writeUInt16(latestVSL);
        buffer.writeUInt16(oldestVSL);
        buffer.writeUInt16(latestProduct);
        buffer.writeUInt16(oldestProduct);
    }

}
