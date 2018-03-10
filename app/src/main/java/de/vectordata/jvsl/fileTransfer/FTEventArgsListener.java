package de.vectordata.jvsl.fileTransfer;

/**
 * Created by Daniel Lerch on 10.03.2018.
 * © 2018 Daniel Lerch
 */

public interface FTEventArgsListener {
    void onCanceled(Object sender);

    void onFinished(Object sender);

    void onProgress(Object sender, FTProgressEventArgs e);

    void onFileMetaReceived(Object sender);
}
