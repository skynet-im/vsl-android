package de.vectordata.libjvsl.fileTransfer.streams;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import de.vectordata.libjvsl.crypt.AesStatic;

import static de.vectordata.libjvsl.fileTransfer.streams.Util.initAesCipher;

/**
 * Created by Twometer on 10.03.2018.
 * (c) 2018 Twometer
 */

public class AesShaOutputStream extends HashOutputStream {

    private OutputStream stream;

    private int position;

    private boolean first;

    private byte[] key;
    private byte[] iv;
    private CryptographicOperation cryptographicOperation;

    private ShaOutputStream shaStream;
    private OutputStream topStream;

    public AesShaOutputStream(OutputStream outputStream, byte[] key, CryptographicOperation operation) {
        this.stream = outputStream;
        if (operation == CryptographicOperation.Encrypt)
            iv = AesStatic.generateIV();
        else if (operation != CryptographicOperation.Decrypt)
            throw new IllegalArgumentException("The AesShaInputStream only supports Encrypt and Decrypt CryptoOperation");

        this.key = key;
        this.cryptographicOperation = operation;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public byte[] getHash() {
        return shaStream.getHash();
    }

    @Override
    public void write(@NonNull byte[] b, int off, int len) throws IOException {
        int done = 0;
        CipherOutputStream aesStream;
        if (cryptographicOperation == CryptographicOperation.Encrypt) {
            if (first) {
                stream.write(iv, 0, 16);
                aesStream = new CipherOutputStream(stream, initAesCipher(Cipher.ENCRYPT_MODE, key, iv));
                shaStream = new ShaOutputStream(aesStream);
                topStream = shaStream;
                first = false;
            }
        } else {
            if (first) {
                if (len < 16) throw new IllegalArgumentException();
                iv = new byte[16];
                System.arraycopy(b, off, iv, 0, 16);
                off += 16;
                len -= 16;
                done += 16;
                shaStream = new ShaOutputStream(stream);
                aesStream = new CipherOutputStream(shaStream, initAesCipher(Cipher.DECRYPT_MODE, key, iv));
                topStream = aesStream;
                first = false;
            }
        }
        topStream.write(b, off, len);
        position += done + len;
    }

    @Override
    public void flush() throws IOException {
        topStream.flush();
    }
}
