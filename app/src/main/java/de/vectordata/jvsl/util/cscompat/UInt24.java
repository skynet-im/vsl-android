package de.vectordata.jvsl.util.cscompat;

import de.vectordata.jvsl.util.Util;

/**
 * Created by Twometer on 09.03.2018.
 * (c) 2018 Twometer
 */

public class UInt24 {

    private int value;

    public UInt24(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public byte[] toByteArray(Endianness endianness) {
        byte[] array = new byte[]{(byte) value, (byte) (value >> 8), (byte) (value >> 16)};
        if (endianness != Endianness.BigEndian)
            Util.reverseBytes(array);
        return array;
    }

    public static UInt24 fromByteArray(byte[] b, Endianness endianness) {
        if (b.length != 3)
            throw new IllegalArgumentException("UInt24 can only be created of a byte array with length of 3");
        if (endianness != Endianness.BigEndian)
            Util.reverseBytes(b);
        int v1 = endianness == Endianness.BigEndian ? 0x000000FF : 0x00FF0000;
        int v2 = 0x0000FF00;
        int v3 = endianness == Endianness.BigEndian ? 0x00FF0000 : 0x000000FF;
        int value = ((int) (b[0]) & v1) | (((int) (b[1]) << 8) & v2) | (((int) (b[2]) << 16) & v3);
        return new UInt24(value);
    }

    public enum Endianness {
        LittleEndian,
        BigEndian
    }
}
