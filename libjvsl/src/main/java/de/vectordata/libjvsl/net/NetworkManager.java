package de.vectordata.libjvsl.net;

import java.io.IOException;
import java.lang.ref.WeakReference;

import de.vectordata.libjvsl.VSLClient;
import de.vectordata.libjvsl.crypt.AesStatic;
import de.vectordata.libjvsl.crypt.HmacStatic;
import de.vectordata.libjvsl.crypt.RsaStatic;
import de.vectordata.libjvsl.net.packet.Packet;
import de.vectordata.libjvsl.util.PacketBuffer;
import de.vectordata.libjvsl.util.Util;
import de.vectordata.libjvsl.util.cscompat.Ref;
import de.vectordata.libjvsl.util.UInt24;

/**
 * Created by Daniel Lerch on 07.03.2018.
 * © 2018 Daniel Lerch
 */

public class NetworkManager {

    private VSLClient parent;
    private String rsaKey;
    private byte[] aesKey;
    private byte[] hmacKey;
    private byte[] receiveIv;
    private byte[] sendIv;

    public NetworkManager(VSLClient parent, String rsaKey) {
        this.parent = parent;
        this.rsaKey = rsaKey;
    }

    void receiveData() throws IOException {
        byte algo = parent.getChannel().receiveByte();
        if (algo == -1)
            throw new IOException("Reached end of stream while trying to receive");
        CryptoAlgorithm algorithm = CryptoAlgorithm.values()[algo];
        switch (algorithm) {
            case NONE:
                receivePacket_Plaintext();
                break;
            case RSA_2048_OAEP:
                receivePacket_RSA_2048_OAEP();
                break;
            //case AES_256_CBC_SP:
            case AES_256_CBC_HMAC_SHA256_MP3:
                receivePacket_AES_256_CBC_HMAC_SHA_256_MP3();
                break;
            case AES_256_CBC_HMAC_SHA256_CTR:
                receivePacket_AES_256_CBC_HMAC_SHA_256_CTR();
                break;
            default:
                throw new EnumConstantNotPresentException(CryptoAlgorithm.class, algorithm.toString());
        }
    }

    public SendItem sendPacket(byte id, byte[] content, Priority priority) {
        return sendPacket(CryptoAlgorithm.AES_256_CBC_HMAC_SHA256_CTR, id, content, priority);
    }

    private SendItem sendPacket(CryptoAlgorithm alg, byte id, byte[] content, Priority priority) {
        return sendPacket(alg, id, true, content, priority);
    }

    public SendItem sendPacket(Packet packet, Priority priority) {
        return sendPacket(CryptoAlgorithm.AES_256_CBC_HMAC_SHA256_CTR, packet, priority);
    }

    public SendItem sendPacket(CryptoAlgorithm alg, Packet packet, Priority priority) {
        byte[] content;
        PacketBuffer buf = new PacketBuffer();
        packet.writePacket(buf);
        content = buf.toArray();
        return sendPacket(alg, packet.getPacketId(), !packet.getConstantLength().hasValue(), content, priority);
    }

    private SendItem sendPacket(CryptoAlgorithm alg, byte realId, boolean size, byte[] content, Priority priority) {
        switch (alg) {
            case NONE:
                return sendPacket_Plaintext(realId, size, content);
            case RSA_2048_OAEP:
                return sendPacket_RSA_2048_OAEP(realId, size, content);
            case AES_256_CBC_HMAC_SHA256_MP3:
                return sendPacket_AES_256_CBC_HMAC_SHA256_MP3(realId, size, content);
            case AES_256_CBC_HMAC_SHA256_CTR:
                return sendPacket_AES_256_CBC_HMAC_SHA256_CTR(realId, size, content, priority);
            default:
                throw new IllegalArgumentException();
        }
    }

