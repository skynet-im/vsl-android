package de.vectordata.jvsl.fileTransfer;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

import de.vectordata.jvsl.crypt.AesStatic;
import de.vectordata.jvsl.crypt.ContentAlgorithm;
import de.vectordata.jvsl.crypt.Hash;
import de.vectordata.jvsl.crypt.HmacStatic;
import de.vectordata.jvsl.util.Constants;
import de.vectordata.jvsl.util.PacketBuffer;
import de.vectordata.jvsl.util.Util;
import de.vectordata.jvsl.util.cscompat.DateTime;
import de.vectordata.jvsl.util.cscompat.FileAttributes;
import de.vectordata.jvsl.util.cscompat.FileUtils;

/**
 * Created by Twometer on 10.03.2018.
 * (c) 2018 Twometer
 */

public class FileMeta {

    private static final String TAG = "FileMeta";

    private byte[] encryptedContent;

    private ContentAlgorithm algorithm = ContentAlgorithm.NONE;
    private long length;
    private boolean available;

    private byte[] hmacKey;
    private byte[] aesKey;
    private String name;
    private FileAttributes attributes;
    private DateTime creationTime;
    private DateTime lastAccessTime;
    private DateTime lastWriteTime;
    private byte[] thumbnail;
    private byte[] sha256;
    private ContentAlgorithm fileEncryption = ContentAlgorithm.NONE;
    private byte[] fileKey;

    public FileMeta(byte[] binaryData, short connectionVersion) {
        if (binaryData == null) throw new IllegalArgumentException("binaryData must not be null");
        if (connectionVersion < Constants.COMPATIBILITY_VERSION || connectionVersion > Constants.VERSION_NUMBER)
            throw new UnsupportedOperationException("Incompatible version");

        if (connectionVersion == 1) {
            if (binaryData.length < 44)
                throw new IllegalArgumentException("Invalid binary data length");
            read_v1_1(new PacketBuffer(binaryData));
        } else if (connectionVersion == 2) {
            if (binaryData.length < 78)
                throw new IllegalArgumentException("Invalid binary data length");
            read_v1_2(new PacketBuffer(binaryData));
        }
    }

    public FileMeta(byte[] binaryData, byte[] hmacKey, byte[] aesKey) {
        if (binaryData == null) throw new IllegalArgumentException("binaryData must not be null");
        if (binaryData.length < 78) throw new IllegalArgumentException("Invalid binaryData length");
        if (hmacKey == null) throw new IllegalArgumentException("hmacKey must not be null");
        if (hmacKey.length != 32) throw new IllegalArgumentException("hmacKey must be 32 bytes");
        if (aesKey == null) throw new IllegalArgumentException("aesKey must not be null");
        if (aesKey.length != 32) throw new IllegalArgumentException("aesKey must be 32 bytes");
        read_v1_2(new PacketBuffer(binaryData), hmacKey, aesKey);
    }

    private void read_v1_1(PacketBuffer buf) {
        algorithm = ContentAlgorithm.NONE;
        name = buf.readString();
        length = buf.readInt64();
        attributes = FileAttributes.parse((int) buf.readUInt32());
        creationTime = buf.readDate();
        lastAccessTime = buf.readDate();
        lastWriteTime = buf.readDate();
        thumbnail = buf.readByteArray();
        if (buf.getPending() >= 32)
            sha256 = buf.readByteArray(32);
        else
            sha256 = new byte[32];
        available = true;
    }

    private void read_v1_2(PacketBuffer buf) {
        read_v1_2_header(buf);
        if (algorithm == ContentAlgorithm.NONE) {
            read_v1_2_core(buf);
            available = true;
        } else encryptedContent = buf.readByteArray(buf.getPending());
    }

    private void read_v1_2(PacketBuffer buf, byte[] hmacKey, byte[] aesKey) {
        read_v1_2_header(buf);
        if (algorithm == ContentAlgorithm.NONE)
            read_v1_2_core(buf);
        else if (algorithm == ContentAlgorithm.AES_256_CBC_HMAC_SHA_256) {
            byte[] hmac = buf.readByteArray();
            int pos = buf.getPosition();
            int pending = buf.getPending();
            byte[] b = buf.readByteArray(pending);
            if (!Util.sequenceEqual(hmac, HmacStatic.computeHmacSHA256(b, hmacKey)))
                throw new SecurityException("Message corrupted");
            buf.setPosition(pos);
            this.hmacKey = hmacKey;
            byte[] iv = buf.readByteArray(16);
            byte[] plain = AesStatic.decrypt(buf.readByteArray(buf.getPending()), aesKey, iv);
            this.aesKey = aesKey;
            PacketBuffer innerBuf = new PacketBuffer(plain);
            read_v1_2_core(innerBuf);
            available = true;
        } else encryptedContent = buf.readByteArray(buf.getPending());
    }

