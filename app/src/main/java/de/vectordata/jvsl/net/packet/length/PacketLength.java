package de.vectordata.jvsl.net.packet.length;

/**
 * Created by Twometer on 09.06.2017.
 * (c) 2017 Twometer
 */
public abstract class PacketLength {

    public abstract LengthType getType();

    public abstract long getLenValue();

}