    private void receivePacket_Plaintext() throws IOException {
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

    private void receivePacket_RSA_2048_OAEP() throws IOException {
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

    private void receivePacket_AES_256_CBC_HMAC_SHA_256_MP3() throws IOException {
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
                throw new IllegalArgumentException("Packet too big!");
            byte[] content = plaintext.readByteArray(length);
            if (success)
                parent.getHandler().handleInternalPacket(id, content, CryptoAlgorithm.AES_256_CBC_HMAC_SHA256_MP3);
            else
                parent.onPacketReceived(id, content);
        }
    }

    private void receivePacket_AES_256_CBC_HMAC_SHA_256_CTR() throws IOException {
        CryptoAlgorithm alg = CryptoAlgorithm.AES_256_CBC_HMAC_SHA256_CTR;
        byte[] buffer = parent.getChannel().receive(35);
        int blocks = UInt24.fromByteArray(Util.takeBytes(buffer, 3, 0), UInt24.Endianness.LittleEndian).getValue();
        byte[] hmac = Util.skipBytes(buffer, 3);
        int pendingLength = (blocks + 1) * 16;
        byte[] cipherblock = parent.getChannel().receive(pendingLength);
        if (!Util.sequenceEqual(hmac, HmacStatic.computeHmacSHA256(cipherblock, hmacKey)))
            throw new SecurityException("Message corrupted");
        byte[] plainbuffer = AesStatic.decrypt(cipherblock, aesKey, receiveIv);
        receiveIv = AesStatic.incrementIv(receiveIv);
        PacketBuffer plaintext = new PacketBuffer(plainbuffer);
        byte id = plaintext.readByte();
        boolean isInternal = parent.getHandler().isInternalPacket(id);
        if (isInternal && parent.getHandler().findRule(id, alg) == null)
            return;
        byte[] content = plaintext.readToEnd();
        if (isInternal)
            parent.getHandler().handleInternalPacket(id, content, alg);
        else
            parent.onPacketReceived(id, content);
    }

    private SendItem sendPacket_Plaintext(byte realId, boolean size, byte[] content) {
        PacketBuffer pbuf = new PacketBuffer();
        pbuf.writeByte((byte) CryptoAlgorithm.NONE.ordinal());
        pbuf.writeByte(realId);
        if (size) pbuf.writeUInt32(content.length);
        pbuf.writeByteArray(content, false);
        return parent.getChannel().sendAsync(pbuf.toArray());
    }

    private SendItem sendPacket_RSA_2048_OAEP(byte realId, boolean size, byte[] content) {
        PacketBuffer packetBuf = new PacketBuffer();
        packetBuf.writeByte(realId);
        if (size) packetBuf.writeUInt32(content.length);
        packetBuf.writeByteArray(content, false);
        byte[] ciphertext = RsaStatic.encryptBlock(packetBuf.toArray(), rsaKey);
        byte[] buf = new byte[1 + ciphertext.length];
        buf[0] = (byte) CryptoAlgorithm.RSA_2048_OAEP.ordinal();
        System.arraycopy(ciphertext, 0, buf, 1, ciphertext.length);
        return parent.getChannel().sendAsync(buf);
    }

    private SendItem sendPacket_AES_256_CBC_HMAC_SHA256_MP3(byte realId, boolean size, byte[] content) {
        PacketBuffer packetBuf = new PacketBuffer();
        packetBuf.writeByte(realId);
        if (size) packetBuf.writeUInt32(content.length);
        packetBuf.writeByteArray(content, false);
        byte[] iv = AesStatic.generateIV();
        byte[] ciphertext = AesStatic.encrypt(packetBuf.toArray(), aesKey, iv);
        byte[] blocks = new UInt24(ciphertext.length / 16 - 1).toByteArray(UInt24.Endianness.LittleEndian);
        byte[] cipherblock = Util.concatBytes(iv, ciphertext);
        byte[] hmac = HmacStatic.computeHmacSHA256(cipherblock, hmacKey);
        PacketBuffer sbuf = new PacketBuffer();
        sbuf.writeByte((byte) CryptoAlgorithm.AES_256_CBC_HMAC_SHA256_MP3.ordinal());
        sbuf.writeByteArray(blocks, false);
        sbuf.writeByteArray(hmac, false);
        sbuf.writeByteArray(cipherblock, false);
        return parent.getChannel().sendAsync(sbuf.toArray());
    }

    private SendItem sendPacket_AES_256_CBC_HMAC_SHA256_CTR(byte realId, boolean size, byte[] content, Priority priority) {
        byte[] plaintext;
        {
            PacketBuffer pbuf = new PacketBuffer(content.length + 1);
            pbuf.writeByte(realId);
            pbuf.writeByteArray(content, false);
            plaintext = pbuf.toArray();
        }
        byte[] ciphertext = AesStatic.encrypt(plaintext, aesKey, sendIv);
        sendIv = AesStatic.incrementIv(sendIv);
        byte[] blocks = new UInt24(ciphertext.length / 16 - 1).toByteArray(UInt24.Endianness.LittleEndian);
        byte[] hmac = HmacStatic.computeHmacSHA256(ciphertext, hmacKey);
        byte[] buffer;
        {
            PacketBuffer pbuf = new PacketBuffer(1 + 3 + hmac.length + ciphertext.length);
            pbuf.writeByte((byte) CryptoAlgorithm.AES_256_CBC_HMAC_SHA256_CTR.ordinal());
            pbuf.writeByteArray(blocks, false);
            pbuf.writeByteArray(hmac, false);
            pbuf.writeByteArray(ciphertext, false);
            buffer = pbuf.toArray();
        }
        if (priority == Priority.Realtime)
            return parent.getChannel().sendAsync(buffer);
        else
            return parent.getChannel().sendAsyncBackground(buffer);
    }

    public void generateKeys() {
        aesKey = AesStatic.generateKey();
        receiveIv = AesStatic.generateIV();
        sendIv = AesStatic.generateIV();
        hmacKey = Util.concatBytes(sendIv, receiveIv);
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
