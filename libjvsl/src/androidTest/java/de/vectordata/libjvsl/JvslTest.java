package de.vectordata.libjvsl;

import android.util.Log;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.vectordata.libjvsl.crypt.AesStatic;
import de.vectordata.libjvsl.crypt.ContentAlgorithm;
import de.vectordata.libjvsl.fileTransfer.FTEventArgs;
import de.vectordata.libjvsl.fileTransfer.FTEventArgsListener;
import de.vectordata.libjvsl.fileTransfer.FTProgressEventArgs;
import de.vectordata.libjvsl.fileTransfer.FileMeta;
import de.vectordata.libjvsl.fileTransfer.Identifier;
import de.vectordata.libjvsl.util.Util;
import de.vectordata.libjvsl.util.cscompat.Ref;

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

        final Ref<Boolean> finished = new Ref<>(false);

        client.setListener(new VSLClientListener() {
            @Override
            public void onConnectionEstablished() {
                Log.i(TAG, "Connection established");
                client.sendPacket((byte) 5, new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0});
                testTransferDown(client, finished);
                /*try {
                    testTransferUp(client);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
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


        //noinspection StatementWithEmptyBody
        while (!finished.get()) {
        }

        /*
         * FTEventArgs args = new FTEventArgs(new Identifier(0), new FileMeta(path, algorithm, hmacKey, aesKey, null), path);
         args.Progress += VslClient_FTProgress;
         args.Finished += VslClient_FTFinished;
         vslClient.FileTransfer.Upload(args);
         btnReceiveFile.Enabled = false;
         btnSendFile.Enabled = false;
         */

    }

    private void testTransferDown(final VSLClient client, final Ref<Boolean> booleanRef) {
        String file = "/storage/emulated/0/Download/" + Math.floor(Math.random() * 1000) + ".jvsldlf";
        final FTEventArgs args = new FTEventArgs(new Identifier(0), null, file);
        args.setListener(new FTEventArgsListener() {
            @Override
            public void onCanceled(Object sender) {
                Log.i(TAG, "Download cancelled");
            }

            @Override
            public void onFinished(Object sender) {
                Log.i(TAG, "Download completed");
                booleanRef.set(true);
            }

            @Override
            public void onProgress(Object sender, FTProgressEventArgs e) {
                Log.i(TAG, "Download process " + e.getPercentage());
            }

            @Override
            public void onFileMetaReceived(Object sender) {
                FTEventArgs args1 = (FTEventArgs) sender;
                byte[] keys = hexStringToByteArray("efbde67e7540f964c45ee28cd9043603a7b5ab75d9033f6aebf535b0595869fb74a4061e52f6e59ce845d2f3ff4598123dfa91e5608c98f64a2c07a5e63b0b20");
                if (args1.getFileMeta().getAlgorithm() == ContentAlgorithm.AES_256_CBC_HMAC_SHA_256) {
                    args1.getFileMeta().decrypt(Util.takeBytes(keys, 32, 0), Util.takeBytes(keys, 32, 0));
                }
                try {
                    client.getFileTransfer().doContinue(args1);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        client.getFileTransfer().download(args);
    }

    private static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    private void testTransferUp(VSLClient client) throws IOException {
        String file = "/storage/emulated/0/Download/900.0.jvsldlf";


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
