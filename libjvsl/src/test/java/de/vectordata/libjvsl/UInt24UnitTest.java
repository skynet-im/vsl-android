package de.vectordata.libjvsl;

import org.junit.Test;

import de.vectordata.libjvsl.util.UInt24;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UInt24UnitTest {

    @Test
    public void uint24FromBytes() {
        byte[] referenceBytes = {-35, 4, 0};
        int referenceInt = 1245;
        int i = UInt24.fromByteArray(referenceBytes, UInt24.Endianness.LittleEndian).getValue();
        assertEquals(referenceInt, i);
    }

    @Test
    public void uint24ToBytes() {
        byte[] referenceBytes = {-35, 4, 0};
        int referenceInt = 1245;
        byte[] bytes = new UInt24(referenceInt).toByteArray(UInt24.Endianness.LittleEndian);
        assertArrayEquals(referenceBytes, bytes);
    }
}