package de.vectordata.jvsl.net;

import de.vectordata.jvsl.net.packet.Packet;

/**
 * Created by Daniel Lerch on 08.03.2018.
 * © 2018 Daniel Lerch
 */

class PacketRule {
    private final Packet packet;
    private final boolean[] algorithms;

    PacketRule(Packet packet, CryptoAlgorithm... algs) {
        this.packet = packet;
        algorithms = new boolean[4];
        for (CryptoAlgorithm alg : algs) {
            algorithms[alg.ordinal()] = true;
        }
    }

    public Packet getPacket() {
        return packet;
    }

    boolean verifyAlgorithm(CryptoAlgorithm algorithm) {
        return algorithms[algorithm.ordinal()];
    }
}
