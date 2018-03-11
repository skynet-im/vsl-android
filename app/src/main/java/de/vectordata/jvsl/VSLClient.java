package de.vectordata.jvsl;

import de.vectordata.jvsl.fileTransfer.FTSocket;
import de.vectordata.jvsl.net.CryptoAlgorithm;
import de.vectordata.jvsl.net.NetworkChannel;
import de.vectordata.jvsl.net.NetworkManager;
import de.vectordata.jvsl.net.PacketHandler;
import de.vectordata.jvsl.net.packet.P00Handshake;
import de.vectordata.jvsl.net.packet.P01KeyExchange;
import de.vectordata.jvsl.net.packet.util.RequestType;
import de.vectordata.jvsl.util.Constants;

/**
 * Created by Daniel Lerch on 07.03.2018.
 * © 2018 Daniel Lerch
 */

public class VSLClient {

    private NetworkChannel channel;
    private NetworkManager manager;
    private PacketHandler handler;
    private FTSocket fileTransfer;
    private boolean connectionAvailabe;
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

        manager.sendPacket(CryptoAlgorithm.NONE, new P00Handshake(RequestType.DirectPublicKey));
        manager.generateKeys();
        manager.sendPacket(CryptoAlgorithm.RSA_2048_OAEP, new P01KeyExchange(manager.getAesKey(), manager.getHmacKey(),
                Constants.VERSION_NUMBER, Constants.COMPATIBILITY_VERSION, latestProduct, oldestProduct));
        return true;
    }

    public void onConnectionEstablished(int connectionVersion) {
        this.connectionVersion = connectionVersion;
        connectionAvailabe = true;
        if (listener != null) listener.onConnectionEstablished();
    }

    public void onPacketReceived(byte id, byte[] content) {
        if (listener != null) listener.onPacketReceived(invertId(id), content);
    }

    public void sendPacket(byte id, byte[] content) {
        if (!connectionAvailabe)
            throw new IllegalStateException("You cannot send a packet without a secure connection");
        manager.sendPacket(invertId(id), content);
    }

    public void closeConnection(String reason) {
        if (listener != null) listener.onConnectionClosed(reason);
    }

    private byte invertId(byte id) {
        int ubyte = id >= 0 ? id : 127 - id; // remove sign / example: 127--87 = 214
        int value = 255 - ubyte; // get internal/external id / example: 255 - 214 = 41
        return (byte) (value < 128 ? value : 127 - id); // restore sign / example: 127 - 214 = -87
    }
}
