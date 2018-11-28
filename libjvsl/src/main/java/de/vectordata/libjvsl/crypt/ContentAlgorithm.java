package de.vectordata.libjvsl.crypt;

/**
 * Created by Daniel Lerch on 07.03.2018.
 * © 2018 Daniel Lerch
 */

public enum ContentAlgorithm {
    /**
     * No encryption, content will be handled as plaintext.
     */
    NONE,

    /**
     * Encryption using AES 256 with Cipher Block Chaining.
     */
    AES_256_CBC,

    /**
     * Encryption using AES 256 with Cipher Block Chaining and verifying with HMAC SHA256.
     */
    AES_256_CBC_HMAC_SHA_256
}
