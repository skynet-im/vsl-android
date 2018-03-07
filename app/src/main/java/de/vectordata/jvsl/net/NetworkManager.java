package de.vectordata.jvsl.net;

import de.vectordata.jvsl.VSLClient;

/**
 * Created by Daniel Lerch on 07.03.2018.
 * © 2018 Daniel Lerch
 */

public class NetworkManager {

    private VSLClient parent;

    public NetworkManager(VSLClient parent) {
        this.parent = parent;
    }

    public void receiveData() {
        CryptoAlgorithm algorithm = CryptoAlgorithm.values()[parent.getChannel().receiveByte()];
        switch (algorithm) {
            case NONE:
                break;
            case RSA_2048_OAEP:
                break;
            case AES_256_CBC_SP:
                break;
            case AES_256_CBC_HMAC_SHA256_MP3:
                break;
            default:
                throw new EnumConstantNotPresentException(CryptoAlgorithm.class, "");
        }
    }
}
