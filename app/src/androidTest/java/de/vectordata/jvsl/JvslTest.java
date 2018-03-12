package de.vectordata.jvsl;

import android.util.Log;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import de.vectordata.jvsl.crypt.AesStatic;
import de.vectordata.jvsl.crypt.ContentAlgorithm;
import de.vectordata.jvsl.crypt.Hash;
import de.vectordata.jvsl.fileTransfer.FTEventArgs;
import de.vectordata.jvsl.fileTransfer.FTEventArgsListener;
import de.vectordata.jvsl.fileTransfer.FTProgressEventArgs;
import de.vectordata.jvsl.fileTransfer.FTSocketListener;
import de.vectordata.jvsl.fileTransfer.FileMeta;
import de.vectordata.jvsl.fileTransfer.Identifier;
import de.vectordata.jvsl.util.Util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Twometer on 11.03.2018.
 * (c) 2018 Twometer
 */

public class JvslTest {

    private static final String TAG = "JvslTest";

    @Test
    public void testConnection() throws IOException {
        final VSLClient client = new VSLClient(0, 0);
        assertTrue(client.connect("192.168.2.102", 32761, "<RSAKeyValue><Modulus>qBQQScN/+A2Tfi971dmOyPwSXpoq3XVwQBJNzbCCk1ohGVoOzdNK87Csw3thZyrynfaDzujW555S4HkWXxLR5dzo8rj/6KAk0yugYtFMt10XC1iZHRQACQIB3j+lS5wK9ZHfbsE4+/CUAoUdhYa9cad/xEbYrgkkyY0TuZZ1w2piiE1SdOXB+U6NF1aJbkUtKrHU2zcp5YzhYlRePvx7e+GQ5GMctSuT/xFzPpBZ5DZx1I/7lQicq7V21M/ktilRQIeqIslX98j4jLuFriinySwW+oi0s+8hantRwZ9jgAIIEao9+tbDSj8ePHb0Li6hhuoMmLeImLaoadDG39VnFQ==</Modulus><Exponent>AQAB</Exponent></RSAKeyValue>"));

        client.setListener(new VSLClientListener() {
            @Override
            public void onConnectionEstablished() {
                Log.i(TAG, "Connection established");
                client.sendPacket((byte) 5, new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0});
                try {
                    testTransfer(client);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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


        while (true) {}

        /*
         * FTEventArgs args = new FTEventArgs(new Identifier(0), new FileMeta(path, algorithm, hmacKey, aesKey, null), path);
         args.Progress += VslClient_FTProgress;
         args.Finished += VslClient_FTFinished;
         vslClient.FileTransfer.Upload(args);
         btnReceiveFile.Enabled = false;
         btnSendFile.Enabled = false;
         */

    }

    private void testTransfer(VSLClient client) throws IOException {
        String file = "/storage/emulated/0/Download/260663711.webp";


        ContentAlgorithm algo = ContentAlgorithm.AES_256_CBC_HMAC_SHA_256;


        FTEventArgs args = new FTEventArgs(new Identifier(0), new FileMeta(file, algo, AesStatic.generateKey(), AesStatic.generateKey(), null), file);
        args.setListener(new FTEventArgsListener() {
            @Override
            public void onCanceled(Object sender) {
                Log.i(TAG, "File transfer cancelled");
            }

            @Override
            public void onFinished(Object sender) {
                Log.i(TAG, "File transfer completed");
            }

            @Override
            public void onProgress(Object sender, FTProgressEventArgs e) {
                Log.i(TAG, "File transfer " + e.getPercentage());
            }

            @Override
            public void onFileMetaReceived(Object sender) {
                Log.i(TAG, "File transfer got meta");
            }
        });
        client.getFileTransfer().upload(args);
    }
}
