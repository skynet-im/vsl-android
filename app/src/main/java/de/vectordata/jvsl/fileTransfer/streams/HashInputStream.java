package de.vectordata.jvsl.fileTransfer.streams;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Twometer on 10.03.2018.
 * (c) 2018 Twometer
 */

public abstract class HashInputStream extends InputStream {

    public abstract int getPosition();
    public abstract byte[] getHash();

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException("Reference implementation does not provide these methods");
    }

    @Override
    public int read(@NonNull byte[] b) throws IOException {
        throw new UnsupportedOperationException("Reference implementation does not provide these methods");
    }

    @Override
    public abstract int read(@NonNull byte[] b, int off, int len) throws IOException;

}
