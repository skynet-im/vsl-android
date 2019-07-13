package de.vectordata.libjvsl.crypt;

import android.util.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Twometer on 09.03.2018.
 * (c) 2018 Twometer
 */

public class HmacStatic {

    private static final String TAG = "HmacStatic";

    public static byte[] computeHmacSHA256(byte[] input, byte[] key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA256");
            mac.init(keySpec);
            return mac.doFinal(input);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Log.e(TAG, "Failed to compute hmac", e);
        }
        return null;
    }

}
