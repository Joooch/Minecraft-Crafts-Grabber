package joch.grabber.utils;

import joch.grabber.Main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {
    static String output = "";

    private static final File configFile = new File(Main.configDir, "logger.txt");

    public static void WriteLine(String str) {
        output += str + "\n";
    }

    public static void WriteLine(Object obj) {
        if (obj == null) {
            return;
        }
        output += obj.toString() + "\n";
    }

    public static void Save() {
        try {
            configFile.createNewFile();

            FileWriter file = new FileWriter(configFile);
            file.write(output);
            file.close();
        } catch (IOException e) {}

        output = "";
    }
}
