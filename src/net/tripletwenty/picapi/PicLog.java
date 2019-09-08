package net.tripletwenty.picapi;

/**
 * Created by bearbob on 07.09.19.
 */
public class PicLog {

    public static void info(String message) {
        System.out.println(message);
    }

    public static void debug(String message) {
        System.out.println(message);
    }

    public static void trace(String message) {
        System.out.println(message);
    }

    public static void error(String message) {
        System.err.println(message);
    }

}
