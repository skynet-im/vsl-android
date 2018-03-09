package de.vectordata.jvsl;

import de.vectordata.jvsl.net.NetworkChannel;
import de.vectordata.jvsl.net.NetworkManager;
import de.vectordata.jvsl.net.PacketHandler;

/**
 * Created by Daniel Lerch on 07.03.2018.
 * © 2018 Daniel Lerch
 */

public class VSLClient {

    private NetworkChannel channel;
    private NetworkManager manager;
    private PacketHandler handler;

    public VSLClient(int latestProduct, int oldestProduct) {

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

    public void Connect(String host, int port, String serverKey) {
        // TODO Connect TCP client
        channel = new NetworkChannel();
        manager = new NetworkManager(this, serverKey);
        handler = new PacketHandler();
        // TODO Start threads
        // TODO Send handshake packet
        // TODO Generate session keys
        // TODO Send key exchange packet
    }
}
