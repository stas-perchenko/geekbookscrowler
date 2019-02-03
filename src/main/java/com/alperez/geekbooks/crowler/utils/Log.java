package com.alperez.geekbooks.crowler.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;

public class Log {

    private Log() { }

    public static synchronized void d(String tag, String msg_template, String... args) {
        if (args.length > 0) {
            String msg = String.format(msg_template, args);
            System.out.println(String.format("%s: %s", tag, msg));
        } else {
            System.out.println(String.format("%s: %s", tag, msg_template));
        }
    }


    public static synchronized void d(String tag, String msg_template, Object... args) {
        if (args.length > 0) {
            String msg = String.format(msg_template, args);
            System.out.println(String.format("%s: %s", tag, msg));
        } else {
            System.out.println(String.format("%s: %s", tag, msg_template));
        }
    }

    public static synchronized void d(Collection<LogEntry> logs) {
        for (LogEntry msg : logs) {
            System.out.println(msg);
        }
    }

    public static class LogEntry {
        private final String tag, message;

        public LogEntry(String tag, String message) {
            this.tag = tag;
            this.message = message;
        }

        public LogEntry(String tag, Exception e) {
            this.tag = tag;
            ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
            PrintStream ps = new PrintStream(bos, true);
            e.printStackTrace(ps);
            this.message = bos.toString();
        }

        @Override
        public String toString() {
            return String.format("%s: %s", tag, message);
        }
    }

}
