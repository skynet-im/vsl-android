package de.vectordata.jvsl.net.packet.util;


import de.vectordata.jvsl.util.PacketBuffer;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
/// <summary>
/// Provides different identifiers for file transfers.
/// </summary>
public class Identifier {

    private final IdentificationMode Mode;

    final Object ID;

    public Identifier(long id) {
        Mode = IdentificationMode.UInt32;
        ID = id;
    }

    private Identifier(long id, boolean x64) {
        Mode = IdentificationMode.UInt64;
        ID = id;
    }

    private Identifier(byte[] id) {
        Mode = IdentificationMode.ByteArray;
        ID = id;
    }

    private Identifier(String id) {
        Mode = IdentificationMode.String;
        ID = id;
    }

    private enum IdentificationMode {

        UInt32,
        UInt64,
        ByteArray,
        String
    }

    /// <summary>
    /// Reads an identifier from binary data.
    /// </summary>
    /// <param name="buf">PacketBuffer containing the binary identifer.</param>
    /// <returns></returns>
    public static Identifier fromBinary(PacketBuffer buf) {
        IdentificationMode Type = IdentificationMode.values()[buf.readByte()];
        switch (Type) {
            case UInt32:
                return new Identifier(buf.readUInt32());
            case UInt64:
                return new Identifier(buf.readUInt32(), true);
            case ByteArray:
                int length = buf.readUInt16() + 1; // 0 bytes length is not valid
                return new Identifier(buf.readByteArray(length));
            case String:
                return new Identifier(buf.readString());
            default:
                return null;
        }
    }

    /// <summary>
    /// Deserializes the identifier into an byte array.
    /// </summary>
    /// <returns></returns>
    public byte[] toBinary() {
        PacketBuffer buf = new PacketBuffer();
        toBinary(buf);
        return buf.toArray();
    }

    /// <summary>
    /// Deserializes the identifier into binary data.
    /// </summary>
    /// <param name="buf">PacketBuffer to write data in.</param>
    /// <returns></returns>
    public void toBinary(PacketBuffer buf) {
        buf.writeByte((byte) Mode.ordinal());
        switch (Mode) {
            case UInt32:
                buf.writeUInt32((long) ID);
                break;
            case UInt64:
                buf.writeUInt32((long) ID);
                break;
            case ByteArray:
                byte[] id = (byte[]) ID;
                buf.writeUInt16(id.length);
                buf.writeByteArray(id, false);
                break;
            case String:
                buf.writeString((String) ID);
                break;
        }
    }
}
