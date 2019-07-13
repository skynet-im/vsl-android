package de.vectordata.libjvsl.net;

import java.util.concurrent.Semaphore;

public class SendItem {

    private final byte[] data;
    private final Semaphore semaphore;

    SendItem(byte[] data) {
        this.data = data;
        this.semaphore = new Semaphore(0);
    }

    byte[] getData() {
        return data;
    }

    void notifySend() {
        semaphore.release();
    }

    public void waitForSend() throws InterruptedException {
        semaphore.acquire();
    }

}
