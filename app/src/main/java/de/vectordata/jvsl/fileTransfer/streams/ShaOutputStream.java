package de.vectordata.jvsl.fileTransfer.streams;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Twometer on 10.03.2018.
 * (c) 2018 Twometer
 */

public class ShaOutputStream extends HashOutputStream {

    private int position;
    private DigestOutputStream digestOutputStream;

    public ShaOutputStream(OutputStream outputStream) {
        try {
            digestOutputStream = new DigestOutputStream(outputStream, MessageDigest.getInstance("SHA-256"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public byte[] getHash() {
        return digestOutputStream.getMessageDigest().digest();
    }

    @Override
    public void write(@NonNull byte[] b, int off, int len) throws IOException {
        position += len;
        digestOutputStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        digestOutputStream.flush();
    }
}
