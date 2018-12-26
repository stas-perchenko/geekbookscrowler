package com.alperez.geekbooks.crowler.utils;

public class TextUtils {

    public static boolean isEmpty(CharSequence cs) {
        return (cs == null) || (cs.length() == 0);
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return (cs != null) && (cs.length() > 0);
    }

    /**
     * Returns true if a and b are equal, including if they are both null.
     * <p><i>Note: In platform versions 1.1 and earlier, this method only worked well if
     * both the arguments were instances of String.</i></p>
     * @param a first CharSequence to check
     * @param b second CharSequence to check
     * @return true if a and b are equal
     */
    public static boolean equals(CharSequence a, CharSequence b) {
        if (a == b) return true;
        int length;
        if (a != null && b != null && (length = a.length()) == b.length()) {
            if (a instanceof String && b instanceof String) {
                return a.equals(b);
            } else {
                for (int i = 0; i < length; i++) {
                    if (a.charAt(i) != b.charAt(i)) return false;
                }
                return true;
            }
        }
        return false;
    }


    public static boolean checkISBN(CharSequence cs) {
        if (isNotEmpty(cs)) {
            String s = ((cs instanceof String) ? (String) cs : cs.toString()).replaceAll("-", "");
            try {
                Long.parseLong(s);
                return s.length() == 13;
            } catch (NumberFormatException e){}
        }
        return false;
    }

    public static boolean checkAsin(CharSequence cs) {
        return (cs != null) && (cs.length() == 10);
    }


    private TextUtils() { }
}
