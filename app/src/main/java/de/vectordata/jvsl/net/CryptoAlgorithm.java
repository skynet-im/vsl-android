package de.vectordata.jvsl.net;

/**
 * Created by Daniel Lerch on 07.03.2018.
 * © 2018 Daniel Lerch
 */

public enum CryptoAlgorithm {
    /**
     * Plaintext.
     */
    NONE,
    /**
     * RSA-2048 with Optimal Asymmetric Encryption Padding.
     */
    RSA_2048_OAEP,
    /**
     * [Insecure] AES-256 CBC with split packets.
     */
    AES_256_CBC_SP,
    /**
     * AES-256 CBC with HMAC-SHA256, multipacket mode and 3byte length marker.
     */
    AES_256_CBC_HMAC_SHA256_MP3
}
