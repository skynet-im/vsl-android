package de.vectordata.jvsl.fileTransfer.streams;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import de.vectordata.jvsl.crypt.AesStatic;

import static de.vectordata.jvsl.fileTransfer.streams.Util.initAesCipher;

/**
 * Created by Twometer on 10.03.2018.
 * (c) 2018 Twometer
 */

public class AesShaInputStream extends HashInputStream {

    private InputStream stream;

    private int position;

    private boolean first = true;

    private byte[] key;
    private byte[] iv;
    private CryptographicOperation cryptographicOperation;

    private ShaInputStream shaStream;
    private InputStream topStream;

    public AesShaInputStream(InputStream inputStream, byte[] key, CryptographicOperation operation) {
        this.stream = inputStream;
        if (operation == CryptographicOperation.Encrypt)
            iv = AesStatic.generateIV();
        else if (operation != CryptographicOperation.Decrypt)
            throw new IllegalArgumentException("The AesShaInputStream only supports Encrypt and Decrypt CryptoOperation");

        this.key = key;
        this.cryptographicOperation = operation;
    }

    @Override
    public int read(@NonNull byte[] buffer, int offset, int count) throws IOException {
        int done = 0;
        CipherInputStream aesStream;
        if (cryptographicOperation == CryptographicOperation.Encrypt) {
            if (first) {
                System.arraycopy(iv, 0, buffer, offset, 16);
                done += 16;
                offset += 16;
                count -= 16;
                shaStream = new ShaInputStream(stream);
                aesStream = new CipherInputStream(shaStream, initAesCipher(Cipher.ENCRYPT_MODE, key, iv));
                topStream = aesStream;
                first = false;
            }
        } else {
            if (first) {
                iv = new byte[16];
                if (stream.read(iv, 0, 16) < 16) return -1;
                aesStream = new CipherInputStream(stream, initAesCipher(Cipher.DECRYPT_MODE, key, iv));
                shaStream = new ShaInputStream(aesStream);
                topStream = shaStream;
                first = false;
            }
        }
        done += readFromStreamUntilItDies(buffer, offset, count);
        position += done;
        return done;
    }

    private int readFromStreamUntilItDies(byte[] buffer, int offset, int count) throws IOException {
        int done = 0;
        while (done < count - offset) {
            int d1 = topStream.read(buffer, offset + done, count);
            if (d1 < 0) return done;
            done += d1;
        }
        return done;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public byte[] getHash() {
        return shaStream.getHash();
    }
}
