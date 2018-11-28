package de.vectordata.libjvsl.fileTransfer;

import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StreamCorruptedException;

import de.vectordata.libjvsl.VSLClient;
import de.vectordata.libjvsl.crypt.ContentAlgorithm;
import de.vectordata.libjvsl.net.Priority;
import de.vectordata.libjvsl.net.SendItem;
import de.vectordata.libjvsl.net.packet.P06Accepted;
import de.vectordata.libjvsl.net.packet.P07OpenFileTransfer;
import de.vectordata.libjvsl.net.packet.P08FileHeader;
import de.vectordata.libjvsl.net.packet.P09FileDataBlock;
import de.vectordata.libjvsl.net.packet.util.ProblemCategory;
import de.vectordata.libjvsl.util.Util;

/**
 * Created by Daniel Lerch on 10.03.2018.
 * © 2018 Daniel Lerch
 */

public class FTSocket {

    private static final String TAG = "FTSocket";

    private VSLClient parent;
    private FTEventArgs currentItem;
    private FTSocketListener listener;

    public FTSocket(VSLClient parent) {
        this.parent = parent;
    }

    public void setListener(FTSocketListener listener) {
        this.listener = listener;
    }

    public void accept(FTEventArgs e, String path) throws IOException {
        accept(e, path, null);
    }

    private void accept(FTEventArgs e, String path, FileMeta meta) throws IOException {
        e.setPath(path);
        e.assign(parent, this);
        e.setFileMeta(meta);

        parent.getManager().sendPacket(new P06Accepted(true, (byte) 7, ProblemCategory.None), Priority.Realtime);
        if (currentItem.getMode() == StreamMode.PUSH_FILE || currentItem.getMode() == StreamMode.PUSH_FILE) {
            if (currentItem.getFileMeta() == null)
                currentItem.setFileMeta(new FileMeta(path, ContentAlgorithm.NONE));
            parent.getManager().sendPacket(new P08FileHeader(currentItem.getFileMeta().getBinaryData(parent.getConnectionVersion())), Priority.Realtime);
        } else if (meta != null) {
            Log.i(TAG, "Got a FileMeta for receiving, ignoring it.");
        }
    }

    public void cancel(FTEventArgs e) throws StreamCorruptedException {
        parent.getManager().sendPacket(new P06Accepted(false, (byte) 7, ProblemCategory.None), Priority.Realtime);
        e.assign(parent, this);
        e.closeStream(false);
        currentItem = null;
    }

    public void downloadHeader(FTEventArgs e) {
        e.assign(parent, this);
        e.setMode(StreamMode.GET_HEADER);
        currentItem = e;
        parent.getManager().sendPacket(new P07OpenFileTransfer(e.getIdentifier(), e.getMode()), Priority.Realtime);
    }

    public void download(FTEventArgs e) {
        e.assign(parent, this);
        e.setMode(StreamMode.GET_FILE);
        currentItem = e;
        parent.getManager().sendPacket(new P07OpenFileTransfer(e.getIdentifier(), e.getMode()), Priority.Realtime);
    }

    public void upload(FTEventArgs e) {
        e.assign(parent, this);
        e.setMode(StreamMode.PUSH_FILE);
        currentItem = e;
        parent.getManager().sendPacket(new P07OpenFileTransfer(e.getIdentifier(), e.getMode()), Priority.Realtime);
    }

    public void doContinue(FTEventArgs e) throws FileNotFoundException {
        if (e.getMode() != StreamMode.GET_FILE)
            throw new IllegalStateException("Cannot continue with of StreamMode != GET_FILE");
        currentItem.openStream();
        parent.getManager().sendPacket(new P06Accepted(true, (byte) 8, ProblemCategory.None), Priority.Realtime);
    }

