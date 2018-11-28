package de.vectordata.libjvsl.util.cscompat;

import java.io.File;

/**
 * Created by Twometer on 10.03.2018.
 * (c) 2018 Twometer
 */

public class FileUtils {

    public static String pathCombine(String... paths) {
        StringBuilder pathBuilder = new StringBuilder();
        boolean isFirst = true;
        for (String str : paths) {
            if (!isFirst) pathBuilder.append(File.separator);
            pathBuilder.append(str);
            isFirst = false;
        }
        return pathBuilder.toString();
    }

    public static boolean fileExists(String path) {
        File file = new File(path);
        return file.isFile() && file.exists();
    }

    public static boolean directoryExists(String dir) {
        File file = new File(dir);
        return file.isDirectory() && file.exists();
    }

}
