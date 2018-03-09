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
}
