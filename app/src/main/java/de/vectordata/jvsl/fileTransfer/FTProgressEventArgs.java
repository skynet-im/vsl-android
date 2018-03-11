package de.vectordata.jvsl.fileTransfer;

/**
 * Created by Daniel Lerch on 10.03.2018.
 * © 2018 Daniel Lerch
 */

public class FTProgressEventArgs {

    private float percentage;
    private long transferredBytes;
    private long totalBytes;

    public FTProgressEventArgs(long transferred, long total) {
        if (transferred < 0 && transferred != -1)
            throw new IllegalArgumentException("Invalid transferred value");
        if (total < 0 && total != -1) throw new IllegalArgumentException("Invalid total value");
        if ((transferred == -1 || total == -1) && transferred != total)
            throw new IllegalArgumentException("Whatever");
        this.transferredBytes = transferred;
        this.totalBytes = total;
        if (transferred == -1) percentage = -1f;
        else percentage = (float) transferred / total;
    }

    public float getPercentage() {
        return percentage;
    }

    public long getTransferredBytes() {
        return transferredBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }
}
