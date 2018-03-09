package de.vectordata.jvsl.crypt;

import android.util.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Twometer on 09.03.2018.
 * (c) 2018 Twometer
 */

public class Hmac {

    private static final String TAG = "Hmac";

    public static byte[] computeHmacSHA256(byte[] input, byte[] key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA256");
            mac.init(keySpec);
            return mac.doFinal(input);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Log.e(TAG, "Failed to compute Hmac", e);
        }
        return null;
    }

}
