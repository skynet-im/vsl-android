package de.vectordata.jvsl.fileTransfer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;

import de.vectordata.jvsl.VSLClient;
import de.vectordata.jvsl.crypt.ContentAlgorithm;
import de.vectordata.jvsl.fileTransfer.streams.AesShaInputStream;
import de.vectordata.jvsl.fileTransfer.streams.AesShaOutputStream;
import de.vectordata.jvsl.fileTransfer.streams.CryptographicOperation;
import de.vectordata.jvsl.fileTransfer.streams.HashInputStream;
import de.vectordata.jvsl.fileTransfer.streams.HashOutputStream;
import de.vectordata.jvsl.fileTransfer.streams.ShaInputStream;
import de.vectordata.jvsl.fileTransfer.streams.ShaOutputStream;
import de.vectordata.jvsl.util.Util;

/**
 * Created by Daniel Lerch on 10.03.2018.
 * © 2018 Daniel Lerch
 */

public class FTEventArgs {

    private VSLClient parent;
    private FTSocket socket;
    private FTEventArgsListener listener;

    private Identifier identifier;
    private StreamMode mode;
    private FileMeta fileMeta;
    private String path;
    private HashInputStream hashInputStream;
    private HashOutputStream hashOutputStream;


    public FTEventArgs(Identifier identifier) {
        this.identifier = identifier;
    }

    public FTEventArgs(Identifier identifier, FileMeta meta, String path) {
        this.identifier = identifier;
        this.fileMeta = meta;
        this.path = path;
    }

    protected FTEventArgs(Identifier identifier, StreamMode mode) {
        this.identifier = identifier;
        this.mode = mode;
    }

    public void setListener(FTEventArgsListener listener) {
        this.listener = listener;
    }

    public boolean assign(VSLClient parent, FTSocket socket) {
        if (this.parent == null || this.socket == null) {

            if (parent == null) throw new IllegalArgumentException("parent must not be null");
            this.parent = parent;

            if (socket == null) throw new IllegalArgumentException("socket must not be null");
            this.socket = socket;

            return true;
        }
        return false;
    }

    public void openStream() throws FileNotFoundException {
        if (mode == StreamMode.GET_HEADER || mode == StreamMode.PUSH_HEADER)
            throw new IllegalStateException("Cannot open file stream with " + mode);

        if (mode == StreamMode.GET_FILE) {
            FileOutputStream outputStream = new FileOutputStream(path);
            if (fileMeta.getFileEncryption() == ContentAlgorithm.NONE || !fileMeta.isAvailable())
                hashOutputStream = new ShaOutputStream(outputStream);
            else if (fileMeta.getFileEncryption() == ContentAlgorithm.AES_256_CBC)
                hashOutputStream = new AesShaOutputStream(outputStream, fileMeta.getFileKey(), CryptographicOperation.Decrypt);
            else
                throw new IllegalArgumentException("Cannot open file stream with " + fileMeta.getFileEncryption().name());
        } else {
            FileInputStream inputStream = new FileInputStream(path);
            if (fileMeta.getFileEncryption() == ContentAlgorithm.NONE || !fileMeta.isAvailable())
                hashInputStream = new ShaInputStream(inputStream);
            else if (fileMeta.getFileEncryption() == ContentAlgorithm.AES_256_CBC)
                hashInputStream = new AesShaInputStream(inputStream, fileMeta.getFileKey(), CryptographicOperation.Decrypt);
            else
                throw new IllegalArgumentException("Cannot open file stream with " + fileMeta.getFileEncryption().name());
        }
    }

    public void closeStream(boolean success) throws StreamCorruptedException {
        if (success) {
            if (hashOutputStream != null) try {
                hashOutputStream.flush();
            } catch (IOException e) {
                onCancelled();
            }
            byte[] hash = getHash();
            if (fileMeta.isAvailable() && parent.getConnectionVersion() > 1 && !Util.sequenceEqual(hash, fileMeta.getSha256())) {
                throw new StreamCorruptedException("Integrity of file cannot be validated.");
            }
        }
        closeStreamInternal(true, success);
    }

    public void closeStreamInternal(boolean events, boolean success) {
        if (events)
            onFinished();
        hashInputStream = null;
        hashOutputStream = null;
    }

    public void onFileMetaTransferred() {
        if (mode == StreamMode.GET_HEADER || mode == StreamMode.GET_FILE)
            if (listener != null) listener.onFileMetaReceived(this);
        FTProgressEventArgs args = new FTProgressEventArgs(0, fileMeta.getLength());
        if (listener != null) listener.onProgress(this, args);
    }

    public void onProgress() {
        FTProgressEventArgs args = new FTProgressEventArgs(getPosition(), fileMeta.getLength());
        if (listener != null) listener.onProgress(this, args);
    }

    public void onFinished() {
        if (listener != null) listener.onFinished(this);
    }

    public void onCancelled() {
        if (listener != null) listener.onCanceled(this);
    }

    long getPosition() {
        if (hashOutputStream != null) return hashOutputStream.getPosition();
        else if (hashInputStream != null) return hashInputStream.getPosition();
        return -1;
    }

    private byte[] getHash() {
        if (hashOutputStream != null) return hashOutputStream.getHash();
        else if (hashInputStream != null) return hashInputStream.getHash();
        return null;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public StreamMode getMode() {
        return mode;
    }

    public FileMeta getFileMeta() {
        return fileMeta;
    }

    public String getPath() {
        return path;
    }

    public HashInputStream getHashInputStream() {
        return hashInputStream;
    }

    public HashOutputStream getHashOutputStream() {
        return hashOutputStream;
    }

    void setFileMeta(FileMeta fileMeta) {
        this.fileMeta = fileMeta;
    }

    void setPath(String path) {
        this.path = path;
    }

    public void setMode(StreamMode mode) {
        this.mode = mode;
    }
}

