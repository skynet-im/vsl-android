package de.vectordata.jvsl.net.packet.length;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public class VariableLength extends PacketLength {
    @Override
    public LengthType getType() {
        return LengthType.UInt32;
    }

    @Override
    public long getLenValue() {
        throw new IllegalStateException("Its variable");
    }
}
