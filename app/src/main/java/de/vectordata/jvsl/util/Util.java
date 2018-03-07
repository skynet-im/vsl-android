package de.vectordata.jvsl.util;

/**
 * Created by Daniel Lerch on 07.03.2018.
 * © 2018 Daniel Lerch
 */

public class Util {

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * Splits a byte array into blocks.
     *
     * @param b
     * @param blocksize
     * @return
     */
    public static byte[][] splitBytes(byte[] b, int blocksize) {
        int length = b.length;
        int blocks = getTotalSize(length, blocksize) / blocksize;
        byte[][] value = new byte[blocks][];
        if (length == 0) {
            value[0] = new byte[0];
            return value;
        }
        int i;
        for (i = 0; i < blocks - 1; i++) {
            value[i] = new byte[blocksize];
            System.arraycopy(b, i * blocksize, value[i], 0, blocksize);
        }
        int pending = length - i * blocksize;
        value[blocks - 1] = new byte[pending];
        System.arraycopy(b, i * blocksize, value[blocks - 1], 0, pending);
        return value;
    }

    /**
     * Concatenates multiple byte arrays to one.
     *
     * @param byteArrays An array of byte arrays to concatenate.
     * @return
     */
    public static byte[] concatBytes(byte[]... byteArrays) {
        int n = 0;
        for (byte[] byteArray : byteArrays) {
            n += byteArray.length;
        }
        byte[] concatenated = new byte[n];
        n = 0;
        for (byte[] b : byteArrays) {
            System.arraycopy(b, 0, concatenated, n, b.length);
            n += b.length;
        }
        return concatenated;
    }

    public static byte[] skipBytes(byte[] array, int amount) {
        byte[] result = new byte[array.length - amount];
        System.arraycopy(array, amount, result, 0, result.length);
        return result;
    }

    public static byte[] takeBytes(byte[] array, int amount, int index) {
        byte[] result = new byte[amount];
        System.arraycopy(array, index, result, 0, amount);
        return result;
    }

    /**
     * Converts a byte array to a hexadecimal string.
     *
     * @param buffer
     * @return
     */
    public static String toHexString(byte[] buffer) {
        char[] hexChars = new char[buffer.length * 2];
        for (int j = 0; j < buffer.length; j++) {
            int v = buffer[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Gets the total size if only full blocks are allowed.
     *
     * @param normalSize The default size of the input data.
     * @param blockSize  The blocksize of the algorithm to apply on the data.
     * @return
     */
    public static int getTotalSize(int normalSize, int blockSize) {
        int mod = normalSize % blockSize;
        if (mod > 0)
            return normalSize - mod + blockSize;
        else
            return normalSize;
    }

    /**
     * Gets the total size if only full blocks are allowed.
     *
     * @param normalSize The default size of the input data.
     * @param blockSize  The blocksize of the algorithm to apply on the data.
     * @return
     */
    public static long getTotalSize(long normalSize, int blockSize) {
        long mod = normalSize % blockSize;
        if (mod > 0)
            return normalSize - mod + blockSize;
        else
            return normalSize;
    }

    static byte[] reverseBytes(byte[] b) {
        byte[] reversed = new byte[b.length];
        System.arraycopy(b, 1, reversed, 1, b.length - 1);
        return reversed;
    }
}
