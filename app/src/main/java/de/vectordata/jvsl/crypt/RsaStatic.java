package de.vectordata.jvsl.crypt;

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
        throw new UnsupportedOperationException();
    }

    /**
     * Encrypts data using RSA with OAEP.
     * @param plaintext Data to encrypt with any length.
     * @param key
     * @return
     */
    public static byte[] encrypt(byte[] plaintext, String key){
        throw new UnsupportedOperationException();
    }
}
