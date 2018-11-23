package de.vectordata.jvsl.net;

public class SendItem {

    private final byte[] data;
    private final Object waitHandle;

    public SendItem(byte[] data) {
        this.data = data;
        this.waitHandle = new Object();
    }

    public byte[] getData() {
        return data;
    }

    public void notifySend() {
        synchronized (waitHandle) {
            waitHandle.notifyAll();
        }
    }

    public void waitForSend() throws InterruptedException {
        synchronized (waitHandle) {
            waitHandle.wait();
        }
    }

}
