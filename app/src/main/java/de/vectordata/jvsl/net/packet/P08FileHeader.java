package de.vectordata.jvsl.net.packet;

import de.vectordata.jvsl.net.PacketHandler;
import de.vectordata.jvsl.util.PacketBuffer;
import de.vectordata.jvsl.util.cscompat.DateTime;
import de.vectordata.jvsl.util.cscompat.Nullable;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public class P08FileHeader implements Packet {

    public String name;
    public long length;
    private long attributes;
    private DateTime creationTime;
    private DateTime lastAccessTime;
    public DateTime lastWriteTime;
    private byte[] thumbnail;
    private byte[] sha256;

    public P08FileHeader(String name, long length, long attributes, DateTime creationTime, DateTime lastAccessTime, DateTime lastWriteTime, byte[] thumbnail, byte[] sha256) {
        this.name = name;
        this.length = length;
        this.attributes = attributes;
        this.creationTime = creationTime;
        this.lastAccessTime = lastAccessTime;
        this.lastWriteTime = lastWriteTime;
        this.thumbnail = thumbnail;
        this.sha256 = sha256;
    }

    public P08FileHeader() {
    }

    @Override
    public byte getPacketId() {
        return 8;
    }

    @Override
    public Nullable<Integer> getConstantLength() {
        return new Nullable<>(null);
    }

    @Override
    public Packet createNew() {
        return new P08FileHeader();
    }

    @Override
    public void handlePacket(PacketHandler handler) {
        handler.handleP08FileHeader(this);
    }

    @Override
    public void readPacket(PacketBuffer buffer) {
        name = buffer.readString();
        length = buffer.readLong();
        attributes = buffer.readUInt32();
        creationTime = buffer.readDate();
        lastAccessTime = buffer.readDate();
        lastWriteTime = buffer.readDate();
        thumbnail = buffer.readByteArray();
        sha256 = buffer.readByteArray(32);
    }

    @Override
    public void writePacket(PacketBuffer buffer) {
        buffer.writeString(name);
        buffer.writeLong(length);
        buffer.writeUInt32(attributes);
        buffer.writeDate(creationTime);
        buffer.writeDate(lastAccessTime);
        buffer.writeDate(lastWriteTime);
        buffer.writeByteArray(thumbnail);
        buffer.writeByteArray(sha256, false);
    }

}
