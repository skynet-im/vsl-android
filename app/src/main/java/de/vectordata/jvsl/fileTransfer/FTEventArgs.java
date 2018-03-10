package de.vectordata.jvsl.fileTransfer;

import de.vectordata.jvsl.VSLClient;

/**
 * Created by Daniel Lerch on 10.03.2018.
 * © 2018 Daniel Lerch
 */

public class FTEventArgs {

    private VSLClient parent;
    private FTSocket socket;
    private FTEventArgsListener listener;

    public FTEventArgs(Identifier identifier){

    }

    protected FTEventArgs(Identifier identifier, StreamMode mode){

    }

    public void setListener(FTEventArgsListener listener) {
        this.listener = listener;
    }
}