    private void read_v1_2_header(PacketBuffer buf) {
        algorithm = ContentAlgorithm.values()[buf.readByte()];
        length = buf.readInt64();
        fileEncryption = ContentAlgorithm.values()[buf.readByte()];
    }

    private void read_v1_2_core(PacketBuffer buf) {
        name = buf.readString();
        attributes = FileAttributes.parse((int) buf.readUInt32());
        creationTime = buf.readDate();
        lastAccessTime = buf.readDate();
        lastWriteTime = buf.readDate();
        thumbnail = buf.readByteArray();
        sha256 = buf.readByteArray(32);
        if (fileEncryption == ContentAlgorithm.AES_256_CBC)
            fileKey = buf.readByteArray(32);
    }

    public void decrypt(byte[] hmacKey, byte[] aesKey) {
        if (hmacKey == null) throw new IllegalArgumentException("hmacKey must not be null");
        if (hmacKey.length != 32) throw new IllegalArgumentException("hmacKey must be 32 bytes");
        if (aesKey == null) throw new IllegalArgumentException("aesKey must not be null");
        if (aesKey.length != 32) throw new IllegalArgumentException("aesKey must be 32 bytes");

        PacketBuffer buf = new PacketBuffer(getBinaryData(Constants.VERSION_NUMBER));
        read_v1_2(buf, hmacKey, aesKey);
    }

    public FileMeta(String path, ContentAlgorithm algorithm) throws IOException {
        this(path, algorithm, null, null, null);
    }

    public FileMeta(String path, ContentAlgorithm algorithm, byte[] hmacKey, byte[] aesKey, byte[] fileKey) throws IOException {
        if (path == null || path.trim().length() == 0)
            throw new IllegalArgumentException("path must not be null");
        if (algorithm != ContentAlgorithm.NONE && algorithm != ContentAlgorithm.AES_256_CBC_HMAC_SHA_256)
            throw new UnsupportedOperationException("content algorithm not supported");

        this.algorithm = algorithm;
        this.aesKey = aesKey;
        this.hmacKey = hmacKey;
        this.fileKey = fileKey;

        if (algorithm == ContentAlgorithm.AES_256_CBC_HMAC_SHA_256) {
            if (this.aesKey == null)
                this.aesKey = AesStatic.generateKey();
            else if (aesKey.length != 32)
                throw new IllegalArgumentException("aesKey must be 32 bytes");

            if (this.hmacKey == null)
                this.hmacKey = AesStatic.generateKey();
            else if (hmacKey.length != 32)
                throw new IllegalArgumentException("hmacKey must be 32 bytes");

            if (this.fileKey == null)
                this.fileKey = AesStatic.generateKey();
            else if (fileKey.length != 32)
                throw new IllegalArgumentException("fileKey must be 32 bytes");

            fileEncryption = ContentAlgorithm.AES_256_CBC;
        }

        loadFromFile(path);
        available = true;
    }

    public byte[] getBinaryData(int version) {
        PacketBuffer buf = new PacketBuffer();
        if (version == 1)
            write_v1_1(buf);
        else {
            if (algorithm == ContentAlgorithm.NONE) {
                write_v1_2_header(buf);
                write_v1_2_core(buf);
            } else if (algorithm == ContentAlgorithm.AES_256_CBC_HMAC_SHA_256) {
                write_v1_2_header(buf);
                if (available) {
                    byte[] plaindata;
                    PacketBuffer ibuf = new PacketBuffer();
                    write_v1_2_core(ibuf);
                    plaindata = ibuf.toArray();

                    byte[] iv = AesStatic.generateIV();
                    byte[] ciphertext = AesStatic.encrypt(plaindata, aesKey, iv);
                    buf.writeByteArray(HmacStatic.computeHmacSHA256(Util.concatBytes(iv, ciphertext), hmacKey), false);
                    buf.writeByteArray(iv, false);
                    buf.writeByteArray(ciphertext, false);
                } else
                    buf.writeByteArray(encryptedContent, false);
            }
        }
        return buf.toArray();
    }

