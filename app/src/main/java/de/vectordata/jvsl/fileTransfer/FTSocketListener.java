package de.vectordata.jvsl.fileTransfer;

/**
 * Created by Daniel Lerch on 10.03.2018.
 * © 2018 Daniel Lerch
 */

interface FTSocketListener {
    void onRequest(Object sender, FTEventArgs e);
}
