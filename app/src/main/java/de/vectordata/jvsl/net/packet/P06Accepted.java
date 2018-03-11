package de.vectordata.jvsl.net.packet;


import java.io.IOException;

import de.vectordata.jvsl.net.PacketHandler;
import de.vectordata.jvsl.net.packet.util.ProblemCategory;
import de.vectordata.jvsl.util.PacketBuffer;
import de.vectordata.jvsl.util.cscompat.Nullable;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public class P06Accepted implements Packet {

    public boolean accepted;
    public byte relatedPacket;
    private ProblemCategory problemCategory;

    public P06Accepted(boolean accepted, byte relatedPacket, ProblemCategory problemCategory) {
        this.accepted = accepted;
        this.relatedPacket = relatedPacket;
        this.problemCategory = problemCategory;
    }

    public P06Accepted() {
    }

    @Override
    public byte getPacketId() {
        return 6;
    }

    @Override
    public Nullable<Integer> getConstantLength() {
        return new Nullable<>(3);
    }

    @Override
    public Packet createNew() {
        return new P06Accepted();
    }

    @Override
    public void handlePacket(PacketHandler handler) throws IOException {
        handler.handleP06Accepted(this);
    }

    @Override
    public void readPacket(PacketBuffer buffer) {
        accepted = buffer.readBool();
        relatedPacket = buffer.readByte();
        problemCategory = ProblemCategory.values()[buffer.readByte()];
    }

    @Override
    public void writePacket(PacketBuffer buffer) {
        buffer.writeBool(accepted);
        buffer.writeByte(relatedPacket);
        buffer.writeByte((byte) problemCategory.ordinal());
    }
}

