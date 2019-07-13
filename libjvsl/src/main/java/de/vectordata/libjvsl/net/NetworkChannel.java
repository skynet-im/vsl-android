package de.vectordata.libjvsl.net;


import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.vectordata.libjvsl.VSLClient;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public class NetworkChannel {

    private static final String TAG = "NetworkChannel";

    private VSLClient parent;

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private boolean shouldExit = false;
    private final ConcurrentLinkedQueue<SendItem> sendCache = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<SendItem> sendCacheBackground = new ConcurrentLinkedQueue<>();

    private final Object sendingWaitHandle = new Object();

    public NetworkChannel(VSLClient parent) {
        this.parent = parent;
    }

    public boolean connect(String host, int port) {
        try {
            socket = new Socket();
            socket.setKeepAlive(true);
            socket.setSoLinger(true, 0);
            socket.setReuseAddress(false);
            socket.setReceiveBufferSize(65536);
            socket.connect(new InetSocketAddress(host, port), 10000);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to connect to the server", e);
            return false;
        }
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startThreads() {
        startSenderThread();
        startReceiverThread();
    }

    private void startSenderThread() {
        (new Thread(() -> {
            while (!shouldExit) {
                try {
                    synchronized (sendingWaitHandle) {
                        boolean priorityCacheEmpty;
                        while ((priorityCacheEmpty = sendCache.isEmpty()) && sendCacheBackground.isEmpty())
                            sendingWaitHandle.wait();
                        SendItem item = priorityCacheEmpty ? sendCacheBackground.poll() : sendCache.poll();
                        if (item != null) {
                            outputStream.write(item.getData());
                            item.notifySend();
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    shouldExit = true;
                    Log.e(TAG, "Failed to send packet", e);
                }
            }
            handleDisconnect();
        })).start();
    }

    private void startReceiverThread() {
        (new Thread(() -> {
            while (!shouldExit) {
                try {
                    if (inputStream.available() == -1)
                        throw new IOException("Connection closed: No more data");
                    parent.getManager().receiveData();
                } catch (IOException e) {
                    shouldExit = true;
                    Log.e(TAG, "Failed to receive", e);
                }
            }
            handleDisconnect();
        })).start();
    }

    private void handleDisconnect() {
        sendCache.clear();
        this.close();
        parent.closeConnection("Socket disconnected");
    }

    SendItem sendAsync(byte[] array) {
        SendItem item = new SendItem(array);
        sendCache.add(item);
        wakeUp();
        return item;
    }

    SendItem sendAsyncBackground(byte[] array) {
        SendItem item = new SendItem(array);
        sendCacheBackground.add(item);
        wakeUp();
        return item;
    }

    byte receiveByte() {
        byte[] b = receive(1);
        if (b == null)
            return -1;
        return b[0];
    }

    byte[] receive(int len) {
        try {
            byte[] bytes = new byte[len];

            int total = 0;

            while (total < len) {
                int received = inputStream.read(bytes, total, len - total);
                if (received == -1) throw new IOException("Connection closed: No more data");
                total += received;
            }

            if (total != len)
                throw new IOException("Buffer length and received data length mismatch.");
            return bytes;

        } catch (IOException e) {
            shouldExit = true;
            Log.e(TAG, "Failed to receive", e);
        }
        return null;
    }

    private void wakeUp() {
        synchronized (sendingWaitHandle) {
            sendingWaitHandle.notifyAll();
        }
    }

    private void close() {
        try {
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
