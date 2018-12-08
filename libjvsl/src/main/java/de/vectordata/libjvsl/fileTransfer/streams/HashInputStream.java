package de.vectordata.libjvsl.fileTransfer.streams;

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
    public int read() {
        throw new UnsupportedOperationException("Reference implementation does not provide these methods");
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public abstract int read(byte[] b, int off, int len) throws IOException;

}
