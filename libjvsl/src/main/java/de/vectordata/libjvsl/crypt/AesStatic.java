package de.vectordata.libjvsl.crypt;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import de.vectordata.libjvsl.util.PacketBuffer;
import de.vectordata.libjvsl.util.Util;

/**
 * Created by Daniel Lerch on 07.03.2018.
 * © 2018 Daniel Lerch
 */

public class AesStatic {

    /**
     * Executes an AES encryption.
     *
     * @param buffer Plaintext.
     * @param key    AES key (128 or 256 bit).
     * @param iv     Initialization vector (128 bit).
     * @return
     */
    public static byte[] encrypt(byte[] buffer, byte[] key, byte[] iv) {
        if (key == null)
            throw new IllegalArgumentException("key must not be null");
        if (key.length < 32)
            throw new IllegalArgumentException("Key has to be 256 bit (was " + key.length + ")");

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(buffer);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Executes an AES decryption.
     *
     * @param buffer Ciphertext.
     * @param key    AES key (128 or 256 bit).
     * @param iv     Initialization vector (128 bit).
     * @return
     */
    public static byte[] decrypt(byte[] buffer, byte[] key, byte[] iv) {
        if (key.length < 32)
            throw new IllegalArgumentException("Key has to be 256 bit (was " + key.length + ")");

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(buffer);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Generates a new 256 bit AES key
     *
     * @return
     */
    public static byte[] generateKey() {
        return generateRandom(32);
    }

    /**
     * Generates a new 128 bit initialization vector.
     *
     * @return
     */
    public static byte[] generateIV() {
        return generateRandom(16);
    }

    private static byte[] generateRandom(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    public static byte[] incrementIv(byte[] iv) {
        PacketBuffer bufferIn = new PacketBuffer(iv);
        PacketBuffer bufferOut = new PacketBuffer();
        bufferOut.writeInt64(bufferIn.readInt64() + 1);
        bufferOut.writeInt64(bufferIn.readInt64());
        return bufferOut.toArray();
    }

    public static byte[] decryptWithHmac(PacketBuffer input, int length, byte[] hmacKey, byte[] aesKey) {
        if (length == 0)
            length = (int) input.readUInt32();
        byte[] hmac = input.readByteArray(32);
        byte[] iv = input.readByteArray(16);
        byte[] ciphertext = input.readByteArray(length - 48);
        if (!Util.sequenceEqual(hmac, HmacStatic.computeHmacSHA256(Util.concatBytes(iv, ciphertext), hmacKey)))
            throw new RuntimeException("Data corrupted");
        return decrypt(ciphertext, aesKey, iv);
    }

    public static void encryptWithHmac(byte[] input, PacketBuffer output, boolean writeLength, byte[] hmacKey, byte[] aesKey) {
        byte[] iv = AesStatic.generateIV();
        byte[] ciphertext = AesStatic.encrypt(input, aesKey, iv);
        if (writeLength)
            output.writeUInt32(32 + 16 + Util.getTotalSize(input.length + 1, 16));
        output.writeByteArray(HmacStatic.computeHmacSHA256(Util.concatBytes(iv, ciphertext), hmacKey), false);
        output.writeByteArray(iv, false);
        output.writeByteArray(ciphertext, false);
    }
}
