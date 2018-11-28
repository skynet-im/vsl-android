package de.vectordata.libjvsl.fileTransfer;

import de.vectordata.libjvsl.util.PacketBuffer;
import de.vectordata.libjvsl.util.Util;

/**
 * Created by Daniel Lerch on 10.03.2018.
 * © 2018 Daniel Lerch
 */

public class Identifier {

    private IdentificationMode mode;
    private Object id;

    public Identifier(int id) {
        mode = IdentificationMode.UINT_32;
        this.id = id;
    }

    private Identifier(long id) {
        mode = IdentificationMode.UINT_64;
        this.id = id;
    }

    private Identifier(byte[] id) {
        mode = IdentificationMode.BYTE_ARRAY;
        this.id = id;
    }

    private Identifier(String id) {
        mode = IdentificationMode.STRING;
        this.id = id;
    }

    public enum IdentificationMode {
        UINT_32,
        UINT_64,
        BYTE_ARRAY,
        STRING
    }

    public static Identifier fromBinary(PacketBuffer buf) {
        IdentificationMode type = IdentificationMode.values()[buf.readByte()];
        switch (type) {
            case UINT_32:
                return new Identifier(buf.readInt32()); // We must use writeInt32 to force an overflow if the id gets big.
            case UINT_64:
                return new Identifier(buf.readInt64());
            case BYTE_ARRAY:
                return new Identifier(buf.readByteArray(buf.readUInt16() + 1));
            case STRING:
                return new Identifier(buf.readString());
            default:
                return null;
        }
    }

    public void toBinary(PacketBuffer buf) {
        buf.writeByte((byte) mode.ordinal());
        switch (mode) {
            case UINT_32:
                buf.writeInt32((int) id); // We must use writeInt32 to force an overflow if the id gets big.
                break;
            case UINT_64:
                buf.writeInt64((long) id);
                break;
            case BYTE_ARRAY:
                byte[] bytes = (byte[]) this.id;
                buf.writeUInt16(bytes.length - 1);
                buf.writeByteArray(bytes, false);
                break;
            case STRING:
                buf.writeString((String) id);
                break;
        }
    }

    @Override
    public String toString() {
        switch (mode) {
            case UINT_32:
                return ((Integer) (int) id).toString();
            case UINT_64:
                return ((Long) (long) id).toString();
            case BYTE_ARRAY:
                return Util.toHexString((byte[]) id);
            case STRING:
                return (String) id;
            default:
                return getClass().getSuperclass().toString();
        }
    }
}
