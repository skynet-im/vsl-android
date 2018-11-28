package de.vectordata.libjvsl;

/**
 * Created by Daniel Lerch on 11.03.2018.
 * © 2018 Daniel Lerch
 */

public interface VSLClientListener {

    void onConnectionEstablished();

    void onPacketReceived(byte id, byte[] content);

    void onConnectionClosed(String reason);
}
