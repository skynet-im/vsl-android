package de.vectordata.jvsl.net;

import de.vectordata.jvsl.VSLClient;
import de.vectordata.jvsl.crypt.AesStatic;
import de.vectordata.jvsl.crypt.HmacStatic;
import de.vectordata.jvsl.crypt.RsaStatic;
import de.vectordata.jvsl.net.packet.Packet;
import de.vectordata.jvsl.util.PacketBuffer;
import de.vectordata.jvsl.util.Util;
import de.vectordata.jvsl.util.cscompat.Ref;

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
        int blocks = 0; // TODO Implement reading from UInt24
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
