package de.vectordata.jvsl.fileTransfer;

import de.vectordata.jvsl.VSLClient;

/**
 * Created by Daniel Lerch on 10.03.2018.
 * © 2018 Daniel Lerch
 */

public class FTSocket {

    private VSLClient parent;
    private FTEventArgs currentItem;
    private FTSocketListener listener;

    public FTSocket(VSLClient parent) {
        this.parent = parent;
    }

    public void setListener(FTSocketListener listener) {
        this.listener = listener;
    }
}
