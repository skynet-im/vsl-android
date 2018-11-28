package de.vectordata.libjvsl.fileTransfer.streams;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Twometer on 10.03.2018.
 * (c) 2018 Twometer
 */

public abstract class HashOutputStream extends OutputStream {


    public abstract int getPosition();

    public abstract byte[] getHash();

    @Override
    public void write(int b) throws IOException {
        throw new UnsupportedOperationException("Reference implementation does not provide these methods");
    }

    @Override
    public void write(@NonNull byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public abstract void write(@NonNull byte[] b, int off, int len) throws IOException;

    @Override
    public void flush() throws IOException {
        throw new UnsupportedOperationException("Reference implementation does not provide these methods");
    }
}