    public byte[] getPlainData(int version) {
        PacketBuffer buf = new PacketBuffer();
        if (version == 1)
            write_v1_1(buf);
        else {
            write_v1_2_header(buf);
            write_v1_2_core(buf);
        }
        return buf.toArray();
    }

    private void loadFromFile(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) throw new FileNotFoundException("File to load not found");
        sha256 = Hash.sha256(file);
        name = file.getName();
        if (fileEncryption == ContentAlgorithm.NONE)
            length = file.length();
        else if (fileEncryption == ContentAlgorithm.AES_256_CBC)
            length = Util.getTotalSize(file.length() + 17, 16);

        attributes = FileAttributes.Normal;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            BasicFileAttributes fileAttr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            creationTime = DateTime.fromMillis(fileAttr.creationTime().toMillis());
            lastAccessTime = DateTime.fromMillis(fileAttr.lastAccessTime().toMillis());
            lastWriteTime = DateTime.fromMillis(fileAttr.lastModifiedTime().toMillis());
        } else {
            creationTime = DateTime.now();
            lastAccessTime = DateTime.now();
            lastWriteTime = DateTime.now();
        }
        thumbnail = new byte[0]; // still unsupported

    }

    private void write_v1_1(PacketBuffer buf) {
        algorithm = ContentAlgorithm.NONE;
        buf.writeString(name);
        buf.writeUInt64(length);
        buf.writeUInt32(attributes.getValue());
        buf.writeDate(creationTime);
        buf.writeDate(lastAccessTime);
        buf.writeDate(lastWriteTime);
        buf.writeByteArray(thumbnail, true);
        buf.writeByteArray(sha256, false);
    }

    private void write_v1_2_header(PacketBuffer buf) {
        buf.writeByte((byte) algorithm.ordinal());
        buf.writeUInt64(length);
        buf.writeByte((byte) fileEncryption.ordinal());
    }

    private void write_v1_2_core(PacketBuffer buf) {
        buf.writeString(name);
        buf.writeUInt32(attributes.getValue());
        buf.writeDate(creationTime);
        buf.writeDate(lastAccessTime);
        buf.writeDate(lastWriteTime);
        buf.writeByteArray(thumbnail, true);
        buf.writeByteArray(sha256, true);
        if (fileEncryption == ContentAlgorithm.AES_256_CBC)
            buf.writeByteArray(fileKey, false);
    }

    public String apply(String sourcePath, String targetDir) {
        if (!available)
            throw new IllegalStateException("can't apply encrypted filemeta");
        if (!FileUtils.directoryExists(targetDir))
            throw new IllegalArgumentException("Target dir not found");

        File current = new File(sourcePath);
        if (FileUtils.fileExists(FileUtils.pathCombine(targetDir, name))) {
            long count = 2;
            String name = this.name.substring(0, this.name.lastIndexOf("."));
            String extension = this.name.substring(this.name.lastIndexOf("."));
            while (true) {
                String newpath = FileUtils.pathCombine(targetDir, name + " (" + count + ")." + extension);
                if (FileUtils.fileExists(newpath))
                    count++;
                else {
                    if (!current.renameTo(new File(newpath)))
                        Log.e(TAG, "Failed to move file to " + newpath);
                    break;
                }
            }
        } else {
            if (!current.renameTo(new File(FileUtils.pathCombine(targetDir, name))))
                Log.e(TAG, "Failed to move file");
        }
        return current.getAbsolutePath();
    }

    public ContentAlgorithm getAlgorithm() {
        return algorithm;
    }

    public long getLength() {
        return length;
    }

    public boolean isAvailable() {
        return available;
    }

    public byte[] getHmacKey() {
        return hmacKey;
    }

    public byte[] getAesKey() {
        return aesKey;
    }

    public String getName() {
        return name;
    }

    public FileAttributes getAttributes() {
        return attributes;
    }

    public DateTime getCreationTime() {
        return creationTime;
    }

    public DateTime getLastAccessTime() {
        return lastAccessTime;
    }

    public DateTime getLastWriteTime() {
        return lastWriteTime;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public byte[] getSha256() {
        return sha256;
    }

    public ContentAlgorithm getFileEncryption() {
        return fileEncryption;
    }

    public byte[] getFileKey() {
        return fileKey;
    }
}
