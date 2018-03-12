package de.vectordata.jvsl.net;

import java.io.IOException;

import de.vectordata.jvsl.VSLClient;
import de.vectordata.jvsl.net.packet.P00Handshake;
import de.vectordata.jvsl.net.packet.P01KeyExchange;
import de.vectordata.jvsl.net.packet.P03FinishHandshake;
import de.vectordata.jvsl.net.packet.P05KeepAlive;
import de.vectordata.jvsl.net.packet.P06Accepted;
import de.vectordata.jvsl.net.packet.P07OpenFileTransfer;
import de.vectordata.jvsl.net.packet.P08FileHeader;
import de.vectordata.jvsl.net.packet.P09FileDataBlock;
import de.vectordata.jvsl.net.packet.Packet;
import de.vectordata.jvsl.net.packet.util.KeepAliveRole;
import de.vectordata.jvsl.util.PacketBuffer;
import de.vectordata.jvsl.util.cscompat.Ref;

/**
 * Created by Daniel Lerch on 07.03.2018.
 * © 2018 Daniel Lerch
 */

public class PacketHandler {

    private static PacketRule[] registeredPackets;

    static {
        registeredPackets = initRules(
                // P00Handshake     -   Server only
                // P01KeyExchange   -   Server only
                // P02Certificate   -   Not supported in VSL 1.1/1.2
                new PacketRule(new P03FinishHandshake(), CryptoAlgorithm.NONE, CryptoAlgorithm.AES_256_CBC_HMAC_SHA256_MP3),
                // P04ChangeIV      -   Server only
                new PacketRule(new P05KeepAlive(), CryptoAlgorithm.NONE),
                new PacketRule(new P06Accepted(), CryptoAlgorithm.AES_256_CBC_HMAC_SHA256_MP3),
                new PacketRule(new P07OpenFileTransfer(), CryptoAlgorithm.AES_256_CBC_HMAC_SHA256_MP3),
                new PacketRule(new P08FileHeader(), CryptoAlgorithm.AES_256_CBC_HMAC_SHA256_MP3),
                new PacketRule(new P09FileDataBlock(), CryptoAlgorithm.AES_256_CBC_HMAC_SHA256_MP3)
        );
    }

    private static PacketRule[] initRules(PacketRule... rules) {
        PacketRule[] value = new PacketRule[10];
        for (PacketRule rule : rules) {
            value[rule.getPacket().getPacketId()] = rule;
        }
        return value;
    }

    private VSLClient parent;

    public PacketHandler(VSLClient parent) {
        this.parent = parent;
    }

    public boolean tryGetPacket(byte id, Ref<Packet> packet) {
        if (id >= registeredPackets.length || id < 0) {
            packet.set(null);
            return false;
        }
        packet.set(registeredPackets[id].getPacket());
        return packet.get() != null;
    }

    public void handleInternalPacket(byte id, byte[] content, CryptoAlgorithm alg) throws IOException {
        if (id >= registeredPackets.length)
            throw new IllegalArgumentException("Unknown packet");
        PacketRule rule = registeredPackets[id];
        if (rule == null)
            throw new IllegalArgumentException("Invalid packet");
        if (!rule.verifyAlgorithm(alg))
            throw new IllegalArgumentException("Wrong algorithm");
        Packet packet = rule.getPacket().createNew();
        PacketBuffer buf = new PacketBuffer(content);
        packet.readPacket(buf);
        packet.handlePacket(this);
    }

    public void handleP00Handshake(P00Handshake p00Handshake) {
        throw new IllegalStateException("VSL clients cannot handle P00Handshake.");
    }

    public void handleP01KeyExchange(P01KeyExchange p01KeyExchange) {
        throw new IllegalStateException("VSL clients cannot handle P01KeyExchange.");
    }

    /*
    public void handleP02Certificate(P02Certificate) {
        throw new UnsupportedOperationException("VSL 1.2 does not support key exchange validated by certificates.");
    }
    */

    public void handleP03FinishHandshake(P03FinishHandshake p03FinishHandshake) throws IOException {
        switch (p03FinishHandshake.connectionState) {
            case CompatibilityMode:
                throw new UnsupportedOperationException("This VSL client does not support protocol version 1.1.");
            case Redirect:
                throw new UnsupportedOperationException("This VSL version does not support redirects.");
            case NotCompatible:
                throw new IOException("Server refused connection due to incompatible versions.");
            case Compatible:
                parent.onConnectionEstablished(p03FinishHandshake.vslVersion);
                break;
        }
    }

    /*
    public void handleP04ChangeIV(P04ChangeIV p04ChangeIV) {
        throw new IllegalStateException("VSL clients can not handle P04ChangeIV.");
    }
    */

    public void handleP05KeepAlive(P05KeepAlive p05KeepAlive) {
        if (p05KeepAlive.getRole() == KeepAliveRole.REQUEST)
            parent.getManager().sendPacket(new P05KeepAlive(KeepAliveRole.RESPONSE));
    }

    public void handleP06Accepted(P06Accepted p06Accepted) throws IOException {
        if (p06Accepted.relatedPacket > 5 && p06Accepted.relatedPacket < 10)
            parent.getFileTransfer().onPacketReceived(p06Accepted);
        else
            throw new IllegalArgumentException("Could not resume related packet with id " + p06Accepted.relatedPacket);
    }

    public void handleP07OpenFileTransfer(P07OpenFileTransfer p07OpenFileTransfer) {
        parent.getFileTransfer().onPacketReceived(p07OpenFileTransfer);
    }

    public void handleP08FileHeader(P08FileHeader p08FileHeader) {
        parent.getFileTransfer().onPacketReceived(p08FileHeader);
    }

    public void handleP09FileDataBlock(P09FileDataBlock p09FileDataBlock) throws IOException {
        parent.getFileTransfer().onPacketReceived(p09FileDataBlock);
    }
}
