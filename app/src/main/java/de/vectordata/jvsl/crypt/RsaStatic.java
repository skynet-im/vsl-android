package de.vectordata.jvsl.crypt;

import android.util.Log;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import de.vectordata.jvsl.util.Util;

/**
 * Created by Daniel Lerch on 07.03.2018.
 * © 2018 Daniel Lerch
 */

public class RsaStatic {

    private static final String TAG = "RsaStatic";

    /**
     * Encrypts one block using RSA with OAEP.
     *
     * @param plaintext Data to encrypt (max. 214 bytes)
     * @param key
     * @return
     */
    public static byte[] encryptBlock(byte[] plaintext, String key) {
        RsaParams params = RsaParams.fromXml(key, false);
        try {
            BigInteger modulus = new BigInteger(1, params.modulus);
            BigInteger exponent = new BigInteger(1, params.exponent);

            KeyFactory factory = KeyFactory.getInstance("RSA");
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");

            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
            PublicKey publicKey = factory.generatePublic(keySpec);

            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(plaintext);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException | InvalidKeyException e) {
            Log.e(TAG, "Failed to encrypt", e);
        }
        return null;
    }

    /**
     * Encrypts data using RSA with OAEP.
     *
     * @param plaintext Data to encrypt with any length.
     * @param key
     * @return
     */
    public static byte[] encrypt(byte[] plaintext, String key) {
        if (plaintext == null) throw new IllegalArgumentException("Plaintext must not be null");
        if (key == null) throw new IllegalArgumentException("Key must not be null");
        int blocks = Util.getTotalSize(plaintext.length, 214) / 214;
        byte[] ciphertext = new byte[blocks * 256];
        for (int i = 0; i < blocks - 1; i++) {
            byte[] buf = encryptBlock(Util.takeBytes(plaintext, 214, 214 * i), key);
            System.arraycopy(buf, 0, ciphertext, i * 256, 256);
        }
        int llen = plaintext.length % 214;
        byte[] lbuf = encryptBlock(Util.takeBytes(plaintext, llen != 0 ? llen : 214, (blocks - 1) * 214), key);
        System.arraycopy(lbuf, 0, ciphertext, (blocks - 1) * 256, 256);
        return ciphertext;
    }

    // TODO Comments
    public static byte[] decryptBlock(byte[] ciphertext, String key) {
        RsaParams params = RsaParams.fromXml(key, true);
        try {
            BigInteger modulus = new BigInteger(1, params.modulus);
            BigInteger d = new BigInteger(1, params.d);

            KeyFactory factory = KeyFactory.getInstance("RSA");
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");

            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(modulus, d);
            PrivateKey privateKey = factory.generatePrivate(keySpec);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            return cipher.doFinal(ciphertext);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException | InvalidKeyException e) {
            Log.e(TAG, "Failed to decrypt", e);
        }
        return null;
    }

    // TODO Comments
    public static byte[] decrypt(byte[] ciphertext, String key) {
        if (ciphertext == null) throw new IllegalArgumentException("Plaintext must not be null");
        if (key == null) throw new IllegalArgumentException("Key must not be null");
        if (ciphertext.length % 256 != 0)
            throw new IllegalArgumentException("The blocksize must be 256 bytes");
        int blocks = ciphertext.length / 256;
        byte[] tmp_plaintext = new byte[(blocks - 1) * 214];

        for (int i = 0; i < blocks - 1; i++) {
            byte[] buf = decryptBlock(Util.takeBytes(ciphertext, 256, i * 256), key);
            System.arraycopy(buf, 0, tmp_plaintext, i * 214, buf.length);
        }
        byte[] lbuf = decryptBlock(Util.takeBytes(ciphertext, 256, (blocks - 1) * 256), key);
        byte[] plaintext = new byte[(blocks - 1) * 214 + lbuf.length];
        System.arraycopy(tmp_plaintext, 0, plaintext, 0, (blocks - 1) * 214);
        System.arraycopy(lbuf, 0, plaintext, (blocks - 1) * 214, lbuf.length);
        return plaintext;
    }
}
