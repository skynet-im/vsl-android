package de.vectordata.libjvsl.crypt;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.vectordata.libjvsl.fileTransfer.streams.ShaInputStream;

/**
 * Created by Twometer on 10.03.2018.
 * (c) 2018 Twometer
 */

public class Hash {

    private static final String TAG = "Hash";

    public static byte[] sha256(String s) {
        return sha256(s.getBytes(Charset.forName("UTF-8")));
    }

    private static byte[] sha256(byte[] buf) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(buf);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Failed to compute SHA-256 hash", e);
            return null;
        }
    }

    public static byte[] sha256(File file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            ShaInputStream stream = new ShaInputStream(inputStream);
            byte[] buf = new byte[8192];
            while (true) {
                int i = stream.read(buf, 0, buf.length);
                if (i < 0) break;
            }
            return stream.getHash();
        } catch (IOException e) {
            Log.e(TAG, "Failed to compute SHA-256 hash of file", e);
            return null;
        }
    }

}
