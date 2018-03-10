package de.vectordata.jvsl.fileTransfer.streams;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import de.vectordata.jvsl.crypt.AesStatic;

import static de.vectordata.jvsl.fileTransfer.streams.Util.initAesCipher;

/**
 * Created by Twometer on 10.03.2018.
 * (c) 2018 Twometer
 */

public class AesShaInputStream extends HashInputStream {

    private InputStream stream;

    private int position;

    private boolean first;

    private byte[] key;
    private byte[] iv;
    private CryptographicOperation cryptographicOperation;

    private ShaInputStream shaStream;
    private CipherInputStream aesStream;
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
        if (cryptographicOperation == CryptographicOperation.Encrypt) {
            if (first) {
                System.arraycopy(iv, 0, buffer, offset, 16);
                done += 16;
                offset += 16;
                count -= 16;
                shaStream = new ShaInputStream(stream);
                aesStream = new CipherInputStream(shaStream, initAesCipher(key, iv));
                topStream = aesStream;
                first = false;
            }
        } else {
            if (first) {
                iv = new byte[16];
                if (stream.read(iv, 0, 16) < 16) return -1;
                aesStream = new CipherInputStream(stream, initAesCipher(key, iv));
                shaStream = new ShaInputStream(aesStream);
                topStream = shaStream;
                first = false;
            }
        }
        done += topStream.read(buffer, offset, count);
        position += done;
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
