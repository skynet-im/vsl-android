package de.vectordata.jvsl.util;

/**
 * Created by Daniel Lerch on 07.03.2018.
 * © 2018 Daniel Lerch
 */

public class Constants {

    /**
     * The installed version as ushort.
     */
    public static final int VERSION_NUMBER = 3;
    /**
     * The oldest supported version of VSL.
     */
    public static final int COMPATIBILITY_VERSION = 3;

    /**
     * The count of internal packets. This is important to split the ID range in two spaces.
     */
    public static final int INTERNAL_PACKET_COUNT = 10;

    /**
     * Returns the product version of the current assembly with the specified precision.
     *
     * @param length
     * @return
     */
    public static String getProductVersion(int length) {
        // TODO Get version and build number from manifest
        throw new UnsupportedOperationException();
    }
}
