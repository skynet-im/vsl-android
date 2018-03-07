package de.vectordata.jvsl.crypt;

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

/**
 * Created by Daniel Lerch on 07.03.2018.
 * © 2018 Daniel Lerch
 */

public class RsaStatic {
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
            e.printStackTrace();
        }
        return null;
    }

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
            e.printStackTrace();
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
        throw new UnsupportedOperationException();
    }
}
