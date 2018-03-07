package de.vectordata.jvsl.net.packet.handler;

import java.io.IOException;

import de.vectordata.skynet.net.jvsl.VSLClient;
import de.vectordata.skynet.net.jvsl.packet.P00Handshake;
import de.vectordata.skynet.net.jvsl.packet.P01KeyExchange;
import de.vectordata.skynet.net.jvsl.packet.P03FinishHandshake;
import de.vectordata.skynet.net.jvsl.packet.P04ChangeIV;
import de.vectordata.skynet.net.jvsl.packet.P05KeepAlive;
import de.vectordata.skynet.net.jvsl.packet.P06Accepted;
import de.vectordata.skynet.net.jvsl.packet.P07OpenFileTransfer;
import de.vectordata.skynet.net.jvsl.packet.P08FileHeader;
import de.vectordata.skynet.net.jvsl.packet.P09FileDataBlock;
import de.vectordata.skynet.net.jvsl.packet.Packet;
import de.vectordata.skynet.net.jvsl.util.PacketBuffer;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public class PacketHandler {

    private final VSLClient parent;

    public PacketHandler(VSLClient parent) {
        this.parent = parent;
    }

    private static final Packet[] packets = new Packet[]{
            new P03FinishHandshake(),
            new P06Accepted(),
            new P08FileHeader(),
            new P09FileDataBlock()
    };

    public static Packet getPacket(int id) {
        for (Packet packet : packets) {
            if (packet.getPacketId() == id)
                return packet;
        }
        return null;
    }

    public void handleInternalPacket(byte id, byte[] content) {
        for (Packet packet : packets) {
            if (packet.getPacketId() == id) {
                Packet instance = packet.createNew();
                PacketBuffer buffer = new PacketBuffer(content);
                instance.readPacket(buffer);
                instance.handlePacket(this);
                return;
            }
        }
    }

    public void handleP00Handshake(P00Handshake p00Handshake) {

    }

    public void handleP01KeyExchange(P01KeyExchange p01KeyExchange) {

    }

    public void handleP03FinishHandshake(P03FinishHandshake p03FinishHandshake) {
        switch (p03FinishHandshake.connectionType) {
            case Compatible:
                parent.getPacketCallback().connectionEstablished();
                break;
            case Redirect:
                throw new IllegalStateException("No redirects possible ATM");
            case NotCompatible:
                parent.close();
                break;
        }
    }

    public void handleP04ChangeIV(P04ChangeIV p04ChangeIV) {

    }

    public void handleP05KeepAlive(P05KeepAlive p05KeepAlive) {

    }

    public void handleP06Accepted(P06Accepted p06Accepted) {
        if (p06Accepted.relatedPacket > 5 && p06Accepted.relatedPacket < 10)
            try {
                parent.fileTransfer.onAccepted(p06Accepted);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void handleP07OpenFileTransfer(P07OpenFileTransfer p07OpenFileTransfer) {

    }

    public void handleP08FileHeader(P08FileHeader p08FileHeader) {
        try {
            parent.fileTransfer.onHeaderReceived(p08FileHeader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleP09FileDataBlock(P09FileDataBlock p09FileDataBlock) {
        try {
            parent.fileTransfer.onDataBlockReceived(p09FileDataBlock);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
