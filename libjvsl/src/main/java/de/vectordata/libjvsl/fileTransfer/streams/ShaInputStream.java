package de.vectordata.libjvsl.fileTransfer.streams;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Twometer on 10.03.2018.
 * (c) 2018 Twometer
 */

public class ShaInputStream extends HashInputStream {

    private DigestInputStream digestInputStream;
    private int position;

    public ShaInputStream(InputStream inputStream) {
        try {
            this.digestInputStream = new DigestInputStream(inputStream, MessageDigest.getInstance("SHA-256"));
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
        return digestInputStream.getMessageDigest().digest();
    }

    @Override
    public int read(@NonNull byte[] b, int off, int len) throws IOException {
        int i = digestInputStream.read(b, off, len);
        position += i;
        return i;
    }
}
