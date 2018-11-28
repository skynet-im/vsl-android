package de.vectordata.libjvsl;

import de.vectordata.libjvsl.fileTransfer.FTSocket;
import de.vectordata.libjvsl.net.CryptoAlgorithm;
import de.vectordata.libjvsl.net.NetworkChannel;
import de.vectordata.libjvsl.net.NetworkManager;
import de.vectordata.libjvsl.net.PacketHandler;
import de.vectordata.libjvsl.net.Priority;
import de.vectordata.libjvsl.net.packet.P00Handshake;
import de.vectordata.libjvsl.net.packet.P01KeyExchange;
import de.vectordata.libjvsl.net.packet.util.RequestType;
import de.vectordata.libjvsl.util.Constants;

/**
 * Created by Daniel Lerch on 07.03.2018.
 * © 2018 Daniel Lerch
 */

public class VSLClient {

    private NetworkChannel channel;
    private NetworkManager manager;
    private PacketHandler handler;
    private FTSocket fileTransfer;
    private boolean connectionAvailable;
    private int connectionVersion;
    private int latestProduct;
    private int oldestProduct;
    private VSLClientListener listener;

    public VSLClient(int latestProduct, int oldestProduct) {
        this.latestProduct = latestProduct;
        this.oldestProduct = oldestProduct;
    }

    public NetworkChannel getChannel() {
        return channel;
    }

    public NetworkManager getManager() {
        return manager;
    }

    public PacketHandler getHandler() {
        return handler;
    }

    public FTSocket getFileTransfer() {
        return fileTransfer;
    }

    public int getConnectionVersion() {
        return connectionVersion;
    }

    public void setListener(VSLClientListener listener) {
        this.listener = listener;
    }

    public boolean connect(String host, int port, String serverKey) {
        if (host == null) throw new IllegalArgumentException("host must not be null");
        if (port < 0 || port > 65535)
            throw new IllegalArgumentException("port must be in a range from 0 to 65535");
        if (serverKey == null) throw new IllegalArgumentException("serverKey must not be null");

        channel = new NetworkChannel(this);
        if (!channel.connect(host, port)) return false;
        manager = new NetworkManager(this, serverKey);
        handler = new PacketHandler(this);
        fileTransfer = new FTSocket(this);
        channel.startThreads();

        manager.sendPacket(CryptoAlgorithm.NONE, new P00Handshake(RequestType.DirectPublicKey), Priority.Realtime);
        manager.generateKeys();
        manager.sendPacket(CryptoAlgorithm.RSA_2048_OAEP, new P01KeyExchange(manager.getAesKey(), manager.getHmacKey(),
                Constants.VERSION_NUMBER, Constants.COMPATIBILITY_VERSION, latestProduct, oldestProduct), Priority.Realtime);
        return true;
    }

    public void onConnectionEstablished(int connectionVersion) {
        this.connectionVersion = connectionVersion;
        connectionAvailable = true;
        if (listener != null) listener.onConnectionEstablished();
    }

    public void onPacketReceived(byte id, byte[] content) {
        if (listener != null) listener.onPacketReceived(invertId(id), content);
    }

    public void sendPacket(byte id, byte[] content) {
        if (!connectionAvailable)
            throw new IllegalStateException("You cannot send a packet without a secure connection");
        manager.sendPacket(invertId(id), content, Priority.Realtime);
    }

    public void closeConnection(String reason) {
        if (listener != null) listener.onConnectionClosed(reason);
    }

    private byte invertId(byte id) {
        return (byte) (255 - id);
    }
}
