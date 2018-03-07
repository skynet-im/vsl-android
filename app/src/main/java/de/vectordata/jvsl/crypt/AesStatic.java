package de.vectordata.jvsl.crypt;

/**
 * Created by Daniel Lerch on 07.03.2018.
 * © 2018 Daniel Lerch
 */

public class AesStatic {
    /**
     * Executes an AES encryption.
     * @param buffer Plaintext.
     * @param key AES key (128 or 256 bit).
     * @param iv Initialization vector (128 bit).
     * @return
     */
    public static byte[] encrypt(byte[] buffer, byte[] key, byte[] iv) {
        throw new UnsupportedOperationException();
    }

    /**
     * Executes an AES decryption.
     * @param buffer Ciphertext.
     * @param key AES key (128 or 256 bit).
     * @param iv Initialization vector (128 bit).
     * @return
     */
    public static byte[] decrypt(byte[] buffer, byte[] key, byte[] iv){
        throw new UnsupportedOperationException();
    }

    /**
     * Generates a new 256 bit AES key
     * @return
     */
    public static byte[] generateKey(){
        return generateRandom(32);
    }

    /**
     * Generates a new 128 bit initialization vector.
     * @return
     */
    public static byte[] generateIV(){
        return generateRandom(16);
    }

    private static byte[] generateRandom(int length){
        throw new UnsupportedOperationException();
    }
}
