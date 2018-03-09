package de.vectordata.jvsl.net;

import de.vectordata.jvsl.VSLClient;
import de.vectordata.jvsl.crypt.AesStatic;
import de.vectordata.jvsl.crypt.HmacStatic;
import de.vectordata.jvsl.crypt.RsaStatic;
import de.vectordata.jvsl.net.packet.Packet;
import de.vectordata.jvsl.util.PacketBuffer;
import de.vectordata.jvsl.util.Util;
import de.vectordata.jvsl.util.cscompat.Ref;
import de.vectordata.jvsl.util.cscompat.UInt24;

/**
 * Created by Daniel Lerch on 07.03.2018.
 * © 2018 Daniel Lerch
 */

public class NetworkManager {

    private VSLClient parent;
    private String rsaKey;
    private byte[] aesKey;
    private byte[] hmacKey;

    public NetworkManager(VSLClient parent, String rsaKey) {
        this.parent = parent;
        this.rsaKey = rsaKey;
    }

    public void receiveData() {
        CryptoAlgorithm algorithm = CryptoAlgorithm.values()[parent.getChannel().receiveByte()];
        switch (algorithm) {
            case NONE:
                receivePacket_Plaintext();
                break;
            case RSA_2048_OAEP:
                break;
            //case AES_256_CBC_SP:
            case AES_256_CBC_HMAC_SHA256_MP3:
                break;
            default:
                throw new EnumConstantNotPresentException(CryptoAlgorithm.class, algorithm.toString());
        }
    }

    public void sendPacket(byte id, byte[] content) {
        sendPacket(CryptoAlgorithm.AES_256_CBC_HMAC_SHA256_MP3, id, content);
    }

    public void sendPacket(CryptoAlgorithm alg, byte id, byte[] content) {
        sendPacket(alg, id, true, content);
    }

    public void sendPacket(Packet packet) {
        sendPacket(CryptoAlgorithm.AES_256_CBC_HMAC_SHA256_MP3, packet);
    }

    public void sendPacket(CryptoAlgorithm alg, Packet packet) {
        byte[] content;
        PacketBuffer buf = new PacketBuffer();
        packet.writePacket(buf);
        content = buf.toArray();
        sendPacket(alg, packet.getPacketId(), !packet.getConstantLength().hasValue(), content);
    }

    public void sendPacket(CryptoAlgorithm alg, byte realId, boolean size, byte[] content) {
        switch (alg) {
            case NONE:
                sendPacket_Plaintext(realId, size, content);
            case RSA_2048_OAEP:
                sendPacket_RSA_2048_OAEP(realId, size, content);
            case AES_256_CBC_HMAC_SHA256_MP3:
                sendPacket_AES_256_CBC_HMAC_SHA256_MP3(realId, size, content);
            default:
                throw new IllegalArgumentException();
        }
    }

    private void receivePacket_Plaintext() {
        byte id = parent.getChannel().receiveByte();
        Ref<Packet> packetRef = new Ref<>();
        if (!parent.getHandler().tryGetPacket(id, packetRef))
            throw new IllegalArgumentException();
        Packet packet = packetRef.get();
        int length;
        if (packet.getConstantLength().hasValue())
            length = packet.getConstantLength().getValue();
        else
            length = (int) new PacketBuffer(parent.getChannel().receive(4)).readUInt32();
        parent.getHandler().handleInternalPacket(id, parent.getChannel().receive(length), CryptoAlgorithm.NONE);
    }

    private void receivePacket_RSA_2048_OAEP() {
        byte[] ciphertext = parent.getChannel().receive(256);
        PacketBuffer plaintext = new PacketBuffer(RsaStatic.decryptBlock(ciphertext, rsaKey));
        byte id = plaintext.readByte();
        Ref<Packet> packetRef = new Ref<>();
        if (!parent.getHandler().tryGetPacket(id, packetRef))
            throw new IllegalArgumentException();
        Packet packet = packetRef.get();
        int length;
        if (packet.getConstantLength().hasValue())
            length = packet.getConstantLength().getValue();
        else
            length = (int) plaintext.readUInt32();
        if (length > 214)
            throw new IllegalArgumentException("Too big packet!");
        parent.getHandler().handleInternalPacket(id, plaintext.readByteArray(length), CryptoAlgorithm.RSA_2048_OAEP);
    }