    public void onPacketReceived(P06Accepted packet) throws IOException {
        if (currentItem == null) throw new IllegalStateException("Invalid packet");
        if (!packet.accepted && packet.relatedPacket == 7) {
            currentItem.closeStream(false);
            currentItem = null;
        } else if (packet.accepted && packet.relatedPacket == 7) {
            if (currentItem.getMode() == StreamMode.PUSH_HEADER || currentItem.getMode() == StreamMode.PUSH_FILE)
                parent.getManager().sendPacket(new P08FileHeader(currentItem.getFileMeta().getBinaryData(parent.getConnectionVersion())), Priority.Realtime);
        } else if (packet.accepted && packet.relatedPacket == 8) {
            currentItem.onFileMetaTransferred();
            if (currentItem.getMode() == StreamMode.PUSH_FILE) {
                currentItem.openStream();
                sendFile();
            } else if (currentItem.getMode() != StreamMode.PUSH_HEADER)
                throw new IllegalStateException("Invalid state (mode) for pid 8");
        } else if (packet.accepted && packet.relatedPacket == 9) {
            throw new UnsupportedOperationException();
        }
    }

    private void sendFile() {
        new Thread(() -> {
            try {
                byte[] buffer = new byte[65536];
                long position = 0;
                int count = 0;
                do {
                    position = currentItem.getPosition();
                    count = currentItem.getHashInputStream().read(buffer, 0, buffer.length);
                    SendItem item = parent.getManager().sendPacket(new P09FileDataBlock(position, Util.takeBytes(buffer, count, 0)), Priority.Background);
                    item.waitForSend();
                    currentItem.onProgress();
                } while (count == buffer.length);
                currentItem.closeStream(true);
                currentItem = null;
            } catch (InterruptedException | IOException e) {
                // TODO: When this fails, the connection is obviously broken and has to be closed.
                Log.e(TAG, "Failed to send file", e);
            }
        }).start();
    }

    public void onPacketReceived(P07OpenFileTransfer packet) {
        if (currentItem == null) {
            FTEventArgs e = new FTEventArgs(packet.getIdentifier(), packet.getStreamMode());
            currentItem = e;
            if (listener != null) listener.onRequest(this, e);
        } else {
            throw new IllegalStateException("Can't run two file transfers at the same time");
        }
    }

    public void onPacketReceived(P08FileHeader packet) {
        if (currentItem == null)
            throw new UnsupportedOperationException("Cannot resume file transfer for the received file header.");
        if (currentItem.getMode() != StreamMode.GET_HEADER && currentItem.getMode() != StreamMode.GET_FILE)
            throw new UnsupportedOperationException("The running file transfer is not supposed to receive a file header.");
        if (currentItem.getFileMeta() != null)
            throw new IllegalStateException("Already received file header");
        currentItem.setFileMeta(new FileMeta(packet.binaryData, (short) parent.getConnectionVersion()));
        currentItem.onFileMetaTransferred();
        if (currentItem.getMode() == StreamMode.GET_HEADER) {
            currentItem.onFinished();
            parent.getManager().sendPacket(new P06Accepted(true, (byte) 8, ProblemCategory.None), Priority.Realtime);
        }
    }

    public void onPacketReceived(P09FileDataBlock packet) throws IOException {
        if (currentItem == null)
            throw new UnsupportedOperationException("Cannot resume file transfer for the received file data block.");
        if (currentItem.getMode() != StreamMode.GET_HEADER && currentItem.getMode() != StreamMode.GET_FILE)
            throw new UnsupportedOperationException("The running file transfer is not supposed to receive a file data block.");
        if (currentItem.getHashOutputStream() == null && currentItem.getHashInputStream() == null)
            throw new IllegalStateException("Stream not initialized");
        currentItem.getHashOutputStream().write(packet.dataBlock, 0, packet.dataBlock.length);
        currentItem.onProgress();
        if (currentItem.getHashOutputStream().getPosition() == currentItem.getFileMeta().getLength()) {
            currentItem.closeStream(true);
            currentItem = null;
        }
        else if (currentItem.getPosition() > currentItem.getFileMeta().getLength()){
            Log.e(TAG, "End of file expected");

            // TODO: Log and close connection
        }
    }
}
