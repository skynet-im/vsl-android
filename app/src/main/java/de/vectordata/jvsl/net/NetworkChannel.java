package de.vectordata.jvsl.net;


import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.vectordata.skynet.net.jvsl.VSLClient;
import de.vectordata.skynet.net.jvsl.util.CancellableThreadSleeper;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public class NetworkChannel {

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private WakeLockHolder sendingWakeLockHolder;
    private WakeLockHolder receivingWakeLockHolder;

    private NetworkManager networkManager;

    private ChannelMode mode = ChannelMode.Realtime;
    private final ConcurrentLinkedQueue<byte[]> sendCache = new ConcurrentLinkedQueue<>();

    private ShutdownState shutdownState;

    private boolean disconnected;
    private long delay;

    private final CancellableThreadSleeper receivingThreadSleeper = new CancellableThreadSleeper();
    private final Object sendingWaitHandle = new Object();

    public ConnectionResult connect(Context context, String host, int port, VSLClient parent, String publicKey) {
        try {
            sendingWakeLockHolder = new WakeLockHolder(context);
            sendingWakeLockHolder.initialize(WakeLockHolder.Mode.Sending);
            receivingWakeLockHolder = new WakeLockHolder(context);
            receivingWakeLockHolder.initialize(WakeLockHolder.Mode.Receiving);
            socket = new Socket();
            socket.setKeepAlive(true);
            socket.setSoLinger(true, 0);
            socket.setReuseAddress(false);
            socket.setReceiveBufferSize(65536);
            socket.connect(new InetSocketAddress(host, port), 10000);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            networkManager = new NetworkManager(receivingWakeLockHolder, this, parent, publicKey);
            shutdownState = new ShutdownState();
            disconnected = false;
            runThreads();
            return new ConnectionResult();
        } catch (IOException e) {
            return new ConnectionResult(e);
        }
    }

    public boolean isConnected() {
        return !disconnected && socket != null && shutdownState != null && !shutdownState.shouldShutDown() && networkManager != null;
    }

    private void runThreads() {
        runSenderThread();
        runReceiverThread();
    }

    private void runSenderThread() {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                while (!shutdownState.shouldShutDown()) {
                    boolean successfullySent = false;
                    try {
                        synchronized (sendingWaitHandle) {
                            while (sendCache.isEmpty())
                                sendingWaitHandle.wait();
                            byte[] array = sendCache.poll();
                            sendingWakeLockHolder.acquire();
                            outputStream.write(array);
                            successfullySent = true;
                        }
                    } catch (IOException | InterruptedException e) {
                        shutdownState.shutdown(e);
                        successfullySent = false;
                    } finally {
                        if (successfullySent)
                            networkManager.parent.getPacketCallback().onPacketSuccess();
                        sendingWakeLockHolder.release();
                    }
                }
                onConnectionLost();
            }
        })).start();
    }

    private void runReceiverThread() {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                while (!shutdownState.shouldShutDown()) {
                    try {
                        if (inputStream.available() == -1)
                            throw new IOException("Connection lost");
                        networkManager.receiveData();
                        networkManager.parent.getPacketCallback().updateDelay();
                        if (delay > 0)
                            if (mode == ChannelMode.Background)
                                receivingThreadSleeper.sleep(50 + delay);
                            else if (mode == ChannelMode.EnergySaver)
                                receivingThreadSleeper.sleep(150 + delay);
                            else if (mode == ChannelMode.Realtime)
                                Thread.sleep(20);
                    } catch (Exception e) {
                        shutdownState.shutdown(e);
                        break;
                    }
                }
                onConnectionLost();
            }
        })).start();
    }

    private void onConnectionLost() {
        if (!disconnected) {
            disconnected = true;
            sendingWakeLockHolder.release();
            receivingWakeLockHolder.release();
            sendCache.clear();
            close();
            networkManager.parent.getPacketCallback().connectionLost(shutdownState.getCause());
        }
    }

    void sendAsync(byte[] array) {
        sendCache.add(array);
        wakeUp(true);
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
                if (received == -1) throw new IOException("Connection lost");
                total += received;
            }

            if (total != len) {
                throw new IOException("Unrecoverable buffer error");
            }
            return bytes;

        } catch (IOException e) {
            shutdownState.shutdown(e);
        }
        return null;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public ChannelMode getMode() {
        return mode;
    }

    public void setMode(ChannelMode mode) {
        this.mode = mode;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public void wakeUp(boolean sendOnly) {
        if (!sendOnly)
            receivingThreadSleeper.cancel();
        synchronized (sendingWaitHandle) {
            sendingWaitHandle.notifyAll();
        }
    }

    public void close() {
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