    private void receivePacket_AES_256_CBC_HMAC_SHA_256_MP3() {
        int blocks = UInt24.fromByteArray(parent.getChannel().receive(3), UInt24.Endianness.LittleEndian).getValue();
        byte[] hmac = parent.getChannel().receive(32);
        byte[] cipherblock = parent.getChannel().receive((blocks + 2) * 16);
        if (!Util.sequenceEqual(hmac, HmacStatic.computeHmacSHA256(cipherblock, hmacKey)))
            throw new SecurityException("MessageCorrupted");
        byte[] iv = Util.takeBytes(cipherblock, 16, 0);
        byte[] ciphertext = Util.skipBytes(cipherblock, 16);
        PacketBuffer plaintext = new PacketBuffer(AesStatic.decrypt(ciphertext, aesKey, iv));
        while (plaintext.getPosition() < plaintext.getLength() - 1) {
            byte id = plaintext.readByte();
            Ref<Packet> packetRef = new Ref<>();
            boolean success = parent.getHandler().tryGetPacket(id, packetRef);
            int length;
            if (success && packetRef.get().getConstantLength().hasValue())
                length = packetRef.get().getConstantLength().getValue();
            else
                length = (int) plaintext.readUInt32();
            if (length > plaintext.getPending())
                throw new IllegalArgumentException("Too big packet!");
            byte[] content = plaintext.readByteArray(length);
            if (success)
                parent.getHandler().handleInternalPacket(id, content, CryptoAlgorithm.AES_256_CBC_HMAC_SHA256_MP3);
            else
                ; // TODO Implement VSLClient.OnPacketReceived()
        }
    }

    private void sendPacket_Plaintext(byte realId, boolean size, byte[] content) {
        PacketBuffer pbuf = new PacketBuffer();
        pbuf.writeByte((byte) CryptoAlgorithm.NONE.ordinal());
        pbuf.writeByte(realId);
        if (size) pbuf.writeUInt32(content.length);
        pbuf.writeByteArray(content, false);
        parent.getChannel().sendAsync(pbuf.toArray());
    }

    private void sendPacket_RSA_2048_OAEP(byte realId, boolean size, byte[] content) {
        PacketBuffer pbuf = new PacketBuffer();
        pbuf.writeByte(realId);
        if (size) pbuf.writeUInt32(content.length);
        pbuf.writeByteArray(content, false);
        byte[] ciphertext = RsaStatic.encryptBlock(pbuf.toArray(), rsaKey);
        byte[] buf = new byte[1 + ciphertext.length];
        buf[0] = (byte) CryptoAlgorithm.RSA_2048_OAEP.ordinal();
        System.arraycopy(ciphertext, 0, buf, 1, ciphertext.length);
        parent.getChannel().sendAsync(buf);
    }

    private void sendPacket_AES_256_CBC_HMAC_SHA256_MP3(byte realId, boolean size, byte[] content) {
        PacketBuffer pbuf = new PacketBuffer();
        pbuf.writeByte(realId);
        if (size) pbuf.writeUInt32(content.length);
        pbuf.writeByteArray(content, false);
        byte[] iv = AesStatic.generateIV();
        byte[] ciphertext = AesStatic.encrypt(pbuf.toArray(), aesKey, iv);
        byte[] blocks = new UInt24(ciphertext.length / 16 - 1).toByteArray(UInt24.Endianness.LittleEndian);
        byte[] cipherblock = Util.concatBytes(iv, ciphertext);
        byte[] hmac = HmacStatic.computeHmacSHA256(cipherblock, hmacKey);
        PacketBuffer sbuf = new PacketBuffer();
        sbuf.writeByte((byte) CryptoAlgorithm.AES_256_CBC_HMAC_SHA256_MP3.ordinal());
        sbuf.writeByteArray(blocks, false);
        sbuf.writeByteArray(hmac, false);
        sbuf.writeByteArray(cipherblock, false);
        parent.getChannel().sendAsync(sbuf.toArray());
    }

    public byte[] getAesKey() {
        return aesKey;
    }

    public void setAesKey(byte[] value) {
        aesKey = value;
    }

    public byte[] getHmacKey() {
        return hmacKey;
    }

    public void setHmacKey(byte[] value) {
        hmacKey = value;
    }
}
