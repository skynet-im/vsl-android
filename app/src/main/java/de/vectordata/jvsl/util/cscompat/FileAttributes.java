package de.vectordata.jvsl.util.cscompat;

/**
 * Created by Twometer on 10.03.2018.
 * (c) 2018 Twometer
 */

public enum FileAttributes {
    ReadOnly(0x1),
    Hidden(0x2),
    System(0x4),
    Directory(0x10),
    Archive(0x20),
    Device(0x40),
    Normal(0x80),
    Temporary(0x100),
    SparseFile(0x200),
    ReparsePoint(0x400),
    Compressed(0x800),
    Offline(0x1000),
    NotContentIndexed(0x2000),
    Encrypted(0x4000);

    private final int value;

    FileAttributes(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static FileAttributes parse(int val) {
        for (FileAttributes attr : values()) if (attr.getValue() == val) return attr;
        return null;
    }
}
