package com.alperez.geekbooks.crowler.utils;

public class Log {

    private Log() { }

    public static void d(String tag, String msg_template, String... args) {
        if (args.length > 0) {
            String msg = String.format(msg_template, args);
            System.out.println(String.format("%s: %s", tag, msg));
        } else {
            System.out.println(String.format("%s: %s", tag, msg_template));
        }
    }


    public static void d(String tag, String msg_template, Object... args) {
        if (args.length > 0) {
            String msg = String.format(msg_template, args);
            System.out.println(String.format("%s: %s", tag, msg));
        } else {
            System.out.println(String.format("%s: %s", tag, msg_template));
        }
    }

}
