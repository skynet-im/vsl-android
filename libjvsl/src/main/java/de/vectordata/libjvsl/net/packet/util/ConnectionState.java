package de.vectordata.libjvsl.net.packet.util;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public enum ConnectionState {
    /**
     * Connection with protocol version 1.1. This entry replaces Compatible at 0 from VSL 1.1.
     */
    CompatibilityMode,
    /**
     * The server cannot handle the requested version. The client is redirected to another server specified in P03FinishHandshake.
     */
    Redirect,
    /**
     * The connection was refused by the server because of incompatible versions.
     */
    NotCompatible,
    /**
     * Connection with protocol version 1.2 or higher. The specific version is specified in P03FinishHandshake.
     */
    Compatible
}
