package de.vectordata.libjvsl.fileTransfer;

/**
 * Created by Daniel Lerch on 10.03.2018.
 * © 2018 Daniel Lerch
 */

public enum StreamMode {
    GET_HEADER,
    GET_FILE,
    PUSH_HEADER,
    PUSH_FILE;

    public StreamMode inverse() {
        return StreamMode.values()[(this.ordinal() + 2) % 4];
    }
}
