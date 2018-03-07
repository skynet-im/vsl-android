package de.vectordata.jvsl.net.packet.length;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public class ConstantLength extends PacketLength {

    private final long length;

    public ConstantLength(long length) {
        this.length = length;
    }

    @Override
    public LengthType getType() {
        return LengthType.Constant;
    }

    @Override
    public long getLenValue() {
        return length;
    }
}
