package de.vectordata.jvsl;

import android.util.Log;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by Twometer on 11.03.2018.
 * (c) 2018 Twometer
 */

public class JvslTest {

    private static final String TAG = "JvslTest";

    @Test
    public void testConnection() {
        final VSLClient client = new VSLClient(0, 0);
        assertTrue(client.connect("10.2.132.68", 32761, "<RSAKeyValue><Modulus>qBQQScN/+A2Tfi971dmOyPwSXpoq3XVwQBJNzbCCk1ohGVoOzdNK87Csw3thZyrynfaDzujW555S4HkWXxLR5dzo8rj/6KAk0yugYtFMt10XC1iZHRQACQIB3j+lS5wK9ZHfbsE4+/CUAoUdhYa9cad/xEbYrgkkyY0TuZZ1w2piiE1SdOXB+U6NF1aJbkUtKrHU2zcp5YzhYlRePvx7e+GQ5GMctSuT/xFzPpBZ5DZx1I/7lQicq7V21M/ktilRQIeqIslX98j4jLuFriinySwW+oi0s+8hantRwZ9jgAIIEao9+tbDSj8ePHb0Li6hhuoMmLeImLaoadDG39VnFQ==</Modulus><Exponent>AQAB</Exponent></RSAKeyValue>"));

        client.setListener(new VSLClientListener() {
            @Override
            public void onConnectionEstablished() {
                Log.i(TAG, "Connection established");
                client.sendPacket((byte) 5, new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0});
            }

            @Override
            public void onPacketReceived(byte id, byte[] content) {
                Log.i(TAG, "Received packet with id " + id + " and content length " + content.length);
            }

            @Override
            public void onConnectionClosed(String reason) {
                Log.i(TAG, "Connection lost: " + reason);
            }
        });


    }

}
