package de.vectordata.jvsl.net;

import de.vectordata.jvsl.net.packet.P00Handshake;
import de.vectordata.jvsl.net.packet.P01KeyExchange;
import de.vectordata.jvsl.net.packet.P03FinishHandshake;
import de.vectordata.jvsl.net.packet.P04ChangeIV;
import de.vectordata.jvsl.net.packet.P05KeepAlive;
import de.vectordata.jvsl.net.packet.P06Accepted;
import de.vectordata.jvsl.net.packet.P07OpenFileTransfer;
import de.vectordata.jvsl.net.packet.P08FileHeader;
import de.vectordata.jvsl.net.packet.P09FileDataBlock;
import de.vectordata.jvsl.net.packet.Packet;
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

    public boolean tryGetPacket(byte id, Ref<Packet> packet) {
        if (id >= registeredPackets.length) {
            packet.set(null);
            return false;
        }
        packet.set(registeredPackets[id].getPacket());
        return packet.get() != null;
    }

    public void handleInternalPacket(byte id, byte[] receive, CryptoAlgorithm none) {
        // TODO Implement
    }

    public void handleP00Handshake(P00Handshake p00Handshake) {

    }

    public void handleP01KeyExchange(P01KeyExchange p01KeyExchange) {

    }

    public void handleP03FinishHandshake(P03FinishHandshake p03FinishHandshake) {

    }

    public void handleP04ChangeIV(P04ChangeIV p04ChangeIV) {

    }

    public void handleP05KeepAlive(P05KeepAlive p05KeepAlive) {

    }

    public void handleP06Accepted(P06Accepted p06Accepted) {

    }

    public void handleP07OpenFileTransfer(P07OpenFileTransfer p07OpenFileTransfer) {

    }

    public void handleP08FileHeader(P08FileHeader p08FileHeader) {

    }

    public void handleP09FileDataBlock(P09FileDataBlock p09FileDataBlock) {

    }
}
